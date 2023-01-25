package vm.fs.store.queryResults;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import vm.datatools.DataTypeConvertor;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.queryResults.QueryExecutionStatsStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSQueryExecutionStatsStoreImpl implements QueryExecutionStatsStoreInterface {

    private static final Logger LOG = Logger.getLogger(FSQueryExecutionStatsStoreImpl.class.getName());
    protected final StatsAttributesComparator statsComp = new StatsAttributesComparator();
    private final File output;
    protected final Map<String, String[]> content;

    public static enum DATA_NAMES_IN_FILE_NAME {
        ground_truth_name, ground_truth_query_set_name, ground_truth_nn_count,
        cand_set_name, cand_set_query_set_name, storing_result_name,
        cand_set_fixed_size
    }

    public static enum QUERY_STATS {
        query_obj_id, recall,
        cand_set_dynamic_size, query_execution_time,
        additional_stats
    }

    public FSQueryExecutionStatsStoreImpl(String groundTruthName, String groundTruthQuerySetName, int groundTruthNNCount,
            String candSetName, String candSetQuerySetName, String resultName,
            Integer candSetFixedSize) {
        Map<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> attributesForFileName = new HashMap<>();
        attributesForFileName.put(DATA_NAMES_IN_FILE_NAME.ground_truth_name, groundTruthName);
        attributesForFileName.put(DATA_NAMES_IN_FILE_NAME.ground_truth_query_set_name, groundTruthQuerySetName);
        attributesForFileName.put(DATA_NAMES_IN_FILE_NAME.ground_truth_nn_count, Integer.toString(groundTruthNNCount));
        attributesForFileName.put(DATA_NAMES_IN_FILE_NAME.cand_set_name, candSetName);
        attributesForFileName.put(DATA_NAMES_IN_FILE_NAME.cand_set_query_set_name, candSetQuerySetName);
        attributesForFileName.put(DATA_NAMES_IN_FILE_NAME.storing_result_name, resultName);
        if (candSetFixedSize != null) {
            attributesForFileName.put(DATA_NAMES_IN_FILE_NAME.cand_set_fixed_size, candSetFixedSize.toString());
        }
        output = getFileForStats(attributesForFileName);
        content = parseAsMap();
    }

    /**
     *
     * @param attributesForFileName: ground_truth_name,
     * ground_truth_query_set_name, ground_truth_nn_count, cand_set_name,
     * cand_set_query_set_name, storing_result_name. Voluntary:
     * cand_set_fixed_size
     */
    public FSQueryExecutionStatsStoreImpl(Map<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> attributesForFileName) {
        output = getFileForStats(attributesForFileName);
        content = parseAsMap();
    }

    @Override
    public void storeStatsForQuery(Object queryObjId, Integer distanceComputationsCount, long time, Object... additionalParametersToStore) {
        TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String> treeMap = new TreeMap(new StatsAttributesComparator());
        treeMap.put(QUERY_STATS.query_obj_id, queryObjId.toString());
        treeMap.put(QUERY_STATS.cand_set_dynamic_size, Integer.toString(distanceComputationsCount));
        if (time != -1) {
            treeMap.put(QUERY_STATS.query_execution_time, Long.toString(time));
        } else {
            treeMap.put(QUERY_STATS.query_execution_time, "null");
        }
        if (additionalParametersToStore != null && additionalParametersToStore.length != 0) {
            String additionalStats = DataTypeConvertor.objectsToString(additionalParametersToStore, ";");
            treeMap.put(QUERY_STATS.additional_stats, additionalStats);
        } else {
            treeMap.put(QUERY_STATS.additional_stats, "null");
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<QUERY_STATS, String> entry : treeMap.entrySet()) {
            sb.append(entry.getValue()).append(";");
        }
        String line = sb.toString();
        content.put(queryObjId.toString(), line.split(";"));
    }

    public void saveFile() {
        BufferedWriter bw = null;
        try {
            GZIPOutputStream datasetOutputStream = new GZIPOutputStream(new FileOutputStream(output, false), true);
            bw = new BufferedWriter(new OutputStreamWriter(datasetOutputStream));
            for (String[] line : content.values()) {
                TreeMap<QUERY_STATS, String> map = dataArrayToMap(line);
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<QUERY_STATS, String> entry : map.entrySet()) {
                    sb.append(entry.getValue()).append(";");
                }
                try {
                    bw.write(sb.toString());
                    bw.write("\n");
                } catch (IOException ex) {
                    Logger.getLogger(FSQueryExecutionStatsStoreImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            bw.write(bw.toString());
            bw.write("\n");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.flush();
                bw.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * returns list of rows. Each row is represented by a map where the first
     * column (representing the queryObjId) is the key, and valu is to content
     * of the row
     *
     * @return
     */
    public final Map<String, String[]> parseAsMap() {
        Map<String, String[]> ret = new HashMap<>();
        if (!output.exists()) {
            return ret;
        }
        List<String[]> values = Tools.parseCsvRowOriented(output.getAbsolutePath(), ";");
        Iterator<String[]> it = values.iterator();
        while (it.hasNext()) {
            String[] next = it.next();
            for (int i = 0; i < next.length; i++) {
                ret.put(next[0], next);
            }
        }
        return ret;
    }

    protected TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String> dataArrayToMap(String[] array) {
        TreeMap<FSQueryExecutionStatsStoreImpl.QUERY_STATS, String> ret = new TreeMap<>(statsComp);
        for (int i = 0; i < array.length; i++) {
            QUERY_STATS key = statsComp.indexToStats(i);
            String string = array[i];
            ret.put(key, string);
        }
        return ret;
    }

    public final File getFileForStats(Map<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> dataNamesInFileName) {
        TreeMap<FSQueryExecutionStatsStoreImpl.DATA_NAMES_IN_FILE_NAME, String> treeMap = new TreeMap(new FileNameAttributesComparator());
        treeMap.putAll(dataNamesInFileName);
        StringBuilder path = new StringBuilder();
        for (Map.Entry<DATA_NAMES_IN_FILE_NAME, String> entry : treeMap.entrySet()) {
            path.append(entry.getValue()).append("_");
        }
        File f = new File(FSGlobal.RESULT_STATS_FOLDER);
        String fileName = path.toString();
        f.mkdirs();
        LOG.log(Level.INFO, "Folder: " + f.getAbsolutePath() + ", file: " + fileName);
        return new File(f, fileName + ".gz");

    }

    private class FileNameAttributesComparator implements Comparator<DATA_NAMES_IN_FILE_NAME> {

        @Override
        public int compare(DATA_NAMES_IN_FILE_NAME o1, DATA_NAMES_IN_FILE_NAME o2) {
            int order1 = getOrder(o1);
            int order2 = getOrder(o2);
            return Integer.compare(order1, order2);
        }

        private int getOrder(DATA_NAMES_IN_FILE_NAME o) {
            switch (o) {
                case ground_truth_name: {
                    return 0;
                }
                case ground_truth_query_set_name: {
                    return 1;
                }
                case ground_truth_nn_count: {
                    return 2;
                }
                case cand_set_name: {
                    return 3;
                }
                case cand_set_query_set_name: {
                    return 4;
                }
                case storing_result_name: {
                    return 5;
                }
                case cand_set_fixed_size: {
                    return 6;
                }
            }
            return -1;
        }
    }

    public class StatsAttributesComparator implements Comparator<QUERY_STATS> {

        @Override
        public int compare(QUERY_STATS o1, QUERY_STATS o2) {
            int order1 = getOrder(o1);
            int order2 = getOrder(o2);
            return Integer.compare(order1, order2);
        }

        public QUERY_STATS indexToStats(int index) {
            switch (index) {
                case 0: {
                    return QUERY_STATS.query_obj_id;
                }
                case 1: {
                    return QUERY_STATS.recall;
                }
                case 2: {
                    return QUERY_STATS.cand_set_dynamic_size;
                }
                case 3: {
                    return QUERY_STATS.query_execution_time;
                }
                case 4: {
                    return QUERY_STATS.additional_stats;
                }
            }
            return null;
        }

        public int getOrder(QUERY_STATS o) {
            switch (o) {
                case query_obj_id: {
                    return 0;
                }
                case recall: {
                    return 1;
                }
                case cand_set_dynamic_size: {
                    return 2;
                }
                case query_execution_time: {
                    return 3;
                }
                case additional_stats: {
                    return 4;
                }
            }
            return -1;
        }
    }

}
