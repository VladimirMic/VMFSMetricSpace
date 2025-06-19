package vm.fs.main.datatools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeSet;
import vm.search.algorithm.impl.GroundTruthEvaluator;
import vm.fs.dataset.FSDatasetInstances;
import vm.searchSpace.ToolsSpaceDomain;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class FSPrintDDOfNearNeighboursAndDatasetOrigAndTransformedMain {

    public static void main(String[] args) {
        Dataset datasetOrig = new FSDatasetInstances.DeCAFDataset();
        Dataset datasetTransformed = new FSDatasetInstances.DeCAF_PCA10Dataset();
        String datasetName = datasetOrig.getDatasetName();
        String transformedDatasetName = datasetTransformed.getDatasetName();
        float transformedDistInterval = 1.0f;

//      getHistogramsForRandomAndNearestNeighbours
        int objCount = FSPrintAndPlotDDOfDatasetMain.IMPLICIT_OBJ_COUNT;//100,000
        int distCount = FSPrintAndPlotDDOfDatasetMain.IMPLICIT_DIST_COUNT;//1000,000
        int queriesCount = 1;//1000
        int k = 100;
        List<Object[]> idsOfRandomPairs = new ArrayList<>();
        List<Object[]> idsOfNNPairs = new ArrayList<>();
        SortedMap<Float, Float> ddRandomSample = ToolsSpaceDomain.createDistanceDensityPlot(datasetOrig, objCount, distCount, idsOfRandomPairs);
        float distInterval = ToolsSpaceDomain.computeBasicDistInterval(ddRandomSample.lastKey());
        SortedMap<Float, Float> ddOfNNSample = createDDOfNNSample(datasetOrig, queriesCount, objCount, k, idsOfNNPairs);
//      print
        printDDOfRandomAndNearNeighbours(datasetName, distInterval, ddRandomSample, ddOfNNSample);

//      find the same pairs in the transformed dataset and print corresponding distances
        List<Object> transformedObjects = datasetTransformed.getSearchSpacesStorage().getSampleOfDataset(transformedDatasetName, -1);
        AbstractSearchSpace searchSpace = datasetTransformed.getSearchSpace();
        Map<Comparable, Object> searchObjectsAsIdObjectMap = ToolsSpaceDomain.getSearchObjectsAsIdDataMap(searchSpace, transformedObjects);
        DistanceFunctionInterface distanceFunctionForTransformedDataset = searchSpace.getDistanceFunction();
        SortedMap<Float, Float> ddRandomSampleTransformed = evaluateDDForPairs(distanceFunctionForTransformedDataset, idsOfRandomPairs, searchObjectsAsIdObjectMap);
        SortedMap<Float, Float> ddOfNNSampleTransformed = evaluateDDForPairs(distanceFunctionForTransformedDataset, idsOfNNPairs, searchObjectsAsIdObjectMap);

//      print
        printDDOfRandomAndNearNeighbours(transformedDatasetName, transformedDistInterval, ddRandomSampleTransformed, ddOfNNSampleTransformed);
    }

    private static SortedMap<Float, Float> createDDOfNNSample(Dataset dataset, int queryObjCount, int sampleCount, int k, List<Object[]> idsOfNNPairs) {
        List<Object> queryObjects = dataset.getQueryObjects(queryObjCount);
        List<Object> searchObjects = dataset.getSampleOfDataset(sampleCount + queryObjCount);
        AbstractSearchSpace searchSpace = dataset.getSearchSpace();
        for (int i = 0; i < queryObjCount; i++) {
            searchObjects.remove(0);
        }
        GroundTruthEvaluator gte = new GroundTruthEvaluator(dataset, k);
        TreeSet<Map.Entry<Object, Float>>[] groundTruth = gte.evaluateIteratorInParallel(searchObjects.iterator());
        List<Float> distances = new ArrayList<>();
        for (int i = 0; i < groundTruth.length; i++) {
            TreeSet<Map.Entry<Object, Float>> evaluatedQuery = groundTruth[i];
            Object queryObjID = searchSpace.getIDOfObject(queryObjects.get(i));
            for (Map.Entry<Object, Float> entry : evaluatedQuery) {
                Object idOfObject = entry.getKey();
                idsOfNNPairs.add(new Object[]{queryObjID, idOfObject});
                distances.add(entry.getValue());
            }
        }
        return ToolsSpaceDomain.createDistanceDensityPlot(distances);
    }

    private static SortedMap<Float, Float> evaluateDDForPairs(DistanceFunctionInterface distanceFunction, List<Object[]> idsPairs, Map<Comparable, Object> searchObjects) {
        List<Float> distances = new ArrayList<>();
        for (Object[] idsPair : idsPairs) {
            Object o1 = searchObjects.get(idsPair[0]);
            Object o2 = searchObjects.get(idsPair[1]);
            float distance = distanceFunction.getDistance(o1, o2);
            distances.add(distance);
        }
        return ToolsSpaceDomain.createDistanceDensityPlot(distances);
    }

    private static void printDDOfRandomAndNearNeighbours(String datasetName, float distInterval, SortedMap<Float, Float> ddRandomSample, SortedMap<Float, Float> ddOfNNSample) {
        System.out.println(datasetName);
        System.out.println("Distance;Density of random sample; Density of distances to near neighbours");
        for (float dist = 0; true; dist += distInterval) {
            float rand = ddRandomSample.containsKey(dist) ? ddRandomSample.get(dist) : 0;
            float nn = ddOfNNSample.containsKey(dist) ? ddOfNNSample.get(dist) : 0;
            System.out.println(dist + ";" + rand + ";" + nn);
            if (dist > ddOfNNSample.lastKey() && dist > ddRandomSample.lastKey()) {
                break;
            }
        }
    }
}
