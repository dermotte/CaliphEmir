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
import at.lux.fotoannotation.CreationTableModel;
import at.lux.fotoannotation.mpeg7.Mpeg7CreationInformation;
import at.lux.fotoannotation.mpeg7.Mpeg7MediaFormat;
import at.lux.fotoannotation.utils.TextChangesListener;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

public class CreationPanel extends JPanel implements AnnotationPanel {
    JComboBox agents;
    AgentComboBoxModel model;
    private JTable datatable;
    private CreationTableModel ctm;

    public CreationPanel(AgentComboBoxModel model) {
        super(new BorderLayout());
        this.model = model;
        init();
    }

    private void init() {
        agents = new JComboBox(model);
        agents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stateChanged(event);
            }
        });
        agents.addActionListener(TextChangesListener.getInstance());
        ctm = new CreationTableModel();
        datatable = new JTable(ctm);
        datatable.setShowGrid(false);
        datatable.setEnabled(false);
        datatable.setTableHeader(null);
        datatable.setBorder(BorderFactory.createEmptyBorder(0,3,0,3));
        JPanel apanel = new JPanel(new BorderLayout());
        apanel.add(new JLabel("Who took the photo: "), BorderLayout.WEST);
        apanel.add(agents, BorderLayout.CENTER);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(datatable), BorderLayout.CENTER);

        datatable.getColumnModel().getColumn(0).setMaxWidth(120);
        datatable.getColumnModel().getColumn(0).setMinWidth(80);

        datatable.addKeyListener(TextChangesListener.getInstance());

        this.add(ComponentFactory.createTitledPanel("Creator of the image:",apanel), BorderLayout.NORTH);
        this.add(ComponentFactory.createTitledPanel("Creation, EXIF & technical information:", tablePanel), BorderLayout.CENTER);
        this.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.WEST);
    }

    private void stateChanged(ActionEvent event) {
//        debug("state changed: " + (agents.getSelectedIndex() + 1) + ". item selected");
        if (agents.getSelectedIndex() + 1 == agents.getModel().getSize()) {
//            debug("a new agent shall be created!");
            model.createNewAgent();
        } else
            model.reReadAgents();
    }

    public Element createXML() {
        if (agents.getSelectedIndex() + 1 < agents.getModel().getSize()) {
            Element e = (Element) AgentComboBoxModel.getAgents().get(agents.getSelectedIndex());
            Vector settings = new Vector();
            // Eigenschaften ab 7 werden als Settings fürs Tool ausgelesen (EXIF-Tags) :)
            // ist keines gesetzt, so wird der Vector wieder auf null gesetzt :)
            boolean atLeastOneSettingDefined = false;
            Iterator itVals = ctm.getValues().iterator();
            int tempCount = 0;
            for (Object o : ctm.getKeys()) {
                String tempKey = (String) o;
                String tempVal = (String) itVals.next();
                if (tempVal.length() > 0 && tempCount > 5) { // Wenn Wert da und wenns keiner der ersten 6 std werte is
                    settings.add(tempKey + "=" + tempVal);
                    atLeastOneSettingDefined = true;
                }
                tempCount++;
            }
            if (!atLeastOneSettingDefined)
                settings = null;
            String time = null;
            if (datatable.getValueAt(0, 1).toString().length() > 0)
                time = datatable.getValueAt(0, 1).toString();

            String tool = null;
            int count = 0;
            for (Object o1 : ctm.getKeys()) {
                String s = (String) o1;
                if (s.equals("Make")) {
                    if (tool == null) tool = new String();
                    tool = ctm.getValues().get(count) + " " + tool;
                }
                if (s.equals("Model")) {
                    if (tool == null) tool = new String();
                    tool = tool + ctm.getValues().get(count);
                }
                count++;
            }
            Mpeg7CreationInformation m7ci = new Mpeg7CreationInformation(e, tool, settings, time);
            // Getting data ....
            String bits = null, fformat = null, fsize = null, ih = null, iw = null;
            if (datatable.getValueAt(1, 1).toString().length() > 0) fformat = datatable.getValueAt(1, 1).toString();
            if (datatable.getValueAt(2, 1).toString().length() > 0) fsize = datatable.getValueAt(2, 1).toString();
            if (datatable.getValueAt(4, 1).toString().length() > 0) ih = datatable.getValueAt(4, 1).toString();
            if (datatable.getValueAt(3, 1).toString().length() > 0) iw = datatable.getValueAt(3, 1).toString();
            if (datatable.getValueAt(5, 1).toString().length() > 0) bits = datatable.getValueAt(5, 1).toString();
            Mpeg7MediaFormat m7mf = new Mpeg7MediaFormat(bits, fformat, fsize, ih, iw);
            Element mediaFormat = m7mf.createDocument();
            Element ret = new Element("return", mediaFormat.getNamespace()).addContent(mediaFormat);
            ret.addContent(m7ci.createDocument().detach());
            return ret;
        } else {
            return null;
        }
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

//    public void setInstrument(String size) {
//        datatable.setValueAt(size, 0, 1);
//    }

    public void setTime(String time) {
        datatable.setValueAt(time, 0, 1);
    }

//    public void setExposure(String size) {
//        datatable.setValueAt(size, 2, 1);
//    }
//
//    public void setFlash(String size) {
//        datatable.setValueAt(size, 3, 1);
//    }
//
//    public void setFocalLength(String size) {
//        datatable.setValueAt(size, 4, 1);
//    }
//
//    public void setAparture(String size) {
//        datatable.setValueAt(size, 5, 1);
//    }
//
//    public void setBrightness(String size) {
//        datatable.setValueAt(size, 6, 1);
//    }
//
//    public void setDistance(String size) {
//        datatable.setValueAt(size, 7, 1);
//    }

    public void setFileFormat(String fileformat) {
        datatable.setValueAt(fileformat, 1, 1);
    }

    public void setFileSize(String size) {
        datatable.setValueAt(size, 2, 1);
    }

    public void setImageSize(String x, String y) {
        datatable.setValueAt(x, 3, 1);
        datatable.setValueAt(y, 4, 1);
    }

    public void setBitsPerPixel(String x) {
        datatable.setValueAt(x, 5, 1);
        // datatable.setValueAt(y, 11, 1);
    }

    public void addKeyValuePair(String key, String value) {
        ctm.addKeyValuePair(key, value);

    }

    public void updateTable() {
        ctm.fireTableDataChanged();
    }

    public void resetTable() {
        ctm.resetValues();
    }

}
