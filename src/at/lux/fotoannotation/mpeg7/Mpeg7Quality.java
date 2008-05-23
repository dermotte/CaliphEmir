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

public class Mpeg7Quality {
    private String quality;
    private Element agent;
    private Namespace mpeg7;
    private Namespace xsi;

    public Mpeg7Quality(Element agent, String quality) {
        this.agent = agent;
        this.quality = quality;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    public Element createDocument() {
        Element root;
        root = new Element("MediaQuality", mpeg7);
        // root.addNamespaceDeclaration(xsi);
        // root.addNamespaceDeclaration(mpeg7);
        Element qrate = new Element("QualityRating", mpeg7).setAttribute("type", "subjective");
        qrate.addContent(new Element("RatingValue", mpeg7).addContent(quality));
        Element qscheme = new Element("RatingScheme", mpeg7);
        qscheme.setAttribute("style", "higherBetter");
        qscheme.setAttribute("worst", "1");
        qscheme.setAttribute("best", "5");
        qrate.addContent(qscheme);

        Element ag = new Element("RatingSource", mpeg7);
        ag = ((Element) agent.clone()).setName("RatingSource");
        ag.setAttribute("type", "PersonType", xsi);

        root.addContent(qrate);
        root.addContent(ag);

        return root;
    }
}

/*
<MediaQuality>
    <QualityRating type="subjective">
        <RatingValue>1</RatingValue>
        <RatingScheme style="higherBetter" best="5" worst="1"/>
    </QualityRating>
    <RatingSource xsi:type="PersonType">
        <Name>
            <GivenName>Mathias</GivenName>
            <FamilyName>Lux</FamilyName>
        </Name>
    </RatingSource>
</MediaQuality>
*/