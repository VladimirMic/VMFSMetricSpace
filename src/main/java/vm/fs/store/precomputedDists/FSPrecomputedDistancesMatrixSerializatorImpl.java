package vm.fs.store.precomputedDists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import vm.fs.FSGlobal;
import vm.fs.main.precomputeDistances.FSEvalAndStoreObjectsToPivotsDistsMain;
import vm.searchSpace.Dataset;
import vm.searchSpace.distance.AbstractDistanceFunction;
import vm.searchSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixSerializator;
import vm.searchSpace.distance.storedPrecomputedDistances.MainMemoryStoredPrecomputedDistances;

/**
 *
 * @author xmic
 */
public class FSPrecomputedDistancesMatrixSerializatorImpl extends AbstractPrecomputedDistancesMatrixSerializator {

    private static final Logger LOG = Logger.getLogger(FSPrecomputedDistancesMatrixSerializatorImpl.class.getName());

    public MainMemoryStoredPrecomputedDistances loadPrecomPivotsToObjectsDists(File file, Dataset dataset, int maxColumnCount) {
        List<float[]> retList = new ArrayList<>();
        int maxPivots = maxColumnCount > 0 ? maxColumnCount : Integer.MAX_VALUE;
        float[][] dists = dataset == null ? null : new float[dataset.getPrecomputedDatasetSize()][maxPivots];
        try {
            BufferedReader br;
            if (file.getName().toLowerCase().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            }
            try {
                String line = br.readLine();
                String[] columns = line.split(";");
                columnHeaders = new HashMap<>();
                for (int i = 1; i < columns.length; i++) {
                    columnHeaders.put(columns[i], i - 1);
                }
                rowHeaders = new HashMap<>();
                for (int counter = 1; line != null; counter++) {
                    line = br.readLine();
                    String[] split = line.split(";");
                    maxPivots = Math.min(split.length - 1, maxPivots);
                    float[] lineFloats = new float[maxPivots];
                    rowHeaders.put(split[0], counter - 1);
                    for (int i = 0; i < lineFloats.length; i++) {
                        lineFloats[i] = Float.parseFloat(split[i + 1]);
                    }
                    if (dists != null) {
                        dists[counter - 1] = lineFloats;
                    } else {
                        retList.add(lineFloats);
                    }
                    if (counter % 50000 == 0) {
                        LOG.log(Level.INFO, "Parsed precomputed distances between pivots and {0} objects", counter);
                        if (vm.javatools.Tools.getRatioOfConsumedRam(true) >= 0.9) {
                            System.gc();
                        }
                    }
                }
            } catch (NullPointerException ex) {
            }
            if (dists == null) {
                dists = new float[retList.size()][maxPivots];
                for (int i = 0; i < retList.size(); i++) {
                    dists[i] = retList.get(i);
                }
            }
//            if (dataset != null) {
//                checkOrdersOfPivots(dataset.getPivots(maxColumnCount), dataset.getSearchSpace());
//            }
            MainMemoryStoredPrecomputedDistances ret = new MainMemoryStoredPrecomputedDistances(dists, columnHeaders, rowHeaders);
            return ret;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;

    }

    @Override
    public MainMemoryStoredPrecomputedDistances loadPrecomPivotsToObjectsDists(Dataset dataset, String dfModification, int pivotCount) {
        String datasetName = dataset.getDatasetName();
        String pivotSetName = dataset.getPivotSetName();
        File file = deriveFileForDatasetAndPivots(datasetName, dfModification, dataset.getDistanceFunction(), pivotSetName, pivotCount, false);
        if (!file.exists()) {
            LOG.log(Level.WARNING, "No precomputed distances found for dataset {0} pivot set {1} and {2} pivots", new Object[]{datasetName, pivotSetName, pivotCount});
            return null;
        }
        return loadPrecomPivotsToObjectsDists(file, dataset, pivotCount);

    }

    public File deriveFileForDatasetAndPivots(String datasetName, String dfModification, AbstractDistanceFunction nativeDF, String pivotSetName, int pivotCount, boolean willBeDeleted) {
        String suf = "";
        if (dfModification != null && !dfModification.equals("") && !dfModification.equals(nativeDF.getName())) {
            suf = "_" + dfModification;
        }
        return deriveFileForDatasetAndPivots(datasetName + suf, pivotSetName, pivotCount, willBeDeleted);
    }

    public File deriveFileForDatasetAndPivots(String datasetName, String pivotSetName, int pivotCount, boolean willBeDeleted) {
        File ret = new File(FSGlobal.PRECOMPUTED_DISTS_FOLDER, datasetName + "_" + pivotSetName + "_" + pivotCount + "pivots.csv.gz");
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        if (!willBeDeleted && !ret.exists()) {
            FilenameFilter filter = (File file, String string) -> string.contains(datasetName + "_" + pivotSetName);
            File folder = new File(FSGlobal.PRECOMPUTED_DISTS_FOLDER);
            File[] candidates = folder.listFiles(filter);
            int bestCount = Integer.MAX_VALUE;
            for (File candidate : candidates) {
                int pivotCountInFile = parsePivotCountFromFileName(candidate.getName());
                if (pivotCountInFile >= pivotCount && pivotCountInFile < bestCount) {
                    bestCount = pivotCountInFile;
                    ret = candidate;
                }
            }
            if (bestCount == Integer.MAX_VALUE) {
                LOG.log(Level.WARNING, "File with precomputed distances does not exist: {0}", ret.getAbsolutePath());
            } else {
                LOG.log(Level.INFO, "Since the file with precomputed distances to {0} pivots does not exist, returning file with distances to {1} pivots", new Object[]{pivotCount, bestCount});
            }
        }
        return ret;
    }

    private int parsePivotCountFromFileName(String name) {
        try {
            name = name.substring(name.lastIndexOf("_") + 1);
            name = name.substring(0, name.indexOf("pivot"));
            return Integer.parseInt(name);
        } catch (Exception e) {
            return -Integer.MAX_VALUE;
        }
    }

    @Override
    public void serializeColumnsHeaders(Dataset dataset, int pivotCount, String additionalName, Map<Comparable, Integer> columnKeys) throws IOException {
        try (OutputStream outputStream = getGZIPOutputStream(dataset, pivotCount, additionalName, false)) {
            serializeColumnsHeaders(outputStream, columnKeys);
        }
    }

    public void serializeColumnsHeaders(OutputStream outputStream, Map<Comparable, Integer> columnKeys) throws IOException {
        outputStream.write(';');
        for (Comparable pivotID : columnKeys.keySet()) {
            outputStream.write(pivotID.toString().getBytes());
            outputStream.write(';');
        }
        outputStream.write('\n');
        outputStream.flush();
    }

    @Override
    public int serializeRows(Dataset dataset, int pivotCount, String additionalName, Map<Comparable, Integer> rowKeys, Map<Comparable, Integer> columnKeys, float[][] distsInRow, int rowCounter) throws IOException {
        int ret;
        try (OutputStream outputStream = getGZIPOutputStream(dataset, pivotCount, additionalName, true)) {
            ret = serializeRows(outputStream, rowKeys, columnKeys, distsInRow, rowCounter);
        }
        return ret;
    }

    public int serializeRows(OutputStream outputStream, Map<Comparable, Integer> rowKeys, Map<Comparable, Integer> columnKeys, float[][] distsInRow, int rowCounter) throws IOException {
        for (Map.Entry<Comparable, Integer> oID : rowKeys.entrySet()) {
            rowCounter++;
            String oIdString = oID.getKey().toString();
            outputStream.write(oIdString.getBytes());
            outputStream.write(';');
            for (Map.Entry<Comparable, Integer> colunm : columnKeys.entrySet()) {
                Integer pIdx = colunm.getValue();
                float distance = distsInRow[oID.getValue()][pIdx];
                outputStream.write(Float.toString(distance).getBytes());
                outputStream.write(';');
            }
            outputStream.write('\n');
            if (rowCounter % 20000 == 0) {
                LOG.log(Level.INFO, "Stored {0} rows", rowCounter);
            }
        }
        outputStream.flush();
        return rowCounter;
    }

    public int serialize(OutputStream os, Map<Comparable, Integer> rowKeys, Map<Comparable, Integer> columnKeys, float[][] distsInRow, int rowCounter) throws IOException {
        serializeColumnsHeaders(os, columnKeys);
        return serializeRows(os, rowKeys, columnKeys, distsInRow, rowCounter);
    }

    public int serialize(OutputStream os, Map<Comparable, Integer> rowKeys, Map<Comparable, Integer> columnKeys, float[][] distsInRow) throws IOException {
        return serialize(os, rowKeys, columnKeys, distsInRow, 0);
    }

    public static GZIPOutputStream getGZIPOutputStream(Dataset dataset, int pivotCount, boolean append) {
        return getGZIPOutputStream(dataset, pivotCount, null, append);
    }

    public static GZIPOutputStream getGZIPOutputStream(Dataset dataset, int pivotCount, String dfModification, boolean append) {
        FSPrecomputedDistancesMatrixSerializatorImpl loader = new FSPrecomputedDistancesMatrixSerializatorImpl();
        String output = loader.deriveFileForDatasetAndPivots(dataset.getDatasetName(), dfModification, dataset.getDistanceFunction(), dataset.getPivotSetName(), pivotCount, !append).getAbsolutePath();
        GZIPOutputStream outputStream = null;
        try {
            outputStream = new GZIPOutputStream(new FileOutputStream(output, append), true);
        } catch (IOException ex) {
            FSEvalAndStoreObjectsToPivotsDistsMain.LOG.log(Level.SEVERE, null, ex);
        }
        return outputStream;
    }

}
