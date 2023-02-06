package vm.fs.metricSpaceImpl;

import java.util.AbstractMap;
import java.util.logging.Logger;
import vm.metricspace.AbstractMetricSpace;
import vm.metricspace.distance.impl.L2OnFloatsArray;
import vm.metricspace.distance.impl.Sapir3DistanceFunction;
import vm.metricspace.distance.DistanceFunctionInterface;

/**
 *
 * @author xmic
 * @param <T>
 */
public class FSMetricSpaceImpl <T> extends AbstractMetricSpace<T> {

    private static final Logger LOG = Logger.getLogger(FSMetricSpaceImpl.class.getName());

    @Override
    public DistanceFunctionInterface getDistanceFunctionForDataset(String datasetName, Object... params) {
        if (datasetName.contains("_PCA")) {
            return new L2OnFloatsArray();
        }
        switch (datasetName) {
            case ("decaf_1m"):
            case ("sift_1m"):
                return new L2OnFloatsArray();
            case ("random_20dim_uniform_1m"): {
                return new L2OnFloatsArray();
            }
            case ("mpeg7_1m"): {
                return new Sapir3DistanceFunction();
            }
        }
        throw new IllegalArgumentException("Unknown dataset name " + datasetName + ". No distance function provided.");
    }

    @Override
    public Object getIDOfMetricObject(Object o) {
        AbstractMap.SimpleEntry<String, String> entry = (AbstractMap.SimpleEntry<String, String>) o;
        return entry.getKey();
    }

    @Override
    public T getDataOfMetricObject(Object o) {
        AbstractMap.SimpleEntry<Object, T> entry = (AbstractMap.SimpleEntry<Object, T>) o;
        return entry.getValue();
    }

    @Override
    public Object createMetricObject(Object id, T data) {
        return new AbstractMap.SimpleEntry<>(id, data);
    }

}
