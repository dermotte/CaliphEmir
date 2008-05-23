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

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.util.Iterator;
import java.util.Vector;
import java.util.List;

public class Mpeg7DescriptionMetadata {
    Element root;
    Namespace mpeg7, xsi;

    /**
     * creates a new MPEG-7 DescriptionMetadata element + subelements
     *
     * @param version                      gives the version of the description, e.g. "1.0"
     * @param givenName                    first name of the person who annotates, e.g. John
     * @param familyName                   familyname of the person who annotates, e.g. Smith. Can be <code>null</code>
     * @param organization                 name of the organization, e.g. W3C. Can be <code>null</code>
     * @param addressLines                 Vector of Strings, lines of the postal address of the annotator. Can be <code>null</code>
     * @param phone                        telephone number of the annotator. Can be <code>null</code>
     * @param fax                          fax number of the annotator. Can be <code>null</code>
     * @param email                        email address of the annotator. Can be <code>null</code>
     * @param url                          homnepage url of the annotator. Can be <code>null</code>
     * @param creationLocationAddressLines Vector of Strings, lines of the postal address of the creation location. Can be <code>null</code>
     * @param toolName                     name of the instrument used to create the annotation. Can be <code>null</code>
     * @param freeText                     additional comment. Can be <code>null</code>
     */
    public Mpeg7DescriptionMetadata(String version, String givenName, String familyName,
                                    String organization, List addressLines, String phone,
                                    String fax, String email, String url, Vector creationLocationAddressLines,
                                    String toolName, String freeText) {

        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        root = new Element("DescriptionMetadata", mpeg7);
        // root.addNamespaceDeclaration(xsi);
        // root.addNamespaceDeclaration(mpeg7);
        root.addContent(new Element("Version", mpeg7).addContent(version));
        if (freeText != null)
            root.addContent(new Element("Comment", mpeg7).addContent(new Element("FreeTextAnnotation", mpeg7).addContent(freeText)));
        // creator
        Element creator = new Element("Creator", mpeg7);
        root.addContent(creator);
        creator.addContent(new Element("Role", mpeg7).setAttribute("href", "creatorCS").addContent(new Element("Name", mpeg7).addContent("Creator")));
        Element agent = new Element("Agent", mpeg7).setAttribute("type", "PersonType", xsi);
        creator.addContent(agent);
        Element name = new Element("Name", mpeg7);
        name.addContent(new Element("GivenName", mpeg7).addContent(givenName));
        if (familyName != null) name.addContent(new Element("FamilyName", mpeg7).addContent(familyName));
        agent.addContent(name);
        if (organization != null) agent.addContent(new Element("Affiliation", mpeg7).addContent(new Element("Organization", mpeg7).addContent(new Element("Name", mpeg7).addContent(organization))));
        if (addressLines != null) {
            Element paddress = new Element("PostalAddress", mpeg7);
            for (Iterator i = addressLines.iterator(); i.hasNext();) {
                String s = (String) i.next();
                paddress.addContent(new Element("AddressLine", mpeg7).addContent(s));
            }
            agent.addContent(new Element("Address", mpeg7).addContent(paddress));
        }
        Element eaddress = new Element("ElectronicAddress", mpeg7);
        if ((phone != null) || (fax != null) || (email != null) || (url != null))
            agent.addContent(eaddress);
        if (phone != null) eaddress.addContent(new Element("Telephone", mpeg7).addContent(phone));
        if (fax != null) eaddress.addContent(new Element("Fax", mpeg7).addContent(fax));
        if (email != null) eaddress.addContent(new Element("Email", mpeg7).addContent(email));
        if (url != null) eaddress.addContent(new Element("Url", mpeg7).addContent(url));
        // creationlocation
        if (creationLocationAddressLines != null) {
            Element paddress = new Element("PostalAddress", mpeg7);
            for (Iterator i = creationLocationAddressLines.iterator(); i.hasNext();) {
                String s = (String) i.next();
                paddress.addContent(new Element("AddressLine", mpeg7).addContent(s));
            }
            root.addContent(new Element("CreationLocation", mpeg7).addContent(paddress));
        }
        // creationtool
        if (toolName != null) {
            root.addContent(new Element("Instrument", mpeg7).addContent((new Element("Tool", mpeg7).addContent((new Element("Name", mpeg7).addContent(toolName))))));
        }

    }

