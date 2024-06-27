package vm.fs.metricSpaceImpl;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import vm.fs.FSGlobal;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.ToolsMetricDomain;

/**
 *
 * @author Vlada
 * @param <T>
 */
public class VMMVStorage<T> {

    public static final Logger LOG = Logger.getLogger(VMMVStorage.class.getName());
    private static final String MAP_NAME = "data";

    private final MVStore storage;
    private final String datasetName;
    private final boolean willBeDeleted;
    private final MVMap<Object, T> map;

    public VMMVStorage(String datasetName, boolean createNew) {
        this.datasetName = datasetName;
        this.willBeDeleted = createNew;
        storage = getStorage();
        if (storage != null) {
            map = storage.openMap(MAP_NAME);
        } else {
            map = null;
        }
    }

    private MVStore getStorage() {
        if (datasetName == null) {
            throw new Error("datasetName cannot be null");
        }
        File file = getFile();
        if (!file.exists() && !willBeDeleted) {
            LOG.log(Level.SEVERE, "The file {0} does not exists. Cannot read.", file.getAbsolutePath());
            return null;
        }
        MVStore.Builder ret = new MVStore.Builder().fileName(file.getAbsolutePath()).compressHigh();
        if (!willBeDeleted) {
            ret = ret.readOnly();
            ret = ret.autoCommitDisabled();
        }
        return ret.open();
    }

    public static boolean exists(String datasetName) {
        File ret = new File(FSGlobal.DATASET_MVSTORAGE_FOLDER, datasetName);
        ret = FSGlobal.checkFileExistence(ret, false);
        return ret.exists();
    }

    public static void delete(String datasetName) {
        File ret = new File(FSGlobal.DATASET_MVSTORAGE_FOLDER, datasetName);
        ret = FSGlobal.checkFileExistence(ret, true);
        ret.delete();

    }

