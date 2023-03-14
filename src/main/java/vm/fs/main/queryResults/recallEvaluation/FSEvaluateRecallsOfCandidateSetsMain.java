package vm.fs.main.queryResults.recallEvaluation;

import java.util.HashMap;
import java.util.Map;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;

/**
 *
 * @author Vlada
 */
public class FSEvaluateRecallsOfCandidateSetsMain {

    public static void main(String[] args) {
//        String groundTruthDatasetName = "decaf_1m";
//        String groundTruthDatasetName = "decaf_1m_PCA256";
//        String groundTruthDatasetName = "sift_1m";
        String groundTruthDatasetName = "mpeg7_1m";
//        String groundTruthDatasetName = "random_20dim_uniform_1m";
        String groundTruthQuerySetName = groundTruthDatasetName;

        String candSetDatasetPrefix = groundTruthDatasetName + "_GHP_50_";
//        String resultsDataset = "sift_1m_PCA4";
        String candQuerySet = candSetDatasetPrefix;
        int k = 100;
//        Integer kCand = null; // null if dynamic, otherwise fixed number
        int[] kCands = new int[]{10000, 5000, 4000, 2500, 2000};
        int[] sketchLengths = new int[]{512, 256, 192, 128, 64};
//        int[] kCands = new int[]{30, 50, 100, 200, 500, 1000, 2000, 5000};

        int pcaLength = 256;
        int prefixLength = 24;
        int querySampleCount = 100;
        int dataSampleCount = 100000;
        float percentile = 0.85f;

        for (int sketchLength : sketchLengths) {
            String candSetDataset = candSetDatasetPrefix + sketchLength;
            candQuerySet = candSetDataset;
            for (int kCand : kCands) {
//        String resultName = "pure_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_double_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnDecreasingErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
                String resultName = "ground_truth";
                evaluateRecallOfTheCandidateSet(groundTruthDatasetName, groundTruthQuerySetName, k, candSetDataset, candQuerySet, resultName, kCand);
            }

        }
    }

    public static final void evaluateRecallOfTheCandidateSet(String groundTruthDatasetName, String groundTruthQuerySetName, int groundTruthNNCount,
            String candSetName, String candSetQuerySetName, String resultSetName, Integer candidateNNCount) {

        FSNearestNeighboursStorageImpl groundTruthStorage = new FSNearestNeighboursStorageImpl();
        Map<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> attributesForFileName = new HashMap<>();
        attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.ground_truth_name, groundTruthDatasetName);
        attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.ground_truth_query_set_name, groundTruthQuerySetName);
        attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.ground_truth_nn_count, Integer.toString(groundTruthNNCount));
        attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.cand_set_name, candSetName);
        attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.cand_set_query_set_name, candSetQuerySetName);
        attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.storing_result_name, resultSetName);
        attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.cand_set_fixed_size, candidateNNCount.toString());

        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(attributesForFileName);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(groundTruthStorage, recallStorage);
        evaluator.evaluateAndStoreRecallsOfQueries(groundTruthDatasetName, groundTruthQuerySetName, groundTruthNNCount, candSetName, candSetQuerySetName, resultSetName, candidateNNCount);
        recallStorage.saveFile();
    }
}
