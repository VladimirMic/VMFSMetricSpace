package vm.fs.main.datatools;

import java.io.File;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.search.algorithm.impl.GroundTruthEvaluator;
import vm.fs.dataset.FSDatasetInstances;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.queryResults.QueryNearestNeighboursStoreInterface;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.DatasetOfCandidates;

/**
 *
 * @author Vlada
 */
// see learning of all the metadata for the filtering in FSLearnMetadataForAllPivotFilterings? It includes the ground-truth as well.
public class FSEvaluateGroundTruthMain {

    public static final Logger LOG = Logger.getLogger(FSEvaluateGroundTruthMain.class.getName());

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstances.MOCAP10FPS(),
            new FSDatasetInstances.MOCAP30FPS()
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
        //            new FSDatasetInstanceSingularizator.RandomDataset100Uniform(),
        //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_192Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_256Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_384Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_1024Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries),
        //            
        //            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_192Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_256Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_384Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_1024Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_512Dataset(publicQueries),
        //
        //            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_192Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_256Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_384Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_1024Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_512Dataset(publicQueries)
        //                    new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset()
        //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
        //            new FSDatasetInstanceSingularizator.SIFTdataset(),
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Dot(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Angular(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries), 
        //            new FSDatasetInstanceSingularizator.DeCAFDataset()
//            new FSDatasetInstances.Faiss_Clip_100M_PCA256_Candidates(),
//            new FSDatasetInstances.Faiss_DeCAF_100M_PCA256_Candidates()
        };
        for (Dataset dataset : datasets) {
            run(dataset, GroundTruthEvaluator.K_IMPLICIT_FOR_QUERIES); // multiple iteration due to caching are already employed
            run(dataset, GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH);
        }
    }

    public static void run(Dataset dataset, int k) {
        String datasetName = dataset.getDatasetName();
        AbstractSearchSpace space = dataset.getSearchSpace();

        QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();

        List<Object> queryObjects = dataset.getQueryObjects();
        GroundTruthEvaluator gte = new GroundTruthEvaluator(dataset, k, Float.MAX_VALUE, queryObjects.size());
        TreeSet[] results;
        if (dataset instanceof DatasetOfCandidates) {
            results = gte.evaluateIteratorsSequentiallyForEachQuery(dataset, queryObjects, k);
        } else {
            if (k == GroundTruthEvaluator.K_IMPLICIT_FOR_QUERIES) {
                results = gte.evaluateIteratorSequentially(dataset);
            } else {
                results = gte.evaluateIteratorInParallel(dataset.getSearchObjectsFromDataset(datasetName), datasetName, dataset.getQuerySetName());
            }
        }
        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), "ground_truth", null);
        statsStorage.storeStatsForQueries(gte.getDistCompsPerQueries(), gte.getTimesPerQueries(), gte.getAdditionalStats());
        statsStorage.save();

        LOG.log(Level.INFO, "Storing results of queries");
        groundTruthStorage.storeQueryResults(space, queryObjects, results, k, dataset.getDatasetName(), dataset.getQuerySetName(), "ground_truth");

        FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), "ground_truth", null);
        storeRecallAlwaysOne(dataset.getSearchSpace(), queryObjects, recallStorage);
        System.gc();
    }

    public static boolean existsForDataset(Dataset dataset, Integer k) {
        if (k == null || k <= 0) {
            k = GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH;
        }
        FSNearestNeighboursStorageImpl groundTruthStorage = new FSNearestNeighboursStorageImpl();
        File fileWithResults = groundTruthStorage.getFileWithResults("ground_truth", dataset.getDatasetName(), dataset.getQuerySetName(), k, false);
        return fileWithResults.exists();
    }

    private static void storeRecallAlwaysOne(AbstractSearchSpace searchSpace, List<Object> queryObjects, FSRecallOfCandidateSetsStorageImpl recallStorage) {
        for (Object queryObject : queryObjects) {
            Comparable queryID = searchSpace.getIDOfObject(queryObject);
            recallStorage.storeRecallForQuery(queryID, 1f);
        }
        recallStorage.save();
    }

}
