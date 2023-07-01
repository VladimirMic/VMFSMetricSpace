package vm.fs.main.objTransforms.apply;

import java.util.Iterator;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.dataTransforms.FSSVDStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.objTransforms.MetricObjectTransformerInterface;
import vm.objTransforms.MetricObjectsParallelTransformerImpl;
import vm.objTransforms.perform.PCAMetricObjectTransformer;
import vm.objTransforms.perform.PCAPrefixMetricObjectTransformer;

/**
 *
 * @author Vlada
 */
public class FSApplyPCAMain {

    public static final Integer PREFFIX_TO_STORE = null;

    public static void main(String[] args) {
//        run(new FSDatasetInstanceSingularizator.LAION_100M_Dataset());
        System.gc();
        run(new FSDatasetInstanceSingularizator.LAION_30M_Dataset());
        System.gc();
//        run(new FSDatasetInstanceSingularizator.LAION_10M_Dataset());
        System.gc();
    }

    private static void run(Dataset dataset) {
        int sampleSetSize = 500000;
//        int[] finalDimensions = new int[]{100, 128, 30, 4, 6, 72, 8}; // SIFT
//        int[] finalDimensions = new int[]{20, 18, 16, 15, 12, 10, 8}; // Random 20
//        int[] finalDimensions = new int[]{10, 12, 128, 1540, 16, 46, 2387, 24, 256, 32, 4, 6, 670, 68, 8}; // DeCAF
        int[] finalFullDimensions = new int[]{256}; // DeCAF

        AbstractMetricSpace<float[]> metricSpage = dataset.getMetricSpace();
        MetricSpacesStorageInterface spaceStorage = dataset.getMetricSpacesStorage();
        String origDatasetName = dataset.getDatasetName();
        FSSVDStorageImpl svdStorage = new FSSVDStorageImpl(origDatasetName, sampleSetSize, false);
        float[][] vtMatrixFull = svdStorage.getVTMatrix();

        for (int finalDimension : finalFullDimensions) {
            float[][] vtMatrix = Tools.shrinkMatrix(vtMatrixFull, finalDimension, vtMatrixFull[0].length);

            MetricObjectTransformerInterface pca;
            if (PREFFIX_TO_STORE != null && PREFFIX_TO_STORE > 0) {
                pca = new PCAPrefixMetricObjectTransformer(vtMatrix, svdStorage.getMeansOverColumns(), metricSpage, metricSpage, PREFFIX_TO_STORE);
            } else {
                pca = new PCAMetricObjectTransformer(vtMatrix, svdStorage.getMeansOverColumns(), metricSpage, metricSpage);
            }

            MetricObjectsParallelTransformerImpl parallelTransformerImpl = new MetricObjectsParallelTransformerImpl(pca, spaceStorage, pca.getNameOfTransformedSetOfObjects(origDatasetName, false));
            transformPivots(dataset.getPivotSetName(), spaceStorage, parallelTransformerImpl, "Pivot set with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + sampleSetSize + " to the length " + finalDimension);
            transformQueryObjects(dataset.getQuerySetName(), spaceStorage, parallelTransformerImpl, "Query set with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + sampleSetSize + " to the length " + finalDimension);
            transformDataset(origDatasetName, spaceStorage, parallelTransformerImpl, "Dataset with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + sampleSetSize + " to the length " + finalDimension);
            try {
                spaceStorage.updateDatasetSize(pca.getNameOfTransformedSetOfObjects(origDatasetName, false));
            } catch (Exception e) {
            }
        }
    }

    private static void transformDataset(String origDatasetName, MetricSpacesStorageInterface spaceStorage, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getObjectsFromDataset(origDatasetName);
        parallelTransformerImpl.processIteratorInParallel(it, MetricSpacesStorageInterface.OBJECT_TYPE.DATASET_OBJECT, vm.javatools.Tools.PARALELISATION, additionalParameters);
    }

    private static void transformPivots(String pivotSetName, MetricSpacesStorageInterface spaceStorage, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getPivots(pivotSetName).iterator();
        parallelTransformerImpl.processIteratorSequentially(it, MetricSpacesStorageInterface.OBJECT_TYPE.PIVOT_OBJECT, additionalParameters);
    }

    private static void transformQueryObjects(String querySetName, MetricSpacesStorageInterface spaceStorage, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getQueryObjects(querySetName).iterator();
        parallelTransformerImpl.processIteratorSequentially(it, MetricSpacesStorageInterface.OBJECT_TYPE.QUERY_OBJECT, additionalParameters);
    }

    public static void transformDataset(Iterator<Object> dataIterator, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        parallelTransformerImpl.processIteratorInParallel(dataIterator, MetricSpacesStorageInterface.OBJECT_TYPE.DATASET_OBJECT, vm.javatools.Tools.PARALELISATION, additionalParameters);
    }

    public static void transformPivots(Iterator<Object> pivotIterator, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        parallelTransformerImpl.processIteratorSequentially(pivotIterator, MetricSpacesStorageInterface.OBJECT_TYPE.PIVOT_OBJECT, additionalParameters);
    }

    public static void transformQueryObjects(Iterator<Object> queriesIterator, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        parallelTransformerImpl.processIteratorSequentially(queriesIterator, MetricSpacesStorageInterface.OBJECT_TYPE.QUERY_OBJECT, additionalParameters);
    }

}
