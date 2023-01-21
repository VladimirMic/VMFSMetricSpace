//package vm.fs.main.groundTruth;
//
//import java.sql.SQLException;
//import java.util.List;
//import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
//import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
//import vm.queryResults.GroundTruthEvaluator;
//import vm.queryResults.QueryNearestNeighboursStoreInterface;
//import vm.metricspace.AbstractMetricSpace;
//import vm.metricspace.MetricSpacesStorageInterface;
//import vm.metricspace.dataToStringConvertors.SingularisedConvertors;
//import vm.metricspace.distance.DistanceFunction;
//
///**
// *
// * @author Vlada
// */
//public class FSEvaluateGroundTruthMain {
//
//    public static void main(String[] args) throws SQLException {
////        String datasetName = "random20uniform_1m";
////        String datasetName = "decaf_1m";
////        String datasetName = "sift_1m";
//        String datasetName = "mpeg7_1m";
////        String datasetName = "sift_1m_PCA100";
////        String datasetName = "decaf_1m_PCA16";
//        String querySetName = datasetName;
//        int k = 50000;
//        AbstractMetricSpace space = new FSMetricSpaceImpl();
//        DistanceFunction distanceFunction = space.getDistanceFunctionForDataset(datasetName);
//        MetricSpacesStorageInterface spaceStorage = new FSMetricSpacesStorage(space, SingularisedConvertors.MPEG7_SPACE);
//        QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();
//
//        evaluateGroundTruth(space, distanceFunction, spaceStorage, groundTruthStorage, querySetName, datasetName, k);
//    }
//
//    public static void evaluateGroundTruth(AbstractMetricSpace space, DistanceFunction distanceFunction, MetricSpacesStorageInterface spaceStorage, QueryNearestNeighboursStoreInterface groundTruthStorage, String querySetName, String datasetName, int k) {
//        List<Object> metricQueryObjects = spaceStorage.getQueryObjects(querySetName);
//        GroundTruthEvaluator gte = new GroundTruthEvaluator(space, distanceFunction, metricQueryObjects, k, groundTruthStorage);
//        gte.processIteratorInParallel(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
//    }
//
//}
