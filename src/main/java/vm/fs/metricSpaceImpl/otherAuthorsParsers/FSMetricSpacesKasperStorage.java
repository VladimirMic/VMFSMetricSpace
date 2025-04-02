/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.metricSpaceImpl.otherAuthorsParsers;

import java.io.File;
import java.util.AbstractMap;
import java.util.Iterator;
import vm.datatools.DataTypeConvertor;
import vm.fs.FSGlobal;
import vm.fs.dataset.FSDatasetInstanceSingularizator;
import vm.metricSpace.Dataset;
import vm.metricSpace.data.toStringConvertors.MetricObjectDataToStringInterface;
import vm.metricSpace.data.toStringConvertors.SingularisedConvertors;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.metricSpace.distance.impl.L2OnFloatsArray;

/**
 *
 * @author Vlada
 * @param <T>
 */
public class FSMetricSpacesKasperStorage<T> extends AbstractFSMetricSpacesStorageWithOthersDatasetStorage<T> {

    public FSMetricSpacesKasperStorage(DistanceFunctionInterface<T> df, MetricObjectDataToStringInterface<T> dataSerializator) {
        super(df, dataSerializator);
    }

    @Override
    protected File getFileForDataset(String datasetName) {
        int dimSqrt = parseDim(datasetName);
        String folderName = vm.mathtools.Tools.formatFirstZeros(dimSqrt * dimSqrt, 7) + "dim";
        File ret = new File(FSGlobal.DATASET_FOLDER, folderName);
        ret = new File(ret, datasetName + ".gz");
        return ret;
    }
///////////////////////////////// priprietary

    public static final Dataset<float[]> createDataset(int type, int dimSquareRooted) {
        if (type == 1 && dimSquareRooted == 512) {
            return null;
        }
        if (type == 1 && dimSquareRooted == 1024) {
            return null;
        }
        String name = null;
        switch (type) {
            case 0:
                name = "misfit_Bi5d_full_h5_" + dimSquareRooted + "_dct.txt";
                break;
            case 1:
                name = "gr_flake_full_h5_" + dimSquareRooted + "_dct.txt";
                break;
            case 2:
                name = "misfit_Se3d_" + dimSquareRooted + "_h5_dct.txt";
                break;
            case 3:
                name = "misfit_VB_" + dimSquareRooted + "_h5_dct.txt";
                break;
            default:
                throw new IllegalArgumentException("Type: " + type);
        }
        FSMetricSpacesKasperStorage storage = new FSMetricSpacesKasperStorage(new L2OnFloatsArray(), SingularisedConvertors.FLOAT_VECTOR_SPACE);
        return new FSDatasetInstanceSingularizator.FSFloatVectorDataset(name, storage);
    }

    private int parseDim(String datasetName) {
        String[] pref = new String[]{
            "misfit_Bi5d_full_h5_",
            "gr_flake_full_h5_",
            "misfit_Se3d_",
            "misfit_VB_"};
        for (String p : pref) {
            if (datasetName.startsWith(p)) {
                String ret = datasetName.substring(p.length());
                int min = Math.min(ret.indexOf("_"), ret.indexOf("."));
                ret = ret.substring(0, min);
                return Integer.parseInt(ret);
            }
        }
        throw new RuntimeException("Unknown name of file" + datasetName);
    }

    @Override
    protected Iterator<AbstractMap.SimpleEntry<String, T>> createIteratorForReader(Iterator<String> lines, int maxObjCountToReturn) {
        return new Iterator<AbstractMap.SimpleEntry<String, T>>() {
            private int counter = 0;

            @Override
            public boolean hasNext() {
                return counter < maxObjCountToReturn && lines.hasNext();
            }

            @Override
            public AbstractMap.SimpleEntry<String, T> next() {
                counter++;
                String next = lines.next();
                float[] values = DataTypeConvertor.stringToFloats(next, " ");
                return new AbstractMap.SimpleEntry(Integer.toString(counter), values);
            }
        };
    }

}
