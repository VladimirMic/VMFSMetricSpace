/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.search.filtering.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.filtering.FSSimRelThresholdsTOmegaStorage;
import vm.fs.store.voronoiPartitioning.FSVoronoiPartitioningStorage;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.ToolsMetricDomain;
import vm.search.impl.SimRelSeqScanKNNCandSet;
import vm.search.impl.VoronoiPartitionsCandSetIdentifier;
import vm.simRel.impl.learn.SimRelEuclideanPCAForLearning;

/**
 *
 * @author Vlada
 */
public class LearnTOmegaThresholdsForSimRelCranberry {

    private static final Logger LOG = Logger.getLogger(LearnTOmegaThresholdsForSimRelCranberry.class.getName());

    public static void main(String[] args) {
        Dataset[] fullDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset()
        };
        Dataset[] pcaDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset()
        };
        for (int i = 0; i < fullDatasets.length; i++) {
            Dataset fullDataset = fullDatasets[i];
            Dataset pcaDataset = pcaDatasets[i];
            run(fullDataset, pcaDataset);
        }
    }

    private static void run(Dataset fullDataset, Dataset<float[]> pcaDataset) {
//        /* max size of the voronoi answer */
        int kVoronoi = 500000;
        /* min size of the simRel answer */
        int kPCA = 50;
        /* length of the PCA */
        int pcaLength = 256;
        /* number of query objects to learn t(\Omega) thresholds. We use different objects than the pivots tested. */
        int querySampleCount = 100;//200
        Integer pivotsCount = 20000;

        FSSimRelThresholdsTOmegaStorage simRelStorage = new FSSimRelThresholdsTOmegaStorage(querySampleCount, pcaLength, kPCA, pivotsCount, kVoronoi);
        VoronoiPartitionsCandSetIdentifier voronoiAlg = new VoronoiPartitionsCandSetIdentifier(fullDataset, new FSVoronoiPartitioningStorage(), pivotsCount);

        AbstractMetricSpace<float[]> fullDatasetMetricSpace = fullDataset.getMetricSpace();
        AbstractMetricSpace<float[]> pcaDatasetMetricSpace = pcaDataset.getMetricSpace();
        List<Object> fullQuerySamples = fullDataset.getPivots(querySampleCount);
        Map<Object, Object> pcaQueryObjectsMap = ToolsMetricDomain.getMetricObjectsAsIdObjectMap(pcaDatasetMetricSpace, pcaDataset.getPivots(-1), false);
        Map<Object, Object> pcaAllObjectsMap = ToolsMetricDomain.getMetricObjectsAsIdObjectMap(pcaDatasetMetricSpace, pcaDataset.getMetricObjectsFromDataset(), false);

        SimRelEuclideanPCAForLearning simRelLearn = new SimRelEuclideanPCAForLearning(pcaLength);

        SimRelSeqScanKNNCandSet simRelAlg = new SimRelSeqScanKNNCandSet(simRelLearn, kPCA);

        for (int i = 0; i < fullQuerySamples.size(); i++) {
            Object fullQueryObject = fullQuerySamples.get(i);
            simRelLearn.resetCounters(pcaLength);
            Object queryObjId = fullDatasetMetricSpace.getIDOfMetricObject(fullQueryObject);
            Object pcaQueryObject = pcaQueryObjectsMap.get(queryObjId);
            List voronoiCandsIDs = voronoiAlg.candSetKnnSearch(fullDatasetMetricSpace, fullQueryObject, kVoronoi, null);
            List pcaOfCandidates = new ArrayList();
            for (Object voronoiCandID : voronoiCandsIDs) {
                Object pcaObj = pcaAllObjectsMap.get(voronoiCandID);
                pcaOfCandidates.add(pcaObj);
            }

            simRelAlg.candSetKnnSearch(pcaDataset.getMetricSpace(), pcaQueryObject, kPCA, pcaOfCandidates.iterator());
            LOG.log(Level.INFO, "Learning tresholds with the query obj {0}, i.e., qID {0}", new Object[]{i + 1, queryObjId});
        }

        float[][] ret = simRelLearn.getDiffWhenWrong(FSSimRelThresholdsTOmegaStorage.PERCENTILES);
        simRelStorage.store(ret, pcaDataset.getDatasetName());

    }

}
