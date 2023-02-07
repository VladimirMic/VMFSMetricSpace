package vm.fs.dataset;

import java.util.Map;
import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
import vm.metricspace.Dataset;
import vm.metricspace.dataToStringConvertors.SingularisedConvertors;

/**
 *
 * @author xmic
 */
public class FSDatasetInstanceSingularizator {

    public static class DeCAFDataset extends DBFloatVectorDataset {

        public DeCAFDataset() {
            super("decaf_1m");
        }

    }

    public static class RandomDataset20Uniform extends DBFloatVectorDataset {

        public RandomDataset20Uniform() {
            super("random_20dim_uniform_1m");
        }
    }

    public static class SIFTdataset extends DBFloatVectorDataset {

        public SIFTdataset() {
            super("sift_1m");
        }
    }

    public static class MPEG7dataset extends Dataset<Map<String, Object>> {

        public MPEG7dataset() {
            this.datasetName = "mpeg7_1m";
            this.metricSpace = new FSMetricSpaceImpl();
            this.metricSpacesStorage = new FSMetricSpacesStorage<>(metricSpace, SingularisedConvertors.MPEG7_SPACE);
        }

        }

    private static class DBFloatVectorDataset extends Dataset<float[]> {

        public DBFloatVectorDataset(String datasetName) {
            this.datasetName = datasetName;
            this.metricSpace = new FSMetricSpaceImpl();
            this.metricSpacesStorage = new FSMetricSpacesStorage<>(metricSpace, SingularisedConvertors.FLOAT_VECTOR_SPACE);
        }

    }

}
