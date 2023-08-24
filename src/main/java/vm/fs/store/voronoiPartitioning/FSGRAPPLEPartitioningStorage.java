/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.store.voronoiPartitioning;

import java.io.File;
import vm.fs.FSGlobal;

/**
 *
 * @author Vlada
 */
public class FSGRAPPLEPartitioningStorage extends FSVoronoiPartitioningStorage {

    @Override
    public File getFile(String datasetName, int pivotCount, boolean willBeDeleted) {
        String name = datasetName + "_" + pivotCount + "pivots.csv.gz";
        File ret = new File(FSGlobal.GRAPPLE_PARTITIONING_STORAGE, name);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

}
