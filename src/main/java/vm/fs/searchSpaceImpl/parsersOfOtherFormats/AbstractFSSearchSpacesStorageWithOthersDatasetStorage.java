package vm.fs.searchSpaceImpl.parsersOfOtherFormats;

import java.io.BufferedReader;
import java.io.File;
import java.util.AbstractMap;
import java.util.Iterator;
import vm.fs.FSGlobal;
import vm.fs.searchSpaceImpl.FSSearchSpacesStorage;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.data.toStringConvertors.SearchObjectDataToStringInterface;

/**
 *
 * @author Vlada
 * @param <T>
 */
public abstract class AbstractFSSearchSpacesStorageWithOthersDatasetStorage<T> extends FSSearchSpacesStorage<T> {

    public AbstractFSSearchSpacesStorageWithOthersDatasetStorage(AbstractSearchSpace space, SearchObjectDataToStringInterface<T> dataSerializator) {
        super(space, dataSerializator);
    }

    @Override
    protected File getFileForObjects(String folder, String datasetName, boolean willBeDeleted) {
        if (!folder.equals(FSGlobal.DATASET_FOLDER)) {
            return super.getFileForObjects(folder, datasetName, willBeDeleted);
        }
        File ret = getFileForDataset(datasetName);
        if (willBeDeleted && ret.exists()) {
            throw new IllegalArgumentException("Attempt to delete implicit dataset!");
        }
        return ret;
    }

    @Override
    protected Iterator<AbstractMap.SimpleEntry<String, T>> getIteratorForReader(BufferedReader br, int count, String filePath) {
        if (filePath.startsWith(FSGlobal.DATASET_FOLDER)) {
            return createIteratorForReader(br.lines().iterator(), count);
        }
        return super.getIteratorForReader(br, count, filePath);
    }

    protected abstract Iterator<AbstractMap.SimpleEntry<String, T>> createIteratorForReader(Iterator<String> lines, int maxObjCountToReturn);

    protected abstract File getFileForDataset(String datasetName);
}
