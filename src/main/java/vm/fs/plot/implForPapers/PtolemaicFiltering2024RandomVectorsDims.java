/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers;

import java.io.FilenameFilter;
import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotCategoricalPlotter;

/**
 *
 * @author au734419
 */
public class PtolemaicFiltering2024RandomVectorsDims extends FSAbstractPlotterFromResults {

    @Override
    public FilenameFilter getFilenameFilterFolders() {
        String[] array = array(
                "2024_03_256_pivots_30NN_seq_triangle_inequality",
                "2024_03_256_pivots_30NN_seq_data-dependent_metric_filtering",
                "2024_03_256_pivots_30NN_seq_data-dependent_generalised_ptolemaic_filtering_pivot_array_selection",
                "2024_03_256_pivots_30NN_seq_ptolemaios",
                "2024_03_256_pivots_30NN_seq_FourPointBasedFiltering"
        );
        return getFileNameFilterOR(false, array);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMeansFolders() {
        return array(
                "Triangle Inequality",
                "Data-dependent Metric Filtering",
                "Four Point Property",
                "Ptolemaic Filtering",
                "Data-dependent Generalised Ptolemaic Filtering"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFolderNameForDisplaydTrace() {
        return array(
                "triangle_inequality",
                "ata-dependent_metric_filtering",
                "ourPointBasedFiltering",
                "ptolemaios",
                "ndent_generalised_ptolemaic_filtering_pivot_array_selection"
        );
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return array(
                "10",
                "15",
                "20",
                "25",
                "30",
                "35",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        String[] dims = getDisplayedNamesOfGroupsThatMeansFiles();
        String[] ret = new String[dims.length];
        for (int i = 0; i < dims.length; i++) {
            ret[i] = dims[i] + "dim";
        }
        return ret;
    }

    @Override
    public String getXAxisLabel() {
        return "Dimensionality";
    }

    @Override
    public String getUnitForTime() {
        return "s";
    }

    @Override
    public AbstractPlotter getPlotter() {
        return new BoxPlotCategoricalPlotter();
    }

}
