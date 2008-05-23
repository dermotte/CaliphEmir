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

import at.lux.retrieval.calculations.DistanceMatrix;
import at.lux.retrieval.calculations.SimilarityMatrix;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Date: 07.02.2005
 * Time: 22:19:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ArraySimilarityMatrix implements SimilarityMatrix {
    private float[][] similarity;
    private ArrayList objects;
    private HashMap<Object, Integer> objects2position;
    private int dimension;

    public ArraySimilarityMatrix(float[][] distance, ArrayList objects) {
        this.similarity = distance;
        this.objects = objects;
        dimension = objects.size();
        objects2position = new HashMap<Object, Integer>(dimension);
        int count = 0;
        for (Iterator iterator = objects.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            objects2position.put(o, count);
            count++;
        }
    }

    /**
     * Calculates the similarity between objects using the similarity function or the stored values.
     * If it has not been computed previously it is computed and stored now.
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @return the similarity as float from [0, 1]
     */
    public float getSimilarity(Object o1, Object o2) {
        int i1 = objects2position.get(o1);
        int i2 = objects2position.get(o2);
        if (i1>=0 && i1 <dimension && i2>=0 && i2<dimension) {
            return getSimilarity(i1, i2);
        }
        else return -1f;
    }

    /**
     * Calculates the Similarity between objects using the Similarity function for k = 0,
     * using . If it has not
     * been computed previously it is computed and stored now.
     *
     * @param index1 index of first object to compute
     * @param index2 index of second object to compute
     * @return the Similarity as float from [0, infinite)
     */
    public float getSimilarity(int index1, int index2) {
        int i1 = Math.min(index1, index2);
        int i2 = Math.max(index1, index2);
        if (i1>=0 && i1 <dimension && i2>=0 && i2<dimension) {
            return similarity[i1][i2];
        }
        return 0;
    }

    /**
     * Used for the heuristic for getting the pivots as described in the paper. This method calls
     * with parameters (row, 0, null, null).
     *
     * @param row defines the row where we want to find the maximum
     * @return the index of the object with maximum Similarity to the row object.
     */
    public int getMaximumSimilarity(int row) {
        float maxSimilarity = 0f;
        int maxIndex = -1;
        for (int i = 0; i < dimension; i++) {
            if (i != row) {
                float similarity = getSimilarity(i,row);
                if (similarity>maxSimilarity) {
                    maxSimilarity = similarity;
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    /**
     * Returns the index of the col with minimum similarity to the given row
     *
     * @param row defines the row where we want to find the minimum
     * @return the index of the object with minimum Similarity to the row object.
     */
    public int getMinimumSimilarity(int row) {
        float minSimilarity = 1f;
        int minIndex = -1;
        for (int i = 0; i < dimension; i++) {
            if (i != row) {
                float similarity = getSimilarity(i,row);
                if (similarity<minSimilarity) {
                    minSimilarity = similarity;
                    minIndex = i;
                }
            }
        }
        return minIndex;
    }

    /**
     * Returns the dimension of the matrix
     *
     * @return dimension, which is > 0
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Returns the user object for given index number
     *
     * @param rowNumber
     * @return
     * @see #getIndexOfObject(Object)
     */
    public Object getUserObject(int rowNumber) {
        return objects.get(rowNumber);
    }

    /**
     * Returns the index in the matrix of the given user object or -1 if not found
     *
     * @param o the object to search for
     * @return the index number of the object or -1 if not found
     * @see #getUserObject(int)
     */
    public int getIndexOfObject(Object o) {
        return objects2position.get(o);
    }

    public DistanceMatrix getDistanceMatrix() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String toString() {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        df.setMaximumIntegerDigits(1);
        df.setMinimumIntegerDigits(1);
        df.setMaximumFractionDigits(3);
        df.setMinimumFractionDigits(3);
        StringWriter sw = new StringWriter(512);
        for (int i = 0; i< dimension; i++) {
            for (int j = 0; j< dimension; j++) {
                sw.append(df.format(similarity[i][j]) + "\t");
            }
            sw.append("\n");
        }

        return sw.toString();
    }
}
