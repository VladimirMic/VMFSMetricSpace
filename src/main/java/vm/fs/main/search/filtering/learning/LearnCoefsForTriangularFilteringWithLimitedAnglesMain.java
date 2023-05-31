package vm.fs.main.search.filtering.learning;

import java.util.ArrayList;
import java.util.List;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.onepivot.learning.LearningTriangleInequalityWithLimitedAngles;

/**
 *
 * @author Vlada
 */
public class LearnCoefsForTriangularFilteringWithLimitedAnglesMain {

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_100k_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_300k_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset()
        };

        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) {
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        int pivotCount = 2048;
        List sampleOfDataset = dataset.getSampleOfDataset(11000);
        List sampleOfQueries = new ArrayList(sampleOfDataset.subList(0, 1000));
        sampleOfDataset.removeAll(sampleOfQueries);
        List pivots = dataset.getPivots(pivotCount);

        FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl();
        LearningTriangleInequalityWithLimitedAngles learning = new LearningTriangleInequalityWithLimitedAngles(metricSpace, df, pivots, sampleOfDataset, sampleOfQueries, storage, dataset.getDatasetName());
        learning.execute();
    }

}
