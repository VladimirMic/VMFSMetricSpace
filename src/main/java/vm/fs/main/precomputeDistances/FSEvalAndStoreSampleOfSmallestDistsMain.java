package vm.fs.main.precomputeDistances;

import java.util.TreeSet;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.precomputedDists.FSPrecomputedDistPairsStorageImpl;
import vm.metricSpace.Dataset;
import static vm.metricSpace.distance.bounding.onepivot.learning.LearningTriangleInequalityWithLimitedAngles.RATIO_OF_SMALLEST_DISTS;

/**
 *
 * @author Vlada
 */
public class FSEvalAndStoreSampleOfSmallestDistsMain {

    public static final Integer SAMPLE_SET_SIZE = 10000;
    public static final Integer SAMPLE_QUERY_SET_SIZE = 1000;
    public static final Logger LOG = Logger.getLogger(FSEvalAndStoreSampleOfSmallestDistsMain.class.getName());
    /**
     * Number of stored minimum distances
     */
    public static final Integer IMPLICIT_K = (int) (RATIO_OF_SMALLEST_DISTS * SAMPLE_SET_SIZE * SAMPLE_QUERY_SET_SIZE);

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
//            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(true),
//            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(true),
//            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(true),
//            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(true),
//            new FSDatasetInstanceSingularizator.DeCAFDataset(),
//            new FSDatasetInstanceSingularizator.SIFTdataset(),
//            new FSDatasetInstanceSingularizator.MPEG7dataset(),
//            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_256Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_64Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_192Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_128Dataset(),
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    public static void run(Dataset dataset) {
        TreeSet result = dataset.evaluateSampleOfSmallestDistances(SAMPLE_SET_SIZE, SAMPLE_QUERY_SET_SIZE, IMPLICIT_K, null);
        FSPrecomputedDistPairsStorageImpl storage = new FSPrecomputedDistPairsStorageImpl(dataset.getDatasetName(), SAMPLE_SET_SIZE, SAMPLE_QUERY_SET_SIZE);
        storage.storePrecomputedDistances(result);
    }

    public static boolean existsForDataset(Dataset dataset) {
        FSPrecomputedDistPairsStorageImpl storage = new FSPrecomputedDistPairsStorageImpl(dataset.getDatasetName(), SAMPLE_SET_SIZE, SAMPLE_QUERY_SET_SIZE);
        return storage.getFileForResults(false).exists();
    }

}
