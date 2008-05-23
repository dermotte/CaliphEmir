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

package at.lux.fotoretrieval;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

/**
 * Description
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class RetrievalFrameProperties {
    private int frameWidth = 600, frameHeigth = 800, frameLocationX = 0, frameLocationY = 0;
    private int lrSplit = 220, tbSplit = 550;
    private int textViewWidth = 640, textViewHeight = 480;
    private String lastDir = ".",

    externalViewer = null;
    private JMenuBar menuBar = null;
    private Document propertiesDocument;
    private ActionListener listener;
    private Element propertiesRoot;
    private File propertyFile = null;

    public RetrievalFrameProperties(File propertyFile, ActionListener listener) {
        this.listener = listener;
        SAXBuilder builder = new SAXBuilder();
        try {
            propertiesDocument = builder.build(propertyFile);
            propertiesRoot = propertiesDocument.getRootElement();
            this.propertyFile = propertyFile;
            initProperties();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (JaxenException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initProperties() throws JaxenException {
        long mtime = System.currentTimeMillis();
        XPath path;
        List results;

        path = new JDOMXPath("properties/frame/size/width");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting frameWidth to " + e.getTextTrim());
            frameWidth = Integer.parseInt(e.getTextTrim());
        }

        path = new JDOMXPath("properties/frame/size/height");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting frameHeight to " + e.getTextTrim());
            frameHeigth = Integer.parseInt(e.getTextTrim());
        }

        path = new JDOMXPath("properties/frame/location/x");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting frameLocationX to " + e.getTextTrim());
            frameLocationX = Integer.parseInt(e.getTextTrim());
        }

        path = new JDOMXPath("properties/frame/location/y");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting frameLocationY to " + e.getTextTrim());
            frameLocationY = Integer.parseInt(e.getTextTrim());
        }

        path = new JDOMXPath("properties/files/lastdirectory");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting lastDirectory to " + e.getTextTrim());
            lastDir = e.getTextTrim();
        }

        path = new JDOMXPath("properties/files/externalviewer");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting externalViewer to " + e.getTextTrim());
            externalViewer = e.getTextTrim();
            if (externalViewer.length() < 2)
                externalViewer = null;
        }

        path = new JDOMXPath("properties/frame/split/lr");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting lrSplit divider location to " + e.getTextTrim());
            lrSplit = Integer.parseInt(e.getTextTrim());
        }

        path = new JDOMXPath("properties/frame/split/tb");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting tbSplit divider location to " + e.getTextTrim());
            tbSplit = Integer.parseInt(e.getTextTrim());
        }

        path = new JDOMXPath("textpreview/size/width");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting textViewWidth divider location to " + e.getTextTrim());
            textViewWidth = Integer.parseInt(e.getTextTrim());
        }

        path = new JDOMXPath("textpreview/size/height");
        results = path.selectNodes(propertiesDocument);
        if (results.size() > 0) {
            Element e = (Element) results.get(0);
            debug("Setting textViewHeight divider location to " + e.getTextTrim());
            textViewHeight = Integer.parseInt(e.getTextTrim());
        }

        menuBar = readMenuFromDocument();

        mtime = System.currentTimeMillis() - mtime;
        debug("Reading config file took " + mtime + " ms");
    }

    private JMenuBar readMenuFromDocument() {
        JMenuBar menuBar = new JMenuBar();
        Element xMenu = propertiesRoot.getChild("menu");
        List subs = xMenu.getChildren("menu");
        for (Iterator i = subs.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            menuBar.add(createSubMenu(e));
        }
        return menuBar;
    }

    private JMenu createSubMenu(Element node) {
        JMenu menu = new JMenu(node.getAttributeValue("name"));
        List subs = node.getChildren();
        for (Iterator i = subs.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            if (e.getName().equals("separator"))
                menu.addSeparator();
            if (e.getName().equals("menuitem")) {
                JMenuItem item = new JMenuItem(e.getAttributeValue("name"));
                item.setActionCommand(e.getAttributeValue("command"));
                item.addActionListener(listener);
                if (e.getAttributeValue("key") != null)
                    item.setAccelerator(KeyStroke.getKeyStroke(e.getAttributeValue("key")));
                menu.add(item);
            }
            if (e.getName().equals(menu))
                menu.add(createSubMenu(e));
        }
        return menu;
    }

    public void saveConfiguration() {
        Element size = propertiesRoot.getChild("frame").getChild("size");
        size.getChild("width").setText("" + frameWidth);
        size.getChild("height").setText("" + frameHeigth);
        Element location = propertiesRoot.getChild("frame").getChild("location");
        location.getChild("x").setText(frameLocationX + "");
        location.getChild("y").setText(frameLocationY + "");
        Element split = propertiesRoot.getChild("frame").getChild("split");
        split.getChild("lr").setText(lrSplit + "");
        split.getChild("tb").setText(tbSplit + "");
        Element tpSize = propertiesRoot.getChild("textpreview").getChild("size");
        tpSize.getChild("width").setText(textViewWidth + "");
        tpSize.getChild("height").setText(textViewHeight + "");
        propertiesRoot.getChild("files").getChild("lastdirectory").setText(lastDir);
        propertiesRoot.getChild("files").getChild("externalviewer").setText(externalViewer);
        debug("saving configuration to file " +
                propertyFile.toString());
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            FileOutputStream fos = new FileOutputStream(propertyFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            outputter.output(propertiesRoot, osw);
            osw.close();
            fos.close();
            // outputter.output(propertiesRoot, System.out);
            // System.out.println();
            debug("finished saving configuration to file " +
                    propertyFile.toString());
        } catch (IOException e) {
            debug("IOException while saving file "
                    + propertyFile.toString() + ": " + e.toString());
        } //catch (UnsupportedEncodingException e) {
        //  debug("UnsupportedEncodingException (tried saving UTF-8) while saving file "
        //          + propertyFile.toString() + ": " + e.toString());
    }

    private void debug(String message) {
        if (RetrievalFrame.DEBUG) System.out.println("[at.lux.fotoannotation.AnnotationFrameProperties] " + message);
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeigth() {
        return frameHeigth;
    }

    public int getFrameLocationX() {
        return frameLocationX;
    }

    public int getFrameLocationY() {
        return frameLocationY;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeigth(int frameHeigth) {
        this.frameHeigth = frameHeigth;
    }

    public void setFrameLocationX(int frameLocationX) {
        this.frameLocationX = frameLocationX;
    }

    public void setFrameLocationY(int frameLocationY) {
        this.frameLocationY = frameLocationY;
    }

    public String getLastDir() {
        return lastDir;
    }

    public void setLastDir(String lastDir) {
        this.lastDir = lastDir;
    }

    public int getLrSplit() {
        return lrSplit;
    }

    public void setLrSplit(int lrSplit) {
        this.lrSplit = lrSplit;
    }

    public int getTbSplit() {
        return tbSplit;
    }

    public void setTbSplit(int tbSplit) {
        this.tbSplit = tbSplit;
    }

    public int getTextViewWidth() {
        return textViewWidth;
    }

    public void setTextViewWidth(int textViewWidth) {
        this.textViewWidth = textViewWidth;
    }

    public int getTextViewHeight() {
        return textViewHeight;
    }

    public void setTextViewHeight(int textViewHeight) {
        this.textViewHeight = textViewHeight;
    }

    public String getExternalViewer() {
        return externalViewer;
    }

    public void setExternalViewer(String externalViewer) {
        this.externalViewer = externalViewer;
    }

}
