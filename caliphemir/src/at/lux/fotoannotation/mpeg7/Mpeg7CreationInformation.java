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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
package at.lux.fotoannotation.mpeg7;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class Mpeg7CreationInformation {
    private String time, placename;
    private Element agent;
    private String tool;
    private Vector settings;

    Namespace mpeg7, xsi;
    Element root;
    Document doc;

    public Mpeg7CreationInformation(Element agent, String tool, Vector settings, String time) {
        this.agent = agent;
        this.settings = settings;
        this.tool = tool;
        this.time = time;
        placename = null;
        init();
    }

    private void init() {
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root = new Element("CreationInformation", mpeg7);
        doc = new Document(root);

        // creator & title
        Element creation = new Element("Creation", mpeg7);
        Element title = new Element("Title", mpeg7).addContent("Creation information");
        if (agent != null) {
            Element creator = new Element("Creator", mpeg7);
            Element role = new Element("Role", mpeg7).setAttribute("href", "creatorCS").addContent(new Element("Name", mpeg7).addContent("Creator"));
            creator.addContent(role).addContent(((Element) agent.clone()));
            creation.addContent(title).addContent(creator);
        }

        if (time != null || placename != null) {
            Element creationCoordinates = new Element("CreationCoordinates", mpeg7);
            if (time != null) creationCoordinates.addContent(new Element("Date", mpeg7).addContent((new Element("TimePoint", mpeg7).addContent(time))));
            if (placename != null)
                creationCoordinates.addContent(new Element("Location", mpeg7).addContent(new Element("Name", mpeg7).addContent(placename)));
            creation.addContent(creationCoordinates);
        }

        if (tool != null) {
            Element toolElement = new Element("Tool", mpeg7).addContent(new Element("Name", mpeg7).addContent(tool));
            Element creationTool = new Element("CreationTool", mpeg7).addContent(toolElement);
            if (settings != null) {
                for (Iterator i = settings.iterator(); i.hasNext();) {
                    String s = (String) i.next();
                    StringTokenizer st = new StringTokenizer(s, "=");
                    Element e = new Element("Setting", mpeg7);
                    e.setAttribute("name", st.nextToken());
                    e.setAttribute("value", st.nextToken());
                    creationTool.addContent(e);
                }
            }
            creation.addContent(creationTool);
        }
        root.addContent(creation);
        // Element creation = new Element("Creation", mpeg7);

    }

    public Element createDocument() {
        return root;
    }
}
