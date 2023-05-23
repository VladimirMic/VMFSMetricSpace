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
        Dataset dataset;
//        dataset = new FSDatasetInstanceSingularizator.DeCAFDataset();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.SIFTdataset();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.MPEG7dataset();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.RandomDataset20Uniform();
//        run(dataset);
        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_256Dataset();
        run(dataset);
        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_64Dataset();
        run(dataset);
        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_128Dataset();
        run(dataset);
        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_192Dataset();
        run(dataset);
    }

    private static void run(Dataset dataset) {
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        int pivotCount = 2560;
        List sampleOfDataset = dataset.getSampleOfDataset(11000);
        List sampleOfQueries = new ArrayList(sampleOfDataset.subList(0, 1000));
        sampleOfDataset.removeAll(sampleOfQueries);
        List pivots = dataset.getPivots(pivotCount);

        FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl();
        LearningTriangleInequalityWithLimitedAngles learning = new LearningTriangleInequalityWithLimitedAngles(metricSpace, df, pivots, sampleOfDataset, sampleOfQueries, storage, dataset.getDatasetName());
        learning.execute();
    }

}
