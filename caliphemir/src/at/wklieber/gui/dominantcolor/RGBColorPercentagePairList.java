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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.gui.dominantcolor;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

public class RGBColorPercentagePairList extends Vector {
    private Point upperLeftCorner, bottomRightCorner;

    public synchronized boolean add(RGBColorPercentagePair o) {
        int i = 0;
        int index = 0;
        Enumeration e = this.elements();
        while (e.hasMoreElements()) {
            RGBColorPercentagePair cp = (RGBColorPercentagePair) e.nextElement();
            if (!(o.getPercentage() > cp.getPercentage()))
                index = i + 1;
            i++;
        }
        super.add(index, o);
        return true;
    }

    public synchronized String toString() {
        StringBuffer buffer = new StringBuffer();
        Enumeration e = this.elements();
        DecimalFormat df = new DecimalFormat();
        df.setMinimumIntegerDigits(2);
        df.setMinimumFractionDigits(4);
        df.setMaximumFractionDigits(4);
        while (e.hasMoreElements()) {
            RGBColorPercentagePair cp = (RGBColorPercentagePair) e.nextElement();
            buffer.append(df.format(cp.getPercentage() * 100.0) + "% - [" + cp.getRGBColor()[0] + "," + cp.getRGBColor()[1] + "," + cp.getRGBColor()[2] + "]\n");
        }
        return buffer.toString();
    }

    public Point getUpperLeftCorner() {
        return upperLeftCorner;
    }

    public void setUpperLeftCorner(Point upperLeftCorner) {
        this.upperLeftCorner = upperLeftCorner;
    }

    public Point getBottomRightCorner() {
        return bottomRightCorner;
    }

    public void setBottomRightCorner(Point bottomRightCorner) {
        this.bottomRightCorner = bottomRightCorner;
    }
}
