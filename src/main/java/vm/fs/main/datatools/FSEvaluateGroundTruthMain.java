package vm.fs.main.datatools;

import java.io.File;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.evaluatorsToBeUsed.GroundTruthEvaluator;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.queryResults.QueryNearestNeighboursStoreInterface;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.AbstractMetricSpacesStorage;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
// how about learning all the metadata for the filtering in FSLearnMetadataForAllPivotFilterings at once? It includes the ground-truth as well.
public class FSEvaluateGroundTruthMain {

    public static final Logger LOG = Logger.getLogger(FSEvaluateGroundTruthMain.class.getName());

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
            //            new FSDatasetInstanceSingularizator.DeCAFDataset(),
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
            //                    new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries)
            //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
            //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_192Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_256Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_384Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_1024Dataset(publicQueries),
//                    new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(publicQueries)
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
                    new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    public static void run(Dataset dataset) {
        String datasetName = dataset.getDatasetName();
        String querySetName = dataset.getQuerySetName();
        int k = GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH;
        AbstractMetricSpace space = dataset.getMetricSpace();

        DistanceFunctionInterface distanceFunction = space.getDistanceFunctionForDataset(datasetName);
        AbstractMetricSpacesStorage spaceStorage = dataset.getMetricSpacesStorage();
        QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();

        List<Object> metricQueryObjects = spaceStorage.getQueryObjects(querySetName);
        GroundTruthEvaluator gte = new GroundTruthEvaluator(space, distanceFunction, metricQueryObjects, k);
        TreeSet[] results = gte.evaluateIteratorInParallel(spaceStorage.getObjectsFromDataset(datasetName, 1000000), datasetName, querySetName);
//        TreeSet[] results = gte.evaluateIteratorSequentially(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);

        LOG.log(Level.INFO, "Storing statistics of queries");
        FSQueryExecutionStatsStoreImpl statsStorage = new FSQueryExecutionStatsStoreImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), "ground_truth", null);
        statsStorage.storeStatsForQueries(gte.getDistCompsPerQueries(), gte.getTimesPerQueries(), gte.getAddditionalStats());
        statsStorage.save();

        LOG.log(Level.INFO, "Storing results of queries");
        groundTruthStorage.storeQueryResults(space, metricQueryObjects, results, k, dataset.getDatasetName(), dataset.getQuerySetName(), "ground_truth");

        System.gc();
    }

    public static boolean existsForDataset(Dataset dataset) {
        FSNearestNeighboursStorageImpl groundTruthStorage = new FSNearestNeighboursStorageImpl();
        File fileWithResults = groundTruthStorage.getFileWithResults("ground_truth", dataset.getDatasetName(), dataset.getQuerySetName(), GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH, false);
        return fileWithResults.exists();
    }

}
