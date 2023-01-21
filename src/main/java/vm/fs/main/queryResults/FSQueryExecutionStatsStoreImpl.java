package vm.fs.main.queryResults;

import java.util.Map;
import java.util.logging.Logger;
import vm.queryResults.QueryExecutionStatsStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSQueryExecutionStatsStoreImpl implements QueryExecutionStatsStoreInterface {

    private static final Logger LOG = Logger.getLogger(FSQueryExecutionStatsStoreImpl.class.getName());

    public static enum DATA_NAMES_IN_FILE_NAME {
        ground_truth_name, ground_truth_query_set_name, ground_truth_nn_count,
        cand_set_name, cand_set_query_set_name, storing_result_name,
        cand_set_fixed_size
    };

    public static enum DATA_NAMES_IN_FILE {
        query_obj_id, recall,
        cand_set_dynamic_size, query_execution_time,
        additional_stats
    };

    private final Map<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> dataNamesInFileName;

    public FSQueryExecutionStatsStoreImpl(Map<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> dataNamesInFileName) {
//                + "query_obj_id,"
//                + "ground_truth_name,"
//                + "ground_truth_query_set_name,"
//                + "ground_truth_nn_count,"
//                + "cand_set_name,"
//                + "cand_set_query_set_name,"
//                + "cand_set_fixed_size,"
//                + "cand_set_dynamic_size,"
//                + "result_set_name,"
//                + "query_execution_time,"
//                + "additional_stats)"
//                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)"
//        );
        this.dataNamesInFileName = dataNamesInFileName;
    }

    @Override
    public void storeStatsForQuery(Object queryObjId, Integer distanceComputationsCount, Integer time, Object... additionalParametersToStore) {
//        try {
//            if (additionalParametersToStore == null) {
//                additionalParametersToStore = dataNamesInFileName;
//            } else {
//                additionalParametersToStore = Tools.concatArrays(dataNamesInFileName, additionalParametersToStore);
//            }
//            String groundTruthDatasetName = additionalParametersToStore[0].toString();
//            String groundTruthQuerySetName = additionalParametersToStore[1].toString();
//            int groundTruthNNCount = Integer.parseInt(additionalParametersToStore[2].toString());
//            String candSetName = additionalParametersToStore[3].toString();
//            String candSetQuerySetName = additionalParametersToStore[4].toString();
//            String resultSetName = additionalParametersToStore[5].toString();
//            Integer staticCandSetSize = additionalParametersToStore[6] == null ? null : Integer.parseInt(additionalParametersToStore[6].toString());
//            String additionalStats = additionalParametersToStore[7].toString();
//            stStoringStats.clearParameters();
//            stStoringStats.setString(1, queryObjId.toString());
//            stStoringStats.setString(2, groundTruthDatasetName);
//            stStoringStats.setString(3, groundTruthQuerySetName);
//            stStoringStats.setInt(4, groundTruthNNCount);
//            stStoringStats.setString(5, candSetName);
//            stStoringStats.setString(6, candSetQuerySetName);
//            if (staticCandSetSize == null) {
//                stStoringStats.setNull(7, java.sql.Types.NULL);
//            } else {
//                stStoringStats.setInt(7, staticCandSetSize);
//            }
//            stStoringStats.setInt(8, distanceComputationsCount);
//            stStoringStats.setString(9, resultSetName);
//            if (time == null) {
//                stStoringStats.setNull(10, java.sql.Types.NULL);
//            } else {
//                stStoringStats.setInt(10, time);
//            }
//            if (additionalStats == null) {
//                stStoringStats.setNull(11, java.sql.Types.NULL);
//            } else {
//                stStoringStats.setString(11, additionalStats);
//            }
//            stStoringStats.execute();
//        } catch (SQLException ex) {
//            LOG.log(Level.SEVERE, null, ex);
//        }
    }

}
