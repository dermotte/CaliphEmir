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

import at.lux.fotoannotation.AgentComboBoxModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * DeleteAgentDialog
 * , motte@sbox.tu-graz.ac.at
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class DeleteAgentDialog extends JDialog implements ActionListener {
    private JButton cancel, ok;
    private JList list;
    private AgentComboBoxModel model;

    public DeleteAgentDialog(JFrame owner) {
        super(owner, "Delete Agent", true);
        model = new AgentComboBoxModel(owner);
        init();
    }

    private void init() {
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel");
        ok = new JButton("OK");
        ok.addActionListener(this);
        ok.setActionCommand("ok");

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(cancel);
        buttonsPanel.add(ok);

        list = new JList(model);

        this.getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        this.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            if (list.getSelectedIndex() > -1) {
                model.deleteAgent(list.getSelectedValue().toString());
                this.setVisible(false);
            }
        } else if (e.getActionCommand().equals("cancel")) {
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "Not implemented!");
        }
    }
}

;
