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
import at.lux.fotoannotation.mpeg7.Mpeg7Quality;
import at.lux.fotoannotation.utils.TextChangesListener;
import org.jdom.Element;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * Description
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class QualityPanel extends JPanel implements AnnotationPanel {
    JComboBox agents;
    AgentComboBoxModel model;
    JSlider slider;
//    JLabel quality;
    JCheckBox useDescriptor;
    String[] q = {"Unsatisfactory", "Poor", "Fair", "Good", "Excellent"};

    public QualityPanel(AgentComboBoxModel model) {
        super();
        this.model = model;
        init();
    }

    private void init() {
        this.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
//        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Image quality:"));
        this.setLayout(new BorderLayout());

        agents = new JComboBox(model);
        agents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stateChanged(event);
            }
        });
        agents.addActionListener(TextChangesListener.getInstance());
        slider = new JSlider(JSlider.VERTICAL, 1, 5, 1);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(2);
        slider.setMinorTickSpacing(1);
        Hashtable table = new Hashtable();
        for (int i = 0; i < q.length; i++) {
            if (i % 2 == 0) table.put(new Integer(i + 1), new JLabel(q[i]));
        }
        slider.setLabelTable(table);
        slider.setPaintLabels(true);

        slider.setValue(3);
        slider.setPreferredSize(new Dimension(120, 100));

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                useDescriptor.setSelected(true);
//                setQualityLabel();
            }
        });
        slider.addChangeListener(TextChangesListener.getInstance());

        useDescriptor = new JCheckBox();
        useDescriptor.setSelected(false);
        useDescriptor.addActionListener(TextChangesListener.getInstance());
//        quality = new JLabel("", JLabel.CENTER);
//        setQualityLabel();

        JPanel spane = new JPanel(new BorderLayout());
        spane.add(slider, BorderLayout.CENTER);
//        spane.add(quality, BorderLayout.SOUTH);

        JPanel apane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        apane.add(new JLabel("Who rated the quality of the picture: "));

        JPanel usePane = new JPanel(new FlowLayout());
        usePane.add(useDescriptor);
        usePane.add(new JLabel("Include quality rating"));

        apane.add(agents);
        apane.add(usePane);
//        apane.add(quality);

        JPanel tmpPanel = new JPanel(new BorderLayout());
        tmpPanel.add(apane, BorderLayout.CENTER);
        tmpPanel.add(spane, BorderLayout.WEST);
        this.add(ComponentFactory.createTitledPanel("Image Quality Rating:", tmpPanel), BorderLayout.CENTER);
//        this.repaint();
    }

    private void stateChanged(ActionEvent event) {
        debug("state changed: " + (agents.getSelectedIndex() + 1) + ". item selected");
        if (agents.getSelectedIndex() + 1 == agents.getModel().getSize()) {
            debug("a new agent shall be created!");
            model.createNewAgent();
        } else
            model.reReadAgents();
    }

//    private void setQualityLabel() {
//        quality.setText(q[slider.getValue()-1]);
//    }

    public Element createXML() {
        if (agents.getSelectedIndex() + 1 < agents.getModel().getSize()) {
            Element e = (Element) model.getAgents().get(agents.getSelectedIndex());
            Mpeg7Quality mq = new Mpeg7Quality(e, slider.getValue() + "");
            if (useDescriptor.isSelected())
                return mq.createDocument();
            else
                return null;
        } else {
            JOptionPane.showMessageDialog(this, "No agent selected!");
            return null;
        }
    }

    public void setQuality(int quality) {
        slider.setValue(quality);
//        setQualityLabel();
    }

    public void setAgent(String agentName) {
        int index = 0;
        debug("scanning " + agents.getItemCount() + " agents");
        for (int i = 0; i < agents.getItemCount(); i++) {
            if (agents.getItemAt(i).toString().equals(agentName)) {
                index = i;
                debug(agents.getItemAt(i).toString() + "matched " + agentName);
            } else {
                debug(agents.getItemAt(i).toString() + "not matched " + agentName);
            }
        }
        agents.setSelectedIndex(index);
    }

    public void setIncludeQuality(boolean inc) {
        useDescriptor.setSelected(inc);
    }

    private void debug(String message) {
        if (AnnotationFrame.DEBUG) System.out.println("[at.lux.fotoannotation.panels.QualityPanel] " + message);
    }
}
