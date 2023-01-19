package vm.fs.dataset;

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
            this.metricSpace = new DBMetricSpaceImpl();
            this.metricSpacesStorage = new DBMetricSpacesStorage<>(metricSpace, SingularisedConvertors.MPEG7_SPACE);
        }

    }

    private static class DBFloatVectorDataset extends Dataset<float[]> {

        public DBFloatVectorDataset(String datasetName) throws SQLException {
            this.datasetName = datasetName;
            this.metricSpace = new DBMetricSpaceImpl();
            this.metricSpacesStorage = new DBMetricSpacesStorage<>(metricSpace, SingularisedConvertors.FLOAT_VECTOR_SPACE);
        }

    }

}
