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

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DominantColorDs extends Mpeg7Template
        implements DsInterface {
    private static Console console = Console.getReference();

    // offset in a complete mpeg7 document where this descriptor occusrs
    // is used to extract data
    // format no starting and leading separator, e.g. "Mpeg7/content"
    // note: the offset strings are used also somewhere else
    // offest is used to write to the mpeg when creating
    public static String OFFSET_PATH = ROOT_TAG + "/Description/MultimediaContent/Image/SpatialDecomposition/StillRegion/VisualDescriptor";

    // the name of the Desctipor
    // this is the root tag name of this dom document
    // format: no starting and leading separator, e.g. "DescriptionMetadata"
    public static String DESCRIPTOR_TAG = "VisualDescriptor";

    private static String XSI_TYPE = "DominantColorType";
    private static String PATH_SPATIAL_COHERENCY = "SpatialCoherency";
    private static String PATH_PERCENTAGE = "Value/Percentage";  // 5 bit value. 1 = 0%, 31 = 100% of the color occurence
    private static String PATH_COLOR_INDEX = "Value/Index";     // color values. The values depend on the color model (RGB, ..).
    private static String PATH_VARIANCE = "Value/ColorVariance";  // three numbers. Indicates the variance of the main color to all in the region (Standardabweichung)

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
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;
        setDomAttribute(offset, mpeg7Namespace, "type", XSI_TYPE, xsiNamespace);

        // set the range of the color to 8 bit [0..255]
        Element offsetElement = simpleXpath(offset + "/ColorQuantization", mpeg7Namespace, null, true);
        Element colorElement;
        colorElement = new Element("Component", mpeg7Namespace);
        colorElement.setText("R");
        offsetElement.addContent(colorElement);
        colorElement = new Element("NumOfBins", mpeg7Namespace);
        colorElement.setText("8");
        offsetElement.addContent(colorElement);

        colorElement = new Element("Component", mpeg7Namespace);
        colorElement.setText("G");
        offsetElement.addContent(colorElement);
        colorElement = new Element("NumOfBins", mpeg7Namespace);
        colorElement.setText("8");
        offsetElement.addContent(colorElement);

        colorElement = new Element("Component", mpeg7Namespace);
        colorElement.setText("B");
        offsetElement.addContent(colorElement);
        colorElement = new Element("NumOfBins", mpeg7Namespace);
        colorElement.setText("8");
        offsetElement.addContent(colorElement);


    } // end method

    public void setData(Color color) {
        setData(color, "31", "31", "0 0 0");
    }

    /**
     * percentage, spatialCoherency and colorVariance are ignored
     */
    public void setData(Color color, String percentage, String spatialCoherency,
                        String colorVariance) {

        percentage = "31";
        spatialCoherency = "31";
        colorVariance = "0 0 0";

        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";
        setDomValue(offset + PATH_SPATIAL_COHERENCY, mpeg7Namespace, spatialCoherency, "");

        setDomValue(offset + PATH_PERCENTAGE, mpeg7Namespace, percentage, "");
        setDomValue(offset + "/" + PATH_COLOR_INDEX, mpeg7Namespace, mpeg7Convert.color2String(color, "0 0 0"), "");
        setDomValue(offset + PATH_VARIANCE, mpeg7Namespace, colorVariance, "");
    }

    public Color getColor() {
        /*System.out.println("----------------XX-------------------------------");
        System.out.println(toString());
        System.out.println("----------------XX-------------------------------");*/
        String colorRgb = getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_COLOR_INDEX, "");
        return mpeg7Convert.string2Color(colorRgb, new Color(0, 0, 0));
    }


    /**
     * extract the Metadata description from a complete mpeg 7 document
     */
    public List extractFromMpeg7(Document mpeg7Document) {
        List returnValue = new Vector();
        //Element rootElement = mpeg7Document.getRootElement();
        this.document = mpeg7Document;

        //String path = "/" + OFFSET_PATH + "/" + DESCRIPTOR_TAG;
        String xPath = "//" + DESCRIPTOR_TAG + "[@xsi:type=\"" + XSI_TYPE + "\"]";


        logger.fine("DS read from: \"" + xPath + "\"");
        List elementList = XmlTools.xpathQuery(mpeg7Document, xPath, xsiNamespace);

        logger.fine("e: " + elementList.size());

        String offset = DESCRIPTOR_TAG + "/";

        for (Iterator it = elementList.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();

            // extract all needed data for setData
            //System.out.println(XmlTools.documentToString(elem));
            Element colorElement = XmlTools.simpleXpath(elem, offset + PATH_COLOR_INDEX, mpeg7Namespace,
                    null, false);
            if (colorElement == null) {
                colorElement = XmlTools.simpleXpath(elem, offset + PATH_COLOR_INDEX,
                    null, false);
            }

            String color ="";
            if (colorElement != null) {
                color = colorElement.getText();
            } else {
               logger.severe("unable to extract color info from \"" + XmlTools.documentToString(elem) + "\"");
            }

            /*List list = XmlTools.simpleXpath(elem, offset + PATH_COLOR_INDEX, mpeg7Namespace, new Element("dummy"), false).getChildren();
            if (list.size() > 0) {
               Element colorElement = (Element) list.get(0);

               if (colorElement != null) {
                  color = colorElement.getName();
               }
            }*/

            // generate a new Desciptor
            DominantColorDs descr = new DominantColorDs();
            descr.createTemplateDocument();

            descr.setData(mpeg7Convert.string2Color(color, new Color(0, 0, 0)));
//       Mpeg7DateFormat.format(creationTime, null), creationPlace, toolName);

            returnValue.add(descr);
            logger.fine("DS read: " + descr.toString());
        }

        return returnValue;
    } // end method


} // end class