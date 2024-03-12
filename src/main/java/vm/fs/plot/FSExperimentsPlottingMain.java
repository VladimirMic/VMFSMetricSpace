/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot;

import vm.fs.plot.implForPapers.PtolemaicFiltering2024RandomVectorsDims;

/**
 *
 * @author au734419
 */
public class FSExperimentsPlottingMain {

    public static void main(String[] args) {
        FSAbstractPlotterFromResults plotter = new PtolemaicFiltering2024RandomVectorsDims();
        plotter.makePlots();
    }
}
