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
import vm.fs.main.precomputeDistances.FSEvalAndStoreObjectsToPivotsDistsMain;
import vm.fs.main.precomputeDistances.FSEvalAndStoreSampleOfSmallestDistsMain;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentMetricFilteringMain;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentPtolemyFilteringMain;
import vm.m2.dataset.M2DatasetInstanceSingularizator;
import vm.metricSpace.Dataset;

/**
 *
 * @author au734419
 */
public class FSPrepareNewDatasetSelectQueriesPivotsLearnGroundTruthAndAllPivotFilterings {

    public static final Logger LOG = Logger.getLogger(FSPrepareNewDatasetSelectQueriesPivotsLearnGroundTruthAndAllPivotFilterings.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        boolean publicQueries = true;
        Dataset[] datasets = {
            //            new FSDatasetInstanceSingularizator.RandomDataset10Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(),
            //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform()
            //                        new FSDatasetInstanceSingularizator.RandomDataset25Uniform(),
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
            //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset(),
            //            new FSDatasetInstanceSingularizator.LAION_30M_PCA256Dataset()
            //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset_Euclid(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset_Euclid(publicQueries)
            new FSDatasetInstanceSingularizator.DeCAF100M_PCA256Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) throws FileNotFoundException {
        String datasetName = dataset.getDatasetName();
        boolean prohibited = PrintAndPlotDDOfDatasetMain.existsForDataset(dataset);
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, printing distance density plots", datasetName);
            PrintAndPlotDDOfDatasetMain.run(dataset);
        } else {
            LOG.log(Level.INFO, "Dataset: {0}, distance density plot already exists", datasetName);
        }

        prohibited = dataset.getPivots(-1) != null;
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, trying to select pivots and queries", datasetName);
            FSSelectRandomQueryObjectsAndPivotsFromDatasetMain.run(dataset);
        } else {
            LOG.log(Level.INFO, "Dataset: {0}, pivots already preselected", datasetName);
        }

        prohibited = FSEvaluateGroundTruthMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Ground already existed for dataset {0}", datasetName);
            prohibited = askForRewriting("Ground truth", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, evaluating ground truth", datasetName);
            FSEvaluateGroundTruthMain.run(dataset);
        }
        prohibited = FSEvalAndStoreSampleOfSmallestDistsMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Smallest distances already evaluated for dataset {0}", datasetName);
            prohibited = askForRewriting("Smallest distance", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, evaluating smallest distances", dataset);
            FSEvalAndStoreSampleOfSmallestDistsMain.run(dataset);
        }

        datasetName = datasetName.toLowerCase();
        prohibited = FSEvalAndStoreObjectsToPivotsDistsMain.existsForDataset(dataset, FSEvalAndStoreObjectsToPivotsDistsMain.PIVOT_COUNT);
        if (prohibited) {
            LOG.log(Level.WARNING, "Dists to pivots already evaluated for dataset {0}", datasetName);
            prohibited = askForRewriting("Dists to pivots", dataset);
        }
        if (!prohibited && (datasetName.contains("10m") || datasetName.contains("1m"))) {
            LOG.log(Level.INFO, "Dataset: {0}, evaluating objects to pivot distances", datasetName);
            FSEvalAndStoreObjectsToPivotsDistsMain.run(dataset, FSEvalAndStoreObjectsToPivotsDistsMain.PIVOT_COUNT);
        }

        prohibited = FSLearnCoefsForDataDepenentMetricFilteringMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Coefs for Data-dependent Metric Filtering already evaluated for dataset {0}", datasetName);
            prohibited = askForRewriting("Coefs for Data-dependent Metric Filtering", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, learning data dependent metric filtering", datasetName);
            FSLearnCoefsForDataDepenentMetricFilteringMain.run(dataset);
        }

        prohibited = FSLearnCoefsForDataDepenentPtolemyFilteringMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Coefs for Data-dependent Generalised Ptolemaic Filtering already evaluated for dataset {0}", datasetName);
            prohibited = askForRewriting("Coefs for Data-dependent Generalised Ptolemaic Filtering", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, learning coefs for data dependent ptolemaic filtering", datasetName);
            FSLearnCoefsForDataDepenentPtolemyFilteringMain.run(dataset);
        }
    }

    /**
     *
     * @param type
     * @param dataset
     * @return true iff the file CANNOT be overwritten
     */
    private static boolean askForRewriting(String type, Dataset dataset) {
        try {
            String question = type + " for " + dataset.getDatasetName() + " already exists. Do you want to delete its content? (NEED TO BE CONFIRMED AGAIN AFTER EVALUATION)";
            Object[] options = new String[]{"Yes", "No"};
            LOG.log(Level.WARNING, "Asking for a question, waiting for the reply: {0} for {1}", new Object[]{type, dataset.getDatasetName()});
            int add = JOptionPane.showOptionDialog(null, question, "New file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.NO_OPTION);
            return add == 1;
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "File exists and I cannot ask you for a permission to delete it. Skipping step.");
            return true;
        }
    }
}
