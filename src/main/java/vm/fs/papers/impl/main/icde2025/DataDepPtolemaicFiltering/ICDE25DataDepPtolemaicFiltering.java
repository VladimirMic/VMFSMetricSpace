package vm.fs.papers.impl.main.icde2025.DataDepPtolemaicFiltering;

import java.util.List;
import vm.fs.FSGlobal;
import vm.fs.main.datatools.FSPrepareNewDatasetForPivotFilterings;
import vm.fs.main.search.perform.FSKNNQueriesSeqScanWithFilteringMain;
import vm.fs.plot.impl.paper.submittedcodes.vldb2024.ICDEPlotter;
import vm.fs.searchSpaceImpl.FSSearchSpaceImpl;
import vm.fs.searchSpaceImpl.FSSearchSpacesStorage;
import vm.search.algorithm.SearchingAlgorithm;
import vm.search.algorithm.impl.GroundTruthEvaluator;
import vm.searchSpace.Dataset;
import vm.searchSpace.DatasetOfCandidates;
import vm.searchSpace.data.RandomVectorsGenerator;
import vm.searchSpace.data.toStringConvertors.SingularisedConvertors;
import vm.searchSpace.distance.DistanceFunctionInterface;
import vm.searchSpace.distance.bounding.BoundsOnDistanceEstimation;
import vm.searchSpace.distance.impl.L2OnFloatsArray;
import vm.searchSpace.data.toStringConvertors.SearchObjectDataToStringInterface;

/**
 *
 * @author Vlada
 */
public class ICDE25DataDepPtolemaicFiltering {

    /**
     * Just for naming produces files.
     */
    public static final String DATASET_PREFIX_NAME = "ICDE_random";
    /**
     * Not necessary for small dataset, useful if time-consuming.
     */
    public static final Boolean STORE_DISTANCES_TO_PIVOTS = false;
    /**
     * If true, checks whether the filterings are learnt, and if so, asks for
     * re-learning them. If false and filterings are learnt, they are
     * immediatelly re-learnt without asking. Results of the filtering are
     * rewritten with each new run.
     */
    public static final Boolean SKIP_EVERYHING_PREPARED = true;

    public static void main(String[] args) {
        FSGlobal.askWhenGoingToOverrideFile = false;
        // params. Feel free to modify.
        int[] dimensionalities = {10, 40};
        // number of vectors in each dataset
        int datasetObjectCount = 1000 * 1000;
        // number of pivots used for the filterings. In current settings, it also equals the number of defined lower bounds per each distance.
        int pivotsCount = 64;
        // number of generated and examined query objects
        int queriesCount = 1000;
        // the result set size for kNN search
        int k = 30;

        // Do not modify from here.
        Dataset[] datasets = createOrGetRandomUniformDatasetQueriesPivots(datasetObjectCount, pivotsCount, queriesCount, dimensionalities);
        for (Dataset dataset : datasets) {
            FSPrepareNewDatasetForPivotFilterings.setSkipEverythingEvaluated(SKIP_EVERYHING_PREPARED);
            learnFilterings(dataset);
            List pivots = dataset.getPivots(pivotsCount);
            FSKNNQueriesSeqScanWithFilteringMain.initPODists(dataset, pivotsCount, -1, pivots, false);
            BoundsOnDistanceEstimation[] filters = FSKNNQueriesSeqScanWithFilteringMain.initTestedFilters("ICDE_", pivots, dataset, k);
            FSPrepareNewDatasetForPivotFilterings.evaluateGroundTruth(dataset, GroundTruthEvaluator.K_IMPLICIT_FOR_GROUND_TRUTH);
            FSPrepareNewDatasetForPivotFilterings.evaluateGroundTruth(dataset, k);
            FSPrepareNewDatasetForPivotFilterings.setSkipEverythingEvaluated(false);
            FSKNNQueriesSeqScanWithFilteringMain.run(dataset, filters, pivots, k);
            createPlotsForDataset(dataset, filters, k, pivots);
            System.gc();
        }

    }

    private static Dataset[] createOrGetRandomUniformDatasetQueriesPivots(int datasetObjectCount, int pivotsCount, int queriesCount, int... dimensionalities) {
        DistanceFunctionInterface df = new L2OnFloatsArray();
        FSSearchSpaceImpl searchSpace = new FSSearchSpaceImpl(df);
        SearchObjectDataToStringInterface<float[]> dataSerializator = SingularisedConvertors.FLOAT_VECTOR_SPACE;
        int[] sizes = {datasetObjectCount, queriesCount, pivotsCount};
        RandomVectorsGenerator generator = new RandomVectorsGenerator(new FSSearchSpacesStorage(searchSpace, dataSerializator), sizes, dimensionalities);
        return generator.createOrGet(DATASET_PREFIX_NAME);
    }

    private static void learnFilterings(Dataset dataset) {
        FSPrepareNewDatasetForPivotFilterings.precomputeDatasetSize(dataset);
        Dataset origDataset = dataset;
        if (dataset instanceof DatasetOfCandidates) {
            origDataset = ((DatasetOfCandidates) dataset).getOrigDataset();
            FSPrepareNewDatasetForPivotFilterings.plotDistanceDensity(origDataset);
        }
        FSPrepareNewDatasetForPivotFilterings.plotDistanceDensity(dataset); // not necessary
        FSPrepareNewDatasetForPivotFilterings.evaluateSampleOfSmallestDistances(dataset);
        if (STORE_DISTANCES_TO_PIVOTS) {
            FSPrepareNewDatasetForPivotFilterings.precomputeObjectToPivotDists(origDataset);
        }
        FSPrepareNewDatasetForPivotFilterings.learnDataDependentMetricFiltering(dataset);
        FSPrepareNewDatasetForPivotFilterings.learnDataDependentPtolemaicFiltering(dataset);
    }

    private static void createPlotsForDataset(Dataset dataset, BoundsOnDistanceEstimation[] filters, int k, List pivots) {
        String[] folders = new String[filters.length + 1];
        int i;
        for (i = 0; i < filters.length; i++) {
            BoundsOnDistanceEstimation filter = filters[i];
            SearchingAlgorithm alg = FSKNNQueriesSeqScanWithFilteringMain.initAlg(filter, dataset, dataset.getSearchSpace(), pivots, null, null);
            folders[i] = alg.getResultName();
        }
        folders[i] = "ground_truth";
        ICDEPlotter plotter = new ICDEPlotter(k, dataset, folders);
        plotter.makePlots();
    }
}
