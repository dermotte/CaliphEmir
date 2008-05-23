package at.lux.retrieval.graphisomorphism;

import at.lux.retrieval.calculations.SimilarityMatrix;
import at.lux.retrieval.clustering.ArraySimilarityMatrix;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * Lazy implementation of a distance matrix.
 * Date: 13.01.2005
 * Time: 22:36:39
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DistanceMatrix {
    private float[][] distance;
    private ArrayList<Element> objects;
    private HashMap<Element, Integer> objects2position;
    private DistanceFunction distanceFct;
    private int dimension;
    private boolean distributeObjects = false;

    /**
     * Creates a new distance matrix. Please note that the distance matrix uses storage in quadratic size of
     * the user object count. The DistanceCalculator has to be able to work on those userObjects.
     *
     * @param userObjects      gives the collection of object to be processed
     * @param distanceFunction allows the distance calculation  or -1 if objects distance cannot be computes, has to be a metric
     */
    public DistanceMatrix(List<Element> userObjects, DistanceFunction distanceFunction) {
        init(distanceFunction, userObjects);
    }

    /**
     * Creates a new distance matrix. Please note that the distance matrix uses storage in quadratic size of
     * the user object count. The DistanceCalculator has to be able to work on those userObjects.
     *
     * @param userObjects      gives the collection of object to be processed
     * @param distanceFunction allows the distance calculation  or -1 if objects distance cannot be computes, has to be a metric
     * @param userObjects      select true if you want to distribute not equal but zero distance objects.
     */
    public DistanceMatrix(List<Element> userObjects, DistanceFunction distanceFunction, boolean distributeObjects) {
        init(distanceFunction, userObjects);
        this.distributeObjects = distributeObjects;
    }

    private void init(DistanceFunction distanceFunction, List<Element> userObjects) {
        distanceFct = distanceFunction;
        this.objects = new ArrayList<Element>(userObjects.size());
        this.objects.addAll(userObjects);
        dimension = objects.size();
        distance = new float[dimension][dimension];
        for (float[] floats : distance) {
            for (int j = 0; j < floats.length; j++) {
                floats[j] = -1f;
            }
        }
        objects2position = new HashMap<Element, Integer>(dimension);
        int count = 0;
        for (Element o : objects) {
            objects2position.put(o, count);
            count++;
        }
    }

    /**
     * Calculates the distance between objects using the distance function for k = 0,
     * using {@link at.lux.retrieval.fastmap.DistanceCalculator#getDistance(Object, Object)}. If it has not
     * been computed previously it is computed and stored now.
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @return the distance as float from [0, infinite)
     */
    public float getDistance(Element o1, Element o2) {
        int num1, num2;
        num1 = objects2position.get(o1);
        num2 = objects2position.get(o2);
        return getDistance(num1, num2);
    }

    /**
     * Calculates the distance between objects using the distance function for k = 0,
     * using {@link at.lux.retrieval.fastmap.DistanceCalculator#getDistance(Object, Object)}. If it has not
     * been computed previously it is computed and stored now.
     *
     * @param index1 index of first object to compute
     * @param index2 index of second object to compute
     * @return the distance as float from [0, infinite)
     */
    public float getDistance(int index1, int index2) {
        int tmp;

        if (index1 == index2) return 0f;

        if (index1 > index2) {
            tmp = index1;
            index1 = index2;
            index2 = tmp;
        }

        if (distance[index1][index2] < 0) {
            float distance = distanceFct.getDistance(objects.get(index1), objects.get(index2));
            if (distributeObjects && distance == 0) {
                distance = 0.2f;
            }
            this.distance[index1][index2] = distance;
        }

        return distance[index1][index2];
    }

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
    public float getDistance(int index1, int index2, int k, float[] x1, float[] x2) {
        // kind of speed up ...
        if (index1 == index2) return 0f;

        float originalDistance = getDistance(index1, index2);
        if (k == 0) {
            return originalDistance;
        } else {
            float distance = originalDistance * originalDistance;
            for (int i = 0; i < k; i++) {
                float xDifference = x1[i] - x2[i];
                distance = distance - xDifference * xDifference;
            }
            return (float) Math.sqrt(distance);
        }
    }

    /**
     * Used for the heuristic for getting the pivots as described in the paper.
     *
     * @param row    defines the row where we want to find the maximum
     * @param k      defines the dimension of current fastmap operation
     * @param points is needed when k > 0 (see documentation above), all x1[l] with l &lt; k have to be present.
     * @return the index of the object with maximum distance to the row object.
     */
    public int getMaximumDistance(int row, int k, float[][] points) {
        float max = 0f;
        int result = 0;
        for (int i = 0; i < dimension; i++) {
            float[] point1 = null;
            float[] point2 = null;
            if (points != null) {
                point1 = points[row];
                point2 = points[i];
            }
            float currentDistance = getDistance(row, i, k, point1, point2);
            if (currentDistance > max) {
                max = currentDistance;
                result = i;
            }
        }
        return result;
    }

    /**
     * Used for the heuristic for getting the pivots as described in the paper. This method calls
     * {@link #getMaximumDistance(int, int, float[][])} with parameters (row, 0, null, null).
     *
     * @param row defines the row where we want to find the maximum
     * @return the index of the object with maximum distance to the row object.
     * @see #getMaximumDistance(int, int, float[][])
     */
    public int getMaximumDistance(int row) {
        return getMaximumDistance(row, 0, null);
    }

    public int getDimension() {
        return dimension;
    }

    /**
     * Returns the user object for given index number
     *
     * @param rowNumber
     * @return
     * @see #getIndexOfObject(Element)
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
    public int getIndexOfObject(Element o) {
        Integer index = objects2position.get(o);
        if (index == null)
            return -1;
        else
            return index;
    }

    /**
     * Creates and returns a newly created similarity Matrix from the given
     * distance Matrix
     *
     * @return the similarityMatrix or null if not implemented or possible
     */
    public SimilarityMatrix getSimilarityMatrix() {
        float maximumDistance = 0f;
        for (int i = 0; i < dimension; i++) {
            int maxDist = getMaximumDistance(i);
            float distance = getDistance(i, maxDist);
            if (distance > maximumDistance) maximumDistance = distance;
        }
        float[][] similarity = new float[dimension][dimension];
        for (int i = 0; i < similarity.length; i++) {
            for (int j = 0; j < similarity[i].length; j++) {
                similarity[i][j] = 1f - (getDistance(i, j) / maximumDistance);
            }
        }
        ArrayList<Element> objects = new ArrayList<Element>(this.objects);
        return new ArraySimilarityMatrix(similarity, objects);  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Normalizes the matrix for all values to [0,1]
     */
    public void normalize() {
        float maximumDistance = 0f;
        for (int i = 0; i < dimension; i++) {
            int maxDist = getMaximumDistance(i);
            float distance = getDistance(i, maxDist);
            if (distance > maximumDistance) maximumDistance = distance;
        }
        float[][] newDistances = new float[dimension][dimension];
        for (int i = 0; i < newDistances.length; i++) {
            for (int j = 0; j < newDistances[i].length; j++) {
                newDistances[i][j] = (getDistance(i, j) / maximumDistance);
            }
        }
        distance = newDistances;
    }

}
