package vm.fs.searchSpaceImpl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.searchSpace.AbstractSearchSpacesStorage;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.data.toStringConvertors.SearchObjectDataToStringInterface;

/**
 *
 * @author xmic
 * @param <T>
 */
public class FSSearchSpacesStorage<T> extends AbstractSearchSpacesStorage<T> {

    public static final Logger LOG = Logger.getLogger(FSSearchSpacesStorage.class.getName());

    private VMMVStorage singularizatorOfDiskStorage = null;

    /**
     * Methods searchSpace.getIDOfObject and
     * searchSpace.getDataOfObject are used to store the search objects in
     * the "key-value" format
     *
     * @param searchSpace
     * @param dataSerializator transforms T to string and vice versa see
     * @SingularisedConvertors
     */
    public FSSearchSpacesStorage(AbstractSearchSpace<T> searchSpace, SearchObjectDataToStringInterface<T> dataSerializator) {
        super(searchSpace, dataSerializator);
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

    protected Iterator<Object> getIteratorOfObjects(String folder, String setName, Object... params) {
        File f = getFileForObjects(folder, setName, false);
        if (!f.exists()) {
            LOG.log(Level.SEVERE, "No file for objects {0} exists", f.getAbsolutePath());
            if (!folder.equals(FSGlobal.DATASET_FOLDER)) {
                return null;
            }
            if (singularizatorOfDiskStorage == null) {
                singularizatorOfDiskStorage = new VMMVStorage(setName, false);
            }
            if (singularizatorOfDiskStorage == null) {
                LOG.log(Level.SEVERE, "No file for objects {0} exists", f.getAbsolutePath());
                return null;
            }
            Map<Object, T> map = singularizatorOfDiskStorage.getKeyValueStorage();
            if (map == null) {
                return null;
            }
            Iterator<Map.Entry<Object, T>> iterator = map.entrySet().iterator();
            return new Dataset.StaticIteratorOfSearchObjectsMadeOfKeyValueMap(iterator, searchSpace, params);
        }
        return getIteratorOfObjects(f, params);
    }

    public VMMVStorage getSingularizatorOfDiskStorage() {
        return singularizatorOfDiskStorage;
    }

    public void setSingularizatorOfDiskStorage(VMMVStorage singularizatorOfDiskStorage) {
        this.singularizatorOfDiskStorage = singularizatorOfDiskStorage;
    }

    public Iterator getIteratorOfObjects(File f, Object... params) {
        BufferedReader br;
        try {
            if (f.getName().toLowerCase().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            }
            int count = params.length > 0 && params[0] instanceof Integer ? (int) params[0] : Integer.MAX_VALUE;
            if (count < 0) {
                count = Integer.MAX_VALUE;
            }
            return getIteratorForReader(br, count, f.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(FSSearchSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param searchObject
     * @param datasetName
     * @param additionalParamsToStoreWithNewDataset zero must be the instance of
     * the search space which is used to extract the ID of the search object and
     * its data
     */
    @Override
    public void storeObjectToDataset(Object searchObject, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        GZIPOutputStream datasetOutputStream = null;
        try {
            File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName, false);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, true), true);
            storeSearchObject(searchObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
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

    protected void storeSearchObject(Object searchObject, OutputStream datasetOutputStream, Object... additionalParamsToStoreWithNewDataset) throws IOException {
        if (searchObject == null) {
            throw new IllegalArgumentException("Attempt to store null object as the search object");
        }
        String id = searchSpace.getIDOfObject(searchObject).toString();
        String data = dataSerializator.searchObjectDataToString((T) searchSpace.getDataOfObject(searchObject));
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
            File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName, false);
            if (additionalParamsToStoreWithNewDataset.length > 0 && additionalParamsToStoreWithNewDataset[0] instanceof Boolean && additionalParamsToStoreWithNewDataset[0].equals(true)) {
                FSGlobal.checkFileExistence(f);
                datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, false), true);
            } else {
                datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, true), true);
            }
            for (ret = 1; it.hasNext(); ret++) {
                Object searchObject = it.next();
                storeSearchObject(searchObject, datasetOutputStream, additionalParamsToStoreWithNewDataset);
                if (ret % 50000 == 0) {
                    LOG.log(Level.INFO, "Stored {0} search objects", ret);
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

    protected File getFileForObjects(String folder, String fileName, boolean willBeDeleted) {
        File f = new File(folder, fileName);
        f = FSGlobal.checkFileExistence(f, false);
        if (!f.exists()) {
            fileName += ".gz";
            File fGZ = new File(folder, fileName);
            fGZ = FSGlobal.checkFileExistence(fGZ, willBeDeleted);
            if (!fGZ.exists() && !willBeDeleted) {
                LOG.log(Level.WARNING, "File on the path {0} does not exist. The params are: folder: {1}, fileName: {2}. Returning zipped file: {3}", new Object[]{f.getAbsolutePath(), folder, fileName, fGZ.getName()});
            }
            checkBigTertiaryDatasetStorage(fGZ);
            return fGZ;
        }
        checkBigTertiaryDatasetStorage(f);
        return f;
    }

    /**
     *
     * @param pivots search objects to store
     * @param pivotSetName identifier of the pivot set
     * @param additionalParamsToStoreWithNewPivotSet zero must be the instance
     * of the search space which is used to extract the ID of the search object
     * and its data
     */
    @Override
    public void storePivots(List<Object> pivots, String pivotSetName, Object... additionalParamsToStoreWithNewPivotSet) {
        boolean delete = true;
        if (additionalParamsToStoreWithNewPivotSet != null && additionalParamsToStoreWithNewPivotSet.length > 0 && additionalParamsToStoreWithNewPivotSet[0] instanceof Boolean) {
            delete = (boolean) additionalParamsToStoreWithNewPivotSet[0];
        }
        GZIPOutputStream os = null;
        try {
            File f = getFileForObjects(FSGlobal.PIVOT_FOLDER, pivotSetName, delete);
            os = new GZIPOutputStream(new FileOutputStream(f, !delete), true);
            for (Object searchObject : pivots) {
                storeSearchObject(searchObject, os, additionalParamsToStoreWithNewPivotSet);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                os.flush();
                os.close();
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
     * of the search space which is used to extract the ID of the search object
     * and its data
     */
    @Override
    public void storeQueryObjects(List<Object> queryObjs, String querySetName, Object... additionalParamsToStoreWithNewQuerySet) {
        boolean delete = true;
        if (additionalParamsToStoreWithNewQuerySet != null && additionalParamsToStoreWithNewQuerySet.length > 0 && additionalParamsToStoreWithNewQuerySet[0] instanceof Boolean) {
            delete = (boolean) additionalParamsToStoreWithNewQuerySet[0];
        }
        GZIPOutputStream datasetOutputStream = null;
        try {
            File f = getFileForObjects(FSGlobal.QUERY_FOLDER, querySetName, delete);
            FSGlobal.checkFileExistence(f);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(f, !delete), true);
            for (Object searchObject : queryObjs) {
                storeSearchObject(searchObject, datasetOutputStream, additionalParamsToStoreWithNewQuerySet);
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
            File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName + "_size.txt", false);
            if (!f.exists()) {
                return -1;
            }
            br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            return Integer.parseInt(line);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return -1;
    }

    public void evaluateAndStoreNumberOfObjectsInDataset(String datasetName) {
        Iterator<Object> it = getObjectsFromDataset(datasetName);
        int i;
        for (i = 0; it.hasNext(); i++) {
            it.next();
            if (i % 100000 == 0) {
                Logger.getLogger(FSSearchSpacesStorage.class.getName()).log(Level.INFO, "Read {0} objects", i);
            }
        }
        File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName + "_size.txt", false);
        FSGlobal.checkFileExistence(f, true);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            bw.write(Integer.toString(i));
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(FSSearchSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateDatasetSize(String datasetName, int count) {
        FileOutputStream os = null;
        try {
            File f = getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName + "_size.txt", false);
            os = new FileOutputStream(f);
            byte[] bytes = Integer.toString(count).getBytes();
            os.write(bytes);
        } catch (IOException ex) {
            Logger.getLogger(FSSearchSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(FSSearchSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void checkBigTertiaryDatasetStorage(File fGZ) {
        File folder = fGZ.getParentFile();
        File datasetFolder = new File(FSGlobal.DATASET_FOLDER);
        File keyValueFolder = new File(FSGlobal.DATASET_MVSTORAGE_FOLDER);
        if (fGZ.exists() && Files.isSymbolicLink(fGZ.toPath()) && (folder.equals(datasetFolder) || folder.equals(keyValueFolder))) {
            LOG.log(Level.WARNING, "Returned file {0} is a symbolic file. Reading might be slow", fGZ.getAbsolutePath());
            try {
                String realPath = fGZ.toPath().toRealPath().toFile().getAbsolutePath();
                boolean startsWith = realPath.toLowerCase().contains("nas");
                if (startsWith && FSGlobal.STOP_ON_READING_FROM_NAS) {
                    System.err.println("");
                    throw new RuntimeException("File " + realPath + " is on the tertiary storage. Stopping. The symbolic link path is " + fGZ.getAbsolutePath());
                }
            } catch (IOException ex) {
                Logger.getLogger(FSSearchSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     * @param br
     * @param count
     * @param filePath allows to distinguish in overriden methods
     * @return
     */
    protected Iterator<AbstractMap.SimpleEntry<String, T>> getIteratorForReader(BufferedReader br, int count, String filePath) {
        return new SearchObjectFileIterator(br, count);
    }

    protected class SearchObjectFileIterator<T> implements Iterator<AbstractMap.SimpleEntry<String, T>> {

        protected AbstractMap.SimpleEntry<String, T>[] nextObjects;
        private int pointer;
        protected AbstractMap.SimpleEntry<String, T> currentObject;
        private final BufferedReader br;
        private int maxCount;
        private int counterX;

        private final int BATCH_SIZE = 256;

        public SearchObjectFileIterator(BufferedReader br, int maxCount) {
            this.br = br;
            nextObjects = new AbstractMap.SimpleEntry[BATCH_SIZE];
            this.maxCount = maxCount;
            counterX = 0;
            pointer = 0;
            loadBatch();
        }

        @Override
        public boolean hasNext() {
            if (pointer == BATCH_SIZE) {
                loadBatch();
                pointer = 0;
            }
            boolean ret = nextObjects[pointer] != null && counterX < maxCount;
            if (!ret) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(FSSearchSpacesStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ret;
        }

        @Override
        public AbstractMap.SimpleEntry<String, T> next() {
            currentObject = nextObjects[pointer];
            pointer++;
            counterX++;
            return currentObject;
        }

        private void nextStreamObjects(int limit) {
            try {
                if (limit == 0) {
                    nextObjects[0] = null;
                }
                String[] lines = new String[limit];
                int i;
                for (i = 0; i < lines.length; i++) {
                    lines[i] = br.readLine();
                    if (lines[i] == null) {
                        maxCount = counterX + i;
                        break;
                    }
                }
                for (i = 0; i < lines.length; i++) {
                    if (lines[i] == null) {
                        break;
                    }
                    try {
                        String[] split = lines[i].split(":");
                        T obj = (T) dataSerializator.parseString(split[1]);
                        AbstractMap.SimpleEntry<String, T> entry = new AbstractMap.SimpleEntry<>(split[0], obj);
                        nextObjects[i] = entry;
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "The file is corrupted. Exception occured when reading. Trying to read next line", e);
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }

        private void loadBatch() {
            int limit = Math.min(maxCount - counterX, BATCH_SIZE);
            nextStreamObjects(limit);
        }
    }

}
