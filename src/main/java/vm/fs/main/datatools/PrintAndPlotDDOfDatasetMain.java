package vm.fs.main.datatools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.JFreeChart;
import vm.fs.FSGlobal;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.math.Tools;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.ToolsMetricDomain;
import vm.metricSpace.AbstractMetricSpacesStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.plot.impl.XYLinesPlotter;

/**
 *
 * @author xmic
 */
public class PrintAndPlotDDOfDatasetMain {

    public static final int IMPLICIT_OBJ_COUNT = 1000 * 1000;//1,000,000
    public static final int IMPLICIT_DIST_COUNT = 1000 * 10000;//10,000,000

    public static void main(String[] args) {
        Dataset[] datasets = {
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(true),
            new FSDatasetInstanceSingularizator.DeCAFDataset(),
//            new FSDatasetInstanceSingularizator.LAION_100M_PCA256Dataset()
        };
        float[] distIntervals = {
            1f / 100,
            2f,
            2f
        };
        for (int i = 0; i < datasets.length; i++) {
            run(datasets[i], distIntervals[i]);
        }
    }

    public static void run(Dataset dataset, float distInterval) {
        String datasetName = dataset.getDatasetName();
//      getHistogramsForRandomPairs
        File f = getFileForDistDensity(datasetName, distInterval, IMPLICIT_OBJ_COUNT, IMPLICIT_DIST_COUNT, false);
        SortedMap<Float, Float> ddRandomSample;
        if (f.exists()) {
            ddRandomSample = vm.datatools.Tools.parseCsvMapFloats(f.getAbsolutePath());
        } else {
            ddRandomSample = createDDOfRandomSample(dataset, datasetName, IMPLICIT_OBJ_COUNT, IMPLICIT_DIST_COUNT, distInterval, null);
        }
//      print
        Map<Float, Float> mapOfValues = printDD(distInterval, f, ddRandomSample);
        createPlot(f, mapOfValues);
    }

    protected static SortedMap<Float, Float> createDDOfRandomSample(Dataset dataset, String datasetName, int objCount, int distCount, float distInterval, List<Object[]> examinedPairs) {
        return createDDOfRandomSample(dataset.getMetricSpace(), dataset.getMetricSpacesStorage(), dataset.getDistanceFunction(), datasetName, objCount, distCount, distInterval, examinedPairs);
    }

    protected static SortedMap<Float, Float> createDDOfRandomSample(AbstractMetricSpace metricSpace, AbstractMetricSpacesStorage metricSpacesStorage, DistanceFunctionInterface df, String datasetName, int objCount, int distCount, float distInterval, List<Object[]> examinedPairs) {
        List<Object> metricObjects = metricSpacesStorage.getSampleOfDataset(datasetName, objCount);
        return ToolsMetricDomain.createDistanceDensityPlot(metricSpace, metricObjects, df, distCount, distInterval, examinedPairs);
    }

    private static Map<Float, Float> printDD(float distInterval, File f, SortedMap<Float, Float> histogram) {
        PrintStream ps = null;
        Map<Float, Float> ret = new TreeMap<>();
        try {
            ps = new PrintStream(f);
            ps.println("Distance;Density of random sample");
            float lastDist = 0;
            for (float dist : histogram.keySet()) {
                while (dist - lastDist > distInterval * 1.1d) {
                    lastDist += distInterval;
                    lastDist = Tools.round(lastDist, distInterval, false);
                    ps.println(lastDist + ";" + 0);
                    ret.put(lastDist, 0f);
                }
                Float value = histogram.get(dist);
                ps.println(dist + ";" + value);
                ret.put(dist, value);
                lastDist = dist;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrintAndPlotDDOfDatasetMain.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ps.flush();
            ps.close();
        }
        return ret;
    }

    private static File getFileForDistDensity(String datasetName, float distInterval, int objCount, int distCount, boolean willBeDeleted) {
        String fileName = datasetName + "_int" + distInterval + "_o" + objCount + "_d" + distCount + ".csv";
        File ret = new File(FSGlobal.DIST_DISTRIBUTION_PLOTS_FOLDER, fileName);
        ret = FSGlobal.checkFileExistence(ret, willBeDeleted);
        return ret;
    }

    private static void createPlot(File f, Map<Float, Float> mapOfValues) {
        XYLinesPlotter plotter = new XYLinesPlotter();
        float[] traceXValues = new float[mapOfValues.size()];
        float[] traceYValues = new float[mapOfValues.size()];
        Iterator<Map.Entry<Float, Float>> it = mapOfValues.entrySet().iterator();
        for (int i = 0; it.hasNext(); i++) {
            Map.Entry<Float, Float> entry = it.next();
            traceXValues[i] = entry.getKey();
            traceYValues[i] = entry.getValue();
        }
        JFreeChart plot = plotter.createPlot("", "Distance", "", "", traceXValues, traceYValues);
        String path = f.getAbsolutePath();
        path = path.substring(0, path.lastIndexOf("."));
        plotter.storePlotPDF(path, plot);
        plotter.setLogY(true);
        plot = plotter.createPlot("", "Distance", "occurences", "", traceXValues, traceYValues);
        path += "_log";
        plotter.storePlotPDF(path, plot);
    }

}
