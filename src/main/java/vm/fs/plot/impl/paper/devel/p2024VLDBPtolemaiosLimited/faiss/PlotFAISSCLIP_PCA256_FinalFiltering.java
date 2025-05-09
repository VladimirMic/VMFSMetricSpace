/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.impl.paper.devel.p2024VLDBPtolemaiosLimited.faiss;

import vm.colour.StandardColours;
import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.fs.plot.FSPlotFolders;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotXCategoriesPlotter;
import vm.search.algorithm.SearchingAlgorithm;

/**
 *
 * @author Vlada
 */
public class PlotFAISSCLIP_PCA256_FinalFiltering extends FSAbstractPlotterFromResults {

    public PlotFAISSCLIP_PCA256_FinalFiltering(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return strings(
                "CLIP_PCA256"
        );
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "Triangle Ineq.",
                "Data-dep. Metric Filtering",
                "Four Point Prop.",
                "Ptolemaic Filtering",
                "Ptolemaic with Dyn. Pivots",
                "Data-dep. Ptolemaic Filtering",
                "Data-dep. Ptolemaic Filtering STRAIN_60_150",
                "Sequential Brute Force"
        );
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "2024_08_64_pivots_30NN_seq_triangle_inequality",
                "2024_08_64_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_08_64_pivots_30NN_seq_FourPointBasedFiltering",
                "2024_08_64_pivots_30NN_seq_ptolemaios_64LB_random_pivots",
                "2024_08_64_pivots_30NN_seq_ptolemaios_64LB",
                "2024_08_64_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_64LB",
                "2024_08_64_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_64LB_60perc_150objMem",
                "ground_truth"
        );
    }

    @Override
    protected StandardColours.COLOUR_NAME[] getVoluntaryColoursForTracesOrNull() {
        return new StandardColours.COLOUR_NAME[]{
            StandardColours.COLOUR_NAME.C1_BLUE,
            StandardColours.COLOUR_NAME.C2_RED,
            StandardColours.COLOUR_NAME.C3_GREEN,
            StandardColours.COLOUR_NAME.C4_ORANGE,
            StandardColours.COLOUR_NAME.C6_BROWN,
            StandardColours.COLOUR_NAME.C5_VIOLET,
            StandardColours.COLOUR_NAME.C7_PURPLE,
            StandardColours.COLOUR_NAME.CX_BLACK};
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "Faiss_Clip_100M_PCA256_Candidates__laion2B-en-clip768v2-n=100M.h5_PCA256__30__Faiss_Clip_100M_PCA256_Candidates__laion2B-en-clip768v2-n=100M.h5_PCA256__");
    }

    @Override
    public String getXAxisLabel() {
        return "";
    }

    @Override
    public AbstractPlotter getPlotter() {
        return new BoxPlotXCategoriesPlotter();
    }

    @Override
    public String getResultName() {
        return "Filterings_Clip";
    }

    @Override
    public String getFolderForPlots() {
        return FSPlotFolders.Y2024_PTOLEMAIOS_LIMITED_FILTERING;
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return "# LBs checked per distance";
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / (750 - SearchingAlgorithm.K_IMPLICIT_FOR_QUERIES);
    }

}
