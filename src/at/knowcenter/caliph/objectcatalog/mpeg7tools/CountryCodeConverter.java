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

import at.knowcenter.caliph.objectcatalog.semanticscreator.BeeDataExchange;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Date: 29.08.2002
 * Time: 11:47:33
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class CountryCodeConverter {
    public static String[] genericCodes3 = {"gre", "den", "ger", "ned", "sui", "eng", "uae", "kuw", "gua", "crc", "par", "sco", "por", "ksa", "mas", "lib", "sin", "van", "tri", "zim"};
    public static String[] genericCodeTable = {"gr", "dk", "de", "nl", "ch", "en", "ae", "kw", "gt", "hr", "py", "en", "pt", "sa", "my", "lb", "sg", "vu", "tt", "zw"};

    /**
     * converts an 3 letter ISO country code into an 2 letter ISO country code (e.g. AUT -> AT)
     *
     * @param cc is the 3 letter country code to be converted
     * @return null if no matching country code is found.
     */
    public static String convert3to2(String cc) {
        String returnVal = null;
        SAXBuilder sb = new SAXBuilder();
        Document d = null;
        try {
            d = sb.build(CountryCodeConverter.class.getResource("countrycodes.xml"));

            Element root = d.getRootElement();
            List l = root.getChildren();
            for (Iterator i = l.iterator(); i.hasNext();) {
                Element e = (Element) i.next();
                if (cc.toLowerCase().equals(e.getAttributeValue("code3").toLowerCase()))
                    returnVal = e.getAttributeValue("code2");
            }
            if (!(returnVal != null)) {
                for (int i = 0; i < genericCodes3.length; i++) {
                    String s = genericCodes3[i];
                    if (cc.toLowerCase().equals(s))
                        returnVal = genericCodeTable[i];
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVal;
    }

    /**
     * converts an 3 letter Fifa World Cup country code into an 2 letter ISO country code (e.g. AUT -> AT)
     *
     * @param cc is the 3 letter country code to be converted
     * @return null if no matching country code is found.
     */
    public static String convertFifaCodetoISO2(String cc) {
        String returnVal = null;
        String[] cc3 = BeeDataExchange.COUNTRIES_CODE_3_LETTERS;
        String[] cc2 = BeeDataExchange.ISO_COUNTRIES_CODE_2_LETTERS;
        for (int i = 0; i < cc3.length; i++) {
            if (cc3[i].equals(cc))
                returnVal = cc2[i];
        }
        return returnVal;
    }

    /**
     * converts an 2 letter ISO country code into an 3 letter ISO country code (e.g. AT -> AUT)
     *
     * @param cc is the 2 letter country code to be converted
     * @return null if no matching country code is found.
     */
    public static String convert2to3(String cc) {
        String returnVal = null;
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build(CountryCodeConverter.class.getResource("countrycodes.xml"));
            Element root = d.getRootElement();
            List l = root.getChildren();
            for (Iterator i = l.iterator(); i.hasNext();) {
                Element e = (Element) i.next();
                if (cc.toLowerCase().equals(e.getAttributeValue("code2").toLowerCase()))
                    returnVal = e.getAttributeValue("code3");
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVal;
    }

    /**
     * Returns the name of the country given by the countrycode
     *
     * @param countryCode is the 2 or 3 letter country code to be searched for (both are accepted)
     * @return null if no matching country code is found.
     */
    public static String getCountryOfCode(String countryCode) {
        String returnVal = null;
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build(CountryCodeConverter.class.getResource("countrycodes.xml"));
            Element root = d.getRootElement();
            List l = root.getChildren();
            for (Iterator i = l.iterator(); i.hasNext();) {
                Element e = (Element) i.next();
                if (countryCode.toLowerCase().equals(e.getAttributeValue("code2").toLowerCase()) || countryCode.toLowerCase().equals(e.getAttributeValue("code3").toLowerCase()))
                    returnVal = e.getTextTrim();
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVal;

    }

    /**
     * Converts a given file of comma separated values into an valid XML file needed for the country code conversion
     * and writes it to STDOUT.<br>
     * The format of the file is as follows:
     * <code>[country name];[2 letter code];[3 letter code];[number]</code>
     */
    public static void convertCsvToXml(File f) {
        Element root = new Element("countries");
        Document d = new Document(root);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String s = br.readLine().trim();
            while (s != null) {
                if (s.length() > 7) {
                    StringTokenizer st = new StringTokenizer(s, ";", false);
                    String country = toName(st.nextToken());
                    String cc2 = st.nextToken().toLowerCase();
                    String cc3 = st.nextToken().toLowerCase();
                    String ccNum = st.nextToken().toLowerCase();
                    Element eCountry = new Element("country");
                    eCountry.setAttribute("code2", cc2);
                    eCountry.setAttribute("code3", cc3);
                    eCountry.setAttribute("number", ccNum);
                    eCountry.addContent(country);
                    root.addContent(eCountry);
                }
                s = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        try {
            out.output(d, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toName(String name) {
        StringTokenizer st = new StringTokenizer(name, " ");
        StringBuffer returnVal = new StringBuffer();
        while (st.hasMoreElements()) {
            char[] token = st.nextToken().toLowerCase().toCharArray();
            token[0] = Character.toUpperCase(token[0]);
            returnVal.append(token);
            returnVal.append(" ");
        }
        return returnVal.toString().trim();
    }

//    public static void main(String[] args) {
//        CountryCodeConverter.convertCsvToXml(new File("data/countries.txt"));
//    }
}
