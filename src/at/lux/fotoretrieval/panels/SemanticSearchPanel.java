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
package at.lux.fotoretrieval.panels;

import at.lux.fotoretrieval.RetrievalFrame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * XPathSearchPanel
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SemanticSearchPanel extends JPanel implements ActionListener {
    private JTextField object1, object2, object3, relation1, relation2, path;
    private JButton search, browse;
    private JPanel buttonPanel, semanticsPane, dp;
    private RetrievalFrame parent;
    private JCheckBox inverse1, inverse2;
    private VisSemanticSearchPanel visSearch;

    public SemanticSearchPanel(RetrievalFrame parent) {
        setLayout(new BorderLayout());
        this.parent = parent;
        dp = new JPanel(new BorderLayout());
        path = new JTextField(RetrievalFrame.BASE_DIRECTORY);
        path.setEnabled(false);
        dp.add(path, BorderLayout.CENTER);
        browse = new JButton("Directory ...");
        browse.setActionCommand("selectDirectory");
        browse.addActionListener(this);
        dp.add(browse, BorderLayout.EAST);
        dp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select directory"));

        JPanel labelPane = new JPanel(new GridLayout(0, 1));
        JPanel inputPane = new JPanel(new GridLayout(0, 1));
        JPanel inversePane = new JPanel(new GridLayout(0, 1));
        object1 = new JTextField();
        object2 = new JTextField();
        object3 = new JTextField();
        relation1 = new JTextField();
        relation2 = new JTextField();
        object1.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                updateGraph();
            }
        });
        object2.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                updateGraph();
            }
        });
        object3.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                updateGraph();
            }
        });
        relation1.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                updateGraph();
            }
        });
        relation2.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                updateGraph();
            }
        });
        inverse1 = new JCheckBox("inverse");
        inverse1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateGraph();
            }
        });
        inverse2 = new JCheckBox("inverse");
        inverse2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateGraph();
            }
        });
        labelPane.add(new JLabel("Object I:"));
        inputPane.add(object1);
        inversePane.add(new JLabel());
        labelPane.add(new JLabel("Relation I:"));
        inputPane.add(relation1);
        inversePane.add(inverse1);
        labelPane.add(new JLabel("Object II:"));
        inputPane.add(object2);
        inversePane.add(new JLabel());
        labelPane.add(new JLabel("Relation II:"));
        inputPane.add(relation2);
        inversePane.add(inverse2);
        labelPane.add(new JLabel("Object III:"));
        inputPane.add(object3);
        inversePane.add(new JLabel());

        buttonPanel = new JPanel(new FlowLayout());
        search = new JButton("Search");
        search.addActionListener(this);
        search.setActionCommand("search");
        buttonPanel.add(search);

        semanticsPane = new JPanel(new BorderLayout());
        semanticsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Define Semantics:"));
        semanticsPane.add(labelPane, BorderLayout.WEST);
        semanticsPane.add(inputPane, BorderLayout.CENTER);
        semanticsPane.add(inversePane, BorderLayout.EAST);
        semanticsPane.add(dp, BorderLayout.SOUTH);
        this.add(semanticsPane, BorderLayout.NORTH);
        visSearch = new VisSemanticSearchPanel();
        this.add(visSearch, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

    }

    private void selectDirectory() {
        JFileChooser jfc = new JFileChooser(".");
        jfc.setMultiSelectionEnabled(false);

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        jfc.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else
                    return false;
            }

            public String getDescription() {
                return "Directories";
            }
        });
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                path.setText(jfc.getSelectedFile().getCanonicalPath());
                RetrievalFrame.BASE_DIRECTORY = jfc.getSelectedFile().getCanonicalPath();
            } catch (IOException e) {
                System.err.println("Error reading directory: IOException - " + e.getMessage());
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("search")) {
            startSearch();
        } else if (e.getActionCommand().equals("selectDirectory")) {
            selectDirectory();
        } else {
            JOptionPane.showMessageDialog(this, "Not implemented yet!");
        }
    }

    private void startSearch() {

        // check if it is a valid search:
        // . case 1: o1 filled
        // case 2: o1 && r1 && o2 filled
        // case 3: o1 && r1 && o2 && r2 && o3 filled
        // case 4: o1 && o2 filled
        // case 5: o1 && o2 && o3 filled

        int o1, o2, o3, r1, r2;

        o1 = object1.getText().length();
        o2 = object2.getText().length();
        o3 = object3.getText().length();
        r1 = relation1.getText().length();
        r2 = relation2.getText().length();
        boolean valid = false;

        // case 1
        if (o1 >= 1 && o2 < 1 && o3 < 1 && r1 < 1 && r2 < 1)
            valid = true;
        // case 4
        else if (o1 >= 1 && o2 >= 1 && o3 < 1 && r1 < 1 && r2 < 1)
            valid = true;
        // case 2
        else if (o1 >= 1 && o2 >= 1 && o3 < 1 && r1 >= 1 && r2 < 1)
            valid = true;
        // case 5
        else if (o1 >= 1 && o2 >= 1 && o3 >= 1 && r1 < 1 && r2 < 1)
            valid = true;
        // case 3
        else if (o1 >= 1 && o2 >= 1 && o3 >= 1 && r1 >= 1 && r2 >= 1)
            valid = true;

        if (!valid) {
            JOptionPane.showMessageDialog(this, "This combination of objects and relations is\nnot a supported search request!", "Not implemented yet!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String countRel = "";
        String countObj = "";
        String searchObj1 = "";
        String searchObj2 = "";
        String searchObj3 = "";
        String searchRel1 = "";
        String searchRel2 = "";

        // Relations: //Semantic[./Graph/Relation[@type='urn:mpeg:mpeg7:cs:SemanticRelationCS:2001:patientOf'] and count(./Graph/Relation) >= 2]
        // Objects: //Semantic[./SemanticBase[contains(descendant-or-self::node(), 'Tob')] and count(./SemanticBase) >= 2]

        // count Relations:
        if (relation1.getText().length() > 0 && relation2.getText().length() > 0) {
            countRel = "count(./Graph/Relation) > 1";
        }

        // count Objects
        if (object1.getText().length() > 0 && object2.getText().length() > 0 && object3.getText().length() > 0) {
            countObj = "count(./SemanticBase) > 2";
        } else if (object1.getText().length() > 0 && object2.getText().length() > 0) {
            countObj = "count(./SemanticBase) > 1";
        }

        // search Relations:
        if (relation1.getText().length() > 0 && relation1.getText().indexOf('*') < 0) {
            searchRel1 = "./Graph/Relation[@type='urn:mpeg:mpeg7:cs:SemanticRelationCS:2001:" + relation1.getText() + "']";
        }
        if (relation2.getText().length() > 0 && relation2.getText().indexOf('*') < 0) {
            searchRel2 = "./Graph/Relation[@type='urn:mpeg:mpeg7:cs:SemanticRelationCS:2001:" + relation2.getText() + "']";
        }

        // search Objects:
        if (object1.getText().length() > 0 && object1.getText().indexOf('*') < 0) {
            searchObj1 = "./SemanticBase[contains(descendant-or-self::node(), '" + object1.getText() + "')]";
        }
        if (object2.getText().length() > 0 && object2.getText().indexOf('*') < 0) {
            searchObj2 = "./SemanticBase[contains(descendant-or-self::node(), '" + object2.getText() + "')]";
        }
        if (object3.getText().length() > 0 && object3.getText().indexOf('*') < 0) {
            searchObj3 = "./SemanticBase[contains(descendant-or-self::node(), '" + object3.getText() + "')]";
        }

        // build XPath Statement:
        StringBuffer expr = new StringBuffer("//Semantic[");
        boolean addAnd = false;
        if (countRel.length() > 0) {
            if (addAnd) expr.append(" and ");
            expr.append(countRel);
            addAnd = true;
        }
        if (countObj.length() > 0) {
            if (addAnd) expr.append(" and ");
            expr.append(countObj);
            addAnd = true;
        }
        if (searchRel1.length() > 0) {
            if (addAnd) expr.append(" and ");
            expr.append(searchRel1);
            addAnd = true;
        }
        if (searchRel2.length() > 0) {
            if (addAnd) expr.append(" and ");
            expr.append(searchRel2);
            addAnd = true;
        }
        if (searchObj1.length() > 0) {
            if (addAnd) expr.append(" and ");
            expr.append(searchObj1);
            addAnd = true;
        }
        if (searchObj2.length() > 0) {
            if (addAnd) expr.append(" and ");
            expr.append(searchObj2);
            addAnd = true;
        }
        if (searchObj3.length() > 0) {
            if (addAnd) expr.append(" and ");
            expr.append(searchObj3);
            addAnd = true;
        }
        expr.append("]");

        // old version:
        Vector objects = new Vector();
        objects.add(object1.getText());
        objects.add(object2.getText());
        objects.add(object3.getText());
        objects.add(relation1.getText());
        objects.add(relation2.getText());
        if (inverse1.isSelected())
            objects.add("inverse1");
        else
            objects.add("");
        if (inverse2.isSelected())
            objects.add("inverse2");
        else
            objects.add("");


        parent.searchForImage(expr.toString(), objects, path.getText(), true);
    }

    private void updateGraph() {
        visSearch.changeRelation(inverse1.isSelected(), inverse2.isSelected());
        String lo1, lo2, lo3, lr1, lr2;
        lo1 = object1.getText();
        lo2 = object2.getText();
        lo3 = object3.getText();
        lr1 = relation1.getText();
        lr2 = relation2.getText();

        if (lo1.length() == 0) lo1 = "?";
        if (lo2.length() == 0) lo2 = "?";
        if (lo3.length() == 0) lo3 = "?";
        if (lr1.length() == 0) lr1 = "?";
        if (lr2.length() == 0) lr2 = "?";
        visSearch.setLabels(lo1, lo2, lo3, lr1, lr2);
        visSearch.repaint();
    }

}
