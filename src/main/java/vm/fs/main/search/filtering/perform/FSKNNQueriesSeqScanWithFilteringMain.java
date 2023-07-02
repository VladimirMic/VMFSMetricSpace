package vm.fs.main.search.filtering.perform;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixLoaderImpl;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.onepivot.OnePivotFilter;
import vm.metricSpace.distance.bounding.onepivot.impl.TriangleInequality;
import vm.metricSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixLoader;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;
import vm.search.SearchingAlgorithm;
import vm.search.impl.KNNSearchWithOnePivotFiltering;

/**
 *
 * @author Vlada
 */
public class FSKNNQueriesSeqScanWithFilteringMain {

    private static final Logger LOG = Logger.getLogger(FSKNNQueriesSeqScanWithFilteringMain.class.getName());

    public static void main(String[] args) {
//        run(new FSDatasetInstanceSingularizator.DeCAFDataset());
//        System.gc();
//        run(new FSDatasetInstanceSingularizator.MPEG7dataset());
//        System.gc();
//        run(new FSDatasetInstanceSingularizator.RandomDataset20Uniform());
//        System.gc();
//        run(new FSDatasetInstanceSingularizator.SIFTdataset());
//        System.gc();
//        run(new FSDatasetInstanceSingularizator.DeCAF_GHP_50_256Dataset());
//        System.gc();
//        run(new FSDatasetInstanceSingularizator.DeCAF_GHP_50_192Dataset());
//        System.gc();
        run(new FSDatasetInstanceSingularizator.DeCAF_GHP_50_128Dataset());
//        System.gc();
//        run(new FSDatasetInstanceSingularizator.DeCAF_GHP_50_64Dataset());
    }

    private static void run(Dataset dataset) {
        int k = 100;
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        int pivotCount = 256;

        AbstractPrecomputedDistancesMatrixLoader pd = new FSPrecomputedDistancesMatrixLoaderImpl();
        float[][] poDists = pd.loadPrecomPivotsToObjectsDists(dataset.getDatasetName(), dataset.getDatasetName(), pivotCount);
        List queries = dataset.getMetricQueryObjects();
        List pivots = dataset.getPivots(pivotCount);

//        TwoPivotsFilter filter = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(pivotCount + "_pivots", dataset.getDatasetName(), pivotCount);
//        TwoPivotsFilter filter = new FourPointBasedFiltering(pivotCount + "_pivots");
//        TwoPivotsFilter filter = new PtolemaiosFiltering(pivotCount + "_pivots");
        OnePivotFilter filter = new TriangleInequality(pivotCount + "_pivots");
//        OnePivotFilter filter = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(pivotCount + "_pivots", dataset.getDatasetName());

//        float[][] pivotPivotDists = metricSpace.getDistanceMap(df, pivots, pivots);
//        SearchingAlgorithm alg = new KNNSearchWithTwoPivotFiltering(metricSpace, filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), pivotPivotDists, df);
        SearchingAlgorithm alg = new KNNSearchWithOnePivotFiltering(metricSpace, filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), df);
        TreeSet[] results = alg.completeKnnSearchOfQuerySet(metricSpace, queries, k, dataset.getMetricObjectsFromDataset());

        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName(), null);
        statsStorage.storeStatsForQueries(alg.getDistCompsPerQueries(), alg.getTimesPerQueries());
        statsStorage.saveFile();

        LOG.log(Level.INFO, "Storing results of queries");
        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
        resultsStorage.storeQueryResults(metricSpace, queries, results, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName());

        LOG.log(Level.INFO, "Evaluating accuracy of queries");
        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(dataset.getDatasetName(), dataset.getDatasetName(), k, dataset.getDatasetName(), dataset.getDatasetName(), filter.getTechFullName(), null);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(new FSNearestNeighboursStorageImpl(), recallStorage);
        evaluator.evaluateAndStoreRecallsOfQueries(dataset.getDatasetName(), dataset.getDatasetName(), k, dataset.getDatasetName(), dataset.getDatasetName(), filter.getTechFullName(), k);
        recallStorage.saveFile();
    }

}
