package vm.fs.store.auxiliaryForDistBounding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaiosFilteringWithLimitedAnglesSimpleCoef;
import vm.metricSpace.distance.bounding.twopivots.storeLearned.PtolemyInequalityWithLimitedAnglesCoefsStoreInterface2;

/**
 *
 * @author xmic
 */
public class FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl implements PtolemyInequalityWithLimitedAnglesCoefsStoreInterface2 {

    public static final Logger LOG = Logger.getLogger(FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.class.getName());

    public File getFile(String resultName) {
        File folderFile = new File(FSGlobal.AUXILIARY_FOR_PTOLEMAIOS_COEFS_WITH_LIMITED_ANGLES);
        folderFile.mkdirs();
        File ret = new File(folderFile, resultName);
        if (ret.exists()) {
            Logger.getLogger(FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName()).log(Level.WARNING, "The file already existed");
        }
        LOG.log(Level.INFO, "File path: {0}", ret.getAbsolutePath());
        return ret;
    }

    @Override
    public String getResultDescription(String datasetName, int numberOfTetrahedrons, int pivotPairs, float ratioOfSmallestDists) {
        String ret = datasetName + "__tetrahedrons_" + numberOfTetrahedrons + "__ratio_of_outliers_to_cut_" + ratioOfSmallestDists + "__pivot_pairs_" + pivotPairs + ".csv";
        LOG.log(Level.INFO, "File name: {0}", ret);
        return ret;
    }

    public PtolemaiosFilteringWithLimitedAnglesSimpleCoef loadFromFile(String resultPreffixName, String datasetName) {
        File file = getFile(datasetName);
        Map<String, float[]> coefs = Tools.parseCsvMapKeyFloatValues(file.getAbsolutePath());
        return new PtolemaiosFilteringWithLimitedAnglesSimpleCoef(resultPreffixName, coefs);
    }

    @Override
    public void storeCoefficients(Map<String, float[]> results, String resultName) {
        try {
            File resultFile = getFile(resultName);
            PrintStream err = System.err;
            System.setErr(new PrintStream(new FileOutputStream(resultFile, true)));
            Tools.printMap(results);
            System.setErr(err);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public String getResultName(String datasetName) {
        return getResultDescription(datasetName, 100000, 128, 0.01f);
    }

}
