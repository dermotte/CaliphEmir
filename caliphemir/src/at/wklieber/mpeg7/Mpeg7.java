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


import org.jdom.Element;
import org.jdom.Namespace;


/**
 * This class put mpeg7 documents together, that have various Descriptors
 * at the moment it is used for devoping testing
 */
public class Mpeg7 extends Mpeg7Template {

    public Mpeg7() {
        super();
    }

    /**
     * return the element that can be added to the root tag (Mpeg7)
     */
    public Element getOffsetForRoot() {
        String offset = ROOT_TAG;
        return simpleXpath(offset, mpeg7Namespace, new Element("undefined"), false);
    }

/*
  public static void main(String[] args) {
    Mpeg7 mpeg7 = new Mpeg7();
    mpeg7.createTemplateDocument();
    mpeg7.setCameraMovementValues("Mpeg7/Description/VisualDescriptor/Segment", "",0,0, null);

    System.out.println(mpeg7.toString());


    ColorDs color = new ColorDs(Color.RED,1,1,1,1);
    List objects = new Vector();
    objects.add(color);

    //mpeg7.putObjects(objects);

    System.out.println("---------------------------------------------------");
    System.out.println(mpeg7.toString());

    String filename = Settings.getTestOutputDir() + "mpeg7.xml";
    mpeg7.writeToFile(filename);
    System.out.print("written to: " + filename);
    //XmlTools.testJaxen();
  }
  */
    public void setDefaultValues() {
        super.setDefaultValues();
    }

    /**
     * this will be removed somethimes
     */
    public void setCameraMovementValues(String offset, String movement, int start, int duration, Namespace xsi) {
        //setDomAttribute(offset + ".VisualDescriptor", "xsi:type", "CameraMotionType");
        //setDomAttribute(offset + ".VisualDescriptor", "type", "CameraMotionType", xsi);
        //setDomAttribute(offset + ".VisualDescriptor.Segment", "xsi:type", "MixtureCameraMotionSegmentType");
        setDomValue(offset + ".Time.MediaTimePoint", "T00:00:00:07F25");
        setDomValue(offset + ".Time.MediaDuration", "PT03S13N25F");
        setDomValue(offset + ".FractionalPresence", "");
        setDomValue(offset + ".AmountOfMotion." + movement, "10");
    }


    // this methode creats a new documment tree - tructure
    public void createTemplateDocument() {
        super.createTemplateDocument();
    } // end method


    public void addDescriptor(Mpeg7Template descriptor1) {
        if (descriptor1 == null) {
            logger.severe("can not add descriptor that is null");
            return;
        }

        // get the startelement of the main mpeg7 file where the new content has to be added
        //Element pos = XmlTools.simpleXpath(document.getRootElement(), startPos, (Element) null, true);
        Element pos = this.getOffsetForRoot();

        // get the position of the descriptor document where the interessting data is
        //Element desElement = descriptor1.getDocument(new Element(ROOT_TAG));

        //console.line();
        //logger.fine("MPEG before: " + this.toString());
        //logger.fine("DESC before: " + descriptor1.toString());

        Element desElement = descriptor1.getOffsetForRoot();
        //logger.fine("DESC offset: " + XmlTools.documentToString(desElement));
        desElement = (Element) desElement.clone();

        //desElement.detach();
        //logger.fine("DESC detach: " + XmlTools.documentToString(desElement));
        //desElement.addNamespaceDeclaration(mpeg7Namespace);
        pos.addContent(desElement);

        //logger.fine("MPEG after : " + this.toString());
    }

} // end class