/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.fs.plot.FSPlotFolders;
import vm.plot.AbstractPlotter;
import vm.plot.AbstractPlotter.COLOUR_NAMES;
import vm.plot.impl.BoxPlotPlotter;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosRandomData5Tech10_40 extends FSAbstractPlotterFromResults {

    public PlotPtolemaiosRandomData5Tech10_40(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMeansFolders() {
        return strings(
                "Triangle Ineq.",
                "Data-dep. Metric Filtering",
//                "Four Point Property",
                "Ptolemaic Filtering",
                "Data-dep. Ptolemaic Filering"
        );
    }

    @Override
    protected COLOUR_NAMES[] getColourIndexesForTraces() {
        return new COLOUR_NAMES[]{
            COLOUR_NAMES.C1_BLUE,
            COLOUR_NAMES.C2_RED,
            COLOUR_NAMES.C4_ORANGE,
            COLOUR_NAMES.C5_VIOLET
        };
    }

    @Override
    public String[] getFolderNamesForDisplaydTrace() {
        return strings(
                "2024_03_256_pivots_30NN_seq_triangle_inequality",
                "2024_03_256_pivots_30NN_seq_data-dependent_metric_filtering",
//                "2024_03_256_pivots_30NN_seq_FourPointBasedFiltering",
                "2024_03_256_pivots_30NN_seq_ptolemaios",
                "2024_03_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection"
        );
    }

    @Override
    public Object[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return array(
                10,
                15,
                20,
                25,
                30,
                35,
                40
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        Object[] dims = getDisplayedNamesOfGroupsThatMeansFiles();
        String[] ret = new String[dims.length];
        for (int i = 0; i < dims.length; i++) {
            ret[i] = dims[i].toString() + "dim";
        }
        return ret;
    }

    @Override
    public String getXAxisLabel() {
        return "Dimensionality";
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
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / 1000000;
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return "# LBs checked per distance";
    }

}
