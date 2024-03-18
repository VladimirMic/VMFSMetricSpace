/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.JFreeChart;
import vm.datatools.DataTypeConvertor;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl.QUERY_STATS;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotPlotter;
import vm.plot.impl.BoxPlotXYPlotter;

/**
 *
 * @author au734419
 */
public abstract class FSAbstractPlotterFromResults {

    private static final Logger LOG = Logger.getLogger(FSAbstractPlotterFromResults.class.getName());

    private final boolean plotOnlySvg;
    private AbstractPlotter plotter = getPlotter();
    private final Object[] xTicks = getDisplayedNamesOfGroupsThatMeansFiles();
    private final AbstractPlotter.COLOUR_NAMES[] colourIndexesForTraces = getColourIndexesForTraces();
    private final String[] artifactForTraces = getUniqueArtifactIdentifyingFolderNameForDisplaydTrace();

    public FSAbstractPlotterFromResults(boolean plotOnlySvg) {
        this.plotOnlySvg = plotOnlySvg;
        if (plotter instanceof BoxPlotPlotter && Tools.isParseableToFloats(xTicks)) {
            plotter = new BoxPlotXYPlotter();
        }
        if (colourIndexesForTraces != null && colourIndexesForTraces.length != artifactForTraces.length) {
            throw new IllegalArgumentException("Incosistent specification of traces colours and traces. The counts do not match. Colours: " + colourIndexesForTraces.length + ", traces: " + xTicks.length);
        }
    }

    public abstract String[] getDisplayedNamesOfTracesThatMeansFolders();

    public abstract String[] getUniqueArtifactIdentifyingFolderNameForDisplaydTrace();

    public abstract Object[] getDisplayedNamesOfGroupsThatMeansFiles();

    public abstract String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup();

    public abstract String getXAxisLabel();

    public abstract AbstractPlotter getPlotter();

    public abstract String getResultName();

    public abstract String getFolderForPlots();

    protected abstract String getYAxisNameForAdditionalParams();

    protected abstract Float transformAdditionalStatsForQueryToFloat(float firstValue);

    protected abstract AbstractPlotter.COLOUR_NAMES[] getColourIndexesForTraces();

    public FilenameFilter getFilenameFilterStatsFiles() {
        String[] array = getUniqueArtifactIdentifyingFileNameForDisplaydGroup();
        return getFileNameFilterOR(true, array);
    }

    public FilenameFilter getFilenameFilterFolders() {
        return getFileNameFilterOR(false, artifactForTraces);
    }

    private String getResultFullNameWithDate(QUERY_STATS statName) {
        int datasetsCount = xTicks.length;
        int techCount = artifactForTraces.length;
        String plotName = plotter.getSimpleName();
        String className = getClass().getCanonicalName();
        className = className.substring(className.lastIndexOf(".") + 1);

        String fileName = Tools.getDateYYYYMM() + "_" + getResultName() + "_" + className;
        fileName += "_" + datasetsCount + "data_" + techCount + "techs_" + plotName + "_" + statName;
        File file = new File(getFolderForPlots(), fileName);
        FSGlobal.checkFileExistence(file, false);
        return file.getAbsolutePath();
    }

    private QUERY_STATS[] getStatsToPrint() {
        return new QUERY_STATS[]{QUERY_STATS.recall, QUERY_STATS.cand_set_dynamic_size, QUERY_STATS.query_execution_time, QUERY_STATS.error_on_dist, QUERY_STATS.additional_stats};
    }

    private List<File> getFilesWithResultsToBePlotted(int groupsCount, int boxplotsCount) {
        File resultsRoot = new File(FSGlobal.RESULT_FOLDER);
        File[] folders = resultsRoot.listFiles(getFilenameFilterFolders());
        if (folders.length != boxplotsCount) {
            throw new IllegalArgumentException("You have wrong filename filter as number of result folders " + folders.length + "differs from the number of name artifacts " + boxplotsCount);
        } else {
            LOG.log(Level.INFO, "There are {0} folders matching the rule", folders.length);
        }

        String[] uniqueArtifactsForFiles = getUniqueArtifactIdentifyingFileNameForDisplaydGroup();
        folders = reorder(folders, artifactForTraces);
        FilenameFilter filenameFilterFiles = getFilenameFilterStatsFiles();
        List<File> ret = new ArrayList<>();
        for (File folder : folders) {
            File folderWithStats = new File(folder, FSGlobal.RESULT_STATS_FOLDER);
            File[] files = folderWithStats.listFiles(filenameFilterFiles);
            if (files.length != groupsCount) {
                throw new IllegalArgumentException("You have wrong filename filter as number of files " + files.length + " in folder " + folderWithStats.getAbsolutePath() + "is smaller than the number of name artifacts " + groupsCount);
            }
            if (files.length != 0) {
                files = reorder(files, uniqueArtifactsForFiles);
                LOG.log(Level.INFO, "Folder {0} contains {1} matching files", new Object[]{folder.getName(), files.length});
                List list = Tools.arrayToList(files);
                ret.addAll(list);
            }
        }
        LOG.log(Level.INFO, "The final plot will have {0} values in {1} traces which means {2} values per trace on average", new Object[]{ret.size(), folders.length, (ret.size() / folders.length)});
        return ret;
    }

