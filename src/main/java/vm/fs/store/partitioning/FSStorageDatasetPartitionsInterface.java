package vm.fs.store.partitioning;

import java.io.File;
import vm.searchSpace.datasetPartitioning.StorageDatasetPartitionsInterface;

/**
 *
 * @author Vlada
 */
public abstract class FSStorageDatasetPartitionsInterface extends StorageDatasetPartitionsInterface {

    public abstract File getFile(String datasetName, String suffix, int pivotCount, boolean willBeDeleted);

}
