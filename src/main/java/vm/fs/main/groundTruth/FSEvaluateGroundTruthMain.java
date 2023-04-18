package vm.fs.main.groundTruth;

import java.util.List;
import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.evaluatorsToBeUsed.GroundTruthEvaluator;
import vm.queryResults.QueryNearestNeighboursStoreInterface;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.metricSpace.dataToStringConvertors.SingularisedConvertors;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class FSEvaluateGroundTruthMain {

    public static void main(String[] args) {
//        String datasetName = "random_20dim_uniform_1m";
//        String datasetName = "decaf_1m";
//        String datasetName = "sift_1m";
//        String datasetName = "mpeg7_1m";
//
//        String datasetName = "sift_1m_PCA100";
//        String datasetName = "decaf_1m_PCA16";
        String datasetPrefix = "decaf_1m_PCA";
//        String datasetPrefix = "decaf_1m_GHP_50_";
//        String datasetPrefix = "sift_1m_GHP_50_";
//        String datasetPrefix = "random_20dim_uniform_1m_GHP_50_";
//        String datasetPrefix = "mpeg7_1m_GHP_50_";
//        int[] suffixes = new int[]{256, 128, 192, 64, 512};
//        int[] suffixes = new int[]{10, 12, 128, 1540, 16, 2387, 24, 256, 32, 670, 68, 8};;
        int[] suffixes = new int[]{670};;
        for (int suffix : suffixes) {
            String datasetName = datasetPrefix + suffix;
            String querySetName = datasetName;
            int k = 50000; // 1000 for orig datasets, else 50000
            AbstractMetricSpace space = new FSMetricSpaceImpl();

            DistanceFunctionInterface distanceFunction = space.getDistanceFunctionForDataset(datasetName);
            MetricSpacesStorageInterface spaceStorage = new FSMetricSpacesStorage(space, SingularisedConvertors.FLOAT_VECTOR_SPACE);
            QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();

            List<Object> metricQueryObjects = spaceStorage.getQueryObjects(querySetName);
            GroundTruthEvaluator gte = new GroundTruthEvaluator(space, distanceFunction, metricQueryObjects, k, groundTruthStorage);
            gte.evaluateIteratorInParallel(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
        }
    }

}
