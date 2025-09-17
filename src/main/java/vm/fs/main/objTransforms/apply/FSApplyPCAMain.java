package vm.fs.main.objTransforms.apply;

import java.util.Iterator;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstances;
import static vm.fs.main.objTransforms.learning.FSLearnSVDMain.SAMPLE_COUNT;
import vm.fs.searchSpaceImpl.FSSearchSpacesStorage;
import vm.fs.store.dataTransforms.FSSVDStorageImpl;
import vm.objTransforms.SearchObjectsParallelTransformerImpl;
import vm.objTransforms.perform.PCAFloatVectorTransformer;
import vm.objTransforms.perform.PCAPrefixVectorTransformer;
import vm.searchSpace.AbstractSearchSpacesStorage;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.data.toStringConvertors.SingularisedConvertors;
import vm.objTransforms.SearchObjectTransformerInterface;

/**
 *
 * @author Vlada
 */
public class FSApplyPCAMain {

    public static final Integer PREFFIX_TO_STORE = -1;

    public static void main(String[] args) {
        boolean publicQueries = false;
        Dataset[] datasets = {
            new FSDatasetInstances.LAION_100M_Dataset(publicQueries)
        };

        for (Dataset dataset : datasets) {
            AbstractSearchSpacesStorage destStorage = new FSSearchSpacesStorage(dataset.getSearchSpace(), SingularisedConvertors.FLOAT_VECTOR_SPACE);
            run(dataset, destStorage);
            System.gc();
        }
    }

    private static void run(Dataset dataset, AbstractSearchSpacesStorage destStorage) {
//        int[] finalDimensions = new int[]{100, 128, 30, 4, 6, 72, 8}; // SIFT
//        int[] finalDimensions = new int[]{20, 18, 16, 15, 12, 10, 8}; // Random 20
//        int[] finalDimensions = new int[]{10, 12, 128, 1540, 16, 46, 2387, 24, 256, 32, 4, 6, 670, 68, 8}; // DeCAF
        int[] finalFullDimensions = new int[]{256}; // DeCAF

        AbstractSearchSpace<float[]> searchSpage = dataset.getSearchSpace();
        AbstractSearchSpacesStorage sourceSpaceStorage = dataset.getSearchSpacesStorage();
        String origDatasetName = dataset.getDatasetName();
        FSSVDStorageImpl svdStorage = new FSSVDStorageImpl(origDatasetName, SAMPLE_COUNT, false);
        float[][] vtMatrixFull = svdStorage.getVTMatrix();

        for (int finalDimension : finalFullDimensions) {
            float[][] vtMatrix = Tools.shrinkMatrix(vtMatrixFull, finalDimension, vtMatrixFull[0].length);

            SearchObjectTransformerInterface pca;
            if (PREFFIX_TO_STORE != null && PREFFIX_TO_STORE > 0) {
                pca = new PCAPrefixVectorTransformer(vtMatrix, svdStorage.getMeansOverColumns(), searchSpage, searchSpage, PREFFIX_TO_STORE);
            } else {
                pca = new PCAFloatVectorTransformer(vtMatrix, svdStorage.getMeansOverColumns(), searchSpage, searchSpage);
            }
            String newDatasetName = pca.getNameOfTransformedSetOfObjects(origDatasetName, false);
            String newQuerySetName = pca.getNameOfTransformedSetOfObjects(dataset.getQuerySetName(), false);
            String newPivotsName = pca.getNameOfTransformedSetOfObjects(dataset.getPivotSetName(), false);
            SearchObjectsParallelTransformerImpl parallelTransformerImpl = new SearchObjectsParallelTransformerImpl(pca, destStorage, newDatasetName, newQuerySetName, newPivotsName);
            transformPivots(dataset.getPivotSetName(), sourceSpaceStorage, parallelTransformerImpl, "Pivot set with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + SAMPLE_COUNT + " to the length " + finalDimension);
//            transformQueryObjects(dataset.getQuerySetName(), sourceSpaceStorage, parallelTransformerImpl, "Query set with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + SAMPLE_COUNT + " to the length " + finalDimension);
//            transformDataset(origDatasetName, sourceSpaceStorage, parallelTransformerImpl, "Dataset with name \"" + origDatasetName + "\" transformed by VT matrix of svd " + SAMPLE_COUNT + " to the length " + finalDimension);
            try {
                sourceSpaceStorage.updateDatasetSize(pca.getNameOfTransformedSetOfObjects(origDatasetName, false));
            } catch (Exception e) {
            }
        }
    }

    private static void transformDataset(String origDatasetName, AbstractSearchSpacesStorage spaceStorage, SearchObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getObjectsFromDataset(origDatasetName);
        parallelTransformerImpl.processIteratorInParallel(it, AbstractSearchSpacesStorage.OBJECT_TYPE.DATASET_OBJECT, vm.javatools.Tools.PARALELISATION, additionalParameters);
    }

    private static void transformPivots(String pivotSetName, AbstractSearchSpacesStorage spaceStorage, SearchObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getPivots(pivotSetName).iterator();
        parallelTransformerImpl.processIteratorSequentially(it, AbstractSearchSpacesStorage.OBJECT_TYPE.PIVOT_OBJECT, additionalParameters);
    }

    private static void transformQueryObjects(String querySetName, AbstractSearchSpacesStorage spaceStorage, SearchObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        Iterator<Object> it = spaceStorage.getQueryObjects(querySetName).iterator();
        parallelTransformerImpl.processIteratorSequentially(it, AbstractSearchSpacesStorage.OBJECT_TYPE.QUERY_OBJECT, additionalParameters);
    }

    public static void transformDataset(Iterator<Object> dataIterator, SearchObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        parallelTransformerImpl.processIteratorInParallel(dataIterator, AbstractSearchSpacesStorage.OBJECT_TYPE.DATASET_OBJECT, vm.javatools.Tools.PARALELISATION, additionalParameters);
    }

    public static void transformPivots(Iterator<Object> pivotIterator, SearchObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        parallelTransformerImpl.processIteratorSequentially(pivotIterator, AbstractSearchSpacesStorage.OBJECT_TYPE.PIVOT_OBJECT, additionalParameters);
    }

    public static void transformQueryObjects(Iterator<Object> queriesIterator, SearchObjectsParallelTransformerImpl parallelTransformerImpl, Object... additionalParameters) {
        parallelTransformerImpl.processIteratorSequentially(queriesIterator, AbstractSearchSpacesStorage.OBJECT_TYPE.QUERY_OBJECT, additionalParameters);
    }

}
