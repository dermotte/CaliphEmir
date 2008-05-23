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

import at.wklieber.tools.FileTools;
import at.wklieber.tools.XmlTools;
import org.jdom.*;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Werner Klieber
 * @version 0.9
 *          <p/>
 *          description:
 *          This class contains common used things for using Jdom-Documents.
 *          <p/>
 *          Developer-Use
 *          derive child class
 *          add neccessary setXX methods
 *          implement the createTemplateDocument method (to create an empty JDOM-tree)
 *          implement the setDefaultValues method (to initialize all values to a default)
 *          <p/>
 *          User -Usage:
 *          instantiate a derived class.
 *          call setDocument(..) or createTemplateDocument() to get a working DOM-Model
 *          get/set all desired values with setXX methods
 *          call getDocument() or toString to retrieve the contents of the document in a certain format
 */

public class XmlTemplate {

    static Logger logger = Logger.getLogger(XmlTemplate.class.getName());

    // define allowed delimiters to separate Element-Names

    private DocType docType; // a Doctype for setting a Schema-file

    protected Document document;
    protected boolean doValidate_ = false;
    protected String schemaFile_ = "";

    public XmlTemplate() {
        initialize();
    }

    // set all member variables to "null"
    protected void initialize() {
        docType = null;
        document = null;
        doValidate_ = false;     // override this in child-class when validate is needed
        schemaFile_ = "";        // override this in child-class when validate is needed
    }

    protected void validateXml() {
        if (doValidate_) {

            if (!FileTools.existsFile(schemaFile_)) {
                return;
            }

            //logger.fine("Valitate with: <" + schemaFile_ + ">");

            if (!XmlTools.isDocumentValid(XmlTools.documentToString(document), schemaFile_)) {
                logger.severe("XmlQuery is not valid!!");
            }
        }
    } // end method

    // create a new JDOM-Document with a root-Element and optional with a schema definition
    protected void createNewDocument(String rootName1, String dtdUrl1) {
        initialize();
        Element rootElement = new Element(rootName1);
        document = new Document(rootElement);

        setDocType(dtdUrl1);
    }


    // add a "DOCTYPE" definition to the document
    public void setDocType(String dtdUrl1) {
        if (document == null) {
            return;
        }

        Element rootElement = document.getRootElement();
        String rootName = rootElement.getName();

        if ((dtdUrl1 == null) || (dtdUrl1 == "")) {
            // clear the doctype tag
            rootElement.removeChild("DOCTYPE");

        } else {
            docType = new DocType(rootName, dtdUrl1);
            document.setDocType(docType);
        }
    }


    public DocType getDocType() {
        return docType;
    }

    public void writeToFile(String filename) {
        XmlTools.saveToFile(filename, document);
    }

    public void readFromFile(String filename) {
        document = XmlTools.readFromFile(filename, null);
    }

    // ============= various (de)serializers ============

    // set the JDOM Document
    public void setDocument(Document doc1) {
        initialize();
        document = doc1;
    }


    // get the JDOM-Document
    public Document getDocument(Document doc1) {
        validateXml();
        Document ret = doc1;

        if (document != null) {
            ret = document;
        }

        return ret;
    }

    /**
     * Returns the document containing the shape descriptors or the
     * input document if current document is null.
     *
     * @param defaultElement
     * @return the current document or defaultElement if document is null.
     */
    public Element getDocument(Element defaultElement) {
        validateXml();
        Element ret = defaultElement;

        if (document != null) {
            ret = document.getRootElement();
        }

        return ret;
    }

