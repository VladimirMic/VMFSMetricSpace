package vm.fs.metricSpaceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.metricspace.AbstractMetricSpace;
import vm.metricspace.MetricSpacesStorageInterface;
import vm.metricspace.dataToStringConvertors.MetricObjectDataToStringInterface;

/**
 *
 * @author xmic
 * @param <T>
 */
public class FSMetricSpacesStorage<T> extends MetricSpacesStorageInterface {

    public static final Logger LOG = Logger.getLogger(FSMetricSpacesStorage.class.getName());

    private final AbstractMetricSpace metricSpace;
    private final MetricObjectDataToStringInterface<T> dataSerializator;

    /**
     * Methods metricSpace.getIDOfMetricObject and
     * metricSpace.getDataOfMetricObject are used to store the metric objects in
     * the "key-value" format
     *
     * @param metricSpace
     * @param dataSerializator
     */
    public FSMetricSpacesStorage(AbstractMetricSpace metricSpace, MetricObjectDataToStringInterface<T> dataSerializator) {
        this.metricSpace = metricSpace;
        this.dataSerializator = dataSerializator;
    }

    @Override
    public Iterator<Object> getMetricObjectsFromDataset(String datasetName, Object... params) {
        return getIteratorOfObjects(FSGlobal.DATASET_FOLDER, datasetName, params);
    }

    @Override
    public List<Object> getMetricPivots(String pivotSetName, Object... params) {
        Iterator<Object> it = getIteratorOfObjects(FSGlobal.PIVOT_FOLDER, pivotSetName, params);
        return Tools.getObjectsFromIterator(it);
    }

    @Override
    public List<Object> getMetricQueryObjects(String querySetName, Object... params) {
        Iterator<Object> it = getIteratorOfObjects(FSGlobal.QUERY_FOLDER, querySetName, params);
        return Tools.getObjectsFromIterator(it);
    }

