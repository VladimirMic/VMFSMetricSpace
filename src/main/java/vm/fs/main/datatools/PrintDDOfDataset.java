package vm.fs.main.datatools;

import java.util.List;
import java.util.SortedMap;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.ToolsMetricDomain;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author xmic
 */
public class PrintDDOfDataset {

    public static void main(String[] args){
        Dataset dataset = new FSDatasetInstanceSingularizator.LAION_100M_Dataset();
        String datasetName = dataset.getDatasetName();
        float distInterval = 2f;

//      getHistogramsForRandomPairs
        int objCount = 100 * 1000;//100,000
        int distCount = 1000 * 1000;//1,000,000
        SortedMap<Float, Float> ddRandomSample = createDDOfRandomSample(dataset, datasetName, objCount, distCount, distInterval, null);
//      print
        printDD(datasetName, distInterval, ddRandomSample);
    }

    protected static SortedMap<Float, Float> createDDOfRandomSample(Dataset dataset, String datasetName, int objCount, int distCount, float distInterval, List<Object[]> examinedPairs) {
        return createDDOfRandomSample(dataset.getMetricSpace(), dataset.getMetricSpacesStorage(), dataset.getDistanceFunction(), datasetName, objCount, distCount, distInterval, examinedPairs);
    }

    protected static SortedMap<Float, Float> createDDOfRandomSample(AbstractMetricSpace metricSpace, MetricSpacesStorageInterface metricSpacesStorage, DistanceFunctionInterface df, String datasetName, int objCount, int distCount, float distInterval, List<Object[]> examinedPairs) {
        List<Object> metricObjects = metricSpacesStorage.getSampleOfDataset(datasetName, objCount);
        return ToolsMetricDomain.createDistanceDensityPlot(metricSpace, metricObjects, df, distCount, distInterval, examinedPairs);
    }

    private static void printDD(String datasetName, float distInterval, SortedMap<Float, Float> ddRandomSample) {
        System.out.println(datasetName);
        System.out.println("Distance;Density of random sample; Density of distances to near neighbours");
        for (float dist = 0; true; dist += distInterval) {
            float rand = ddRandomSample.containsKey(dist) ? ddRandomSample.get(dist) : 0;
            System.out.println(dist + ";" + rand);
            if (dist > ddRandomSample.lastKey()) {
                break;
            }
        }
    }

}
