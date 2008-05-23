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

import at.lux.fotoannotation.AnnotationFrameProperties;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Namespace;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class RetrievalToolkit {
    public static String PROGRAM_NAME = "Emir";
    public static String PROGRAM_VERSION = "v 0.9b";

    public static List xpathQuery(org.jdom.Element document, String xpathQuery1, Namespace xNs) {
        List returnValue = new Vector();
        String xpathQuery = xpathQuery1;
        XPath xPath;
        try {
            Namespace ns = document.getNamespace();
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
            Object jdomNode = document;
            returnValue = xPath.selectNodes(jdomNode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    public static JMenuBar createRetrievalMenuBar(ActionListener al) {
        JMenu file, help, view, index, vis;
        file = new JMenu("File");
        help = new JMenu("Help");
        view = new JMenu("View");
//        index = new JMenu("Index");
        vis = new JMenu("Repository visualization");
        file.setMnemonic(KeyEvent.VK_F);
        help.setMnemonic(KeyEvent.VK_H);
        view.setMnemonic(KeyEvent.VK_V);
//        index.setMnemonic(KeyEvent.VK_I);
        vis.setMnemonic(KeyEvent.VK_V);

        file.add(createMenuItem("Create index", "wizardIndex", "ctrl W", al, KeyEvent.VK_W, "data/db.png"));
        file.add(createMenuItem("Configuration ...", "showConfig", "alt C", al, KeyEvent.VK_C, "data/config.png"));
        file.addSeparator();
        file.add(createMenuItem("Exit", "exit", "alt F4", al, KeyEvent.VK_E, "data/delete_obj.gif"));
        view.add(createMenuItem("Close active Tab", "closeTab", "alt X", al, KeyEvent.VK_C, "data/error_tsk.gif"));
        view.addSeparator();
        view.add(createMenuItem("Keyword Tab", "k-tab", "alt F5", al, KeyEvent.VK_K, "data/gotoend.png"));
        view.add(createMenuItem("Semantics Tab", "s-tab", "alt F6", al, KeyEvent.VK_S, "data/polygon_unfilled.png"));
        view.add(createMenuItem("XPath Tab", "x-tab", "alt F7", al, KeyEvent.VK_X, "data/framenumberdate.png"));

        vis.add(createMenuItem("ColorLayout", "viCl", "ctrl 1", al, KeyEvent.VK_C, "data/beziereliminatepoints.png"));
        vis.add(createMenuItem("ScalableColor", "viSc", "ctrl 2", al, KeyEvent.VK_S, "data/bezierconvert.png"));
        vis.add(createMenuItem("EdgeHistogram", "viEh", "ctrl 3", al, KeyEvent.VK_E, "data/bezierappend.png"));
        vis.addSeparator();
        vis.add(createMenuItem("Semantic graphs", "viSg", "ctrl 4", al, KeyEvent.VK_G, "data/emblem-cool.png"));

//        index.add(createMenuItem("Indexing wizard", "wizardIndex", "ctrl W", al, KeyEvent.VK_W, "data/db.png"));
//        index.addSeparator();
//        index.add(new JCheckBoxMenuItem("Use Derby DB for CBIR", false));
//        index.addSeparator();
//        index.add(createMenuItem("Create/Update index", "createIndex", "ctrl I", al, KeyEvent.VK_C, "data/refresh_nav.gif"));
//        index.add(createMenuItem("Change data repository location", "indexPath", "ctrl O", al, KeyEvent.VK_D, "data/redo_edit.gif"));
//        index.add(createMenuItem("Show current data repository location", "showIndexPath", "ctrl V", al, KeyEvent.VK_S, "data/db.png"));

//        help.add(createMenuItem("Local help", "help", "F1", al, KeyEvent.VK_H));
        help.add(createMenuItem("Online help", "showHelpOnline", "F1", al, KeyEvent.VK_O, "data/linkto_help.png"));
        help.addSeparator();
        help.add(createMenuItem("About", "about", "alt A", al, KeyEvent.VK_A, "data/showwarn_tsk.gif"));
        help.add(createMenuItem("Visit homepage", "visitHomepage", "alt H", al, KeyEvent.VK_V, "data/redo_edit.gif"));

        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(view);
        bar.add(vis);
//        bar.add(index);
        bar.add(help);

        return bar;
    }

    public static JMenuItem createMenuItem(String label, String command, String Mnemonic, ActionListener al, int mnem) {
        JMenuItem i = new JMenuItem(label);
        if (Mnemonic != null) {
            i.setAccelerator(KeyStroke.getKeyStroke(Mnemonic));
        }
        i.setMnemonic(mnem);
        i.setActionCommand(command);
        i.addActionListener(al);
        return i;
    }

    public static JMenuItem createMenuItem(String label, String command, String Mnemonic, ActionListener al, int mnem, String iconImageFilePath) {
        JMenuItem i = new JMenuItem(label);
        if (Mnemonic != null) {
            i.setAccelerator(KeyStroke.getKeyStroke(Mnemonic));
        }
        i.setMnemonic(mnem);
        i.setActionCommand(command);
        i.addActionListener(al);
        i.setIcon(new ImageIcon(AnnotationFrameProperties.class.getResource(iconImageFilePath)));
        return i;
    }

    public static String[] getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<String> v = new ArrayList<String>();
        File[] f = directory.listFiles();
        for (int i = 0; i < f.length; i++) {
            File file = f[i];
            String fnameLow = file.getName().toLowerCase();
            if (file != null && (fnameLow.endsWith(".jpg") || fnameLow.endsWith(".ppm") || fnameLow.endsWith(".png"))
                    && !file.getName().startsWith("tn_")) {
                v.add(file.getCanonicalPath());
            }

            if (descendIntoSubDirectories && file.isDirectory()) {
                String[] tmp = getAllImages(file, true);
                if (tmp != null) {
                    for (int j = 0; j < tmp.length; j++) {
                        v.add(tmp[j]);
                    }
                }
            }
        }
        if (v.size() > 0)
            return v.toArray(new String[1]);
        else
            return null;
    }


}
