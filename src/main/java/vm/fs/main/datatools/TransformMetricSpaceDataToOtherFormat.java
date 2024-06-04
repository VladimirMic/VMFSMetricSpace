/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.m2.dataset.M2DatasetInstanceSingularizator;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.ToolsMetricDomain;

/**
 *
 * @author Vlada
 */
public class TransformMetricSpaceDataToOtherFormat {

    public static void main(String[] args) {
        Dataset origDataset = new M2DatasetInstanceSingularizator.DeCAF100MDatasetAndromeda();
        Dataset destDataset = new FSDatasetInstanceSingularizator.DeCAF100M_Dataset();

        transformObjects(origDataset.getMetricObjectsFromDataset(), origDataset.getMetricSpace(), destDataset);
    }

    private static void transformObjects(Iterator it, AbstractMetricSpace origMetricSpace, Dataset destDataset) {
        AbstractMetricSpace destMetricSpace = destDataset.getMetricSpace();
        int counter = 0;
        while (it.hasNext()) {
            List batch = Tools.getObjectsFromIterator(it, 50000);
            counter += batch.size();
            List transformed = ToolsMetricDomain.transformMetricObjectsToOtherRepresentation(batch, origMetricSpace, destMetricSpace);
            destDataset.getMetricSpacesStorage().storeObjectsToDataset(transformed.iterator(), -1, destDataset.getDatasetName());
            Logger.getLogger(TransformMetricSpaceDataToOtherFormat.class.getName()).log(Level.INFO, "Transformed {0} objects", counter);
        }
        destDataset.updateDatasetSize();
    }
}
