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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at) and the Know-Center Graz
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */

package at.knowcenter.caliph.objectcatalog.mpeg7tools;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Date: 30.08.2002
 * Time: 13:03:35
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Mpeg7SemanticEvent {
    Document d;
    Element semantics;
    private Namespace mpeg7, xsi;
    private String label;
    private String name_de, name_en, desc;

    /**
     * creates a new semantic event with given parameters
     *
     * @param nameDe        name of the event in german language
     * @param nameEn        name of the event in english
     * @param description   free text description of the event
     * @param documentLabel label of the document, just like a comment or a descriptive name
     */
    public Mpeg7SemanticEvent(String nameDe, String nameEn, String description, String documentLabel) {
        this.desc = description;
        this.label = documentLabel;
        this.name_de = nameDe;
        this.name_en = nameEn;
        initDocument();
    }

    public Mpeg7SemanticEvent(String name, String description, String documentLabel) {
        this.desc = description;
        this.label = documentLabel;
        this.name_de = name;
        this.name_en = null;
        initDocument();
    }

    private void initDocument() {
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Element root = new Element("Mpeg7", mpeg7);
        root.setAttribute("schemaLocation", "urn:mpeg:mpeg7:schema:2001 C:\\mpeg7\\Mpeg7-2001.xsd", xsi);
        root.addNamespaceDeclaration(mpeg7);
        root.addNamespaceDeclaration(xsi);
        d = new Document(root);

        // Description
        Element description = new Element("Description", mpeg7);
        description.setAttribute("type", "SemanticDescriptionType", xsi);
        root.addContent(description);

        // Semantics
        semantics = new Element("Semantics", mpeg7);
        semantics.addContent(new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(label)));
        description.addContent(semantics);
    }

    public Document createDocument() {
        Element event = new Element("SemanticBase", mpeg7);
        event.setAttribute("type", "EventType", xsi);
        event.setAttribute("id", "event-object-" + name_en.toLowerCase().replace(' ', '_'));
        semantics.addContent(event);
        Element label;
        Element mNameDe;
        Element mNameEn;
        if (name_en != null) {
            mNameDe = new Element("Name", mpeg7).addContent(name_de).setAttribute("lang", "de", Namespace.XML_NAMESPACE);
            mNameEn = new Element("Name", mpeg7).addContent(name_en).setAttribute("lang", "en", Namespace.XML_NAMESPACE);
            label = new Element("Label", mpeg7).addContent(mNameDe).addContent(mNameEn);
        } else {
            mNameDe = new Element("Name", mpeg7).addContent(name_de);
            label = new Element("Label", mpeg7).addContent(mNameDe);
        }
//        label = new Element("Label", mpeg7).addContent(mNameDe).addContent(mNameEn);
        event.addContent(label);
        Element definition = new Element("Definition", mpeg7).addContent(new Element("FreeTextAnnotation", mpeg7).addContent(desc));
        event.addContent(definition);
        return d;
    }

    /**
     * returns a valid MPEG7 document as String
     */
    public String toString() {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String strData = outputter.outputString(createDocument());
        return strData;
    }

}
