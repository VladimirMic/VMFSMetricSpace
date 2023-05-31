package vm.fs.store.auxiliaryForDistBounding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.metricSpace.distance.bounding.onepivot.impl.TriangleInequalityWithLimitedAngles;
import static vm.metricSpace.distance.bounding.onepivot.learning.LearningTriangleInequalityWithLimitedAngles.RATIO_OF_SMALLEST_DISTS;
import vm.metricSpace.distance.bounding.onepivot.storeLearned.TriangleInequalityWithLimitedAnglesCoefsStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl implements TriangleInequalityWithLimitedAnglesCoefsStoreInterface {

    public static final Logger LOG = Logger.getLogger(FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName());

    @Override
    public void storeCoefficients(Map<Object, Float> results, String resultName) {
        try {
            File resultFile = getFile(resultName);
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

    private File getFile(String resultName) {
        File folderFile = new File(FSGlobal.AUXILIARY_FOR_TRIANGULAR_FILTERING_WITH_LIMITED_ANGLES);
        folderFile.mkdirs();
        File ret = new File(folderFile, resultName);
        if (ret.exists()) {
            Logger.getLogger(FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName()).log(Level.WARNING, "The file already existed");
        }
        LOG.log(Level.INFO, "File path: {0}", ret.getAbsolutePath());
        return ret;
    }

    @Override
    public String getResultDescription(String datasetName, int pivotsCount, int sampleSetSize, int queriesSampleSize, float ratioOfSmallestDists) {
        String ret = datasetName + "_" + pivotsCount + "pivots_" + sampleSetSize + "samples1_" + queriesSampleSize + "samples2_" + (ratioOfSmallestDists * 100) + "percentSmallest.csv";
        LOG.log(Level.INFO, "File name: {0}", ret);
        return ret;
    }

    public TriangleInequalityWithLimitedAngles loadFromFile(String resultPreffixName, String path) {
        SortedMap<String, Float> values = Tools.parseCsvMapStringFloat(path);
        return new TriangleInequalityWithLimitedAngles(resultPreffixName, values);
    }

    public static TriangleInequalityWithLimitedAngles getLearnedInstanceTriangleInequalityWithLimitedAngles(String resultPreffixName, int pivotsCount, int sampleSetSize, int queriesSampleSize, float ratioOfSmallestDists, String datasetName) {
        FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl storage = new FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl();
        String fileName = storage.getResultDescription(datasetName, pivotsCount, sampleSetSize, queriesSampleSize, RATIO_OF_SMALLEST_DISTS);
        fileName = storage.getFile(fileName).getAbsolutePath();
        return storage.loadFromFile(resultPreffixName, fileName);
    }

}
