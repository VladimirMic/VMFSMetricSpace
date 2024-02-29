/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class PrintiiDimOfDatasetMain {

    public static void main(String[] args) {
        boolean publicQueries = true;
        Dataset[] datasets = new Dataset[]{
            //            new FSDatasetInstanceSingularizator.LAION_100k_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_300k_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(publicQueries),
            //            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(publicQueries),
//            new FSDatasetInstanceSingularizator.LAION_100M_Dataset(publicQueries),
            new FSDatasetInstanceSingularizator.LAION_10M_PCA256Dataset()
        };
        for (Dataset dataset : datasets) {
            run(dataset);
        }

    }

    private static void run(Dataset dataset) {
        int dCount = 1000000;
        List sampleOfDataset = dataset.getSampleOfDataset(dCount);
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        double[] distances = new double[dCount];
        for (int i = 0; i < dCount; i++) {
            Object o1 = metricSpace.getDataOfMetricObject(Tools.randomObject(sampleOfDataset));
            Object o2 = metricSpace.getDataOfMetricObject(Tools.randomObject(sampleOfDataset));
            distances[i] = df.getDistance(o1, o2);
            if (i % 100000 == 0) {
                Logger.getLogger(PrintiiDimOfDatasetMain.class.getName()).log(Level.INFO, "Evaluated {0} distances out of {1}", new Object[]{i, dCount});
            }
        }
        double iDim = vm.math.Tools.getIDim(distances, true);
        System.out.println("");
        System.out.println("iDim: " + iDim);
    }
}
