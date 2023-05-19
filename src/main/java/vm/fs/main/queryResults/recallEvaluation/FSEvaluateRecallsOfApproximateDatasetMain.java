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

    public static void main(String[] args) throws InterruptedException {

//        Thread.sleep(1000 * 60 * 90);
        
        Dataset groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_100k_Dataset();
        Dataset[] approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_100k_PCA32Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100k_PCA96Dataset()
        };
        run(groundTruthDataset, approximatedDatasets);
        
        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_300k_Dataset();
        approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_300k_PCA32Dataset(),
            new FSDatasetInstanceSingularizator.LAION_300k_PCA96Dataset()
        };
        run(groundTruthDataset, approximatedDatasets);

        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_10M_Dataset();
        approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_10M_PCA32Dataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_PCA96Dataset()
        };
        run(groundTruthDataset, approximatedDatasets);

        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_30M_Dataset();
        approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_30M_PCA32Dataset(),
            new FSDatasetInstanceSingularizator.LAION_30M_PCA96Dataset()
        };
        run(groundTruthDataset, approximatedDatasets);

        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_100M_Dataset();
        approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_100M_PCA32Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_PCA96Dataset()
        };
        run(groundTruthDataset, approximatedDatasets);
    }

    public static final void run(Dataset groundTruthDataset, Dataset... approximatedDatasets) {
        int k = 10;
//        Integer kCand = null; // null if dynamic, otherwise fixed number
//        int[] kCands = new int[]{110, 120, 125, 130, 140, 150};
//        int[] kCands = new int[]{10, 50, 100, 150, 200, 250};
        int[] kCands = new int[]{150, 200, 250, 300, 400, 500};

        for (Dataset approximatedDataset : approximatedDatasets) {
            for (int kCand : kCands) {
//        String resultName = "pure_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_double_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnDecreasingErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
                String resultName = "ground_truth";
                evaluateRecallOfTheCandidateSet(groundTruthDataset.getDatasetName(), groundTruthDataset.getQuerySettName(), k,
                        approximatedDataset.getDatasetName(), approximatedDataset.getQuerySettName(), resultName, kCand);
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
