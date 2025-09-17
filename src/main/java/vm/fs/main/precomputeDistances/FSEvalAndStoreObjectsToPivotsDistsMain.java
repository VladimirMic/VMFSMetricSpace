package vm.fs.main.precomputeDistances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import vm.fs.dataset.FSDatasetInstances;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixLoaderImpl;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.ToolsSpaceDomain;
import vm.searchSpace.distance.DistanceFunctionInterface;
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
        if (pivotCount < 0) {
            throw new IllegalArgumentException("The dataset does not specify the number of pivots");
        }
        FSPrecomputedDistancesMatrixLoaderImpl loader = new FSPrecomputedDistancesMatrixLoaderImpl();
        String output = loader.deriveFileForDatasetAndPivots(dataset.getDatasetName(), dataset.getPivotSetName(), pivotCount, true).getAbsolutePath();
        GZIPOutputStream outputStream = null;
        AbstractSearchSpace searchSpace = dataset.getSearchSpace();
        List pivots = dataset.getPivots(pivotCount);
        Iterator objects = dataset.getSearchObjectsFromDataset();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        List<Comparable>  pivotIDs = searchSpace.getIDsOfObjects(pivots.iterator());
        try {
            outputStream = new GZIPOutputStream(new FileOutputStream(output), true);
            int batchSize = 60000;
            int batchCounter = -1;
            int rowCounter = 0;
            while (objects.hasNext()) {
                System.gc();
                batchCounter++;
                MainMemoryStoredPrecomputedDistances pd = ToolsSpaceDomain.evaluateMatrixOfDistances(objects, pivots, searchSpace, df, batchSize);
                Map<Comparable, Integer> rowHeaders = pd.getRowHeaders();
                Map<Comparable, Integer> columnHeaders = pd.getColumnHeaders();
                float[][] dists = pd.loadPrecomPivotsToObjectsDists(null, -1);
                if (batchCounter == 0) {
                    outputStream.write(';');
                    for (Comparable pivotID : pivotIDs) {
                        outputStream.write(pivotID.toString().getBytes());
                        outputStream.write(';');
                    }
                    outputStream.write('\n');
                }
                for (Map.Entry<Comparable, Integer> oID : rowHeaders.entrySet()) {
                    rowCounter++;
                    String oIdString = oID.getKey().toString();
                    outputStream.write(oIdString.getBytes());
                    outputStream.write(';');
                    for (Comparable pivotID : pivotIDs) {
                        Integer pIdx = columnHeaders.get(pivotID);
                        float distance = dists[oID.getValue()][pIdx];
                        outputStream.write(Float.toString(distance).getBytes());
                        outputStream.write(';');
                    }
                    outputStream.write('\n');
                    if (rowCounter % 20000 == 0) {
                        LOG.log(Level.INFO, "Stored {0} rows", rowCounter);
                    }
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

    public static boolean existsForDataset(Dataset dataset, int pivotCount) {
        FSPrecomputedDistancesMatrixLoaderImpl loader = new FSPrecomputedDistancesMatrixLoaderImpl();
        String output = loader.deriveFileForDatasetAndPivots(dataset.getDatasetName(), dataset.getPivotSetName(), pivotCount, false).getAbsolutePath();
        return new File(output).exists();
    }
}
