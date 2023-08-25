/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools.voronoiPartitioning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.voronoiPartitioning.FSGRAPPLEPartitioningStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.datasetPartitioning.AbstractDatasetPartitioning;
import vm.metricSpace.datasetPartitioning.impl.GRAPPLEPartitioning;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaiosFilteringWithLimitedAnglesSimpleCoef;
import vm.metricSpace.distance.bounding.twopivots.learning.LearningPtolemyInequalityWithLimitedAngles;

/**
 *
 * @author Vlada
 */
public class FSGRAPPLEPartitioningMain {

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAFDataset(),
            new FSDatasetInstanceSingularizator.MPEG7dataset(),
            new FSDatasetInstanceSingularizator.SIFTdataset(),
            //            new FSDatasetInstanceSingularizator.LAION_100k_Dataset(),
            //            new FSDatasetInstanceSingularizator.LAION_300k_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset()
        //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) {
        int pivotCount = 512;
        try {
            System.setOut(new PrintStream(new FileOutputStream("h:\\Similarity_search\\Auxiliary_for_filtering\\GRAPPLE_partitioning\\" + dataset.getDatasetName() + "_" + pivotCount + "err.csv", false)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GRAPPLEPartitioning.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<Object> pivots = dataset.getPivots(pivotCount);
        PtolemaiosFilteringWithLimitedAnglesSimpleCoef filter = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(pivotCount + "_pivots", dataset.getDatasetName(), pivotCount, LearningPtolemyInequalityWithLimitedAngles.ALL_PIVOT_PAIRS);
        AbstractDatasetPartitioning partitioning = new GRAPPLEPartitioning(filter, dataset.getMetricSpace(), dataset.getDistanceFunction(), pivots);
        FSGRAPPLEPartitioningStorage storage = new FSGRAPPLEPartitioningStorage();
        partitioning.partitionObjects(dataset.getMetricObjectsFromDataset(), dataset.getDatasetName(), storage, pivotCount);
        System.out.flush();
    }
}