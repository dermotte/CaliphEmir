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
package at.lux.retrieval.fastmap;

import at.lux.retrieval.calculations.DistanceMatrix;

/**
 * Date: 07.02.2005
 * Time: 22:06:25
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface FastmapDistanceMatrix extends DistanceMatrix {

    /**
     * Calculates and returns the distance between two objects. Please note that the
     * distance function has to be symmetric and must obey the triangle inequality.
     * distance in k is: d[k+1](o1,o2)^2 = d[k](o1,o2)^2 - (x1[k]-x2[k])^2 .
     *
     * @param index1 index of first object to compute
     * @param index2 index of second object to compute
     * @param k      defines the dimension of current fastmap operation
     * @param x1     is needed when k > 0 (see documentation above), all x1[l] with l &lt; k have to be present.
     * @param x2     is needed when k > 0 (see documentation above), all x2[l] with l &lt; k have to be present.
     * @return the distance as float from [0, infinite)
     */
    float getDistance(int index1, int index2, int k, float[] x1, float[] x2);

    /**
     * Used for the heuristic for getting the pivots as described in the paper.
     *
     * @param row    defines the row where we want to find the maximum
     * @param k      defines the dimension of current fastmap operation
     * @param points is needed when k > 0 (see documentation above), all x1[l] with l &lt; k have to be present.
     * @return the index of the object with maximum distance to the row object.
     */
    int getMaximumDistance(int row, int k, float[][] points);

    /**
     * Normalizes the matrix for all values to [0,1]
     */
    public void normalize();

}
