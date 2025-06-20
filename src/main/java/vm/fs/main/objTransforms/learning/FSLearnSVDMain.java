package vm.fs.main.objTransforms.learning;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import vm.fs.store.dataTransforms.FSSVDStorageImpl;
import vm.objTransforms.learning.LearnSVD;
import vm.objTransforms.storeLearned.SVDStoreInterface;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;

/**
 *
 * @author Vlada
 */
public class FSLearnSVDMain {

    public static final Integer SAMPLE_COUNT = 500000;

    public static void main(String[] args) {
        Dataset[] datasets = {
//            new M2DatasetInstanceSingularizator.DeCAF100MDatasetAndromeda(),
//            new M2DatasetInstanceSingularizator.DeCAF100M_TMPDataset()
        };

        for (Dataset dataset : datasets) {
            run(dataset, SAMPLE_COUNT);
        }
    }

    private static final List<Object> getTrivialSampleDataset() {
        List<Object> ret = new ArrayList<>();
        ret.add(new AbstractMap.SimpleEntry("1", new float[]{3, 2, 2}));
        ret.add(new AbstractMap.SimpleEntry("2", new float[]{2, 3, -2}));
        return ret;
    }

    private static void run(Dataset dataset, int sampleCount) {
        String datasetName = dataset.getDatasetName();
        AbstractSearchSpace sourceSearchSpace = dataset.getSearchSpace();
        List<Object> sampleOfDataset = dataset.getSampleOfDataset(sampleCount);
        SVDStoreInterface pcaStorage = new FSSVDStorageImpl(datasetName, sampleCount, true);

        LearnSVD svd = new LearnSVD(sourceSearchSpace, pcaStorage, sampleOfDataset, datasetName, sampleCount);
        svd.execute();
    }
}
