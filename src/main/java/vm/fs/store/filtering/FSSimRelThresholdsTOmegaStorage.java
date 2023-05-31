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

    private final int querySampleCount;
    private final int dataSampleCount;
    private final int pcaLength;
    private final int kPCA;
    private final float percentile;
    private final Integer voronoiPivotsCount;
    private final Integer voronoiK;

    public FSSimRelThresholdsTOmegaStorage(int querySampleCount, int dataSampleCount, int pcaLength, int kPCA, float percentile) {
        this(querySampleCount, dataSampleCount, pcaLength, kPCA, percentile, null, null);
    }

    public FSSimRelThresholdsTOmegaStorage(int querySampleCount, int dataSampleCount, int pcaLength, int kPCA, float percentile, Integer voronoiPivotsCount, Integer voronoiK) {
        this.querySampleCount = querySampleCount;
        this.dataSampleCount = dataSampleCount;
        this.pcaLength = pcaLength;
        this.kPCA = kPCA;
        this.percentile = percentile;
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
    public float[][] load(String datasetName) {
        File file = getFile(datasetName, false);
        List<String>[] values = Tools.parseCsv(file.getAbsolutePath(), 2, ";", true);
        float[][] ret = new float[2][values[0].size()];
        for (int i = 0; i < ret[0].length; i++) {
            ret[0][i] = Float.parseFloat(values[0].get(i));
            ret[1][i] = Float.parseFloat(values[1].get(i));
        }
        return ret;
    }

    private File getFile(String datasetName, boolean willBeDeleted) {
        String name = datasetName;
        if (voronoiK != null && voronoiPivotsCount != null) {
            name += "_voronoiP" + voronoiPivotsCount + "_O" + voronoiK;
        }
        name += "_q" + querySampleCount + "_o" + dataSampleCount + "_pcaLength" + pcaLength + "_kPCA" + kPCA + "_perc" + percentile + ".csv";
        File ret = new File(FSGlobal.SIMREL_TOMEGA_THRESHOLDS, name);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

}
