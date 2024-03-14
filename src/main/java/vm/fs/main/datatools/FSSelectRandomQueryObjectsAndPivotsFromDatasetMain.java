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
    public static final Integer IMPLICIT_NUMBER_OF_PIVOTS = 2560; // also selects one tenth and one fifth

    public static void main(String[] args) {
        Dataset[] datasets = {new FSDatasetInstanceSingularizator.RandomDataset20Uniform()};
        for (Dataset dataset : datasets) {
            run(dataset);
        }
    }

    public static void run(Dataset dataset) {
        run(dataset, IMPLICIT_NUMBER_OF_QUERIES, IMPLICIT_NUMBER_OF_PIVOTS);
    }

    public static void run(Dataset dataset, long numberOfQueries, long numberOfPivots) {
        long datasetSize = dataset.getPrecomputedDatasetSize();
        if (datasetSize < 0) {
            datasetSize = dataset.updateDatasetSize();
        }
        Iterator it = dataset.getMetricObjectsFromDataset();
        long batchSizeForQueries = (long) datasetSize / numberOfQueries;
        long batchSizeForPivots = (long) datasetSize / numberOfPivots;
        float lcm = vm.math.Tools.lcm(batchSizeForQueries, batchSizeForPivots);
        lcm = Math.min(lcm, datasetSize);

        int queriesPerBatch = (int) Math.ceil((lcm / datasetSize) * numberOfQueries);
        int pivotsPerBatch = (int) Math.ceil((lcm / datasetSize) * numberOfPivots);

        List queries = new ArrayList<>();
        List pivots = new ArrayList<>();

        while (queries.size() < numberOfQueries || pivots.size() < numberOfPivots) {
            List<Object> batch = Tools.getObjectsFromIterator(it, (int) lcm);
            float ratio = batch.size() / lcm;
            if (ratio != 1) {
                selectObjectsFromBatchUniformly(queries, (int) (numberOfQueries - queries.size()), batch);
                selectObjectsFromBatchUniformly(pivots, (int) (numberOfPivots - pivots.size()), batch);
            } else {
                selectObjectsFromBatchUniformly(queries, (int) (queriesPerBatch * ratio), batch);
                selectObjectsFromBatchUniformly(pivots, (int) (pivotsPerBatch * ratio), batch);
            }
        }
        queries = Tools.truncateList(queries, numberOfQueries);
        pivots = Tools.truncateList(pivots, numberOfPivots);
        String datasetName = dataset.getDatasetName();
        dataset.storeQueryObjects(queries, datasetName);
        dataset.storePivots(pivots, getNameForPivots(datasetName, pivots.size()));
        pivots = selectOneOutOfEachGroup(pivots, 5);
        dataset.storePivots(pivots, getNameForPivots(datasetName, pivots.size()));
        pivots = selectOneOutOfEachGroup(pivots, 2);
        dataset.storePivots(pivots, getNameForPivots(datasetName, pivots.size()));
    }

    private static String getNameForPivots(String datasetName, int count) {
        if (count == 256) {
            return datasetName;
        }
        return datasetName + "_" + count + "pivots.gz";
    }

    private static void selectObjectsFromBatchUniformly(List destination, int toBeSelected, List source) {
        int batchSize = source.size() / toBeSelected;
        for (int i = 0; i < toBeSelected; i++) {
            List subList = source.subList(i * batchSize, (i + 1) * batchSize);
            Object randomObject = Tools.randomObject(subList);
            destination.add(randomObject);
        }
    }

    private static List selectOneOutOfEachGroup(List pivots, int tuple) {
        List ret = new ArrayList();
        for (int i = 0; (i + 1) * tuple <= pivots.size(); i++) {
            List subList = pivots.subList(i * tuple, (i + 1) * tuple);
            Object randomObject = Tools.randomObject(subList);
            ret.add(randomObject);
        }
        return ret;
    }
}
