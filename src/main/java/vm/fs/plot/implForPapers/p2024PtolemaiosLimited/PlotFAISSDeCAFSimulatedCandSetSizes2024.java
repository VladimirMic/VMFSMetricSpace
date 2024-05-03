/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

import vm.fs.plot.FSAbstractPlotterFromResults;
import vm.fs.plot.FSPlotFolders;
import vm.plot.AbstractPlotter;
import vm.plot.impl.BoxPlotPlotter;

/**
 *
 * @author Vlada
 */
public class PlotFAISSDeCAFSimulatedCandSetSizes2024 extends FSAbstractPlotterFromResults {

    public PlotFAISSDeCAFSimulatedCandSetSizes2024(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfTracesThatMatchesFolders() {
        return strings(
                "faiss-100M_DeCAF_PCA256-IVFPQ-tr1000000-cc262144-m32-nbits8-qc1000-k10000"
        );
    }

    @Override
    protected AbstractPlotter.COLOUR_NAMES[] getVoluntaryColoursForTracesOrNull() {
        return null;
    }

    @Override
    public String[] getFolderNamesForDisplayedTraces() {
        return strings(
                "faiss-100M_DeCAF_PCA256-IVFPQ-tr1000000-cc262144-m32-nbits8-qc1000-k10000"
        );
    }

    private final Integer[] kCands = new Integer[]{300, 350, 400, 450, 500, 550, 600, 650, 700, 750};

    @Override
    public Object[] getDisplayedNamesOfGroupsThatMeansFiles() {
        String[] ret = new String[3 * kCands.length];
        for (int i = 0; i < kCands.length; i++) {
            ret[3 * i] = "nprobe64_" + kCands[i] + "Cands";
            ret[3 * i + 1] = "nprobe128_" + kCands[i] + "Cands";
            ret[3 * i + 2] = "nprobe256_" + kCands[i] + "Cands";
        }
        return ret;
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        String[] ret = new String[3 * kCands.length];
        for (int i = 0; i < kCands.length; i++) {
            ret[3 * i] = "nprobe64____" + kCands[i] + "__";
            ret[3 * i] = "nprobe128____" + kCands[i] + "__";
            ret[3 * i] = "nprobe256____" + kCands[i] + "__";
        }
        return ret;
    }

    @Override
    public String getXAxisLabel() {
        return null;
    }

    @Override
    public AbstractPlotter getPlotter() {
        return new BoxPlotPlotter();
    }

    @Override
    public String getResultName() {
        return "FAISS";
    }

    @Override
    public String getFolderForPlots() {
        return FSPlotFolders.Y2024_PTOLEMAIOS_LIMITED;
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return null;
    }

    @Override
    protected String getYAxisNameForAdditionalParams() {
        return null;
    }

}
