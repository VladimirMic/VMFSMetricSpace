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
public class PlotPtolemaiosClipDistFunc128P extends FSAbstractPlotterFromResults {

    public PlotPtolemaiosClipDistFunc128P(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "Triangle Ineq.",
                "Data-dep. Metric Filtering",
                "Four Point Property",
                "Ptolemaic Filtering",
                "Data-dep. Ptolemaic Filering",
                "Sequential Brute Force"
        );
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "2024_05_128_pivots_30NN_seq_triangle_inequality",
                "2024_05_128_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_05_128_pivots_30NN_seq_FourPointBasedFiltering",
                "2024_05_128_pivots_30NN_seq_ptolemaios_128LB",
                "2024_05_128_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_128LB",
                "ground_truth"
        );
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return strings(
                "Dot product",
                "Euclidean",
                "Angular"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "laion2B-en-clip768v2-n=10M.h5DotPro__public-queries-10k-clip768v2.h5__30",
                "laion2B-en-clip768v2-n=10M.h5euclid__public-queries-10k-clip768v2.h5__30",
                "laion2B-en-clip768v2-n=10M.h5Angular__public-queries-10k-clip768v2.h5__30"
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
        return "Filterings_Clip_DF";
    }

    @Override
    public String getFolderForPlots() {
        return FSPlotFolders.Y2024_PTOLEMAIOS_LIMITED_DF;
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return "# LBs checked per distance";
    }

    @Override
    protected AbstractPlotter.COLOUR_NAME[] getVoluntaryColoursForTracesOrNull() {
        return new AbstractPlotter.COLOUR_NAME[]{
            AbstractPlotter.COLOUR_NAME.C1_BLUE,
            AbstractPlotter.COLOUR_NAME.C2_RED,
            AbstractPlotter.COLOUR_NAME.C3_GREEN,
            AbstractPlotter.COLOUR_NAME.C4_ORANGE,
            AbstractPlotter.COLOUR_NAME.C5_VIOLET,
            AbstractPlotter.COLOUR_NAME.CX_BLACK
        };
    }

}
