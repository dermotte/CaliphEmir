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

import at.lux.imageanalysis.VisualDescriptor;


/**
 * Date: 13.01.2005
 * Time: 22:47:01
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class VisualDescriptorDistanceCalculator extends DistanceCalculator {

    /**
     * Allows the distance calculations based on visual descriptors.
     *
     * @param o1
     * @param o2
     * @return
     */
    public double getDistance(Object o1, Object o2) {
        if (o1 instanceof VisualDescriptor && o2 instanceof VisualDescriptor) {
            VisualDescriptor c1 = (VisualDescriptor) o1;
            VisualDescriptor c2 = (VisualDescriptor) o2;
            return c1.getDistance(c2);
        } else {
            return -1d;
        }
    }
}
