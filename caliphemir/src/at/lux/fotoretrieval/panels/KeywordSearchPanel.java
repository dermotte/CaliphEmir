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
import java.util.StringTokenizer;

public class KeywordSearchPanel extends JPanel {
    private JTextField keywords;
    private final String[] modes = {"At least one (OR)", "All words (AND)", "Whole phrase"};
    private JComboBox mode = new JComboBox(modes);
    JRadioButton freeText, metadataDesc, everywhere, semanticLabels, semanticDescriptions, textAnnotation;

    public KeywordSearchPanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Keywords"));
        JPanel labelPane = new JPanel(new GridLayout(0, 1));
        JPanel inputPane = new JPanel(new GridLayout(0, 1));
        JPanel cbPane = new JPanel(new GridLayout(0, 1));
        cbPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        keywords = new JTextField();
        freeText = new JRadioButton("Image description", true);
        metadataDesc = new JRadioButton("Metadata description", false);
        textAnnotation = new JRadioButton("Free text annotations", false);
        everywhere = new JRadioButton("Everywhere", false);
        semanticLabels = new JRadioButton("Semantic object names", false);
        semanticDescriptions = new JRadioButton("Semantic object descriptions", false);
        ButtonGroup b_group = new ButtonGroup();
        b_group.add(freeText);
        b_group.add(metadataDesc);
        b_group.add(textAnnotation);
        b_group.add(everywhere);
        b_group.add(semanticDescriptions);
        b_group.add(semanticLabels);

        JLabel titleLabel = new JLabel("Search for keywords in:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        cbPane.add(titleLabel);
        cbPane.add(freeText);
        cbPane.add(textAnnotation);
        cbPane.add(metadataDesc);
        cbPane.add(semanticLabels);
        cbPane.add(semanticDescriptions);
        cbPane.add(everywhere);

        labelPane.add(new JLabel("Keywords: "));
        labelPane.add(new JLabel("Mode: "));

        inputPane.add(keywords);
        inputPane.add(mode);

        JPanel componentPanel = new JPanel(new BorderLayout());
        componentPanel.add(labelPane, BorderLayout.WEST);
        componentPanel.add(inputPane, BorderLayout.CENTER);
        componentPanel.add(cbPane, BorderLayout.SOUTH);
        this.add(componentPanel, BorderLayout.NORTH);
    }

    public String getKeywords() {
        return keywords.getText();
    }

    public int getMode() {
        return mode.getSelectedIndex();
    }

    public String getXPathStatement() {
        String buffer = null;
        if (keywords.getText().length() > 0) {
            buffer = new String();
            if (everywhere.isSelected()) {
                buffer = ("//*" + createPostfix());
            } else if (textAnnotation.isSelected()) {
                buffer = ("//TextAnnotation/FreeTextAnnotation" + createPostfix());
            } else if (semanticDescriptions.isSelected()) {
                buffer = ("//Definition/FreeTextAnnotation" + createPostfix());
            } else if (semanticLabels.isSelected()) {
                buffer = ("//Definition/FreeTextAnnotation" + createPostfix());
            } else if (freeText.isSelected()) {
                buffer = ("//FreeTextAnnotation" + createPostfix());
            } else if (metadataDesc.isSelected()) {
                buffer = ("//Comment/FreeTextAnnotation" + createPostfix());
            }
        }
        return buffer;
    }

    private String createPostfix() {
        String phrase = "[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),translate('" + keywords.getText() + "', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))]";
        if (mode.getSelectedIndex() == 2) { // PHRASE
            return phrase;
        } else if (mode.getSelectedIndex() == 1) {  // AND
            StringTokenizer st = new StringTokenizer(keywords.getText(), " ", false);
            String retVal = "[";
            while (st.hasMoreElements()) {
                String s = st.nextToken();
                retVal = retVal + "contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') ,translate('" + s + "', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))";
                if (st.hasMoreElements()) retVal = retVal + " and ";
            }
            retVal = retVal + "]";
            return retVal;
        } else {  // OR
            StringTokenizer st = new StringTokenizer(keywords.getText(), " ", false);
            String retVal = "[";
            while (st.hasMoreElements()) {
                String s = st.nextToken();
                retVal = retVal + "contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') ,translate('" + s + "', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))";
                if (st.hasMoreElements()) retVal = retVal + " or ";
            }
            retVal = retVal + "]";
            return retVal;
        }
    }

}
