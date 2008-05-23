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
import at.wklieber.tools.Mpeg7MediaDuration;
import at.wklieber.tools.XmlTools;
import org.jdom.Document;
import org.jdom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class CameraMotionDs extends Mpeg7Template
        implements DsInterface {
    private static Console console = Console.getReference();

    private static String XSI_MOTION_TYPE = "MixtureCameraMotionSegmentType";
    private static String PATH_TIME_POINT = "MediaTime/MediaTimePoint";
    private static String PATH_DURATION = "MediaTime/MediaDuration";
    private static String PATH_FRACTIONAL = "FractionalPresence";
    private static String PATH_MOTION = "AmountOfMotion";

    public CameraMotionDs() {
        super();

        // offset in a complete mpeg7 document where this descriptor occusrs
        // is used to extract data
        // format no starting and leading separator, e.g. "Mpeg7/content"
        // note: the offset strings are used also somewhere else
        OFFSET_PATH = ROOT_TAG + "/Description/MultimediaContent/Video/VisualDescriptor";

        // the name of the Desctipor
        // this is the root tag name of this dom document
        // format: no starting and leading separator, e.g. "DescriptionMetadata"
        DESCRIPTOR_TAG = "Segment";
    }

    /**
     * return the path where the description data begins
     */
    public Element getOffsetForRoot() {
        return simpleXpath(ROOT_TAG + "/Description", mpeg7Namespace, new Element("undefined"), false);
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

        setDomAttribute(OFFSET_PATH, mpeg7Namespace, "type", "CameraMotionType", xsiNamespace);
        setDomAttribute(OFFSET_PATH + "/" + DESCRIPTOR_TAG, mpeg7Namespace, "type", XSI_MOTION_TYPE, xsiNamespace);


    } // end method

    public void setData(String timePoint, String duration, String motionType,
                        String motionAmount) {


        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";
        setDomValue(offset + PATH_TIME_POINT, mpeg7Namespace, timePoint, "");

        setDomValue(offset + PATH_DURATION, mpeg7Namespace, Mpeg7MediaDuration.dateToMediaDuration(duration), "");
        setDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_FRACTIONAL, mpeg7Namespace, "", "");
        setDomValue(offset + PATH_MOTION + "/" + motionType, mpeg7Namespace, motionAmount, "");
    }

    public String getTimePoint() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_TIME_POINT, "");
    }

    public String getDuration() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_DURATION, "");
    }

    public String getMotionName() {
        String returnValue = "";

        java.util.List list = simpleXpath(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_MOTION, mpeg7Namespace, (Element) null, false).getChildren();
        if (list.size() > 0) {
            Element result = (Element) list.get(0);

            if (result != null) {
                returnValue = result.getName();
            }
        }

        /*    Element result = (Element) simpleXpath(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_MOTION, mpeg7Namespace, (Element) null, false).getChildren().get(0);
            if (result != null) {
               returnValue = result.getName();
            }*/
        return returnValue;
    }

    public String getMotionAmount() {
        String returnValue = "";

        java.util.List list = simpleXpath(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_MOTION, mpeg7Namespace, (Element) null, false).getChildren();
        if (list.size() > 0) {
            Element result = (Element) list.get(0);

            if (result != null) {
                returnValue = result.getText();
            }
        }

/*
      Element result = (Element) simpleXpath(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_MOTION, mpeg7Namespace, (Element) null, false).getChildren().get(0);
      if (result != null) {
         returnValue = result.getText();
      }
*/
        return returnValue;
    }


    /**
     * extract the Metadata description from a complete mpeg 7 document
     */
    public List extractFromMpeg7(Document mpeg7Document) {
        List returnValue = new Vector();
        //Element rootElement = mpeg7Document.getRootElement();
        this.document = mpeg7Document;

        //String path = "/" + OFFSET_PATH + "/" + DESCRIPTOR_TAG;
        String xPath = "//" + DESCRIPTOR_TAG + "[@xsi:type=\"" + XSI_MOTION_TYPE + "\"]";
        logger.fine("DS read from: \"" + xPath + "\"");
        List elementList = XmlTools.xpathQuery(mpeg7Document, xPath, xsiNamespace);

        //logger.fine("e: " + elementList.size());

        String offset = DESCRIPTOR_TAG + "/";

        for (Iterator it = elementList.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();

            // extract all needed data for setData
            String startTimePoint = XmlTools.simpleXpath(elem, offset + PATH_TIME_POINT, mpeg7Namespace, new Element("dummy"), false).getText();
            String duration = XmlTools.simpleXpath(elem, offset + PATH_DURATION, mpeg7Namespace, new Element("dummy"), false).getText();

            String motionName = "";
            String amountOfMotion = "";
            java.util.List list = XmlTools.simpleXpath(elem, offset + PATH_MOTION, mpeg7Namespace, new Element("dummy"), false).getChildren();
            if (list.size() > 0) {
                Element motionElement = (Element) list.get(0);

                if (motionElement != null) {
                    motionName = motionElement.getName();
                    amountOfMotion = motionElement.getText();
                }
            }

            // generate a new Desciptor
            CameraMotionDs descr = new CameraMotionDs();
            descr.createTemplateDocument();

            descr.setData(startTimePoint, duration, motionName, amountOfMotion);
//       Mpeg7DateFormat.format(creationTime, null), creationPlace, toolName);

            returnValue.add(descr);
            //logger.fine("DS read: " + descr.toString());
        }

        return returnValue;
    } // end method


} // end class