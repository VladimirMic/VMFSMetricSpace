/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools;

import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.main.precomputeDistances.FSEvalAndStoreObjectsToPivotsDistsMain;
import vm.fs.store.precomputedDists.FSPrecomputedDistancesMatrixLoaderImpl;
import vm.searchSpace.Dataset;
import vm.searchSpace.distance.impl.LocalUltraMetricDFWithPrecomputedValues;

/**
 *
 * @author au734419
 */
public class FSCreateAndStoreLocallyUltraMetricDF {

    public static final Boolean DELETE_AT_THE_BEGINNING = true;
    private static final Logger LOG = Logger.getLogger(FSCreateAndStoreLocallyUltraMetricDF.class.getName());

    public static void run(Dataset dataset) {
        if (DELETE_AT_THE_BEGINNING) {
            FSEvalAndStoreObjectsToPivotsDistsMain.delete(dataset, dataset.getPrecomputedDatasetSize());
        }
        LOG.log(Level.INFO, "Precomputing distances for dataset {0}", dataset.getDatasetName());
        FSPrepareNewDatasetForPivotFilterings.precomputeObjectToPivotDists(dataset);
        LOG.log(Level.INFO, "Learning ultra metric for dataset {0}", dataset.getDatasetName());
        LocalUltraMetricDFWithPrecomputedValues lum = new LocalUltraMetricDFWithPrecomputedValues(new FSPrecomputedDistancesMatrixLoaderImpl(), dataset, false);
        FSEvalAndStoreObjectsToPivotsDistsMain.delete(dataset, dataset.getPrecomputedDatasetSize());
        LOG.log(Level.INFO, "Storing ultra metric for dataset {0}", dataset.getDatasetName());
        FSEvalAndStoreObjectsToPivotsDistsMain.run(dataset, dataset.getPrecomputedDatasetSize(), lum);
    }
}
