package vm.fs.main.groundTruth;

import java.util.List;
import messif.objects.LocalAbstractObject;
import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.evaluatorsToBeUsed.GroundTruthEvaluator;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.m2.dataset.M2DatasetInstanceSingularizator;
import vm.queryResults.QueryNearestNeighboursStoreInterface;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.metricSpace.dataToStringConvertors.SingularisedConvertors;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class FSEvaluateGroundTruthMain {

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            //            new M2DatasetInstanceSingularizator.DeCAF20MDataset()
            new FSDatasetInstanceSingularizator.DeCAF20M_PCA256Dataset()
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
        for (Dataset dataset : datasets) {
            String datasetName = dataset.getDatasetName();
            String querySetName = datasetName;
            int k = 50000; // 1000 for orig datasets, else 50000
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
