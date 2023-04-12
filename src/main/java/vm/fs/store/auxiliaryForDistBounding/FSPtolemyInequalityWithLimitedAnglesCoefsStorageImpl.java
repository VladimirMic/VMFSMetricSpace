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
import vm.fs.main.precomputeDistances.EvalAndStoreSampleOfSmallestDists;
import vm.metricSpace.distance.bounding.twopivots.impl.PtolemaiosFilteringWithLimitedAnglesSimpleCoef;
import vm.metricSpace.distance.bounding.twopivots.storeLearned.PtolemyInequalityWithLimitedAnglesCoefsStoreInterface2;

/**
 *
 * @author xmic
 */
public class FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl implements PtolemyInequalityWithLimitedAnglesCoefsStoreInterface2 {

    public static final Logger LOG = Logger.getLogger(FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.class.getName());

    public static File getFile(String resultName, boolean willBeDeleled) {
        File folderFile = new File(FSGlobal.AUXILIARY_FOR_PTOLEMAIOS_COEFS_WITH_LIMITED_ANGLES);
        folderFile.mkdirs();
        File ret = new File(folderFile, resultName);
        if (willBeDeleled) {
            FSGlobal.checkFileExistence(ret);
        }
        LOG.log(Level.INFO, "File path: {0}", ret.getAbsolutePath());
        return ret;
    }

    @Override
    public String getResultDescription(String datasetName, int numberOfTetrahedrons, int pivots, float ratioOfSmallestDists) {
        String ret = datasetName + "__tetrahedrons_" + numberOfTetrahedrons + "__ratio_of_outliers_to_cut_" + ratioOfSmallestDists + "__pivots_" + pivots + ".csv";
        LOG.log(Level.INFO, "File name: {0}", ret);
        return ret;
    }

    public static PtolemaiosFilteringWithLimitedAnglesSimpleCoef getLearnedInstance(String resultPreffixName, String datasetName, int pivotCount) {
        FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl();
        String fileName = storage.getNameOfFileWithCoefs(datasetName, pivotCount);
        File file = getFile(fileName, false);
        Map<String, float[]> coefs = Tools.parseCsvMapKeyFloatValues(file.getAbsolutePath());
        return new PtolemaiosFilteringWithLimitedAnglesSimpleCoef(resultPreffixName, coefs);
    }

    @Override
    public void storeCoefficients(Map<Object, float[]> results, String resultName) {
        try {
            File resultFile = getFile(resultName, true);
            PrintStream err = System.err;
            System.setErr(new PrintStream(new FileOutputStream(resultFile, true)));
            Tools.printMapOfKeyFloatValues(results);
            System.setErr(err);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private String getNameOfFileWithCoefs(String datasetName, int pivotCount) {
        return getResultDescription(datasetName, EvalAndStoreSampleOfSmallestDists.IMPLICIT_K, pivotCount, PtolemaiosFilteringWithLimitedAnglesSimpleCoef.RATIO_OF_OUTLIERS_TO_CUT);
    }

}
