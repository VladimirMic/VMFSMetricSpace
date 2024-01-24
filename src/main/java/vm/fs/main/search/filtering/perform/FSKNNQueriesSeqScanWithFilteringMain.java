package vm.fs.main.search.filtering.perform;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixLoaderImpl;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.ToolsMetricDomain;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.twopivots.TwoPivotsFilter;
import vm.metricSpace.distance.bounding.twopivots.learning.LearningPtolemyInequalityWithLimitedAngles;
import vm.metricSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixLoader;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;
import vm.search.algorithm.SearchingAlgorithm;
import vm.search.algorithm.impl.KNNSearchWithTwoPivotFiltering;

/**
 *
 * @author Vlada
 */
public class FSKNNQueriesSeqScanWithFilteringMain {

    private static final Logger LOG = Logger.getLogger(FSKNNQueriesSeqScanWithFilteringMain.class.getName());

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
//            new FSDatasetInstanceSingularizator.LAION_10M_Dataset()
            new FSDatasetInstanceSingularizator.DeCAFDataset()
//                    new FSDatasetInstanceSingularizator.SIFTdataset(),
        //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
        //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
        //            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_256Dataset(),
        //            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_192Dataset(),
        //            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_128Dataset(),
        //            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_64Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
            System.gc();
        }
    }

    private static void run(Dataset dataset) {
        int maxObjectsCount = 1000000;
        int k = 30;
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        int pivotCount = 256;

        AbstractPrecomputedDistancesMatrixLoader pd = new FSPrecomputedDistancesMatrixLoaderImpl();
        float[][] poDists = pd.loadPrecomPivotsToObjectsDists(dataset.getDatasetName(), dataset.getPivotSetName(), pivotCount);
        List queries = dataset.getMetricQueryObjects();
        queries = queries.subList(0, 1000);;
        List pivots = dataset.getPivots(pivotCount);
        if (poDists == null || poDists.length == 0) {
            pd = ToolsMetricDomain.evaluateMatrixOfDistances(dataset.getMetricObjectsFromDataset(maxObjectsCount), pivots, metricSpace, df);
            poDists = pd.loadPrecomPivotsToObjectsDists(null, null, -1);
        }

        TwoPivotsFilter filter = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(pivotCount + "_pivots", dataset.getDatasetName(), pivotCount, LearningPtolemyInequalityWithLimitedAngles.ALL_PIVOT_PAIRS);
//        TwoPivotsFilter filter = new FourPointBasedFiltering(pivotCount + "_pivots");
//        TwoPivotsFilter filter = new PtolemaiosFiltering(pivotCount + "_pivots");
//        OnePivotFilter filter = new TriangleInequality(pivotCount + "_pivots");
//        OnePivotFilter filter = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(pivotCount + "_pivots", dataset.getDatasetName());

        float[][] pivotPivotDists = metricSpace.getDistanceMap(df, pivots, pivots);
        SearchingAlgorithm alg = new KNNSearchWithTwoPivotFiltering(metricSpace, filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), pivotPivotDists, df, true);
//        SearchingAlgorithm alg = new KNNSearchWithOnePivotFiltering(metricSpace, filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), df);
        TreeSet[] results = alg.completeKnnFilteringWithQuerySet(metricSpace, queries, k, dataset.getMetricObjectsFromDataset(maxObjectsCount));

        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName(), null);
        statsStorage.storeStatsForQueries(alg.getDistCompsPerQueries(), alg.getTimesPerQueries(), alg.getAddditionalStats());
        statsStorage.save();

        LOG.log(Level.INFO, "Storing results of queries");
        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
        resultsStorage.storeQueryResults(metricSpace, queries, results, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName());

        LOG.log(Level.INFO, "Evaluating accuracy of queries");
        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName(), null);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(new FSNearestNeighboursStorageImpl(), recallStorage);
        evaluator.evaluateAndStoreRecallsOfQueries(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName(), k);
        recallStorage.save();
    }

}
