package vm.fs.store.dataTransforms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.fs.store.precomputedDists.FSPrecomputedDistPairsStorageImpl;
import static vm.fs.store.precomputedDists.FSPrecomputedDistPairsStorageImpl.LOG;
import vm.metricSpace.AbstractMetricSpace;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSGHPSketchesPivotPairsStorageImpl implements GHPSketchingPivotPairsStoreInterface {

    @Override
    public void storeSketching(String resultName, AbstractMetricSpace<Object> metricSpace, List<Object> pivots, Object... additionalInfoToStoreWithLearningSketching) {
        OutputStreamWriter w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(getFileForResults(resultName, true), false));
            for (int i = 0; i < pivots.size(); i += 2) {
                Object p1 = metricSpace.getIDOfMetricObject(pivots.get(i));
                Object p2 = metricSpace.getIDOfMetricObject(pivots.get(i + 1));
                w.write(p1 + ";" + p2 + "\n");
            }
            if (additionalInfoToStoreWithLearningSketching.length != 0) {
                for (Object object : additionalInfoToStoreWithLearningSketching) {
                    w.write(object.toString() + "\n");
                }
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

    private File getFileForResults(String resultName, boolean willBeDeleted) {
        File ret = new File(FSGlobal.BINARY_SKETCHES, resultName + ".csv");
        if (willBeDeleted) {
            FSGlobal.askForAFileExistence(ret);
        }
        return ret;
    }

    public TreeSet<Map.Entry<String, Float>> loadPivotPairsIDs(String sketchesName) {
        BufferedReader br = null;
        try {
            File file = getFileForResults(sketchesName, false);
            if (!file.exists()) {
                throw new IllegalArgumentException("File with pivot pairs does no exists. Sketches name " + sketchesName);
            }
            Comparator<Map.Entry<String, Float>> comp = new Tools.MapByValueComparator<>();
            TreeSet<Map.Entry<String, Float>> ret = new TreeSet(comp);
            br = new BufferedReader(new FileReader(file));
            try {
                while (true) {
                    String line = br.readLine();
                    String[] split = line.split(";");
                    String key = split[0] + ";" + split[1];
                    float value = Float.parseFloat(split[2]);
                    AbstractMap.SimpleEntry<String, Float> e = new AbstractMap.SimpleEntry<>(key, value);
                    ret.add(e);
                }
            } catch (NullPointerException ex) {
            }
            return ret;
        } catch (IOException ex) {
            Logger.getLogger(FSPrecomputedDistPairsStorageImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(FSPrecomputedDistPairsStorageImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

}
