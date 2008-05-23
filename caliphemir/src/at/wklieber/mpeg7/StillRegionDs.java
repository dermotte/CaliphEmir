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
import org.jdom.Namespace;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class StillRegionDs extends Mpeg7Template
        implements DsInterface {
    private static Console console = Console.getReference();

    // offset in a complete mpeg7 document where this descriptor occusrs
    // is used to extract data
    // format no starting and leading separator, e.g. "Mpeg7/content"
    // note: the offset strings are used also somewhere else
    // offest is used to write to the mpeg when creating
    public static String OFFSET_PATH = ROOT_TAG + "/Description/MultimediaContent/Image/SpatialDecomposition";

    // the name of the Desctipor
    // this is the root tag name of this dom document
    // format: no starting and leading separator, e.g. "DescriptionMetadata"
    public static String DESCRIPTOR_TAG = "StillRegion";

    //private static String XSI_TYPE = "DominantColorType";
    private static String PATH_SPATIAL_LOCATOR = "SpatialLocator";
    private static String PATH_VISUAL_DESCRIPTOR = "VisualDescriptor"; // contains for example dominantColor

    private Rectangle box = null;
    private List shape = null;  // list of Point()

    public List getShape() {
        return shape;
    }

    public void setShape(List shape) {
        this.shape = shape;
    }

    private Color[] colors = null;

    /**
     * return the path where the description data begins
     */
    public Element getOffset() {
        //String offset = ROOT_TAG + DESCRIPTOR_TAG;
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;

        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
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
        setDomAttribute(ROOT_TAG + "/Description/MultimediaContent", mpeg7Namespace, "type", "ImageType", xsiNamespace);

        //setDomAttribute(OFFSET_PATH, mpeg7Namespace, "type", "CameraMotionType", xsiNamespace);
        //setDomAttribute(OFFSET_PATH + "/" + DESCRIPTOR_TAG, mpeg7Namespace, "type", XSI_TYPE, xsiNamespace);


    } // end method

    public void setData(SpatialLocatorDs spatial, DominantColorDs dominantColor) {
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";
        Element offsetElement = simpleXpath(offset, mpeg7Namespace, null, true);
        offsetElement.addContent(spatial.getOffset().detach());

        if (dominantColor != null) {
            offsetElement.addContent(dominantColor.getOffset().detach());
        }
    }

    public List getData() {
        //String Rgb = getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_SPATIAL_LOCATOR, "");
        return null;
    }

    /**
     * return a spatialDecomostion if available
     */
    public Rectangle getBox() {
        logger.fine("getBox: " + box);
        return box;
    }

    public Color[] getColors() {
        return colors;
    }

    public void setBox(Rectangle box) {
        logger.fine("getBox: " + box.toString());
        this.box = box;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    /**
     * extract the Metadata description from a complete mpeg 7 document
     * retrun value is a list of "StillRegionDs"
     */
    public List extractFromMpeg7(Document mpeg7Document) {
        List returnValue = new Vector();

        try {
            //Element rootElement = mpeg7Document.getRootElement();
            Document clone = (Document) mpeg7Document.clone();
            XmlTools.removeAllNamespacesRecursive(clone.getRootElement());
            this.document = clone;
            Namespace mpeg7Namespace =null;
            String xPath = "//" + DESCRIPTOR_TAG;
            logger.fine("DS read from: \"" + xPath + "\"");
            List elementList;
            elementList = XmlTools.xpathQuery(document, xPath, xsiNamespace);
             if (elementList.size() == 0) {
                  elementList = XmlTools.xpathQuery(document, xPath);
             }

            logger.fine("e: " + elementList.size());

            String offset = DESCRIPTOR_TAG + "/";

            for (Iterator it = elementList.iterator(); it.hasNext();) {
                Element elem = (Element) it.next();

                // try to extract extract a Box with colors
                //logger.fine(XmlTools.documentToString(elem));
                String boxStr = XmlTools.simpleXpath(elem, offset + PATH_SPATIAL_LOCATOR + "/Box",
                        mpeg7Namespace, new Element("dummy"), false).getText();
                if (boxStr.length() > 0) {
                    /*System.out.println("----------------- BOXXX ---------------------");
                    System.out.println(XmlTools.documentToString(elem));
                    System.out.println("----------------- BOXXX ---------------------");*/


                    Rectangle localBox = mpeg7Convert.string2Rectangle(boxStr, null);
                    //Document doc = new Document(elem.detach());
                    Document doc = XmlTools.getNewDocumentFromElement(elem);

                    /*System.out.println("----------------- BOXXX ---------------------");
                    System.out.println(XmlTools.documentToString(doc));
                    System.out.println("----------------- BOXXX ---------------------");*/

                    List dominantColors = new DominantColorDs().extractFromMpeg7(doc);
                    Color[] localColors = new Color[dominantColors.size()];
                    for (int i = 0; i < dominantColors.size(); i++) {
                        DominantColorDs colorDs = (DominantColorDs) dominantColors.get(i);
                        if (colorDs == null) {
                            logger.severe("colorDs is null");
                        } else {
                            localColors[i] = colorDs.getColor();
                        }
                    }

                    // generate a new Desciptor
                    StillRegionDs descr = new StillRegionDs();
                    descr.createTemplateDocument();
                    descr.setColors(localColors);
                    descr.setBox(localBox);

                    returnValue.add(descr);
                    logger.fine("DS read: " + descr.toString());
                } else { // try to extract a shape
                    String shapeStr = XmlTools.simpleXpath(elem, offset + PATH_SPATIAL_LOCATOR + "/Polygon/Coords",
                            mpeg7Namespace, new Element("dummy"), false).getText();
                    if (shapeStr.length() > 0) {
                        List localList = mpeg7Convert.string2Pointlist(shapeStr);
                        //Document doc = new Document(elem.detach());
                        Document doc = XmlTools.getNewDocumentFromElement(elem);

                        List dominantColors = new DominantColorDs().extractFromMpeg7(doc);
                        Color[] localColors = new Color[dominantColors.size()];
                        for (int i = 0; i < dominantColors.size(); i++) {
                            DominantColorDs colorDs = (DominantColorDs) dominantColors.get(i);
                            if (colorDs == null) {
                                logger.severe("colorDs is null");
                            } else {
                                localColors[i] = colorDs.getColor();
                            }
                        }


                        // generate a new Desciptor
                        StillRegionDs descr = new StillRegionDs();
                        descr.createTemplateDocument();
                        descr.setShape(localList);
                        descr.setColors(localColors);

                        returnValue.add(descr);
                        logger.fine("DS read: " + descr.toString());

                    }
                } // end if select the type to extract
            }
        } catch (Exception e) {
            logger.severe(e.toString());
        }

        return returnValue;
    } // end method


} // end class