/*
 * This file is part of Lire (Lucene Image Retrieval).
 *
 * Lire is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Lire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lire; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2008 by Mathias Lux (mathias@juggle.at)
 * http://www.semanticmetadata.net
 */
package net.semanticmetadata.lire.matrix;

/**
 * Date: 07.02.2005
 * Time: 22:10:54
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface SimilarityMatrix {
    /**
     * Calculates the distance between objects using the distance function for k = 0,
     * using . If it has not
     * been computed previously it is computed and stored now.
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @return the distance as float from [0, infinite)
     */
    public float getSimilarity(Object o1, Object o2);

    /**
     * Calculates the Similarity between objects using the Similarity function for k = 0,
     * using . If it has not
     * been computed previously it is computed and stored now.
     *
     * @param index1 index of first object to compute
     * @param index2 index of second object to compute
     * @return the Similarity as float from [0, infinite)
     */
    public float getSimilarity(int index1, int index2);

    /**
     * Used for the heuristic for getting the pivots as described in the paper. This method calls
     * with parameters (row, 0, null, null).
     *
     * @param row defines the row where we want to find the maximum
     * @return the index of the object with maximum Similarity to the row object.
     */
    public int getMaximumSimilarity(int row);

    /**
     * Returns the index of the col with minimum similarity to the given row
     *
     * @param row defines the row where we want to find the minimum
     * @return the index of the object with minimum Similarity to the row object.
     */
    public int getMinimumSimilarity(int row);

    /**
     * Returns the dimension of the matrix
     *
     * @return dimension, which is > 0
     */
    public int getDimension();

    /**
     * Returns the user object for given index number
     *
     * @param rowNumber
     * @return
     * @see #getIndexOfObject(Object)
     */
    public Object getUserObject(int rowNumber);

    /**
     * Returns the index in the matrix of the given user object or -1 if not found
     *
     * @param o the object to search for
     * @return the index number of the object or -1 if not found
     * @see #getUserObject(int)
     */
    public int getIndexOfObject(Object o);

    public DistanceMatrix getDistanceMatrix();
}