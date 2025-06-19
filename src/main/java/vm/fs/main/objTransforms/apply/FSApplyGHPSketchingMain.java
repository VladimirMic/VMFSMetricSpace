package vm.fs.main.objTransforms.apply;

import vm.objTransforms.perform.TransformDataToGHPSketches;
import vm.fs.dataset.FSDatasetInstances;
import vm.fs.searchSpaceImpl.FSSearchSpaceImpl;
import vm.fs.searchSpaceImpl.FSSearchSpacesStorage;
import vm.fs.store.dataTransforms.FSGHPSketchesPivotPairsStorageImpl;
import vm.objTransforms.storeLearned.PivotPairsStoreInterface;
import vm.searchSpace.AbstractSearchSpacesStorage;
import vm.searchSpace.Dataset;
import vm.searchSpace.data.toStringConvertors.SingularisedConvertors;

/**
 *
 * @author Vlada
 */
public class FSApplyGHPSketchingMain {

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
//            new FSDatasetInstanceSingularizator.LAION_100k_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_300k_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
            new FSDatasetInstances.LAION_100M_Dataset(publicQueries)
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) {
        AbstractSearchSpacesStorage storageForSketches = new FSSearchSpacesStorage(dataset.getSearchSpace(), SingularisedConvertors.LONG_VECTOR_SPACE);
        PivotPairsStoreInterface storageOfPivotPairs = new FSGHPSketchesPivotPairsStorageImpl();
        TransformDataToGHPSketches evaluator = new TransformDataToGHPSketches(dataset, storageOfPivotPairs, storageForSketches, 0.5f, -1);
//        int[] sketchesLengths = new int[]{192, 256};
        int[] sketchesLengths = new int[]{512};
//        String[] csvPivotFileName = new String[]{"laion2B-en-clip768v2-n=1M_sample.h5_GHP_50_512"};
//        evaluator.createSketchesForDatasetPivotsAndQueries(sketchesLengths, csvPivotFileName);
        evaluator.createSketchesForDatasetPivotsAndQueries(sketchesLengths);
    }

}
