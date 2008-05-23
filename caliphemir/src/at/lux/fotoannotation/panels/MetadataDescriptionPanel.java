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

package at.lux.fotoannotation.panels;

import at.lux.fotoannotation.AgentComboBoxModel;
import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.AnnotationToolkit;
import at.lux.fotoannotation.mpeg7.Mpeg7DescriptionMetadata;
import at.lux.fotoannotation.utils.TextChangesListener;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Description
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class MetadataDescriptionPanel extends JPanel implements AnnotationPanel {
    JComboBox agents;
//    JLabel description;
    JTextField version, time, tool;
    JTextField freeText;
    AgentComboBoxModel model;

    public MetadataDescriptionPanel(AgentComboBoxModel model) {
        super();
        this.model = model;
        init();
    }

    private void init() {
        this.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        this.setLayout(new BorderLayout());

        agents = new JComboBox(model);
        agents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stateChanged(event);
            }
        });
        agents.addActionListener(TextChangesListener.getInstance());
        version = new JTextField("1.0", 40);
        version.addKeyListener(TextChangesListener.getInstance());
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        time = new JTextField(AnnotationToolkit.getMpeg7Time(), 40);
        time.addKeyListener(TextChangesListener.getInstance());
        tool = new JTextField(AnnotationToolkit.PROGRAM_NAME + " " + AnnotationToolkit.PROGRAM_VERSION, 40);
        freeText = new JTextField();
        freeText.addKeyListener(TextChangesListener.getInstance());
        JPanel gridPanel = new JPanel(new GridLayout(0, 1));
//        gridPanel.add(new JLabel("Version of the meta data document: "));
//        gridPanel.add(new JLabel("Tool: "));
//        gridPanel.add(new JLabel("Time: "));
        gridPanel.add(new JLabel("Who created the meta data: "));
        gridPanel.add(new JLabel("Comment: "));

        JPanel gridPane2 = new JPanel(new GridLayout(0, 1));
        // Version is now tracked automatically ..
//        gridPane2.add(version);
//        gridPane2.add(tool);
//        gridPane2.add(time);
        gridPane2.add(agents);
        gridPane2.add(freeText);

        JPanel tempPanel = new JPanel(new BorderLayout());
        tempPanel.add(gridPanel, BorderLayout.WEST);
        tempPanel.add(gridPane2, BorderLayout.CENTER);

        this.add(ComponentFactory.createTitledPanel("Meta Data Description:", tempPanel), BorderLayout.NORTH);
    }

    private void stateChanged(ActionEvent event) {
        debug("state changed: " +
                (agents.getSelectedIndex() + 1) + ". item selected");
        if (agents.getSelectedIndex() + 1 == agents.getModel().getSize()) {
            debug("a new agent shall be created!");
            model.createNewAgent();
        } else
            model.reReadAgents();
    }

    private String getDescription() {
        StringBuffer buffer = new StringBuffer("<html>");
        if (agents.getSelectedIndex() + 1 < agents.getModel().getSize()) {
            Element e = (Element) model.getAgents().get(agents.getSelectedIndex());
            buffer.append("<font size=\"+1\">" + e.getChild("Name", e.getNamespace()).getChildTextTrim("GivenName",
                    e.getNamespace()) + " ");
            buffer.append(e.getChild("Name", e.getNamespace()).getChildTextTrim("FamilyName",
                    e.getNamespace()) + "</font>");
            // Organization
            java.util.List results = AnnotationToolkit.xpathQuery(e, "Affiliation/Organization/Name", null);
            for (Iterator i = results.iterator(); i.hasNext();) {
                Element orgName = (Element) i.next();
                buffer.append("<br> Organization: <i>" + orgName.getTextTrim() + "</i>");
            }
            // Email
            results = AnnotationToolkit.xpathQuery(e, "ElectronicAddress/Email", null);
            for (Iterator i = results.iterator(); i.hasNext();) {
                Element mail = (Element) i.next();
                buffer.append("<br> Email: <i>" + mail.getTextTrim() + "</i>");
            }
            // Address
            results = AnnotationToolkit.xpathQuery(e, "Address/PostalAddress/AddressLine", null);
            if (results.size() > 0) {
                buffer.append("<br>Address:");
            }
            for (Iterator i = results.iterator(); i.hasNext();) {
                Element orgName = (Element) i.next();
                buffer.append("<br>&nbsp;&nbsp;<i>" + orgName.getTextTrim() + "</i>");
            }
            // More to come ....
        } else {
            buffer.append("No agent selected");
        }
        buffer.append("</html>");
        debug("setting description to: " + buffer.toString());
        return buffer.toString();
    }

    public Element createXML() {
        if (agents.getSelectedIndex() + 1 < agents.getModel().getSize()) {
            Element e = (Element) model.getAgents().get(agents.getSelectedIndex());
            Mpeg7DescriptionMetadata md =
                    new Mpeg7DescriptionMetadata(version.getText(), (Element) ((Element) e.clone()).detach(), null, tool.getText(), freeText.getText(), time.getText());
            return md.getDescriptionMetadata();
        } else {
            JOptionPane.showMessageDialog(this, "No agent selected!");
            return null;
        }
    }

    public void setDescriptionMetadata(Element e) {
        // String time, tool, version, freeText;
        java.util.List list = AnnotationToolkit.xpathQuery(e, "CreationTime", null);
        if (list.size() > 0)
            time.setText(((Element) list.get(0)).getTextTrim());

        list = AnnotationToolkit.xpathQuery(e, "Instrument/Tool/Name", null);
        if (list.size() > 0)
            tool.setText(((Element) list.get(0)).getTextTrim());
        list = AnnotationToolkit.xpathQuery(e, "Version", null);
        if (list.size() > 0)
            version.setText(((Element) list.get(0)).getTextTrim());
        list = AnnotationToolkit.xpathQuery(e, "Comment/FreeTextAnnotation", null);
        if (list.size() > 0)
            freeText.setText(((Element) list.get(0)).getTextTrim());
    }


    public void setTextDescription(String descriptionText) {
        freeText.setText(descriptionText);
    }

    public void setVersion(String version) {
        this.version.setText(version);
    }

    public void setInstrument(String instrument) {
        this.tool.setText(instrument);
    }

    public void setTime(String time) {
        this.time.setText(time);
    }

    public void setToCurrentTime() {
        time.setText(AnnotationToolkit.getMpeg7Time());
    }

    public void setAgent(String agentName) {
        int index = 0;
        debug("scanning " + agents.getItemCount() + " agents");
        for (int i = 0; i < agents.getItemCount(); i++) {
            if (agents.getItemAt(i).toString().equals(agentName)) {
                index = i;
                debug("matched " + agentName);
            } else {
                debug("not matched " + agentName);
            }
        }
        agents.setSelectedIndex(index);
    }

    private void debug(String message) {
        if (AnnotationFrame.DEBUG) System.out.println("[at.lux.fotoannotation.MetadataDescriptionPanel] " + message);
    }

}
