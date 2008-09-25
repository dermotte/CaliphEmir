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


import at.lux.imageanalysis.VisualDescriptor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * SIFT feature container
 */
public class Feature implements Comparable<Feature>, Serializable, VisualDescriptor {
    private Logger logger = Logger.getLogger(getClass().getName());
    public float scale;
    public float orientation;
    public float[] location;
    public float[] descriptor;

    /**
     * Dummy constructor for Serialization to work properly.
     */
    public Feature() {
    }

    public Feature(float s, float o, float[] l, float[] d) {
        scale = s;
        orientation = o;
        location = l;
        descriptor = d;
    }

    /**
     * comparator for making Features sortable
     * please note, that the comparator returns -1 for
     * this.scale &gt; o.scale, to sort the features in a descending order
     */
    public int compareTo(Feature f) {
        return scale < f.scale ? 1 : scale == f.scale ? 0 : -1;
    }

    public float descriptorDistance(Feature f) {
        float d = 0;
        for (int i = 0; i < descriptor.length; ++i) {
            float a = descriptor[i] - f.descriptor[i];
            d += a * a;
        }
        return (float) Math.sqrt(d);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < descriptor.length; i++) {
            sb.append(descriptor[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public float getDistance(VisualDescriptor vd) {
        if (vd instanceof Feature) return descriptorDistance((Feature) vd);
        else return -1f;
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("sift");
        sb.append(' ');
        sb.append(scale);
        sb.append(' ');
        sb.append(orientation);
        sb.append(' ');
        // we assume that the location is 2D:
        assert (location.length == 2);
        sb.append(location[0]);
        sb.append(' ');
        sb.append(location[1]);
        sb.append(' ');
        // we add the descriptor: (default size == 4*4*8
        for (int i = 0; i < descriptor.length; i++) {
            sb.append(descriptor[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s, " ");
        if (!st.nextToken().equals("sift")) {
            logger.warning("This is not a SIFT feature.");
            return;
        }
        scale = Float.parseFloat(st.nextToken());
        orientation = Float.parseFloat(st.nextToken());
        location = new float[2];
        location[0] = Float.parseFloat(st.nextToken());
        location[1] = Float.parseFloat(st.nextToken());

        // parse descriptor:
        LinkedList<Float> descVals = new LinkedList<Float>();
        while (st.hasMoreTokens()) descVals.add(Float.parseFloat(st.nextToken()));

        // set descriptor:
        descriptor = new float[descVals.size()];
        for (int i = 0; i < descriptor.length; i++) {
            descriptor[i] = descVals.get(i);
        }
    }
}

