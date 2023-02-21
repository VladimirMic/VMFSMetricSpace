package vm.fs;

/**
 *
 * @author xmic
 */
public class FSGlobal {

    public static final String ROOT_FOLDER_PATH = "h:\\Similarity_search\\";
//    public static final String ROOT_FOLDER_PATH = "c:\\Data\\Similarity_search\\";

    public static final String TRIALS_FOLDER = ROOT_FOLDER_PATH + "Trials\\";

    public static final String DATA_FOLDER = ROOT_FOLDER_PATH + "Dataset\\";
    public static final String DATASET_FOLDER = DATA_FOLDER + "Dataset\\";
    public static final String PIVOT_FOLDER = DATA_FOLDER + "Pivot\\";
    public static final String QUERY_FOLDER = DATA_FOLDER + "Query\\";
    public static final String PRECOMPUTED_DISTS_FOLDER = DATA_FOLDER + "DistsToPivots";

    public static final String RESULT_FOLDER = ROOT_FOLDER_PATH + "Results\\";
    public static final String RESULT_STATS_FOLDER = "Processed_stats\\";
    public static final String GROUND_TRUTH_FOLDER = RESULT_FOLDER + "Ground_truth\\";

    public static final String AUXILIARY_FOR_DATA_TRANSFORMS = ROOT_FOLDER_PATH + "AuxiliaryDataForTransforms\\";
    public static final String AUXILIARY_FOR_SVD_TRANSFORMS = AUXILIARY_FOR_DATA_TRANSFORMS + "SVD\\";

    public static final String AUXILIARY_FOR_DATA_FILTERING = ROOT_FOLDER_PATH + "AuxiliaryDataForFiltering\\";
    public static final String AUXILIARY_FOR_TRIANGULAR_FILTERING_WITH_LIMITED_ANGLES = AUXILIARY_FOR_DATA_FILTERING + "Triangle_ineq_with_limited_angles\\";
    public static final String AUXILIARY_FOR_PTOLEMAIOS_WITH_LIMITED_ANGLES = AUXILIARY_FOR_DATA_FILTERING + "Ptolemaios_limited_angles\\";
    public static final String AUXILIARY_FOR_PTOLEMAIOS_COEFS_WITH_LIMITED_ANGLES = AUXILIARY_FOR_DATA_FILTERING + "Ptolemaios_limited_angles\\Simple_coefs";
}
