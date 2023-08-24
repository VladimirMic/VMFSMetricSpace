/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools.voronoiPartitioning;

import java.util.List;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.voronoiPartitioning.FSVoronoiPartitioningStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.datasetPartitioning.AbstractDatasetPartitioning;
import vm.metricSpace.datasetPartitioning.impl.GRAPPLEPartitioning;
import vm.metricSpace.datasetPartitioning.impl.VoronoiPartitioning;

/**
 *
 * @author Vlada
 */
public class FSGRAPPLEPartitioningMain {

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAFDataset()
        //            new FSDatasetInstanceSingularizator.LAION_100k_Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_300k_Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) {
        int pivotCount = 256;
        List<Object> pivots = dataset.getPivots(pivotCount);
//        AbstractDatasetPartitioning partitioning = new GRAPPLEPartitioning(dataset.getMetricSpace(), dataset.getDistanceFunction(), pivots);
//        FSVoronoiPartitioningStorage storage = new FSVoronoiPartitioningStorage();
//        partitioning.partitionObjects(dataset.getMetricObjectsFromDataset(), dataset.getDatasetName(), storage, pivotCount);
    }
}
