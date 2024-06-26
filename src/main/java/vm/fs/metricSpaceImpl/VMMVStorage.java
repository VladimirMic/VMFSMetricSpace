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
        int stored;
        Iterator metricObjects = dataset.getMetricObjectsFromDataset();
        AbstractMetricSpace<T> metricSpace = dataset.getMetricSpace();
        SortedMap<Object, T> batch = loadBatch(metricObjects, metricSpace);
        int batchSize = batch.size();
        if (metricObjects.hasNext()) {
            SortedSet allSortedIDs = new TreeSet<>(ToolsMetricDomain.getIDs(dataset.getMetricObjectsFromDataset(), metricSpace));
            LOG.log(Level.INFO, "Loaded and sorted {0} IDs", allSortedIDs.size());
            SortedMap<Object, T> prefixToStore = new TreeMap<>();
            stored = storePrefix(allSortedIDs, batch, 0);
            // process wisely, search for first
            while (!allSortedIDs.isEmpty()) {
                LOG.log(Level.INFO, "Remaining to store {0} objects", allSortedIDs.size());
                metricObjects = dataset.getMetricObjectsFromDataset();
                System.gc();
                stored += performPrefixBatch(metricObjects, metricSpace, prefixToStore, allSortedIDs, batchSize);
            }
        } else {
            map.putAll(batch);
            stored = batch.size();
        }
        LOG.log(Level.INFO, "Stored {0} objects", stored);
        System.gc();
        storage.commit();
    }

    public int size() {
        return getKeyValueStorage().size();
    }

    private SortedMap<Object, T> loadBatch(Iterator metricObjects, AbstractMetricSpace<T> metricSpace) {
        SortedMap<Object, T> ret = new TreeMap<>();
        List<Object> objectsFromIterator = vm.datatools.Tools.getObjectsFromIterator(metricObjects);
        for (int i = 0; i < objectsFromIterator.size(); i++) {
            Object next = objectsFromIterator.get(i);
            String id = metricSpace.getIDOfMetricObject(next).toString();
            T dataOfMetricObject = metricSpace.getDataOfMetricObject(next);
            ret.put(id, dataOfMetricObject);
        }
        LOG.log(Level.INFO, "Loaded batch of {0} objects", ret.size());
        return ret;
    }

    private int storePrefix(SortedSet allSortedIDs, SortedMap<Object, T> batch, int counter) {
        Object firstID = allSortedIDs.first();
        Object batchID = batch.firstKey();
        while (firstID.equals(batchID)) {
            map.put(firstID, batch.get(batchID));
            allSortedIDs.remove(firstID);
            batch.remove(firstID);
            firstID = allSortedIDs.first();
            batchID = batch.firstKey();
            counter++;
            if (counter % 100000 == 0) {
                LOG.log(Level.INFO, "Stored {0} data objects", counter);
            }
        }
        return counter;
    }

    private int performPrefixBatch(Iterator metricObjects, AbstractMetricSpace<T> metricSpace, SortedMap<Object, T> prefixOfIDsToStore, SortedSet allSortedIDs, int batchSize) {
        prefixOfIDsToStore.clear();
        int stored = 0;
        SortedSet batchOfIDs = new TreeSet();
        getAndRemovePrefix(allSortedIDs, batchOfIDs, batchSize);
        while (metricObjects.hasNext() && !allSortedIDs.isEmpty()) {
            Object o = metricObjects.next();
            Object id = metricSpace.getIDOfMetricObject(o);
            if (batchOfIDs.contains(id)) {
                prefixOfIDsToStore.put(id, metricSpace.getDataOfMetricObject(o));
                if (prefixOfIDsToStore.size() == 100) {
                    stored += storePrefix(batchOfIDs, prefixOfIDsToStore, stored);
                    getAndRemovePrefix(allSortedIDs, batchOfIDs, batchSize);
                }
            }
        }
        stored += storePrefix(batchOfIDs, prefixOfIDsToStore, stored);
        return stored;
    }

    private void getAndRemovePrefix(SortedSet allSortedIDs, SortedSet batchOfIDs, int batchSize) {
        Iterator it = allSortedIDs.iterator();
        while (batchOfIDs.size() != batchSize && it.hasNext()) {
            Object id = it.next();
            batchOfIDs.add(id);
        }
        allSortedIDs.removeAll(batchOfIDs);
    }

    public void close() {
        storage.close();
    }
}
