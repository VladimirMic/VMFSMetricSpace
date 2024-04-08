/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot;

import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosDeCAF1M5Tech;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosTransformedClips5Tech;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData4Tech10_40Time;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData4Tech50_100Time;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData5Tech10_40;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomData5Tech50_100;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosRandomDataPivotSelectionForPtolemaios;
import vm.fs.plot.implForPapers.p2024PtolemaiosLimited.PlotPtolemaiosTransformedClipsAbblation;

/**
 *
 * @author au734419
 */
public class FSExperimentsPlottingMain {

    public static final Boolean PLOT_ONLY_PDF = true;
    
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData5Tech10_40 = new PlotPtolemaiosRandomData5Tech10_40(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData4Tech10_40_Time = new PlotPtolemaiosRandomData4Tech10_40Time(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData5Tech50_100 = new PlotPtolemaiosRandomData5Tech50_100(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosRandomData4Tech50_100_Time = new PlotPtolemaiosRandomData4Tech50_100Time(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosTransformedClips5Tech = new PlotPtolemaiosTransformedClips5Tech(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosDeCAF1M5Tech = new PlotPtolemaiosDeCAF1M5Tech(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosPivotSelection = new PlotPtolemaiosRandomDataPivotSelectionForPtolemaios(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotPtolemaiosCLIP_AbblationStudy = new PlotPtolemaiosTransformedClipsAbblation(PLOT_ONLY_PDF);

    public static void main(String[] args) {
//        Y2024_PlotPtolemaiosRandomData5Tech10_40.makePlots();
//        Y2024_PlotPtolemaiosRandomData5Tech50_100.makePlots();
//        Y2024_PlotPtolemaiosRandomData4Tech10_40_Time.makePlots();
//        Y2024_PlotPtolemaiosRandomData4Tech50_100_Time.makePlots();
//        Y2024_PlotPtolemaiosTransformedClips5Tech.makePlots();
//        Y2024_PlotPtolemaiosDeCAF1M5Tech.makePlots();
//        Y2024_PlotPtolemaiosPivotSelection.makePlots();
        Y2024_PlotPtolemaiosCLIP_AbblationStudy.makePlots();
    }
}
