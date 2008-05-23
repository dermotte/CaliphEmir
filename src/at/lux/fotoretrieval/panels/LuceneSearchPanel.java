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
import at.lux.fotoretrieval.dialogs.LuceneHelpDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * XPathSearchPanel
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LuceneSearchPanel extends JPanel implements ActionListener {

    private JTextField xpath;
    private JTextArea pleaseNote;
    private JButton search;
    private JPanel buttonPanel, xpanel;
    private RetrievalFrame parent;
//    private JButton searchSemantics;
    private JButton helpButton;

    public LuceneSearchPanel(RetrievalFrame parent) {
        setLayout(new BorderLayout());
        this.parent = parent;

        search = new JButton("Search");
        search.addActionListener(this);
        search.setActionCommand("search");

        pleaseNote = new JTextArea("Please note: " +
            "Before searching an index has to be created. " +
            "Use the menu \"File -> Create index\" to create the index or the button " +
            "below this text.");

        pleaseNote.setEditable(false);
        pleaseNote.setRows(2);
        pleaseNote.setLineWrap(true);
        pleaseNote.setWrapStyleWord(true);
        pleaseNote.setFont(search.getFont());

        pleaseNote.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 1), BorderFactory.createEmptyBorder(5,5,5,5)));

        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(5);
        borderLayout.setVgap(5);
        xpanel = new JPanel(borderLayout);
        xpath = new JTextField();
        xpanel.add(new JLabel("Query: "), BorderLayout.WEST);
        xpanel.add(xpath, BorderLayout.CENTER);
        xpanel.add(search, BorderLayout.EAST);
        xpanel.add(pleaseNote, BorderLayout.SOUTH);
        xpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonPanel = new JPanel(new FlowLayout());

        // adding option to search

        helpButton = new JButton("Help");
        helpButton.setActionCommand("help");
        helpButton.addActionListener(this);

//        searchSemantics = new JButton("Semantic Search");
//        searchSemantics.setActionCommand("ssearch");
//        searchSemantics.addActionListener(this);

        buttonPanel.add(helpButton);
//        buttonPanel.add(searchSemantics);

        // Adding a keylistener for enter - embedGraph search on enter
        xpath.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                    startSearch();
                }
            }
        });

        GeneralActionsPanel actionsPanel = new GeneralActionsPanel();
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        actionsPanel.visitHomepageButton.addActionListener(parent);
        actionsPanel.createIndexButton.addActionListener(parent);
        actionsPanel.showHelpButton.addActionListener(parent);
        actionsPanel.showAboutButton.addActionListener(parent);

        // Parameters for search:
        JPanel above = new JPanel(new BorderLayout());
        this.add(xpanel, BorderLayout.NORTH);
        this.add(actionsPanel, BorderLayout.CENTER);
//        this.add(buttonPanel, BorderLayout.SOUTH);

        // Help for search:
//        JEditorPane helpText = new JEditorPane("text/html", HTML_HELP);
//        helpText.setEditable(false);
//
//        JScrollPane scrollTextHelp = new JScrollPane(helpText);
//        scrollTextHelp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Help"));

//        this.add(above, BorderLayout.NORTH);
//        this.add(scrollTextHelp, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("search")) {
            String text = xpath.getText();
            if (text.length() > 2)
                startSearch();
            else
                JOptionPane.showMessageDialog(this, "Your search string is not long enough, minimum is 3 letters!");

        } else if (e.getActionCommand().equals("ssearch")) {
            String text = xpath.getText();
            if (text.indexOf('[') > -1 && text.indexOf(']') > -1 && text.length() > 4)
                startSemanticSearch();
            else
                JOptionPane.showMessageDialog(this, "This is no valid semantic search!");
        } else if (e.getActionCommand().equals("help")) {
            LuceneHelpDialog diag = new LuceneHelpDialog(parent);
            diag.setVisible(true);
            diag.scrollToZero();
        } else {
            JOptionPane.showMessageDialog(this, "Not implemented yet!");
        }
    }

    private void startSemanticSearch() {
        if (xpath.getText().length() > 0) {
            parent.searchForSemanticsInIndex(xpath.getText(), RetrievalFrame.BASE_DIRECTORY, true);
        }
    }

    private void startSearch() {
        if (xpath.getText().length() > 0) {
            parent.searchForImageInIndex(xpath.getText(), RetrievalFrame.BASE_DIRECTORY, true);
        }
    }

}
