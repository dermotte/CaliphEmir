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


import at.wklieber.Settings;
import at.wklieber.tools.Console;
import at.wklieber.tools.XmlTools;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.List;


/**
 * special class for mpeg7 files: namespaces, ...
 */
public class Mpeg7Template extends XmlTemplate
        implements DsInterface {
    protected static Console console = Console.getReference();
    protected Mpeg7ConversionTools mpeg7Convert = Mpeg7ConversionTools.getReference();

    protected static final String ROOT_TAG = "Mpeg7";
    protected static final String CONTENT_DESCRIPTION_TAG = "Description";

    public static final String SCHEMA_LOCATION = Settings.getReference().getSchemaLocation();

    public static final String mpeg7Urn = "urn:mpeg:mpeg7:schema:2001";
    public static final String xsiUrn = "http://www.w3.org/2001/XMLSchema-instance";

    protected static final Namespace mpeg7Namespace = Namespace.getNamespace("", mpeg7Urn);
    protected static final Namespace xsiNamespace = Namespace.getNamespace("xsi", xsiUrn);

    // offset in a complete mpeg7 document where this descriptor occusrs
    // is used to extract data
    // format no starting and leading separator, e.g. "Mpeg7/content"
    // note: the offset strings are used also somewhere else
    // offest is used to write to the mpeg when creating
    protected String OFFSET_PATH = ROOT_TAG + "/Description";

    // the name of the Desctipor
    // this is the root tag name of this dom document
    // format: no starting and leading separator, e.g. "DescriptionMetadata"
    protected String DESCRIPTOR_TAG = "Undefined";


    public Mpeg7Template() {
        super();
    }

    public Mpeg7Template(Element mpeg7Element) {
        super();
        createTemplateDocument(false);
        mpeg7Element.detach();
        //XmlTools.removeAllNamespacePrefix(mpeg7Element);
        getDocument((Document) null).setRootElement(mpeg7Element);
        XmlTools.removeAllNamespacesRecursive(getDocument((Document) null).getRootElement());
        //XmlTools.removeAllNamespacePrefix(getDocument((Document) null).getRootElement());
    }

    public Mpeg7InstanceMetadata getInstanceMetadataAccess() {
        return new Mpeg7InstanceMetadata(this);
    }

    /**
     * return the path where the description data begins
     */
    public Element getOffset() {
        String offset = OFFSET_PATH + "/" + DESCRIPTOR_TAG;
        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
    }


    /**
     * return the element that can be added to the root tag (Mpeg7)
     */
    public Element getOffsetForRoot() {
        String offset = ROOT_TAG + "/" + CONTENT_DESCRIPTION_TAG;
        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
    }

    public static String getRootTag() {
        return ROOT_TAG;
    }

    public static Namespace getMpeg7Namespace() {
        return mpeg7Namespace;
    }

    public static Namespace getXsiNamespace() {
        return xsiNamespace;
    }

    public void setDefaultValues() {
        super.setDefaultValues();
    }

    // create a new JDOM-Document with a root-Element and mpeg7 setting
    protected void createNewDocument(String rootName1, String dtdUrl1, boolean withMpeg7Namespace) {
        initialize();
        Element rootElement = null;
        if (withMpeg7Namespace) {
            rootElement = new Element(rootName1, mpeg7Namespace);
            rootElement.setAttribute("schemaLocation", "urn:mpeg:mpeg7:schema:2001 " + SCHEMA_LOCATION, xsiNamespace);
            rootElement.addNamespaceDeclaration(mpeg7Namespace);
            rootElement.addNamespaceDeclaration(xsiNamespace);
        } else {
            rootElement = new Element(rootName1);
        }

        document = new Document(rootElement);
    }


    public void createTemplateDocument() {
        createTemplateDocument(true);
    }

    // this methode creats a new documment tree - tructure
    public void createTemplateDocument(boolean withMpeg7NameSpace) {
        createNewDocument(ROOT_TAG, null, true);
    } // end method

    /**
     * derived classes can override this to extract data specific to their content type.
     * e.g DominantColors, SeminticDescriptors, ...
     *
     * @param mpeg7Document
     * @return
     */
    public List extractFromMpeg7(Document mpeg7Document) {
        return null;
    }

    /**
     * walks though the child specified in loacation1 and returns the text
     * from that Element or the content of "default1" if the child doesn't exists
     */
    protected String getDomValue(String location1, String default1) {
        String ret = default1;

        Element result = simpleXpath(location1, mpeg7Namespace, (Element) null, false);
        if (result != null) {
            ret = result.getText();
        }

        return ret;
    }

    public Mpeg7Template clone() {
        Mpeg7Template returnValue = null;
        Element elem = this.getDocument((Element)null);
        Element newElem = (Element) elem.clone();
        assert(elem != newElem);
        returnValue = new Mpeg7Template(newElem);

        return returnValue;
    }

} // end class