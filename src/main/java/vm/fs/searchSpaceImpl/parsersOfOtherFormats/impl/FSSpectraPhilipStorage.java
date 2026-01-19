/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vm.fs.searchSpaceImpl.parsersOfOtherFormats.impl;

import java.io.File;
import java.util.AbstractMap;
import java.util.Iterator;
import vm.datatools.DataTypeConvertor;
import vm.fs.FSGlobal;
import vm.fs.dataset.FSDatasetInstances;
import vm.fs.searchSpaceImpl.FSSearchSpaceImpl;
import vm.fs.searchSpaceImpl.parsersOfOtherFormats.AbstractFSSearchSpacesStorageWithOthersDatasetStorage;
import vm.searchSpace.Dataset;
import vm.searchSpace.data.toStringConvertors.SingularisedConvertors;
import vm.searchSpace.distance.AbstractDistanceFunction;
import vm.searchSpace.distance.impl.L2OnFloatsArray;
import vm.searchSpace.data.toStringConvertors.SearchObjectDataToStringInterface;

/**
 *
 * @author au734419
 * @param <T>
 */
public class FSSpectraPhilipStorage<T> extends AbstractFSSearchSpacesStorageWithOthersDatasetStorage<T> {

    public static final String DATASET_NAME = "data95_dyn.txt";

    public FSSpectraPhilipStorage(AbstractDistanceFunction<T> df, SearchObjectDataToStringInterface<T> dataSerializator) {
        super(new FSSearchSpaceImpl(df), dataSerializator);
    }

    @Override
    protected File getFileForDataset(String datasetName) {
        File ret = new File(FSGlobal.DATASET_FOLDER, "Philip");
        ret = new File(ret, datasetName);
        return ret;
    }
///////////////////////////////// priprietary

    public static final Dataset<float[]> createDataset() {
        FSSpectraPhilipStorage storage = new FSSpectraPhilipStorage(new L2OnFloatsArray(), SingularisedConvertors.FLOAT_VECTOR_SPACE);
        return new FSDatasetInstances.FSDatasetWithOtherSource(DATASET_NAME, storage) {
            @Override
            public boolean shouldStoreDistsToPivots() {
                return false;
            }

            @Override
            public boolean shouldCreateKeyValueStorage() {
                return false;
            }
        };
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
                float[] values = DataTypeConvertor.stringToFloats(next, ",");
                return new AbstractMap.SimpleEntry(Integer.toString(counter), values);
            }
        };
    }
}
