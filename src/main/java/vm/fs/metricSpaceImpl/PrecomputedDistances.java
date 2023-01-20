package vm.fs.metricSpaceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import vm.datatools.Tools;
import vm.fs.FSGlobal;

/**
 *
 * @author xmic
 */
public class PrecomputedDistances {
    
    private static final Logger LOG = Logger.getLogger(PrecomputedDistances.class.getName());

    public float[][] loadPrecomPivotsToObjectsDists(String datasetName, String pivotSetName, int pivotCount, List<String> columnHeaders, List<String> rowHeaders) {
        try {
            List<float[]> retList = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(deriveFileForDatasetAndPivots(datasetName, pivotSetName, pivotCount)))));
            int maxPivots = pivotCount > 0 ? pivotCount : Integer.MAX_VALUE;
            try {
                String line = br.readLine();
                String[] columns = line.split(";");
                columnHeaders = Tools.arrayToList(columns).subList(1, columns.length - 1);
                rowHeaders = new ArrayList<>();
                for (int counter = 1; line != null; counter++) {
                    line = br.readLine();
                    String[] split = line.split(";");
                    maxPivots = Math.min(split.length - 1, maxPivots);
                    float[] lineFloats = new float[maxPivots];
                    rowHeaders.add(counter - 1, split[0]);
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
    
    private File deriveFileForDatasetAndPivots(String datasetName, String pivotSetName, int pivotCount){
        File f = new File(FSGlobal.PRECOMPUTED_DISTS_FOLDER);
        f.mkdirs();
        return new File(f, datasetName + "_" + pivotSetName + "_" + pivotCount + "pivots.csv.gz");        
    }

}
