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

    private File getFile() {
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

    public void insertObjects(Dataset dataset) {
        Iterator metricObjects = dataset.getMetricObjectsFromDataset();
        AbstractMetricSpace<T> metricSpace = dataset.getMetricSpace();
        SortedMap<Object, T> batch = loadBatch(metricObjects, metricSpace);
        int batchSize = batch.size();
        if (metricObjects.hasNext()) {
            SortedSet allSortedIDs = new TreeSet<>(ToolsMetricDomain.getIDs(dataset.getMetricObjectsFromDataset(), metricSpace));
            LOG.log(Level.INFO, "Loaded and sorted {0} IDs", allSortedIDs.size());
            SortedMap<Object, T> prefixToStore = new TreeMap<>();
            SortedSet batchOfIDs = new TreeSet();
            storePrefix(allSortedIDs, batch);
            int goArounds = 0;
            // process wisely, search for first
            while (!allSortedIDs.isEmpty()) {
                goArounds++;
                metricObjects = dataset.getMetricObjectsFromDataset();
                System.gc();
                performPrefixBatch(metricObjects, metricSpace, batchOfIDs, prefixToStore, allSortedIDs, batchSize, goArounds);
            }
        } else {
            map.putAll(batch);
        }
        LOG.log(Level.INFO, "Stored {0} objects", map.size());
        System.gc();
        storage.commit();
    }

    public int size() {
        return getKeyValueStorage().size();
    }

    private SortedMap<Object, T> loadBatch(Iterator metricObjects, AbstractMetricSpace<T> metricSpace) {
        SortedMap<Object, T> ret = new TreeMap<>();
        List<Object> objectsFromIterator = vm.datatools.Tools.getObjectsFromIterator(50f, metricObjects);
        for (int i = 0; i < objectsFromIterator.size(); i++) {
            Object next = objectsFromIterator.get(i);
            String id = metricSpace.getIDOfMetricObject(next).toString();
            T dataOfMetricObject = metricSpace.getDataOfMetricObject(next);
            ret.put(id, dataOfMetricObject);
        }
        LOG.log(Level.INFO, "Loaded batch of {0} objects", ret.size());
        return ret;
    }

    private void storePrefix(SortedSet allSortedIDs, SortedMap<Object, T> batch) {
        Object firstID = allSortedIDs.first();
        Object batchID = batch.firstKey();
//        LOG.log(Level.INFO, "firstID: {0}, batchID: {1}, comparison {2}", new Object[]{firstID, batchID, firstID.toString().compareTo(batchID.toString())});
        while (firstID.equals(batchID)) {
            map.put(firstID, batch.get(batchID));
            allSortedIDs.remove(firstID);
            batch.remove(firstID);
            if (allSortedIDs.isEmpty() || batch.isEmpty()) {
                return;
            }
            firstID = allSortedIDs.first();
            batchID = batch.firstKey();
        }
    }

    private void performPrefixBatch(Iterator metricObjects, AbstractMetricSpace<T> metricSpace, SortedSet batchOfIDs, SortedMap<Object, T> prefixOfIDsToStore, SortedSet allSortedIDs, int batchSize, int goArounds) {
        getAndRemovePrefix(allSortedIDs, batchOfIDs, batchSize);
        int couter = 0;
        while (metricObjects.hasNext() && !allSortedIDs.isEmpty()) {
            couter++;
            Object o = metricObjects.next();
            Object id = metricSpace.getIDOfMetricObject(o);
            if (batchOfIDs.contains(id)) {
                prefixOfIDsToStore.put(id, metricSpace.getDataOfMetricObject(o));
                if (prefixOfIDsToStore.size() % 100 == 0) {
                    storePrefix(batchOfIDs, prefixOfIDsToStore);
                    getAndRemovePrefix(allSortedIDs, batchOfIDs, batchSize);
                }
            }
            if (couter % 100000 == 0) {
                LOG.log(Level.INFO, "Pass {4}, read {0} objects, stored {5}, remain {1}, loaded from the first: {2} out of {3}", new Object[]{couter, allSortedIDs.size(), prefixOfIDsToStore.size(), batchOfIDs.size(), goArounds, map.size()});
            }
        }
        storePrefix(batchOfIDs, prefixOfIDsToStore);
    }

    private void getAndRemovePrefix(SortedSet allSortedIDs, SortedSet batchOfIDs, int batchSize) {
        Iterator it = allSortedIDs.iterator();
        SortedSet remove = new TreeSet();
        while (batchOfIDs.size() != batchSize && it.hasNext()) {
            Object id = it.next();
            batchOfIDs.add(id);
            remove.add(id);
        }
        allSortedIDs.removeAll(remove);
    }

    public void close() {
        storage.close();
    }
}
