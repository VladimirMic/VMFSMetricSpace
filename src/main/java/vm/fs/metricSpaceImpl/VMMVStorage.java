package vm.fs.metricSpaceImpl;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import vm.fs.FSGlobal;
import vm.metricSpace.AbstractMetricSpace;

/**
 *
 * @author Vlada
 * @param <T>
 */
public class VMMVStorage<T> {

    public static final Logger LOG = Logger.getLogger(VMMVStorage.class.getName());
    private static final String MAP_NAME = "data";
    private static final int BATCH_SIZE = 5000000;

    private final MVStore storage;
    private final String datasetName;
    private final boolean willBeDeleted;
    private final MVMap<Object, T> map;

    public VMMVStorage(String datasetName, boolean createNew) {
        this.datasetName = datasetName;
        this.willBeDeleted = createNew;
        storage = getStorage();
        map = storage.openMap(MAP_NAME);
    }

    private MVStore getStorage() {
        if (datasetName == null) {
            throw new Error("datasetName cannot be null");
        }
        File file = getFile();
        if (!file.exists() && !willBeDeleted) {
            LOG.log(Level.SEVERE, "The file {0} does not exists. Cannot read.", file.getAbsolutePath());
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
        return Collections.unmodifiableMap(map);
    }

    public void insertObjects(Iterator metricObjects, AbstractMetricSpace<T> metricSpace) {
        int batchCount = 0;
        int lastGC = 0;
        while (metricObjects.hasNext()) {
            batchCount++;
            Map<Object, T> batch = loadBatch(metricObjects, metricSpace);
            map.putAll(batch);
            int size = batchCount * BATCH_SIZE;
            LOG.log(Level.INFO, "Stored {0} data objects", size);
            if (size - lastGC >= 500000) {
                lastGC = size;
                System.gc();
            }
        }
        storage.commit();
    }

    public int size() {
        return getKeyValueStorage().size();
    }

    private Map<Object, T> loadBatch(Iterator metricObjects, AbstractMetricSpace<T> metricSpace) {
        Map<Object, T> ret = new TreeMap<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            if (metricObjects.hasNext()) {
                Object next = metricObjects.next();
                String id = metricSpace.getIDOfMetricObject(next).toString();
                T dataOfMetricObject = metricSpace.getDataOfMetricObject(next);
                ret.put(id, dataOfMetricObject);
            }
        }
        return ret;
    }

}
