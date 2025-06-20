package vm.fs.plot.impl.paper.submittedcodes.vldb2024;

import java.io.File;
import vm.colour.StandardColours.COLOUR_NAME;
import vm.fs.FSGlobal;
import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.fs.plot.FSPlotFolders;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl.QUERY_STATS;
import vm.searchSpace.Dataset;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotXCategoriesPlotter;

/**
 *
 * @author Vlada
 */
public class ICDEPlotter extends FSAbstractPlotterFromResults {

    private final int k;
    private final int datasetSize;
    private final String[] folderNamesForDisplayedTraces;
    private final String datasetName;
    private final String querysetName;

    public ICDEPlotter(int k, Dataset dataset, String[] folderNamesForDisplayedTraces) {
        super(true, null, folderNamesForDisplayedTraces);
        this.k = k;
        this.folderNamesForDisplayedTraces = folderNamesForDisplayedTraces;
        this.datasetName = dataset.getDatasetName();
        this.querysetName = dataset.getQuerySetName();
        this.datasetSize = dataset.getPrecomputedDatasetSize();
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
            COLOUR_NAME.C5_VIOLET,
            COLOUR_NAME.CX_BLACK
        };
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return folderNamesForDisplayedTraces;
    }

    @Override
    public Object[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return array(datasetName);
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        String ret = datasetName + "__" + querysetName + "__" + k + "__" + datasetName + "__" + querysetName;
        return strings(ret);
    }

    @Override
    public String getXAxisLabel() {
        return "Dimensionality";
    }

    @Override
    public AbstractPlotter getPlotter() {
        return new BoxPlotXCategoriesPlotter();
    }

    @Override
    public String getResultName() {
        return "";
    }

    @Override
    public String getFolderForPlots() {
        return FSPlotFolders.Y2024_VLDB_PTOLEMAIOS_LIMITED;
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / (datasetSize - k);
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return "# LBs checked per distance";
    }

    @Override
    protected FSQueryExecutionStatsStoreImpl.QUERY_STATS[] getStatsToPrint() {
        return new FSQueryExecutionStatsStoreImpl.QUERY_STATS[]{
            FSQueryExecutionStatsStoreImpl.QUERY_STATS.recall,
            FSQueryExecutionStatsStoreImpl.QUERY_STATS.cand_set_dynamic_size,
            FSQueryExecutionStatsStoreImpl.QUERY_STATS.error_on_dist,
            FSQueryExecutionStatsStoreImpl.QUERY_STATS.additional_stats
        };
    }

    @Override
    protected String getResultFullNameWithDate(QUERY_STATS statName) {
        String fileName = datasetName + "_" + querysetName + "_" + k + "NN";
        fileName += "_" + statName;
        File file = new File(getFolderForPlots(), fileName);
        FSGlobal.checkFileExistence(file, false);
        return file.getAbsolutePath();
    }
}
