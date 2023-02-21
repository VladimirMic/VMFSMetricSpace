package vm.fs.main.search.filtering.learning;

import java.util.ArrayList;
import java.util.List;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.metricspace.AbstractMetricSpace;
import vm.metricspace.Dataset;
import vm.metricspace.distance.DistanceFunctionInterface;
import vm.metricspace.distance.bounding.onepivot.learning.LearningTriangleInequalityWithLimitedAngles;

/**
 *
 * @author Vlada
 */
public class LearnCoefsForTriangularFilteringWithLimitedAnglesMain {

    public static void main(String[] args) {
        run(new FSDatasetInstanceSingularizator.SIFTdataset());
        System.gc();
        run(new FSDatasetInstanceSingularizator.MPEG7dataset());
        System.gc();
        run(new FSDatasetInstanceSingularizator.RandomDataset20Uniform());
        System.gc();
        run(new FSDatasetInstanceSingularizator.DeCAFDataset());
    }

    private static void run(Dataset dataset) {
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        int pivotCount = 2560;
        List sampleOfDataset = dataset.getSampleOfDataset(11000);
        List sampleOfQueries = new ArrayList(sampleOfDataset.subList(0, 1000));
        sampleOfDataset.removeAll(sampleOfQueries);
        List pivots = dataset.getPivotsForTheSameDataset(pivotCount);

        FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl();
        LearningTriangleInequalityWithLimitedAngles learning = new LearningTriangleInequalityWithLimitedAngles(metricSpace, df, pivots, sampleOfDataset, sampleOfQueries, storage, dataset.getDatasetName());
        learning.execute();
    }

}
