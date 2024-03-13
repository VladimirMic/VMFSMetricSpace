/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.plot.implForPapers.p2024PtolemaiosLimited;

/**
 *
 * @author au734419
 */
public class PlotPtolemaiosAllClassic extends PlotPtolemaiosRandomData {

    public PlotPtolemaiosAllClassic(boolean plotOnlySvg) {
        super(plotOnlySvg);
    }
    @Override
    public String[] getDisplayedNamesOfGroupsThatMeansFiles() {
        return array(
                "10",
                "15",
                "20",
                "25",
                "30",
                "35",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
        );
    }

    @Override
    public String[] getUniqueArtifactIdentifyingFileNameForDisplaydGroup() {
        return array(
                "10",
                "15",
                "20",
                "25",
                "30",
                "35",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
        );
    }

    @Override
    public String getResultName() {
        return "Filterings";
    }

}
