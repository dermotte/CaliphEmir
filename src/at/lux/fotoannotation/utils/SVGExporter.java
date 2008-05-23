package at.lux.fotoannotation.utils;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.io.*;
import java.awt.*;
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

/**
 * This file is part of Caliph & Emir
 * Date: 06.01.2006
 * Time: 18:40:10
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SVGExporter {
    private final SVGGraphics2D svgGenerator;

    public SVGExporter() {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);
        svgGenerator = new SVGGraphics2D(document);
    }

    public void writeFile(File file) throws Exception {
        boolean useCSS = true;
        FileOutputStream fos = new FileOutputStream(file, false);
        Writer out = new OutputStreamWriter(fos, "UTF-8");
        svgGenerator.stream(out, useCSS);
    }

    public Graphics2D getGraphics() {
        return svgGenerator;
    }
}