    public Mpeg7DescriptionMetadata(String version, Element agent, Vector creationLocationAddressLines,
                                    String toolName, String freeText, String time) {

        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        root = new Element("DescriptionMetadata", mpeg7);
        // root.addNamespaceDeclaration(xsi);
        // root.addNamespaceDeclaration(mpeg7);
        root.addContent(new Element("Version", mpeg7).addContent(version));
        if (freeText != null)
            root.addContent(new Element("Comment", mpeg7).addContent(new Element("FreeTextAnnotation", mpeg7).addContent(freeText)));
        // creator
        Element creator = new Element("Creator", mpeg7);
        root.addContent(creator);
        creator.addContent(new Element("Role", mpeg7).setAttribute("href", "creatorCS").addContent(new Element("Name", mpeg7).addContent("Creator")));
        creator.addContent(agent);
        // creationlocation
        if (creationLocationAddressLines != null) {
            Element paddress = new Element("PostalAddress", mpeg7);
            for (Iterator i = creationLocationAddressLines.iterator(); i.hasNext();) {
                String s = (String) i.next();
                paddress.addContent(new Element("AddressLine", mpeg7).addContent(s));
            }
            root.addContent(new Element("CreationLocation", mpeg7).addContent(paddress));
        }
        if (time != null)
            root.addContent(new Element("CreationTime", mpeg7).addContent(time));
        // creationtool
        if (toolName != null) {
            root.addContent(new Element("Instrument", mpeg7).addContent((new Element("Tool", mpeg7).addContent((new Element("Name", mpeg7).addContent(toolName))))));
        }

    }

    public Element getDescriptionMetadata() {
        return root;
    }

    /**
     * returns string representation of the content
     *
     * @return the annotaion infa as formatted XML string
     */
    public String toString() {
        return new XMLOutputter(Format.getPrettyFormat()).outputString(root);
    }

/* SAMPLE DESCRIPTION:
    <DescriptionMetadata>
		<Version>1.0</Version>
		<Comment>
			<FreeTextAnnotation>Description of the Annotation</FreeTextAnnotation>
		</Comment>
		<Creator>
			<Role href="creatorCS">
				<Name>Creator</Name>
			</Role>
			<Agent xsi:type="PersonType">
				<Name>
					<GivenName>Mathias</GivenName>
					<FamilyName>Lux</FamilyName>
				</Name>
				<Affiliation>
					<Organization>
						<Name>Know Center</Name>
					</Organization>
				</Affiliation>
				<Address>
					<PostalAddress>
						<AddressLine>Know-Center</AddressLine>
						<AddressLine>z.H. Mathias Lux</AddressLine>
						<AddressLine>Inffeldgasse 16c</AddressLine>
						<AddressLine>8010 Graz</AddressLine>
						<AddressLine>Austria</AddressLine>
					</PostalAddress>
				</Address>
				<ElectronicAddress>
					<Telephone>+43 (316) 873 - 5669</Telephone>
					<Fax>+43 (316) 873 - 5688</Fax>
					<Email>mathias@juggle.at</Email>
					<Url>http://www.at.know-center.at</Url>
				</ElectronicAddress>
			</Agent>
		</Creator>
		<CreationLocation>
			<PostalAddress>
				<AddressLine>Know-Center</AddressLine>
				<AddressLine>z.H. Mathias Lux</AddressLine>
				<AddressLine>Inffeldgasse 16c</AddressLine>
				<AddressLine>8010 Graz</AddressLine>
				<AddressLine>Austria</AddressLine>
			</PostalAddress>
		</CreationLocation>
		<CreationTime/>
		<Instrument>
			<Tool>
				<Name>IMB Prototype</Name>
			</Tool>
		</Instrument>
	</DescriptionMetadata>

    */
}
