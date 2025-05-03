/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.impl.paper.devel.p2025ICDEPtolemaiosLimited;

import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.fs.plot.impl.paper.devel.p2024VLDBPtolemaiosLimited.random.PlotPtolemaiosRandom100Abblation;
import static vm.fs.plot.impl.paper.used.p2024VLDBPtolemaiosLimited.FSPtolemaiosPlottingMain.PLOT_ONLY_PDF;

/**
 *
 * @author xmic
 */
public class FSPtolemaiosPlottingMain {

    public static final FSAbstractPlotterFromResults Y2025_PlotPtolemaiosMocap30FPS = new PlotPtolemaiosMOCAP30FPS(PLOT_ONLY_PDF);

    public static void main(String[] args) {
        Y2025_PlotPtolemaiosMocap30FPS.makePlots();
//        Y2025_PlotPtolemaiosMocap30FPS.makePlots();

    }
}
