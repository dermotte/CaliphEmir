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

/*
 * @author Mathias Lux, mathias@juggle.at
 * Date: 27.08.2002
 * Time: 12:58:34
 */
package at.knowcenter.caliph.objectcatalog.mpeg7tools;

import at.knowcenter.caliph.objectcatalog.OCToolkit;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Mpeg7Venue {
    private static boolean ADD_CAPACITY = false;
    private Document d;
    private Element root, semantics;
    private Namespace mpeg7, xsi, fsw;

    private String freeText, venueName, venueRegion, venueAddress, venueCapacity;
    private String idPlace, idState;

    /**
     * creates an empty Mpeg7 description of a venue with given label
     *
     * @param DocumentLabel is a descriptive Label of the resulting mpeg7 document
     */
    public Mpeg7Venue(String DocumentLabel) {
        freeText = "";
        venueName = "";
        venueRegion = "";
        venueAddress = "";
        venueCapacity = "";
        initDocument(DocumentLabel);
    }

    /**
     * creates an full Mpeg7 description of a venue with given label and content
     *
     * @param DocumentLabel is a descriptive Label of the resulting mpeg7 document
     * @param freeText      is a free text description of the venue
     * @param venueAddress  gives the address of the venue
     * @param venueCapacity defines how many people can get a place to wath the game, if it is <code>null</code> no capacity is given.
     * @param venueName     gives the name of the venue
     * @param venueRegion   gives the region of the venue, has to be a 2 character country code like at for austria or uk for england
     */
    public Mpeg7Venue(String DocumentLabel, String freeText, String venueAddress, String venueCapacity, String venueName, String venueRegion) {
        this.freeText = freeText;
        this.venueAddress = venueAddress;
        this.venueCapacity = venueCapacity;
        this.venueName = venueName;
        this.venueRegion = venueRegion;
        initDocument(DocumentLabel);
    }

    private void initDocument(String DocumentLabel) {
        // Set IDs:
        String time = Long.toString(System.currentTimeMillis());
        idPlace = "vp_" + time;
        idState = "vs_" + time;

        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        fsw = OCToolkit.getFSWNamespace();
        root = new Element("Mpeg7", mpeg7);
        root.setAttribute("schemaLocation", "urn:mpeg:mpeg7:schema:2001 Mpeg7-2001.xsd http://www.at.know-center.at/fsw Know-soccer.xsd", xsi);
        root.addNamespaceDeclaration(mpeg7);
        root.addNamespaceDeclaration(xsi);
        root.addNamespaceDeclaration(fsw);
        d = new Document(root);

        // Description
        Element description = new Element("Description", mpeg7);
        description.setAttribute("type", "SemanticDescriptionType", xsi);
        root.addContent(description);

        // Semantics
        semantics = new Element("Semantics", mpeg7);
        semantics.addContent(new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(DocumentLabel)));
        description.addContent(semantics);
    }

    /**
     * returns a valid MPEG7 document (org.jdom.Document)
     */
    public Document createDocument() {
        String tmpID = new String(venueName).toLowerCase().trim().replace(' ', '_').replace('\r', '_').replace('\t', '_').replace('\n', '_');
        idState = "vs_" + tmpID;
        idPlace = "vp_" + tmpID;
        // Venuet:
        Element venue = new Element("SemanticBase", mpeg7);
        venue.setAttribute("type", "fsw:FSWVenueType", xsi);
        venue.setAttribute("id", idPlace);
        semantics.addContent(venue);
        // Label of venue: International Stadium of Yokohama
        Element label = new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(venueName));
        venue.addContent(label);
        // Free text annotation of the venue
        Element definition = new Element("Definition", mpeg7).addContent(new Element("FreeTextAnnotation", mpeg7).addContent(freeText));
        venue.addContent(definition);
        // Place of the venue:
        Element place = new Element("Place", fsw);
        place.addContent(new Element("Name", mpeg7).addContent(venueName));
        place.addContent(new Element("Region", mpeg7).addContent(venueRegion));
        place.addContent(new Element("PostalAddress", mpeg7).addContent(new Element("AddressLine", mpeg7).addContent(venueAddress)));
        venue.addContent(place);

        venue.addContent(new Element("Capacity", fsw).addContent(venueCapacity));

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

//    /**
//     * for testing purpose only
//     */
//    public static void main(String[] args) {
//        try {
//            String toSearch = "ha";
//            SAXBuilder builder = new SAXBuilder();
//            Class c = Class.forName("org.apache.xindice.client.xmldb.DatabaseImpl");
//            Database database = (Database) c.newInstance();
//            DatabaseManager.registerDatabase(database);
//            Collection col = DatabaseManager.getCollection("xmldb:xindice:///db/imb/");
//            // String xpath = "//venue[contains(stadium,'" + toSearch + "')] | //venue[contains(address,'" + toSearch + "')]";
//            String xpath = "//venue";
//            XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
//            ResourceSet resultSet = service.query(xpath);
//            ResourceIterator results = resultSet.getIterator();
//            while (results.hasMoreResources()) {
//                Resource res = results.nextResource();
//                System.out.println((String) res.getContent() + "\n---\n");
//                Document d = builder.build(new StringReader((String) res.getContent()));
//                Element tmpResult = d.getRootElement();
//                String place = tmpResult.getChildText("city");
//                String country = tmpResult.getChildText("country");
//                String address = tmpResult.getChildText("address");
//                String name = tmpResult.getChildText("stadium");
//                String capacity = tmpResult.getChildText("capacity");
//                String cc = tmpResult.getChild("country").getAttribute("code").getValue();
//                if (cc.equals("jpn"))
//                    cc = "jp";
//                else
//                    cc = "kr";
//                Mpeg7Venue mv = new Mpeg7Venue("Venue test", "FIFA World Cup 2002 soccer stadium in " + place + ", " + country, address, capacity, name, cc);
//                System.out.println(mv.toString());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
