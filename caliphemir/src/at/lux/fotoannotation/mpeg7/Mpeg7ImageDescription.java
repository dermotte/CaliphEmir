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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Iterator;

public class Mpeg7ImageDescription {
    private Element descMetadata, mediaFormat, mediaQuality, mediaInstance, thumbNails;
    private Element creationInformation, visualDescriptor, textAnnotation, semantics;

    private Element mediaProfile;

    private Document doc;
    private Element root;
    private Namespace mpeg7, xsi;

    public Mpeg7ImageDescription(Element creationInformation, Element descMetadata, Element mediaFormat,
                                 Element mediaInstance, Element mediaQuality, Element thumbNail, Element semantics, Element textAnnotation,
                                 Element visualDescriptor) {
        this.creationInformation = creationInformation;
        this.descMetadata = descMetadata;
        this.mediaFormat = mediaFormat;
        this.mediaInstance = mediaInstance;
        this.mediaProfile = null;
        this.thumbNails = thumbNail;
        this.mediaQuality = mediaQuality;
        this.semantics = semantics;
        this.textAnnotation = textAnnotation;
        this.visualDescriptor = visualDescriptor;
    }

    public Mpeg7ImageDescription(Element creationInformation, Element descMetadata, Element mediaProfile,
                                 Element semantics, Element textAnnotation, Element visualDescriptor) {
        this.creationInformation = creationInformation;
        this.descMetadata = descMetadata;
        this.mediaFormat = null;
        this.mediaInstance = null;
        this.mediaProfile = mediaProfile;
        this.mediaQuality = null;
        this.thumbNails = null;
        this.semantics = semantics;
        this.textAnnotation = textAnnotation;
        this.visualDescriptor = visualDescriptor;
    }

    public Mpeg7ImageDescription(Element descMetadata, Element mediaInstance,
                                 Element mediaQuality, Element mediaFormat, Element textAnnotation) {
        this.creationInformation = null;
        this.descMetadata = descMetadata;
        this.mediaFormat = mediaFormat;
        this.mediaInstance = mediaInstance;
        this.mediaProfile = null;
        this.mediaQuality = mediaQuality;
        this.semantics = null;
        this.thumbNails = null;
        this.textAnnotation = textAnnotation;
        this.visualDescriptor = null;
    }

    private void init() {
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        root = new Element("Mpeg7", mpeg7);
        root.addNamespaceDeclaration(mpeg7);
        root.addNamespaceDeclaration(xsi);

        doc = new Document(root);
        // DescriptionMetadata
        if (descMetadata != null)
            root.addContent(descMetadata);

        // setting up structure to Description/MultimediaContent/Image
        Element desc = new Element("Description", mpeg7);
        root.addContent(desc);
        desc.setAttribute("type", "ContentEntityType", xsi);
        Element mumeContent = new Element("MultimediaContent", mpeg7);
        mumeContent.setAttribute("type", "ImageType", xsi);
        Element img = new Element("Image", mpeg7);
        desc.addContent(mumeContent.addContent(img));

        // Profile
        if (mediaProfile != null) {
            img.addContent(new Element("MediaInformation", mpeg7).addContent(mediaProfile));
        } else {
            Element profile = new Element("MediaProfile", mpeg7);
            profile.setAttribute("master", "true");
            img.addContent(new Element("MediaInformation", mpeg7).addContent(profile));
            if (mediaFormat != null) profile.addContent(mediaFormat);
            if (mediaQuality != null) profile.addContent(mediaQuality);
            if (mediaInstance != null) profile.addContent(mediaInstance);
        }

        // Falls ein Thumbnail existiert kommt er als 2. Profil rein, das erste ist der master :)
        if (thumbNails != null) {
            img.getChild("MediaInformation", mpeg7).addContent(thumbNails);
        }

        // CreationInformatio
        if (creationInformation != null) {
            img.addContent(creationInformation);
        }

        // TextAnnotation
        if (textAnnotation != null) {
            img.addContent(textAnnotation);
        }

        // Semantic
        if (semantics != null) {
            img.addContent(semantics);
        }

        // Visuals
        if (visualDescriptor != null) {
            if (visualDescriptor.getName().equals("VisualDescriptor"))
                img.addContent(visualDescriptor);
            else {
                java.util.List _list = visualDescriptor.getChildren();
                for (Iterator i = _list.iterator(); i.hasNext();) {
                    Element _tmpElem = (Element) i.next();
                    img.addContent((Element) _tmpElem.clone());
                }
            }
        }
    }

    /**
     * returns the full MPEG-7 Description
     *
     * @return MPEG-7 Description
     */
    public Document createDocument() {
        init();
        return doc;
    }
}
