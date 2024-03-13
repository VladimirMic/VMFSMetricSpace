/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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
public class FSLearnGroundTruthAndAllPivotFilterings {

    public static final Logger LOG = Logger.getLogger(FSLearnGroundTruthAndAllPivotFilterings.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        boolean publicQueries = true;
        Dataset[] datasets = {
            //            new FSDatasetInstanceSingularizator.RandomDataset10Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset25Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset30Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset35Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset40Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset50Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset60Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset70Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset80Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset90Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset100Uniform()
            //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
            //            //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
                        new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset(),
            //            new FSDatasetInstanceSingularizator.LAION_30M_PCA256Dataset()
//            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_30M_Dataset_Euclid(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset_Euclid(publicQueries)
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) throws FileNotFoundException {
        boolean prohibited = FSEvaluateGroundTruthMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Ground already existed for dataset {0}", dataset);
            prohibited = askForRewriting("Ground truth", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, evaluating ground truth", dataset);
            FSEvaluateGroundTruthMain.run(dataset);
        }
        prohibited = FSEvalAndStoreSampleOfSmallestDistsMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Smallest distances already evaluated for dataset {0}", dataset);
            prohibited = askForRewriting("Smallest distance", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, evaluating smallest distances", dataset);
            FSEvalAndStoreSampleOfSmallestDistsMain.run(dataset);
        }

        String datasetName = dataset.getDatasetName().toLowerCase();
        prohibited = FSEvalAndStoreObjectsToPivotsDistsMain.existsForDataset(dataset, FSEvalAndStoreObjectsToPivotsDistsMain.PIVOT_COUNT);
        if (prohibited) {
            LOG.log(Level.WARNING, "Dists to pivots already evaluated for dataset {0}", dataset);
            prohibited = askForRewriting("Dists to pivots", dataset);
        }
        if (!prohibited && (datasetName.contains("10m") || datasetName.contains("1m"))) {
            LOG.log(Level.INFO, "Dataset: {0}, evaluating objects to pivot distances", dataset);
            FSEvalAndStoreObjectsToPivotsDistsMain.run(dataset, FSEvalAndStoreObjectsToPivotsDistsMain.PIVOT_COUNT);
        }

        prohibited = FSLearnCoefsForDataDepenentMetricFilteringMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Coefs for Data-dependent Metric Filtering already evaluated for dataset {0}", dataset);
            prohibited = askForRewriting("Coefs for Data-dependent Metric Filtering", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, learning data dependent metric filtering", dataset);
            FSLearnCoefsForDataDepenentMetricFilteringMain.run(dataset);
        }

        prohibited = FSLearnCoefsForDataDepenentPtolemyFilteringMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Coefs for Data-dependent Generalised Ptolemaic Filtering already evaluated for dataset {0}", dataset);
            prohibited = askForRewriting("Coefs for Data-dependent Generalised Ptolemaic Filtering", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, learning coefs for data dependent ptolemaic filtering", dataset);
            FSLearnCoefsForDataDepenentPtolemyFilteringMain.run(dataset);
        }
    }

    private static boolean askForRewriting(String type, Dataset dataset) {
        String question = type + " for " + dataset.getDatasetName() + " already exists. Do you want to delete its content? (NEED TO BE CONFIRMED AGAIN AFTER EVALUATION)";
        Object[] options = new String[]{"Yes", "No"};
        LOG.log(Level.WARNING, "Asking for a question, waiting for the reply: {0} for {1}", new Object[]{type, dataset.getDatasetName()});
        int add = JOptionPane.showOptionDialog(null, question, "New file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.NO_OPTION);
        return add == 1;
    }
}
