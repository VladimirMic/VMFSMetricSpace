package vm.fs.store.filtering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.simRel.impl.learn.storeLearnt.SimRelEuclidThresholdsTOmegaStorage;

/**
 *
 * @author Vlada
 */
public class FSSimRelThresholdsTOmegaStorage implements SimRelEuclidThresholdsTOmegaStorage {

    public static final Logger LOG = Logger.getLogger(FSSimRelThresholdsTOmegaStorage.class.getName());

    @Override
    public void store(float[][] thresholds, String datasetName, int querySampleCount, int dataSampleCount, int pcaLength, int kPCA) {
        File f = getFile(datasetName, querySampleCount, dataSampleCount, pcaLength, kPCA, true);
        OutputStreamWriter w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(f, false));
            for (int i = 0; i < thresholds[0].length; i++) {
                w.write(thresholds[0][i] + ";" + thresholds[1][i] + "\n");
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                w.flush();
                w.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public float[][] load(String datasetName, int querySampleCount, int dataSampleCount, int pcaLength, int kPCA) {
        File file = getFile(datasetName, querySampleCount, dataSampleCount, pcaLength, kPCA, false);
        List<String>[] values = Tools.parseCsv(file.getAbsolutePath(), 1, ";", true);
        float[][] ret = new float[2][values[0].size()];
        for (int i = 0; i < ret.length; i++) {
            ret[0][i] = Float.parseFloat(values[0].get(i));
            ret[1][i] = Float.parseFloat(values[1].get(i));
        }
        return ret;
    }

    private File getFile(String datasetName, int querySampleCount, int dataSampleCount, int pcaLength, int kPCA, boolean willBeDeleted) {
        File ret = new File(FSGlobal.SIMREL_TOMEGA_THRESHOLDS, datasetName + "_q" + querySampleCount + "_o" + dataSampleCount + "_pcaLength" + pcaLength + "_kPCA" + kPCA + ".csv");
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

}
