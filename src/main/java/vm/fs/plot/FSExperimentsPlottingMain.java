/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot;

import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosDeCAF1M5Tech;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosTransformedClips5Tech;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData3Tech10_40;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData3Tech50_100;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData5Tech10_40;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData5Tech50_100;

/**
 *
 * @author au734419
 */
public class FSExperimentsPlottingMain {

    public static final Boolean PLOT_ONLY_SVG = true;
    
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData5Tech10_40 = new PlotPtolemaiosRandomData5Tech10_40(PLOT_ONLY_SVG);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData3Tech10_40 = new PlotPtolemaiosRandomData3Tech10_40(PLOT_ONLY_SVG);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData5Tech50_100 = new PlotPtolemaiosRandomData5Tech50_100(PLOT_ONLY_SVG);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData3Tech50_100 = new PlotPtolemaiosRandomData3Tech50_100(PLOT_ONLY_SVG);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosTransformedClips5Tech = new PlotPtolemaiosTransformedClips5Tech(PLOT_ONLY_SVG);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosDeCAF1M5Tech = new PlotPtolemaiosDeCAF1M5Tech(PLOT_ONLY_SVG);

    public static void main(String[] args) {
        Y2024_PlotPtolemaiosRandomData5Tech10_40.makePlots();
        Y2024_PlotPtolemaiosRandomData3Tech10_40.makePlots();
        Y2024_PlotPtolemaiosRandomData5Tech50_100.makePlots();
        Y2024_PlotPtolemaiosRandomData3Tech50_100.makePlots();
        Y2024_PlotPtolemaiosTransformedClips5Tech.makePlots();
        Y2024_PlotPtolemaiosDeCAF1M5Tech.makePlots();
    }
}
