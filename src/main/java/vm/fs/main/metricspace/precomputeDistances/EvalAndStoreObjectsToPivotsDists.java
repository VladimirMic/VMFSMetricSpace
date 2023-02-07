package vm.fs.main.metricspace.precomputeDistances;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.metricspace.distance.precomputedDistances.PrecomputedDistancesLoaderImpl;
import vm.metricspace.AbstractMetricSpace;
import vm.metricspace.Dataset;
import vm.metricspace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class EvalAndStoreObjectsToPivotsDists {

    public static final Logger LOGGER = Logger.getLogger(EvalAndStoreObjectsToPivotsDists.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        run(new FSDatasetInstanceSingularizator.SIFTdataset());
        System.gc();
        run(new FSDatasetInstanceSingularizator.MPEG7dataset());
        System.gc();
        run(new FSDatasetInstanceSingularizator.RandomDataset20Uniform());
        System.gc();
        run(new FSDatasetInstanceSingularizator.DeCAFDataset());
    }

    private static void run(Dataset dataset) {
        int pivotCount = 256;
        String output = PrecomputedDistancesLoaderImpl.deriveFileForDatasetAndPivots(dataset.getDatasetName(), dataset.getDatasetName(), pivotCount).getAbsolutePath();
        GZIPOutputStream outputStream = null;
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        List pivots = dataset.getPivotsForTheSameDataset(pivotCount);
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
                    LOGGER.log(Level.INFO, "Evaluated {0} objects", i);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

}