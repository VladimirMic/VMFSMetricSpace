package vm.fs.store.queryResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.JOptionPane;
import vm.datatools.Tools;
import vm.fs.FSGlobal;
import vm.queryResults.QueryNearestNeighboursStoreInterface;

/**
 *
 * @author Vlada
 */
public class FSNearestNeighboursStorageImpl extends QueryNearestNeighboursStoreInterface {

    private static final Logger LOG = Logger.getLogger(FSNearestNeighboursStorageImpl.class.getName());

    private File getFileForGroundTruth(String resultsName, String datasetName, String querySetName) {
        return new File(FSGlobal.GROUND_TRUTH_FOLDER, resultsName + "_" + datasetName + "_" + querySetName + ".gz");
    }

    private File getFileForResults(String resultsName, String datasetName, String querySetName) {
        File ret = new File(FSGlobal.RESULT_FOLDER);
        ret.mkdirs();
        ret = new File(ret, resultsName + "_" + datasetName + "_" + querySetName + ".gz");
        LOG.log(Level.INFO, "File for results: " + ret.getAbsolutePath());
        return ret;
    }

    @Override
    public void storeQueryResults(List<Object> queryObjectsIDs, TreeSet<Map.Entry<Object, Float>>[] queryResults, String datasetName, String querySetName, String resultsName) {
        GZIPOutputStream datasetOutputStream = null;
        try {
            checkAndAskForResultsExistence(datasetName, querySetName, resultsName);
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(getFileForResults(resultsName, datasetName, querySetName), false), true);
            for (int i = 0; i < queryObjectsIDs.size(); i++) {
                if (queryResults[i] == null) {
                    continue;
                }
                String queryId = queryObjectsIDs.get(i).toString();
                store(datasetOutputStream, queryId, queryResults[i]);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                datasetOutputStream.flush();
                datasetOutputStream.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    private boolean ask = true;

    @Override
    public void storeQueryResult(Object queryObjectID, TreeSet<Map.Entry<Object, Float>> queryResults, String datasetName, String querySetName, String resultsName) {
        GZIPOutputStream datasetOutputStream = null;
        try {
            if (ask) {
                checkAndAskForResultsExistence(datasetName, querySetName, resultsName);
            }
            ask = false;
            datasetOutputStream = new GZIPOutputStream(new FileOutputStream(getFileForGroundTruth(resultsName, datasetName, querySetName), true), true);
            String queryId = queryObjectID.toString();
            store(datasetOutputStream, queryId, queryResults);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                datasetOutputStream.flush();
                datasetOutputStream.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void checkAndAskForResultsExistence(String datasetName, String querySetName, String resultsName) {
        Object[] options = new String[]{"Yes", "No"};
        if (existResultSetSpace(datasetName, querySetName, resultsName)) {
            LOG.log(Level.WARNING, "Asking for a question, waiting for the reply");
            String question = "Storing space for result set " + resultsName + " on the dataset " + datasetName + " and query set " + querySetName + " already exists. Do you want to delete results in it? Answer no causes immediate stop.";
            int add = JOptionPane.showOptionDialog(null, question, "New query results?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.NO_OPTION);
            if (add == 1) {
                System.exit(1);
            }
        }
        LOG.log(Level.INFO, "Ok, continuing");
    }

    public boolean existResultSetSpace(String datasetName, String querySetName, String resultsName) {
        File fileForResults = getFileForResults(resultsName, datasetName, querySetName);
        return fileForResults.exists();
    }

    @Override
    public Map<String, TreeSet<Map.Entry<Object, Float>>> getQueryResultsForDataset(String queryResultsName, String datasetName, String querySetName) {
        try {
            Map<String, TreeSet<Map.Entry<Object, Float>>> ret = new HashMap<>();
            File file = getFileForResults(queryResultsName, datasetName, querySetName);
            if (!file.exists()) {
                LOG.log(Level.SEVERE, "The file with the results does not exist: " + file.getAbsolutePath());
                return ret;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            String line = br.readLine();
            while (line != null) {
                String[] pairsOfNearestNeighbours = line.split(";");
                String queryObjId = pairsOfNearestNeighbours[0];
                TreeSet<Map.Entry<Object, Float>> nearestNeighbours = new TreeSet<>(new Tools.MapByValueComparator());
                for (int i = 1; i < pairsOfNearestNeighbours.length; i++) {
                    String nearestNeighbourPair = pairsOfNearestNeighbours[i];
                    if (nearestNeighbourPair.isEmpty()) {
                        continue;
                    }
                    String[] idDistPair = nearestNeighbourPair.split(":");
                    nearestNeighbours.add(new AbstractMap.SimpleEntry<>(idDistPair[0], Float.parseFloat(idDistPair[1])));
                }
                ret.put(queryObjId, nearestNeighbours);
                line = br.readLine();
            }
            return ret;
        } catch (IOException ex) {
            Logger.getLogger(FSNearestNeighboursStorageImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void store(GZIPOutputStream datasetOutputStream, String queryId, TreeSet<Map.Entry<Object, Float>> queryResult) throws IOException {
        StringBuilder buffer = new StringBuilder(queryResult.size() * 16);
        buffer.append(queryId);
        buffer.append(";");
        Iterator<Map.Entry<Object, Float>> it = queryResult.iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Float> nn = it.next();
            buffer.append(nn.getKey().toString());
            buffer.append(":");
            buffer.append(nn.getValue().toString());
            buffer.append(";");
        }
        datasetOutputStream.write(buffer.toString().getBytes());
        datasetOutputStream.write('\n');
    }

}
