package vm.fs.main.datatools.storage;

import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstances;
import vm.searchSpace.Dataset;
import vm.fs.searchSpaceImpl.VMMVStorage;

/**
 *
 * @author Vlada
 */
public class VMMVStorageInsertMain {

    public static final Logger LOG = Logger.getLogger(VMMVStorageInsertMain.class.getName());

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstances.SIFTdataset(),
            new FSDatasetInstances.DeCAFDataset(),
            new FSDatasetInstances.MPEG7dataset(),
            new FSDatasetInstances.RandomDataset20Uniform()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    public static void run(Dataset dataset) {
        VMMVStorage storage = new VMMVStorage(dataset.getDatasetName(), true);
        storage.insertObjects(dataset);
        LOG.log(Level.INFO, "Finished. Stored {0} objects", storage.size());
    }

}
