package vm.fs.main.groundTruth;

import java.io.File;
import java.util.List;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.evaluatorsToBeUsed.GroundTruthEvaluator;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
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

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
            //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform()
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries)
        //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
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
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    public static void run(Dataset dataset) {
        String datasetName = dataset.getDatasetName();
        String querySetName = dataset.getQuerySetName();
        int k = GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH; // 1000 for orig datasets, else 20000
        AbstractMetricSpace space = dataset.getMetricSpace();

        DistanceFunctionInterface distanceFunction = space.getDistanceFunctionForDataset(datasetName);
        AbstractMetricSpacesStorage spaceStorage = dataset.getMetricSpacesStorage();
        QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();

        List<Object> metricQueryObjects = spaceStorage.getQueryObjects(querySetName);
        GroundTruthEvaluator gte = new GroundTruthEvaluator(space, distanceFunction, metricQueryObjects, k, groundTruthStorage);
        gte.evaluateIteratorInParallel(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
//            gte.evaluateIteratorSequentially(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
        System.gc();
    }

    public static boolean existsForDataset(Dataset dataset) {
        FSNearestNeighboursStorageImpl groundTruthStorage = new FSNearestNeighboursStorageImpl();
        File fileWithResults = groundTruthStorage.getFileWithResults("ground_truth", dataset.getDatasetName(), dataset.getQuerySetName(), GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH, false);
        return fileWithResults.exists();
    }

}