    public File getFile() {
        File ret = new File(FSGlobal.DATASET_MVSTORAGE_FOLDER, datasetName);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

    public Map<Object, T> getKeyValueStorage() {
        if (map == null) {
            return null;
        }
        return Collections.unmodifiableMap(map);
    }

    public void insertObjects(Dataset<T> dataset) {
        Iterator metricObjects = dataset.getMetricObjectsFromDataset();
        AbstractMetricSpace<T> metricSpace = dataset.getMetricSpace();
        SortedMap<Comparable, T> priorityObjects = loadBatch(metricObjects, metricSpace);
        int batchSize = priorityObjects.size();
        if (metricObjects.hasNext()) {
            SortedSet allSortedIDs = new TreeSet<>(ToolsMetricDomain.getIDs(dataset.getMetricObjectsFromDataset(), metricSpace));
            LOG.log(Level.INFO, "Loaded and sorted {0} IDs", allSortedIDs.size());
            SortedSet<Comparable> batchOfIDs = new TreeSet();
            storePrefix(allSortedIDs, priorityObjects);
            int goArounds = 0;
            // process wisely, search for first
            while (!allSortedIDs.isEmpty() || !batchOfIDs.isEmpty()) {
                goArounds++;
                System.gc();
                performPrefixBatch(metricObjects, metricSpace, batchOfIDs, priorityObjects, allSortedIDs, batchSize, goArounds);
                metricObjects = dataset.getMetricObjectsFromDataset();
            }
        } else {
            map.putAll(priorityObjects);
        }
        LOG.log(Level.INFO, "Stored {0} objects", map.size());
        System.gc();
        storage.commit();
    }

    public int size() {
        return getKeyValueStorage().size();
    }

    private SortedMap<Comparable, T> loadBatch(Iterator metricObjects, AbstractMetricSpace<T> metricSpace) {
        SortedMap<Comparable, T> ret = new TreeMap<>();
        List<Object> objectsFromIterator = vm.datatools.Tools.getObjectsFromIterator(66f, metricObjects);
        for (int i = 0; i < objectsFromIterator.size(); i++) {
            Object next = objectsFromIterator.get(i);
            Comparable id = metricSpace.getIDOfMetricObject(next);
            T dataOfMetricObject = metricSpace.getDataOfMetricObject(next);
            ret.put(id, dataOfMetricObject);
        }
        LOG.log(Level.INFO, "Loaded batch of {0} objects", ret.size());
        return ret;
    }

    private boolean storePrefix(SortedSet<Comparable> topIDs, SortedMap<Comparable, T> batch) {
        boolean ret = false;
        if (topIDs.isEmpty() || batch.isEmpty()) {
            return ret;
        }
        Comparable firstID = topIDs.first();
        Comparable batchID = batch.firstKey();
//        LOG.log(Level.INFO, "firstID: {0}, batchID: {1}, comparison {2}", new Object[]{firstID, batchID, firstID.toString().compareTo(batchID.toString())});
        while (firstID.equals(batchID)) {
            map.put(firstID, batch.get(batchID));
            ret = true;
            topIDs.remove(firstID);
            batch.remove(firstID);
            if (topIDs.isEmpty() || batch.isEmpty()) {
                return ret;
            }
            firstID = topIDs.first();
            batchID = batch.firstKey();
        }
        return ret;
    }

    private void performPrefixBatch(Iterator metricObjects, AbstractMetricSpace<T> metricSpace, SortedSet<Comparable> batchOfIDs, SortedMap<Comparable, T> priorityObjects, SortedSet allSortedIDs, int batchSize, int goArounds) {
        createPriorityPrefix(allSortedIDs, batchOfIDs, batchSize);
        int itCounter = 0;
        while (metricObjects.hasNext() && (!batchOfIDs.isEmpty() || !allSortedIDs.isEmpty())) {
            itCounter++;
            Object o = metricObjects.next();
            Comparable id = metricSpace.getIDOfMetricObject(o);
            T data = metricSpace.getDataOfMetricObject(o);
            boolean add = (priorityObjects.size() < batchSize || id.compareTo(priorityObjects.lastKey()) < 0)
                    && (id.compareTo(batchOfIDs.first()) >= 0);
            if (add) {
                priorityObjects.put(id, data);
//                LOG.log(Level.INFO, "Adding {0}, waiting for {1}, priority size {2}, first obj in priority: {3}, comparison: {4}", new Object[]{id, batchOfIDs.first(), priorityObjects.size(), priorityObjects.firstKey(), priorityObjects.firstKey().compareTo(batchOfIDs.first())});
                boolean check = (priorityObjects.size() >= batchSize * 0.7f && priorityObjects.size() % 100 == 0) || allSortedIDs.isEmpty();
                while (check) {
                    check = storePrefix(batchOfIDs, priorityObjects);
                    createPriorityPrefix(allSortedIDs, batchOfIDs, batchSize);
                }
            }
            while (priorityObjects.size() > batchSize) {
                priorityObjects.remove(priorityObjects.lastKey());
            }
            if (itCounter % 100000 == 0) {
                LOG.log(Level.INFO, "Pass {4}, read {0} objects, stored {5}, remain {1}, cached: {2} out of capacity {3}", new Object[]{itCounter, allSortedIDs.size(), priorityObjects.size(), batchOfIDs.size(), goArounds, map.size()});
            }
        }
        storePrefix(batchOfIDs, priorityObjects);
    }

    private void createPriorityPrefix(SortedSet allSortedIDs, SortedSet batchOfIDs, int batchSize) {
        Iterator it = allSortedIDs.iterator();
        Object end = null;
        int endIdx = batchSize - batchOfIDs.size();
        if (endIdx == 0) {
//            LOG.log(Level.INFO, "allSortedIDs size {0}, batchOfIDs size {1}, batchsize {2}, endidx: {3}", new Object[]{allSortedIDs.size(), batchOfIDs.size(), batchSize, endIdx});
            return;
        }
        for (int i = 0; i < endIdx && it.hasNext(); i++) {
            end = it.next();
        }
        if (end == null) {
            return;
        }
        SortedSet remove = new TreeSet<>(allSortedIDs.headSet(end));
        remove.add(end);
        batchOfIDs.addAll(remove);
        allSortedIDs.removeAll(remove);
    }

    public void close() {
        storage.close();
    }
}
