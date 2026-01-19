package vm.fs.main.precomputeDistances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import vm.fs.dataset.FSDatasetInstances;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixSerializatorImpl;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.ToolsSpaceDomain;
import vm.searchSpace.distance.AbstractDistanceFunction;
import vm.searchSpace.distance.storedPrecomputedDistances.MainMemoryStoredPrecomputedDistances;

/**
 * TODO - paralelisation?!
 *
 * @author Vlada
 */
public class FSEvalAndStoreObjectsToPivotsDistsMain {

    public static final Logger LOG = Logger.getLogger(FSEvalAndStoreObjectsToPivotsDistsMain.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
            //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(true),
            //            new FSDatasetInstanceSingularizator.SIFTdataset(),
            //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
            //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform()
            new FSDatasetInstances.LAION_10M_Dataset(publicQueries), //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset, dataset.getRecommendedNumberOfPivotsForFiltering());
            System.gc();
        }
    }

    public static void run(Dataset dataset, int pivotCount) {
        run(dataset, pivotCount, dataset.getDistanceFunction());
    }

    public static void run(Dataset dataset, int pivotCount, AbstractDistanceFunction df) {
        if (pivotCount < 0) {
            pivotCount = Integer.MAX_VALUE;
        }
        FSPrecomputedDistancesMatrixSerializatorImpl loader = new FSPrecomputedDistancesMatrixSerializatorImpl();
        GZIPOutputStream outputStream = FSPrecomputedDistancesMatrixSerializatorImpl.getGZIPOutputStream(dataset, pivotCount, false);
        AbstractSearchSpace searchSpace = dataset.getSearchSpace();
        List pivots = dataset.getPivots(pivotCount);
        Iterator objects = dataset.getSearchObjectsFromDataset();
        try {
            int batchSize = 60000;
            int batchCounter = -1;
            int rowCounter = 0;
            while (objects.hasNext()) {
                System.gc();
                batchCounter++;
                MainMemoryStoredPrecomputedDistances pd = ToolsSpaceDomain.evaluateMatrixOfDistances(objects, pivots, searchSpace, df, batchSize);
                Map<Comparable, Integer> rowHeaders = pd.getRowHeaders();
                Map<Comparable, Integer> columnHeaders = pd.getColumnHeaders();
                float[][] dists = pd.getDists();
                if (batchCounter == 0) {
                    loader.serializeColumnsHeaders(outputStream, columnHeaders);
                }
                rowCounter = loader.serializeRows(outputStream, rowHeaders, columnHeaders, dists, rowCounter);
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

    public static boolean existsForDataset(Dataset dataset, int pivotCount) {
        FSPrecomputedDistancesMatrixSerializatorImpl loader = new FSPrecomputedDistancesMatrixSerializatorImpl();
        String output = loader.deriveFileForDatasetAndPivots(dataset.getDatasetName(), dataset.getPivotSetName(), pivotCount, false).getAbsolutePath();
        return new File(output).exists();
    }
}
