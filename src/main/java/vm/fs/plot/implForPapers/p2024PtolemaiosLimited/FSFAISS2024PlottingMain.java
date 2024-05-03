/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

import vm.fs.plot.FSAbstractPlotterFromResults;

/**
 *
 * @author Vlada
 */
public class FSFAISS2024PlottingMain {

    public static final Boolean PLOT_ONLY_PDF = true;

    public static final FSAbstractPlotterFromResults Y2024_PlotFaissCLIPIndexes = new PlotFAISSCLIPIndexes2024(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotFaissCLIPConfig = new PlotFAISSCLIPIndexConfig2024(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotFaissDeCAFConfig = new PlotFAISSDeCAFIndexConfig2024(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotFaissDeCAFSimulatedCandSetSizes = new PlotFAISSDeCAFSimulatedCandSetSizes2024(PLOT_ONLY_PDF);
    public static final FSAbstractPlotterFromResults Y2024_PlotFaissCLIPSimulatedCandSetSizes = new PlotFAISSCLIPSimulatedCandSetSizes2024(PLOT_ONLY_PDF);

    public static void main(String[] args) {
//        Y2024_PlotFaissCLIPIndexes.makePlots();
//        Y2024_PlotFaissCLIPConfig.makePlots();
//        Y2024_PlotFaissDeCAFConfig.makePlots();
        Y2024_PlotFaissDeCAFSimulatedCandSetSizes.makePlots();
        Y2024_PlotFaissCLIPSimulatedCandSetSizes.makePlots();
    }

}
