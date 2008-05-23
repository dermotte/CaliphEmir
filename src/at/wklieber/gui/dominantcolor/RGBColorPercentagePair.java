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

public class RGBColorPercentagePair {
    private int[] rgbColor;
    private double percentage;
    private Color color;

    public RGBColorPercentagePair(int[] rgbColor, double percentage) {
        this.rgbColor = rgbColor;
        this.percentage = percentage;
        color = new Color(rgbColor[0], rgbColor[1], rgbColor[2]);
    }

    public RGBColorPercentagePair(Color rgbColor, double percentage) {
        this.color = rgbColor;
        this.percentage = percentage;
        this.rgbColor = new int[3];
        this.rgbColor[0] = color.getRed();
        this.rgbColor[1] = color.getGreen();
        this.rgbColor[2] = color.getBlue();
    }

    public int[] getRGBColor() {
        return rgbColor;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public void setRGBColor(int[] RGBColor) {
        this.rgbColor = RGBColor;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String toString() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return (df.format(percentage * 100.0) + "% [" + rgbColor[0] + "," + rgbColor[1] + "," + rgbColor[2] + "]");
    }
}
