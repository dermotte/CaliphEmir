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
package at.wklieber.tools;

import at.wklieber.Settings;


import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class XmlTools {
    static Logger cat = Logger.getLogger(XmlTools.class.getName());
    private static Console console = Console.getReference();
    private static Settings cfg = Settings.getReference();

    static final String XML_PATH_SEPARATOR = "/\\,. ;";

    public XmlTools() {
    }


    /**
     * This method parses the location-String and walks through the JDOM tree until
     * the right child-Element is reached and returns this Element. If create is
     * false, the default value is returned if a child doesn't exist. If create is
     * true, non existing children are created.
     * Note: if namespace is not null, the namespace is added to each new created element
     * Note: support for attributes is not implemented
     * Note: if there are more than one tag with the same name on a
     * level, the one retrieved from getChild() is used (the first one).
     * Note: high level methods for simpleXpath is in XmlTemplate class
     */
    public static org.jdom.Element simpleXpath(org.jdom.Element rootElement1, String location1,
                                               org.jdom.Element default1, boolean create1) {
        return simpleXpath(rootElement1, location1, null, default1, create1);
    }

    public static org.jdom.Element simpleXpath(org.jdom.Element rootElement1, String location1,
                                               Namespace nameSpace, org.jdom.Element default1, boolean create1) {
        org.jdom.Element returnValue = default1;

        try {
            //System.out.println("go for: " + location1);

            if ((location1 == null) || (location1.length() == 0) || (rootElement1 == null)) {
                return returnValue;
            }

            String location = location1.trim();

            StringTokenizer tokens = new StringTokenizer(location, XML_PATH_SEPARATOR, false);

            // walk down the xml-hierachy
            Element baseElement = rootElement1;
            boolean isRootElement = true;
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                if (baseElement == null) ;
                Element newBase;
                if (isRootElement) {
                    newBase = baseElement;
                    isRootElement = false;
                } else {
                    if (nameSpace == null) {
                        newBase = baseElement.getChild(token);
                    } else {
                        newBase = baseElement.getChild(token, nameSpace);
                    }
                }
                if (newBase == null) { // this child doesn't exist
                    if (create1) {
                        if (nameSpace == null) {
                            newBase = new Element(token);
                        } else {
                            newBase = new Element(token, nameSpace);
                        }
                        baseElement.addContent(newBase);
                        //console.echo("Name: " + newBase.getName() + ", ns pre: <" + newBase.getNamespace().getPrefix() + ">, url:  <" + newBase.getNamespace().getURI() + ">");
                        //console.echo(XmlTools.documentToString(rootElement1));
                    } else {
                        return returnValue;
                    }
                } // end if baseElement
                baseElement = newBase;
            } // end while

            returnValue = baseElement;
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    } // end method

    // same as obove, just with Document instead of Element
    public static org.jdom.Element simpleXpath(org.jdom.Document document1, String location1,
                                               org.jdom.Element default1, boolean create1) {
        org.jdom.Element returnValue = default1;

        if ((location1 == null) || (location1.length() == 0) || (document1 == null)) {
            return returnValue;
        }

        return simpleXpath(document1.getRootElement(), location1, returnValue, create1);
    } // end method


    public static String documentToString(Document doc) {
        String ret = "";

        if (doc != null) {
            try {
                XMLOutputter xml_out = new XMLOutputter(Format.getPrettyFormat());
                StringWriter out = new StringWriter();
                xml_out.output(doc, out);
                ret = out.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public static String documentToString(org.jdom.Element elem) {
        String ret = "";
        if (elem != null) {
            org.jdom.Element elem2 = (org.jdom.Element) elem.clone(); // we need an Element with no parent
            Document doc = new Document(elem2);
            ret = documentToString(doc);
        }

        return ret;
    }

    public static Document stringToDocument(String strDoc1, boolean doValidate1) {
        return stringToDocument(strDoc1, doValidate1, null);
    }

    public static Document stringToDocument(String strDoc1, boolean doValidate1, Document defaultValue1) {
        Document returnValue = defaultValue1;

        if (strDoc1 == null) {
            return returnValue;
        }

        try {
            Document temp = null;
            SAXBuilder builder = new SAXBuilder();
            /*
            File f = File.createTempFile("xml", null);
            FileTools.saveToFile(f.getAbsolutePath(), strDoc1);
            File in = new File(f.getAbsolutePath());
            */

            StringReader strIn = new StringReader(strDoc1);

            builder.setValidation(doValidate1);
            temp = builder.build(strIn);
            if (temp != null) {
                returnValue = temp;
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }


    public static Document fileToDocument(String fileName1, boolean doValidate1) {
        Document ret = null;
        if (fileName1 == null) {
            cat.severe("Filename is NULL");
            return ret;
        }

        String filename = FileTools.resolvePath(fileName1);
        cat.fine("loading file" + filename);
        if (!FileTools.existsFile(filename)) {
            cat.severe("File \"" + filename + "\" doesn't exist");
            return ret;
        }

        Document temp = null;
        try {
            SAXBuilder builder = new SAXBuilder();

            File inFile = new File(filename);

            builder.setValidation(doValidate1);
            if (!doValidate1) {
                builder.setDTDHandler(null);
            }
            temp = builder.build(inFile);
        } catch (JDOMException je) {
            cat.severe(je.toString());
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        ret = temp;
        return ret;
    }

    // this method replaces the content of "target" with the content of "source".
    // this means everything: children, texts, ...
    // not tested with attributes
    public static void replaceElement(org.jdom.Element target, org.jdom.Element source) {
        //target.removeChildren();
        target.getChildren().clear();
        //List new_copy = source.getChildren();

        //Element new_copy = (Element) source.clone(); // without parent
        List children = source.getChildren();
        Iterator it = children.iterator();
        Vector newList = new Vector();
        while (it.hasNext()) {
            // get child without parent
            org.jdom.Element child = (org.jdom.Element) it.next();
            newList.add(child.clone());
        }
        //target.setChildren(newList);
        target.addContent(newList);
    }

    // convert a JDOM - document to a W3C-Element
    public static org.w3c.dom.Document jdom2w3c(Document jdomElement1, org.w3c.dom.Document default1) {
        org.w3c.dom.Document returnValue = default1;

        try {
            DOMOutputter outputter = new DOMOutputter();
            returnValue = outputter.output(jdomElement1);
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    // convert a JDOM - Element to a W3C-Element
    public static org.w3c.dom.Element jdom2w3c(Document jdomElement1, org.w3c.dom.Element default1) {
        org.w3c.dom.Element returnValue = default1;

        try {
            DOMOutputter outputter = new DOMOutputter();
            //returnValue = outputter.output(jdomElement1.getRootElement());
            org.w3c.dom.Document d = outputter.output(jdomElement1);
            returnValue = d.getDocumentElement();

            //org.w3c.dom.Document resultDocument = outputter.output(jdomElement1);
            //returnValue = Document.
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * Reads the file "fileName" and converts it to a JDOM-Document.
     * If anything fails the input-default Document "doc" is returned
     */
    public static Document readFromFile(String fileName, Document doc) {
        Document returnValue = null;
        try {
            SAXBuilder builder = new SAXBuilder();
            //File file = new File(new URI(fileName));
            cat.fine("READ: " + fileName);
            //File file = new File(FileTools.removeFileUrlPrefix(fileName));
            returnValue = builder.build(new URL(FileTools.setUrlPrefix(fileName)));
        } catch (Exception je) {
            console.error("ERROR while reading file \"" + fileName + "\"");
            je.printStackTrace();
            returnValue = doc;
        }
        return returnValue;
    }

    /**
     * Reads the file "fileName" and converts it to a e3c Element.
     * If anything fails the input-default Element "default1" is returned
     */
    public static org.w3c.dom.Element readFromFileW3c(String fileName1, org.w3c.dom.Element default1) {
        org.w3c.dom.Element returnValue = default1;

        Document doc = readFromFile(fileName1, null);
        if (doc != null) {
            returnValue = jdom2w3c(doc, returnValue);
        }

        return returnValue;
    }

    /**
     * Reads the file "fileName" and converts it to a w3c document.
     * If anything fails the input-default Document "default1" is returned
     */
    public static org.w3c.dom.Document readFromFileW3c(String fileName1, org.w3c.dom.Document default1) {
        org.w3c.dom.Document returnValue = default1;

        Document doc = fileToDocument(fileName1, false);
        if (doc != null) {
            returnValue = jdom2w3c(doc, returnValue);
        }

        return returnValue;
    }


    // writes a JDOM document to a file in UFT
    public static void saveToFile(String fname, Document doc) {
        try {
            XMLOutputter xml_out = new XMLOutputter(Format.getPrettyFormat());

            String data = null;

            if (doc != null) {
                StringWriter out = new StringWriter();
                xml_out.output(doc, out);
                data = out.toString();
            }

            if (data == null)
                data = "";

            cat.fine("write string in utf8 to: " + fname);
            FileTools.saveToFileUtf8(fname, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  // end method

    // validate an xml-String against an given dtd
    public static boolean isDocumentValid(String strDoc1, String dtdFile) {
        boolean ret = false;

        cat.fine("Input-XML-String: " + strDoc1);
        if (!FileTools.existsFile(dtdFile)) {
            cat.severe("File \"" + dtdFile + "\" not found. Unable to validate");
            return ret;
        }

        Document docOrginal = stringToDocument(strDoc1, false);
        if (docOrginal == null) {
            cat.severe("XML-String is not a valid XML-syntax");
            return ret;
        }

        try {
            //org.jdom.Element getRootElement;
            //getRootElement = docOrginal.getRootElement();
            String rootElement = docOrginal.getRootElement().getName();
            rootElement = rootElement.trim();
            cat.fine("Root-Element: <" + rootElement + ">");

            String fileName = FileTools.resolvePath(dtdFile);
            URL url = FileTools.getFileURL(fileName, null);
            if (url == null) {
                fileName = "";
            } else {
                fileName = url.toExternalForm();
            }

            DocType docType = new DocType(rootElement, fileName);

            docOrginal.setDocType(docType);
            String strDocDtd = documentToString(docOrginal);
            cat.fine("strdocdtd: " + strDocDtd);

            Document docDtd = stringToDocument(strDocDtd, true);
            if (docDtd == null)
                return ret;
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }


    // make a String, that represents the jaxb-tree
    /*public static String jaxbSerialize(MarshallableRootElement element1) {

        String ret = ConvertValues.buildErrorResponse("unable to generate xml-stream");
        try {
            OutputStream os = new ByteArrayOutputStream();
            element1.validate();
            element1.marshal(os);
            ret = os.toString();
            os.close();

        } catch (Exception e) {
            console.exitOnException(e);
        }
        return ret;
    }
*/
    /*   // sava jaxb-tree to a xml-file
       public static void jaxbSaveToFile(String filename1, MarshallableRootElement element1) {
           try {
               OutputStream os = new FileOutputStream(filename1);
               element1.validate();
               element1.marshal(os);
               os.close();

           } catch (Exception e) {
               console.exitOnException(e);
           }
       } // end method*/


    // lazy comparision. removes all #10,#13, tabs and spaces from the Strings
    // and compares them.
    // problem: the order of attributes may differ => method returns false
    /*public static boolean compareXml(String xml1, String xml2) {
        String in1 = xml1;
        String in2 = xml2;


        char[] ascChar = new char[1];
        ascChar[0] = 9;
        String asc_09 = new String(ascChar);
        ascChar[0] = 10;
        String asc_10 = new String(ascChar);
        ascChar[0] = 13;
        String asc_13 = new String(ascChar);

        in1 = StringTools.replace(in1, asc_10, "");
        in2 = StringTools.replace(in2, asc_10, "");

        in1 = StringTools.replace(in1, asc_13, "");
        in2 = StringTools.replace(in2, asc_13, "");

        in1 = StringTools.replace(in1, asc_09, "");
        in2 = StringTools.replace(in2, asc_09, "");

        in1 = StringTools.replace(in1, " ", "");
        in2 = StringTools.replace(in2, " ", "");

        //cat.fine("str1: \"" + in1 + "\"");
        //cat.fine("str2: \"" + in2 + "\"");

        FileTools.saveToFile(cfg.getTestOutputDir() + "in1.txt", in1);
        FileTools.saveToFile(cfg.getTestOutputDir() + "in2.txt", in2);





        //return in1.equals(in2);
        return in1.length() == in2.length();
    }*/


    // input String is a xpath query and the document to search
    // output is a list of all matching Elements
    public static List<Element> xpathQuery(org.jdom.Document document, String xpathQuery1, Namespace xNs) {
        List<Element> returnValue = new ArrayList<Element>();
        String xpathQuery = xpathQuery1;
        XPath xPath;
        try {
            if (document == null || document.getRootElement() == null) {
                return returnValue; //------------- EXIT POINT -------------
            }

            // if there is a xmlns namespace wihtout a prefix, then
            // a default one has to be added. Otherwise jaxen does not work
            // Note: the prefix "x" has to be used in the xpath-query as well.
            Namespace ns = document.getRootElement().getNamespace();
            if (ns.getPrefix().length() == 0) {
                /*
                StringTokenizer tokens = new StringTokenizer(xpathQuery, "/", true);
                xpathQuery = "";
                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
                    if (!token.equalsIgnoreCase("/")) {
                        token = "x:" + token;
                    }
                    xpathQuery += token;
                }*/

                //xPath = new JDOMXPath(xpathQuery);
                xPath = XPath.newInstance(xpathQuery);
                //xPath.addNamespace(ns);

                String uri = ns.getURI();
                if (uri == null || uri.length() == 0) {
                    uri = "dummy";
                }
                //System.out.println("uir: \"" + uri + "\"");
                //xPath.addNamespace("x", uri);
            } else {
                //xPath = new JDOMXPath(xpathQuery);
                xPath = XPath.newInstance(xpathQuery);
                xPath.addNamespace(ns);
            }

            if (xNs != null) {
                xPath.addNamespace(xNs.getPrefix(), xNs.getURI());
            }

            //cat.fine("OLD: \"" + xpathQuery1 + "\", NEW: \"" + xpathQuery + "\"");

            Object jdomNode = document; // Document, Element etc.
            //XPath path = new JDOMXPath("/*"); //--> das root element

            returnValue = xPath.selectNodes(jdomNode); // entries are from the type "org.jdom.Element"
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    public static List<Element> xpathQuery(org.jdom.Document document, String xpathQuery1) {
        return xpathQuery(document, xpathQuery1, null);
    }

    /**
     * attention: the parent of the element is detached when no cloning
     *
     * @param aElement
     * @param xpathQuery
     * @param doClone
     * @return
     */
    public static List<Element> xpathQuery(org.jdom.Element aElement, String xpathQuery, boolean doClone) {
        List<Element> returnValue = new ArrayList<Element>();

        try {
            //Document parentDocument;
            //parentDocument = aElement.getDocument();

            Document document;
            Document oldDocument = null;
            if (doClone) {
                Element clone = (Element) aElement.clone();
                document = new Document(clone);
            } else {
                oldDocument = aElement.getDocument();
                aElement.detach();
                document = new Document(aElement);
            }
            returnValue = xpathQuery(document, xpathQuery);


        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    // just some test stuff for the jaxen xpath library
/*
    public static void testJaxen() {
        try {
            org.jdom.Element root = new org.jdom.Element("root");
            org.jdom.Element child = new org.jdom.Element("child");
            child.setText("Ich bin ein Child");
            root.addContent(child);
            //org.jdom.Document doc1 = new org.jdom.Document(root);

            org.jdom.Document doc = XmlTools.readFromFile(Settings.getConfigFile(), null);

            org.jdom.Element e = doc.getRootElement();
            Object jdomNode = e; // Document, Element etc.
*/
    //XPath path = new JDOMXPath("/*"); //--> das root element
    //XPath path = new JDOMXPath("/imbConfig/mmdbConnection/*/host");
/*
            List results = path.selectNodes(jdomNode);
            System.out.println("Test: \"" + documentToString(e) + "\"");
            System.out.println("Results: " + results.size());

            Iterator it = results.iterator();
            while (it.hasNext()) {
                org.jdom.Element elem = (org.jdom.Element) it.next();
                System.out.println(elem.getName() + ", " + elem.getText());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    public void xml2Pdf(org.jdom.Document document, OutputStream out) {
        /*Templates template;
        TransformerFactory tFactory = TransformerFactory.newInstance();

        XMLFilter xmlFilter = ((SAXTransformerFactory) tFactory).newXMLFilter(template);
        xmlFilter.setParent(XMLReaderFactory.createXMLReader());

        Driver driver = new Driver();
        driver.setRenderer(driver.RENDER_PDF);
        driver.setOutputStream(out);
        driver.render(xmlFilter, (new JDOMSource(document)).getInputSource());
        driver.reset();*/
    }

    private static void goRecursive(Element startElement, String namespaceName, Namespace nameSpace, Namespace newNamespace) {
        startElement.removeNamespaceDeclaration(nameSpace);
        startElement.setNamespace(newNamespace);

        List aList = startElement.getAttributes();
        for (int i = 0; i < aList.size(); i++) {
            Attribute a = (Attribute) aList.get(i);
            //System.out.println("N: " + a.getName());
            //if (a.getName().equalsIgnoreCase("type")) {
            //    System.out.println("NN: " + a.getName());
            //}

            String prefix = namespaceName + ":";
            String value = a.getValue();
            if (value.startsWith(prefix)) {
                a.setValue(value.substring(prefix.length()));
            }
        }

        List childList = startElement.getChildren();
        for (int i = 0; i < childList.size(); i++) {
            org.jdom.Element element = (org.jdom.Element) childList.get(i);
            goRecursive(element, namespaceName, nameSpace, newNamespace);
        }
    }

    /**
     * Remove all tag element prefixes, eg. "<mpeg7:Mpeg7>" -> "<Mpeg7>"
     *
     * @param doc
     */
    public static void removeAllNamespacesRecursive(org.jdom.Element doc) {
        if (doc == null) {
            return;
        }

        doc.setNamespace(null);

        /*
        console.echo("Prefix: \"" + namespace.getPrefix() + "\", URI: \"" + namespace.getURI() +
                    "\", toString: \"" + namespace.toString() + "\"");

        console.echo("additional:");
        for (int i = 0; i < docList.size(); i++) {
            namespace = (Namespace) docList.get(i);
            console.echo("Prefix: \"" + namespace.getPrefix() + "\", URI: \"" + namespace.getURI() +
                    "\", toString: \"" + namespace.toString() + "\"");
        }
        */


        List childList = doc.getChildren();
        for (int i = 0; i < childList.size(); i++) {
            org.jdom.Element element = (org.jdom.Element) childList.get(i);
            removeAllNamespacesRecursive(element);
        }
    }

    /**
     * Remove all tag element prefixes, eg. "<mpeg7:Mpeg7>" -> "<Mpeg7>"
     *
     * @param doc
     */
    public static void removeAllNamespacePrefix(org.jdom.Element doc) {
        if (doc == null) {
            return;
        }

        //Element rootElement = doc.getRootElement();
        org.jdom.Element rootElement = doc;
        String prefix = rootElement.getNamespacePrefix();

        if (prefix == null || prefix.length() < 1) {
            return;
        }

        //Namespace mpeg7Namespace = Mpeg7Template.getMpeg7Namespace();
        //Namespace mpeg7Mpeg7Namespace = Namespace.getNamespace("mpeg7", "urn:mpeg:mpeg7:schema:2001");

        Namespace newNameSpace = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");

        Namespace namespace = Namespace.getNamespace(prefix);
        goRecursive(rootElement, prefix, namespace, newNameSpace);
    }

    /**
     * detaches the node from the docuemnt and make a new document from it
     *
     * @param node
     * @return
     */
    public static Document getNewDocumentFromElement(Element node) {
        Content c = node.detach();
        java.util.List l = new ArrayList();
        l.add(c);
        Document d = new Document(l);
        return d;
    }

    /**
     * returns the text of the element the xpath points to. if the element does not exist, defaultValue is returned
     *
     * @param element      the base element. e.g. new Element("root").add(..);
     * @param xPath        e.g. "root/person/firstname"
     * @param defaultValue "no first name given"
     * @return
     */
    public static String getXmlText(Element element, String xPath, String defaultValue) {
        String returnValue = defaultValue;

        try {
            Element elem = simpleXpath(element, xPath, null, false);
            if (elem != null && elem.getText() != null) {
                returnValue = elem.getText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    public static Document readFromFile(InputStream stream, Document defaultDocument) {
        Document returnValue = null;
        try {
            SAXBuilder builder = new SAXBuilder();
            //File file = new File(new URI(fileName));
            cat.fine("READ: " + stream);
            //File file = new File(FileTools.removeFileUrlPrefix(fileName));
            returnValue = builder.build(stream);
        } catch (Exception je) {
            console.error("ERROR while reading stream \"" + stream + "\"");
            je.printStackTrace();
            returnValue = defaultDocument;
        }
        return returnValue;
    }
}  // end class