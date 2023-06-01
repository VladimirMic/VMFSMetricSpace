///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package vm.fs.main.search.filtering.learning;
//
//import vm.fs.dataset.FSDatasetInstanceSingularizator;
//import vm.fs.store.filtering.FSSimRelThresholdsTOmegaStorage;
//import vm.fs.store.voronoiPartitioning.FSVoronoiPartitioningStorage;
//import vm.metricSpace.Dataset;
//import vm.search.impl.VoronoiPartitionsCandSetIdentifier;
//import vm.simRel.impl.learn.ThresholdsTOmegaEvaluator;
//
///**
// *
// * @author Vlada
// */
//public class LearnTOmegaThresholdsForSimRelEuclidWithVoronoiMain {
//
//    public static void main(String[] args) {
//        Dataset[] datasets = new Dataset[]{
//            new FSDatasetInstanceSingularizator.LAION_10M_PCA96Dataset()
//        };
//        for (Dataset pcaDataset : datasets) {
//            run(new FSDatasetInstanceSingularizator.LAION_10M_Dataset(), pcaDataset);
//        }
//    }
//
//    private static void run(Dataset fullDataset, Dataset<float[]> pcaDataset) {
////        /* max size of the voronoi answer */
//        int kVoronoi = 300000;//300000
//        /* min size of the simRel answer */
//        int kPCA = 2000;
//        /* length of the PCA */
//        int pcaLength = 96;
//        /* number of query objects to learn t(\Omega) thresholds. We use different objects than the pivots tested. */
//        int querySampleCount = 200;//200
//        Integer pivotsCount = 2048;
//
//        FSSimRelThresholdsTOmegaStorage simRelStorage = new FSSimRelThresholdsTOmegaStorage(querySampleCount, pcaLength, kPCA, pivotsCount, kVoronoi);
////        SimRelEuclidThresholdsTOmegaStorage simRelStorage = new FSSimRelThresholdsTOmegaStorage(querySampleCount, dataSampleCount, pcaLength, kPCA);
//        ThresholdsTOmegaEvaluator evaluator = new ThresholdsTOmegaEvaluator(querySampleCount, kPCA);
//        VoronoiPartitionsCandSetIdentifier alg = new VoronoiPartitionsCandSetIdentifier(fullDataset, new FSVoronoiPartitioningStorage(), pivotsCount);
//        evaluator.learnTOmegaThresholds(fullDataset, pcaDataset, simRelStorage, alg, kVoronoi, pcaLength, FSSimRelThresholdsTOmegaStorage.PERCENTILES);
//    }
//
//}
