/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.search.perform;

import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.fs.store.queryResults.recallEvaluation.FSRecallOfCandidateSetsStorageImpl;
import vm.metricSpace.Dataset;
import vm.search.AlgorithmEvaluator;
import vm.search.algorithm.SearchingAlgorithm;

/**
 *
 * @author Vlada
 */
public class EvaluateQuerySetWithAlgorithm {

    public static void main(String[] args) {
        int k = 100;
        Dataset[] datasets = new Dataset[]{};
        SearchingAlgorithm alg = null;

        for (Dataset dataset : datasets) {
            evaluate(alg, dataset, k);
        }
    }

    private static void evaluate(SearchingAlgorithm alg, Dataset dataset, int k) {
        FSRecallOfCandidateSetsStorageImpl statsStorage = new FSRecallOfCandidateSetsStorageImpl(dataset.getDatasetName(), dataset.getQuerySetName(), k, dataset.getDatasetName(), dataset.getQuerySetName(), alg.getResultName(), null);
        FSNearestNeighboursStorageImpl resultsStorage = new FSNearestNeighboursStorageImpl();
        AlgorithmEvaluator evaluator = new AlgorithmEvaluator(alg, statsStorage, resultsStorage, statsStorage, statsStorage);
        evaluator.evaluate(dataset, dataset.getMetricQueryObjects(), k, alg.getResultName());
    }
}
