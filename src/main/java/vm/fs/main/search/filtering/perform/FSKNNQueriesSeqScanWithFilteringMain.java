package vm.fs.main.search.filtering.perform;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.main.search.filtering.learning.LearnCoefsForTriangularFilteringWithLimitedAnglesMain;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixLoaderImpl;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.ToolsMetricDomain;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.BoundsOnDistanceEstimation;
import vm.metricSpace.distance.bounding.onepivot.OnePivotFilter;
import vm.metricSpace.distance.bounding.onepivot.impl.TriangleInequality;
import vm.metricSpace.distance.bounding.twopivots.TwoPivotsFilter;
import vm.metricSpace.distance.bounding.twopivots.impl.FourPointBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaiosFiltering;
import vm.metricSpace.distance.bounding.twopivots.learning.LearningPtolemyInequalityWithLimitedAngles;
import vm.metricSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixLoader;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;
import vm.search.algorithm.SearchingAlgorithm;
import vm.search.algorithm.impl.KNNSearchWithOnePivotFiltering;
import vm.search.algorithm.impl.KNNSearchWithTwoPivotFiltering;

/**
 *
 * @author Vlada
 */
public class FSKNNQueriesSeqScanWithFilteringMain {

    private static final Logger LOG = Logger.getLogger(FSKNNQueriesSeqScanWithFilteringMain.class.getName());

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.MPEG7dataset(),
            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
            new FSDatasetInstanceSingularizator.SIFTdataset(),
            new FSDatasetInstanceSingularizator.DeCAFDataset(),
//            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries)
        };

        int pivotCount = 256;
        int k = 30;

        for (Dataset dataset : datasets) {
            BoundsOnDistanceEstimation[] filters = initTestedFilters(pivotCount, dataset, k);
            for (BoundsOnDistanceEstimation filter : filters) {
                run(dataset, filter, pivotCount, k);
                System.gc();
            }
        }
    }

    private static void run(Dataset dataset, BoundsOnDistanceEstimation filter, int pivotCount, int k) {
        int maxObjectsCount = -1;
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();

        AbstractPrecomputedDistancesMatrixLoader pd = new FSPrecomputedDistancesMatrixLoaderImpl();
        float[][] poDists = pd.loadPrecomPivotsToObjectsDists(dataset.getDatasetName(), dataset.getPivotSetName(), pivotCount);
        List queries = dataset.getMetricQueryObjects();
        queries = queries.subList(0, 1000);;
        List pivots = dataset.getPivots(pivotCount);
        if (poDists == null || poDists.length == 0) {
            pd = ToolsMetricDomain.evaluateMatrixOfDistances(dataset.getMetricObjectsFromDataset(maxObjectsCount), pivots, metricSpace, df);
            poDists = pd.loadPrecomPivotsToObjectsDists(null, null, -1);
        }

        float[][] pivotPivotDists = metricSpace.getDistanceMap(df, pivots, pivots);
        SearchingAlgorithm alg;
        if (filter instanceof TwoPivotsFilter) {
            alg = new KNNSearchWithTwoPivotFiltering(metricSpace, (TwoPivotsFilter) filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), pivotPivotDists, df, LearningPtolemyInequalityWithLimitedAngles.ALL_PIVOT_PAIRS);
        } else if (filter instanceof OnePivotFilter) {
            alg = new KNNSearchWithOnePivotFiltering(metricSpace, (OnePivotFilter) filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), df);
        } else {
            throw new IllegalArgumentException("What a weird algorithm ... This is for the pivot filtering, uh?");
        }
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

    private static BoundsOnDistanceEstimation[] initTestedFilters(int pivotCount, Dataset dataset, int k) {
        String namePrefix = Tools.getDateYYYYMM() + "_" + pivotCount + "_pivots_" + k + "NN";
        OnePivotFilter metricFiltering = new TriangleInequality(namePrefix);
        OnePivotFilter dataDependentMetricFiltering = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(namePrefix,
                pivotCount,
                LearnCoefsForTriangularFilteringWithLimitedAnglesMain.SAMPLE_O_COUNT,
                LearnCoefsForTriangularFilteringWithLimitedAnglesMain.SAMPLE_Q_COUNT,
                dataset
        );
        TwoPivotsFilter fourPointPropertyBased = new FourPointBasedFiltering(namePrefix);
        TwoPivotsFilter ptolemaicFiltering = new PtolemaiosFiltering(namePrefix);
        TwoPivotsFilter dataDependentPtolemaicFiltering = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(
                namePrefix,
                dataset,
                pivotCount,
                LearningPtolemyInequalityWithLimitedAngles.ALL_PIVOT_PAIRS);
//        return new BoundsOnDistanceEstimation[]{metricFiltering, dataDependentMetricFiltering, fourPointPropertyBased, ptolemaicFiltering, dataDependentPtolemaicFiltering};
//        return new BoundsOnDistanceEstimation[]{fourPointPropertyBased, ptolemaicFiltering};
        return new BoundsOnDistanceEstimation[]{dataDependentPtolemaicFiltering};
    }

}
