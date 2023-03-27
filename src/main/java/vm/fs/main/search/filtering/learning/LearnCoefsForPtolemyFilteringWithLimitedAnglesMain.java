package vm.fs.main.search.filtering.learning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.precomputedDists.FSPrecomputedDistPairsStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.twopivots.learning.LearningPtolemyInequalityWithLimitedAngles;
import vm.metricSpace.distance.storedPrecomputedDistances.PrecomputedPairsOfDistancesStoreInterface;

/**
 *
 * @author Vlada
 */
public class LearnCoefsForPtolemyFilteringWithLimitedAnglesMain {

    public static final Integer SAMPLE_SET_SIZE = 10000;
    public static final Integer SAMPLE_QUERY_SET_SIZE = 1000;
    public static final Integer PIVOTS = 256;

    public static void main(String[] args) throws IOException {
        Dataset dataset;
//        dataset = new FSDatasetInstanceSingularizator.DeCAFDataset();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.SIFTdataset();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.MPEG7dataset();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.RandomDataset20Uniform();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_256Dataset();
//        run(dataset);
//        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_64Dataset();
//        run(dataset);
        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_128Dataset();
        run(dataset);
        dataset = new FSDatasetInstanceSingularizator.DeCAF_GHP_50_192Dataset();
        run(dataset);
    }

    private static void run(Dataset dataset) throws FileNotFoundException {
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        List<Object> pivots = dataset.getPivotsForTheSameDataset(PIVOTS);
        PrecomputedPairsOfDistancesStoreInterface smallDistSample = new FSPrecomputedDistPairsStorageImpl(dataset.getDatasetName(), SAMPLE_SET_SIZE, SAMPLE_QUERY_SET_SIZE);
        FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl();

        List<Object> sampleObjectsAndQueries = dataset.getSampleOfDataset(SAMPLE_SET_SIZE + SAMPLE_QUERY_SET_SIZE);
        TreeSet<Map.Entry<String, Float>> smallDistsOfSampleObjectsAndQueries = smallDistSample.loadPrecomputedDistances();

        LearningPtolemyInequalityWithLimitedAngles learning = new LearningPtolemyInequalityWithLimitedAngles(metricSpace, df, pivots, sampleObjectsAndQueries, smallDistsOfSampleObjectsAndQueries, storage, dataset.getDatasetName());
        learning.execute();
    }
}