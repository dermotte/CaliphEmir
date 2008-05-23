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


import at.wklieber.gui.data.IComponentData;
import at.wklieber.tools.Console;
import at.wklieber.tools.Mpeg7DateFormat;
import at.wklieber.tools.XmlTools;


import org.jdom.Document;
import org.jdom.Element;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DescriptionMetadataDs extends Mpeg7Template
        implements DsInterface {
    private static Console console = Console.getReference();
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(DescriptionMetadataDs.class.getName());


    private static String PATH_DESCRIPTION = "Comment/FreeTextAnnotation";
    private static String PATH_CREATOR_GIVEN_NAME = "Creator/Agent/Name/GivenName";
    private static String PATH_CREATOR_FAMILY_NAME = "Creator/Agent/Name/FamilyName";
    private static String PATH_CREATION_TIME = "CreationTime";
    private static String PATH_CREATION_PLACE = "CreationLocation";
    private static String PATH_CREATION_TOOL = "Instrument/Tool/Name";

    public DescriptionMetadataDs() {
        super();

        // offset in a complete mpeg7 document where this descriptor occusrs
        // is used to extract data and write a valid mpeg7 file
        // format no starting and leading separator, e.g. "Mpeg7/content"
        OFFSET_PATH = ROOT_TAG;

        // the name of the Desctipor
        // this is the root tag name of this dom document
        // format no starting and leading separator, e.g. "DescriptionMetadata"
        DESCRIPTOR_TAG = "DescriptionMetadata";
    }


    /**
     * return the path where the description data begins
     */
    /*  public Element getOffset() {
         return simpleXpath(OFFSET_PATH + "/" + DESCRIPTOR_TAG, mpeg7Namespace, new Element("undefined"), false);
      }*/

    /**
     * return the element that can be added to the root tag (Mpeg7)
     */
    public Element getOffsetForRoot() {
        String offset = ROOT_TAG + "/" + DESCRIPTOR_TAG;
        return simpleXpath(offset, mpeg7Namespace, new Element("Undefined"), false);
    }


    public void setDefaultValues() {
        //super.setDefaultValues();
    }


    // this methode creats a new document tree - tructure
    public void createTemplateDocument() {
        super.createTemplateDocument();
        //console.line();


        Date currentDate = new Date(System.currentTimeMillis());

        Element defaultValue = null;

        //console.line();
        //logger.fine("BEFORE: " + this.toString());
        //setDomAttribute(OFFSET_PATH, "type", "CreationDescriptionType", xsiNamespace);
        //logger.fine("AFTER : " + this.toString());
        //console.line();

        //Element data = new Element("DescriptionMetadata", mpeg7Namespace);
        //data.addNamespaceDeclaration(mpeg7Namespace);
        //document.getRootElement().addContent(data);

        //simpleXpath(DESCRIPTOR_TAG, null, true);

        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";
        setDomValue(offset + "Version", mpeg7Namespace, "1.0", "Version of the mpeg 7 schema this file applies to");
        setDomValue(offset + "LastUpdate", mpeg7Namespace, Mpeg7DateFormat.date2Timepoint(currentDate), "Date of the last change of this document");
        //logger.fine("BEFORE2: " + this.toString());

    } // end method

    public void setData(String description, String creatorGivenName, String creatorFamilyName,
                        Date creationTime, String creationPlace, String toolName) {

        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/";
        setDomValue(offset + PATH_DESCRIPTION, mpeg7Namespace, description, "");
        setDomAttribute(offset + "Creator/Role", mpeg7Namespace, "href", "creatorCS");
        setDomValue(offset + "Creator/Role/Name", mpeg7Namespace, "Creator", "");
        setDomAttribute(offset + "Creator/Agent", mpeg7Namespace, "type", "PersonType", xsiNamespace);
        setDomValue(offset + PATH_CREATOR_GIVEN_NAME, mpeg7Namespace, creatorGivenName, "");
        setDomValue(offset + PATH_CREATOR_FAMILY_NAME, mpeg7Namespace, creatorFamilyName, "");
        setDomValue(offset + PATH_CREATION_PLACE, mpeg7Namespace, creationPlace, "");
        setDomValue(offset + PATH_CREATION_TIME, mpeg7Namespace, Mpeg7DateFormat.date2Timepoint(creationTime), "");
        setDomValue(offset + PATH_CREATION_TOOL, mpeg7Namespace, toolName, "");
        //logger.fine("BEFORE3: " + this.toString());
    }

    public String getCreationDescription() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_DESCRIPTION, "");
    }

    public String getCreatorGivenName() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_CREATOR_GIVEN_NAME, "");
    }

    public String getCreatorFamilyName() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_CREATOR_FAMILY_NAME, "");
    }

    public String getCreationTime() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_CREATION_TIME, "");
    }


    public String getCreationPlace() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_CREATION_PLACE, "");
    }

    public String getCreationTool() {
        return getDomValue(OFFSET_PATH + "/" + DESCRIPTOR_TAG + "/" + PATH_CREATION_TOOL, "");
    }


    /**
     * extract the Metadata description from a complete mpeg 7 document
     */
    public List<IComponentData> extractFromMpeg7(Document mpeg7Document) {
        List returnValue = new Vector();
        this.document = mpeg7Document;

        Element rootElement = document.getRootElement();

        String path = "//" + DESCRIPTOR_TAG;
        List elementList = XmlTools.xpathQuery(document, path);

        String offset = DESCRIPTOR_TAG + "/";
        for (Iterator it = elementList.iterator(); it.hasNext();) {

            Element elem = (Element) it.next();

            // extract all needed data for setData
            String description = XmlTools.simpleXpath(elem, offset + PATH_DESCRIPTION, mpeg7Namespace, new Element("dummy"), false).getText();
            String creatorGivenName = XmlTools.simpleXpath(elem, offset + PATH_CREATOR_GIVEN_NAME, mpeg7Namespace, new Element("dummy"), false).getText();

            String creatorFamilyName = XmlTools.simpleXpath(elem, offset + PATH_CREATOR_FAMILY_NAME, mpeg7Namespace, new Element("dummy"), false).getText();

            String creationTime = XmlTools.simpleXpath(elem, offset + PATH_CREATION_TIME, mpeg7Namespace, new Element("dummy"), false).getText();

            String creationPlace = XmlTools.simpleXpath(elem, offset + PATH_CREATION_PLACE, mpeg7Namespace, new Element("dummy"), false).getText();

            String toolName = XmlTools.simpleXpath(elem, offset + PATH_CREATION_TOOL, mpeg7Namespace, new Element("dummy"), false).getText();


            // generate a new Desciptor
            DescriptionMetadataDs descr = new DescriptionMetadataDs();
            descr.createTemplateDocument();

            descr.setData(description, creatorGivenName, creatorFamilyName,
                    Mpeg7DateFormat.format(creationTime, null), creationPlace, toolName);

            returnValue.add(descr);
            //logger.fine("DS read: " + descr.toString() + ", " + descr.getCreationDescription() + ", e: " + description);
        }

        return returnValue;
    } // end method


} // end class