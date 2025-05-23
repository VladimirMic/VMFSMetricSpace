/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.impl.paper.used.p2024VLDBPtolemaiosLimited;

import vm.colour.StandardColours;
import vm.fs.plot.impl.paper.devel.p2024VLDBPtolemaiosLimited.random.PlotPtolemaiosRandomData5Tech40_100Recall;
import vm.search.algorithm.SearchingAlgorithm;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosClips10MPCA5Tech extends PlotPtolemaiosRandomData5Tech40_100Recall {

    public PlotPtolemaiosClips10MPCA5Tech(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "Triangle Ineq.",
                "Data-dep. Metric Filtering",
                "Four Point Prop.",
                "Ptolemaic Filtering",
                "Ptolemaic with Dyn. Pivots",
                "Data-dep. Ptolemaic Filering",
                "Data-dep. Ptolemaic Filering STRAIN_25_50000",
                "Sequential Brute Force"
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
            StandardColours.COLOUR_NAME.CX_BLACK
        };
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "2024_05_128_pivots_30NN_seq_triangle_inequality",
                "2024_05_128_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_05_128_pivots_30NN_seq_FourPointBasedFiltering",
                "2024_08_128_pivots_30NN_seq_ptolemaios_128LB_random_pivots",
                "2024_05_128_pivots_30NN_seq_ptolemaios_128LB",
                "2024_05_128_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_128LB",
                "2024_05_128_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection_128LB_25perc_50000objMem",
                "ground_truth"
        );
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return strings(
                "CLIP_10M_PCA256"
        //                "CLIP_10M_GHP512"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "laion2B-en-clip768v2-n=10M.h5_PCA256__laion2B-en-clip768v2-n=10M.h5_PCA256__30__laion2B-en-clip768v2-n=10M.h5_PCA256__laion2B-en-clip768v2-n=10M.h5_PCA256__"
        //                "laion2B-en-clip768v2-n=10M.h5_GHP_50_512__public-queries-10k-clip768v2.h5_GHP_50_512__30__laion2B-en-clip768v2-n=10M.h5_GHP_50_512__public-queries-10k-clip768v2.h5_GHP_50_512__"
        );
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / (10120191 - SearchingAlgorithm.K_IMPLICIT_FOR_QUERIES);
    }

    @Override
    public String getXAxisLabel() {
        return null;
    }

}
