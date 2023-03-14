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
import vm.metricSpace.distance.storedPrecomputedDistances.AbstractPrecomputedDistancesMatrixLoader;

/**
 *
 * @author xmic
 */
public class FSPrecomputedDistancesMatrixLoaderImpl extends AbstractPrecomputedDistancesMatrixLoader {

    private static final Logger LOG = Logger.getLogger(FSPrecomputedDistancesMatrixLoaderImpl.class.getName());

    @Override
    public float[][] loadPrecomPivotsToObjectsDists(String datasetName, String pivotSetName, int pivotCount) {
        List<float[]> retList = new ArrayList<>();
        File file = deriveFileForDatasetAndPivots(datasetName, pivotSetName, pivotCount);
        if (!file.exists()) {
            throw new IllegalArgumentException("File with the precomputed distances does no exists for datasetName " + datasetName + ", pivotSetName " + pivotSetName + ", pivotCount " + pivotCount);
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
            return ret;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public final static File deriveFileForDatasetAndPivots(String datasetName, String pivotSetName, int pivotCount) {
        File f = new File(FSGlobal.PRECOMPUTED_DISTS_FOLDER);
        f.mkdirs();
        File ret = new File(f, datasetName + "_" + pivotSetName + "_" + pivotCount + "pivots.csv.gz");
        LOG.log(Level.INFO, "File for precumputed distances: " + ret.getAbsolutePath());
        return ret;
    }

}
