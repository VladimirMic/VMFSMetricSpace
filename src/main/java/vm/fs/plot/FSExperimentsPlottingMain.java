/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot;

import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosClassicData5Tech;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData3Tech;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData5Tech;

/**
 *
 * @author au734419
 */
public class FSExperimentsPlottingMain {

    public static final Boolean PLOT_ONLY_SVG = true;
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData5Tech = new PlotPtolemaiosRandomData5Tech(PLOT_ONLY_SVG);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData3Tech = new PlotPtolemaiosRandomData3Tech(PLOT_ONLY_SVG);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosClassicData5Tech = new PlotPtolemaiosClassicData5Tech(PLOT_ONLY_SVG);

    public static void main(String[] args) {
        Y2024_PlotPtolemaiosRandomData5Tech.makePlots();
        Y2024_PlotPtolemaiosRandomData3Tech.makePlots();
        Y2024_PlotPtolemaiosClassicData5Tech.makePlots();
    }
}
