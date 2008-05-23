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
package at.lux.fotoannotation.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ExternalViewerSelectDialog extends JDialog {
    JTextField viewer;
    String oldViewer;

    public ExternalViewerSelectDialog(Frame owner, String currentViewer) {
        super(owner, true);
        oldViewer = currentViewer;

        setTitle("Choose external viewer");
        viewer = new JTextField(15);
        if (currentViewer != null)
            viewer.setText(currentViewer);

        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        JButton browse = new JButton("Browse ...");

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browse();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout());
        JPanel tmp = new JPanel(new FlowLayout());

        buttons.add(ok);
        buttons.add(cancel);

        tmp.add(new JLabel("External viewer: "));
        tmp.add(viewer);
        tmp.add(browse);

        this.getContentPane().add(tmp, BorderLayout.CENTER);
        this.getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void ok() {
        setVisible(false);
    }

    private void cancel() {
        if (oldViewer != null) {
            viewer.setText(oldViewer);
        } else {
            viewer.setText("");
        }
        setVisible(false);
    }

    private void browse() {
        JFileChooser fc = new JFileChooser(".");
        fc.setMultiSelectionEnabled(false);

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                viewer.setText(fc.getSelectedFile().getCanonicalPath());
            } catch (IOException e) {
            }
        }
    }

    public String getExternalViewer() {
        if (viewer.getText().length() > 1)
            return viewer.getText();
        else
            return null;
    }

}
