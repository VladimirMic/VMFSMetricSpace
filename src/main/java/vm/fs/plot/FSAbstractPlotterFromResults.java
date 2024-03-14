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
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl.QUERY_STATS;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.plot.AbstractPlotter;

/**
 *
 * @author au734419
 */
public abstract class FSAbstractPlotterFromResults {

    private final boolean plotOnlySvg;

    public FSAbstractPlotterFromResults(boolean plotOnlySvg) {
        this.plotOnlySvg = plotOnlySvg;
    }

    private static final Logger LOG = Logger.getLogger(FSAbstractPlotterFromResults.class.getName());

    public abstract String[] getDisplayedNamesOfTracesThatMeansFolders();

    public abstract String[] getUniqueArtifactIdentifyingFolderNameForDisplaydTrace();

    public abstract String[] getDisplayedNamesOfGroupsThatMeansFiles();

    public abstract String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup();

    public abstract String getXAxisLabel();

    public abstract String getUnitForTime();

    public abstract AbstractPlotter getPlotter();

    public abstract String getResultName();

    public abstract String getFolderForPlots();

    public FilenameFilter getFilenameFilterStatsFiles() {
        String[] array = getUniqueArtifactIdentifyingFileNameForDisplaydGroup();
        return getFileNameFilterOR(true, array);
    }

    public FilenameFilter getFilenameFilterFolders() {
        String[] array = getUniqueArtifactIdentifyingFolderNameForDisplaydTrace();
        return getFileNameFilterOR(false, array);
    }

    private String getResultFullNameWithDate(QUERY_STATS statName) {
        int datasetsCount = getDisplayedNamesOfGroupsThatMeansFiles().length;
        int techCount = getUniqueArtifactIdentifyingFolderNameForDisplaydTrace().length;
        String plotName = getPlotter().getSimpleName();
        String className = getClass().getCanonicalName();
        className = className.substring(className.lastIndexOf(".") + 1);

        String fileName = Tools.getDateYYYYMM() + "_" + getResultName() + "_" + className;
        fileName += "_" + datasetsCount + "data_" + techCount + "techs_" + plotName + "_" + statName;
        File file = new File(getFolderForPlots(), fileName);
        FSGlobal.checkFileExistence(file, false);
        return file.getAbsolutePath();
    }

    private QUERY_STATS[] getStatsToPrint() {
        return new QUERY_STATS[]{QUERY_STATS.recall, QUERY_STATS.cand_set_dynamic_size, QUERY_STATS.query_execution_time};
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
        folders = reorder(folders, getUniqueArtifactIdentifyingFolderNameForDisplaydTrace());
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
            }
        }
        return ret;
    }

    public void makePlots() {
        int groupsCount = getDisplayedNamesOfGroupsThatMeansFiles().length;
        int boxplotsCount = getDisplayedNamesOfTracesThatMeansFolders().length;
        List<File> files = getFilesWithResultsToBePlotted(groupsCount, boxplotsCount);
        Map<QUERY_STATS, List<Float>[][]> dataForStats = loadStatsFromFileAsListOfXYValues(files, groupsCount, boxplotsCount);
        AbstractPlotter plotter = getPlotter();
        Set<QUERY_STATS> keyForPlots = dataForStats.keySet();
        Map<QUERY_STATS, String> yLabels = queryStatsToYAxisLabels();
        for (QUERY_STATS key : keyForPlots) {
            makePlotsForQueryStats(key, dataForStats, plotter, yLabels.get(key));
        }
    }

    private void makePlotsForQueryStats(QUERY_STATS key, Map<QUERY_STATS, List<Float>[][]> dataForStats, AbstractPlotter plotter, String yAxisLabel) {
        List<Float>[][] values = dataForStats.get(key);
        String path = getResultFullNameWithDate(key);
        LOG.log(Level.INFO, "Path for future plot: {0}", path);
        JFreeChart plot = plotter.createPlot("", yAxisLabel, getDisplayedNamesOfTracesThatMeansFolders(), getDisplayedNamesOfGroupsThatMeansFiles(), values);
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

    protected String[] array(String... strings) {
        return strings;
    }

    private Map<QUERY_STATS, String> queryStatsToYAxisLabels() {
        Map<QUERY_STATS, String> ret = new HashMap<>();
        ret.put(QUERY_STATS.cand_set_dynamic_size, "CandSet(q) size");
        ret.put(QUERY_STATS.error_on_dist, "Error on Dist");
        String unitForTime = getUnitForTime();
        if (unitForTime == null) {
            unitForTime = "";
        } else {
            unitForTime = " (" + unitForTime + ")";
        }
        ret.put(QUERY_STATS.query_execution_time, "Time" + unitForTime);
        ret.put(QUERY_STATS.recall, "Recall");
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
        String unitForTime = getUnitForTime();
        if (unitForTime != null) {
            unitForTime = unitForTime.trim().toLowerCase();
        }
        for (Map.Entry<String, TreeMap<QUERY_STATS, String>> statsForQueryEntry : stats.entrySet()) {
            TreeMap<QUERY_STATS, String> statsForQuery = statsForQueryEntry.getValue();
            String valueString = statsForQuery.get(stat);
            if (valueString != null) {
                Float valueOf = Float.valueOf(valueString);
                if (stat == QUERY_STATS.query_execution_time && unitForTime != null && unitForTime.equals("s")) {
                    valueOf /= 1000;
                }
                list.add(valueOf);
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

}
