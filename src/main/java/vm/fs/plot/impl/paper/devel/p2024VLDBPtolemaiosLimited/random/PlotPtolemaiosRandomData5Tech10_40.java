/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.impl.paper.devel.p2024VLDBPtolemaiosLimited.random;

import org.jfree.chart.JFreeChart;
import vm.colour.StandardColours.COLOUR_NAME;
import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.fs.plot.FSPlotFolders;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotXCategoriesPlotter;
import vm.search.algorithm.SearchingAlgorithm;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosRandomData5Tech10_40 extends FSAbstractPlotterFromResults {

    public static final Integer PIVOTS = 128;

    public PlotPtolemaiosRandomData5Tech10_40(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "Triangle Ineq.",
                "Data-dep. Metric Filtering",
                "Four Point Prop.",
                "Ptolemaic Filtering",
                "Ptolemaic w. Dyn. Pivots",
                "Data-dep. Pt.Filt. with Rand. P.",
                "Data-dep. Ptolemaic Filtering",
                "Sequential Brute Force"
        );
    }

    @Override
    protected COLOUR_NAME[] getVoluntaryColoursForTracesOrNull() {
        return new COLOUR_NAME[]{
            COLOUR_NAME.C1_BLUE,
            COLOUR_NAME.C2_RED,
            COLOUR_NAME.C3_GREEN,
            COLOUR_NAME.C4_ORANGE,
            COLOUR_NAME.C6_BROWN,
            COLOUR_NAME.C7_PURPLE,
            COLOUR_NAME.C5_VIOLET,
            COLOUR_NAME.CX_BLACK
        };
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "2024_08_" + PIVOTS + "_pivots_30NN_seq_triangle_inequality",
                "2024_08_" + PIVOTS + "_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_08_" + PIVOTS + "_pivots_30NN_seq_FourPointBasedFiltering",
                "2024_08_" + PIVOTS + "_pivots_30NN_seq_ptolemaios_" + PIVOTS + "LB_random_pivots",
                "2024_08_" + PIVOTS + "_pivots_30NN_seq_ptolemaios_" + PIVOTS + "LB",
                "2026_10_" + PIVOTS + "_pivots_30NN_data-dependent_ptolemaic_filtering_randomPivots_" + PIVOTS + "LB_random_pivots",
                "2024_08_" + PIVOTS + "_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_" + PIVOTS + "LB",
                "ground_truth"
        );
    }

    public int[] getDims() {
        return new int[]{10, 15, 20, 25, 30, 35, 40};
    }

    @Override
    public Object[] getDisplayedNamesOfGroupsThatMeansFiles() {
        int[] dims = getDims();
        Object[] ret = new Object[dims.length];
        for (int i = 0; i < dims.length; i++) {
            ret[i] = "Dimensionality " + dims[i];
        }
        return ret;
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        int[] dims = getDims();
        String[] ret = new String[dims.length];
        for (int i = 0; i < dims.length; i++) {
            ret[i] = dims[i] + "dim_uniform_1M__30";
        }
        return ret;
    }

    @Override
    public String getXAxisLabel() {
        return null;
    }

    @Override
    public AbstractPlotter getPlotter() {
        BoxPlotXCategoriesPlotter ret = new BoxPlotXCategoriesPlotter();
        ret.setBoxWidthScale(0.8f);
        ret.setForceHorizontalXLabels(true);
        return ret;
    }

//    @Override
//    protected void storePDF(AbstractPlotter plotter, String path, JFreeChart plot) {
//        plotter.storePlotPDF(path, plot, 3200, 600);
//    }

    @Override
    public String getResultName() {
        return "Filterings_" + PIVOTS + "p";
    }

    @Override
    public String getFolderForPlots() {
        return FSPlotFolders.Y2025_PTOLEMAIOS_LIMITEDRandom;
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / (1000000 - SearchingAlgorithm.K_IMPLICIT_FOR_QUERIES);
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return "# LBs checked per distance";
    }

}
