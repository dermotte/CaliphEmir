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
import at.lux.fotoretrieval.retrievalengines.LuceneRetrievalEngine;
import at.lux.fotoretrieval.retrievalengines.RetrievalEngineFactory;
import at.lux.fotoannotation.panels.ComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * XPathSearchPanel
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GraphSearchPanel extends JPanel implements ActionListener {
    private static final boolean USE_PATH_INDEX = true;
    private RetrievalFrame parent;
    private JTextField newObjectLabel;
    private JButton addObject, browse;
    GraphConstructionPanel graphConstruction = new GraphConstructionPanel();
    private LuceneRetrievalEngine retrievalEngine = null;
    private int countAnonymousNodes = 0;
    public static final String ANONYMOUS_NODE_NAME = "anonymous node ";

    public GraphSearchPanel(RetrievalFrame parent) {
        setLayout(new BorderLayout());
        this.parent = parent;

        newObjectLabel = new JTextField();
        newObjectLabel.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    String text = newObjectLabel.getText();
                    if (text.length() > 2 || text.equals("*")) {
                        addNewNode(text);
                        newObjectLabel.setText("");
                    }
                }
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        addObject = new JButton("Create node");
        addObject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = newObjectLabel.getText();
                if (text.length() > 2 || text.equals("*")) {
                    addNewNode(text);
                    newObjectLabel.setText("");
                }
            }
        });
        JPanel graphPanel = new JPanel(new BorderLayout());
        JPanel graphButtonPanel = new JPanel(new BorderLayout());
        graphButtonPanel.add(newObjectLabel, BorderLayout.CENTER);
        graphButtonPanel.add(addObject, BorderLayout.EAST);
//        graphButtonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Create new nodes for the query graph:"));
        graphPanel.add(graphConstruction, BorderLayout.CENTER);
        graphPanel.add(ComponentFactory.createTitledPanel("Create new nodes for the query graph:", graphButtonPanel), BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton search = new JButton("Start search");
        search.setActionCommand("search");
        search.addActionListener(this);

        buttonPanel.add(search, BorderLayout.CENTER);

        add(graphPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addNewNode(String text) {
        if (!text.equals("*")) {
            if (retrievalEngine == null) {
                retrievalEngine = (LuceneRetrievalEngine) RetrievalEngineFactory.getLuceneRetrievalEngine();
            }
            int hits = retrievalEngine.getNodes(text, RetrievalFrame.BASE_DIRECTORY).size();
            graphConstruction.addNode(text + " (" + hits + ")");
        } else {
            graphConstruction.addNode(ANONYMOUS_NODE_NAME + ++countAnonymousNodes);
        }
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("search")) {
            if (USE_PATH_INDEX) {
                startPathSearch();
            } else {
                startSearch();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Not implemented yet!");
        }
    }

    private void startPathSearch() {
        String pathSearchString = graphConstruction.getPathSearchString();
        if (pathSearchString.length() > 0) {
            parent.searchForSemanticsInPathIndex(pathSearchString, RetrievalFrame.BASE_DIRECTORY, true);
        }
    }

    private void startSearch() {
        if (graphConstruction.getSearchString().length() > 0) {
            String searchString = graphConstruction.getSearchString();
//                System.out.println(searchString);
            parent.searchForSemanticsInIndex(searchString, RetrievalFrame.BASE_DIRECTORY, true);
        }
    }

}
