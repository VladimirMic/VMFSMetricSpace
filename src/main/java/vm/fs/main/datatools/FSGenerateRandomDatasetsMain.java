package vm.fs.main.datatools;

import vm.fs.searchSpaceImpl.FSSearchSpaceImpl;
import vm.fs.searchSpaceImpl.FSSearchSpacesStorage;
import vm.searchSpace.AbstractSearchSpacesStorage;
import vm.searchSpace.data.RandomVectorsGenerator;
import vm.searchSpace.data.toStringConvertors.impl.FloatVectorToStringConvertor;
import vm.searchSpace.distance.impl.L2OnFloatsArray;

/**
 *
 * @author au734419
 */
public class FSGenerateRandomDatasetsMain {

    public static void main(String[] args) {
        FloatVectorToStringConvertor floatVectorConvertor = new FloatVectorToStringConvertor();
        FSSearchSpaceImpl<float[]> searchSpace = new FSSearchSpaceImpl<>(new L2OnFloatsArray());
        AbstractSearchSpacesStorage storage = new FSSearchSpacesStorage(searchSpace, floatVectorConvertor);

        RandomVectorsGenerator generator = new RandomVectorsGenerator(storage);
        generator.run();
    }

}
