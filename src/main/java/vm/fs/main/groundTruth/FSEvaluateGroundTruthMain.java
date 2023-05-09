package vm.fs.main.groundTruth;

import java.util.List;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.evaluatorsToBeUsed.GroundTruthEvaluator;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.queryResults.QueryNearestNeighboursStoreInterface;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class FSEvaluateGroundTruthMain {

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            //            new M2DatasetInstanceSingularizator.DeCAF20MDataset()
            new FSDatasetInstanceSingularizator.LAION_100k_Dataset()
        };
        for (Dataset dataset : datasets) {
            String datasetName = dataset.getDatasetName();
            String querySetName = dataset.getQuerySettName();
            int k = 1000; // 1000 for orig datasets, else 50000
            AbstractMetricSpace space = dataset.getMetricSpace();

            DistanceFunctionInterface distanceFunction = space.getDistanceFunctionForDataset(datasetName);
            MetricSpacesStorageInterface spaceStorage = dataset.getMetricSpacesStorage();
            QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();

            List<Object> metricQueryObjects = spaceStorage.getQueryObjects(querySetName);
            GroundTruthEvaluator gte = new GroundTruthEvaluator(space, distanceFunction, metricQueryObjects, k, groundTruthStorage);
            gte.evaluateIteratorInParallel(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
        }
    }

}
