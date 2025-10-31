package vm.fs.main.search.perform;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstances;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixSerializatorImpl;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;
import vm.search.algorithm.SearchingAlgorithm;
import vm.search.algorithm.impl.GroundTruthEvaluator;
import vm.search.algorithm.impl.KNNSearchWithOnePivotFiltering;
import vm.search.algorithm.impl.KNNSearchWithGenericTwoPivotFiltering;
import vm.search.algorithm.impl.KNNSearchWithPtolemaicFiltering;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.DatasetOfCandidates;
import vm.searchSpace.ToolsSpaceDomain;
import vm.searchSpace.distance.DistanceFunctionInterface;
import vm.searchSpace.distance.bounding.BoundsOnDistanceEstimation;
import vm.searchSpace.distance.bounding.onepivot.AbstractOnePivotFilter;
import vm.searchSpace.distance.bounding.onepivot.impl.TriangleInequality;
import vm.searchSpace.distance.bounding.twopivots.AbstractPtolemaicBasedFiltering;
import vm.searchSpace.distance.bounding.twopivots.AbstractTwoPivotsFilter;
import vm.searchSpace.distance.bounding.twopivots.impl.DataDependentPtolemaicFiltering;
import vm.searchSpace.distance.bounding.twopivots.impl.FourPointBasedFiltering;
import vm.searchSpace.distance.bounding.twopivots.impl.PtolemaicFiltering;
import vm.searchSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixSerializator;
import vm.searchSpace.distance.storedPrecomputedDistances.MainMemoryStoredPrecomputedDistances;

/**
 *
 * @author Vlada
 */
public class FSKNNQueriesSeqScanWithFilteringMain {

    private static final Logger LOG = Logger.getLogger(FSKNNQueriesSeqScanWithFilteringMain.class.getName());
    private static final Integer QUERIES_COUNT = -1;

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{ //            new FSDatasetInstances.DeCAF(),
        //            new FSDatasetInstances.MOCAP10FPS(),
        //            new FSDatasetInstances.MOCAP30FPS(),
        //            new FSDatasetInstances.DeCAFDataset()
        //                        new FSDatasetInstances.LAION_10M_PCA256Dataset(),
        //            new FSDatasetInstances.LAION_30M_PCA256Dataset(),
        //            new FSDatasetInstances.LAION_100M_PCA256Dataset()
        //            new FSDatasetInstances.Faiss_Clip_100M_PCA256_Candidates()
        //            new FSDatasetInstances.Faiss_DeCAF_100M_Candidates()
        //            new FSDatasetInstanceSingularizator.Faiss_DeCAF_100M_PCA256_Candidates()
        //            new FSDatasetInstanceSingularizator.DeCAFDataset(),
        //            new FSDatasetInstanceSingularizator.SIFTdataset(),
        //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Angular(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Dot(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.RandomDataset10Uniform()
        //            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset25Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset30Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset35Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset40Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset50Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset60Uniform(),
        //                    new FSDatasetInstanceSingularizator.RandomDataset70Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset80Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset90Uniform(),
        //                    new FSDatasetInstanceSingularizator.RandomDataset100Uniform()
        };

        int k = GroundTruthEvaluator.K_IMPLICIT_FOR_QUERIES;

