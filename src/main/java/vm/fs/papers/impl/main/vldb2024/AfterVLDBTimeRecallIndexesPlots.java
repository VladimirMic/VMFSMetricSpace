/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.papers.impl.main.vldb2024;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jfree.chart.JFreeChart;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import static vm.fs.main.search.perform.FSKNNQueriesSeqScanWithFilteringMain.RATIOS_OF_CANDIDATES_TO_TEST;
import static vm.fs.plot.FSPlotFolders.Y2025_AFTER_VLDB_PTOLEMAIOS_LIMITED_FILTERING_INDEXES;
import vm.fs.store.auxiliaryForDistBounding.FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.auxiliaryForDistBounding.FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.metricSpace.Dataset;
import vm.metricSpace.DatasetOfCandidates;
import vm.metricSpace.distance.bounding.BoundsOnDistanceEstimation;
import vm.metricSpace.distance.bounding.onepivot.AbstractOnePivotFilter;
import vm.metricSpace.distance.bounding.onepivot.impl.TriangleInequality;
import vm.metricSpace.distance.bounding.twopivots.AbstractPtolemaicBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.AbstractTwoPivotsFilter;
import vm.metricSpace.distance.bounding.twopivots.impl.DataDependentPtolemaicFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.FourPointBasedFiltering;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaicFiltering;
import vm.plot.impl.LinesOrPointsPlotter;
import vm.search.algorithm.impl.GroundTruthEvaluator;

/**
 *
 * @author xmic
 */
public class AfterVLDBTimeRecallIndexesPlots {

    public static void main(String[] args) {
        DatasetOfCandidates[] datasets = new DatasetOfCandidates[]{
            new FSDatasetInstanceSingularizator.Faiss_Clip_100M_PCA256_Candidates(),
            new FSDatasetInstanceSingularizator.Faiss_DeCAF_100M_Candidates()
        };

        int k = GroundTruthEvaluator.K_IMPLICIT_FOR_QUERIES;

        for (DatasetOfCandidates dataset : datasets) {
            int pivotCount = dataset.getRecommendedNumberOfPivotsForFiltering();
            if (pivotCount < 0) {
                throw new IllegalArgumentException("Dataset " + dataset.getDatasetName() + " does not specify the number of pivots");
            }
            List<Integer> cands = getCandCounts(dataset.getCandidatesProvided(), RATIOS_OF_CANDIDATES_TO_TEST);
            BoundsOnDistanceEstimation[] filters = initTestedFilters(null, dataset.getPivots(pivotCount), dataset, k);
            run(dataset, filters, cands, k);
            System.gc();
        }

    }

    private static void run(DatasetOfCandidates dataset, BoundsOnDistanceEstimation[] filters, List<Integer> cands, int k) {
        LinesOrPointsPlotter plotter = new LinesOrPointsPlotter();
        plotter.setIncludeZeroForXAxis(true);
        plotter.setIncludeZeroForYAxis(false);
        float[][] xValues = new float[filters.length][cands.size()];
        float[][] yValues = new float[filters.length][cands.size()];
        for (int i = 0; i < filters.length; i++) {
            BoundsOnDistanceEstimation filter = filters[i];
            for (int j = 0; j < cands.size(); j++) {
                Integer cand = cands.get(j);
                float sumTimes = 0;
                float sumRecall = 0;
                dataset.setMaxNumberOfCandidatesToReturn(cand);
                String resultName = filter.getTechFullName();
                FSRecallOfCandidateSetsStorageImpl recallStorage = new FSRecallOfCandidateSetsStorageImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), resultName, null);
                Map<String, TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String>> map = recallStorage.getContent();
                for (Map.Entry<String, TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String>> entry : map.entrySet()) {
                    TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String> queryResult = entry.getValue();
                    String recall = queryResult.get(FSQueryExecutionStatsStoreImpl.QUERY_STATS.recall);
                    String time = queryResult.get(FSQueryExecutionStatsStoreImpl.QUERY_STATS.query_execution_time);
                    sumRecall += Float.parseFloat(recall);
                    sumTimes += Float.parseFloat(time);
                }
                xValues[i][j] = sumTimes / map.size();
                yValues[i][j] = sumRecall / map.size();
            }
        }
        String[] tracesNames = transformFiltersToTracesNames(filters);
        JFreeChart plot = plotter.createPlot("", "Time (ms)", "Recall of 30NN", tracesNames, null, xValues, yValues);
        File f = new File(Y2025_AFTER_VLDB_PTOLEMAIOS_LIMITED_FILTERING_INDEXES, dataset.getDatasetName());
        f.getParentFile().mkdirs();
        plotter.storePlotPDF(f, plot);
    }

    private static List<Integer> getCandCounts(int candidatesProvided, float[] ratios) {
        List<Integer> ret = new ArrayList<>();
        for (float ratio : ratios) {
            ret.add((int) (ratio * candidatesProvided));
        }
        return ret;
    }

    private static String[] transformFiltersToTracesNames(BoundsOnDistanceEstimation[] filters) {
        String[] ret = new String[filters.length];
        for (int i = 0; i < filters.length; i++) {
            BoundsOnDistanceEstimation filter = filters[i];
            ret[i] = filter.getTechName();
        }
        return ret;
    }

    public static final BoundsOnDistanceEstimation[] initTestedFilters(String resultSetPrefix, List pivots, Dataset dataset, Integer k) {
        int pivotCount = pivots.size();
        List pivotsData = dataset.getMetricSpace().getDataOfMetricObjects(pivots);
        if (resultSetPrefix == null) {
            resultSetPrefix = Tools.getDateYYYYMM() + "_" + pivotCount + "_pivots";
        }
        if (k != null) {
            resultSetPrefix += "_" + k + "NN";
        }
        AbstractOnePivotFilter metricFiltering = new TriangleInequality(resultSetPrefix);
        AbstractOnePivotFilter dataDependentMetricFiltering = FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(resultSetPrefix, pivotCount, dataset);
        AbstractTwoPivotsFilter fourPointPropertyBased = new FourPointBasedFiltering(resultSetPrefix);

        AbstractPtolemaicBasedFiltering ptolemaicFilteringRandomPivots = new PtolemaicFiltering(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), false);
        AbstractPtolemaicBasedFiltering ptolemaicFiltering = new PtolemaicFiltering(resultSetPrefix, pivotsData, dataset.getDistanceFunction(), true);
        DataDependentPtolemaicFiltering dataDependentPtolemaicFiltering = FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstance(
                resultSetPrefix,
                dataset,
                pivotCount
        );
        return new BoundsOnDistanceEstimation[]{
            metricFiltering,
            dataDependentMetricFiltering,
            fourPointPropertyBased,
            ptolemaicFilteringRandomPivots,
            ptolemaicFiltering,
            dataDependentPtolemaicFiltering
        };
    }

}
