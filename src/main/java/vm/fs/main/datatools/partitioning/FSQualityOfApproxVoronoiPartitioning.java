package vm.fs.main.datatools.partitioning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.DataTypeConvertor;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.partitioning.FSVoronoiPartitioningStorage;
import vm.metricSpace.Dataset;

/**
 *
 * @author Vlada
 */
public class FSQualityOfApproxVoronoiPartitioning {

    public static void main(String[] args) {
        boolean publicQueries = false;
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAFDataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
            //            //            new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset(),
            //            //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset_Euclid(publicQueries),
            //            //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries)
            new FSDatasetInstanceSingularizator.SIFTdataset(),
            new FSDatasetInstanceSingularizator.RandomDataset15Uniform()
        };
        int pivotCount = 256;

        for (Dataset dataset : datasets) {
            run(dataset, pivotCount);
        }
    }

    private static void run(Dataset groundTruthDataset, int pivotCount) {
        FSVoronoiPartitioningStorage storage = new FSVoronoiPartitioningStorage();
        File gtFile = storage.getFile(groundTruthDataset.getDatasetName(), null, pivotCount, false);
        Map<Comparable, TreeSet<Comparable>> gt = storage.loadAsTreeSets(gtFile);
        File[] files = storage.filesWithApproximatePartitionings(groundTruthDataset.getDatasetName(), pivotCount);
        File output = new File(gtFile.getAbsolutePath() + "_quality_WRT_groundTruth.csv");
        try {
            System.setErr(new PrintStream(new FileOutputStream(output, false)));
            for (File file : files) {
                Map<Comparable, TreeSet<Comparable>> approx = storage.loadAsTreeSets(file);
                float intersection = computeIntersection(gt, approx);
                System.err.println(vm.javatools.Tools.getCurrDateAndTime() + ";" + file.getName() + ";" + intersection);
            }
            System.err.flush();
            System.err.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(FSQualityOfApproxVoronoiPartitioning.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static float computeIntersection(Map<Comparable, TreeSet<Comparable>> gt, Map<Comparable, TreeSet<Comparable>> approx) {
        int numerator = 0;
        int denominator = 0;
        for (Map.Entry<Comparable, TreeSet<Comparable>> gtCellEntry : gt.entrySet()) {
            Comparable gtPivotId = gtCellEntry.getKey();
            TreeSet<Comparable> gtCell = gtCellEntry.getValue();
            denominator += gtCell.size();
            if (!approx.containsKey(gtPivotId)) {
                continue;
            }
            TreeSet<Comparable> approxCell = approx.get(gtPivotId);
            for (Comparable idFromGTCell : gtCell) {
                if (approxCell.contains(idFromGTCell)) {
                    numerator++;
                }
            }
        }
        return (float) numerator / denominator;
    }

}
