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
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * TextInputSearchPanel
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TextInputSearchPanel extends JPanel {
    private AgentSearchPanel agentSearchPanel;
    private KeywordSearchPanel keywordSearchPanel;
    private JPanel searchButtonPanel;
    private QualityConstraintPanel qualityPanel;
    private RetrievalFrame parent;
    private JTextField path;
    private JButton browse;


    public TextInputSearchPanel(RetrievalFrame parent) {
        this.parent = parent;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        JButton search = new JButton("Search");
        search.setActionCommand("search");
        search.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startSearch();
            }
        });
        searchButtonPanel = new JPanel();
        searchButtonPanel.add(search);

        JPanel dp = new JPanel(new BorderLayout());
//        path = new JTextField(RetrievalFrame.BASE_DIRECTORY);
        path = new JTextField(".");
        path.setEnabled(false);
        dp.add(path, BorderLayout.CENTER);
        browse = new JButton("Directory ...");
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectDirectory();
            }
        });
        dp.add(browse, BorderLayout.EAST);
        dp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select directory"));


        agentSearchPanel = new AgentSearchPanel();
        keywordSearchPanel = new KeywordSearchPanel();
        qualityPanel = new QualityConstraintPanel();
        JPanel westPanel = new JPanel(new GridLayout(0, 1));
        westPanel.add(agentSearchPanel);
        westPanel.add(qualityPanel);
        JPanel searchParameterPanel = new JPanel(new GridLayout(0, 2));
        searchParameterPanel.add(westPanel);

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(keywordSearchPanel, BorderLayout.CENTER);
        eastPanel.add(dp, BorderLayout.SOUTH);

        searchParameterPanel.add(eastPanel);
        JPanel searchPanel = new JPanel(new BorderLayout());
        this.add(searchParameterPanel, BorderLayout.CENTER);
        this.add(searchButtonPanel, BorderLayout.SOUTH);

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


    private void startSearch() {
        if (agentSearchPanel.getXPathStatement() != null && !(keywordSearchPanel.getXPathStatement() != null)) {
            parent.searchForImage(agentSearchPanel.getXPathStatement(), path.getText(), true);
        } else if (!(agentSearchPanel.getXPathStatement() != null) && (keywordSearchPanel.getXPathStatement() != null)) {
            parent.searchForImage(keywordSearchPanel.getXPathStatement(), path.getText(), true);
        } else {
            JOptionPane.showMessageDialog(this, "Only one at a time is allowed:\n remove text from agents or keywords to continue!");
        }
    }

    public QualityConstraintPanel getQualityConstraints() {
        return qualityPanel;
    }
}
