package vm.fs.main.datatools.partitioning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentMetricFilteringMain;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.partitioning.FSGRAPPLEPartitioningStorage;
import vm.fs.store.partitioning.FSStorageDatasetPartitionsInterface;
import vm.fs.store.partitioning.FSVoronoiPartitioningStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.datasetPartitioning.AbstractDatasetPartitioning;
import vm.metricSpace.datasetPartitioning.impl.GRAPPLEPartitioning;
import vm.metricSpace.datasetPartitioning.impl.VoronoiPartitioning;
import vm.metricSpace.distance.bounding.BoundsOnDistanceEstimation;
import vm.metricSpace.distance.bounding.onepivot.AbstractOnePivotFilter;
import vm.metricSpace.distance.bounding.onepivot.impl.TriangleInequality;
import vm.metricSpace.distance.bounding.twopivots.AbstractPtolemaicBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.AbstractTwoPivotsFilter;
import vm.metricSpace.distance.bounding.twopivots.impl.DataDependentGeneralisedPtolemaicFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.FourPointBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaicFiltering;

/**
 *
 * @author Vlada
 */
public class FSPartitioningMain {

    public static void main(String[] args) {
        boolean publicQueries = false;
        Dataset[] datasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAFDataset(), //            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset_Euclid(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset(),
        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset_Euclid(publicQueries),
        //            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries)
        //            //            new FSDatasetInstanceSingularizator.MPEG7dataset(),
        //            //            new FSDatasetInstanceSingularizator.SIFTdataset()
        //            //            new FSDatasetInstanceSingularizator.RandomDataset15Uniform()
        };

        int pivotCount = 256;

        for (Dataset dataset : datasets) {
            runVoronoiPartitioning(dataset, pivotCount);
        }
    }

    private static void runVoronoiPartitioning(Dataset dataset, int pivotCount) {
        List<Object> pivots = dataset.getPivots(pivotCount);
        String resultSetPrefix = pivotCount + "pivots";

        BoundsOnDistanceEstimation[] filters = initTestedFilters(resultSetPrefix, pivots, dataset);
        for (BoundsOnDistanceEstimation filter : filters) {
            VoronoiPartitioning vp = new VoronoiPartitioning(dataset.getMetricSpace(), dataset.getDistanceFunction(), pivots, filter);
            FSVoronoiPartitioningStorage storage = new FSVoronoiPartitioningStorage();

            partition(dataset, vp, pivotCount, storage);
        }
    }

    private static void runGRAPPLEPartitioning(Dataset dataset, BoundsOnDistanceEstimation filter, int pivotCount) {
        List<Object> pivots = dataset.getPivots(pivotCount);
        filter = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(pivotCount + "_pivots", dataset, pivotCount);
        AbstractDatasetPartitioning partitioning = new GRAPPLEPartitioning((DataDependentGeneralisedPtolemaicFiltering) filter, dataset.getMetricSpace(), dataset.getDistanceFunction(), pivots);
        FSGRAPPLEPartitioningStorage storage = new FSGRAPPLEPartitioningStorage();

        partition(dataset, partitioning, pivotCount, storage);

    }

    private static Map<Comparable, List<Comparable>> partition(Dataset dataset, AbstractDatasetPartitioning partitioning, int pivotCount, FSStorageDatasetPartitionsInterface storage) {
        Map ret = partitioning.partitionObjects(dataset.getMetricObjectsFromDataset(), dataset.getDatasetName(), storage, pivotCount);
        try {
            String path = storage.getFile(dataset.getDatasetName(), pivotCount, true).getAbsolutePath();
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

    public static final BoundsOnDistanceEstimation[] initTestedFilters(String resultSetPrefix, List pivots, Dataset dataset) {
        int pivotCount = pivots.size();
        List pivotsData = dataset.getMetricSpace().getDataOfMetricObjects(pivots);
        if (resultSetPrefix == null) {
            resultSetPrefix = Tools.getDateYYYYMM() + "_" + pivotCount + "_pivots";
        }
        AbstractOnePivotFilter metricFiltering = new TriangleInequality(resultSetPrefix);
        AbstractOnePivotFilter dataDependentMetricFiltering = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(
                resultSetPrefix,
                pivotCount,
                FSLearnCoefsForDataDepenentMetricFilteringMain.SAMPLE_O_COUNT,
                FSLearnCoefsForDataDepenentMetricFilteringMain.SAMPLE_Q_COUNT,
                dataset
        );
        AbstractTwoPivotsFilter fourPointPropertyBased = new FourPointBasedFiltering(resultSetPrefix);

        AbstractPtolemaicBasedFiltering ptolemaicFilteringRandomPivots = new PtolemaicFiltering(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), false);
        AbstractPtolemaicBasedFiltering ptolemaicFiltering = new PtolemaicFiltering(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), true);
        DataDependentGeneralisedPtolemaicFiltering dataDependentPtolemaicFiltering = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(
                resultSetPrefix,
                dataset,
                pivotCount
        );
        return new BoundsOnDistanceEstimation[]{
//            null,
            metricFiltering,
            dataDependentMetricFiltering
//            fourPointPropertyBased
        //            ptolemaicFilteringRandomPivots
        //            ptolemaicFiltering,
        //            dataDependentPtolemaicFiltering
        };
    }

}
