package vm.fs.main.datatools.partitioning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.partitioning.FSStorageDatasetPartitionsInterface;
import vm.fs.store.partitioning.FSVoronoiPartitioningStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.datasetPartitioning.AbstractDatasetPartitioning;
import vm.metricSpace.datasetPartitioning.impl.StreamKNNClassifierWithFilter;
import vm.metricSpace.datasetPartitioning.impl.VoronoiPartitioningWithoutFilter;
import vm.metricSpace.distance.bounding.BoundsOnDistanceEstimation;
import vm.metricSpace.distance.bounding.onepivot.AbstractOnePivotFilter;
import vm.metricSpace.distance.bounding.onepivot.impl.TriangleInequality;
import vm.metricSpace.distance.bounding.twopivots.AbstractPtolemaicBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.AbstractTwoPivotsFilter;
import vm.metricSpace.distance.bounding.twopivots.impl.DataDependentPtolemaicFilteringForStreamKNNClassifier;
import vm.metricSpace.distance.bounding.twopivots.impl.FourPointBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaicFilteringForStreamKNNClassifier;

/**
 *
 * @author au734419
 */
public class FSStreamKNNPartitioning {

    public static void main(String[] args) {
        boolean publicQueries = false;
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAFDataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
            new FSDatasetInstanceSingularizator.SIFTdataset(),
            new FSDatasetInstanceSingularizator.RandomDataset15Uniform(), //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries)
        //                    new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries)
        //        //                        new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset(),
        //        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset_Euclid(publicQueries),
        //        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries)
        };
        int clusterCount = 1000;

        for (Dataset dataset : datasets) {
            int pivotCount = dataset.getRecommendedNumberOfPivotsForFiltering();
            run(dataset, pivotCount, clusterCount);
        }
    }

    public static void run(Dataset dataset, int pivotCount, int clusterCount) {
        List<Object> centroids = dataset.getPivots(clusterCount);
        List<Object> pivots = centroids.subList(0, pivotCount);
        String resultSetPrefix = pivotCount + "pivotsFilt_" + clusterCount + "clusters";
        BoundsOnDistanceEstimation[] filters = initTestedFilters(resultSetPrefix, pivots, dataset);
        for (BoundsOnDistanceEstimation filter : filters) {
            StreamKNNClassifierWithFilter classifier = new StreamKNNClassifierWithFilter<>(dataset.getMetricSpace(), dataset.getDistanceFunction(), pivots, filter);
            FSVoronoiPartitioningStorage storage = new FSVoronoiPartitioningStorage();
            partition(dataset, classifier, pivotCount, storage);
            System.gc();
        }
    }

    private static Map<Comparable, List<Comparable>> partition(Dataset dataset, AbstractDatasetPartitioning partitioning, int pivotCount, FSStorageDatasetPartitionsInterface storage) {
        Iterator it = dataset.getMetricObjectsFromDataset();
        Map ret = partitioning.partitionObjects(it, dataset.getDatasetName(), storage, pivotCount);
        try {
            String path = storage.getFile(dataset.getDatasetName(), null, pivotCount, true).getAbsolutePath();
            path += ".log.csv";
            PrintStream tmp = System.err;
            System.setErr(new PrintStream(new FileOutputStream(path, true)));
            System.err.print(vm.javatools.Tools.getCurrDateAndTime());
            System.err.print(";");
            System.err.print(partitioning.getName());
            System.err.print(";Time;");
            System.err.print(partitioning.getLastTimeOfPartitioning());
            System.err.print(";Dist comps;");
            System.err.print(partitioning.getDcOfPartitioning());
            System.err.print(";");
            System.err.print(partitioning.getAdditionalStats());
            System.err.println();
            System.err.flush();
            System.setErr(tmp);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FSPartitioningMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    private static final BoundsOnDistanceEstimation[] initTestedFilters(String resultSetPrefix, List pivots, Dataset dataset) {
        int pivotCount = pivots.size();
        List pivotsData = dataset.getMetricSpace().getDataOfMetricObjects(pivots);
        if (resultSetPrefix == null) {
            resultSetPrefix = Tools.getDateYYYYMM() + "_" + pivotCount + "_pivots";
        }
        AbstractOnePivotFilter metricFiltering = new TriangleInequality(resultSetPrefix);
        AbstractOnePivotFilter dataDependentMetricFiltering = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(resultSetPrefix, pivotCount, dataset);
        AbstractTwoPivotsFilter fourPointPropertyBased = new FourPointBasedFiltering(resultSetPrefix);

        AbstractPtolemaicBasedFiltering ptolemaicFilteringRandomPivots = new PtolemaicFilteringForStreamKNNClassifier(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), false);
        AbstractPtolemaicBasedFiltering ptolemaicFiltering = new PtolemaicFilteringForStreamKNNClassifier(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), true);
        DataDependentPtolemaicFilteringForStreamKNNClassifier dataDependentPtolemaicFilteringRandomPivots = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceForVoronoiPartitioning(resultSetPrefix, dataset, pivotCount, false);
        DataDependentPtolemaicFilteringForStreamKNNClassifier dataDependentPtolemaicFiltering = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceForVoronoiPartitioning(resultSetPrefix, dataset, pivotCount, true);
        return new BoundsOnDistanceEstimation[]{
            null,
            metricFiltering,
            dataDependentMetricFiltering,
            fourPointPropertyBased,
            ptolemaicFilteringRandomPivots,
            ptolemaicFiltering,
            dataDependentPtolemaicFilteringRandomPivots,
            dataDependentPtolemaicFiltering
        };
    }

}
