/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.papers.impl.main;

import vm.metricSpace.Dataset;

/**
 *
 * @author Vlada
 */
public class VLDB24DataDepPtolemaicFiltering {

    public static void main(String[] args) {
        int dimensionality = 70;
        int pivotsCount = 128;
        int queriesCount = 1000;
        Dataset dataset = createRandomUniformDatasetQueriesPivots(dimensionality, pivotsCount, queriesCount);
        learnFilterings(dataset);

    }

    private static Dataset createRandomUniformDatasetQueriesPivots(int dimensionality, int pivotsCount, int queriesCount) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private static void learnFilterings(Dataset dataset) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
