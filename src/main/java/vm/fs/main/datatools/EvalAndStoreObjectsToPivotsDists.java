package vm.fs.main.datatools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.metricspace.AbstractMetricSpace;
import vm.metricspace.Dataset;
import vm.metricspace.distance.DistanceFunctionInterface;

/**
 *
 * @author Vlada
 */
public class EvalAndStoreObjectsToPivotsDists {

    public static final Logger LOGGER = Logger.getLogger(EvalAndStoreObjectsToPivotsDists.class.getName());

    public static void main(String[] args) throws SQLException, FileNotFoundException, InterruptedException {
        Dataset dataset;
        dataset = new FSDatasetInstanceSingularizator.MPEG7dataset();
        dataset = new FSDatasetInstanceSingularizator.RandomDataset20Uniform();
        dataset = new FSDatasetInstanceSingularizator.SIFTdataset();
        dataset = new FSDatasetInstanceSingularizator.DeCAFDataset();
        int pivotCount = 512;
        String output = "h:\\Similarity_search\\DistsToPivots\\" + dataset.getDatasetName() + "_" + pivotCount + "pivots.csv.gz";
        GZIPOutputStream outputStream = null;
        AbstractMetricSpace metricSpace = dataset.getMetricSpace();
        List pivots = dataset.getPivotsForTheSameDataset(pivotCount);
        Iterator objects = dataset.getMetricObjectsFromDataset();
        DistanceFunctionInterface df = dataset.getDistanceFunction();
        try {
            outputStream = new GZIPOutputStream(new FileOutputStream(output), true);
            outputStream.write(';');
            for (int i = 0; i < pivots.size(); i++) {
                String pId = metricSpace.getIDOfMetricObject(pivots.get(i)).toString();
                outputStream.write(pId.getBytes());
                outputStream.write(';');
            }
            outputStream.write('\n');
            for (int i = 1; objects.hasNext(); i++) {
                Object o = objects.next();
                String oId = metricSpace.getIDOfMetricObject(o).toString();
                outputStream.write(oId.getBytes());
                outputStream.write(';');
                Object oData = metricSpace.getDataOfMetricObject(o);
                for (int j = 0; j < pivots.size(); j++) {
                    Object p = pivots.get(j);
                    Object pData = metricSpace.getDataOfMetricObject(p);
                    float distance = df.getDistance(oData, pData);
                    outputStream.write(Float.toString(distance).getBytes());
                    outputStream.write(';');
                }
                outputStream.write('\n');
                if (i % 10000 == 0) {
                    LOGGER.log(Level.INFO, "Evaluated {0} objects", i);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

    }

    private static void processBatch(AbstractMetricSpace metricSpace, Iterator<Object> objects, List<Object> pivots, GZIPOutputStream outputStream, DistanceFunctionInterface df, CountDownLatch latch, ConcurrentLinkedQueue<String> queue, ExecutorService threadPool) throws InterruptedException, IOException {
        while (objects.hasNext()) {
            final Object next = objects.next();
            final Object nextID = metricSpace.getIDOfMetricObject(next);
            final Object nextData = metricSpace.getDataOfMetricObject(next);

            threadPool.execute(() -> {
                StringBuilder sb = new StringBuilder();
                sb.append(nextID.toString()).append(":");
                for (Object pivot : pivots) {
                    Object pivotID = metricSpace.getIDOfMetricObject(pivot);
                    Object pivotData = metricSpace.getDataOfMetricObject(pivot);
                    float dist = df.getDistance(nextData, pivotData);
                    sb.append(pivotID.toString()).append(",").append(dist).append(",");
                }
                sb.append('\n');
                queue.add(sb.toString());
                latch.countDown();
            });
            while (!queue.isEmpty()) {
                String poll = queue.poll();
                outputStream.write(poll.getBytes());
            }
        }
    }
}
