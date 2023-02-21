package vm.fs.store.dataTransforms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import vm.fs.FSGlobal;
import vm.metricSpace.dataToStringConvertors.SingularisedConvertors;
import vm.objTransforms.storeLearned.SVDStoreInterface;

/**
 *
 * @author Vlada
 */
public class TODOFSSVDStorageImpl implements SVDStoreInterface {

    private static final Logger LOG = Logger.getLogger(TODOFSSVDStorageImpl.class.getName());
    private File output;

    public TODOFSSVDStorageImpl(String datasetName, int sampleCount) {
        output = getFileWithSVD(datasetName, Integer.toString(sampleCount));
    }

    @Override
    public void storeSVD(float[] meansOverColumns, float[] singularValues, float[][] matrixU, float[][] matrixVT, Object... additionalInfoToStoreWithPCA) {
        BufferedWriter bw = null;
        try {
            GZIPOutputStream datasetOutputStream = new GZIPOutputStream(new FileOutputStream(output, true), true);
            bw = new BufferedWriter(new OutputStreamWriter(datasetOutputStream));

            bw.write("Means\n");
            bw.write(SingularisedConvertors.FLOAT_VECTOR_SPACE.metricObjectDataToString(meansOverColumns));
            bw.write("\n");
            LOG.log(Level.INFO, "Stored sizes of arrays: meansOverColumns: {0}", meansOverColumns.length);

            bw.write("Singular values\n");
            bw.write(SingularisedConvertors.FLOAT_VECTOR_SPACE.metricObjectDataToString(singularValues));
            bw.write("\n");
            LOG.log(Level.INFO, "Stored sizes of arrays: singularValues: {0}", singularValues.length);

            bw.write("matrixVT\n");
            bw.write(SingularisedConvertors.FLOAT_MATRIX_SPACE.metricObjectDataToString(matrixVT));
            bw.write("\n");
            LOG.log(Level.INFO, "Stored sizes of arrays: matrixVT: {0} * {1}", new Object[]{matrixVT.length, matrixVT[0].length});

            bw.write("matrixU\n");
            bw.write(SingularisedConvertors.FLOAT_MATRIX_SPACE.metricObjectDataToString(matrixU));
            bw.write("\n");
            LOG.log(Level.INFO, "Stored sizes of arrays: matrixU: {0} * {1}", new Object[]{matrixU.length, matrixU[0].length});

            LOG.log(Level.INFO, "Stored");
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

    private Map<String, String> fileContent = null;

    private Map<String, String> parseFile() {
        // TODO
        return null;
    }

    @Override
    public float[][] getVTMatrix(Object... params) {
        if (fileContent == null) {
            fileContent = parseFile();
        }
        String matrixAsString = fileContent.get("matrixVT");
        return SingularisedConvertors.FLOAT_MATRIX_SPACE.parseDBString(matrixAsString);
    }

    @Override
    public float[][] getUMatrix(Object... params) {
        if (fileContent == null) {
            fileContent = parseFile();
        }
        String matrixAsString = fileContent.get("matrixU");
        return SingularisedConvertors.FLOAT_MATRIX_SPACE.parseDBString(matrixAsString);
    }

    @Override
    public float[] getSingularValues(Object... params) {
        if (fileContent == null) {
            fileContent = parseFile();
        }
        String matrixAsString = fileContent.get("Singular values");
        return SingularisedConvertors.FLOAT_VECTOR_SPACE.parseDBString(matrixAsString);
    }

    @Override
    public float[] getMeansOverColumns(Object... params) {
        if (fileContent == null) {
            fileContent = parseFile();
        }
        String matrixAsString = fileContent.get("Means");
        return SingularisedConvertors.FLOAT_VECTOR_SPACE.parseDBString(matrixAsString);
    }

    public final File getFileWithSVD(String datasetName, String sampleCount) {
        File f = new File(FSGlobal.AUXILIARY_FOR_SVD_TRANSFORMS);
        f.mkdirs();
        String fileName = datasetName + "_" + sampleCount;
        LOG.log(Level.INFO, "Folder: " + f.getAbsolutePath() + ", file: " + fileName);
        return new File(f, fileName + ".gz");
    }

}
