package vm.fs.main.objTransforms.apply;

import java.util.BitSet;
import vm.objTransforms.perform.TransformDataToGHPSketches;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
import vm.fs.store.dataTransforms.FSGHPSketchesPivotPairsStorageImpl;
import vm.metricSpace.Dataset;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.metricSpace.dataToStringConvertors.SingularisedConvertors;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSApplyGHPSketchingMain {

    public static void main(String[] args) {
//        run(new FSDatasetInstanceSingularizator.DeCAFDataset());
        run(new FSDatasetInstanceSingularizator.SIFTdataset());
        run(new FSDatasetInstanceSingularizator.MPEG7dataset());
        run(new FSDatasetInstanceSingularizator.RandomDataset20Uniform());
    }

    private static void run(Dataset dataset) {
        GHPSketchingPivotPairsStoreInterface storageOfPivotPairs = new FSGHPSketchesPivotPairsStorageImpl();
        MetricSpacesStorageInterface storageForSketches = new FSMetricSpacesStorage(new FSMetricSpaceImpl<BitSet>(), SingularisedConvertors.LONG_VECTOR_SPACE);
        TransformDataToGHPSketches evaluator = new TransformDataToGHPSketches(dataset, storageOfPivotPairs, storageForSketches);
        int[] sketchesLengths = new int[]{256, 192, 128, 64, 512};
        evaluator.createSketchesForDatasetPivotsAndQueries(sketchesLengths);
    }

}
