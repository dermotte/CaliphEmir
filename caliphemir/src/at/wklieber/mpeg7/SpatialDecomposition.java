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
import org.jdom.Document;
import org.jdom.Element;

import java.util.List;

/**
 * holds a list of StillRegion - entries
 */
public class SpatialDecomposition extends Mpeg7Template
        implements DsInterface {
    private static Console console = Console.getReference();

    // offset in a complete mpeg7 document where this descriptor occusrs
    // is used to extract data
    // format no starting and leading separator, e.g. "Mpeg7/content"
    // note: the offset strings are used also somewhere else
    // offest is used to write to the mpeg when creating
    public static String OFFSET_PATH = ROOT_TAG + "/Description/MultimediaContent/Image";

    // the name of the Desctipor
    // this is the root tag name of this dom document
    // format: no starting and leading separator, e.g. "DescriptionMetadata"
    public static String DESCRIPTOR_TAG = "SpatialDecomposition";

    private static String XSI_TYPE = "ImageType";
    private static String PATH_STILL_REGION = "StillRegion"; // the name of the children

    /**
     * return the path where the description data begins
     */
    public Element getOffset() {
        //String offset = ROOT_TAG + DESCRIPTOR_TAG;
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;

        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
    }

    /**
     * return the element that can be added to the root tag (Mpeg7)
     */
    /* public Element getOffsetForRoot() {
        String offset = ROOT_TAG + CONENT_DESCRIPTION_TAG;
        //String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;

        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
     }*/


    public void setDefaultValues() {
        //super.setDefaultValues();
    }


    // this methode creats a new document tree - structure
    // with all tags that are unchanged by a "setData"
    public void createTemplateDocument() {
        super.createTemplateDocument();
        //console.line();

        setDomAttribute(ROOT_TAG + "/" + CONTENT_DESCRIPTION_TAG, mpeg7Namespace, "type", "ContentEntityType", xsiNamespace);
        //setDomAttribute(ROOT_TAG + "/Description/MultimediaContent", mpeg7Namespace, "type", "ImageType", xsiNamespace);

        //setDomAttribute(OFFSET_PATH, mpeg7Namespace, "type", "CameraMotionType", xsiNamespace);
        //setDomAttribute(OFFSET_PATH, mpeg7Namespace, "type", XSI_TYPE, xsiNamespace); // set to imageType
        setDomAttribute(ROOT_TAG + "/" + CONTENT_DESCRIPTION_TAG + "/MultimediaContent", mpeg7Namespace, "type", XSI_TYPE, xsiNamespace);

    } // end method

    public void addData(StillRegionDs region) {
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";
        Element offsetElement = simpleXpath(offset, mpeg7Namespace, null, true);
        Element regionElement = region.getOffset();

        //logger.fine("regionElement (StillRegion): " + regionElement.getName());
        //System.exit(1);

        offsetElement.addContent(regionElement.detach());
    }

    public List getStillRegionDsList() {
        //String colorRgb = getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_COLOR_INDEX, "");
        return null;
    }


    /**
     * extract the Metadata description from a complete mpeg 7 document
     */
    public List extractFromMpeg7(Document mpeg7Document) {
        return null;

/*
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
         String color = XmlTools.simpleXpath(elem, offset + PATH_COLOR_INDEX, mpeg7Namespace, new Element("dummy"), false).getText();

         List list = XmlTools.simpleXpath(elem, offset + PATH_COLOR_INDEX, mpeg7Namespace, new Element("dummy"), false).getChildren();
         if (list.size() > 0) {
            Element colorElement = (Element) list.get(0);

            if (colorElement != null) {
               color = colorElement.getName();
            }
         }

         // generate a new Desciptor
         SpatialDecomposition descr = new SpatialDecomposition();
         descr.createTemplateDocument();

         descr.addData(new Color(0, 0, 0));
//       Mpeg7DateFormat.format(creationTime, null), creationPlace, toolName);

         returnValue.add(descr);
         logger.fine("DS read: " + descr.toString());
      }

      return returnValue;
      */
    } // end method


} // end class