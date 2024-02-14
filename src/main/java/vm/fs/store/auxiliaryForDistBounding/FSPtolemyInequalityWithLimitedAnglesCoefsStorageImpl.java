package vm.fs.store.auxiliaryForDistBounding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.fs.main.precomputeDistances.FSEvalAndStoreSampleOfSmallestDistsMain;
import vm.metricSpace.Dataset;
import vm.metricSpace.ToolsMetricDomain;
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

    public static DataDependentGeneralisedPtolemaicFiltering getLearnedInstance(String resultPreffixName, Dataset dataset, int pivotCount, boolean allPivotPairs) {
        FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl();
        String fileName = storage.getNameOfFileWithCoefs(dataset.getDatasetName(), pivotCount, allPivotPairs);
        File file = getFile(fileName, false);
//        if (KNNSearchWithOnePivotFiltering.SORT_PIVOTS && (!LearningCoefsForPtolemyInequalityWithLimitedAngles.ALL_PIVOT_PAIRS || !allPivotPairs)) {
//            throw new IllegalArgumentException("If the pivots are sorted, then the code needs coefficients for all pivot pairs. The params, though, are inconsistent: KNNSearchWithOnePivotFiltering.SORT_PIVOTS: " + KNNSearchWithOnePivotFiltering.SORT_PIVOTS + ", LearningPtolemyInequalityWithLimitedAngles.ALL_PIVOT_PAIRS (used in the algorithm init): " + LearningCoefsForPtolemyInequalityWithLimitedAngles.ALL_PIVOT_PAIRS + ", allPivotPairs (used to init file): " + allPivotPairs);
//        }
        Map<String, float[]> coefs = Tools.parseCsvMapKeyFloatValues(file.getAbsolutePath());
        List pivots = dataset.getPivots(pivotCount);
        List pivotIDs = ToolsMetricDomain.getIDsAsList(pivots.iterator(), dataset.getMetricSpace());
        float[][][] coefsToArrays = transformsCoefsToArrays(coefs, pivotIDs);
        return new DataDependentGeneralisedPtolemaicFiltering(resultPreffixName, coefsToArrays);
    }

    private static float[][][] transformsCoefsToArrays(Map<String, float[]> coefs, List pivotIDs) {
        Iterator<String> it = coefs.keySet().iterator();
        float[][][] ret = new float[pivotIDs.size()][pivotIDs.size()][4];
        while (it.hasNext()) {
            String key = it.next();
            String[] pivots = key.split("-");
            int idx1 = pivotIDs.indexOf(pivots[0]);
            int idx2 = pivotIDs.indexOf(pivots[1]);
            ret[idx1][idx2] = coefs.get(key);
            ret[idx2][idx1] = coefs.get(key);
        }
        return ret;
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
        return getResultDescription(datasetName, FSEvalAndStoreSampleOfSmallestDistsMain.IMPLICIT_K, FSEvalAndStoreSampleOfSmallestDistsMain.SAMPLE_SET_SIZE, FSEvalAndStoreSampleOfSmallestDistsMain.SAMPLE_QUERY_SET_SIZE, pivotCount, allPivotPairs);
    }

}
