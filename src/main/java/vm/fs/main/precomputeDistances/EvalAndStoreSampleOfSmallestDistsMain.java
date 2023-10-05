package vm.fs.main.precomputeDistances;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.precomputedDists.FSPrecomputedDistPairsStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class EvalAndStoreSampleOfSmallestDistsMain {

    public static final Integer SAMPLE_SET_SIZE = 10000;
    public static final Integer SAMPLE_QUERY_SET_SIZE = 1000;
    public static final Logger LOG = Logger.getLogger(EvalAndStoreSampleOfSmallestDistsMain.class.getName());
    /**
     * Number of stored minimum distances
     */
    public static final Integer IMPLICIT_K = 40000;

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(true),
            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(true),
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(true),
//            new FSDatasetInstanceSingularizator.DeCAFDataset(),
//            new FSDatasetInstanceSingularizator.SIFTdataset(),
//            new FSDatasetInstanceSingularizator.MPEG7dataset(),
//            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_256Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_64Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_192Dataset(),
//            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_128Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) {
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();

        DistanceFunctionInterface df = dataset.getDistanceFunction();

        List<Object> metricObjects = dataset.getSampleOfDataset(SAMPLE_SET_SIZE + SAMPLE_QUERY_SET_SIZE);
        List<Object> sampleObjects = metricObjects.subList(0, SAMPLE_SET_SIZE);
        List<Object> queriesSamples = metricObjects.subList(SAMPLE_SET_SIZE, SAMPLE_SET_SIZE + SAMPLE_QUERY_SET_SIZE);

        Comparator<Map.Entry<String, Float>> comp = new Tools.MapByValueComparator<>();
        TreeSet<Map.Entry<String, Float>> result = new TreeSet(comp);
        for (int i = 0; i < sampleObjects.size(); i++) {
            Object o = sampleObjects.get(i);
            Object oData = metricSpace.getDataOfMetricObject(o);
            Object oID = metricSpace.getIDOfMetricObject(o);
            for (Object q : queriesSamples) {
                Object qData = metricSpace.getDataOfMetricObject(q);
                Object qID = metricSpace.getIDOfMetricObject(q);
                float dist = df.getDistance(oData, qData);
                String key = oID + ";" + qID;
                SimpleEntry<String, Float> e = new AbstractMap.SimpleEntry(key, dist);
                result.add(e);
                while (result.size() > IMPLICIT_K) {
                    result.remove(result.last());
                }
            }
            if ((i + 1) % 500 == 0) {
                LOG.log(Level.INFO, "Processed object {0} out of {1}", new Object[]{i + 1, sampleObjects.size()});
            }
        }
        FSPrecomputedDistPairsStorageImpl storage = new FSPrecomputedDistPairsStorageImpl(dataset.getDatasetName(), SAMPLE_SET_SIZE, SAMPLE_QUERY_SET_SIZE);
        storage.storePrecomputedDistances(result);
    }

}
