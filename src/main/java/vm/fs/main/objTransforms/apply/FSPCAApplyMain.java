package vm.fs.main.objTransforms.apply;

import java.sql.SQLException;
import java.util.Iterator;
import vm.fs.metricSpaceImpl.FSMetricSpaceImpl;
import vm.fs.metricSpaceImpl.FSMetricSpacesStorage;
import vm.fs.store.dataTransforms.TODOFSSVDStorageImpl;
import vm.metricspace.AbstractMetricSpace;
import vm.metricspace.MetricSpacesStorageInterface;
import vm.metricspace.dataToStringConvertors.SingularisedConvertors;
import vm.objTransforms.MetricObjectsParallelTransformerImpl;
import vm.objTransforms.perform.PCAMetricObjectTransformer;

/**
 *
 * @author Vlada
 */
public class FSPCAApplyMain {

    public static void main(String[] args) throws SQLException {
//        String origDatasetName = "sift_1m";
//        int finalDimension = 10;// 4, 8, 30, 72, 100, 128
//        int svdId = 3;

        String origDatasetName = "decaf_1m";
        int sampleSetSize = 100000;
        int finalDimension = 24;// 4, 8, 10, 12, 16, 24, 32, 68, 670, 1540, 2387
        int svdId = 6;

        AbstractMetricSpace<float[]> space = new FSMetricSpaceImpl();
        FSMetricSpacesStorage spaceStorage = new FSMetricSpacesStorage(space, SingularisedConvertors.FLOAT_VECTOR_SPACE);
        TODOFSSVDStorageImpl svdStorage = new TODOFSSVDStorageImpl(origDatasetName, sampleSetSize);
        float[][] vtMatrix = svdStorage.getVTMatrix(svdId);
        vtMatrix = vm.datatools.Tools.shrinkMatrix(vtMatrix, finalDimension, vtMatrix[0].length);
        PCAMetricObjectTransformer pca = new PCAMetricObjectTransformer(vtMatrix, svdStorage.getMeansOverColumns(svdId), space);

        MetricObjectsParallelTransformerImpl parallelTransformerImpl = new MetricObjectsParallelTransformerImpl(pca, spaceStorage, origDatasetName);
        transformDataset(origDatasetName, spaceStorage, parallelTransformerImpl, "Dataset with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + svdId + " to the length " + finalDimension);
        spaceStorage.updateDatasetSize(pca.getNameOfTransformedSetOfObjects(origDatasetName));
        transformPivots(origDatasetName, spaceStorage, parallelTransformerImpl, "Pivot set with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + svdId + " to the length " + finalDimension);
        transformQueryObjects(origDatasetName, spaceStorage, parallelTransformerImpl, "Query set with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + svdId + " to the length " + finalDimension);
    }

    private static void transformDataset(String origDatasetName, MetricSpacesStorageInterface spaceStorage, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getObjectsFromDataset(origDatasetName);
        parallelTransformerImpl.processIteratorInParallel(it, MetricSpacesStorageInterface.OBJECT_TYPE.DATASET_OBJECT, 16, additionalParameters);
    }

    private static void transformPivots(String pivotSetName, MetricSpacesStorageInterface spaceStorage, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getPivots(pivotSetName).iterator();
        parallelTransformerImpl.processIteratorSequentially(it, MetricSpacesStorageInterface.OBJECT_TYPE.PIVOT_OBJECT, additionalParameters);
    }

    private static void transformQueryObjects(String querySetName, MetricSpacesStorageInterface spaceStorage, MetricObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getQueryObjects(querySetName).iterator();
        parallelTransformerImpl.processIteratorSequentially(it, MetricSpacesStorageInterface.OBJECT_TYPE.QUERY_OBJECT, additionalParameters);
    }

}
