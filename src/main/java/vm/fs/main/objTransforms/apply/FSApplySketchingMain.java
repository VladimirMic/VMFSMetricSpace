package vm.fs.main.objTransforms.apply;

import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.metricSpace.Dataset;
import vm.objTransforms.perform.AbstractObjectToSketchTransformator;
import vm.objTransforms.perform.SketchingGHP;

/**
 *
 * @author Vlada
 */
public class FSApplySketchingMain {

    public static void main(String[] args) {
        Dataset dataset = new FSDatasetInstanceSingularizator.DeCAFDataset();
        float balance = 0.5f;
        int[] sketchesLengths = new int[]{256, 192, 128, 64, 512};
        // potrebuju skecovaci techiku abych dostal nazev a pritom ji nemam dokud nedostanu csv soubor pojmenovany dle nazvu
        // normalni pivoty a ty pak az nahrat a predelat na zaklade csv
        AbstractObjectToSketchTransformator sketchingTechnique = new SketchingGHP();

        for (int sketchLength : sketchesLengths) {
            String sketchesName = sketchingTechnique.deriveResultName(dataset.getDatasetName(), sketchLength, balance);
        }
    }
}
