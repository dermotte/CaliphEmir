package at.lux.imageanalysis;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Created by IntelliJ IDEA.
 * User: mlux
 * Date: 13.12.2006
 * Time: 16:16:04
 * To change this template use File | Settings | File Templates.
 */
public class ColorStructure extends ColorStructureImplementation implements JDomVisualDescriptor {
    /**
     * xsiXMLns is constant for XML Namespace xmlns:xsi = "urn:mpeg:mpeg7:schema:2001"
     */

    private static final Namespace xsiXMLns = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    /**
     * xsiXMLns is constant for XML Namespace xmlns:xsi = "urn:mpeg:mpeg7:schema:2001"
     */

    private static final Namespace xsiSLXMLns = Namespace.getNamespace("schemaLocation", "urn:mpeg:mpeg7:schema:2001 .\\Mpeg7-2001.xsd");

    /**
     * XMLns is constant for XML Namespace xmlns = "http://www.mpeg7.org/2001/MPEG-7_Schema"
     */

    private static final Namespace XMLns = Namespace.getNamespace("urn:mpeg:mpeg7:schema:2001");


    public Element getDescriptor() {
        StringBuilder sb = new StringBuilder(256);
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element vdesc = new Element("VisualDescriptor", mpeg7);
        vdesc.setAttribute("type", "ColorStructureDescriptor", xsi);

        Element histogramm = new Element("Values", mpeg7);
        vdesc.addContent(histogramm);

        for (float value : ColorHistogram) {
            sb.append((int) value);
            sb.append(' ');
        }
        histogramm.setText(sb.toString());

        return vdesc;  //To change body of implemented methods use File | Settings | File Templates.

    }

    /**
     * The <code>setDescriptionFromString(String xmlString)</code> class is responsible for reading
     * XML Documents
     */

    public void setDescriptionFromString(String xmlString) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        java.io.StringReader XMLsr = new java.io.StringReader(xmlString);
        Document doc = builder.build(XMLsr);
        Element root = doc.getRootElement();

        if (root.getName().equals("Mpeg7")) {
            Element child = root.getChild("Description", XMLns);
        } else throw new Exception("XML format error: not Mpeg7 Descriptor");
    }

    /**
     * The <code>getDescriptionAsString()</code> class is responsible for writing XML Documents.
     * XMLOutputter method helps to serialize the document and stores it into a String. Encoding is set to iso-8859-1
     */

    public String getDescriptionAsString() throws Exception {

        String Histogram = new String("");                                // String containing ColorStructureDescriptor histogramm for XML Output

        Element root = new Element("Mpeg7", XMLns);
        Element description = new Element("Description");
        Element mmediaContent = new Element("MultimediaContent");
        Element image = new Element("Image");
        Element visualDescriptor = new Element("VisualDescriptor");
        Element histogramm = new Element("Values");

        root.addNamespaceDeclaration(xsiXMLns);
        root.addNamespaceDeclaration(xsiSLXMLns);
        description.addContent(mmediaContent);
        mmediaContent.addContent(image);
        image.addContent(visualDescriptor);
        visualDescriptor.addContent(histogramm);
        description.setAttribute("type", "ContentEntityType");

        visualDescriptor.setAttribute("type", "ColorStructureDescriptor");

        for (float histogram : ColorHistogram) {
            Histogram = Histogram + (int) histogram + " ";        // build an string with the histogram values in a row
        }
        histogramm.setText(Histogram);
        root.addContent(description);
        Document document = new Document(root);
        try {

            Format format = Format.getPrettyFormat();
            format.setEncoding("iso-8859-1");
            XMLOutputter serializer = new XMLOutputter(format);

            return serializer.outputString(document);

        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
