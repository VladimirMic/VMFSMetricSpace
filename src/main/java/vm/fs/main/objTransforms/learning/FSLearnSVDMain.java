package vm.fs.main.objTransforms.learning;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
import vm.fs.store.dataTransforms.TODOFSSVDStorageImpl;
import vm.metricSpace.dataToStringConvertors.SingularisedConvertors;
import vm.objTransforms.learning.LearnSVD;
import vm.objTransforms.storeLearned.SVDStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSLearnSVDMain {

    public static void main(String[] args) throws SQLException {
        String datasetName = "sift_1M";
        datasetName = "decaf_1M";
        int sampleCount = 100000;
//
        FSMetricSpaceImpl<float[]> sourceMetricSpace = new FSMetricSpaceImpl<>();
        FSMetricSpacesStorage metricSpaceStorage = new FSMetricSpacesStorage(sourceMetricSpace, SingularisedConvertors.FLOAT_VECTOR_SPACE);
//
        List<Object> sampleOfDataset = metricSpaceStorage.getSampleOfDataset(datasetName, sampleCount);
//        List<Object> sampleOfDataset = getTrivialSampleDataset();
        SVDStoreInterface pcaStorage = new TODOFSSVDStorageImpl(datasetName, sampleCount);

        LearnSVD svd = new LearnSVD(sourceMetricSpace, pcaStorage, sampleOfDataset, datasetName, sampleCount);
        svd.execute();
    }

    private static final List<Object> getTrivialSampleDataset() {
        List<Object> ret = new ArrayList<>();
        ret.add(new AbstractMap.SimpleEntry("1", new float[]{3, 2, 2}));
        ret.add(new AbstractMap.SimpleEntry("2", new float[]{2, 3, -2}));
        return ret;
    }
}
