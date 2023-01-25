package vm.fs.store.queryResults.recallEvaluation;

import java.util.Map;
import vm.fs.store.queryResults.FSQueryExecutionStatsStoreImpl;
import vm.queryResults.recallEvaluation.RecallOfCandsSetsStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSRecallOfCandidateSetsStorageImpl extends FSQueryExecutionStatsStoreImpl implements RecallOfCandsSetsStoreInterface {

    /**
     *
     * @param attributesForFileName: ground_truth_name,
     * ground_truth_query_set_name, ground_truth_nn_count, cand_set_name,
     * cand_set_query_set_name, storing_result_name. Voluntary:
     * cand_set_fixed_size
     */
    public FSRecallOfCandidateSetsStorageImpl(Map<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> attributesForFileName) {
        super(attributesForFileName);
    }

    @Override
    public void storeRecallForQuery(Object queryObjId, float recall, Object... additionalParametersToStore) {
        String[] line = content.get(queryObjId.toString());
        int order = statsComp.getOrder(QUERY_STATS.recall);
        line[order] = Float.toString(recall);
        if (additionalParametersToStore[0] != null) {
            String candidateNNCount = additionalParametersToStore[0].toString();
            order = statsComp.getOrder(QUERY_STATS.cand_set_dynamic_size);
            line[order] = candidateNNCount;
        }
    }

}