        for (Dataset dataset : datasets) {
            int pivotCount = dataset.getRecommendedNumberOfPivotsForFiltering();
            if (pivotCount < 0) {
                throw new IllegalArgumentException("Dataset " + dataset.getDatasetName() + " does not specify the number of pivots");
            }
            List pivots = dataset.getPivots(pivotCount);
            BoundsOnDistanceEstimation[] filters = initTestedFilters(null, pivots, dataset, k);
            run(dataset, filters, pivots, k);
            System.gc();
            pd = null;
        }
    }

    private static AbstractPrecomputedDistancesMatrixSerializator pd;
    private static float[][] poDists = null;

    public static final void run(Dataset dataset, BoundsOnDistanceEstimation[] filters, List pivots, int k) {
        for (BoundsOnDistanceEstimation filter : filters) {
            Logger.getLogger(FSKNNQueriesSeqScanWithFilteringMain.class.getName()).log(Level.INFO, "Processing filter {0}", filter.getTechFullName());
            run(dataset, filter, pivots, k);
        }
        // due to caching, first filter is evaluated again
        if (dataset instanceof DatasetOfCandidates) {
            Logger.getLogger(FSKNNQueriesSeqScanWithFilteringMain.class.getName()).log(Level.INFO, "Processing filter {0}", filters[0].getTechFullName());
            run(dataset, filters[0], pivots, k);
        }
    }

    private static void run(Dataset dataset, BoundsOnDistanceEstimation filter, List pivots, int k) {
        LOG.log(Level.INFO, "Going to search for {0}NN in dataset {1} with the filter {2}", new Object[]{k, dataset.getDatasetName(), filter.getTechFullName()});
        int maxObjectsCount = -1;
        int pivotCount = pivots.size();
        AbstractSearchSpace searchSpace = dataset.getSearchSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();

        initPODists(dataset, pivotCount, maxObjectsCount, pivots);
        int qCount;
        String datasetName = dataset.getDatasetName();
        if (datasetName.contains("actions-single-subject-all-POS")) {
            qCount = -1;
        } else {
            qCount = QUERIES_COUNT;
        }
        List queries = dataset.getQueryObjects(qCount);

        float[][] pivotPivotDists = searchSpace.getDistanceMap(df, pivots, pivots);

        int repetitions = SearchingAlgorithm.getNumberOfRepetitionsDueToCaching(dataset);
        evalAndStore(dataset, queries, k, searchSpace, filter, pivots, df, pivotPivotDists, repetitions);
    }

    public static void initPODists(Dataset dataset, int pivotCount, int maxObjectsCount, List pivots) {
        initPODists(dataset, pivotCount, maxObjectsCount, pivots, true);
    }

    public static void initPODists(Dataset dataset, int pivotCount, int maxObjectsCount, List pivots, boolean allowCache) {
        AbstractSearchSpace searchSpace = dataset.getSearchSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        Dataset origDataset = dataset;
        if (dataset instanceof DatasetOfCandidates) {
            origDataset = ((DatasetOfCandidates) dataset).getOrigDataset();
        }
        if (!allowCache) {
            pd = null;
        }
        if (pd == null) {
            pd = new FSPrecomputedDistancesMatrixSerializatorImpl();
            poDists = pd.loadPrecomPivotsToObjectsDists(origDataset, pivotCount).getDists();
        }
        if (poDists == null || poDists.length == 0) {
            int precomputedDatasetSize = origDataset.getPrecomputedDatasetSize();
            pd = ToolsSpaceDomain.evaluateMatrixOfDistances(origDataset.getSearchObjectsFromDataset(maxObjectsCount), pivots, searchSpace, df, precomputedDatasetSize);
            poDists = pd.loadPrecomPivotsToObjectsDists(null, -1).getDists();
        }

    }

    public static AbstractPrecomputedDistancesMatrixSerializator getPd() {
        return pd;
    }

    public static float[][] getPoDists() {
        return poDists;
    }

    public static SearchingAlgorithm initAlg(BoundsOnDistanceEstimation filter, Dataset dataset, AbstractSearchSpace searchSpace, List pivots, DistanceFunctionInterface df) {
        return initAlg(filter, dataset, searchSpace, pivots, df, null);
    }

    public static SearchingAlgorithm initAlg(BoundsOnDistanceEstimation filter, Dataset dataset, AbstractSearchSpace searchSpace, List pivots, DistanceFunctionInterface df, float[][] pivotPivotDists) {
        SearchingAlgorithm alg;
        if (pd == null) {
            initPODists(dataset, pivots.size(), -1, pivots);
        }
        Map<Comparable, Integer> rowHeaders = pd == null ? null : pd.getRowHeaders();
        Map<Comparable, Integer> columnHeaders = pd == null ? null : pd.getColumnHeaders();
        if (filter instanceof AbstractPtolemaicBasedFiltering) {
            alg = new KNNSearchWithPtolemaicFiltering(searchSpace, (AbstractPtolemaicBasedFiltering) filter, pivots, poDists, rowHeaders, df);
            if (filter instanceof DataDependentPtolemaicFiltering && dataset.equals(new FSDatasetInstances.LAION_10M_PCA256Dataset())) {
                KNNSearchWithPtolemaicFiltering tmp = (KNNSearchWithPtolemaicFiltering) alg;
//STRAIN
                tmp.setObjBeforeSeqScan(100000);
                tmp.setThresholdOnLBsPerObjForSeqScan(20);
            }
            if (filter instanceof DataDependentPtolemaicFiltering && dataset.equals(new FSDatasetInstances.Faiss_Clip_100M_PCA256_Candidates())) {
                KNNSearchWithPtolemaicFiltering tmp = (KNNSearchWithPtolemaicFiltering) alg;
//STRAIN
                tmp.setObjBeforeSeqScan(20);
                tmp.setThresholdOnLBsPerObjForSeqScan(62.5f);
            }
        } else if (filter instanceof AbstractTwoPivotsFilter) {
            if (pivotPivotDists == null) {
                pivotPivotDists = searchSpace.getDistanceMap(df, pivots, pivots);
            }
            alg = new KNNSearchWithGenericTwoPivotFiltering(searchSpace, (AbstractTwoPivotsFilter) filter, pivots, poDists, rowHeaders, pivotPivotDists, df);
        } else if (filter instanceof AbstractOnePivotFilter) {
            alg = new KNNSearchWithOnePivotFiltering(searchSpace, (AbstractOnePivotFilter) filter, pivots, poDists, rowHeaders, columnHeaders, df);
        } else {
            throw new IllegalArgumentException("What a weird algorithm ... This class is for the pivot filtering, did you notice?");
        }
        return alg;
    }

    public static final BoundsOnDistanceEstimation[] initTestedFilters(String resultSetPrefix, List pivots, Dataset dataset, Integer k) {
        int pivotCount = pivots.size();
        List pivotsData = dataset.getSearchSpace().getDataOfObjects(pivots);
        if (resultSetPrefix == null) {
            resultSetPrefix = Tools.getDateYYYYMM() + "_" + pivotCount + "_pivots";
        }
        if (k != null) {
            resultSetPrefix += "_" + k + "NN";
        }
        AbstractOnePivotFilter metricFiltering = new TriangleInequality(resultSetPrefix);
        AbstractOnePivotFilter dataDependentMetricFiltering = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(resultSetPrefix, pivotCount, dataset);
        AbstractTwoPivotsFilter fourPointPropertyBased = new FourPointBasedFiltering(resultSetPrefix);

        AbstractPtolemaicBasedFiltering ptolemaicFilteringRandomPivots = new PtolemaicFiltering(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), false);
        AbstractPtolemaicBasedFiltering ptolemaicFiltering = new PtolemaicFiltering(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), true);
        DataDependentPtolemaicFiltering dataDependentPtolemaicFiltering = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(
                resultSetPrefix,
                dataset,
                pivotCount
        );
        return new BoundsOnDistanceEstimation[]{
            metricFiltering,
            dataDependentMetricFiltering,
            fourPointPropertyBased,
            ptolemaicFilteringRandomPivots,
            ptolemaicFiltering,
            dataDependentPtolemaicFiltering
        };
    }

    private static void store(SearchingAlgorithm alg, TreeSet[] results, Dataset dataset, AbstractSearchSpace metricSpace, List queries, int k) {
        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), alg.getResultName(), null);
        statsStorage.storeStatsForQueries(alg.getDistCompsPerQueries(), alg.getTimesPerQueries(), alg.getAdditionalStats());
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

    private static void evalAndStore(Dataset dataset, List queries, int k, AbstractSearchSpace searchSpace, BoundsOnDistanceEstimation filter, List pivots, DistanceFunctionInterface df, float[][] pivotPivotDists, int repetitions) {
        if (dataset == null) {
            return;
        }
        for (int i = 0; i < repetitions; i++) {
            SearchingAlgorithm alg = initAlg(filter, dataset, searchSpace, pivots, df, pivotPivotDists);
            if (dataset instanceof DatasetOfCandidates) {
                DatasetOfCandidates cast = (DatasetOfCandidates) dataset;
                int candidatesProvided = cast.getCandidatesProvided();
                Map<Integer, TreeSet<Map.Entry<Comparable, Float>>[]> mapOfResultsForCandidateSetSizes = alg.evaluateIteratorsSequentiallyForEachQuery(dataset, queries, k, true, candidatesProvided);
                for (Map.Entry<Integer, TreeSet<Map.Entry<Comparable, Float>>[]> entry : mapOfResultsForCandidateSetSizes.entrySet()) {
                    Integer candSetSize = entry.getKey();
                    TreeSet<Map.Entry<Comparable, Float>>[] results = entry.getValue();
                    cast.setMaxNumberOfCandidatesToReturn(candSetSize);
                    store(alg, results, dataset, searchSpace, queries, k);
                }
            } else {
                TreeSet[] results = alg.completeKnnFilteringWithQuerySet(searchSpace, queries, k, dataset.getSearchObjectsFromDataset(-1), 1);
                store(alg, results, dataset, searchSpace, queries, k);
            }
        }
    }

}
