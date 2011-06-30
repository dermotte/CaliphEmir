/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
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
 * (c) 2002-2008 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */
package net.semanticmetadata.lire.imageanalysis;

import at.lux.imageanalysis.ColorLayoutImpl;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Just a wrapper for the use of LireFeature.
 * Date: 27.08.2008
 * Time: 12:07:38
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ColorLayout extends ColorLayoutImpl implements LireFeature {

    /*
        public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(256);
        StringBuilder sbtmp = new StringBuilder(256);
        for (int i = 0; i < numYCoeff; i++) {
            sb.append(YCoeff[i]);
            if (i + 1 < numYCoeff) sb.append(' ');
        }
        sb.append("z");
        for (int i = 0; i < numCCoeff; i++) {
            sb.append(CbCoeff[i]);
            if (i + 1 < numCCoeff) sb.append(' ');
            sbtmp.append(CrCoeff[i]);
            if (i + 1 < numCCoeff) sbtmp.append(' ');
        }
        sb.append("z");
        sb.append(sbtmp);
        return sb.toString();
    }

    public void setStringRepresentation(String descriptor) {
        String[] coeffs = descriptor.split("z");
        String[] y = coeffs[0].split(" ");
        String[] cb = coeffs[1].split(" ");
        String[] cr = coeffs[2].split(" ");

        numYCoeff = y.length;
        numCCoeff = Math.min(cb.length, cr.length);

        YCoeff = new int[numYCoeff];
        CbCoeff = new int[numCCoeff];
        CrCoeff = new int[numCCoeff];

        for (int i = 0; i < numYCoeff; i++) {
            YCoeff[i] = Integer.parseInt(y[i]);
        }
        for (int i = 0; i < numCCoeff; i++) {
            CbCoeff[i] = Integer.parseInt(cb[i]);
            CrCoeff[i] = Integer.parseInt(cr[i]);

        }
    }
     */
    /**
     * Provides a much faster way of serialization.
     * @return a byte array that can be read with the corresponding method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[2*4+numYCoeff*4+2*numCCoeff*4];
        System.arraycopy(SerializationUtils.toBytes(numYCoeff), 0, result, 0, 4);
        System.arraycopy(SerializationUtils.toBytes(numCCoeff), 0, result, 4, 4);
        System.arraycopy(SerializationUtils.toByteArray(YCoeff), 0, result, 8, numYCoeff*4);
        System.arraycopy(SerializationUtils.toByteArray(CbCoeff), 0, result, numYCoeff*4+8, numCCoeff*4);
        System.arraycopy(SerializationUtils.toByteArray(CrCoeff), 0, result, numYCoeff*4+numCCoeff*4+8, numCCoeff*4);
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     * @param in byte array from corresponding method
     */
    public void setByteArrayRepresentation(byte[] in) {
        int[] data = SerializationUtils.toIntArray(in);
        numYCoeff = data[0];
        numCCoeff = data[1];
        YCoeff = new int[numYCoeff];
        CbCoeff = new int[numCCoeff];
        CrCoeff = new int[numCCoeff];
        System.arraycopy(data, 2, YCoeff, 0, numYCoeff);
        System.arraycopy(data, 2 + numYCoeff, CbCoeff, 0, numCCoeff);
        System.arraycopy(data, 2 + numYCoeff + numCCoeff, CrCoeff, 0, numCCoeff);
    }
}
