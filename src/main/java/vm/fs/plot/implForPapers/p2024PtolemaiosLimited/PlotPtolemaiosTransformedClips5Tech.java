/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosTransformedClips5Tech extends PlotPtolemaiosRandomData5Tech10_40 {

    public PlotPtolemaiosTransformedClips5Tech(boolean plotOnlySvg) {
        super(plotOnlySvg);
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return strings(
                "CLIP_10M_GHP512",
                "CLIP_10M_PCA256"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "laion2B-en-clip768v2-n=10M.h5_GHP_50_512__public-queries-10k-clip768v2.h5_GHP_50_512__30__laion2B-en-clip768v2-n=10M.h5_GHP_50_512__public-queries-10k-clip768v2.h5_GHP_50_512__",
                "laion2B-en-clip768v2-n=10M.h5_PCA256__laion2B-en-clip768v2-n=10M.h5_PCA256__30__laion2B-en-clip768v2-n=10M.h5_PCA256__laion2B-en-clip768v2-n=10M.h5_PCA256__"
        );
    }

    @Override
    protected Float transformAdditionalStatsForQueryToFloat(float firstValue) {
        return firstValue / 10120191;
    }

}
