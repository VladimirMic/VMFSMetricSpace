package vm.fs.main.search.perform;

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
import vm.metricSpace.DatasetOfCandidates;
import vm.metricSpace.ToolsMetricDomain;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.BoundsOnDistanceEstimation;
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
import vm.search.algorithm.impl.GroundTruthEvaluator;
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
            //            new FSDatasetInstanceSingularizator.Faiss_Clip_100M_PCA256_Candidates(),
            //            new FSDatasetInstanceSingularizator.Faiss_DeCAF_100M_PCA256_Candidates()
            new FSDatasetInstanceSingularizator.Faiss_DeCAF_100M_Candidates()
        //            new FSDatasetInstanceSingularizator.SIFTdataset(),
        //            new FSDatasetInstanceSingularizator.DeCAFDataset(),
        //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Angular(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Dot(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.RandomDataset10Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset25Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset30Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset35Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset40Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset50Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset60Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset70Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset80Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset90Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset100Uniform()
        };

        int pivotCount = SearchingAlgorithm.IMPLICIT_PIVOT_COUNT;
        int k = GroundTruthEvaluator.K_IMPLICIT_FOR_QUERIES;

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
        LOG.log(Level.INFO, "Going to search for {0}NN in dataset {1} with the filter {2}", new Object[]{k, dataset.getDatasetName(), filter.getTechFullName()});
        int maxObjectsCount = -1;
        int pivotCount = pivots.size();
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();

        initPODists(dataset, pivotCount, maxObjectsCount, pivots, metricSpace, df);

        List queries = dataset.getQueryObjects(1000);

        float[][] pivotPivotDists = metricSpace.getDistanceMap(df, pivots, pivots);

        SearchingAlgorithm alg;
        if (filter instanceof AbstractPtolemaicBasedFiltering) {
            alg = new KNNSearchWithPtolemaicFiltering(metricSpace, (AbstractPtolemaicBasedFiltering) filter, pivots, poDists, pd.getRowHeaders(), df);
            if (filter instanceof DataDependentGeneralisedPtolemaicFiltering && dataset.equals(new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset())) {
                KNNSearchWithPtolemaicFiltering tmp = (KNNSearchWithPtolemaicFiltering) alg;
//STRAIN
                tmp.setObjBeforeSeqScan(100000);
                tmp.setThresholdOnLBsPerObjForSeqScan(20);
            }
        } else if (filter instanceof AbstractTwoPivotsFilter) {
            alg = new KNNSearchWithGenericTwoPivotFiltering(metricSpace, (AbstractTwoPivotsFilter) filter, pivots, poDists, pd.getRowHeaders(), pivotPivotDists, df);
        } else if (filter instanceof AbstractOnePivotFilter) {
            alg = new KNNSearchWithOnePivotFiltering(metricSpace, (AbstractOnePivotFilter) filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), df);
        } else {
            throw new IllegalArgumentException("What a weird algorithm ... This class is for the pivot filtering, did you notice?");
        }

        TreeSet[] results;
        if (dataset instanceof DatasetOfCandidates) {
            results = alg.evaluateIteratorsSequentiallyForEachQuery(dataset, queries, k);
        } else {
            results = alg.completeKnnFilteringWithQuerySet(metricSpace, queries, k, dataset.getMetricObjectsFromDataset(maxObjectsCount), 1);
        }

        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), alg.getResultName(), null);
        statsStorage.storeStatsForQueries(alg.getDistCompsPerQueries(), alg.getTimesPerQueries(), alg.getAddditionalStats());
        statsStorage.save();

        LOG.log(Level.INFO, "Storing results of queries");
        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
        resultsStorage.storeQueryResults(metricSpace, queries, results, k, dataset.getDatasetName(), dataset.getQuerySetName(), alg.getResultName());

        LOG.log(Level.INFO, "Evaluating accuracy of queries");
        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), alg.getResultName(), null);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(new FSNearestNeighboursStorageImpl(), recallStorage);
        Dataset groundTruthDataset = dataset;
        if (dataset instanceof DatasetOfCandidates) {
            groundTruthDataset = ((DatasetOfCandidates) dataset).getOrigDataset();
        }
        evaluator.evaluateAndStoreRecallsOfQueries(groundTruthDataset.getDatasetName(), groundTruthDataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), alg.getResultName(), k);
        recallStorage.save();
    }

    private static void initPODists(Dataset dataset, int pivotCount, int maxObjectsCount, List pivots, AbstractMetricSpace metricSpace, DistanceFunctionInterface df) {
        Dataset origDataset = dataset;
        if (dataset instanceof DatasetOfCandidates) {
            origDataset = ((DatasetOfCandidates) dataset).getOrigDataset();
        }
        if (pd == null) {
            pd = new FSPrecomputedDistancesMatrixLoaderImpl();
            poDists = pd.loadPrecomPivotsToObjectsDists(origDataset, pivotCount);
        }
        if (poDists == null || poDists.length == 0) {
            int precomputedDatasetSize = origDataset.getPrecomputedDatasetSize();
            pd = ToolsMetricDomain.evaluateMatrixOfDistances(origDataset.getMetricObjectsFromDataset(maxObjectsCount), pivots, metricSpace, df, precomputedDatasetSize);
            poDists = pd.loadPrecomPivotsToObjectsDists(null, -1);
        }
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
        AbstractOnePivotFilter dataDependentMetricFiltering = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(
                namePrefix,
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
//        return new BoundsOnDistanceEstimation[]{metricFiltering, dataDependentMetricFiltering, fourPointPropertyBased};
//        return new BoundsOnDistanceEstimation[]{dataDependentMetricFiltering, metricFiltering, ptolemaicFiltering, fourPointPropertyBased};
//        return new BoundsOnDistanceEstimation[]{dataDependentMetricFiltering};
        return new BoundsOnDistanceEstimation[]{
//            dataDependentMetricFiltering, dataDependentMetricFiltering,
            dataDependentPtolemaicFiltering, dataDependentPtolemaicFiltering,
            //            metricFiltering, metricFiltering,
            fourPointPropertyBased, fourPointPropertyBased,
            ptolemaicFiltering, ptolemaicFiltering
        };
//        return new BoundsOnDistanceEstimation[]{dataDependentMetricFiltering, metricFiltering, fourPointPropertyBased, dataDependentPtolemaicFiltering, ptolemaicFiltering};
//        return new BoundsOnDistanceEstimation[]{metricFiltering, dataDependentMetricFiltering, fourPointPropertyBased, ptolemaicFiltering};
//        return new BoundsOnDistanceEstimation[]{metricFiltering, dataDependentMetricFiltering, fourPointPropertyBased, ptolemaicFiltering, dataDependentPtolemaicFiltering};
    }

}
