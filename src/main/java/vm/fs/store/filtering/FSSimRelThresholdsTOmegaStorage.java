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
public class FSSimRelThresholdsTOmegaStorage extends SimRelEuclidThresholdsTOmegaStorage {

    public static final Logger LOG = Logger.getLogger(FSSimRelThresholdsTOmegaStorage.class.getName());

    private final int querySampleCount;
    private final int pcaLength;
    private final int kPCA;
    private final Integer voronoiPivotsCount;
    private final Integer voronoiK;

    public FSSimRelThresholdsTOmegaStorage(int querySampleCount, int pcaLength, int kPCA, int sampleSize) {
        this(querySampleCount, pcaLength, kPCA, null, sampleSize);
    }

    public FSSimRelThresholdsTOmegaStorage(int querySampleCount, int pcaLength, int kPCA, Integer voronoiPivotsCount, Integer voronoiK) {
        this.querySampleCount = querySampleCount;
        this.pcaLength = pcaLength;
        this.kPCA = kPCA;
        this.voronoiPivotsCount = voronoiPivotsCount;
        this.voronoiK = voronoiK;
    }

    @Override
    public void store(float[][] thresholds, String datasetName) {
        File f = getFile(datasetName, true);
        OutputStreamWriter w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(f, false));
            for (int i = 0; i < thresholds[0].length; i++) {
                for (float[] threshold : thresholds) {
                    w.write(threshold[i] + ";");
                }
                w.write("\n");
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
    public float[][] load(String datasetName) {
        File file = getFile(datasetName, false);
        List<String>[] values = Tools.parseCsv(file.getAbsolutePath(), 14, ";", false);
        float[][] ret = new float[14][values[0].size()];
        for (int i = 0; i < ret[0].length; i++) {
            for (int j = 0; j < 14; j++) {
                ret[j][i] = Float.parseFloat(values[j].get(i));
            }
        }
        return ret;
    }

    private File getFile(String datasetName, boolean willBeDeleted) {
        String name = datasetName;
        name += "_q" + querySampleCount + "voronoiP" + voronoiPivotsCount + "_voronoiK" + voronoiK + "_pcaLength" + pcaLength + "_kPCA" + kPCA + ".csv";
        File ret = new File(FSGlobal.SIMREL_TOMEGA_THRESHOLDS, name);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

}