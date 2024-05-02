package vm.fs.main.queryResults.recallEvaluation;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.FSGlobal;
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
        directFiles();
//        forDatasets();
    }

    public static final void run(String folder, String groundTDatasetName, String groundTQuerySetName, String approxDatasetName, String approxQuerySetName) {
        int k = 30;
        Integer[] kCands = new Integer[]{null};
        for (Integer kCand : kCands) {
            evaluateRecallOfTheCandidateSet(groundTDatasetName, groundTQuerySetName, k, approxDatasetName, approxQuerySetName, folder, kCand);
        }
    }

    public static final void run(Dataset groundTruthDataset, Dataset... approximatedDatasets) {
        int k = 30;
//        Integer kCand = null; // null if dynamic, otherwise fixed number
//        int[] kCands = new int[]{100, 110, 120, 125, 130, 140, 150};
        int[] kCands = new int[]{30, 50, 100, 1000};

        for (Dataset approximatedDataset : approximatedDatasets) {
            for (int kCand : kCands) {
//        String resultName = "pure_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_double_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
//        String resultName = "pure_deleteMany_simRel_PCA" + pcaLength + "_decideUsingFirst" + prefixLength + "_learnDecreasingErrorsOn__queries" + querySampleCount + "_dataSamples" + dataSampleCount + "_kSearching" + k + "_percentile" + percentile;
                String resultName = "ground_truth";
                evaluateRecallOfTheCandidateSet(groundTruthDataset.getDatasetName(), groundTruthDataset.getQuerySetName(), k,
                        approximatedDataset.getDatasetName(), approximatedDataset.getQuerySetName(), resultName, kCand);
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
        if (candidateNNCount != null) {
            attributesForFileName.put(FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME.cand_set_fixed_size, candidateNNCount.toString());
        }

        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(attributesForFileName);
        RecallOfCandsSetsEvaluator evaluator = new RecallOfCandsSetsEvaluator(groundTruthStorage, recallStorage);
        evaluator.evaluateAndStoreRecallsOfQueries(groundTruthDatasetName, groundTruthQuerySetName, groundTruthNNCount, candSetName, candSetQuerySetName, resultSetName, candidateNNCount);
        recallStorage.save();
    }

    private static void directFiles() {
        String[] folderNames = {
            "faiss-100M_CLIP_PCA256-IVF-tr20000000-cc262144-qc1000-k100000-nprobe128",
            "faiss-100M_CLIP_PCA256-IVFPQ-tr1000000-cc262144-m64-nbits8-qc1000-k100000",
            "faiss-100M_CLIP_PCA256-IVFPQ-tr1000000-cc262144-m32-nbits8-qc1000-k100000"};

        for (String folderName : folderNames) {
            File folder = new File(FSGlobal.RESULT_FOLDER, folderName);
            String[] files = folder.list((File file, String string) -> string.toLowerCase().endsWith(".gz"));
            for (String fileName : files) {
                Logger.getLogger(FSEvaluateRecallsOfApproximateDatasetMain.class.getName()).log(Level.INFO, "Processing file {0}", fileName);
                fileName = fileName.trim().substring(0, fileName.length() - 3);
                run(folderName, "laion2B-en-clip768v2-n=100M.h5_PCA256", "laion2B-en-clip768v2-n=100M.h5_PCA256", fileName, "");
            }
        }
    }

    private static void forDatasets() {
        boolean publicQueries = true;
        Dataset groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_100k_Dataset(publicQueries);
        Dataset[] approximatedDatasets = new Dataset[]{};

//        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_100k_Dataset(publicQueries);
//        approximatedDatasets = new Dataset[]{
//                        new FSDatasetInstanceSingularizator.LAION_100k_PCA96Dataset()
//        };
//        run(groundTruthDataset, approximatedDatasets);
//
//        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_300k_Dataset();
//        approximatedDatasets = new Dataset[]{
//            new FSDatasetInstanceSingularizator.LAION_300k_PCA96Dataset()
//        };
////        run(groundTruthDataset, approximatedDatasets);
//
        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries);
        approximatedDatasets = new Dataset[]{
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_192Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_256Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_384Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_1024Dataset(publicQueries)
            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
        };
//        run(groundTruthDataset, approximatedDatasets);

        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries);
        approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_192Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_256Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_384Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_512Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_1024Dataset(publicQueries)
        };
//        run(groundTruthDataset, approximatedDatasets);

        groundTruthDataset = new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries);
        approximatedDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_192Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_256Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_384Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_512Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_1024Dataset(publicQueries)
        };
//        run(groundTruthDataset, approximatedDatasets);
    }
}
