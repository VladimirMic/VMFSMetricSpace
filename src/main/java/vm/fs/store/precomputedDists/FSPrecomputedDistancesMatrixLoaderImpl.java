package vm.fs.store.precomputedDists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import vm.fs.FSGlobal;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixLoader;

/**
 *
 * @author xmic
 */
public class FSPrecomputedDistancesMatrixLoaderImpl extends AbstractPrecomputedDistancesMatrixLoader {

    private static final Logger LOG = Logger.getLogger(FSPrecomputedDistancesMatrixLoaderImpl.class.getName());

    @Override
    public float[][] loadPrecomPivotsToObjectsDists(Dataset dataset, int pivotCount) {
        List<float[]> retList = new ArrayList<>();
        String datasetName = dataset.getDatasetName();
        String pivotSetName = dataset.getPivotSetName();
        File file = deriveFileForDatasetAndPivots(datasetName, pivotSetName, pivotCount, false);
        if (!file.exists()) {
            LOG.log(Level.WARNING, "No precomputed distances found for dataset {0} pivot set {1} and {2} pivots", new Object[]{datasetName, pivotSetName, pivotCount});
            return null;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            int maxPivots = pivotCount > 0 ? pivotCount : Integer.MAX_VALUE;
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
                    retList.add(lineFloats);
                    if (counter % 50000 == 0) {
                        LOG.log(Level.INFO, "Parsed precomputed distances between pivots and {0} objects", counter);
                    }
                }
            } catch (NullPointerException ex) {
            }
            float[][] ret = new float[retList.size()][maxPivots];
            for (int i = 0; i < retList.size(); i++) {
                ret[i] = retList.get(i);
            }
        checkOrdersOfPivots(dataset.getPivots(pivotCount), dataset.getMetricSpace());
        return ret;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public File deriveFileForDatasetAndPivots(String datasetName, String pivotSetName, int pivotCount, boolean willBeDeleted) {
        File ret = new File(FSGlobal.PRECOMPUTED_DISTS_FOLDER, datasetName + "_" + pivotSetName + "_" + pivotCount + "pivots.csv.gz");
        FSGlobal.checkFileExistence(ret, willBeDeleted);
        if (!willBeDeleted && !ret.exists()) {
            LOG.log(Level.WARNING, "File with precomputed distances does not exist: {0}", ret.getAbsolutePath());
        }
        return ret;
    }

}