    private Map<QUERY_STATS, List<Float>[][]> loadStatsFromFileAsListOfXYValues(List<File> files, int groupsCount, int boxplotsCount) {
        QUERY_STATS[] statsToPrint = getStatsToPrint();

        Map<QUERY_STATS, List<Float>[][]> ret = initRet(groupsCount, boxplotsCount, statsToPrint);
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            int groupIdx = (int) (i % groupsCount);
            int traceIdx = (int) (i / groupsCount);

            System.out.println("XXX: groupIdx" + groupIdx + " | traceIdx " + traceIdx + " | " + file.getName());

            FSQueryExecutionStatsStoreImpl storage = new FSRecallOfCandidateSetsStorageImpl(file);
            Map<String, TreeMap<QUERY_STATS, String>> results = storage.getContent();
            for (QUERY_STATS stat : statsToPrint) {
                List<Float>[][] listOfValues = ret.get(stat);
                update(listOfValues[traceIdx][groupIdx], results, stat);
                if (listOfValues[traceIdx][groupIdx].isEmpty()) {
                    listOfValues[traceIdx][groupIdx] = null;
                }
            }
        }
        return ret;
    }

    public void makePlots() {
        int groupsCount = xTicks.length;
        int boxplotsCount = getDisplayedNamesOfTracesThatMeansFolders().length;
        List<File> files = getFilesWithResultsToBePlotted(groupsCount, boxplotsCount);
        Map<QUERY_STATS, List<Float>[][]> dataForStats = loadStatsFromFileAsListOfXYValues(files, groupsCount, boxplotsCount);
        Set<QUERY_STATS> keyForPlots = dataForStats.keySet();
        Map<QUERY_STATS, String> yLabels = queryStatsToYAxisLabels(dataForStats.get(QUERY_STATS.query_execution_time));
        for (QUERY_STATS key : keyForPlots) {
            makePlotsForQueryStats(key, dataForStats, plotter, yLabels.get(key));
        }
    }

    private void makePlotsForQueryStats(QUERY_STATS key, Map<QUERY_STATS, List<Float>[][]> dataForStats, AbstractPlotter plotter, String yAxisLabel) {
        List<Float>[][] values = dataForStats.get(key);
        if (isEmpty(values)) {
            return;
        }
        String path = getResultFullNameWithDate(key);
        LOG.log(Level.INFO, "Path for future plot: {0}", path);
        String xAxisLabel = getXAxisLabel();
        JFreeChart plot = plotter.createPlot("", xAxisLabel, yAxisLabel, getDisplayedNamesOfTracesThatMeansFolders(), colourIndexesForTraces, xTicks, values);
        plotter.storePlotSVG(path, plot);
        if (!plotOnlySvg) {
            plotter.storePlotPNG(path, plot);
        }
    }

    public FilenameFilter getFileNameFilterOR(boolean checkSuffixCSV, String... substrings) {
        return (File dir, String name) -> {
            for (String substring : substrings) {
                if (name.contains(substring)) {
                    if (checkSuffixCSV) {
                        return name.endsWith(".csv");
                    } else {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    public FilenameFilter getFileNameFilterAND(boolean checkSuffixCSV, String... substrings) {
        return (File dir, String name) -> {
            for (String substring : substrings) {
                if (!name.contains(substring)) {
                    return false;
                }
            }
            if (checkSuffixCSV) {
                return name.endsWith(".csv");
            }
            return true;
        };
    }

    protected String[] strings(String... strings) {
        return strings;
    }

    protected Object[] array(Object... objects) {
        return objects;
    }

    private String unitForTime;

    private Map<QUERY_STATS, String> queryStatsToYAxisLabels(List<Float>[][] timeValues) {
        Map<QUERY_STATS, String> ret = new HashMap<>();
        ret.put(QUERY_STATS.cand_set_dynamic_size, "CandSet(q) size");
        ret.put(QUERY_STATS.error_on_dist, "Error on Dist");
        unitForTime = setUnitForTime(timeValues);
        if (unitForTime == null) {
            unitForTime = "";
        } else {
            unitForTime = " (" + unitForTime + ")";
        }
        ret.put(QUERY_STATS.query_execution_time, "Time" + unitForTime);
        ret.put(QUERY_STATS.recall, "Recall");
        String yAxisNameForAdditionalParams = getYAxisNameForAdditionalParams();
        if (yAxisNameForAdditionalParams != null) {
            ret.put(QUERY_STATS.additional_stats, yAxisNameForAdditionalParams);
        }
        return ret;
    }

    private File[] reorder(File[] files, String[] uniqueArtifactsForFiles) {
        File[] ret = new File[files.length];
        for (File file : files) {
            int idx = getIndex(uniqueArtifactsForFiles, file.getName().toLowerCase());
            if (idx != -1) {
                if (idx > ret.length) {
                    throw new IllegalArgumentException("You have wrong file/folder name filter as number of files/folders " + ret.length + " is smaller than the number of name artifacts " + uniqueArtifactsForFiles.length);
                } else {
                    ret[idx] = file;
                }
            }
        }
        for (File file : ret) {
            if (file == null) {
                throw new IllegalArgumentException("You have wrong file/folder name filter as number of files/folders " + ret.length + " is bigger than the number of name artifacts " + uniqueArtifactsForFiles.length);
            }
        }
        return ret;
    }

    private int getIndex(String[] uniqueArtifactsForFiles, String name) {
        int ret = -1;
        for (int i = 0; i < uniqueArtifactsForFiles.length; i++) {
            String artifact = uniqueArtifactsForFiles[i];
            if (name.contains(artifact.toLowerCase())) {
                if (ret == -1) {
                    ret = i;
                } else {
                    throw new IllegalArgumentException("Unique artifacts of the file/folder names are not unique: " + name + " matches both, " + uniqueArtifactsForFiles[ret] + " and " + uniqueArtifactsForFiles[i]);
                }
            }
        }
        return ret;
    }

    private void update(List<Float> list, Map<String, TreeMap<QUERY_STATS, String>> stats, QUERY_STATS stat) {
        for (Map.Entry<String, TreeMap<QUERY_STATS, String>> statsForQueryEntry : stats.entrySet()) {
            TreeMap<QUERY_STATS, String> statsForQuery = statsForQueryEntry.getValue();
            String valueString = statsForQuery.get(stat);
            if (valueString != null) {
                Float fValue = Tools.parseFloat(valueString);
                if (fValue != null) {
                    list.add(fValue);
                } else {
                    String[] split = valueString.split(",");
                    fValue = Tools.parseFloat(split[0]);
                    if (split.length > 0 && fValue != null) {
                        fValue = transformAdditionalStatsForQueryToFloat(fValue);
                        if (fValue != null) {
                            list.add(fValue);
                        }
                    }
                }
            }
        }
    }

    private Map<QUERY_STATS, List<Float>[][]> initRet(int groupsCount, int boxplotsCount, QUERY_STATS[] statsToPrint) {
        Map<QUERY_STATS, List<Float>[][]> ret = new HashMap<>();
        for (QUERY_STATS stat : statsToPrint) {
            List[][] lists = new List[boxplotsCount][groupsCount];
            for (int g = 0; g < groupsCount; g++) {
                for (int t = 0; t < boxplotsCount; t++) {
                    lists[t][g] = new ArrayList();
                }
            }
            ret.put(stat, lists);
        }
        return ret;
    }

    private String setUnitForTime(List<Float>[][] timeValues) {
        List<Float> all = new ArrayList<>();
        for (List<Float>[] timeValue : timeValues) {
            for (List<Float> list : timeValue) {
                all.addAll(list);
            }
        }
        double max = vm.math.Tools.getMax(DataTypeConvertor.floatToPrimitiveArray(all));
        if (max >= 1300) {
            for (List<Float>[] timeValue : timeValues) {
                for (List<Float> list : timeValue) {
                    List<Float> listNew = new ArrayList<>();
                    for (int k = 0; k < list.size(); k++) {
                        Float value = list.get(k);
                        listNew.add(value / 1000);
                    }
                    list.clear();
                    list.addAll(listNew);
                }
            }

            return "s";
        }
        return "ms";
    }

    private boolean isEmpty(List<Float>[][] values) {
        for (List<Float>[] value : values) {
            for (List<Float> list : value) {
                if (list != null && !list.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