    private Iterator<Object> getIteratorOfObjects(String folder, String file, Object... params) {
        File f = getFileForObjects(folder, file);
        if (!f.exists()) {
            throw new IllegalArgumentException("No file for objects " + f.getAbsolutePath() + " exists");
        }
        int count = params.length > 0 ? (int) params[0] : Integer.MAX_VALUE;
        if (count < 0) {
            count = Integer.MAX_VALUE;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
            return new MetricObjectFileIterator(br, count);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param metricObject
     * @param datasetName
     * @param additionalParamsToStoreWithNewDataset zero must be the instance of
     * the metric space which is used to extract the ID of the metric object and
     * its data
     */
    @Override
    public void storeMetricObjectToDataset(Object metricObject, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        GZIPOutputStream datasetOutputStream = null;
        try {
            File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, true), true);
            storeMetricObjectToDataset(metricObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                datasetOutputStream.flush();
                datasetOutputStream.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void storeMetricObjectToDataset(Object metricObject, GZIPOutputStream datasetOutputStream, Object... additionalParamsToStoreWithNewDataset) throws IOException {
        String id = metricSpace.getIDOfMetricObject(metricObject).toString();
        String data = dataSerializator.metricObjectDataToString((T) metricSpace.getDataOfMetricObject(metricObject, additionalParamsToExtractDataFromMetricObject));
        datasetOutputStream.write(id.getBytes());
        datasetOutputStream.write(':');
        datasetOutputStream.write(data.getBytes());
        datasetOutputStream.write('\n');
    }

    @Override
    public synchronized int storeMetricObjectsToDataset(Iterator<Object> it, int count, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        GZIPOutputStream datasetOutputStream = null;
        int ret = 0;
        try {
            for (ret = 1; it.hasNext(); ret++) {
                Object metricObject = it.next();
                storeMetricObjectToDataset(metricObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
                if (ret % 10000 == 0) {
                    LOG.log(Level.INFO, "Stored {0} metric objects", ret);
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                datasetOutputStream.flush();
                datasetOutputStream.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }

    private File getFileForObjects(String folder, String fileName) {
        File f = new File(folder);
        f.mkdirs();
        return new File(f, fileName + ".gz");
    }

    private DBStoreMetricPivots insertPivotsManager = null;

    /**
     *
     * @param pivots metric objects to store
     * @param pivotSetName identifier of the pivot set
     * @param additionalParamsToStoreWithNewPivotSet zero must be the instance
     * of the metric space which is used to extract the ID of the metric object
     * and its data
     */
    @Override
    public void storeMetricPivots(List<Object> pivots, String pivotSetName, Object... additionalParamsToStoreWithNewPivotSet) {
        if (insertPivotsManager == null) {
            try {
                dbTableForPivotsAndQueries.checkAndAskForPivotSetExistence(pivotSetName, additionalParamsToStoreWithNewPivotSet);
                insertPivotsManager = new DBStoreMetricPivots();
            } catch (SQLException ex) {
                Logger.getLogger(DBMetricSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        for (Object pivot : pivots) {
            insertPivotsManager.storeMetricObject(pivot, pivotSetName, additionalParamsToStoreWithNewPivotSet);
        }
    }

    private DBStoreMetricQueries insertQueriesManager = null;

    /**
     *
     * @param queryObjs
     * @param querySetName
     * @param additionalParamsToStoreWithNewQuerySet zero must be the instance
     * of the metric space which is used to extract the ID of the metric object
     * and its data
     */
    @Override
    public void storeQueryObjects(List<Object> queryObjs, String querySetName, Object... additionalParamsToStoreWithNewQuerySet) {
        if (insertQueriesManager == null) {
            try {
                dbTableForPivotsAndQueries.checkAndAskForQuerySetExistence(querySetName, additionalParamsToStoreWithNewQuerySet);
                insertQueriesManager = new DBStoreMetricQueries();
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, null, ex);
                return;
            }
        }
        for (Object queryObj : queryObjs) {
            insertQueriesManager.storeMetricObject(queryObj, querySetName, additionalParamsToStoreWithNewQuerySet);
        }
    }

    @Override
    public int getPrecomputedDatasetSize(String datasetName) {
        try {
            String sql = "SELECT obj_count FROM dataset WHERE name='" + datasetName + "'";
            LOG.log(Level.INFO, sql);
            ResultSet rs = st.executeQuery(sql);
            rs.next();
            return rs.getInt("obj_count");
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public void updateDatasetSize(String datasetName) throws SQLException {
        int numberOfObjectsInDataset = reevaluatetNumberOfObjectsInDataset(datasetName);
        String sql = "UPDATE dataset SET obj_count=" + numberOfObjectsInDataset + " WHERE name='" + datasetName + "'";
        System.out.println(datasetName + ": " + numberOfObjectsInDataset + " metric objects");
        st.execute(sql);
    }

    private class MetricObjectFileIterator<T> implements Iterator<Object> {

        protected AbstractMap.SimpleEntry<String, T> nextObject;
        protected AbstractMap.SimpleEntry<String, T> currentObject;
        private final BufferedReader br;
        private final int maxCount;
        private int counter;

        public MetricObjectFileIterator(BufferedReader br, int maxCount) {
            this.br = br;
            this.nextObject = nextStreamObject();
            this.maxCount = maxCount;
            counter = 0;
        }

        @Override
        public boolean hasNext() {
            return nextObject != null && counter < maxCount;
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
            try {
                String line = br.readLine();
                if (line == null) {
                    return null;
                }
                String[] split = line.split(":");
                T transformed = (T) dataSerializator.parseDBString(split[1]);
                AbstractMap.SimpleEntry<String, T> entry = new AbstractMap.SimpleEntry<>(split[0], transformed);
                counter++;
                return entry;
            } catch (IOException ex) {
                Logger.getLogger(DBMetricSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

    private class MetricObjectDBIterator<T> implements Iterator<Object> {

        protected AbstractMap.SimpleEntry<String, T> nextObject;
        protected AbstractMap.SimpleEntry<String, T> currentObject;
        protected ResultSet set;

        /**
         * Number of objects read from the stream
         */
        protected int objectsRead;

        public MetricObjectDBIterator(ResultSet set) {
            this.set = set;
            this.nextObject = nextStreamObject();
        }

        private AbstractMap.SimpleEntry<String, T> nextStreamObject() {
            try {
                set.next();
                String dbIntId = set.getString("id");
                String data = set.getString("data");
                T transformed = (T) dataSerializator.parseDBString(data);
                AbstractMap.SimpleEntry<String, T> entry = new AbstractMap.SimpleEntry<>(dbIntId, transformed);
                return entry;
            } catch (SQLException ex) {
                try {
                    set.close();
                } catch (SQLException ex1) {
                    Logger.getLogger(DBMetricSpacesStorage.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return nextObject != null;
        }

        @Override
        public AbstractMap.SimpleEntry<String, T> next() throws NoSuchElementException, IllegalArgumentException, IllegalStateException {
            if (nextObject == null) {
                throw new NoSuchElementException("No more objects in the stream");
            }
            currentObject = nextObject;
            nextObject = nextStreamObject();
            return currentObject;
        }

    }

    private class DBStoreMetricPivots extends DBAbstractInsertMetricObjects<T> {

        public DBStoreMetricPivots() throws SQLException {
            super(metricSpace, dataSerializator, "INSERT INTO pivot(id, pivot_set_name, data) VALUES (?, ?, ?)");
        }
    }

    private class DBStoreMetricQueries extends DBAbstractInsertMetricObjects<T> {

        public DBStoreMetricQueries() throws SQLException {
            super(metricSpace, dataSerializator, "INSERT INTO query_obj(id, query_set_name, data) VALUES (?, ?, ?)");
        }
    }

}
