package vm.fs.store.auxiliaryForDistBounding;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.fs.FSGlobal;
import vm.metricspace.distance.bounding.twopivots.impl.PtolemaiosFilteringWithLimitedAnglesOrigProposal;
import vm.metricspace.distance.bounding.twopivots.storeLearned.PtolemyInequalityWithLimitedAnglesCoefsStoreInterface;
import vm.structures.ConvexHull2DEuclid;

/**
 *
 * @author Vlada
 */
public class FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl implements PtolemyInequalityWithLimitedAnglesCoefsStoreInterface {

    public static final Logger LOG = Logger.getLogger(FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.class.getName());

    public File getFile(String resultName) {
        File folderFile = new File(FSGlobal.AUXILIARY_FOR_PTOLEMAIOS_WITH_LIMITED_ANGLES);
        folderFile.mkdirs();
        File ret = new File(folderFile, resultName);
        if (ret.exists()) {
            Logger.getLogger(FSTriangleInequalityWithLimitedAnglesCoefsStorageImpl.class.getName()).log(Level.WARNING, "The file already existed");
        }
        LOG.log(Level.INFO, "File path: {0}", ret.getAbsolutePath());
        return ret;
    }

    @Override
    public String getResultDescription(String datasetName, int pivotSize, int sampleSetSize, int queriesSampleSize, float ratioOfSmallestDists) {
        String ret = datasetName + "_" + pivotSize + "pivots_" + sampleSetSize + "samples1_" + queriesSampleSize + "samples2_" + (ratioOfSmallestDists * 100) + "percentSmallest.csv";
        LOG.log(Level.INFO, "File name: {0}", ret);
        return ret;
    }

    public PtolemaiosFilteringWithLimitedAnglesOrigProposal loadFromFile(String resultPreffixName, String resultName) {
        File file = getFile(resultName);
        Map<String, List<Point2D.Double>> hulls = ConvexHull2DEuclid.parsePivotsHulls(file.getAbsolutePath(), true);
        return new PtolemaiosFilteringWithLimitedAnglesOrigProposal(resultPreffixName, hulls);
    }

    @Override
    public void storeHull(String outputPath, String hullID, ConvexHull2DEuclid hullsForPivotPair) {
        try {
            PrintStream err = System.err;
            System.setErr(new PrintStream(new FileOutputStream(outputPath, true)));
            System.err.print(hullID + ";");
            System.err.println(hullsForPivotPair.toString());
            System.setErr(err);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FSPtolemyInequalityWithLimitedAnglesCoefsStorageImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
