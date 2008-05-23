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
package at.lux.fotoannotation.mpeg7;

import org.jdom.Element;
import org.jdom.Namespace;

public class Mpeg7MediaFormat {
    private String fileFormat, fileSize, bitsPerPixel, imgWidth, imgHeight, colorDomain;
    private Element root;
    private Namespace mpeg7, xsi;

    public Mpeg7MediaFormat(String bitsPerPixel, String fileFormat, String fileSize, String imgHeight, String imgWidth) {
        this.bitsPerPixel = bitsPerPixel;
        this.fileFormat = fileFormat;
        this.fileSize = fileSize;
        this.imgHeight = imgHeight;
        this.imgWidth = imgWidth;
        this.colorDomain = null;
        init();
    }

    private void init() {
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root = new Element("MediaFormat", mpeg7).addContent(new Element("Content", mpeg7).setAttribute("href", "image"));

        root.addContent(new Element("FileFormat", mpeg7).setAttribute("href", "urn:mpeg:MPEG7FileFormatCS:1").addContent(new Element("Name", mpeg7).addContent(fileFormat)));

        Element vcoding = new Element("VisualCoding", mpeg7);
        Element format = new Element("Format", mpeg7).setAttribute("href", "urn:mpeg:MPEG7FileFormatCS:1");
        if (colorDomain != null)
            format.setAttribute("colorDomain", colorDomain);
        else
            format.setAttribute("colorDomain", "color");
        Element name = new Element("Name", mpeg7).addContent(fileFormat);
        format.addContent(name);

        vcoding.addContent(format);
        if (bitsPerPixel != null) {
            vcoding.addContent(new Element("Pixel", mpeg7).setAttribute("bitsPer", bitsPerPixel));
        }
        if (imgHeight != null && imgWidth != null) {
            vcoding.addContent(new Element("Frame", mpeg7).setAttribute("width", imgWidth).setAttribute("height", imgHeight));
        }
        root.addContent(vcoding);

    }

    public Element createDocument() {
        return root;
    }
    /*
    <MediaFormat>
        <Content href="image"/>
        <FileFormat href="urn:mpeg:MPEG7FileFormatCS:1">
            <Name>JPEG</Name>
        </FileFormat>
        <FileSize>1024</FileSize>
        <VisualCoding>
            <Format colorDomain="color" href="urn:mpeg:MPEG7FileFormatCS:1">
                <Name>JPEG</Name>
            </Format>
            <Pixel bitsPer="24"/>
            <Frame height="768" width="1024"/>
        </VisualCoding>
    </MediaFormat>
*/
}
