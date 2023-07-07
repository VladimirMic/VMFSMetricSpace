package vm.fs.main.groundTruth;

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
public class FSEvaluateGroundTruthMain {

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
//            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_30M_PCA256Dataset()
        };
        for (Dataset dataset : datasets) {
            String datasetName = dataset.getDatasetName();
            String querySetName = dataset.getQuerySetName();
            int k = 10000; // 1000 for orig datasets, else 20000
            AbstractMetricSpace space = dataset.getMetricSpace();

            DistanceFunctionInterface distanceFunction = space.getDistanceFunctionForDataset(datasetName);
            AbstractMetricSpacesStorage spaceStorage = dataset.getMetricSpacesStorage();
            QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();

            List<Object> metricQueryObjects = spaceStorage.getQueryObjects(querySetName);
            GroundTruthEvaluator gte = new GroundTruthEvaluator(space, distanceFunction, metricQueryObjects, k, groundTruthStorage);
            gte.evaluateIteratorInParallel(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
        }
    }

}