    // set the Document from a w3c-Element tree
    public void setDocument(org.w3c.dom.Element w3cElement1) {
        try {
            DOMBuilder builder = new DOMBuilder();

            Element e = builder.build(w3cElement1);
            document = new Document(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end method


    // return a w3c-Element tree
    public org.w3c.dom.Element getDocument(org.w3c.dom.Element element1) {
        validateXml();
        org.w3c.dom.Element ret = element1;

        try {
            DOMOutputter outputter = new DOMOutputter();
            //ret = outputter.output(document.getRootElement());
            ret = (org.w3c.dom.Element) outputter.output(document);
        } catch (Exception e) {
            logger.severe(e.toString());
        }

        return ret;
    }

    // build the DOM-Document from a java-String
    public void setDocument(String documentStr1) {
        document = null;

        if (documentStr1 == null) {
            return;
        }

        try {
            SAXBuilder builder = new SAXBuilder();
            StringReader strIn = new StringReader(documentStr1);

            builder.setValidation(false);
            document = builder.build(strIn);
        } catch (JDOMException je) {
            //je.printStackTrace();
            logger.severe(je.toString());
        } catch (Exception e) {
            logger.severe(e.toString());
        }
    } // end method

    // convert the DOM-Document to a java-stream
    public StringWriter getDocument(StringWriter stream1) {
        validateXml();
        StringWriter ret = stream1;

        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

            ret = new StringWriter();
            outputter.output(document, ret);
        } catch (Exception e) {
            logger.fine("Error while makeing the outputString. Exception: " + e.getMessage());
            ret = stream1;
        }

        return ret;
    }

    // convert the DOM-Document to a String
    public String getDocument(String default1) {
        String ret = default1;
        try {
            String result = getDocument((StringWriter) null).toString();
            if (result != null) {
                ret = result;
            }
        } catch (Exception e) {
            logger.fine("Error while makeing the outputString. Exception: " + e.getMessage());
            ret = default1;
        }

        return ret;
    }



    // ============ abstract methods that child classes have to implement ==========

    // this methode creats a new documment tree - tructure
    public void createTemplateDocument() {
        throw new java.lang.UnsupportedOperationException("method is not implemented");
    }

    // set all Elements/Attributes to their default values
    public void setDefaultValues() {
        throw new java.lang.UnsupportedOperationException("method is not implemented");
    }


    // ============ some tool-methods ===========================


    /**
     * start with the root-Tag
     *
     * @param location1: the xpath to the desired tag, e.g. "mpeg7/DecriptionMetata"
     * @param default1:  This element is returned if an error occurs
     * @param create1:   if true, any non existing during the location1 path tags are created
     * @return the Element whre loaction1 points through of default on errors
     *         Limitations: multiple occuences of the same tagname are ignored. just one (the first?) is used
     */
    public Element simpleXpath(String location1, Namespace namespace1, Element default1, boolean create1) {
        if (document == null) {
            logger.severe("this.document is NULL. Unable to retrieve an Element");
            return default1;
        }

        return XmlTools.simpleXpath(document.getRootElement(), location1, namespace1, default1, create1);
    }

    public Element simpleXpath(String location1, Element default1, boolean create1) {
        return simpleXpath(location1, null, default1, create1);
    }

    /**
     * walks though the child specified in location1 and returns the text
     * from that Element or the content of "default1" if the child doesn't exists
     */
    protected String getDomValue(String location1, String default1) {
        String ret = default1;

        Element result = simpleXpath(location1, null, false);
        if (result != null) {
            ret = result.getText();
        }

        return ret;
    }

    /**
     * walks though the child specified in loacation1 and returns the text
     * from that Element or the content of "default1" if the child doesn't exists
     */
    protected int getDomValue(String location1, int default1) {
        int returnValue = default1;

        try {
            String result = getDomValue(location1, null);
            if (result != null) {
                returnValue = Integer.parseInt(result);
            }
        } catch (Exception e) {
            logger.severe(e.toString());
        }

        return returnValue;
    }


    protected boolean getDomValue(String location1, boolean default1) {
        boolean returnValue = default1;

        try {
            String result = getDomValue(location1, null);
            if (result != null) {
                returnValue = Boolean.getBoolean(result);
            }
        } catch (Exception e) {
            logger.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * walks though the child specified in loacation1 and returns the text
     * from that Element or the content of "default1" if the child doesn't exists
     */
    public void setDomValue(String location1, String value1, String comment) {
        setDomValue(location1, null, value1, comment);
    }

    public void setDomValue(String location1, Namespace namespace1, String value1, String comment) {
        if (value1 == null) {
            value1 = "";
        }


        Element targetElement = simpleXpath(location1, namespace1, null, true);
        if (targetElement != null) {
            targetElement.setText(value1);
            if ((comment != null) && (comment.length() > 0)) {
                Comment commentElement = new Comment(comment);
                Element parent = (Element) targetElement.getParent();
                parent.addContent(commentElement);
            }
        }
    }


    /**
     * same as apove, just without comments
     */
    protected void setDomValue(String location1, String value1) {
        setDomValue(location1, value1, null);
    }


    /**
     * locate element with use of namespace. and add a attribute to this element without an namespace-attribute
     */
    protected void setDomAttribute(String location1, Namespace namespace1, String attrName1, String attrValue) {
        if ((attrName1 == null) || (attrValue == null)) {
            return;
        }

        Element targetElement = simpleXpath(location1, namespace1, null, true);
        if (targetElement != null) {
            //if (namespace1 != null) {
            //   targetElement.setAttribute(attrName1, attrValue, namespace1);
            //} else {
            targetElement.setAttribute(attrName1, attrValue);
            //}
        }
    }

    /**
     * simple attribute setting without all the namespace stuff
     */
    protected void setDomAttribute(String location1, String attrName1, String attrValue) {
        setDomAttribute(location1, null, attrName1, attrValue);
    }


    /**
     * set a DomAttribute with a processing instruction
     * something like xsi:type="Description"
     * the first namespace is to locate the element, that will be "attributed"
     * the second is the attributenamespace name
     */
    protected void setDomAttribute(String location1, Namespace elementSpace1, String attrName1, String attrValue, Namespace namespace) {
        if ((attrName1 == null) || (attrValue == null)) {
            return;
        }

        Element targetElement = simpleXpath(location1, elementSpace1, null, true);
        if (targetElement != null) {
            targetElement.setAttribute(attrName1, attrValue, namespace);
        }
    }

    // use this if more than one matching elements are possible
    protected List getDomElements(String xpathQuery) {
        return XmlTools.xpathQuery(document, xpathQuery);
    }

    public String toString() {
        return getDocument("no Document available");
    }

    public void destroy() {
        initialize();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        initialize();
    }

} // end class
