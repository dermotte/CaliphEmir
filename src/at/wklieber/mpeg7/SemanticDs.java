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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.mpeg7;


import at.wklieber.tools.Console;
import at.wklieber.tools.XmlTools;
import org.jdom.Document;
import org.jdom.Element;

import java.util.List;
import java.util.Vector;

public class SemanticDs extends Mpeg7Template
        implements DsInterface {
    private static Console console = Console.getReference();

    //private static String XSI_MOTION_TYPE = "MixtureCameraMotionSegmentType";
    private static String SEMANTIC_DS_NAME = "Semantic";

    public SemanticDs() {
        super();

        // offset in a complete mpeg7 document where this descriptor occusrs
        // is used to extract data
        // format no starting and leading separator, e.g. "Mpeg7/content"
        // note: the offset strings are used also somewhere else
        OFFSET_PATH = ROOT_TAG + "/Description/MultimediaContent/Video";

        // the name of the Desctipor
        // this is the root tag name of this dom document
        // format: no starting and leading separator, e.g. "DescriptionMetadata"
        DESCRIPTOR_TAG = SEMANTIC_DS_NAME;
    }

    /**
     * return the path where the description data begins
     */
    public Element getOffsetForRoot() {
        return simpleXpath(OFFSET_PATH, mpeg7Namespace, new Element("undefined"), false);
    }

    public void setDefaultValues() {
        //super.setDefaultValues();
    }


    // this methode creats a new document tree - tructure
    // with all tags that are unchanged by a "setData"
    public void createTemplateDocument() {
        super.createTemplateDocument();
        //console.line();

        setDomAttribute(ROOT_TAG + "/Description", mpeg7Namespace, "type", "ContentEntityType", xsiNamespace);
        setDomAttribute(ROOT_TAG + "/Description/MultimediaContent", mpeg7Namespace, "type", "VideoType", xsiNamespace);

        //setDomAttribute(OFFSET_PATH, mpeg7Namespace, "type", "CameraMotionType", xsiNamespace);
        //setDomAttribute(OFFSET_PATH + "/" + DESCRIPTOR_TAG, mpeg7Namespace, "type", XSI_MOTION_TYPE, xsiNamespace);
        setDomValue(OFFSET_PATH + "/" + SEMANTIC_DS_NAME, mpeg7Namespace, " ", "");

    } // end method

    /**
     * @param semanticElement List of Element. root element name is "Semantic"
     */
    public void setData(List semanticElement) { //

        try {
            // remove existing semantic lists
            Element offsetElement = simpleXpath(OFFSET_PATH, mpeg7Namespace, null, false);
            if (offsetElement != null) {
                offsetElement.removeChildren(SEMANTIC_DS_NAME, mpeg7Namespace);
            }

            if (semanticElement != null) {
                Element startElement = simpleXpath(OFFSET_PATH, mpeg7Namespace, null, true);

                for (int i = 0; i < semanticElement.size(); i++) {
                    Element element = (Element) semanticElement.get(i);
                    element.detach();
                    startElement.addContent(element);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.toString());
        }
    }


    /**
     * extract the Metadata description from a complete mpeg 7 document
     */
    public List extractFromMpeg7(Document mpeg7Document) {
        List returnValue = new Vector();
        //Element rootElement = mpeg7Document.getRootElement();
        this.document = mpeg7Document;

        //String path = "/" + OFFSET_PATH + "/" + DESCRIPTOR_TAG;
        String xPath = "//" + SEMANTIC_DS_NAME;
        logger.fine("DS read from: \"" + xPath + "\"");
        List elementList = XmlTools.xpathQuery(mpeg7Document, xPath, xsiNamespace);

        //logger.fine("e: " + elementList.size());

        String offset = DESCRIPTOR_TAG + "/";

        //for (Iterator it = elementList.iterator(); it.hasNext();) {
        //    Element elem = (Element) it.next();

        // extract all needed data for setData


        // generate a new Desciptor
        SemanticDs descr = new SemanticDs();
        descr.createTemplateDocument();

        descr.setData(elementList);
        returnValue.add(descr);
        //logger.fine("DS read: " + descr.toString());
        //}

        return returnValue;
    } // end method


} // end class