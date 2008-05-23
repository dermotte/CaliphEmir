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
package at.lux.retrieval.fdp;

import at.lux.retrieval.calculations.DistanceMatrix;

/**
 * Date: 02.02.2005
 * Time: 22:42:51
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FDP {
    private float[][] points;
    private DistanceMatrix fastmapDistanceMatrix;
    private FDPParameters parameters = new FDPParameters();
    private float currentMovement = -1;

    public FDP(DistanceMatrix fastmapDistanceMatrix, float[][] points) {
        this.fastmapDistanceMatrix = fastmapDistanceMatrix;
        this.points = points;
        // setting the gravitation related to the number of points.
        parameters.setGravitation(3d/Math.sqrt(points.length));
    }

    public FDP(DistanceMatrix fastmapDistanceMatrix, FDPParameters parameters, float[][] points) {
        this.fastmapDistanceMatrix = fastmapDistanceMatrix;
        this.parameters = parameters;
        this.points = points;
    }

    public void step() {
        currentMovement = 0;
        for (int i = 0; i < points.length; i++) {
            float[] point = points[i];
            double[] moveVector = new double[point.length];
            for (int j = 0; j < points.length; j++) {
                if (i!=j) {
                    // I take for granted that the distance is normalized:
                    double oSimilarity = 1 - fastmapDistanceMatrix.getDistance(i,j);
                    double pDist = 0f;

                    for (int k = 0; k < point.length; k++) {
                        double l1 = point[k] - points[j][k];
                        pDist += l1*l1;
                    }
                    pDist = Math.sqrt(pDist);
                    if (pDist <= Double.MIN_VALUE) pDist = parameters.minimumDistance;
                    double currentForce = Math.pow(oSimilarity, parameters.d) - parameters.w/(Math.pow(pDist, parameters.r)) + parameters.gravitation;
                    for (int k = 0; k < moveVector.length; k++) {
                        moveVector[k] += currentForce * points[j][k] + (1 - currentForce) * point[k];
                    }
                }
            }

            float fraction = 1f/((float) points.length);

            for (int j = 0; j < point.length; j++) {
                float moveInComponent = fraction * (float) moveVector[j];
                point[j] = moveInComponent;
                currentMovement += Math.abs(moveInComponent);
            }

        }
    }

    /**
     * Returns movement of the last step - used to check if
     * equilibrium is reached.
     * @return the sum of absolute movements or -1 if no step was taken
     */
    public float getCurrentMovement() {
        return currentMovement;
    }
}
