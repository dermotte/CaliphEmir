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

import javax.swing.*;
import java.awt.*;

public class AgentSearchPanel extends JPanel {
    JTextField givenName, familyName;
    JCheckBox semantic, creator, metadataCreator;

    public AgentSearchPanel() {
        super(new BorderLayout());
//        this.setFont(Font.getFont("Utopia"));
        init();
    }

    private void init() {
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Agent"));
        JPanel labelPane = new JPanel(new GridLayout(0, 1));
        JPanel inputPane = new JPanel(new GridLayout(0, 1));
        JPanel cbPane = new JPanel(new GridLayout(0, 1));
        cbPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        givenName = new JTextField();
        familyName = new JTextField();
        creator = new JCheckBox("Image creator", true);
        metadataCreator = new JCheckBox("Metadata creator", true);
        semantic = new JCheckBox("Semantic object", true);

        labelPane.add(new JLabel("Given name: "));
        labelPane.add(new JLabel("Family name: "));

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//        cbPane.add(separator);
        JLabel titleLable = new JLabel("Interpret given values as:");
        titleLable.setFont(titleLable.getFont().deriveFont(Font.BOLD));
        cbPane.add(titleLable);
        cbPane.add(semantic);
        cbPane.add(creator);
        cbPane.add(metadataCreator);

//        JPanel tmpPane = new JPanel(new BorderLayout());
//        tmpPane.add(separator, BorderLayout.NORTH);
//        tmpPane.add(cbPane, BorderLayout.CENTER);

        inputPane.add(givenName);
        inputPane.add(familyName);

        JPanel componentPanel = new JPanel(new BorderLayout());
        componentPanel.add(labelPane, BorderLayout.WEST);
        componentPanel.add(inputPane, BorderLayout.CENTER);
        componentPanel.add(cbPane, BorderLayout.SOUTH);

        this.add(componentPanel, BorderLayout.NORTH);
    }

    public String getGivenName() {
        return givenName.getText();
    }

    public String getFamilyName() {
        return familyName.getText();
    }

    public boolean searchSemanticObject() {
        return semantic.isSelected();
    }

    public boolean searchImageCreator() {
        return creator.isSelected();
    }

    public boolean searchMetadataCreator() {
        return metadataCreator.isSelected();
    }

    public String getXPathStatement() {
        String s = null;
        if (givenName.getText().length() > 0 || familyName.getText().length() > 0) {
            if (semantic.isSelected() && creator.isSelected() && metadataCreator.isSelected()) {
                s = "/" + getPostfix();
            } else if (creator.isSelected() && metadataCreator.isSelected()) {
                s = "//Creator" + getPostfix();
            } else if (semantic.isSelected() && creator.isSelected()) {
                s = "//*[contains(name(.), 'Semantic') or contains(name(.), 'Creation') ]/*[contains(name(.), 'SemanticBase') or contains(name(.), 'Creator') ]" + getPostfix();
            } else if (semantic.isSelected() && metadataCreator.isSelected()) {
                s = "//*[contains(name(.), 'Semantic') or contains(name(.), 'DescriptionMetadata') ]/*[contains(name(.), 'SemanticBase') or contains(name(.), 'Creator') ]" + getPostfix();
            } else if (metadataCreator.isSelected()) {
                s = "//DescriptionMetadata/Creator" + getPostfix();
            } else if (creator.isSelected()) {
                s = "//Creation/Creator" + getPostfix();
            } else if (semantic.isSelected()) {
                s = "//SemanticBase" + getPostfix();
            }
        }
        return s;
    }

    private String getPostfix() {
        String s = null;
        if (givenName.getText().length() > 0 && familyName.getText().length() > 0) {
            s = "/Agent/Name[contains(./FamilyName,'" + familyName.getText() +
                    "') and contains(./GivenName,'" + givenName.getText() + "')]";
        } else if (givenName.getText().length() > 0) {
            s = "/Agent/Name[contains(./GivenName,'" + givenName.getText() + "')]";
        } else if (familyName.getText().length() > 0) {
            s = "/Agent/Name[contains(./FamilyName,'" + familyName.getText() + "')]";
        } else {
            s = null;
        }
        return s;
    }
}
