package vm.fs.metricSpaceImpl;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.dataToStringConvertors.MetricObjectDataToStringInterface;

/**
 *
 * @author xmic
 */
public class H5MetricSpacesStorage extends FSMetricSpacesStorage<float[]> {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final Logger LOG = Logger.getLogger(H5MetricSpacesStorage.class.getName());

    public H5MetricSpacesStorage(AbstractMetricSpace<float[]> metricSpace, MetricObjectDataToStringInterface dataSerializator) {
        super(metricSpace, dataSerializator);
    }

    public H5MetricSpacesStorage(MetricObjectDataToStringInterface dataSerializator) {
        super(dataSerializator);
    }

    @Override
    public Iterator<Object> getObjectsFromDataset(String datasetName, Object... params) {
        params = Tools.concatArrays(params, new Object[]{""});
        return getIteratorOfObjects(FSGlobal.DATASET_FOLDER, datasetName, params);
    }

    @Override
    public List<Object> getPivots(String pivotSetName, Object... params) {
        params = Tools.concatArrays(params, new Object[]{"P"});
        Iterator<Object> it = getIteratorOfObjects(FSGlobal.PIVOT_FOLDER, pivotSetName, params);
        return Tools.getObjectsFromIterator(it);
    }

    @Override
    public List<Object> getQueryObjects(String querySetName, Object... params) {
        params = Tools.concatArrays(params, new Object[]{"Q"});
        Iterator<Object> it = getIteratorOfObjects(FSGlobal.QUERY_FOLDER, querySetName, params);
        return Tools.getObjectsFromIterator(it);
    }

    protected Iterator<Object> getIteratorOfObjects(File f, Object... params) {
        List<Object> listOfParams = Tools.arrayToList(params);
        if (params.length > 0 && listOfParams.contains("P")) {
            Iterator<Object> it = super.getIteratorOfObjects(f, params);
//            List<Object> objs = Tools.getObjectsFromIterator(it);
//            List<Object> ret = new ArrayList<>();
//            for (Object obj : objs) {
//                AbstractMap.Entry entry = (AbstractMap.Entry) obj;
//                String key = entry.getKey().toString();
//                AbstractMap.Entry entryP;
//                if (!key.startsWith("P")) {
//                    entryP = new AbstractMap.SimpleEntry("P" + entry.getKey().toString(), entry.getValue());
//                } else {
//                    entryP = entry;
//                }
//                ret.add(entry);
//            }
            return it;
        }
        HdfFile hdfFile = new HdfFile(f.toPath());
        Node node = hdfFile.iterator().next();
        String name = node.getName();
        LOG.log(Level.INFO, "Returning data from the dataset (group) {0} in the file {1}", new Object[]{name, f.getName()});
        Dataset dataset = hdfFile.getDatasetByPath(name);
        int count = params.length > 0 && params[0] instanceof Integer ? (int) params[0] : Integer.MAX_VALUE;
        if (count < 0) {
            count = Integer.MAX_VALUE;
        }
        String prefix = params[params.length - 1].toString();
        return new H5MetricObjectFileIterator(hdfFile, dataset, prefix, count);
    }

    @Override
    protected void storeMetricObject(Object metricObject, OutputStream datasetOutputStream, Object... additionalParamsToStoreWithNewDataset) throws IOException {
        super.storeMetricObject(metricObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
    }

    private class H5MetricObjectFileIterator implements Iterator<Object> {

        protected AbstractMap.SimpleEntry<String, float[]> nextObject;
        protected AbstractMap.SimpleEntry<String, float[]> currentObject;

        private final HdfFile hdfFile;
        private final Dataset dataset;
        private final int maxCount;
        private final int[] vectorDimensions;
        private final String prefixFoIDs;
        private long[] counter;

        private H5MetricObjectFileIterator(HdfFile hdfFile, Dataset dataset, String prefix, int maxCount) {
            this.hdfFile = hdfFile;
            this.dataset = dataset;
            int[] storageDimensions = dataset.getDimensions();
            this.maxCount = Math.min(maxCount, storageDimensions[0]);
            this.vectorDimensions = new int[]{1, storageDimensions[1]};
            this.prefixFoIDs = prefix;
            counter = new long[]{0, 0};
            nextObject = nextStreamObject();
        }

        @Override
        public boolean hasNext() {
            boolean ret = nextObject != null;
            if (!ret) {
                hdfFile.close();
            }
            return ret;
        }

        @Override
        public AbstractMap.SimpleEntry<String, float[]> next() {
            if (nextObject == null) {
                throw new NoSuchElementException("No more objects in the stream");
            }
            currentObject = nextObject;
            nextObject = nextStreamObject();
            return currentObject;
        }

        private AbstractMap.SimpleEntry<String, float[]> nextStreamObject() {
            if (counter[0] >= maxCount) {
                return null;
            }
            float[][] dataBuffer = (float[][]) dataset.getData(counter, vectorDimensions);
            String id = prefixFoIDs + (counter[0] + 1);
            AbstractMap.SimpleEntry<String, float[]> entry = new AbstractMap.SimpleEntry<>(id, dataBuffer[0]);
            counter[0]++;
            return entry;
        }
    }

}
