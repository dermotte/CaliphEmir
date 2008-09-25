/*
 * This file is part of the LIRE project: http://www.SemanticMetadata.net/lire.
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
 * Original class created and published under GPL by
 * Stephan Preibisch <preibisch@mpi-cbg.de> and Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * see: http://fly.mpi-cbg.de/~saalfeld/javasift.html
 *
 * Note, that the SIFT-algorithm is protected by U.S. Patent 6,711,293: "Method and
 * apparatus for identifying scale invariant features in an image and use of same for
 * locating an object in an image" by the University of British Columbia. That is, for
 * commercial applications the permission of the author is required.
 *
 * some further adoptions to Lire made by
 *     Mathias Lux, mathias@juggle.at
 */
package net.semanticmetadata.lire.imageanalysis.sift;

import java.util.ArrayList;
import java.util.Collection;

public class PointMatch {
    final private Point p1;

    final public Point getP1() {
        return p1;
    }

    final private Point p2;

    final public Point getP2() {
        return p2;
    }

    private float weight;

    final public float getWeight() {
        return weight;
    }

    final public void setWeight(float weight) {
        this.weight = weight;
    }

    private float distance;

    final public float getDistance() {
        return distance;
    }

    public PointMatch(
            Point p1,
            Point p2,
            float weight) {
        this.p1 = p1;
        this.p2 = p2;

        this.weight = weight;

        distance = Point.distance(p1, p2);
    }

    public PointMatch(
            Point p1,
            Point p2) {
        this.p1 = p1;
        this.p2 = p2;

        weight = 1.0f;

        distance = Point.distance(p1, p2);
    }

    /**
     * apply a model to p1, update distance
     *
     * @param model
     */
    final public void apply(Model model) {
        p1.apply(model);
        distance = Point.distance(p1, p2);
    }

    /**
     * flip symmetrically, weight remains unchanged
     *
     * @param matches
     * @return
     */
    final public static ArrayList<PointMatch> flip(Collection<PointMatch> matches) {
        ArrayList<PointMatch> list = new ArrayList<PointMatch>();
        for (PointMatch match : matches) {
            list.add(
                    new PointMatch(
                            match.p2,
                            match.p1,
                            match.weight));
		}
		return list;
	}
	
}
