package vm.fs.store.auxiliaryForDistBounding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.FSGlobal;
import vm.metricspace.distance.bounding.onepivot.storeLearned.TriangleInequalityWithLimitedAnglesCoefsStoreInterface;

/**
 *
 * @author Vlada
 */
public class TriangleInequalityWithLimitedAnglesCoefsStorageImpl implements TriangleInequalityWithLimitedAnglesCoefsStoreInterface {

    @Override
    public void storeCoefficients(Map<Object, Float> results, String resultName) {
        try {
            File resultFile = getFile(FSGlobal.AUXILIARY_FOR_TRIANGULAR_FILTERING_WITH_LIMITED_ANGLES, resultName);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile, false))) {
                for (Map.Entry<Object, Float> entry : results.entrySet()) {
                    String pivotID = entry.getKey().toString();
                    String coef = entry.getValue().toString();
                    bw.write(pivotID + ";" + coef + "\n");
                }
                bw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(TriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private File getFile(String folder, String resultName) {
        File folderFile = new File(folder);
        folderFile.mkdirs();
        File ret = new File(folderFile, resultName);
        if (ret.exists()) {
            Logger.getLogger(TriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName()).log(Level.WARNING, "The file already existed");
        }
        return ret;
    }

}
