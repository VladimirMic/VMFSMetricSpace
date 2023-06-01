/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.search.filtering;

import java.util.List;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.filtering.FSSecondaryFilteringWithSketchesStorage;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.nopivot.NoPivotFilter;
import vm.metricSpace.distance.bounding.nopivot.impl.SecondaryFilteringWithSketches;
import vm.metricSpace.distance.bounding.nopivot.learning.LearningSecondaryFilteringWithSketches;
import vm.metricSpace.distance.bounding.nopivot.storeLearned.SecondaryFilteringWithSketchesStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSKNNQueriesSeqScanWithSecondaryFilteringWithSketches {

    private static final Logger LOG = Logger.getLogger(FSKNNQueriesSeqScanWithSecondaryFilteringWithSketches.class.getName());

    public static void main(String[] args) {
        int sketchLength = 256;

        float pCum = 0.75f;

        Dataset[] fullDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAFDataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset()
        };
        Dataset[] sketchesDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_256Dataset()
        };
        float[] distIntervalsForPX = new float[]{
            2,
            0.004f,
            0.004f,
            0.004f,
            0.004f,
            0.004f,
            0.004f
        };
        float[] maxDistsOnFullDataset = new float[]{
            250f,
            2f,
            2f,
            2f,
            2f
        };

        for (int i = 0; i < sketchesDatasets.length; i++) {
            Dataset fullDataset = fullDatasets[i];
            Dataset sketchesDataset = sketchesDatasets[i];
            float distIntervalForPX = distIntervalsForPX[i];
            float maxDistOnFullDataset = maxDistsOnFullDataset[i];
            run(fullDataset, sketchesDataset, distIntervalForPX, sketchLength, maxDistOnFullDataset, pCum);
        }
    }

    private static void run(Dataset fullDataset, Dataset sketchesDataset, float distIntervalForPX, int sketchLength, float maxDistOnFullDataset, float pCum) {
        int k = 100;
        AbstractMetricSpace metricSpace = fullDataset.getMetricSpace();
        DistanceFunctionInterface df = fullDataset.getDistanceFunction();

        List queries = fullDataset.getMetricQueryObjects();
        SecondaryFilteringWithSketchesStoreInterface storage = new FSSecondaryFilteringWithSketchesStorage();
        NoPivotFilter filter = new SecondaryFilteringWithSketches("", fullDataset.getDatasetName(), sketchesDataset, storage, pCum, LearningSecondaryFilteringWithSketches.SKETCHES_SAMPLE_COUNT_FOR_IDIM_PX, LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX, distIntervalForPX);
//        SearchingAlgorithm alg = new KNNSearchWithOnePivotFiltering(metricSpace, filter, pivots, poDists, pd.getRowHeaders(), pd.getColumnHeaders(), df);
//        TreeSet[] results = alg.completeKnnSearchOfQuerySet(metricSpace, queries, k, fullDataset.getMetricObjectsFromDataset());
//
//        LOG.log(Level.INFO, "Storing statistics of queries");
//        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(fullDataset.getDatasetName(), fullDataset.getDatasetName(), k, fullDataset.getDatasetName(), fullDataset.getDatasetName(), filter.getTechFullName(), null);
//        statsStorage.storeStatsForQueries(alg.getDistCompsPerQueries(), alg.getTimesPerQueries());
//        statsStorage.saveFile();
//
//        LOG.log(Level.INFO, "Storing results of queries");
//        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
//        resultsStorage.storeQueryResults(metricSpace, queries, results, fullDataset.getDatasetName(), fullDataset.getDatasetName(), filter.getTechFullName());
//
//        LOG.log(Level.INFO, "Evaluating accuracy of queries");
//        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(fullDataset.getDatasetName(), fullDataset.getDatasetName(), k, fullDataset.getDatasetName(), fullDataset.getDatasetName(), filter.getTechFullName(), null);
//        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(new FSNearestNeighboursStorageImpl(), recallStorage);
//        evaluator.evaluateAndStoreRecallsOfQueries(fullDataset.getDatasetName(), fullDataset.getDatasetName(), k, fullDataset.getDatasetName(), fullDataset.getDatasetName(), filter.getTechFullName(), k);
//        recallStorage.saveFile();
    }

}
