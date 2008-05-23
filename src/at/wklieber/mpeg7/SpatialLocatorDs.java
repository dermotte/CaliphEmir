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
import org.jdom.Element;
import org.jdom.Namespace;

import java.awt.*;
import java.util.List;
import java.util.Vector;

public class SpatialLocatorDs extends Mpeg7Template
        implements DsInterface {
    private static Console console = Console.getReference();

    // offset in a complete mpeg7 document where this descriptor occusrs
    // is used to extract data
    // format no starting and leading separator, e.g. "Mpeg7/content"
    // note: the offset strings are used also somewhere else
    // offest is used to write to the mpeg when creating
    public static String OFFSET_PATH = ROOT_TAG + "/Description/MultimediaContent/Image/SpatialDecomposition/StillRegion";

    // two modes: describe a box or a shape
    public static final int TYPE_BOX = 0;
    public static final int TYPE_SHAPE = 1;

    // the name of the Desctipor
    // this is the root tag name of this dom document
    // format: no starting and leading separator, e.g. "DescriptionMetadata"
    // use init to set the types
    private String REGION_TYPE = null; // "
    private String SPATIALLOCATOR_TAG = null;
    private String DESCRIPTOR_TAG = null;

    // set a extra namespace, because "dim" is defined multiple times
    private static final Namespace xmlnsNamespace = Namespace.getNamespace("mpeg7", mpeg7Urn);
    private static String XSI_TYPE = "mpeg7:dim";

    private int type = TYPE_BOX;

    public SpatialLocatorDs(int type1) {
        type = type1;
        initDescriptors();
    }

    private void initDescriptors() {
        switch (type) {
            case TYPE_BOX:
                REGION_TYPE = "Box";
                break;
            case TYPE_SHAPE:
                REGION_TYPE = "Polygon/Coords";
                break;
        }

        SPATIALLOCATOR_TAG = "SpatialLocator";
        DESCRIPTOR_TAG = SPATIALLOCATOR_TAG + "/" + REGION_TYPE;

    }


    /**
     * return the path where the description data begins
     */
    public Element getOffset() {
        //String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;
        String offset = OFFSET_PATH + "/" + SPATIALLOCATOR_TAG;

        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
    }

    /* public Element getOffsetForRoot() {
        //String offset = ROOT_TAG + DESCRIPTOR_TAG;
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;

        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
     }*/

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

        // set a extra namespace, because "dim" is defined multiple times
        Namespace xmlnsNamespace = Namespace.getNamespace("mpeg7", mpeg7Urn);
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;
        Element box = simpleXpath(offset, mpeg7Namespace, null, true);
        box.addNamespaceDeclaration(xmlnsNamespace);

        switch (type) {
            case TYPE_BOX:
                {
                    setDomAttribute(offset, mpeg7Namespace, "dim", "4", xmlnsNamespace);
                    break;
                }
            case TYPE_SHAPE:
                {
                    setDomAttribute(offset, mpeg7Namespace, "dim", "0", xmlnsNamespace); // set manually in setData
                    break;
                }
        }

    } // end method


    /**
     * use this for setBox
     */
    public void setData(Rectangle rect) {
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";
        String coords = mpeg7Convert.box2String(rect);
        setDomValue(offset, mpeg7Namespace, coords, "");
    }

    /**
     * use this for set shape
     */
    public void setData(List pointList) {
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";

        String coords = mpeg7Convert.pointList2String(pointList);
        String dim = "" + pointList.size() * 2;
        setDomAttribute(offset, mpeg7Namespace, "dim", dim, xmlnsNamespace);
        setDomValue(offset, mpeg7Namespace, coords, "");
    }

    public Rectangle getBox(Rectangle defaultValue1) {
        Rectangle returnValue = defaultValue1;

        try {
            String box = getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG, "");
            returnValue = mpeg7Convert.string2Rectangle(box, returnValue);

        } catch (Exception e) {
            logger.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * return a list of Point
     */
    public List getShape() {
        List returnValue = new Vector();

        try {
            String shapeList = getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG, "");
            returnValue = mpeg7Convert.string2Pointlist(shapeList);

        } catch (Exception e) {
            logger.severe(e.toString());
        }

        return returnValue;
    }


    /**
     * //extract the Metadata description from a complete mpeg 7 document
     * //the mpeg7Element should be a "Stillregion"
     */
    public SpatialLocatorDs extractFromMpeg7(Element mpeg7Element) {
        //is implemented in StillRegionDs;

        return null;
/*List returnValue = new Vector();

      //this.document = mpeg7Document;

      //String xPath = "/" + OFFSET_PATH + "/" + DESCRIPTOR_TAG;
      String xPath = "//" + DESCRIPTOR_TAG;
      logger.fine("DS read from: \"" + xPath + "\"");
      List elementList = XmlTools.xpathQuery(mpeg7Element, xPath, xsiNamespace);
      XmlTools.simpleXpath(mpeg7Element.getDocument(),"", null, false);

      logger.fine("e: " + elementList.size());

      String offset = DESCRIPTOR_TAG + "/";

      for (Iterator it = elementList.iterator(); it.hasNext();) {
         Element elem = (Element) it.next();

         // extract all needed data for setData
         String box = XmlTools.simpleXpath(elem, offset, mpeg7Namespace, new Element("dummy"), false).getText();

         // generate a new Desciptor
         SpatialLocatorDs descr = new SpatialLocatorDs();
         descr.createTemplateDocument();

         descr.setData(mpeg7Convert.string2Rectangle(box, new Rectangle(0,0,0,0)));
//       Mpeg7DateFormat.format(creationTime, null), creationPlace, toolName);

         returnValue.add(descr);
         logger.fine("DS read: " + descr.toString());
      }

      return returnValue;*/
    } // end method


} // end class