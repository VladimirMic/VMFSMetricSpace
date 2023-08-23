package vm.fs.main.precomputeDistances;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixLoaderImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 * TODO - paralelisation?!
 *
 * @author Vlada
 */
public class EvalAndStoreObjectsToPivotsDistsMain {

    public static final Logger LOG = Logger.getLogger(EvalAndStoreObjectsToPivotsDistsMain.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(),
//            new FSDatasetInstanceSingularizator.SIFTdataset(),
//            new FSDatasetInstanceSingularizator.MPEG7dataset(),
//            new FSDatasetInstanceSingularizator.RandomDataset20Uniform()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
            System.gc();
        }
    }

    private static void run(Dataset dataset) {
        int pivotCount = 2048;
        String output = FSPrecomputedDistancesMatrixLoaderImpl.deriveFileForDatasetAndPivots(dataset.getDatasetName(), dataset.getDatasetName(), pivotCount, true).getAbsolutePath();
        GZIPOutputStream outputStream = null;
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        List pivots = dataset.getPivots(pivotCount);
        Iterator objects = dataset.getMetricObjectsFromDataset();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        try {
            outputStream = new GZIPOutputStream(new FileOutputStream(output), true);
            outputStream.write(';');
            for (int i = 0; i < pivots.size(); i++) {
                String pId = metricSpace.getIDOfMetricObject(pivots.get(i)).toString();
                outputStream.write(pId.getBytes());
                outputStream.write(';');
            }
            outputStream.write('\n');
            for (int i = 1; objects.hasNext(); i++) {
                Object o = objects.next();
                String oId = metricSpace.getIDOfMetricObject(o).toString();
                outputStream.write(oId.getBytes());
                outputStream.write(';');
                Object oData = metricSpace.getDataOfMetricObject(o);
                for (int j = 0; j < pivots.size(); j++) {
                    Object p = pivots.get(j);
                    Object pData = metricSpace.getDataOfMetricObject(p);
                    float distance = df.getDistance(oData, pData);
                    outputStream.write(Float.toString(distance).getBytes());
                    outputStream.write(';');
                }
                outputStream.write('\n');
                if (i % 10000 == 0) {
                    LOG.log(Level.INFO, "Evaluated {0} objects", i);
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}
