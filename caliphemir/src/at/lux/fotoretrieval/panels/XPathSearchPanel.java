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
 * XPathSearchPanel
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class XPathSearchPanel extends JPanel implements ActionListener {
    private JTextField xpath, path;
    private JButton search, browse;
    private JPanel buttonPanel, xpanel, dp;
    private RetrievalFrame parent;

    public XPathSearchPanel(RetrievalFrame parent) {
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

        xpanel = new JPanel(new BorderLayout());
        xpath = new JTextField();
        xpanel.add(new JLabel("Input XPath: "), BorderLayout.WEST);
        xpanel.add(xpath, BorderLayout.CENTER);
        xpanel.add(dp, BorderLayout.SOUTH);
        xpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonPanel = new JPanel(new FlowLayout());
        search = new JButton("Search");
        search.addActionListener(this);
        search.setActionCommand("search");
        buttonPanel.add(search);

        this.add(xpanel, BorderLayout.NORTH);
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
        if (xpath.getText().length() > 0) {
            parent.searchForImage(xpath.getText(), path.getText(), true);
        }
    }

    public String getPath() {
        return path.getText().trim();
    }

}
