/*
 * This file is part of Caliph & Emir.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
package at.lux.retrieval.clustering.suffixtree;

import java.util.Set;
import java.util.WeakHashMap;

/**
 * Todo: Recode Similarity matrix in that way, that there's only the edge matrix left, this will bring a linear performance boost.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 07.06.2004
 *         Time: 09:42:05
 */

public class EdgeIndex {
//    private HashMap<BaseCluster, WeakHashMap<BaseCluster, Boolean>> similarityMatrix = null;
    private WeakHashMap<BaseCluster, WeakHashMap<BaseCluster, Object>> edgeIndex = null;

    public EdgeIndex() {
//        similarityMatrix = new HashMap<BaseCluster, WeakHashMap<BaseCluster, Boolean>>();
        edgeIndex = new WeakHashMap<BaseCluster, WeakHashMap<BaseCluster, Object>>();
    }

    public void addBaseCluster(BaseCluster cluster) {
//        assert similarityMatrix.put(cluster, new WeakHashMap<BaseCluster, Boolean>()) == null;
        WeakHashMap<BaseCluster, Object> weakHashMap = edgeIndex.put(cluster, new WeakHashMap<BaseCluster, Object>());
        assert weakHashMap == null;
    }

    public void removeBaseCluster(BaseCluster cluster) {
//        assert similarityMatrix.remove(cluster) != null;
        WeakHashMap<BaseCluster, Object> weakHashMap = edgeIndex.remove(cluster);
        assert weakHashMap != null;
    }

    /**
     * This method similarity lookup of only the upper right half of the similarity matrix.
     * please note: if the entries are not created/updated yet, the similarity will be null!
     *
     * @param b1
     * @param b2
     * @return a boolean indicating the similarity or <code>NULL</code> if it hasn't been calculated yet
     */
//    public Boolean getSimilarity(BaseCluster b1, BaseCluster b2) {
//        BaseCluster c1, c2;
//        if (b1.compareTo(b2) == 0)
//            return new Boolean(true);
//        else if (b1.compareTo(b2) > 0) {
//            c1 = b1;
//            c2 = b2;
//        } else {
//            c1 = b2;
//            c2 = b1;
//        }
//        Boolean result = similarityMatrix.get(c1).get(c2);
//        return result;
//    }

    /**
     * This method updates an existing similarity:
     *
     * @param b1
     * @param b2
     */
    public void update(BaseCluster b1, BaseCluster b2) {
//        BaseCluster c1, c2;
        Boolean result = false;
        if (b1.equals(b2)) return;
//        if (b1.compareTo(b2) > 0) {
//            c1 = b1;
//            c2 = b2;
//        } else {
//            c1 = b2;
//            c2 = b1;
//        }
        if (b1.binarySimilarity(b2) == 1)
            result = true;
//        if (!similarityMatrix.containsKey(c1)) addBaseCluster(c1);
//        similarityMatrix.get(c1).put(c2, result);
        // update the edgeIndex:
        if (result.booleanValue()) {
//            assert edgeIndex.containsKey(c1);
//            assert edgeIndex.containsKey(c2);
            edgeIndex.get(b1).put(b2, null);
            edgeIndex.get(b2).put(b1, null);
        }
    }

    /**
     * Retrieve all known edges from the edge Index.
     *
     * @param cluster
     * @return
     */
    public Set<BaseCluster> getAllConnectedBaseClusters(BaseCluster cluster) {
        if (edgeIndex.get(cluster) != null)
            return edgeIndex.get(cluster).keySet();
        else
            return null;
    }

    /*
     * Updates all Similarities. Returns true if there has been any update.
     *
     * @return

    public boolean updateAllSimilarities() {
        Set<BaseCluster> keySet = similarityMatrix.keySet();
        boolean result = false;
        for (Iterator<BaseCluster> iterator = keySet.iterator(); iterator.hasNext();) {
            BaseCluster b1 = iterator.next();
            for (Iterator<BaseCluster> it1 = keySet.iterator(); it1.hasNext();) {
                BaseCluster b2 = it1.next();
                if (b1.compareTo(b2) > 0) {
                    Boolean nSim = b1.binarySimilarity(b2) == 1 ? true : false;
                    Boolean oSim = similarityMatrix.get(b1).get(b2);
                    if ((nSim.booleanValue() && oSim.booleanValue()) || (!nSim.booleanValue() && !oSim.booleanValue())) {
                        result = true;
                    } else {
                        similarityMatrix.get(b1).put(b2, nSim);
                    }
                }
            }
        }
        return !result;
    }

     */

}
