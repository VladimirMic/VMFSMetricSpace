package vm.fs.plot.impl.paper.used.p2025ICDEPtolemaiosLimited;

import org.jfree.chart.JFreeChart;
import vm.colour.StandardColours;
import vm.fs.plot.FSAbstractPlotterFromResults;
import static vm.fs.plot.FSAbstractPlotterFromResults.strings;
import vm.fs.plot.FSPlotFolders;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotXCategoriesPlotter;

/**
 *
 * @author Vlada
 */
public class PlotPtolemaiosWikipedia extends FSAbstractPlotterFromResults {

    protected static int pivotCount;
    protected static String month = "06";

    public static final int WIDTH = 600;
    public static final int HEIGHT = 470;

    public PlotPtolemaiosWikipedia(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    protected void storePDF(AbstractPlotter plotter, String path, JFreeChart plot) {
        plotter.storePlotPDF(path, plot, WIDTH, HEIGHT);
    }

    @Override
    protected void storePNG(AbstractPlotter plotter, String path, JFreeChart plot) {
        plotter.storePlotPNG(path, plot, WIDTH, HEIGHT);
    }

    public static void setPivotCount(int pivotCount) {
        PlotPtolemaiosWikipedia.pivotCount = pivotCount;
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "Triangle Ineq.",
                "Data-dep. Metric Filtering",
                "Four Point Prop.",
                "Ptolemaic Filtering",
                "Ptolemaic w. Dyn. Pivots",
                "Data-dep. Pt.Filt. with Rand. P",
                "Data-dep. Ptolemaic Filtering",
                "Sequential Brute Force"
        );
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "2026_10_" + pivotCount + "_pivots_30NN_triangle_inequality",
                "2026_10_" + pivotCount + "_pivots_30NN_data-dependent_metric_filtering",
                "2026_10_" + pivotCount + "_pivots_30NN_FourPointBasedFiltering",
                "2026_10_" + pivotCount + "_pivots_30NN_ptolemaios_randomPivots_" + pivotCount + "LB_random_pivots",
                "2026_10_" + pivotCount + "_pivots_30NN_ptolemaios_" + pivotCount + "LB",
                "2026_10_" + pivotCount + "_pivots_30NN_data-dependent_ptolemaic_filtering_randomPivots_" + pivotCount + "LB_random_pivots",
                "2026_10_" + pivotCount + "_pivots_30NN_data-dependent_ptolemaic_filtering_" + pivotCount + "LB",
                "ground_truth"
        );
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return null;
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "benchmark-dev-wikipedia-bge-m3.h5__benchmark-dev-wikipedia-bge-m3.h5__30__benchmark-dev-wikipedia-bge-m3.h5__benchmark-dev-wikipedia-bge-m3.h5__"
        );
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / 1000000;
    }

    @Override
    public String getXAxisLabel() {
        return null;
    }

    @Override
    public AbstractPlotter getPlotter() {
        AbstractPlotter ret = new BoxPlotXCategoriesPlotter();
        ret.setShowLegend(false);
        return ret;
    }

    @Override
    public String getResultName() {
        return "Filterings " + pivotCount + "pivots_Wiki";
    }

    @Override
    public String getFolderForPlots() {
        return FSPlotFolders.Y2025_PTOLEMAIOS_LIMITEDWIKI;
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return "# LBs checked per distance";
    }

    @Override
    protected StandardColours.COLOUR_NAME[] getVoluntaryColoursForTracesOrNull() {
        return new StandardColours.COLOUR_NAME[]{
            StandardColours.COLOUR_NAME.C1_BLUE,
            StandardColours.COLOUR_NAME.C2_RED,
            StandardColours.COLOUR_NAME.C3_GREEN,
            StandardColours.COLOUR_NAME.C4_ORANGE,
            StandardColours.COLOUR_NAME.C6_BROWN,
            StandardColours.COLOUR_NAME.C7_PURPLE,
            StandardColours.COLOUR_NAME.C5_VIOLET,
            StandardColours.COLOUR_NAME.CX_BLACK};
    }

}
