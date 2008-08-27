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
import java.util.StringTokenizer;

/**
 * Created by: Mathias Lux, mathias@juggle.at
 * Date: 13.09.2004
 * Time: 12:50:44
 */
public class EdgeHistogram extends EdgeHistogramImplementation implements JDomVisualDescriptor {
    public EdgeHistogram(String descriptor) {
        super(descriptor);
    }

    public EdgeHistogram(BufferedImage image) {
        super(image);
    }

    public EdgeHistogram() {
        super();
    }

    public EdgeHistogram(Element descriptor) throws VisualDescriptorException {
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
//        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Element bins = descriptor.getChild("BinCounts", mpeg7);
        int[] hist = new int[80];
        if (bins != null) {
            StringTokenizer st = new StringTokenizer(bins.getTextTrim());
            int count = 0;
            do {
                hist[count] = Integer.parseInt(st.nextToken());
                count++;
            } while (st.hasMoreTokens());
            if (count < 80)
                throw new VisualDescriptorException("Edgehistogram has less than 80 bins - that too less!");
            else
                edgeHistogram = hist;
        } else {
            throw new VisualDescriptorException("This is no EdgeHistogram Descriptor!");
        }
    }

    private Element createXML() {
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        org.jdom.Element descriptor = new Element("VisualDescriptor", mpeg7);
        org.jdom.Element BinCounts = new Element("BinCounts", mpeg7);
        descriptor.addContent(BinCounts);
        descriptor.setAttribute("type", "EdgeHistogramType", xsi);
        StringBuilder bc = new StringBuilder();
        bc.append(edgeHistogram[0]);
        for (int i = 1; i < edgeHistogram.length; i++) {
            bc.append(' ');
            bc.append(edgeHistogram[i]);
        }
        BinCounts.setText(bc.toString());
        return descriptor;
    }

    public Element getDescriptor() {
        return createXML();
    }

    public int[] getHistogram() {
        return edgeHistogram;
    }
}
