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

package at.knowcenter.caliph.objectcatalog.semanticscreator;

import org.jdom.Element;
import org.jdom.Namespace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * This file is part of Caliph & Emir.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class NewObject extends JDialog implements ActionListener {
    Element node;
    private JPanel buttonPanel, gnp, fnp, cp, fp, tmpPanel;
    private JComboBox types;
    private JTextField gnf;
    private JTextArea fta;
    private JButton ok, cancel;
    private BeeDataExchange bde;

    public NewObject(BeeDataExchange bde) throws HeadlessException {
        this.bde = bde;
        this.setTitle("Create a new object");
        init();
    }

    private void init() {
        // this.setSize(640, 480);
        node = null;

        String[] availabletypes = {"SemanticPlaceType", "SemanticTimeType", "ObjectType"};
        types = new JComboBox(availabletypes);

        cp = new JPanel(new BorderLayout());
        cp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Type"));
        cp.add(new JLabel("Type: "), BorderLayout.WEST);
        cp.add(types, BorderLayout.CENTER);

        gnf = new JTextField(20);
        gnp = new JPanel(new BorderLayout());
        gnp.add(new JLabel("Name: "), BorderLayout.WEST);
        gnp.add(gnf, BorderLayout.CENTER);


        ok = new JButton("Ok");
        cancel = new JButton("Cancel");
        ok.addActionListener(this);
        cancel.addActionListener(this);
        ok.setActionCommand("ok");
        cancel.setActionCommand("cancel");

        fta = new JTextArea(10, 20);
        fp = new JPanel(new BorderLayout());
        fp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Description"));
        fp.add(new JScrollPane(fta), BorderLayout.CENTER);
        buttonPanel = new JPanel();
        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        JPanel tempgnp = new JPanel(new BorderLayout());
        tempgnp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Name"));
        tempgnp.add(gnp, BorderLayout.CENTER);
        // tmpPanel.add(fnp, BorderLayout.CENTER);


        tmpPanel = new JPanel(new BorderLayout());
        tmpPanel.add(cp, BorderLayout.NORTH);
        tmpPanel.add(tempgnp, BorderLayout.CENTER);

        this.getContentPane().add(tmpPanel, BorderLayout.NORTH);
        this.getContentPane().add(fp, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generateDocument() {
        Namespace mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        node = new Element("SemanticBase", mpeg7);
        node.setAttribute("type", types.getSelectedItem().toString(), xsi);

        Element label = new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(gnf.getText()));
        node.addContent(label);

        Element definition = new Element("Definition", mpeg7);
        Element ta = new Element("FreeTextAnnotation", mpeg7);
        ta.addContent(fta.getText());
        definition.addContent(ta);

        node.addContent(definition);
    }

    public Element getNode() {
        return node;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("cancel")) {
            setVisible(false);
        } else if (e.getActionCommand().equals("ok")) {
            generateDocument();
            Vector v = new Vector();
            v.add(node);
            bde.addVenues(v);
            setVisible(false);
        }
    }
}
