package vm.fs.searchSpaceImpl.parsersOfOtherFormats.impl;

import java.io.BufferedReader;
import java.io.File;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import vm.fs.FSGlobal;
import vm.fs.searchSpaceImpl.FSSearchSpaceImpl;
import vm.fs.searchSpaceImpl.parsersOfOtherFormats.AbstractFSSearchSpacesStorageWithOthersDatasetStorage;
import vm.searchSpace.Dataset;
import vm.searchSpace.ToolsSpaceDomain;
import vm.searchSpace.data.toStringConvertors.SingularisedConvertors;
import vm.searchSpace.distance.impl.DTWOnFloatsArray;

/**
 *
 * @author xmic
 */
public class FSMocapJanStorage extends AbstractFSSearchSpacesStorageWithOthersDatasetStorage<List<float[][]>> {

    public static final String DATASET_NAME_10FPS = "actions-single-subject-all-POS-fps10.data";
    public static final String DATASET_NAME_30FPS = "actions-single-subject-all-POS.data";

    public FSMocapJanStorage() {
        super(new FSSearchSpaceImpl(new DTWOnFloatsArray()), SingularisedConvertors.MOCAP_SPACE);
    }

    @Override
    protected File getFileForDataset(String datasetName) {
        return getFileForObjects(FSGlobal.DATASET_FOLDER, datasetName, false);
    }

    @Override
    protected File getFileForObjects(String folder, String datasetName, boolean willBeDeleted) {
        File ret = new File(folder, "Honza");
        ret = new File(ret, datasetName);
        if (willBeDeleted && ret.exists()) {
            throw new IllegalArgumentException("Attempt to delete implicit dataset!");
        }
        return ret;
    }

    @Override
    protected Iterator<AbstractMap.SimpleEntry<String, List<float[][]>>> getIteratorForReader(BufferedReader br, int count, String filePath) {
        if (filePath.startsWith(FSGlobal.PIVOT_FOLDER)) {
            return super.getIteratorForReader(br, count, filePath);
        }
        return createIteratorForReader(br.lines().iterator(), count);
    }

///////////////////////////////// priprietary
    public static final Dataset<List<float[][]>> createInstanceOfOriginalDataset(String datasetNameSeeConstants) {
        FSMocapJanStorage storage = new FSMocapJanStorage();
        return new Dataset<List<float[][]>>(datasetNameSeeConstants, storage) {
            @Override
            public Map<Comparable, List<float[][]>> getKeyValueStorage() {
                Iterator<Object> it = storage.getObjectsFromDataset(datasetName, -1);
                Map<Comparable, List<float[][]>> ret = ToolsSpaceDomain.getObjectsAsIdDataMap(this);
                return ret;
            }

            @Override
            public boolean hasKeyValueStorage() {
                return false;
            }

            @Override
            public void deleteKeyValueStorage() {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public int getRecommendedNumberOfPivotsForFiltering() {
                return 64;
            }

            @Override
            public boolean shouldStoreDistsToPivots() {
                return true;
            }

            @Override
            public boolean shouldCreateKeyValueStorage() {
                return true;
            }
        };
    }

    @Override
    protected Iterator<AbstractMap.SimpleEntry<String, List<float[][]>>> createIteratorForReader(Iterator<String> lines, int maxObjCountToReturn) {
        return new MocapIterator(lines, maxObjCountToReturn);
    }

    protected class MocapIterator implements Iterator<AbstractMap.SimpleEntry<String, List<float[][]>>> {

        private String nextLine = null;
        private int counter = 0;
        private final Iterator<String> lines;
        private final int maxObjCountToReturn;

        public MocapIterator(Iterator<String> lines, int maxObjCountToReturn) {
            this.lines = lines;
            this.maxObjCountToReturn = maxObjCountToReturn;
            if (lines.hasNext()) {
                nextLine = lines.next();
            } else {
                nextLine = null;
            }
        }

        @Override
        public boolean hasNext() {
            return nextLine != null && maxObjCountToReturn > counter;
        }

        @Override
        public final AbstractMap.SimpleEntry<String, List<float[][]>> next() {
            if (hasNext()) {
                String[] split = nextLine.split(" ");
                String id = split[2];
                StringBuilder sb = new StringBuilder();
                nextLine = lines.next();
                nextLine = lines.next();
                while (nextLine != null && !nextLine.contains("key")) {
                    sb.append(nextLine).append("|");
                    if (lines.hasNext()) {
                        nextLine = lines.next();
                    } else {
                        nextLine = null;
                    }
                }
                List<float[][]> movement = dataSerializator.parseString(sb.toString());
                AbstractMap.SimpleEntry<String, List<float[][]>> ret = new AbstractMap.SimpleEntry(id, movement);
                counter++;
                return ret;
            }
            return null;
        }

    }

}
