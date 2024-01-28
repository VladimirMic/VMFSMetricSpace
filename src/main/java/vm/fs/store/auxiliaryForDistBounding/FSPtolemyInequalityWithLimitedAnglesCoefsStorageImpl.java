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
import vm.fs.main.precomputeDistances.FSEvalAndStoreSampleOfSmallestDistsMain;
import vm.metricSpace.distance.bounding.twopivots.impl.DataDependentGeneralisedPtolemaicFiltering;
import vm.metricSpace.distance.bounding.twopivots.storeLearned.PtolemyInequalityWithLimitedAnglesCoefsStoreInterface;

/**
 *
 * @author xmic
 */
public class FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl implements PtolemyInequalityWithLimitedAnglesCoefsStoreInterface {

    public static final Logger LOG = Logger.getLogger(FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.class.getName());

    public static File getFile(String resultName, boolean willBeDeleled) {
        File folderFile = new File(FSGlobal.AUXILIARY_FOR_PTOLEMAIOS_COEFS_WITH_LIMITED_ANGLES);
        folderFile.mkdirs();
        File ret = new File(folderFile, resultName);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleled);
        LOG.log(Level.INFO, "File path: {0}", ret.getAbsolutePath());
        return ret;
    }

    @Override
    public String getResultDescription(String datasetName, int smallestDists, int sampleOCount, int sampleQcount, int pivots, boolean allPivotPairs) {
        String ret = datasetName + "__smallestDists_" + smallestDists + "_sampleOCount" + sampleOCount + "_sampleQcount" + sampleQcount + "__pivots_" + pivots + "__allPivotPairs_" + allPivotPairs + ".csv";
        LOG.log(Level.INFO, "File name: {0}", ret);
        return ret;
    }

    public static DataDependentGeneralisedPtolemaicFiltering getLearnedInstance(String resultPreffixName, String datasetName, int pivotCount, boolean allPivotPairs) {
        FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl();
        String fileName = storage.getNameOfFileWithCoefs(datasetName, pivotCount, allPivotPairs);
        File file = getFile(fileName, false);
        Map<String, float[]> coefs = Tools.parseCsvMapKeyFloatValues(file.getAbsolutePath());
        return new DataDependentGeneralisedPtolemaicFiltering(resultPreffixName, coefs);
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

    public String getNameOfFileWithCoefs(String datasetName, int pivotCount, boolean allPivotPairs) {
        return getResultDescription(datasetName, FSEvalAndStoreSampleOfSmallestDistsMain.IMPLICIT_K,  FSEvalAndStoreSampleOfSmallestDistsMain.SAMPLE_SET_SIZE,  FSEvalAndStoreSampleOfSmallestDistsMain.SAMPLE_QUERY_SET_SIZE, pivotCount, allPivotPairs);
    }

}
