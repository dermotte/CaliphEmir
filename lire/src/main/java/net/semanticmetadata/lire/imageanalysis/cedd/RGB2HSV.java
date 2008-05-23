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
 * (c) 2008 by Savvas A. Chatzichristofis, savvash@gmail.com
 */

package net.semanticmetadata.lire.imageanalysis.cedd;

public class RGB2HSV {

    public int[] ApplyFilter(int red, int green, int blue) {
        int[] Results = new int[3];
        int HSV_H = 0;
        int HSV_S = 0;
        int HSV_V = 0;

        double MaxHSV = (Math.max(red, Math.max(green, blue)));
        double MinHSV = (Math.min(red, Math.min(green, blue)));

        //Παραγωγη Του V του HSV
        HSV_V = (int) (MaxHSV);

        //Παραγωγη Του S του HSV
        HSV_S = 0;
        if (MaxHSV != 0) HSV_S = (int) (255 - 255 * (MinHSV / MaxHSV));

        //Παραγωγη Του H
        if (MaxHSV != MinHSV) {

            int IntegerMaxHSV = (int) (MaxHSV);

            if (IntegerMaxHSV == red && green >= blue) {
                HSV_H = (int) (60 * (green - blue) / (MaxHSV - MinHSV));
            } else if (IntegerMaxHSV == red && green < blue) {
                HSV_H = (int) (359 + 60 * (green - blue) / (MaxHSV - MinHSV));
            } else if (IntegerMaxHSV == green) {
                HSV_H = (int) (119 + 60 * (blue - red) / (MaxHSV - MinHSV));
            } else if (IntegerMaxHSV == blue) {
                HSV_H = (int) (239 + 60 * (red - green) / (MaxHSV - MinHSV));
            }


        } else HSV_H = 0;

        Results[0] = HSV_H;
        Results[1] = HSV_S;
        Results[2] = HSV_V;

        return (Results);
    }
}
