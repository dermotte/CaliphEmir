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
package net.semanticmetadata.lire.indexing.fastmap;


/**
 * Date: 13.01.2005
 * Time: 23:18:35
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FastMap implements Runnable {
    private double[][] X;
    private int[][] PA;
    private int col, currentDimension;
    private FastmapDistanceMatrix matrixFastmap;
    int dimensions;

    /**
     * Creates a new FastMap which uses attached FastmapDistanceMatrix and projects to
     * a space with given dimensions
     *
     * @param dimensions    defines the dimensions of the target space
     * @param matrixFastmap the distance matrixFastmapFastmap upon the computation takes place
     */
    public FastMap(FastmapDistanceMatrix matrixFastmap, int dimensions) {
        this.matrixFastmap = matrixFastmap;
        this.dimensions = dimensions;
        init();
    }

    private void init() {
        X = new double[matrixFastmap.getDimension()][dimensions];
        PA = new int[2][dimensions];
        col = 0;
        currentDimension = 0;
    }

    public double[][] getPoints() {
        return X;
    }

    public void run() {
        while (fastMap() > 0) ;
    }

    private int fastMap() {
        int k = dimensions - currentDimension;
        if (k <= 0)
            return 0;
        else
            col++;

        findPivots(k);
        int pivot1 = PA[0][k - 1];
        int pivot2 = PA[1][k - 1];
        if (matrixFastmap.getDistance(pivot1, pivot2, currentDimension, X[pivot1], X[pivot2]) == 0f) {
            for (int i = 0; i < X.length; i++) {
                double[] floats = X[i];
                floats[col - 1] = 0d;
            }
            // and stop here ...
            return 0;
        }

        for (int i = 0; i < matrixFastmap.getDimension(); i++) {
            // X[num1][col-2]-X[num2][col-2]
            double d_ab2 = matrixFastmap.getDistance(pivot1, pivot2, currentDimension, X[pivot1], X[pivot2]);
            double d_ai2 = matrixFastmap.getDistance(pivot1, i, currentDimension, X[pivot1], X[i]);
            double d_bi2 = matrixFastmap.getDistance(pivot2, i, currentDimension, X[pivot2], X[i]);

            X[i][col - 1] = (d_ai2 * d_ai2 + d_ab2 * d_ab2 - d_bi2 * d_bi2) / (2f * d_ab2);
        }
        currentDimension++;
        return k - 1;
    }

    /**
     * Finds the pivots where the other points are interpolated in between. This method is definitely issue
     * to tuning. Currently a greedy approach is implemented.
     *
     * @param k
     */
    private void findPivots(int k) {
        // increase this number to get it more precise, decrease to get it faster.
        int numIterations = 4;
        int randomRow = (int) (Math.random() * matrixFastmap.getDimension());
        int pivot1 = 0;
        int pivot2 = randomRow;
        for (int i = 0; i < numIterations; i++) {
            pivot1 = matrixFastmap.getMaximumDistance(pivot2, currentDimension, X);
            pivot2 = matrixFastmap.getMaximumDistance(pivot1, currentDimension, X);
        }
        PA[1][k - 1] = pivot2;
        PA[0][k - 1] = pivot1;
        // float distance = matrixFastmap.getDistance(pivot1, pivot2, currentDimension, X[pivot1], X[pivot2]);
        // System.out.println("Pivots: " + PA[0][k - 1] + ", " + PA[1][k - 1] + " with distance " + distance + " at k=" + k + " and currentDimension=" + currentDimension);
    }

    public int getIndexOfObject(Object o) {
        return matrixFastmap.getIndexOfObject(o);
    }
}
