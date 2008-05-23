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
 * (c) 2005 by Mathias Lux (mathias@juggle.at)
 * http://caliph-emir.sourceforge.net
 *
 * Note: The implementation is based on the code of Wolfgang Seiringer,
 * w.seiringer@utanet.at, who published the source without license or
 * copyright notice 
 */
package at.lux.imageanalysis;

import org.jdom.Element;
import org.jdom.Namespace;

import java.awt.image.BufferedImage;
import java.io.StringWriter;
import java.util.*;

/**
 * Date: 03.10.2005
 * Time: 19:57:18
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DominantColor implements JDomVisualDescriptor {
    at.lux.imageanalysis.DominantColorImplementation dc;
    private at.lux.imageanalysis.DominantColorImplementation.DominantColorValues dcData = null;

    /**
     * Td is the maximum distance for two colors to be considered similar, and dmax = alpha*Td.
     * A normal value for Td is between 10-20 in the CIE LUV color space and for alpha is
     * between 1.0-1.5.
     */
    private static double alpha = 1.0;
    private static double Td = 10;
    /**
     * Where SC_Diff = abs(SpatialCoherency_1 – SpatialCoherency_2), SpatialCoherency_1 and
     * SpatialCoherency _2 are normalized from 0 to 1 and non-uniformly quantized in prior
     * to calculate SC_Diff. DC_Diff is the difference between two set of dominant colors,
     * W1 (e.g. set to 0.3) is the weight of the first term and W2 (e.g. set to 0.7) is the
     * weight of the second term. Set W1 to 0 if SpatialCoherency is not available.
     */
    private static double w1 = 0.0;

    private double percentageSum = 0d;

    /**
     * Main constructor for creating and extracting the DominantColor
     * descriptor.
     *
     * @param img gives the image data.
     */
    public DominantColor(BufferedImage img) {
        dc = new at.lux.imageanalysis.DominantColorImplementation();
        dcData = dc.extractDescriptor(img);
        for (int i = 0; i < this.getSize(); i++) {
            percentageSum += ((double) getPercentage(i));
        }
    }

    /**
     * Use this constructor if the pixel data was taken from
     * a non rectangular region. Please note that the neighbourhood
     * of pixels cannot be taken into account (No reliable spatial
     * coherency can be created). The pixels array goes like:
     * {pixel1[0], pixel1[1], pixel1[2], pixel2[0], pixel2[1], ...}
     *
     * @param pixels gives the pixels one int after another.
     */
    public DominantColor(int[] pixels) {
        dc = new at.lux.imageanalysis.DominantColorImplementation();
        dcData = dc.extractDescriptor(pixels);
        for (int i = 0; i < this.getSize(); i++) {
            percentageSum += ((double) getPercentage(i));
        }
    }

    public DominantColor(Element descriptor) throws VisualDescriptorException {
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        if (!descriptor.getAttribute("type", xsi).getValue().equals("DominantColorType")) {
            throw new VisualDescriptorException("This is not a DominantColor descriptor!");
        } else {
            dcData = new at.lux.imageanalysis.DominantColorImplementation.DominantColorValues();
            String spatialCoh = descriptor.getChild("SpatialCoherency", mpeg7).getTextTrim();
            dcData.setCoherency(Integer.parseInt(spatialCoh));

            List values = descriptor.getChildren("Values", mpeg7);
            ArrayList<String> percentages = new ArrayList<String>();
            LinkedList<String> colorValues = new LinkedList<String>();

            for (Iterator iterator = values.iterator(); iterator.hasNext();) {
                Element element = (Element) iterator.next();
                percentages.add(element.getChild("Percentage", mpeg7).getTextTrim());
                colorValues.add(element.getChild("ColorValueIndex", mpeg7).getTextTrim());
            }
            int count = 0;
            int[][] colorValuesArray = new int[colorValues.size()][3];
            int[] percentagesArray = new int[colorValues.size()];
            for (Iterator<String> iterator = colorValues.iterator(); iterator.hasNext();) {
                StringTokenizer st = new StringTokenizer(iterator.next());
                int[] v = new int[3];
                v[0] = Integer.parseInt(st.nextToken());
                v[1] = Integer.parseInt(st.nextToken());
                v[2] = Integer.parseInt(st.nextToken());
                colorValuesArray[count] = v;

                percentagesArray[count] = Integer.parseInt(percentages.get(count));

                count++;
            }
            dcData.setSize(colorValues.size());
            dcData.setPercentages(percentagesArray);
            dcData.setColorValues(colorValuesArray);
        }
    }

    /**
     * Compares one descriptor to another.
     *
     * @param descriptor
     * @return the distance from [0,infinite) or -1 if descriptor type does not match
     */
    public float getDistance(VisualDescriptor descriptor) {
        if (descriptor instanceof DominantColor) {
            DominantColor dc2 = (DominantColor) descriptor;
            double sc_diff = Math.abs(dc2.getSpatialCoherency() - this.getSpatialCoherency());

            double dc_diff = 0;
            double firstSquare = 0;
            double secondSquare = 0;
            for (int i = 0; i < this.getSize(); i++) {
                firstSquare += (this.getPercentageNormalized(i) * this.getPercentageNormalized(i));
            }
            for (int i = 0; i < dc2.getSize(); i++) {
                secondSquare += (dc2.getPercentageNormalized(i) * dc2.getPercentageNormalized(i));
            }
            double dmax = alpha * Td;
            for (int i = 0; i < this.getSize(); i++) {
                for (int j = 0; j < dc2.getSize(); j++) {
                    double aij = 0;
                    double dkl = euclid(this.getColorValue(i), dc2.getColorValue(j));
                    if (dkl <= Td) {
                        aij = 1 - dkl / dmax;
//                        System.out.println(dkl + " " + Td +  " " + aij);
                    }
                    dc_diff += aij * ((double) this.getPercentageNormalized(i)) * ((double) dc2.getPercentageNormalized(j));
                }
            }
            dc_diff = firstSquare + secondSquare - 2 * dc_diff;
            return (float) (w1 * sc_diff * dc_diff + (1.0 - w1) * dc_diff);
        } else {
            return -1f;
        }
    }

    /**
     * Euclidean distance between the colors ...
     *
     * @param colorValue
     * @param colorValue1
     * @return a double.
     */
    private double euclid(int[] colorValue, int[] colorValue1) {
        double result = 0;
        for (int i = 0; i < colorValue.length; i++) {
            double quad = (double) (colorValue[i] - colorValue1[i]);
            result += (quad * quad);
        }
        Math.sqrt(result);
        return result;
    }

    /**
     * Returns the descriptor as JDOM Element
     *
     * @return the descriptor as JDOM Element.
     */
    public Element getDescriptor() {
//        System.out.println(dc.toXMLString());
        // TODO: generate real descriptor similar to the one created by the other descriptors
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element vdesc = new Element("VisualDescriptor", mpeg7).setAttribute("type", "DominantColorType", xsi);
        // vdesc.setAttribute("size", Integer.toString(getSize()), mpeg7);

        Element spatialCoherency = new Element("SpatialCoherency", mpeg7);
        vdesc.addContent(spatialCoherency);
        spatialCoherency.setText(getSpatialCoherency() + "");

        for (int i = 0; i < getSize(); i++) {
            Element values = new Element("Value", mpeg7);
            Element percentage = new Element("Percentage", mpeg7);
            Element colorValueIndex = new Element("Index", mpeg7);

            values.addContent(percentage);
            percentage.setText(Integer.toString(getPercentage(i)));
            values.addContent(colorValueIndex);
            int[] colorValue = getColorValue(i);
            StringWriter sw = new StringWriter(colorValue.length * 3);
            for (int j = 0; j < colorValue.length; j++) {
                int value = colorValue[j];
                sw.append(Integer.toString(value));
                sw.append(" ");
            }
            colorValueIndex.setText(sw.toString().trim());

            vdesc.addContent(values);
        }

        return vdesc;
    }

    public int getSize() {
        return dcData.getSize();
    }

    public int getSpatialCoherency() {
        return dcData.getSpatialCoherency();
    }

    public int getPercentage(int index) {
        return dcData.getPercentage(index);
    }

    /**
     * Normalize the percentage as in MPEG-7 part 8 is recommended for
     * distance calculation
     *
     * @param index defines which color percentage is needed.
     * @return the normalized percentage: The sum of all percentages adds up to 1.
     */
    public double getPercentageNormalized(int index) {
        double percentage = dcData.getPercentage(index);
        return (percentage / percentageSum);
    }

    public int[] getColorValue(int index) {
        return dcData.getColorValue(index);
    }

    public String getStringRepresentation() {
        // TODO: implement this ..
        return null;
    }

    public void setStringRepresentation(String descriptor) {
        // TODO: implement this
    }

}
