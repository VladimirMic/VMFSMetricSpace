package vm.fs.dataset;

import java.util.Map;
import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
import vm.fs.metricSpaceImpl.H5MetricSpacesStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.dataToStringConvertors.SingularisedConvertors;
import vm.fs.metricSpaceImpl.VMMVStorage;

/**
 *
 * @author xmic
 */
public class FSDatasetInstanceSingularizator {

    public static class DeCAFDataset extends FSFloatVectorDataset {

        public DeCAFDataset() {
            super("decaf_1m");
        }
    }

    public static class DeCAF20M_PCA256Dataset extends FSFloatVectorDataset {

        public DeCAF20M_PCA256Dataset() {
            super("decaf_20m_PCA256");
        }
    }

    public static class RandomDataset20Uniform extends FSFloatVectorDataset {

        public RandomDataset20Uniform() {
            super("random_20dim_uniform_1m");
        }
    }

    public static class SIFTdataset extends FSFloatVectorDataset {

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

        @Override
        public Map<Object, Object> getKeyValueStorage() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class DeCAF_PCA8Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA8Dataset() {
            super("decaf_1m_PCA8");
        }

    }

    public static class DeCAF_PCA10Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA10Dataset() {
            super("decaf_1m_PCA10");
        }
    }

    public static class DeCAF_PCA12Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA12Dataset() {
            super("decaf_1m_PCA12");
        }
    }

    public static class DeCAF_PCA16Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA16Dataset() {
            super("decaf_1m_PCA16");
        }
    }

    public static class DeCAF_PCA24Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA24Dataset() {
            super("decaf_1m_PCA24");
        }
    }

    public static class DeCAF_PCA32Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA32Dataset() {
            super("decaf_1m_PCA32");
        }
    }

    public static class DeCAF_PCA46Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA46Dataset() {
            super("decaf_1m_PCA46");
        }
    }

    public static class DeCAF_PCA68Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA68Dataset() {
            super("decaf_1m_PCA68");
        }
    }

    public static class DeCAF_PCA128Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA128Dataset() {
            super("decaf_1m_PCA128");
        }
    }

    public static class DeCAF_PCA256Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA256Dataset() {
            super("decaf_1m_PCA256");
        }
    }

    public static class DeCAF_PCA670Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA670Dataset() {
            super("decaf_1m_PCA670");
        }
    }

    public static class DeCAF_PCA1540Dataset extends FSFloatVectorDataset {

        public DeCAF_PCA1540Dataset() {
            super("decaf_1m_PCA1540");
        }
    }

    public static class DeCAF_GHP_50_256Dataset extends FSHammingSpaceDataset {

        public DeCAF_GHP_50_256Dataset() {
            super("decaf_1m_GHP_50_256");
        }
    }

    public static class DeCAF_GHP_50_192Dataset extends FSHammingSpaceDataset {

        public DeCAF_GHP_50_192Dataset() {
            super("decaf_1m_GHP_50_192");
        }
    }

    public static class DeCAF_GHP_50_128Dataset extends FSHammingSpaceDataset {

        public DeCAF_GHP_50_128Dataset() {
            super("decaf_1m_GHP_50_128");
        }
    }

    public static class DeCAF_GHP_50_64Dataset extends FSHammingSpaceDataset {

        public DeCAF_GHP_50_64Dataset() {
            super("decaf_1m_GHP_50_64");
        }
    }

    public static class LAION_1M_SampleDataset extends FSFloatVectorDataset {

        public LAION_1M_SampleDataset() {
            super("laion2B-en-clip768v2-n=1M_sample.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-clip768v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_20000pivots";
        }
    }

    public static class LAION_100k_Dataset extends H5FloatVectorDataset {

        public LAION_100k_Dataset() {
            super("laion2B-en-clip768v2-n=100K.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-clip768v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_20000pivots";
        }
    }

    public static class LAION_300k_Dataset extends H5FloatVectorDataset {

        public LAION_300k_Dataset() {
            super("laion2B-en-clip768v2-n=300K.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-clip768v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_20000pivots";
        }
    }

    public static class LAION_10M_Dataset extends H5FloatVectorDataset {

        public LAION_10M_Dataset() {
            super("laion2B-en-clip768v2-n=10M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-clip768v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_20000pivots";
        }
    }

    public static class LAION_30M_Dataset extends H5FloatVectorDataset {

        public LAION_30M_Dataset() {
            super("laion2B-en-clip768v2-n=30M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-clip768v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_20000pivots";
        }
    }

    public static class LAION_100M_Dataset extends H5FloatVectorDataset {

        public LAION_100M_Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-clip768v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_20000pivots";
        }
    }

    public static class LAION_100k_PCA32Dataset extends H5FloatVectorDataset {

        public LAION_100k_PCA32Dataset() {
            super("laion2B-en-pca32v2-n=100K.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca32v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA32_20000.gz";

        }
    }

    public static class LAION_300k_PCA32Dataset extends H5FloatVectorDataset {

        public LAION_300k_PCA32Dataset() {
            super("laion2B-en-pca32v2-n=300K.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca32v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA32_20000.gz";
        }
    }

    public static class LAION_10M_PCA32Dataset extends H5FloatVectorDataset {

        public LAION_10M_PCA32Dataset() {
            super("laion2B-en-pca32v2-n=10M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca32v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA32_20000.gz";
        }
    }

    public static class LAION_30M_PCA32Dataset extends H5FloatVectorDataset {

        public LAION_30M_PCA32Dataset() {
            super("laion2B-en-pca32v2-n=30M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA32_20000.gz";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-pca32v2-n=100M.h5";
        }
    }

    public static class LAION_100M_PCA32Dataset extends H5FloatVectorDataset {

        public LAION_100M_PCA32Dataset() {
            super("laion2B-en-pca32v2-n=100M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca32v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA32_20000.gz";
        }
    }

    public static class LAION_100k_PCA96Dataset extends H5FloatVectorDataset {

        public LAION_100k_PCA96Dataset() {
            super("laion2B-en-pca96v2-n=100K.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca96v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA96_20000.gz";
        }
    }

    public static class LAION_300k_PCA96Dataset extends H5FloatVectorDataset {

        public LAION_300k_PCA96Dataset() {
            super("laion2B-en-pca96v2-n=300K.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca96v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA96_20000.gz";

        }
    }

    public static class LAION_10M_PCA96Dataset extends H5FloatVectorDataset {

        public LAION_10M_PCA96Dataset() {
            super("laion2B-en-pca96v2-n=10M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca96v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA96_20000.gz";
        }

    }

    public static class LAION_30M_PCA96Dataset extends H5FloatVectorDataset {

        public LAION_30M_PCA96Dataset() {
            super("laion2B-en-pca96v2-n=30M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca96v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA96_20000.gz";
        }
    }

    public static class LAION_100M_PCA96Dataset extends H5FloatVectorDataset {

        public LAION_100M_PCA96Dataset() {
            super("laion2B-en-pca96v2-n=100M.h5");
        }

        @Override
        public String getQuerySetName() {
            return "public-queries-10k-pca96v2.h5";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA96_20000.gz";
        }
    }

    public static class LAION_10M_PCA256Dataset extends FSFloatVectorDataset {

        public LAION_10M_PCA256Dataset() {
            super("laion2B-en-clip768v2-n=10M.h5_PCA256");
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=10M.h5_PCA256";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=10M.h5_PCA256";
        }
    }

    public static class LAION_30M_PCA256Dataset extends FSFloatVectorDataset {

        public LAION_30M_PCA256Dataset() {
            super("laion2B-en-clip768v2-n=30M.h5_PCA256");
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA256";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA256";
        }
    }

    public static class LAION_100M_PCA256Dataset extends FSFloatVectorDataset {

        public LAION_100M_PCA256Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5_PCA256");
        }

    }

    public static class LAION_100M_PCA256Prefixes24Dataset extends FSFloatVectorDataset {

        public LAION_100M_PCA256Prefixes24Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5_PCA_pref24of256");
        }

    }

    public static class LAION_100M_PCA256Prefixes32Dataset extends FSFloatVectorDataset {

        public LAION_100M_PCA256Prefixes32Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5_PCA_pref32of256");
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA_pref32of256";
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_PCA_pref32of256";
        }
    }

    public static class LAION_100k_GHP_50_192Dataset extends FSHammingSpaceDataset {

        public LAION_100k_GHP_50_192Dataset() {
            super("laion2B-en-clip768v2-n=100k.h5_GHP_50_192");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }
    }

    public static class LAION_300k_GHP_50_192Dataset extends FSHammingSpaceDataset {

        public LAION_300k_GHP_50_192Dataset() {
            super("laion2B-en-clip768v2-n=300k.h5_GHP_50_192");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }

    }

    public static class LAION_10M_GHP_50_192Dataset extends FSHammingSpaceDataset {

        public LAION_10M_GHP_50_192Dataset() {
            super("laion2B-en-clip768v2-n=10M.h5_GHP_50_192");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }
    }

    public static class LAION_30M_GHP_50_192Dataset extends FSHammingSpaceDataset {

        public LAION_30M_GHP_50_192Dataset() {
            super("laion2B-en-clip768v2-n=30M.h5_GHP_50_192");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_192.gz";
        }

    }

    public static class LAION_100M_GHP_50_192Dataset extends FSHammingSpaceDataset {

        public LAION_100M_GHP_50_192Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5_GHP_50_192");
        }

    }

    public static class LAION_100k_GHP_50_256Dataset extends FSHammingSpaceDataset {

        public LAION_100k_GHP_50_256Dataset() {
            super("laion2B-en-clip768v2-n=100k.h5_GHP_50_256");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }

    }

    public static class LAION_300k_GHP_50_256Dataset extends FSHammingSpaceDataset {

        public LAION_300k_GHP_50_256Dataset() {
            super("laion2B-en-clip768v2-n=300k.h5_GHP_50_256");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }
    }

    public static class LAION_10M_GHP_50_256Dataset extends FSHammingSpaceDataset {

        public LAION_10M_GHP_50_256Dataset() {
            super("laion2B-en-clip768v2-n=10M.h5_GHP_50_256");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }

    }

    public static class LAION_30M_GHP_50_256Dataset extends FSHammingSpaceDataset {

        public LAION_30M_GHP_50_256Dataset() {
            super("laion2B-en-clip768v2-n=30M.h5_GHP_50_256");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_256.gz";
        }
    }

    public static class LAION_100M_GHP_50_256Dataset extends FSHammingSpaceDataset {

        public LAION_100M_GHP_50_256Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5_GHP_50_256");
        }

    }

    public static class LAION_100k_GHP_50_384Dataset extends FSHammingSpaceDataset {

        public LAION_100k_GHP_50_384Dataset() {
            super("laion2B-en-clip768v2-n=100k.h5_GHP_50_384");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }

    }

    public static class LAION_300k_GHP_50_384Dataset extends FSHammingSpaceDataset {

        public LAION_300k_GHP_50_384Dataset() {
            super("laion2B-en-clip768v2-n=300k.h5_GHP_50_384");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }
    }

    public static class LAION_10M_GHP_50_384Dataset extends FSHammingSpaceDataset {

        public LAION_10M_GHP_50_384Dataset() {
            super("laion2B-en-clip768v2-n=10M.h5_GHP_50_384");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }

    }

    public static class LAION_30M_GHP_50_384Dataset extends FSHammingSpaceDataset {

        public LAION_30M_GHP_50_384Dataset() {
            super("laion2B-en-clip768v2-n=30M.h5_GHP_50_384");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_384.gz";
        }

    }

    public static class LAION_100M_GHP_50_384Dataset extends FSHammingSpaceDataset {

        public LAION_100M_GHP_50_384Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5_GHP_50_384");
        }

    }

    public static class LAION_100k_GHP_50_512Dataset extends FSHammingSpaceDataset {

        public LAION_100k_GHP_50_512Dataset() {
            super("laion2B-en-clip768v2-n=100k.h5_GHP_50_512");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

    }

    public static class LAION_300k_GHP_50_512Dataset extends FSHammingSpaceDataset {

        public LAION_300k_GHP_50_512Dataset() {
            super("laion2B-en-clip768v2-n=300k.h5_GHP_50_512");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

    }

    public static class LAION_10M_GHP_50_512Dataset extends FSHammingSpaceDataset {

        public LAION_10M_GHP_50_512Dataset() {
            super("laion2B-en-clip768v2-n=10M.h5_GHP_50_512");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

    }

    public static class LAION_30M_GHP_50_512Dataset extends FSHammingSpaceDataset {

        public LAION_30M_GHP_50_512Dataset() {
            super("laion2B-en-clip768v2-n=30M.h5_GHP_50_512.gz");
        }

        @Override
        public String getPivotSetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

        @Override
        public String getQuerySetName() {
            return "laion2B-en-clip768v2-n=100M.h5_GHP_50_512.gz";
        }

    }

    public static class LAION_100M_GHP_50_512Dataset extends FSHammingSpaceDataset {

        public LAION_100M_GHP_50_512Dataset() {
            super("laion2B-en-clip768v2-n=100M.h5_GHP_50_512");
        }

    }

    public static class FSFloatVectorDataset extends Dataset<float[]> {

        public FSFloatVectorDataset(String datasetName) {
            this.datasetName = datasetName;
            this.metricSpace = new FSMetricSpaceImpl();
            this.metricSpacesStorage = new FSMetricSpacesStorage<>(metricSpace, SingularisedConvertors.FLOAT_VECTOR_SPACE);
        }

        @Override
        public Map<Object, Object> getKeyValueStorage() {
            VMMVStorage storage = new VMMVStorage(datasetName, false);
            Map keyValueStorage = storage.getKeyValueStorage();
            return keyValueStorage;
        }

    }

    public static class H5FloatVectorDataset extends Dataset<float[]> {

        public H5FloatVectorDataset(String datasetName) {
            this.datasetName = datasetName;
            this.metricSpace = new FSMetricSpaceImpl();
            this.metricSpacesStorage = new H5MetricSpacesStorage(metricSpace, SingularisedConvertors.FLOAT_VECTOR_SPACE);
        }

        @Override
        public Map<Object, Object> getKeyValueStorage() {
            H5MetricSpacesStorage storage = (H5MetricSpacesStorage) metricSpacesStorage;
            return storage.getAsMap(datasetName);
        }
    }

    public static class FSHammingSpaceDataset extends Dataset<long[]> {

        public FSHammingSpaceDataset(String datasetName) {
            this.datasetName = datasetName;
            this.metricSpace = new FSMetricSpaceImpl();
            this.metricSpacesStorage = new FSMetricSpacesStorage<>(metricSpace, SingularisedConvertors.LONG_VECTOR_SPACE);
        }

        @Override
        public Map<Object, Object> getKeyValueStorage() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
