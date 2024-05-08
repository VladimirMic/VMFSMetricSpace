/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

import vm.plot.AbstractPlotter.COLOUR_NAMES;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosRandomData4Tech10_40Time extends PlotPtolemaiosRandomData5Tech10_40 {

    public PlotPtolemaiosRandomData4Tech10_40Time(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "Triangle Ineq.",
                "Data-dep. Metric Filtering",
                "Four Point Property",
                "Ptolemaic Filtering",
                "Data-dep. Ptolemaic Filering"
        );
    }

    @Override
    protected COLOUR_NAMES[] getVoluntaryColoursForTracesOrNull() {
        return new COLOUR_NAMES[]{COLOUR_NAMES.C1_BLUE, COLOUR_NAMES.C2_RED, COLOUR_NAMES.C3_GREEN, COLOUR_NAMES.C4_ORANGE, COLOUR_NAMES.C5_VIOLET};
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "2024_03_256_pivots_30NN_seq_triangle_inequality",
                "2024_03_256_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_03_256_pivots_30NN_seq_FourPointBasedFiltering",
                "2024_03_256_pivots_30NN_seq_ptolemaios",
                "2024_03_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection"
        );
    }

    }
