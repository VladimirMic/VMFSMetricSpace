/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.datatools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import vm.datatools.Tools;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.metricSpace.Dataset;

/**
 *
 * @author Vlada
 */
public class FSSelectRandomQueryObjectsAndPivotsFromDatasetMain {

    public static final Integer IMPLICIT_NUMBER_OF_QUERIES = 1000;
    public static final Integer IMPLICIT_NUMBER_OF_PIVOTS = 256;

    public static void main(String[] args) {
        Dataset[] datasets = {new FSDatasetInstanceSingularizator.DeCAF100M_TMPDataset()};
        for (Dataset dataset : datasets) {
            run(dataset, IMPLICIT_NUMBER_OF_QUERIES, IMPLICIT_NUMBER_OF_PIVOTS);
        }
    }

    private static void run(Dataset dataset, long numberOfQueries, long numberOfPivots) {
        long datasetSize = dataset.getPrecomputedDatasetSize();
        if (datasetSize < 0) {
            datasetSize = dataset.updateDatasetSize();
        }
        Iterator it = dataset.getMetricObjectsFromDataset();
        long batchSizeForQueries = (long) datasetSize / numberOfQueries;
        long batchSizeForPivots = (long) datasetSize / numberOfPivots;
        long lcm = vm.math.Tools.lcm(batchSizeForQueries, batchSizeForPivots);

        int queriesPerBatch = (int) (numberOfQueries / lcm);
        int pivotsPerBatch = (int) (numberOfPivots / lcm);;

        List queries = new ArrayList<>();
        List pivots = new ArrayList<>();

        while (queries.size() != numberOfQueries) {
            List<Object> batch = Tools.getObjectsFromIterator(it, (int) lcm);
            selectObjectsFromBatchUniformly(queries, queriesPerBatch, batch);
            selectObjectsFromBatchUniformly(pivots, pivotsPerBatch, batch);
        }
        String datasetName = dataset.getDatasetName();
        String pivotSetName = getSetName(datasetName, numberOfPivots);
        String querySetName = getSetName(datasetName, numberOfQueries);
        dataset.storeQueryObjects(queries, querySetName);
        dataset.storePivots(pivots, pivotSetName);
    }

    private static void selectObjectsFromBatchUniformly(List destination, int toBeSelected, List source) {
        int batchSize = source.size() / toBeSelected;
        for (int i = 0; i < toBeSelected; i++) {
            List subList = source.subList(i * batchSize, (i - 1) * batchSize);
            Object randomObject = Tools.randomObject(subList);
            destination.add(randomObject);
        }
    }

    private static String getSetName(String datasetName, long numberOfPivots) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
