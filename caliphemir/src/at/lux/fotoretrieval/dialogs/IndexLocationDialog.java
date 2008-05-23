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
package at.lux.fotoretrieval.dialogs;

import at.lux.fotoretrieval.RetrievalFrame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Date: 12.01.2005
 * Time: 22:45:01
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class IndexLocationDialog extends JDialog implements ActionListener {
    private JTextField path;
    private JButton browse, okay, cancel;

    /**
     * Creates a non-modal dialog without a title with the
     * specified <code>Frame</code> as its owner.  If <code>owner</code>
     * is <code>null</code>, a shared, hidden frame will be set as the
     * owner of the dialog.
     * <p/>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @throws java.awt.HeadlessException if GraphicsEnvironment.isHeadless()
     *                                    returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public IndexLocationDialog(Frame owner) {
        super(owner, true);
        setTitle("Change current data repository location");
        JPanel pathPanel = new JPanel(new BorderLayout());
        browse = new JButton("browse");
        browse.setActionCommand("selectDirectory");
        browse.addActionListener(this);
        path = new JTextField(RetrievalFrame.BASE_DIRECTORY, 40);
        path.setEnabled(false);
        pathPanel.add(new JLabel("Current location: "), BorderLayout.NORTH);
        pathPanel.add(path, BorderLayout.CENTER);
        pathPanel.add(browse, BorderLayout.EAST);

        pathPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        cancel = new JButton("Cancel");
        okay = new JButton("OK");
        cancel.addActionListener(this);
        okay.addActionListener(this);
        cancel.setActionCommand("cancel");
        okay.setActionCommand("okay");
        buttonPanel.add(cancel);
        buttonPanel.add(okay);

        this.getContentPane().add(pathPanel, BorderLayout.NORTH);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("selectDirectory")) {
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
//                    RetrievalFrame.BASE_DIRECTORY = jfc.getSelectedFile().getCanonicalPath();
                } catch (IOException ex) {
                    System.err.println("Error reading directory: IOException - " + ex.getMessage());
                }
            }
        } else if (command.equals("okay")) {
            RetrievalFrame.BASE_DIRECTORY = path.getText();
            this.setVisible(false);
        } else if (command.equals("cancel")) {
            this.setVisible(false);
        }
    }
}
