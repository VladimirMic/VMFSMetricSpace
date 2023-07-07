package vm.fs.main.datatools;

import java.util.List;
import java.util.SortedMap;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.math.Tools;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.ToolsMetricDomain;
import vm.metricSpace.AbstractMetricSpacesStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author xmic
 */
public class PrintDDOfDatasetMain {

    public static void main(String[] args) {
        Dataset dataset = new FSDatasetInstanceSingularizator.LAION_100M_Dataset();
        String datasetName = dataset.getDatasetName();
        float distInterval = 1f / 100;

//      getHistogramsForRandomPairs
        int objCount = 1000 * 1000;//100,000
        int distCount = 1000 * 10000;//1,000,000
        SortedMap<Float, Float> ddRandomSample = createDDOfRandomSample(dataset, datasetName, objCount, distCount, distInterval, null);
//      print
        printDD(datasetName, distInterval, ddRandomSample);
    }

    protected static SortedMap<Float, Float> createDDOfRandomSample(Dataset dataset, String datasetName, int objCount, int distCount, float distInterval, List<Object[]> examinedPairs) {
        return createDDOfRandomSample(dataset.getMetricSpace(), dataset.getMetricSpacesStorage(), dataset.getDistanceFunction(), datasetName, objCount, distCount, distInterval, examinedPairs);
    }

    protected static SortedMap<Float, Float> createDDOfRandomSample(AbstractMetricSpace metricSpace, AbstractMetricSpacesStorage metricSpacesStorage, DistanceFunctionInterface df, String datasetName, int objCount, int distCount, float distInterval, List<Object[]> examinedPairs) {
        List<Object> metricObjects = metricSpacesStorage.getSampleOfDataset(datasetName, objCount);
        return ToolsMetricDomain.createDistanceDensityPlot(metricSpace, metricObjects, df, distCount, distInterval, examinedPairs);
    }

    private static void printDD(String datasetName, float distInterval, SortedMap<Float, Float> histogram) {
        System.out.println(datasetName);
        System.out.println("Distance;Density of random sample");
        float lastDist = 0;
        for (Float dist : histogram.keySet()) {
            while (dist - lastDist > distInterval * 1.1d) {
                lastDist += distInterval;
                lastDist = Tools.round(lastDist, distInterval, false);
                System.out.println(lastDist + ";" + 0);
            }
            System.out.println(dist + ";" + histogram.get(dist));
            lastDist = dist;
        }
    }

}
