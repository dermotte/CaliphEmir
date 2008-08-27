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
package at.lux.imageanalysis;

import org.jdom.Element;
import org.jdom.Namespace;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * Class for extrcating & comparing MPEG-7 based CBIR descriptor ColorLayout
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ColorLayout extends ColorLayoutImpl implements JDomVisualDescriptor {

    public ColorLayout(BufferedImage image) {
        super(image);
    }

    public ColorLayout(int numberOfYCoeff, int numberOfCCoeff, BufferedImage image) {
        super(numberOfYCoeff, numberOfCCoeff, image);
    }

    public ColorLayout() {
        super();
    }

    /**
     * Create a ColorLayout Object from its descriptor
     *
     * @param descriptor the descriptor as JDOM Element
     */
    public ColorLayout(Element descriptor) {
        this.img = null;
        YCoeff = new int[64];
        CbCoeff = new int[64];
        CrCoeff = new int[64];
        colorLayoutImage = null;

        Vector v = getCoeffs(descriptor);
        if (v != null) {
            int[] y = (int[]) v.get(0);
            int[] cb = (int[]) v.get(1);
            int[] cr = (int[]) v.get(2);
            for (int i = 0; i < 64; i++) {
                if (i < y.length) {
                    YCoeff[i] = y[i];
                } else {
                    YCoeff[i] = 16;
                }
                if (i < cb.length) {
                    CbCoeff[i] = cb[i];
                    CrCoeff[i] = cr[i];
                } else {
                    CbCoeff[i] = 16;
                    CrCoeff[i] = 16;
                }

            }
        } else {
//            debug("Descriptor not valid!!!");
        }
    }

    public Element getDescriptor() {
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element vdesc = new Element("VisualDescriptor", mpeg7).setAttribute("type", "ColorLayoutType", xsi);
        // Ersten Werte:
        Element ydc, cbdc, crdc;
        ydc = new Element("YDCCoeff", mpeg7).addContent(Integer.toString(YCoeff[0]));
        cbdc = new Element("CbDCCoeff", mpeg7).addContent(Integer.toString(CbCoeff[0]));
        crdc = new Element("CrDCCoeff", mpeg7).addContent(Integer.toString(CrCoeff[0]));
        vdesc.addContent(ydc);
        vdesc.addContent(cbdc);
        vdesc.addContent(crdc);
        if (numYCoeff > 1) {
            Element yac = new Element("YACCoeff" + (numYCoeff - 1), mpeg7);
            StringBuilder b = new StringBuilder(256);
            for (int i = 1; i < numYCoeff; i++) {
                b.append(YCoeff[i]).append(' ');
            }
            yac.setText(b.toString().trim());
            vdesc.addContent(yac);
        }
        if (numCCoeff > 1) {
            Element cbac = new Element("CbACCoeff" + (numCCoeff - 1), mpeg7);
            Element crac = new Element("CrACCoeff" + (numCCoeff - 1), mpeg7);
            StringBuilder bcb, bcr;
            bcb = new StringBuilder(256);
            bcr = new StringBuilder(256);
            for (int i = 1; i < numCCoeff; i++) {
                bcb.append(CbCoeff[i]).append(' ');
                bcr.append(CrCoeff[i]).append(' ');
            }
            cbac.setText(bcb.toString().trim());
            crac.setText(bcr.toString().trim());

            vdesc.addContent(cbac);
            vdesc.addContent(crac);
        }

        return vdesc;
    }

    public static double getSimilarity(Element c1, Element c2) {
        double val = -1.0;
        int YCoeff1, YCoeff2, CCoeff1, CCoeff2, YCoeff, CCoeff;
        Vector v1 = getCoeffs(c1);
        Vector v2 = getCoeffs(c2);
        int[] y1, cb1, cr1, y2, cb2, cr2;
        if (v1 != null && v2 != null) { // valid??
            y1 = (int[]) v1.get(0);
            cb1 = (int[]) v1.get(1);
            cr1 = (int[]) v1.get(2);
            y2 = (int[]) v2.get(0);
            cb2 = (int[]) v2.get(1);
            cr2 = (int[]) v2.get(2);
            val = getSimilarity(y1, cb1, cr1, y2, cb2, cr2);
        }
        return val;
    }

    /**
     * Takes two ColorLayout DS and calculates similarity.
     *
     * @return Vector of int[] (yCoeff at Vector.get(0), cbCoeff at Vector.get(1), crCoeff cbCoeff at Vector.get(2)) or null if not valid ColorLayoutDS
     */
    public static Vector getCoeffs(Element descriptor) {
        Vector vals = null;
        int[] y, cb, cr;
        int numY = 0;
        int numC = 0;
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
//        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        boolean isValid = false;

        if (descriptor.getChild("YDCCoeff", mpeg7) != null && descriptor.getChild("CbDCCoeff", mpeg7) != null && descriptor.getChild("CrDCCoeff", mpeg7) != null) {
            isValid = true;
            numC = 1;
            numY = 1;
        }
        if (isValid) {
            StringBuilder str_y = new StringBuilder(256);
            StringBuilder str_cb = new StringBuilder(256);
            StringBuilder str_cr = new StringBuilder(256);
            str_y.append(descriptor.getChildTextTrim("YDCCoeff", mpeg7)).append(' ');
            str_cb.append(descriptor.getChildTextTrim("CbDCCoeff", mpeg7)).append(' ');
            str_cr.append(descriptor.getChildTextTrim("CrDCCoeff", mpeg7)).append(' ');
            java.util.List l = descriptor.getChildren();
            for (Iterator i = l.iterator(); i.hasNext();) {
                Element e = (Element) i.next();
                if (e.getName().startsWith("YACCoeff")) {
                    numY = Integer.parseInt(e.getName().substring(8)) + 1;
                    str_y.append(e.getTextTrim());
                } else if (e.getName().startsWith("CbACCoeff")) {
                    numC = Integer.parseInt(e.getName().substring(9)) + 1;
                    str_cb.append(e.getTextTrim());
                } else if (e.getName().startsWith("CrACCoeff")) {
                    numC = Integer.parseInt(e.getName().substring(9)) + 1;
                    str_cr.append(e.getTextTrim());
                }
            }

            // getting y-coeff:
            y = new int[numY];
            cb = new int[numC];
            cr = new int[numC];

//            debug("NumYCoeffs: " + numY + ", NumCCoeffs: " + numC);

            StringTokenizer st = new StringTokenizer(str_y.toString().trim(), " ");
            int countSteps = 0;
            while (st.hasMoreElements()) {
                y[countSteps] = Integer.parseInt(st.nextToken());
                countSteps++;
            }
            st = new StringTokenizer(str_cb.toString().trim(), " ");
            countSteps = 0;
            while (st.hasMoreElements()) {
                cb[countSteps] = Integer.parseInt(st.nextToken());
                countSteps++;
            }
            st = new StringTokenizer(str_cr.toString().trim(), " ");
            countSteps = 0;
            while (st.hasMoreElements()) {
                cr[countSteps] = Integer.parseInt(st.nextToken());
                countSteps++;
            }
            vals = new Vector(3);
            vals.add(y);
            vals.add(cb);
            vals.add(cr);
        }
        return vals;
    }
}
