package vm.fs.store.precomputedDists;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.FSGlobal;
import vm.metricSpace.distance.storedPrecomputedDistances.PrecomputedPairsOfDistancesStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSPrecomputedDistsStorageImpl implements PrecomputedPairsOfDistancesStoreInterface {

    public static final Logger LOG = Logger.getLogger(FSPrecomputedDistsStorageImpl.class.getName());

    private final String resultsName;
    private final int oSize;
    private final int qSize;

    public FSPrecomputedDistsStorageImpl(String datasetName, int oSize, int qSize) {
        this.resultsName = datasetName;
        this.oSize = oSize;
        this.qSize = qSize;
    }

    @Override
    public void storePrecomputedDistances(TreeSet<Map.Entry<String, Float>> dists) {
        OutputStreamWriter os = null;
        try {
            os = new OutputStreamWriter(new FileOutputStream(getFileForResults(), false));
            Iterator<Map.Entry<String, Float>> it = dists.iterator();
            while (it.hasNext()) {
                Map.Entry<String, Float> next = it.next();
                String key = next.getKey();
                os.write(key + ";");
                os.write(next.getValue() + "\n");
            }
            System.out.flush();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                os.flush();
                os.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getFileForResults() {
        String fileName = resultsName + "__sample_" + oSize + "__ queries_" + qSize + ".csv";
        File f = new File(FSGlobal.SMALLEST_DISTANCES, fileName);
        FSGlobal.askForAFileExistence(f);
        return f.getAbsolutePath();
    }

    @Override
    public TreeSet<Map.Entry<String, Float>> loadPrecomputedDistances() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
