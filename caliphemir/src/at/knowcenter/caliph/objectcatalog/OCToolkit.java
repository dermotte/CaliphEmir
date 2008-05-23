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

package at.knowcenter.caliph.objectcatalog;

import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Namespace;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This file is part of Caliph & Emir.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class OCToolkit {
    public static URL getLog4JPropertyFile() {
        URL returnURL = null;
        try {
            returnURL = new File("log4j.properties").toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return returnURL;
    }


    public static URL getRelationsFile() {
        return OCToolkit.class.getResource("data/mpeg7-relations.xml");
    }

    public static URL getBaseObjectsFile() {
        return OCToolkit.class.getResource("data/base-objects.mp7.xml");
    }

    public static List xpathQuery(org.jdom.Document document, String xpathQuery1, Namespace xNs) {
        List returnValue = new Vector();
        String xpathQuery = xpathQuery1;
        XPath xPath;
        try {
            // if there is a xmlns namespace wihtout a prefix, then
            // a default one has to be added. Otherwise jaxen does not work
            // Note: the prefix "x" has to be used in the xpath-query as well.
            Namespace ns = document.getRootElement().getNamespace();
            if (ns.getPrefix().length() == 0) {
                StringTokenizer tokens = new StringTokenizer(xpathQuery, "/", true);
                xpathQuery = "";
                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
                    if (!token.equalsIgnoreCase("/")) {
                        token = "x:" + token;
                    }
                    xpathQuery += token;
                }

                xPath = new JDOMXPath(xpathQuery);
                xPath.addNamespace("x", ns.getURI());
            } else {
                xPath = new JDOMXPath(xpathQuery);
            }

            if (xNs != null) {
                xPath.addNamespace(xNs.getPrefix(), xNs.getURI());
            }

            // console.echo("OLD: \"" + xpathQuery1 + "\", NEW: \"" + xpathQuery + "\"");

            Object jdomNode = document; // Document, Element etc.
            //XPath path = new JDOMXPath("/*"); //--> das root element

            returnValue = xPath.selectNodes(jdomNode); // entries are from the type "org.jdom.Element"
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    public static Namespace getFSWNamespace() {
        return Namespace.getNamespace("fsw", "http://www.at.know-center.at/fsw");
    }

}
