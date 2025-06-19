package vm.fs.store.auxiliaryForDistBounding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.fs.main.search.filtering.learning.FSLearnCoefsForDataDepenentMetricFilteringMain;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.Dataset;
import vm.searchSpace.ToolsSpaceDomain;
import vm.searchSpace.distance.bounding.onepivot.impl.DataDependentMetricFiltering;
import static vm.searchSpace.distance.bounding.onepivot.learning.LearningTriangleInequalityWithLimitedAngles.RATIO_OF_SMALLEST_DISTS;
import vm.searchSpace.distance.bounding.onepivot.storeLearned.TriangleInequalityWithLimitedAnglesCoefsStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl implements TriangleInequalityWithLimitedAnglesCoefsStoreInterface {

    public final Logger LOG = Logger.getLogger(FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName());

    @Override
    public void storeCoefficients(Map<Object, Float> results, String resultName) {
        try {
            File resultFile = getFile(resultName, true);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile, false))) {
                for (Map.Entry<Object, Float> entry : results.entrySet()) {
                    String pivotID = entry.getKey().toString();
                    String coef = entry.getValue().toString();
                    bw.write(pivotID + ";" + coef + "\n");
                }
                bw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getFile(String resultName, boolean willBeDeleted) {
        File folderFile = new File(FSGlobal.AUXILIARY_FOR_TRIANGULAR_FILTERING_WITH_LIMITED_ANGLES);
        File ret = new File(folderFile, resultName);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

    @Override
    public String getResultDescription(String datasetName, int pivotsCount, int sampleSetSize, int queriesSampleSize, float ratioOfSmallestDists) {
        String ret = datasetName + "_" + pivotsCount + "pivots_" + sampleSetSize + "samples1_" + queriesSampleSize + "samples2_" + (ratioOfSmallestDists * 100) + "percentSmallest.csv";
        LOG.log(Level.INFO, "File name: {0}", ret);
        return ret;
    }

    public static DataDependentMetricFiltering getLearnedInstanceTriangleInequalityWithLimitedAngles(String resultPreffixName, int pivotsCount, Dataset dataset) {
        return FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.getLearnedInstanceTriangleInequalityWithLimitedAngles(resultPreffixName, pivotsCount, FSLearnCoefsForDataDepenentMetricFilteringMain.SAMPLE_O_COUNT, FSLearnCoefsForDataDepenentMetricFilteringMain.SAMPLE_Q_COUNT, dataset);
    }

    public static DataDependentMetricFiltering getLearnedInstanceTriangleInequalityWithLimitedAngles(String resultPreffixName, int pivotsCount, int sampleSetSize, int queriesSampleSize, Dataset dataset) {
        FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl();
        String fileName = storage.getResultDescription(dataset.getDatasetName(), pivotsCount, sampleSetSize, queriesSampleSize, RATIO_OF_SMALLEST_DISTS);
        fileName = storage.getFile(fileName, false).getAbsolutePath();
        SortedMap<String, Float> coefsForPivots = Tools.parseCsvMapStringFloat(fileName);
        float[] coefsArray = getCoefsForPivots(dataset.getPivots(pivotsCount), dataset.getSearchSpace(), coefsForPivots);
        return new DataDependentMetricFiltering(resultPreffixName, coefsArray);
    }

    private static float[] getCoefsForPivots(List pivots, AbstractSearchSpace searchSpace, SortedMap<String, Float> coefsForPivots) {
        List iDs = ToolsSpaceDomain.getIDsAsList(pivots.iterator(), searchSpace);
        float[] ret = new float[iDs.size()];
        for (int i = 0; i < iDs.size(); i++) {
            String pivotID = (String) iDs.get(i);
            if (!coefsForPivots.containsKey(pivotID)) {
                throw new IllegalArgumentException("No coefficient for pivot " + pivotID);
            }
            ret[i] = coefsForPivots.get(pivotID);
        }
        return ret;
    }

}
