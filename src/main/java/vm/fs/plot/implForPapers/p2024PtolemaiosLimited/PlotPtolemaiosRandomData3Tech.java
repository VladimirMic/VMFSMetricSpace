/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosRandomData3Tech extends PlotPtolemaiosRandomData5Tech {

    public PlotPtolemaiosRandomData3Tech(boolean plotOnlySvg) {
        super(plotOnlySvg);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMeansFolders() {
        return array(
                "Tr. Ineq.",
                "Data-dep. Metric Filtering",
                "Data-dep. Gen. Ptolemaic Filt."
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFolderNameForDisplaydTrace() {
        return array(
                "2024_03_256_pivots_30NN_seq_triangle_inequality",
                "2024_03_256_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_03_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection"
        );
    }

}
