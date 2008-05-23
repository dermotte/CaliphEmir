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

import at.wklieber.tools.XmlTools;
import org.jdom.Document;
import org.jdom.Element;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


class ModelStruct {
    public Color drawColor = null;
    public int posX = 0;
    public int posY = 0;
    public int heigh = 0;
    public int width = 0;
} // end class


/**
 * @deprecated use DominatColor plus SpatialDecomposition insteade
 */
public class ColorDs extends XmlTemplate {

    private ModelStruct object_ = null;

    public ColorDs(Color color, int posX, int posY, int width, int heigh) {
        object_ = new ModelStruct();
        Color color1 = new Color(3);

        object_.drawColor = color;
        object_.posX = posX;
        object_.posY = posY;
        object_.heigh = heigh;
        object_.width = width;
    }

    public static void main(String[] args) {
        //ColorDs colorDs1 = new ColorDs();
    }

    // retrieve the xml-structure of this Element
    public Element getObjectXml(Element default1) {
        Element returnValue = default1;

        createNewDocument("Segment", null);
        //setDomAttribute("Segment", "xsi:type", "Type");
        setDomValue("Segment.Color.red", Integer.toString(object_.drawColor.getRed()));
        setDomValue("Segment.Color.green", Integer.toString(object_.drawColor.getGreen()));
        setDomValue("Segment.Color.blue", Integer.toString(object_.drawColor.getBlue()));
        setDomValue("Segment.posx", Integer.toString(object_.posX));
        setDomValue("Segment.posy", Integer.toString(object_.posY));
        setDomValue("Segment.heigh", Integer.toString(object_.heigh));
        setDomValue("Segment.width", Integer.toString(object_.width));

        returnValue = document.getRootElement();

//        System.out.println("DS write: " + object_.posX + ", " + object_.posY + ", " + object_.width + ", " + object_.heigh + ", C:  " + object_.drawColor);


        return returnValue;
    }

    public Color color() {
        Color color1 = object_.drawColor;   //new Color(Integer.parseInt((object_.drawColor)));
        return color1;
    }


    public int posX() {
        return object_.posX;
    }

    public int posY() {
        return object_.posY;
    }

    public int height() {
        return object_.heigh;
    }

    public int width() {
        return object_.width;
    }

    // read the parts of the xml file, that belongs to this
    // DS and return a list with all matched and "ColorDS"
    //
    public static List getObjectDSfromXml(Document document1) {
        List returnValue = new Vector();

        List elementList = XmlTools.xpathQuery(document1, "/Mpeg7/Description/VisualDescriptor/Segment");

        Iterator it = elementList.iterator();
        while (it.hasNext()) {
            Element elem = (Element) it.next();
            //String colorName = XmlTools.simpleXpath(elem, "Segment/Color", null, false).getText();

            int red = Integer.parseInt(XmlTools.simpleXpath(elem, "Segment/Color/red", null, false).getText());
            int green = Integer.parseInt(XmlTools.simpleXpath(elem, "Segment/Color/green", null, false).getText());
            int blue = Integer.parseInt(XmlTools.simpleXpath(elem, "Segment/Color/blue", null, false).getText());

            Color drawColor = new Color(red, green, blue);


            int posx = Integer.parseInt(XmlTools.simpleXpath(elem, "Segment/posx", null, false).getText());
            int posy = Integer.parseInt(XmlTools.simpleXpath(elem, "Segment/posy", null, false).getText());
            int heigh = Integer.parseInt(XmlTools.simpleXpath(elem, "Segment/heigh", null, false).getText());
            int width = Integer.parseInt(XmlTools.simpleXpath(elem, "Segment/width", null, false).getText());

            //System.out.println("ColorName: \"" + colorName + "\"");
            ColorDs color = new ColorDs(drawColor, posx, posy, width, heigh);
            returnValue.add(color);

//            System.out.println("DS read: " + posx + ", " + posy + ", " + width + ", " + heigh);
        }

        //System.out.println("done red in ");

        return returnValue;
    }

    public static List getObjectDSfromXml(String filename1) {
        Document doc = XmlTools.readFromFile(filename1, null);


        return getObjectDSfromXml(doc);
    }
}
