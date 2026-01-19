package vm.fs.searchSpaceImpl;

import java.util.AbstractMap;
import java.util.Map;
import java.util.logging.Logger;
import vm.searchSpace.AbstractSearchSpace;
import vm.searchSpace.distance.AbstractDistanceFunction;

/**
 *
 * @author xmic
 * @param <T>
 */
public class FSSearchSpaceImpl<T> extends AbstractSearchSpace<T> {

    private final Logger LOG = Logger.getLogger(FSSearchSpaceImpl.class.getName());

    public FSSearchSpaceImpl(AbstractDistanceFunction<T> df) {
        super(df);
    }

//    @Override
//    public AbstractDistanceFunction<T> getDistanceFunctionForDataset(String datasetName, Object... params) {
//        if (implicitDF != null) {
//            return implicitDF;
//        }
//        if (datasetName.contains("Angular")) {
//            return (AbstractDistanceFunction<T>) new AngularDistance();
//        }
//        if (datasetName.contains("DotPro")) {
//            return (AbstractDistanceFunction<T>) new DotProduct();
//        }
//        if (datasetName.toLowerCase().contains("pca") || datasetName.toLowerCase().contains("euclid")) {
//            return (AbstractDistanceFunction<T>) new L2OnFloatsArray();
//        }
//        if (datasetName.contains("_GHP_50_") || datasetName.contains("_GHP_80_")) {
//            return (AbstractDistanceFunction<T>) new HammingDistanceLongs();
//        }
//        if (datasetName.contains("laion2B-en")) {
//            return (AbstractDistanceFunction<T>) new CosineDistance();
//        }
//        if (datasetName.contains("random_") && datasetName.toLowerCase().contains("_uniform_1m")) {
//            return (AbstractDistanceFunction<T>) new L2OnFloatsArray();
//        }
//        switch (datasetName) {
//            case ("decaf_1m"):
//            case ("decaf_100m"):
//            case ("sift_1m"):
//                return (AbstractDistanceFunction<T>) new L2OnFloatsArray();
//            case ("mpeg7_1m"): {
//                return (AbstractDistanceFunction<T>) new Sapir3DistanceFunction();
//            }
//            case (FSMocapJanStorage.DATASET_NAME_10FPS):
//            case (FSMocapJanStorage.DATASET_NAME_30FPS):
//            case (FSMocapJanStorage.DATASET_NAME_10FPS + "_selected.txt"):
//            case (FSMocapJanStorage.DATASET_NAME_30FPS + "_selected.txt"):
//                return (AbstractDistanceFunction<T>) new DTWOnFloatsArray();
//        }
//        throw new IllegalArgumentException("Unknown dataset name " + datasetName + ". No distance function provided.");
//    }

    @Override
    public Comparable getIDOfObject(Object o) {
        Map.Entry<Comparable, T> entry = (Map.Entry<Comparable, T>) o;
        return entry.getKey();
    }

    @Override
    public T getDataOfObject(Object o) {
        Map.Entry<Comparable, T> entry = (Map.Entry<Comparable, T>) o;
        return entry.getValue();
    }

    @Override
    public Object createSearchObject(Comparable id, T data) {
        return new AbstractMap.SimpleEntry<>(id, data);
    }

}
