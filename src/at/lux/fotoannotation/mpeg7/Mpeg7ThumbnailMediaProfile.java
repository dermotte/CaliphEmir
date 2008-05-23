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

import at.lux.fotoannotation.AnnotationToolkit;
import org.jdom.Element;

import java.awt.image.BufferedImage;
import java.io.File;

public class Mpeg7ThumbnailMediaProfile {
    /*
    <MediaProfile>
        <MediaFormat>
            <Content href="image"/>
            <FileFormat href="urn:mpeg:MPEG7FileFormatCS:1">
                <Name>JPEG</Name>
            </FileFormat>
            <VisualCoding>
                <Format href="urn:mpeg:MPEG7FileFormatCS:1" colorDomain="color">
                    <Name>JPEG</Name>
                </Format>
                <Pixel bitsPer="24"/>
                <Frame width="864" height="1152"/>
            </VisualCoding>
        </MediaFormat>
        <MediaInstance>
            <InstanceIdentifier/>
            <MediaLocator>
                <MediaUri>file:/G:/JavaProjects/EDVProject/testdaten/sommerfest/KnowSommerfest2002_01.JPG</MediaUri>
            </MediaLocator>
        </MediaInstance>
    </MediaProfile>
    */

    private Mpeg7MediaFormat format;
    private Element instance;
    private Element root;

    public Mpeg7ThumbnailMediaProfile(File f, BufferedImage img) {
        format = new Mpeg7MediaFormat("24", "JPEG", f.length() + "", img.getWidth() + "", img.getHeight() + "");
        instance = AnnotationToolkit.getMpeg7MediaInstance(f);
        root = new Element("MediaProfile", instance.getNamespace());
        root.addContent(format.createDocument());
        root.addContent(instance);
    }

    public Mpeg7ThumbnailMediaProfile(File f, int width, int height) {
        format = new Mpeg7MediaFormat("24", "JPEG", f.length() + "", width + "", height + "");
        instance = AnnotationToolkit.getMpeg7MediaInstance(f);
        root = new Element("MediaProfile", instance.getNamespace());
        root.addContent(format.createDocument());
        root.addContent(instance);
    }

    public Element createDocument() {
        return root;
    }
}
