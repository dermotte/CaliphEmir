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

import java.util.Date;

/**
 * Date: 14.01.2005
 * Time: 23:48:14
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DateDistanceCalculator extends DistanceCalculator {
    /**
     * Calculates and returns the distance between two objects. Please note that the
     * distance function has to be symmetric and must obey the triangle inequality.
     * This method is the same as {@link #getDistance(Object, Object, int, float[], float[])}
     * with a k=0.
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @return the distance as float from [0, infinite) or -1 if objects distance cannot be computes
     */
    public float getDistance(Object o1, Object o2) {
        if (o1 instanceof Date && o2 instanceof Date) {
            Date d1, d2;
            d1 = (Date) o1;
            d2 = (Date) o2;
            return (float) Math.abs(d1.getTime() - d2.getTime());
        } else
            return -1f;
    }
}
