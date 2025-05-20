/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.impl.paper.devel.p2025ICDEPtolemaiosLimited;

import static vm.fs.plot.FSAbstractPlotterFromResults.strings;
import vm.fs.plot.impl.paper.used.p2024VLDBPtolemaiosLimited.PlotPtolemaiosClips10MPCA5Tech;
import vm.search.algorithm.SearchingAlgorithm;

/**
 *
 * @author Vlada
 */
public class PlotPtolemaiosClips30MPCA5Tech extends PlotPtolemaiosClips10MPCA5Tech {

    public PlotPtolemaiosClips30MPCA5Tech(boolean plotOnlyPDF) {
        super(plotOnlyPDF);
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return strings(
                "CLIP_30M_PCA256"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "laion2B-en-clip768v2-n=30M.h5_PCA256__laion2B-en-clip768v2-n=30M.h5_PCA256__30__laion2B-en-clip768v2-n=30M.h5_PCA256__laion2B-en-clip768v2-n=30M.h5_PCA256__"
        );
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / (30369256 - SearchingAlgorithm.K_IMPLICIT_FOR_QUERIES);
    }

}
