package vm.fs.metricSpaceImpl;

//import ncsa.hdf.hdf5lib.H5;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.hdf5.DataSet;
import org.bytedeco.hdf5.H5File;
import org.bytedeco.hdf5.PredType;
import static org.bytedeco.hdf5.global.hdf5.H5F_ACC_RDONLY;
import org.bytedeco.javacpp.FloatPointer;
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
    private int vectorDimensionality;
    private int datasetSize;

    public H5MetricSpacesStorage(AbstractMetricSpace<float[]> metricSpace, int datasetSize, int vectorDimensionality, MetricObjectDataToStringInterface dataSerializator) {
        super(metricSpace, dataSerializator);
        this.vectorDimensionality = vectorDimensionality;
        this.datasetSize = datasetSize;
    }

    public H5MetricSpacesStorage(MetricObjectDataToStringInterface dataSerializator) {
        super(dataSerializator);
    }

    @Override
    public Iterator<Object> getObjectsFromDataset(String datasetName, Object... params) {
        return getIteratorOfObjects(FSGlobal.DATASET_FOLDER, datasetName, datasetSize);
    }

    @Override
    public List<Object> getPivots(String pivotSetName, Object... params) {
        Iterator<Object> it = getIteratorOfObjects(FSGlobal.PIVOT_FOLDER, pivotSetName, 512);
        return Tools.getObjectsFromIterator(it);
    }

    @Override
    public List<Object> getQueryObjects(String querySetName, Object... params) {
        Iterator<Object> it = getIteratorOfObjects(FSGlobal.QUERY_FOLDER, querySetName, 10000);
        return Tools.getObjectsFromIterator(it);
    }

    @Override
    protected File getFileForObjects(String folder, String fileName, boolean willBeDeleted) {
        File f = new File(folder, fileName);
        f = FSGlobal.checkFileExistence(f, willBeDeleted);
        LOG.log(Level.INFO, "Folder: {0}, file: {1}", new Object[]{f.getAbsolutePath(), fileName});
        return f;
    }

    @Override
    protected Iterator<Object> getIteratorOfObjects(File f, int count) {
        H5File h5File = new H5File(f.getAbsolutePath(), H5F_ACC_RDONLY);
        DataSet dataset = new DataSet(h5File.openDataSet("emb"));
        FloatBuffer fb = FloatBuffer.allocate(count * vectorDimensionality);
        FloatPointer pointer = new FloatPointer(fb);
        dataset.read(pointer, PredType.NATIVE_FLOAT());
        return new H5MetricObjectFileIterator(pointer, f.getName(), vectorDimensionality, count);
    }

    @Override
    protected void storeMetricObject(Object metricObject, OutputStream datasetOutputStream, Object... additionalParamsToStoreWithNewDataset) throws IOException {
        if (metricObject == null) {
            throw new IllegalArgumentException("Attempt to store null object as the metric object");
        }
        String id = metricSpace.getIDOfMetricObject(metricObject).toString();
        String data = dataSerializator.metricObjectDataToString((float[]) metricSpace.getDataOfMetricObject(metricObject));
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class H5MetricObjectFileIterator implements Iterator<Object> {

        protected AbstractMap.SimpleEntry<String, float[]> nextObject;
        protected AbstractMap.SimpleEntry<String, float[]> currentObject;
        private final String fileName;
        private final FloatPointer pointer;
        private final int maxCount;
        private final int dimensionality;
        private int counter;

        public H5MetricObjectFileIterator(FloatPointer pointer, String fileName, int dimensionality, int maxCount) {
            this.pointer = pointer;
            this.maxCount = maxCount;
            this.dimensionality = dimensionality;
            this.fileName = fileName;
            counter = 0;
            this.nextObject = nextStreamObject();
        }

        @Override
        public boolean hasNext() {
            return nextObject != null && counter <= maxCount;
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
            float[] ret = new float[dimensionality];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = pointer.get(i + counter * dimensionality);
            }
            AbstractMap.SimpleEntry<String, float[]> entry = new AbstractMap.SimpleEntry<>(fileName + counter, ret);
            counter++;
            return entry;
        }
    }
}
