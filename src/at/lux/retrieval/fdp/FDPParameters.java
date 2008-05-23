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

/**
 * Date: 02.02.2005
 * Time: 22:49:16
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FDPParameters {
    /**
     * Parameter prevents points from comint too close to each other.
     * Value is normally 1, allowed are values from (0, infinite).
     */
    double r = 1;
    /**
     * Tunes the atraction of points the higher this value the
     * smaller the attraction. Default value is 1.
     */
    double w = 1;
    /**
     * This parameter makes cluster separation more significant
     * if value is bigger than 1.
     */
    double d = 1;
    /**
     * Constant gravitation of points, should be related somehow
     * with the number of points, the more points the smaller the
     * gravitation.
     */
    double gravitation = 0.3;
    /**
     * defines what is the minimum distance simulated for points
     * at the same location.
     */
    float minimumDistance = 0.0000001f;


    public FDPParameters(double d, double gravitation, float minimumDistance, double r, double w) {
        this.d = d;
        this.gravitation = gravitation;
        this.minimumDistance = minimumDistance;
        this.r = r;
        this.w = w;
    }

    public FDPParameters() {
        // nothing ...
    }

    /**
     * This parameter makes cluster separation more significant
     * if value is bigger than 1.
     */
    public double getD() {
        return d;
    }

    /**
     * This parameter makes cluster separation more significant
     * if value is bigger than 1.
     */
    public void setD(double d) {
        this.d = d;
    }

    /**
     * Constant gravitation of points, should be related somehow
     * with the number of points, the more points the smaller the
     * gravitation.
     */
    public double getGravitation() {
        return gravitation;
    }

    /**
     * Constant gravitation of points, should be related somehow
     * with the number of points, the more points the smaller the
     * gravitation.
     */
    public void setGravitation(double gravitation) {
        this.gravitation = gravitation;
    }

    public float getMinimumDistance() {
        return minimumDistance;
    }

    public void setMinimumDistance(float minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    /**
     * Parameter prevents points from comint too close to each other.
     * Value is normally 1, allowed are values from (0, infinite).
     */
    public double getR() {
        return r;
    }

    /**
     * Parameter prevents points from comint too close to each other.
     * Value is normally 1, allowed are values from (0, infinite).
     */
    public void setR(double r) {
        this.r = r;
    }

    /**
     * Tunes the atraction of points the higher this value the
     * smaller the attraction. Default value is 1.
     */
    public double getW() {
        return w;
    }

    /**
     * Tunes the atraction of points the higher this value the
     * smaller the attraction. Default value is 1.
     */
    public void setW(double w) {
        this.w = w;
    }
}
