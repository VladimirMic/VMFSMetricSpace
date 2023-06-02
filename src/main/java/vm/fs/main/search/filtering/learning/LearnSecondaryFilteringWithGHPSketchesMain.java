/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.main.search.filtering.learning;

import java.io.File;
import java.util.logging.Logger;
import vm.fs.FSGlobal;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.fs.store.filtering.FSSecondaryFilteringWithSketchesStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.bounding.nopivot.learning.LearningSecondaryFilteringWithSketches;
import vm.metricSpace.distance.bounding.nopivot.storeLearned.SecondaryFilteringWithSketchesStoreInterface;

/**
 *
 * @author Vlada
 */
public class LearnSecondaryFilteringWithGHPSketchesMain {

    public static final Logger LOG = Logger.getLogger(LearnSecondaryFilteringWithGHPSketchesMain.class.getName());
    public static void main(String[] args) {
        Dataset[] fullDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAFDataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_Dataset(),
//            new FSDatasetInstanceSingularizator.LAION_30M_Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_Dataset()
        };
        Dataset[] sketchesDatasets = new Dataset[]{
            new FSDatasetInstanceSingularizator.DeCAF_GHP_50_64Dataset(),
            new FSDatasetInstanceSingularizator.LAION_10M_GHP_50_512Dataset(),
//            new FSDatasetInstanceSingularizator.LAION_30M_GHP_50_128Dataset(),
            new FSDatasetInstanceSingularizator.LAION_100M_GHP_50_512Dataset()
        };
        float[] distIntervalsForPX = new float[]{
            2,
            0.004f,
            0.004f,
            0.004f,
            0.004f,
            0.004f,
            0.004f
        };
        float[] maxDistsOnFullDataset = new float[]{
            250f,
            2f,
            2f,
            2f,
            2f
        };
        int sketchLength = 512;
        for (int i = 1; i < sketchesDatasets.length; i++) {
            Dataset fullDataset = fullDatasets[i];
            Dataset sketchesDataset = sketchesDatasets[i];
            float distIntervalForPX = distIntervalsForPX[i];
            float maxDistOnFullDataset = maxDistsOnFullDataset[i];
            run(fullDataset, sketchesDataset, distIntervalForPX, sketchLength, maxDistOnFullDataset);
        }
    }

    private static void run(Dataset fullDataset, Dataset sketchesDataset, float distIntervalForPX, int sketchLength, float maxDistOnFullDataset) {
        SecondaryFilteringWithSketchesStoreInterface storage = new FSSecondaryFilteringWithSketchesStorage();
        //iDim file
        File fileOutputForIDim = new File(FSGlobal.SECONDARY_FILTERING_WITH_SKETCHES_AUXILIARY,
                "iDim_" + sketchesDataset.getDatasetName() + "_"
                + LearningSecondaryFilteringWithSketches.SKETCHES_SAMPLE_COUNT_FOR_IDIM_PX + "sk_"
                + LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX + "distsForIDIM_PX"
                + ".csv");
        fileOutputForIDim = FSGlobal.checkFileExistence(fileOutputForIDim, true);
        //px file
        File fileOutputForPX = new File(FSGlobal.SECONDARY_FILTERING_WITH_SKETCHES_AUXILIARY, "px_" + sketchesDataset.getDatasetName() + "_"
                + LearningSecondaryFilteringWithSketches.SKETCHES_SAMPLE_COUNT_FOR_IDIM_PX + "sk_"
                + LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX + "distsForIDIM_PX_"
                + distIntervalForPX + "px_interval"
                + ".csv");
        fileOutputForPX = FSGlobal.checkFileExistence(fileOutputForPX, true);
        //mapping file preffix
        LearningSecondaryFilteringWithSketches learning = new LearningSecondaryFilteringWithSketches(
                storage,
                fullDataset,
                sketchesDataset,
                fileOutputForIDim,
                distIntervalForPX,
                maxDistOnFullDataset,
                sketchLength,
                fileOutputForPX
        );
        learning.execute();
    }
}