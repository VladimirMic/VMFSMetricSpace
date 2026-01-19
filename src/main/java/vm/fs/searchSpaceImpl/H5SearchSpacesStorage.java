package vm.fs.searchSpaceImpl;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.DataTypeConvertor;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.data.toStringConvertors.SearchObjectDataToStringInterface;
import vm.searchSpace.distance.AbstractDistanceFunction;

/**
 *
 * @author xmic
 * @param <T>
 */
public class H5SearchSpacesStorage<T> extends FSSearchSpacesStorage<T> {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public final Logger LOG = Logger.getLogger(H5SearchSpacesStorage.class.getName());

    public H5SearchSpacesStorage(AbstractSearchSpace<T> searchSpace, SearchObjectDataToStringInterface dataSerializator) {
        super(searchSpace, dataSerializator);
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

    @Override
    public Iterator<Object> getIteratorOfObjects(File f, Object... params) {
        List<Object> listOfParams = DataTypeConvertor.arrayToList(params);
        if (params.length > 0 && listOfParams.contains("P")) {
            Iterator<Object> it = super.getIteratorOfObjects(f, params);
            return it;
        }
        if (!f.exists()) {
            LOG.log(Level.SEVERE, "The file does not exists!");
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
        Class clazz = null;
        for (Object param : params) {
            if (param instanceof AbstractDistanceFunction c) {
                clazz = c.getClassOfComparedData();
            }
        }
        return new H5SearchObjectFileIterator(hdfFile, dataset, prefix, count, clazz);
    }

    @Override
    protected void storeSearchObject(Object searchObject, OutputStream datasetOutputStream, Object... additionalParamsToStoreWithNewDataset) throws IOException {
        super.storeSearchObject(searchObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
    }

    public HdfFile getHDFFile(String datasetName) {
        File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName, false);
        return new HdfFile(f.toPath());
    }

    public Map<Comparable, Object> getAsMap(String datasetName) {
        HdfFile hdfFile = getHDFFile(datasetName);
        Node node = hdfFile.iterator().next();
        String name = node.getName();
        LOG.log(Level.INFO, "Returning data from the dataset (group) {0} in the file {1}", new Object[]{name, hdfFile.getName()});
        Dataset dataset = hdfFile.getDatasetByPath(name);
        VMH5StorageAsMap ret = new VMH5StorageAsMap(dataset);
        return ret;
    }

    private class H5SearchObjectFileIterator implements Iterator<Object> {

        protected AbstractMap.SimpleEntry<String, T> nextObject;
        protected AbstractMap.SimpleEntry<String, T> currentObject;

        private final HdfFile hdfFile;
        private final Dataset dataset;
        private final int maxCount;
        private final int[] vectorDimensions;
        private final String prefixFoIDs;
        private final long[] counter;
        private final Class clazz;

        private H5SearchObjectFileIterator(HdfFile hdfFile, Dataset dataset, String prefix, int maxCount, Class clazz) {
            this.hdfFile = hdfFile;
            this.dataset = dataset;
            int[] storageDimensions = dataset.getDimensions();
            this.maxCount = Math.min(maxCount, storageDimensions[0]);
            this.vectorDimensions = new int[]{1, storageDimensions[1]};
            this.prefixFoIDs = prefix;
            counter = new long[]{0, 0};
            this.clazz = clazz;
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
        public AbstractMap.SimpleEntry<String, T> next() {
            if (nextObject == null) {
                throw new NoSuchElementException("No more objects in the stream");
            }
            currentObject = nextObject;
            nextObject = nextStreamObject();
            return currentObject;
        }

        private AbstractMap.SimpleEntry<String, T> nextStreamObject() {
            if (counter[0] >= maxCount) {
                return null;
            }
            T[] dataBuffer = (T[]) dataset.getData(counter, vectorDimensions);
            T data = null;
            if (clazz == null) {
                throw new IllegalArgumentException("Did you provided distance function as a param?");
            }
            if (clazz.equals(float[].class)) {
                data = (T) DataTypeConvertor.arrayToFloatArray(dataBuffer[0]);
            }
            if (clazz.equals(double[].class)) {
                data = (T) DataTypeConvertor.arrayToDoubleArray(dataBuffer[0]);
            }
            if (clazz.equals(int[].class)) {
                data = (T) DataTypeConvertor.arrayToIntArray(dataBuffer[0]);
            }
            if (data == null) {
                throw new IllegalArgumentException("Unknown class: " + clazz.getName());
            }
            String id = prefixFoIDs + (counter[0] + 1);
            AbstractMap.SimpleEntry<String, T> entry = new AbstractMap.SimpleEntry<>(id, data);
            counter[0]++;
            return entry;
        }
    }

    private class VMH5StorageAsMap implements Map<Comparable, Object> {

        private final Dataset dataset;
        private final int[] dimensions;
        private final int[] vectorDimensions;

        public VMH5StorageAsMap(Dataset dataset) {
            this.dataset = dataset;
            dimensions = dataset.getDimensions();
            vectorDimensions = new int[]{1, dimensions[1]};
        }

        @Override
        public int size() {
            return dimensions[0];
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof Long cast) {
                return cast <= size();
            }
            if (key instanceof Integer cast) {
                return cast <= size();
            }
            if (key instanceof String) {
                int id = Integer.parseInt(key.toString());
                return id <= size();
            }
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public float[] get(Object key) {
            Long keyLong = Tools.getDigitsFromString(key.toString());
            if (containsKey(keyLong)) {
                keyLong -= 1;
                long[] shift = new long[]{keyLong, 0};
                Object dataBuffer = dataset.getData(shift, vectorDimensions);
                if (dataBuffer instanceof float[][] c) {
                    return c[0];
                }
                if (dataBuffer instanceof double[][] c) {
                    return DataTypeConvertor.doublesToFloats(c[0]);
                }
                if (dataBuffer instanceof int[][] c) {
                    return DataTypeConvertor.intsArrayToFloats(c[0]);
                }
                if (dataBuffer instanceof long[][] c) {
                    return DataTypeConvertor.longsArrayToFloats(c[0]);
                }
            }
            return null;
        }

        @Override
        public Object put(Comparable key, Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void putAll(java.util.Map m) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set keySet() {
            System.out.println("Minimum is 0, maximum is " + size() + ". Use row index as the key");
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Collection values() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Set entrySet() {
            Set<Map.Entry<Comparable, Object>> ret = new HashSet<>();
            for (int i = 1; i <= size(); i++) {
                float[] v = get(i);
                ret.add(new AbstractMap.SimpleEntry<>(Integer.toString(i), v));
            }
            return ret;
        }
    };

}
