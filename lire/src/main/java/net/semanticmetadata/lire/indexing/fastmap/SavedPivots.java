package net.semanticmetadata.lire.indexing.fastmap;

import at.lux.imageanalysis.VisualDescriptor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * ...
 * Date: 13.08.2008
 * Time: 14:42:16
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SavedPivots implements Serializable {
    String[][] pivots;

    public SavedPivots(int[][] piv, List<VisualDescriptor> objs) {
        // create a String array holding the two pivots per dimension.
        this.pivots = new String[2][piv[0].length];
        // save string representations for pivots:
        for (int i = 0; i < piv[0].length; i++) {
            pivots[0][i] = objs.get(piv[0][i]).getStringRepresentation();
            pivots[1][i] = objs.get(piv[1][i]).getStringRepresentation();
        }
    }

    /**
     * Create the pivots index array from the stored pivots and add them
     * to the list of objects to fastmap.
     *
     * @param objs       the list of objects to fastmap
     * @param descriptor the actual descriptor class of the VisualDescriptor
     * @return
     */
    public int[][] getPivots(List<VisualDescriptor> objs, Class descriptor) throws IllegalAccessException, InstantiationException {
        int[][] retVal = new int[2][pivots[0].length];
        List<VisualDescriptor> pivs = new LinkedList<VisualDescriptor>();
        int countIndex = 0;
        for (int i = 0; i < pivots[0].length; i++) {
            VisualDescriptor vd1 = (VisualDescriptor) descriptor.newInstance();
            vd1.setStringRepresentation(pivots[0][i]);
            pivs.add(vd1);
            retVal[0][i] = countIndex;
            countIndex++;
            VisualDescriptor vd2 = (VisualDescriptor) descriptor.newInstance();
            vd2.setStringRepresentation(pivots[1][i]);
            pivs.add(vd2);
            retVal[1][i] = countIndex;
            countIndex++;
        }
        objs.addAll(0, pivs);
        return retVal;
    }
}
