package vm.fs.main.search.filtering.learning;

import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.metricSpace.Dataset;
import vm.fs.store.filtering.FSSimRelThresholdsTOmegaStorage;
import vm.fs.store.voronoiPartitioning.FSVoronoiPartitioningStorage;
import vm.search.impl.VoronoiPartitionsCandSetIdentifier;
import vm.simRel.impl.learn.ThresholdsTOmegaEvaluator;
import vm.simRel.impl.learn.storeLearnt.SimRelEuclidThresholdsTOmegaStorage;

/**
 *
 * @author Vlada
 */
public class LearnTOmegaThresholdsForSimRelEuclidMain {

    public static void main(String[] args) {
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_10M_PCA96Dataset(),};
        for (Dataset pcaDataset : datasets) {
            run(new FSDatasetInstanceSingularizator.LAION_10M_Dataset(), pcaDataset);
        }
    }

    private static void run(Dataset fullDataset, Dataset<float[]> pcaDataset) {
        /* max size of the voronoi answer */
        int kVoronoi = 300000;
        /* min size of the simRel answer */
        int kPCA = 500;
        /* length of the PCA */
        int pcaLength = 96;
        /* number of query objects to learn t(\Omega) thresholds. We use different objects than the pivots tested. */
        int querySampleCount = 200;//200
        /* size of the data sample to learn t(\Omega) thresholds, SISAP: 100K */
        int dataSampleCount = 1000000; // 1000000 = 1M
        float percentile = 0.95f;
        Integer pivotsCount = 2048;
  
        
//        FSSimRelThresholdsTOmegaStorage simRelStorage = new FSSimRelThresholdsTOmegaStorage(querySampleCount, dataSampleCount, 96, kPCA, percentile, voronoiPivots, kVoronoi);
        SimRelEuclidThresholdsTOmegaStorage simRelStorage = new FSSimRelThresholdsTOmegaStorage(querySampleCount, dataSampleCount, pcaLength, kPCA, percentile);
        ThresholdsTOmegaEvaluator evaluator = new ThresholdsTOmegaEvaluator(querySampleCount, dataSampleCount, kPCA, percentile);
        VoronoiPartitionsCandSetIdentifier alg = new VoronoiPartitionsCandSetIdentifier(fullDataset, new FSVoronoiPartitioningStorage(), pivotsCount);
        evaluator.learnTOmegaThresholds(fullDataset, pcaDataset, simRelStorage, alg, kVoronoi);
    }

}
