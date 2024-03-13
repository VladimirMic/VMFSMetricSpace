/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot;

import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosAllClassic;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData;

/**
 *
 * @author au734419
 */
public class FSExperimentsPlottingMain {

    public static final Boolean PLOT_ONLY_SVG = true;
    public static final FSAbstractPlotterFromResults Y2024_PTOLEMAIOS_RANDOM = new PlotPtolemaiosRandomData(true);
    public static final FSAbstractPlotterFromResults Y2024_PTOLEMAIOS_CLASSI = new PlotPtolemaiosAllClassic(true);

    public static void main(String[] args) {
        Y2024_PTOLEMAIOS_RANDOM.makePlots();
    }
}
