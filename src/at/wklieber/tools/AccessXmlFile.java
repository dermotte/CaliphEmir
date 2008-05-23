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
package at.wklieber.tools;



import org.jdom.Document;
import org.jdom.Element;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class AccessXmlFile implements IAccessFile {

    static Logger cat = Logger.getLogger(AccessXmlFile.class.getName());
    private static Console console = Console.getReference();

    private static boolean IS_SECTION_SUPPORTED = false;
    private String filename_ = null;
    private Document doc_ = null;
    private Element root_ = null;

    //----------------- Constructor ---------------------------------------------
    public AccessXmlFile() {
    }

    public AccessXmlFile(String file1, Document defaultDocument)
            throws Exception {
        open(file1, defaultDocument);
    }  // end method


    public AccessXmlFile(String file1)
            throws Exception {
        open(file1, new Document(new Element("root")));
    }  // end method

    public AccessXmlFile(InputStream stream) {
        open(stream, new Document(new Element("root")));
    }

    private void open(InputStream stream, Document defaultDocument) {
        cat.fine("open the xml-file");
        doc_ = XmlTools.readFromFile(stream, defaultDocument);

        filename_ =null;
        root_ = doc_.getRootElement();
    }

    public void open(String file1)
            throws Exception {
        open(file1, new Document(new Element("root")));

    }


    public void open(String file1, Document defaultDocument)
            throws Exception {
        cat.fine("trying to open: " + file1);
        //Document tempDoc = new Document(new Element("root"));
        Document tempDoc = defaultDocument;
        close();
        filename_ = file1;

/*// first try open without url
      filename_ = FileTools.removeFileUrlPrefix(file1);
      File file = new File(filename_);
      if (!file.exists()) {
         cat.fine("file not found, try a url: " + filename_);

         filename_ = FileTools.setUrlPrefix(file1);
         file
      }

      if (!file.exists()) {
         cat.fine("create file: " + file1);
         //file.createNewFile();
         XmlTools.saveToFile(filename_, tempDoc);
      }
*/
        if (!FileTools.existsFile(filename_)) {
            //  cat.fine("create file: " + file1);
            //file.createNewFile();
            //XmlTools.saveToFile(filename_, tempDoc);
        }

        cat.fine("open the xml-file");
        doc_ = XmlTools.readFromFile(filename_, tempDoc);

        root_ = doc_.getRootElement();
        //System.out.println("Doc_read in, is-null: " + (doc_ == null));
    } // end method

    public void close() throws Exception {
        if (doc_ != null) {
            XmlTools.saveToFile(filename_, doc_);
        }
        doc_ = null;
        filename_ = null;
    }

    public boolean isSectionSupported() {
        return true;
    }


    private Element getElement(String location1, Element default1, boolean create1) {
        return XmlTools.simpleXpath(root_, location1, default1, create1);
    } // end method

    private Element getElement(String location1, Element default1) {
        return getElement(location1, default1, false);
    }

    public String getProperty(String section1, String key1, String defaultKey1) {
        String ret = defaultKey1;

        Element key = getKey(section1, key1, false);
        //Element key = getElement(section1 + "/" + key1, (Element) null);
        if (key != null) {
            ret = key.getText().trim();
        }

        return ret;
    } // end method


    private Element getKey(String section1, String key1, boolean doCreate1) {
        String xpath;

        if (section1 == null || section1.length() < 1) {
            xpath = "";
        } else {
            xpath = section1; //todo: ohne führendes oder abschliesendes "/"
        }

        if (key1 != null && key1.length() > 0) {
            xpath += "/" + key1;
        }

        Element returnValue = getElement(xpath, (Element) null, doCreate1);

        return returnValue;
    }

    public int getProperty(String section1, String key1, int defaultKey1) {
        int ret = defaultKey1;

        Element key = getKey(section1, key1, false);
        if (key != null) {
            String value = key.getText().trim();

            try {
                ret = Integer.parseInt(value);
            } catch (Exception e) {
                cat.severe(e.toString());
                ret = defaultKey1;
            }
        } // end if

        return ret;
    } // end method


    public boolean getProperty(String section1, String key1, boolean defaultKey1) {
        boolean ret = defaultKey1;

        Element key = getKey(section1, key1, false);
        if (key != null) {
            String value = key.getText().trim();
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") ||
                    value.equalsIgnoreCase("1")) {
                ret = true;
            } else {
                ret = false;
            }
        }

        return ret;
    } // end method


    public void setProperty(String section1, String key1, String value1) throws Exception {
        Element key = getKey(section1, key1, true);
        if (key != null) {
            key.setText(value1);
        }

        XmlTools.saveToFile(filename_, doc_);
    } // end method


    private String makeXpathQuery(String section1, String key1) {
        String returnValue = section1;

        try {
            if (returnValue.charAt(0) != '/')
                returnValue = "/" + returnValue;

            if (returnValue.endsWith("/"))
                returnValue = returnValue.substring(0, returnValue.length() - 1);

            returnValue += "/" + key1;

            if (returnValue.endsWith("/"))
                returnValue = returnValue.substring(0, returnValue.length() - 1);

        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * return all matching entries
     * section must be unique
     */
    public String[] getProperties(String section1, String key1, String[] defaultKey1) {
        String query = makeXpathQuery(section1, key1);

        String[] returnValue = new String[0];

        try {
            List list = getProperties(query);

            if (list.size() > 0) {
                returnValue = new String[list.size()];
                int i = 0;
                for (Iterator it = list.iterator(); it.hasNext(); i++) {
                    Element elem = (Element) it.next();
                    //if (elem.hasChildren()) {
                    if (elem.getChildren().isEmpty()) { // if it has no children, it is assumed it contains a text as value
                        returnValue[i] = elem.getText();
                    } else {
                        returnValue[i] = elem.getName();
                    }
                }
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }
        return returnValue;
    }

    /**
     * make your own xpath-query
     *
     * @return a list of all matching jdom-elements
     */
    public List getProperties(String xpathQuery1) {
        return XmlTools.xpathQuery(doc_, xpathQuery1);
    }

    // set a list of properties with the name key1
    // any other existing properties with the same location are deleted
    public void setProperties(String section1, String key1, List value1)
            throws Exception {

        Element baseElement = XmlTools.simpleXpath(doc_, section1, null, true);
        if (baseElement != null) {
            //baseElement.removeChildren();
            baseElement.getChildren().clear();

            for (Iterator it = value1.iterator(); it.hasNext();) {
                String s = (String) it.next();
                Element child = new Element(key1);
                child.setText(s);
                baseElement.addContent(child);
            }

            XmlTools.saveToFile(filename_, doc_);
        } else
            cat.severe("unable to attach new elements");
    }
}  // end class