package vm.fs.main.search.filtering.perform;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentMetricFilteringMain;
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
import vm.metricSpace.distance.bounding.nopivot.impl.TrivialIneffectiveBound;
import vm.metricSpace.distance.bounding.onepivot.AbstractOnePivotFilter;
import vm.metricSpace.distance.bounding.onepivot.impl.TriangleInequality;
import vm.metricSpace.distance.bounding.twopivots.AbstractPtolemaicBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.AbstractTwoPivotsFilter;
import vm.metricSpace.distance.bounding.twopivots.impl.DataDependentGeneralisedPtolemaicFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.FourPointBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaicFiltering;
import vm.metricSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixLoader;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;
import vm.search.algorithm.SearchingAlgorithm;
import vm.search.algorithm.impl.KNNSearchWithOnePivotFiltering;
import vm.search.algorithm.impl.KNNSearchWithGenericTwoPivotFiltering;
import vm.search.algorithm.impl.KNNSearchWithPtolemaicFiltering;

/**
 *
 * @author Vlada
 */
public class FSKNNQueriesSeqScanWithFilteringMain {

    private static final Logger LOG = Logger.getLogger(FSKNNQueriesSeqScanWithFilteringMain.class.getName());

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
            //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
            //            new FSDatasetInstanceSingularizator.SIFTdataset(),
            //            new FSDatasetInstanceSingularizator.DeCAFDataset(),
            //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
            //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries)
            new FSDatasetInstanceSingularizator.RandomDataset10Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset25Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset30Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset35Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset40Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset50Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset60Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset70Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset80Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset90Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset100Uniform()
        };

        int pivotCount = 256;
        int k = 30;

        for (Dataset dataset : datasets) {
            List pivots = dataset.getPivots(pivotCount);
            BoundsOnDistanceEstimation[] filters = initTestedFilters(pivots, dataset, k);
            for (BoundsOnDistanceEstimation filter : filters) {
                run(dataset, filter, pivots, k);
                System.gc();
            }
            pd = null;
        }
    }

    private static AbstractPrecomputedDistancesMatrixLoader pd;
    private static float[][] poDists = null;

    private static void run(Dataset dataset, BoundsOnDistanceEstimation filter, List pivots, int k) {
        int maxObjectsCount = -1;
        int pivotCount = pivots.size();
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();

        if (pd == null) {
            pd = new FSPrecomputedDistancesMatrixLoaderImpl();
            poDists = pd.loadPrecomPivotsToObjectsDists(dataset, pivotCount);
        }

        List queries = dataset.getMetricQueryObjects();
        queries = queries.subList(0, 1000);

        if (poDists == null || poDists.length == 0) {
            pd = ToolsMetricDomain.evaluateMatrixOfDistances(dataset.getMetricObjectsFromDataset(maxObjectsCount), pivots, metricSpace, df);
            poDists = pd.loadPrecomPivotsToObjectsDists(null, -1);
        }
        float[][] pivotPivotDists = metricSpace.getDistanceMap(df, pivots, pivots);

        SearchingAlgorithm alg;
        if (filter instanceof AbstractPtolemaicBasedFiltering) {
            alg = new KNNSearchWithPtolemaicFiltering(metricSpace, (AbstractPtolemaicBasedFiltering) filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), df);
        } else if (filter instanceof AbstractTwoPivotsFilter) {
            alg = new KNNSearchWithGenericTwoPivotFiltering(metricSpace, (AbstractTwoPivotsFilter) filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), pivotPivotDists, df);
        } else if (filter instanceof AbstractOnePivotFilter) {
            alg = new KNNSearchWithOnePivotFiltering(metricSpace, (AbstractOnePivotFilter) filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), df);
        } else {
            throw new IllegalArgumentException("What a weird algorithm ... This is for the pivot filtering, uh?");
        }
        TreeSet[] results = alg.completeKnnFilteringWithQuerySet(metricSpace, queries, k, dataset.getMetricObjectsFromDataset(maxObjectsCount), 1);

        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName(), null);
        statsStorage.storeStatsForQueries(alg.getDistCompsPerQueries(), alg.getTimesPerQueries(), alg.getAddditionalStats());
        statsStorage.save();

        LOG.log(Level.INFO, "Storing results of queries");
        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
        resultsStorage.storeQueryResults(metricSpace, queries, results, k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName());

        LOG.log(Level.INFO, "Evaluating accuracy of queries");
        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName(), null);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(new FSNearestNeighboursStorageImpl(), recallStorage);
        evaluator.evaluateAndStoreRecallsOfQueries(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), filter.getTechFullName(), k);
        recallStorage.save();
    }

    private static BoundsOnDistanceEstimation[] initTestedFilters(List pivots, Dataset dataset, int k) {
        int pivotCount = pivots.size();
        List pivotsData = dataset.getMetricSpace().getDataOfMetricObjects(pivots);
        String namePrefix = Tools.getDateYYYYMM() + "_" + pivotCount + "_pivots_" + k + "NN";
        if (KNNSearchWithOnePivotFiltering.SORT_PIVOTS) {
            namePrefix += "_SortedP";
        }
        namePrefix += "_seq";
        AbstractOnePivotFilter metricFiltering = new TriangleInequality(namePrefix);
        AbstractOnePivotFilter dataDependentMetricFiltering = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(namePrefix,
                pivotCount,
                FSLearnCoefsForDataDepenentMetricFilteringMain.SAMPLE_O_COUNT,
                FSLearnCoefsForDataDepenentMetricFilteringMain.SAMPLE_Q_COUNT,
                dataset
        );
        AbstractTwoPivotsFilter fourPointPropertyBased = new FourPointBasedFiltering(namePrefix);

        AbstractPtolemaicBasedFiltering ptolemaicFiltering = new PtolemaicFiltering(namePrefix, pivotsData, dataset.getDistanceFunction());
        DataDependentGeneralisedPtolemaicFiltering dataDependentPtolemaicFiltering = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(
                namePrefix,
                dataset,
                pivotCount
        );
        AbstractOnePivotFilter trivial = new TrivialIneffectiveBound(namePrefix);
//        return new BoundsOnDistanceEstimation[]{dataDependentMetricFiltering, metricFiltering, fourPointPropertyBased};
//        return new BoundsOnDistanceEstimation[]{dataDependentPtolemaicFiltering};
//        return new BoundsOnDistanceEstimation[]{ptolemaicFiltering};
//        return new BoundsOnDistanceEstimation[]{trivial};
        return new BoundsOnDistanceEstimation[]{dataDependentMetricFiltering, metricFiltering, fourPointPropertyBased, dataDependentPtolemaicFiltering, ptolemaicFiltering};
//        return new BoundsOnDistanceEstimation[]{metricFiltering, dataDependentMetricFiltering, fourPointPropertyBased, ptolemaicFiltering, dataDependentPtolemaicFiltering};
    }

}
