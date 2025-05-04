/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.impl.paper.devel.p2025ICDEPtolemaiosLimited;

import vm.fs.plot.FSAbstractPlotterFromResults;
import static vm.fs.plot.impl.paper.used.p2024VLDBPtolemaiosLimited.FSPtolemaiosPlottingMain.PLOT_ONLY_PDF;

/**
 *
 * @author xmic
 */
public class FSPtolemaiosPlottingMain {

    public static void main(String[] args) {
        PlotPtolemaiosMOCAPBothFPS.setPivotCount(64);
        new PlotPtolemaiosMOCAPBothFPS(PLOT_ONLY_PDF).makePlots();
        PlotPtolemaiosMOCAPBothFPS.setPivotCount(128);
        new PlotPtolemaiosMOCAPBothFPS(PLOT_ONLY_PDF).makePlots();
    }
}
