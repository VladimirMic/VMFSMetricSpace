package vm.fs.main.search.filtering.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.main.precomputeDistances.FSEvalAndStoreObjectsToPivotsDistsMain;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.onepivot.learning.LearningTriangleInequalityWithLimitedAngles;
import static vm.metricSpace.distance.bounding.onepivot.learning.LearningTriangleInequalityWithLimitedAngles.RATIO_OF_SMALLEST_DISTS;

/**
 *
 * @author Vlada
 */
public class FSLearnCoefsForDataDepenentMetricFilteringMain {

    public static final Integer SAMPLE_O_COUNT = 10000;
    public static final Integer SAMPLE_Q_COUNT = 1000;

    public static void main(String[] args) {
        boolean publicQueries = false;
        Dataset[] datasets = new Dataset[]{
            //            new FSDatasetInstanceSingularizator.SIFTdataset(),
            //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.DeCAFDataset(),
            //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
            //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
//            new FSDatasetInstanceSingularizator.LAION_100k_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_300k_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries)
            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(true)
        };

        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    public static void run(Dataset dataset) {
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        int pivotCount = FSEvalAndStoreObjectsToPivotsDistsMain.PIVOT_COUNT;
        List sampleOfDataset = dataset.getSampleOfDataset(SAMPLE_O_COUNT + SAMPLE_Q_COUNT);
        List sampleOfQueries = new ArrayList(sampleOfDataset.subList(0, SAMPLE_Q_COUNT));
        sampleOfDataset.removeAll(sampleOfQueries);
        List pivots = dataset.getPivots(pivotCount);

        FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl();
        LearningTriangleInequalityWithLimitedAngles learning = new LearningTriangleInequalityWithLimitedAngles(metricSpace, df, pivots, sampleOfDataset, sampleOfQueries, storage, dataset.getDatasetName());
        learning.execute();
    }

    public static boolean existsForDataset(Dataset dataset) {
        FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl();
        String result = storage.getResultDescription(dataset.getDatasetName(), FSEvalAndStoreObjectsToPivotsDistsMain.PIVOT_COUNT, SAMPLE_O_COUNT, SAMPLE_Q_COUNT, RATIO_OF_SMALLEST_DISTS);
        File file = storage.getFile(result, false);
        return file.exists();
    }

}
