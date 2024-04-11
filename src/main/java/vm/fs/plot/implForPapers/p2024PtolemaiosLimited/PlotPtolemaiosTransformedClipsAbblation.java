/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.fs.plot.FSPlotFolders;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotPlotter;

/**
 *
 * @author Vlada
 */
public class PlotPtolemaiosTransformedClipsAbblation extends FSAbstractPlotterFromResults {

    public PlotPtolemaiosTransformedClipsAbblation(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "Data-dep. Metric Filtering 256p_256LB",
                "256p_256LB",
                "256p_128LB",
                "256p_64LB",
                "256p_32LB",
                "64p_256LB",
                "64p_128LB",
                "64p_64LB",
                "64p_32LB", 
                "32p_256LB",
                "32p_128LB",
                "32p_64LB",
                "32p_32LB",
                "Sequential Brute Force"
        );
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "2024_03_256_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_03_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection",
                "2024_04_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_128LB",
                "2024_04_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_64LB",
                "2024_04_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_32LB",
                "2024_04_64_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_256LB",
                "2024_04_64_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_128LB",
                "2024_04_64_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_64LB",
                "2024_04_64_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_32LB",
                "2024_04_32_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_256LB",
                "2024_04_32_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_128LB",
                "2024_04_32_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_64LB",
                "2024_04_32_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_32LB",
                "ground_truth"
        );
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return strings(
                "CLIP_10M_PCA256",
                "CLIP_10M_GHP512"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "laion2B-en-clip768v2-n=10M.h5_PCA256__laion2B-en-clip768v2-n=10M.h5_PCA256__30__laion2B-en-clip768v2-n=10M.h5_PCA256__laion2B-en-clip768v2-n=10M.h5_PCA256__",
                "laion2B-en-clip768v2-n=10M.h5_GHP_50_512__public-queries-10k-clip768v2.h5_GHP_50_512__30__laion2B-en-clip768v2-n=10M.h5_GHP_50_512__public-queries-10k-clip768v2.h5_GHP_50_512__"
        );
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / 10120191;
    }

    @Override
    public String getXAxisLabel() {
        return null;
    }

    @Override
    public AbstractPlotter getPlotter() {
        return new BoxPlotPlotter();
    }

    @Override
    public String getResultName() {
        return "Filterings";
    }

    @Override
    public String getFolderForPlots() {
        return FSPlotFolders.Y2024_PTOLEMAIOS_LIMITED;
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return "# LBs checked per distance";
    }

    @Override
    protected AbstractPlotter.COLOUR_NAMES[] getColoursForTraces() {
        return null;
    }

}
