/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosDeCAF1M5Tech extends PlotPtolemaiosRandomData5Tech {

    public PlotPtolemaiosDeCAF1M5Tech(boolean plotOnlySvg) {
        super(plotOnlySvg);
    }

    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return strings(
                "DeCAF_1M"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return strings(
                "decaf_1m__decaf_1m__30__decaf_1m__decaf_1m__"
        );
    }

    @Override
    public String getXAxisLabel() {
        return "Dimensionality";
    }

}
