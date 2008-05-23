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
package at.lux.retrieval.clustering;

import at.lux.retrieval.calculations.SimilarityMatrix;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Date: 07.02.2005
 * Time: 22:47:15
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class HAC {
    private SimilarityMatrix matrix;
    private int numberOfClusters;
    private HashSet<ArrayList<Integer>> clusters;

    /**
     * Allows to use different types of HAC.
     */
    public enum HACType {
        COMPLETE_LINK, SINGLE_LINK, AVERAGE_LINK
    }

    public HAC(SimilarityMatrix matrix, int numberOfClusters) {
        this.matrix = matrix;
        this.numberOfClusters = numberOfClusters;
        init();
    }

    public HAC(SimilarityMatrix matrix) {
        this.matrix = matrix;
        numberOfClusters = (int) Math.sqrt(matrix.getDimension());
        init();
    }

    private void init() {
        clusters = new HashSet<ArrayList<Integer>>(matrix.getDimension());
        for (int i = 0; i < matrix.getDimension(); i++) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(i);
            clusters.add(list);
        }
    }

    /**
     * @return a positive integer as long as there are steps to make.
     */
    public int step() {
        ArrayList<Integer> elist1 = null, elist2 = null;
        float maxSimilarity = 0f;
        // find best merge candidates:
        for (Iterator<ArrayList<Integer>> iterator = clusters.iterator(); iterator.hasNext();) {
            ArrayList<Integer> list1 = iterator.next();
            for (Iterator<ArrayList<Integer>> iterator1 = clusters.iterator(); iterator1.hasNext();) {
                ArrayList<Integer> list2 = iterator1.next();
                if (!list1.equals(list2)) {
                    float tmp = getSimilarity(list1, list2, HACType.COMPLETE_LINK);
//                    System.out.print(tmp + ", ");
                    if (tmp >= maxSimilarity) {
                        maxSimilarity = tmp;
                        elist1 = list1;
                        elist2 = list2;
//                        System.out.print(maxSimilarity + " ");
                    }
                }
            }
        }
        // merge them:
        clusters.remove(elist2);
//        System.out.println("Merging {" + printList(elist1) + "} with {" + printList(elist2) + "}");
        clusters.remove(elist1);
        elist1.addAll(elist2);
        clusters.add(elist1);
        return clusters.size() - numberOfClusters;
    }

    /**
     * Implements different types of HAC.
     * @param list1
     * @param list2
     * @param type
     * @return
     */
    private float getSimilarity(ArrayList<Integer> list1, ArrayList<Integer> list2, HACType type) {
        float similarity = 1f;
        if (type == HACType.COMPLETE_LINK) {
            for (Iterator<Integer> iterator = list1.iterator(); iterator.hasNext();) {
                int int1 = iterator.next();
                for (Iterator<Integer> iterator1 = list2.iterator(); iterator1.hasNext();) {
                    int int2 = iterator1.next();
                    float tmp = matrix.getSimilarity(int1, int2);
                    if (tmp < similarity) similarity = tmp;
                }
            }
        }
        else if (type == HACType.AVERAGE_LINK) {
            float average = 0f;
            int count = 0;
            for (Iterator<Integer> iterator = list1.iterator(); iterator.hasNext();) {
                int int1 = iterator.next();
                for (Iterator<Integer> iterator1 = list2.iterator(); iterator1.hasNext();) {
                    int int2 = iterator1.next();
                    average += matrix.getSimilarity(int1, int2);
                    count++;
//                    if (tmp < similarity) similarity = tmp;
                }
            }
            similarity = average/((float) count);
        }
        else if (type == HACType.SINGLE_LINK) {
            similarity = 0f;
            for (Iterator<Integer> iterator = list1.iterator(); iterator.hasNext();) {
                int int1 = iterator.next();
                for (Iterator<Integer> iterator1 = list2.iterator(); iterator1.hasNext();) {
                    int int2 = iterator1.next();
                    float tmp = matrix.getSimilarity(int1, int2);
                    if (tmp > similarity) similarity = tmp;
                }
            }
        }
        return similarity;
    }

    public HashSet<ArrayList<Integer>> getClusters() {
        return clusters;
    }

    public SimilarityMatrix getMatrix() {
        return matrix;
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        int count = 0;
        for (Iterator<ArrayList<Integer>> iterator = clusters.iterator(); iterator.hasNext();) {
            count++;
            ArrayList<Integer> integers = iterator.next();
            sw.append("Cluster ");
            sw.append(count + ": ");
            for (Iterator<Integer> iterator1 = integers.iterator(); iterator1.hasNext();) {
                sw.append(iterator1.next().toString());
                if (iterator1.hasNext()) sw.append(", ");
            }
            sw.append("\n");
        }
        return sw.toString();
    }

    /**
     * For debugging lists ...
     * @param list
     * @return all elements of the list as strings
     */
    private String printList(List list) {
        StringWriter sw = new StringWriter(64);
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            sw.append(o.toString());
            if (iterator.hasNext()) sw.append(", ");
        }
        return sw.toString();
    }
}
