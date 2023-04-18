package vm.fs.main.queryResults.recallEvaluation;

import java.util.HashMap;
import java.util.Map;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.metricSpace.Dataset;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsEvaluator;

/**
 *
 * @author Vlada
 */
public class FSEvaluateRecallsOfApproximateDatasetMain {

    public static void main(String[] args) {
        Dataset groundTruthDataset = new FSDatasetInstanceSingularizator.DeCAFDataset();
        Dataset[] approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAF_PCA12Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA16Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA24Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA32Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA46Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA68Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA128Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA256Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA670Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_PCA1540Dataset()
        };
//        String resultsDataset = "sift_1m_PCA4";
        int k = 30;
//        Integer kCand = null; // null if dynamic, otherwise fixed number
//        int[] kCands = new int[]{30, 50, 75, 80, 100, 1000, 5000, 10000};
        int[] kCands = new int[]{5000, 10000, 15000, 20000};

        for (Dataset approximatedDataset : approximatedDatasets) {
            for (int kCand : kCands) {
//        String resultName = "pure_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_double_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnDecreasingErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
                String resultName = "ground_truth";
                evaluateRecallOfTheCandidateSet(groundTruthDataset.getDatasetName(), groundTruthDataset.getDatasetName(), k, approximatedDataset.getDatasetName(), approximatedDataset.getDatasetName(), resultName, kCand);
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
