/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.main.groundTruth.FSEvaluateGroundTruthMain;
import vm.fs.main.precomputeDistances.FSEvalAndStoreObjectsToPivotsDistsMain;
import vm.fs.main.precomputeDistances.FSEvalAndStoreSampleOfSmallestDistsMain;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentMetricFilteringMain;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentPtolemyFilteringMain;
import vm.metricSpace.Dataset;

/**
 *
 * @author au734419
 */
public class FSLearnMetadataForAllPivotFilterings {

    public static final Logger LOG = Logger.getLogger(FSLearnMetadataForAllPivotFilterings.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        Dataset[] datasets = {
//            new FSDatasetInstanceSingularizator.RandomDataset10Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset25Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset30Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset35Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset40Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset50Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset60Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset70Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset80Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset90Uniform(),
            new FSDatasetInstanceSingularizator.RandomDataset100Uniform()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) throws FileNotFoundException {
        LOG.log(Level.INFO, "Dataset: {0}, evaluating ground truth", dataset);
        FSEvaluateGroundTruthMain.run(dataset);
        LOG.log(Level.INFO, "Dataset: {0}, evaluating smallest distances", dataset);
        FSEvalAndStoreSampleOfSmallestDistsMain.run(dataset);
        LOG.log(Level.INFO, "Dataset: {0}, evaluating objects to pivot distances", dataset);
        FSEvalAndStoreObjectsToPivotsDistsMain.run(dataset, FSEvalAndStoreObjectsToPivotsDistsMain.PIVOT_COUNT);
        LOG.log(Level.INFO, "Dataset: {0}, learning data dependent metric filtering", dataset);
        FSLearnCoefsForDataDepenentMetricFilteringMain.run(dataset);
        LOG.log(Level.INFO, "Dataset: {0}, learning coefs for data dependent ptolemaic filtering", dataset);
        FSLearnCoefsForDataDepenentPtolemyFilteringMain.run(dataset);
    }
}
