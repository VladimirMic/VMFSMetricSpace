package vm.fs.metricSpaceImpl;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private final MVStore storage;

    public VMMVStorage(String datasetName, boolean createNew) {
        storage = getStorage(datasetName, createNew);
    }

    private MVStore getStorage(String datasetName, boolean willBeDeleted) {
        if (datasetName == null) {
            throw new Error("datasetName cannot be null");
        }
        File file = getFile(datasetName, willBeDeleted);
        if (!file.exists() && !willBeDeleted) {
            LOG.log(Level.SEVERE, "The file {0} does not exists. Cannot read.", file.getAbsolutePath());
        }
        MVStore.Builder ret = new MVStore.Builder().fileName(file.getAbsolutePath()).compress();
        if (!willBeDeleted) {
            ret.readOnly();
        }
        return ret.open();
    }

    private static File getFile(String datasetName, boolean willBeDeleted) {
        File ret = new File(FSGlobal.DATASET_MVSTORAGE_FOLDER, datasetName);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

    public Map<Object, T> getKeyValueStorage() {
        return storage.openMap(MAP_NAME);
    }

    public void insertObjects(Iterator metricObjects, AbstractMetricSpace<T> metricSpace) {
        Map<Object, T> map = getKeyValueStorage();
        for (int i = 1; metricObjects.hasNext(); i++) {
            Object next = metricObjects.next();
            String id = metricSpace.getIDOfMetricObject(next).toString();
            T dataOfMetricObject = metricSpace.getDataOfMetricObject(next);
            map.put(id, dataOfMetricObject);
            if (i % 50000 == 0) {
                LOG.log(Level.INFO, "Stored {0} data objects", i);
            }
            if (i % 500000 == 0) {
                System.gc();
            }
        }
        storage.commit();
    }

    public int size() {
        return getKeyValueStorage().size();
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        super.finalize();
        storage.close();
    }

}