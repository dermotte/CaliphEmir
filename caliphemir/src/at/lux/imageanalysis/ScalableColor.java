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

import java.util.StringTokenizer;
import java.awt.image.BufferedImage;

/**
 * ScalableColor
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ScalableColor extends ScalableColorImpl implements JDomVisualDescriptor {
    public ScalableColor(String descriptor) {
        super(descriptor);
    }

    public ScalableColor(BufferedImage image) {
        super(image);
    }

    public ScalableColor(BufferedImage image, int NumberOfCoefficients, int NumberOfBitplanesDiscarded) {
        super(image, NumberOfCoefficients, NumberOfBitplanesDiscarded);
    }

    /**
     * Use this constructor if the pixel data was taken from
     * a non rectangular region. Please note that the neighbourhood
     * of pixels cannot be taken into account (No reliable spatial
     * coherency can be created). The pixels array goes like:
     * {pixel1[0], pixel1[1], pixel1[2], pixel2[0], pixel2[1], ...}<br>
     * please note that althought
     *
     * @param pixels gives the pixels one int after another.
     */
    public ScalableColor(int[] pixels) {
        super(pixels);
    }

    public ScalableColor(Element Descriptor) {
        this.img = null;
        this.NumberOfBitplanesDiscarded = 0;
        this.NumberOfCoefficients = 256;

        String numCoeff = Descriptor.getAttributeValue("numOfCoeff");
        String numBitPlanes = Descriptor.getAttributeValue("numOfBitplanesDiscarded");

        if (numCoeff != null && numBitPlanes != null) {
            this.NumberOfBitplanesDiscarded = Integer.parseInt(numBitPlanes);
            this.NumberOfCoefficients = Integer.parseInt(numCoeff);
            String coeffs = Descriptor.getChildText("Coeff", Descriptor.getNamespace());
            haarTransformedHistogram = new int[NumberOfCoefficients];
            StringTokenizer st = new StringTokenizer(coeffs, " ", false);
            int count = 0;
            while (st.hasMoreElements()) {
                String s = st.nextToken();
                haarTransformedHistogram[count] = Integer.parseInt(s.trim());
                count++;
            }
        } else {
            System.out.println("Error: Couldn't read both attributes numOfBitplanesDiscarded and numOfCoeff");
            haarTransformedHistogram = null;
        }

        _xSize = 0;
        _ySize = 0;
        init();
    }

    public Element getDescriptor() {
        /*
          <VisualDescriptor xsi:type="ScalableColorType" numOfBitplanesDiscarded="1" numOfCoeff="16">
	        <Coeff>2 0 12 3 1</Coeff>
		  </VisualDescriptor>
        */
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element vdesc = new Element("VisualDescriptor", mpeg7).setAttribute("type", "ScalableColorType", xsi);
        vdesc.setAttribute("numOfBitplanesDiscarded", Integer.toString(NumberOfBitplanesDiscarded));
        vdesc.setAttribute("numOfCoeff", Integer.toString(NumberOfCoefficients));

        Element coeff = new Element("Coeff", mpeg7);
        vdesc.addContent(coeff);

        StringBuilder buff = new StringBuilder(1024);
        for (int i = 0; i < NumberOfCoefficients; i++) {
            buff.append(haarTransformedHistogram[i]).append(' ');
        }

        coeff.setText(buff.toString().trim());

        return vdesc;
    }
}
