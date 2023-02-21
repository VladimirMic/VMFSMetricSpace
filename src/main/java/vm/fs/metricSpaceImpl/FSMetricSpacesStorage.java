package vm.fs.metricSpaceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.metricSpace.dataToStringConvertors.MetricObjectDataToStringInterface;

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
    public Iterator<Object> getObjectsFromDataset(String datasetName, Object... params) {
        return getIteratorOfObjects(FSGlobal.DATASET_FOLDER, datasetName, params);
    }

    @Override
    public List<Object> getPivots(String pivotSetName, Object... params) {
        Iterator<Object> it = getIteratorOfObjects(FSGlobal.PIVOT_FOLDER, pivotSetName, params);
        return Tools.getObjectsFromIterator(it);
    }

    @Override
    public List<Object> getQueryObjects(String querySetName, Object... params) {
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
    public void storeObjectToDataset(Object metricObject, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        GZIPOutputStream datasetOutputStream = null;
        try {
            File f = getFileForObjects(FSGlobal.DATA_FOLDER, datasetName);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, true), true);
            storeMetricObject(metricObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
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

    private void storeMetricObject(Object metricObject, GZIPOutputStream datasetOutputStream, Object... additionalParamsToStoreWithNewDataset) throws IOException {
        String id = metricSpace.getIDOfMetricObject(metricObject).toString();
        String data = dataSerializator.metricObjectDataToString((T) metricSpace.getDataOfMetricObject(metricObject));
        datasetOutputStream.write(id.getBytes());
        datasetOutputStream.write(':');
        datasetOutputStream.write(data.getBytes());
        datasetOutputStream.write('\n');
    }

    @Override
    public synchronized int storeObjectsToDataset(Iterator<Object> it, int count, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        GZIPOutputStream datasetOutputStream = null;
        int ret = 0;
        try {
            File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, true), true);
            for (ret = 1; it.hasNext(); ret++) {
                Object metricObject = it.next();
                storeMetricObject(metricObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
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
        LOG.log(Level.INFO, "Folder: " + f.getAbsolutePath() + ", file: " + fileName);
        return new File(f, fileName + ".gz");
    }

    /**
     *
     * @param pivots metric objects to store
     * @param pivotSetName identifier of the pivot set
     * @param additionalParamsToStoreWithNewPivotSet zero must be the instance
     * of the metric space which is used to extract the ID of the metric object
     * and its data
     */
    @Override
    public void storePivots(List<Object> pivots, String pivotSetName, Object... additionalParamsToStoreWithNewPivotSet) {
        GZIPOutputStream datasetOutputStream = null;
        try {
            File f = getFileForObjects(FSGlobal.PIVOT_FOLDER, pivotSetName);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, true), true);
            for (Object metricObject : pivots) {
                storeMetricObject(metricObject, datasetOutputStream, additionalParamsToStoreWithNewPivotSet);
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
    }

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
        GZIPOutputStream datasetOutputStream = null;
        try {
            File f = getFileForObjects(FSGlobal.QUERY_FOLDER, querySetName);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, true), true);
            for (Object metricObject : queryObjs) {
                storeMetricObject(metricObject, datasetOutputStream, additionalParamsToStoreWithNewQuerySet);
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
    }

    @Override
    public int getPrecomputedDatasetSize(String datasetName) {
        BufferedReader br = null;
        try {
            File f = getFileForObjects(FSGlobal.QUERY_FOLDER, datasetName + "_size.txt");
            br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            return Integer.parseInt(line);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return -1;
    }

    @Override
    public void updateDatasetSize(String datasetName, int count) {
        FileOutputStream os = null;
        try {
            File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName + "_size.txt");
            os = new FileOutputStream(f);
            byte[] bytes = Integer.toString(count).getBytes();
            os.write(bytes);
        } catch (IOException ex) {
            Logger.getLogger(FSMetricSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(FSMetricSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
                LOG.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

}
