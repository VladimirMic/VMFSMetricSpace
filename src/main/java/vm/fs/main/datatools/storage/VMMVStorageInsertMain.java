package vm.fs.main.datatools.storage;

import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
//import vm.m2.dataset.M2DatasetInstanceSingularizator;
import vm.metricSpace.Dataset;
import vm.fs.metricSpaceImpl.VMMVStorage;

/**
 *
 * @author Vlada
 */
public class VMMVStorageInsertMain {

    public static final Logger LOG = Logger.getLogger(VMMVStorageInsertMain.class.getName());

    public static void main(String[] args) {
        Dataset dataset;
//        if (args.length > 0) {
//            dataset = new M2DatasetInstanceSingularizator.DeCAF20MDataset();
//        } else {
            dataset = new FSDatasetInstanceSingularizator.DeCAFDataset();
//        }
        VMMVStorage storage = new VMMVStorage(dataset.getDatasetName(), true);
        storage.insertObjects(dataset.getMetricObjectsFromDataset(), dataset.getMetricSpace());
        LOG.log(Level.INFO, "Finished. Stored {0} objects", storage.size());
    }

}