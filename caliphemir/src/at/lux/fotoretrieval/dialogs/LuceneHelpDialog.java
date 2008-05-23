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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: mlux
 * Date: 28.10.2004
 * Time: 15:22:57
 */
public class LuceneHelpDialog extends JDialog {
    JButton close;
    private static String HTML_HELP = "<html>\n" +
            "<body>\n" +
            "<h2>Search parameters</h2>\n" +
            "<p>Lucene offers various query options like boolean, fuzzy or proximity searches. A proximity search would be defined like this: &quot;jakarta apache&quot;~10. This example would retrieve all documents where the term jakarta can be found in the range of maximum 10 words of the term apache. </p><br>\n" +
            "<table width=\"100%\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\">\n" +
            "  <tr>\n" +
            "    <td>Single Character Wildcard </td>\n" +
            "    <td>?</td>\n" +
            "    <td>&quot;Te?t&quot; will retrieve &quot;Test&quot;, &quot;Text&quot; etc. </td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td> Multiple Character Wildcard</td>\n" +
            "    <td>*</td>\n" +
            "    <td>&quot;Te*t&quot; will retrieve &quot;Test&quot;, &quot;Text&quot;, &quot;Tet&quot;, &quot;Tetertertet&quot; etc. </td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td>Fuzzy Match </td>\n" +
            "    <td>~</td>\n" +
            "    <td>Is based on editing distance: &quot;roam~&quot; will retrieve e.g. foam, roams, etc </td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td>Boost Factor </td>\n" +
            "    <td>^</td>\n" +
            "    <td>With &quot;Jakarta^4 Lucene&quot; the term Jakarta gets 4 times more weight than Lucene (default boost factor is 1) </td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td>Boolean Operators </td>\n" +
            "    <td>AND, OR, NOT </td>\n" +
            "    <td>&quot;Jakarta AND Lucene&quot;will retrieve all documents containing the term Lucene or the term Jakarta </td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td>Plus / Minus </td>\n" +
            "    <td>+, - </td>\n" +
            "    <td>Requires or prohibits a term like &quot;Star  Trek +Spok -Kirk&quot;</td>\n" +
            "  </tr>\n" +
            "</table><br>\n" +
            "<h2>Searching in Fields</h2>\n" +
            "<p>To search in a specific field just use the field name to specify which field to use like: &quot;GivenName:Mathias FamilyName:Lux&quot;. Following fields are available:</p>\n" +
            "<ul>\n" +
            "  <li>GivenName</li>\n" +
            "  <li>FamilyName</li>\n" +
            "  <li>FreeTextAnnotation</li>\n" +
            "  <li>Label</li>\n" +
            "  <li>Who</li>\n" +
            "  <li>Where</li>\n" +
            "  <li>When</li>\n" +
            "  <li>How</li>\n" +
            "  <li>Why</li>\n" +
            "  <li>WhatAction</li>\n" +
            "  <li>WhatObject  </li>\n" +
            "</ul>\n" +
            "<h2>Semantic Search</h2>" +
            "<p>The semantic search allows the retrieval of graphs with example graphs." +
            "The nodes are referenced by Lucene search statement in square brackets, they can be connected by specifying the " +
            "relation (e.g. agentOf, patientOf) followed by the source and the target node referenced by their position in the statement. " +
            "Example: \"[Mathias~] [Talking] agentOf 1 2\" defines two nodes. The first one must contain a term near to the term " +
            "\"Mathias\", the second one must contain the term \"Talking\" These two are connected by the relation agentOf whereas the " +
            "first node is the source, the second node the target.</p>" +
            "<p><b>Other examples:</b></p>" +
            "<pre>" +
            "[Mathias Lux] [Presentation] [Graz] agentOf 1 2 locationOf 3 2\n" +
            "[Mathias OR Werner] [Juggling] [Juggling Convention] agentOf 1 2 locationOf 3 2\n" +
            "[Mathias] [Talking] [Werner] agentOf 3 2 patientOf 1 2\n" +
            "[Mathias] [Know-Center] [Presentation] [Graz]\n" +
            "</pre>" +
            "<p>&nbsp;</p>\n" +
            "</body>\n" +
            "</html>";
    private JScrollPane scrollTextHelp;

    public LuceneHelpDialog(JFrame owner) {
        super(owner, "Lucene Help");
        JEditorPane helpText = new JEditorPane("text/html", HTML_HELP);
        helpText.setEditable(false);

        scrollTextHelp = new JScrollPane(helpText);

        this.setSize(800, 600);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        this.setLocation((d.width - getWidth()) / 2, (d.height - getHeight()) / 2);

        close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(close);
        this.add(scrollTextHelp, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);


    }

    public void scrollToZero() {
        scrollTextHelp.getVerticalScrollBar().setValue(0);
        scrollTextHelp.getHorizontalScrollBar().setValue(0);
    }
}
