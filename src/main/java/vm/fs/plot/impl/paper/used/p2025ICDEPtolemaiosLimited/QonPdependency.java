package vm.fs.plot.impl.paper.used.p2025ICDEPtolemaiosLimited;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.File;
import java.text.NumberFormat;
import java.util.Map;
import java.util.TreeMap;
import org.jfree.chart.JFreeChart;
import vm.colour.StandardColours;
import vm.fs.FSGlobal;
import vm.fs.plot.FSPlotFolders;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.plot.impl.LinesOrPointsPlotter;

/**
 *
 * @author Vlada
 */
public class QonPdependency {

    private static final int[] pivots = new int[]{32, 48, 64, 80, 96, 128};

    public static void main(String[] args) {
        QonPdependency dep = new QonPdependency();
        String[] traces = dep.getDisplayedNamesOfTracesThatMatchesFolders();
        Map<Float, Float>[] pToQForDatasets = new Map[traces.length];
        Map<Float, Float>[] filteringPower = new Map[traces.length];
        for (int i = 0; i < traces.length; i++) {
            pToQForDatasets[i] = new TreeMap<>();
            filteringPower[i] = new TreeMap<>();
            for (int p = 0; p < pivots.length; p++) {
                int pivot = pivots[p];
                float avg = dep.getAverageFilteringPower(traces[i], pivot);
                if (!Float.isNaN(avg)) {
                    pToQForDatasets[i].put((float) pivot, pivot / avg);
                    filteringPower[i].put((float) pivot, avg);
                }
            }
        }
        LinesOrPointsPlotter plotter = new LinesOrPointsPlotter();
        plotter.setIncludeZeroForXAxis(false);
        plotter.setIncludeZeroForYAxis(true);
        plotter.setXStep(16d);
        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(0);
        for (int i = 0; i < pToQForDatasets.length; i++) {
            Map<Float, Float> pToQForDataset = pToQForDatasets[i];
            plotter.setLabelsAndPointColours(0, filteringPower[i].values(), null);
            plotter.setColouredLabelledPointsOrBars(false);
            plotter.setNumberFormatForTraceLabel(0, defaultFormat);
            JFreeChart plot = plotter.createPlot("", "Pivot count", "Min number of queries", "", StandardColours.COLOUR_NAME.C5_VIOLET, pToQForDataset);
            File file = new File(dep.getFolderForPlots(), traces[i] + "_QonP");
            plotter.storePlotPDF(file, plot, 550, 550);
        }
    }

    private float getAverageFilteringPower(String trace, int pivot) {
        File file = getFile(pivot, trace);
        if (!file.exists()) {
            System.err.println("File " + file.getAbsolutePath() + " does not exist");
            return Float.NaN;
        }
        FSQueryExecutionStatsStoreImpl storage = new FSRecallOfCandidateSetsStorageImpl(file);
        Map<String, TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String>> results = storage.getContent();
        long sum = 0;
        int count = 0;
        for (Map.Entry<String, TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String>> entry : results.entrySet()) {
            TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String> map = entry.getValue();
            String cands = map.get(FSQueryExecutionStatsStoreImpl.QUERY_STATS.cand_set_dynamic_size);
            sum += Integer.parseInt(cands);
            count++;
        }
        int datasetSize = getDatasetSize(trace);
        float remain = sum / (float) count;
        float ret = 1 - (remain / datasetSize);
        System.err.println(trace + ", pivots: " + pivot + ": " + count + " queries, power: " + ret);
        return ret;
    }

    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return new String[]{"DeCAF", "PKU-MMD_10fps", "PKU-MMD_30fps"};
    }

    public File getFile(int pivotCount, String traceName) {
        File ret = new File(FSGlobal.RESULT_FOLDER);
        String month = "06";
        if (pivotCount == 128 && !traceName.equals("DeCAF")) {
            month = "05";
        }
        String folderName = "2025_" + month + "_" + pivotCount + "_pivots_30NN_data-dependent_ptolemaic_filtering_" + pivotCount + "LB";
        ret = new File(ret, folderName);
        ret = new File(ret, FSGlobal.RESULT_STATS_FOLDER);
        String file = null;
        switch (traceName) {
            case "DeCAF":
                file = "decaf_1m__decaf_1m__30__decaf_1m__decaf_1m__.csv";
                break;
            case "PKU-MMD_10fps":
                file = "actions-single-subject-all-POS-fps10.data_selected.txt__actions-single-subject-all-POS-fps10.data_selected.txt__30__actions-single-subject-all-POS-fps10.data_selected.txt__actions-single-subject-all-POS-fps10.data_selected.txt__.csv";
                break;
            case "PKU-MMD_30fps":
                file = "actions-single-subject-all-POS.data_selected.txt__actions-single-subject-all-POS.data_selected.txt__30__actions-single-subject-all-POS.data_selected.txt__actions-single-subject-all-POS.data_selected.txt__.csv";
                break;
        }
        ret = new File(ret, file);
        return ret;
    }

    public String getFileName(int pivotCount, String traceName) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public String getFolderForPlots() {
        return FSPlotFolders.Y2025_PTOLEMAIOS_LIMITED;
    }

    private int getDatasetSize(String trace) {
        if (trace.contains("DeCAF")) {
            return 1000000;
        }
        return 17311;
    }

}
