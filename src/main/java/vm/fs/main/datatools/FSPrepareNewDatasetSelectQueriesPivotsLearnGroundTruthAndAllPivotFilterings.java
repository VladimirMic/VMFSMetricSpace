/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.main.datatools.storage.VMMVStorageInsertMain;
import vm.fs.main.precomputeDistances.FSEvalAndStoreObjectsToPivotsDistsMain;
import vm.fs.main.precomputeDistances.FSEvalAndStoreSampleOfSmallestDistsMain;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentMetricFilteringMain;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDependentPtolemyFilteringMain;
import vm.metricSpace.Dataset;
import vm.search.algorithm.impl.GroundTruthEvaluator;

/**
 *
 * @author au734419
 */
public class FSPrepareNewDatasetSelectQueriesPivotsLearnGroundTruthAndAllPivotFilterings {

    public static final Boolean SKIP_EVERYTHING_EVALUATED = true;
    public static final Logger LOG = Logger.getLogger(FSPrepareNewDatasetSelectQueriesPivotsLearnGroundTruthAndAllPivotFilterings.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        boolean publicQueries = true;
        Dataset[] datasets = {
//            new FSDatasetInstanceSingularizator.Faiss_DeCAF_100M_PCA256_Candidates(),
            new FSDatasetInstanceSingularizator.Faiss_Clip_100M_PCA256_Candidates()
//            new FSDatasetInstanceSingularizator.DeCAF100M_PCA256Dataset()
        //            new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset(),
        //            new FSDatasetInstanceSingularizator.RandomDataset10Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset20Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset25Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset30Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset35Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset40Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset50Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset60Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset70Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset80Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset90Uniform(),
        //            new FSDatasetInstanceSingularizator.RandomDataset100Uniform(),
        //            new FSDatasetInstanceSingularizator.DeCAFDataset(),
        //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
        //            new FSDatasetInstanceSingularizator.SIFTdataset()

        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_30M_PCA256Dataset()
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset_Euclid(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset_Euclid(publicQueries)            
        //            new FSDatasetInstanceSingularizator.DeCAF_PCA1540Dataset(),
        //                    new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
        //                    new FSDatasetInstanceSingularizator.LAION_10M_Dataset(true),
//            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Dot(true)
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(true)
//                    new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Angular(true)
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    private static void run(Dataset dataset) throws FileNotFoundException {
        int precomputedDatasetSize = dataset.getPrecomputedDatasetSize();
        if (precomputedDatasetSize < 0) {
            dataset.updateDatasetSize();
        }
        String datasetName = dataset.getDatasetName();
        boolean prohibited;

        prohibited = PrintAndPlotDDOfDatasetMain.existsForDataset(dataset);
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

        prohibited = FSEvaluateGroundTruthMain.existsForDataset(dataset, null);
        if (prohibited) {
            LOG.log(Level.WARNING, "Ground already exists for dataset {0}", datasetName);
            prohibited = askForRewriting("Ground truth", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, evaluating ground truth", datasetName);
            FSEvaluateGroundTruthMain.run(dataset, GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH);
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

        prohibited = FSLearnCoefsForDataDependentPtolemyFilteringMain.existsForDataset(dataset);
        if (prohibited) {
            LOG.log(Level.WARNING, "Coefs for Data-dependent Generalised Ptolemaic Filtering already evaluated for dataset {0}", datasetName);
            prohibited = askForRewriting("Coefs for Data-dependent Generalised Ptolemaic Filtering", dataset);
        }
        if (!prohibited) {
            LOG.log(Level.INFO, "Dataset: {0}, learning coefs for data dependent ptolemaic filtering", datasetName);
            FSLearnCoefsForDataDependentPtolemyFilteringMain.run(dataset);
        }

        if (dataset.getPrecomputedDatasetSize() > 30000000) {
            Map keyValueStorage = dataset.getKeyValueStorage();
            prohibited = !(keyValueStorage == null || keyValueStorage.isEmpty());
            if (prohibited) {
                LOG.log(Level.WARNING, "The key value storage already exists for dataset {0}", datasetName);
                prohibited = askForRewriting("The key value storage", dataset);
            }
            if (!prohibited) {
                LOG.log(Level.INFO, "Dataset: {0}, creating key-value storage", datasetName);
                VMMVStorageInsertMain.run(dataset);
            }
        }
    }

    /**
     *
     * @param type
     * @param dataset
     * @return true iff the file CANNOT be overwritten
     */
    private static boolean askForRewriting(String type, Dataset dataset) {
        if (SKIP_EVERYTHING_EVALUATED) {
            return true;
        }
        try {
            String question = type + " for " + dataset.getDatasetName() + " already exists. Do you want to delete its content? (NEED TO BE CONFIRMED AGAIN AFTER EVALUATION)";
            Object[] options = new String[]{"Yes", "No"};
            LOG.log(Level.WARNING, "Asking for a question, waiting for the reply: {0} for {1}", new Object[]{type, dataset.getDatasetName()});
            int add = JOptionPane.showOptionDialog(null, question, "New file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.NO_OPTION);
            return add == 1;
//            return add != 0;
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "File exists and I cannot ask you for a permission to delete it. Skipping step.");
            return true;
        }
    }
}
