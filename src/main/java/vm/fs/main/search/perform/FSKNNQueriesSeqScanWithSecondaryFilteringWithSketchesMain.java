package vm.fs.main.search.perform;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstances;
import vm.fs.store.dataTransforms.FSGHPSketchesPivotPairsStorageImpl;
import vm.fs.store.filtering.FSSecondaryFilteringWithSketchesStorage;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.objTransforms.objectToSketchTransformators.AbstractObjectToSketchTransformator;
import vm.objTransforms.objectToSketchTransformators.SketchingGHP;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;
import vm.search.algorithm.SearchingAlgorithm;
import vm.search.algorithm.impl.KNNSearchWithSketchSecondaryFiltering;
import vm.objTransforms.storeLearned.PivotPairsStoreInterface;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.distance.DistanceFunctionInterface;
import vm.searchSpace.distance.bounding.nopivot.impl.SecondaryFilteringWithSketches;
import vm.searchSpace.distance.bounding.nopivot.learning.LearningSecondaryFilteringWithSketches;
import vm.searchSpace.distance.bounding.nopivot.storeLearned.SecondaryFilteringWithSketchesStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSKNNQueriesSeqScanWithSecondaryFilteringWithSketchesMain {

    private static final Logger LOG = Logger.getLogger(FSKNNQueriesSeqScanWithSecondaryFilteringWithSketchesMain.class.getName());

    public static void main(String[] args) {
        float[] pCums = new float[]{0.45f, 0.5f, 0.55f, 0.6f};
        int sketchLength = 192;
        boolean publicQueries = false;
        Dataset[] fullDatasets = new Dataset[]{
            new FSDatasetInstances.DeCAFDataset(),
            new FSDatasetInstances.LAION_100M_Dataset(publicQueries),
            new FSDatasetInstances.LAION_30M_Dataset(publicQueries),
            new FSDatasetInstances.LAION_10M_Dataset(publicQueries)
        };
        Dataset[] sketchesDatasets = new Dataset[]{
            new FSDatasetInstances.DeCAF_GHP_50_256Dataset(),
            new FSDatasetInstances.LAION_100M_GHP_50_192Dataset(publicQueries),
            new FSDatasetInstances.LAION_30M_GHP_50_192Dataset(publicQueries),
            new FSDatasetInstances.LAION_10M_GHP_50_192Dataset(publicQueries)
        };
        float[] distIntervalsForPX = new float[]{
            2,
            0.004f,
            0.004f,
            0.004f
        };
        for (float pCum : pCums) {
            for (int i = 1; i < sketchesDatasets.length; i++) {
                String pivotPairsFileName = "laion2B-en-clip768v2-n=1M_sample.h5_GHP_50_" + sketchLength;
                Dataset fullDataset = fullDatasets[i];
                Dataset sketchesDataset = sketchesDatasets[i];
                float distIntervalForPX = distIntervalsForPX[i];
                run(fullDataset, sketchesDataset, distIntervalForPX, pCum, sketchLength, pivotPairsFileName);
                System.gc();
            }
        }
    }

    private static void run(Dataset fullDataset, Dataset sketchesDataset, float distIntervalForPX, float pCum, int sketchLength, String pivotPairsFileName) {
        int k = 10;
        AbstractSearchSpace searchSpace = fullDataset.getSearchSpace();
        DistanceFunctionInterface df = fullDataset.getDistanceFunction();

        PivotPairsStoreInterface storageOfPivotPairs = new FSGHPSketchesPivotPairsStorageImpl();
        List pivots = fullDataset.getPivots(-1);
        AbstractObjectToSketchTransformator sketchingTechnique;
        if (pivotPairsFileName == null) {
            sketchingTechnique = new SketchingGHP(df, searchSpace, pivots, fullDataset.getDatasetName(), 0.5f, sketchLength, storageOfPivotPairs);
        } else {
            sketchingTechnique = new SketchingGHP(df, searchSpace, pivots, pivotPairsFileName, storageOfPivotPairs);
        }

        SecondaryFilteringWithSketchesStoreInterface storage = new FSSecondaryFilteringWithSketchesStorage();
        SecondaryFilteringWithSketches filter = new SecondaryFilteringWithSketches(pCum + "_pCum" + sketchLength + "_skLength", fullDataset.getDatasetName(), sketchesDataset, storage, pCum, LearningSecondaryFilteringWithSketches.SKETCHES_SAMPLE_COUNT_FOR_IDIM_PX, LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX, distIntervalForPX);

        SearchingAlgorithm alg = new KNNSearchWithSketchSecondaryFiltering(fullDataset, filter, sketchingTechnique);

        List queries = fullDataset.getQueryObjects();

        TreeSet[] results = alg.completeKnnFilteringWithQuerySet(searchSpace, queries, k, fullDataset.getSearchObjectsFromDataset());

        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(fullDataset.getDatasetName(), fullDataset.getQuerySetName(), k, fullDataset.getDatasetName(), fullDataset.getQuerySetName(), filter.getTechFullName(), null);
        statsStorage.storeStatsForQueries(alg.getDistCompsPerQueries(), alg.getTimesPerQueries(), alg.getAdditionalStats());
        statsStorage.save();

        LOG.log(Level.INFO, "Storing results of queries");
        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
        resultsStorage.storeQueryResults(searchSpace, queries, results, k, fullDataset.getDatasetName(), fullDataset.getQuerySetName(), filter.getTechFullName());

        LOG.log(Level.INFO, "Evaluating accuracy of queries");
        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(fullDataset.getDatasetName(), fullDataset.getQuerySetName(), k, fullDataset.getDatasetName(), fullDataset.getQuerySetName(), filter.getTechFullName(), null);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(new FSNearestNeighboursStorageImpl(), recallStorage);
        evaluator.evaluateAndStoreRecallsOfQueries(fullDataset.getDatasetName(), fullDataset.getQuerySetName(), k, fullDataset.getDatasetName(), fullDataset.getQuerySetName(), filter.getTechFullName(), k);
        recallStorage.save();
    }

}
