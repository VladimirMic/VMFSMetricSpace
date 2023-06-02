/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.search.filtering.multipleperform;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.dataTransforms.FSGHPSketchesPivotPairsStorageImpl;
import vm.fs.store.filtering.FSSecondaryFilteringWithSketchesStorage;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.fs.store.voronoiPartitioning.FSVoronoiPartitioningStorage;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.bounding.nopivot.impl.SecondaryFilteringWithSketches;
import vm.metricSpace.distance.bounding.nopivot.learning.LearningSecondaryFilteringWithSketches;
import vm.metricSpace.distance.bounding.nopivot.storeLearned.SecondaryFilteringWithSketchesStoreInterface;
import vm.metricSpace.voronoiPartitioning.StorageLearnedVoronoiPartitioningInterface;
import vm.objTransforms.objectToSketchTransformators.AbstractObjectToSketchTransformator;
import vm.objTransforms.objectToSketchTransformators.SketchingGHP;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;
import vm.search.SearchingAlgorithm;
import vm.search.impl.KNNSearchWithSketchSecondaryFiltering;
import vm.search.impl.VoronoiPartitionsCandSetIdentifier;

/**
 *
 * @author Vlada
 */
public class FSVoronoiPlusSecondaryWithSketches {

    private static final Logger LOG = Logger.getLogger(FSVoronoiPlusSecondaryWithSketches.class.getName());

    public static void main(String[] args) {
        float pCum = 0.75f;
        int sketchLength = 256;
        int pivotCount = 2048;
        Dataset[] fullDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset()
        };
        Dataset[] sketchesDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_256Dataset()
        };
        float[] distIntervalsForPX = new float[]{
            0.004f,
            0.004f,
            0.004f
        };
        int[] voronoiK = new int[]{
            400000,
            400000,
            400000,
        };
        for (int i = 0; i < sketchesDatasets.length; i++) {
            Dataset fullDataset = fullDatasets[i];
            Dataset sketchesDataset = sketchesDatasets[i];
            float distIntervalForPX = distIntervalsForPX[i];
            run(fullDataset, sketchesDataset, distIntervalForPX, pCum, sketchLength, pivotCount, voronoiK[i]);
            System.exit(0);
        }
    }

    private static void run(Dataset fullDataset, Dataset sketchesDataset, float distIntervalForPX, float pCum, int sketchLength, int pivotCountUsedForVoronoiLearning, int voronoiK) {
        int k = 10;
        AbstractMetricSpace metricSpace = fullDataset.getMetricSpace();
        DistanceFunctionInterface df = fullDataset.getDistanceFunction();

        GHPSketchingPivotPairsStoreInterface storageOfPivotPairs = new FSGHPSketchesPivotPairsStorageImpl();
        List pivots = fullDataset.getPivots(-1);

        StorageLearnedVoronoiPartitioningInterface voronoiPartitioningStorage = new FSVoronoiPartitioningStorage();
        SearchingAlgorithm voronoi = new VoronoiPartitionsCandSetIdentifier(fullDataset, voronoiPartitioningStorage, pivotCountUsedForVoronoiLearning);

        AbstractObjectToSketchTransformator sketchingTechnique = new SketchingGHP(df, metricSpace, pivots, false, fullDataset.getDatasetName(), 0.5f, sketchLength, storageOfPivotPairs);
        SecondaryFilteringWithSketchesStoreInterface secondaryFilteringStorage = new FSSecondaryFilteringWithSketchesStorage();
        SecondaryFilteringWithSketches filter = new SecondaryFilteringWithSketches("", fullDataset.getDatasetName(), sketchesDataset, secondaryFilteringStorage, pCum, LearningSecondaryFilteringWithSketches.SKETCHES_SAMPLE_COUNT_FOR_IDIM_PX, LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX, distIntervalForPX);

        SearchingAlgorithm secondaryWithSketches = new KNNSearchWithSketchSecondaryFiltering(fullDataset, filter, sketchingTechnique);

        List queries = fullDataset.getMetricQueryObjects();

        Map keyValueStorage = fullDataset.getKeyValueStorage();
        TreeSet[] results = new TreeSet[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            Object query = queries.get(i);
            List candSetKnnSearch = voronoi.candSetKnnSearch(metricSpace, query, voronoiK, null);
            results[i] = secondaryWithSketches.completeKnnSearch(metricSpace, query, k, candSetKnnSearch.iterator(), keyValueStorage);
        }

        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(fullDataset.getDatasetName(), fullDataset.getQuerySetName(), k, fullDataset.getDatasetName(), fullDataset.getQuerySetName(), filter.getTechFullName(), null);
        statsStorage.storeStatsForQueries(secondaryWithSketches.getDistCompsPerQueries(), secondaryWithSketches.getTimesPerQueries());
        statsStorage.saveFile();

        LOG.log(Level.INFO, "Storing results of queries");
        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
        resultsStorage.storeQueryResults(metricSpace, queries, results, fullDataset.getDatasetName(), fullDataset.getDatasetName(), filter.getTechFullName());

        LOG.log(Level.INFO, "Evaluating accuracy of queries");
        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(fullDataset.getDatasetName(), fullDataset.getDatasetName(), k, fullDataset.getDatasetName(), fullDataset.getDatasetName(), filter.getTechFullName(), null);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(new FSNearestNeighboursStorageImpl(), recallStorage);
        evaluator.evaluateAndStoreRecallsOfQueries(fullDataset.getDatasetName(), fullDataset.getDatasetName(), k, fullDataset.getDatasetName(), fullDataset.getDatasetName(), filter.getTechFullName(), k);
        recallStorage.saveFile();
    }

}
