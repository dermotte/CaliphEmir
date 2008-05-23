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

import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.AnnotationToolkit;
import org.jdom.Element;
import org.jdom.Namespace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class NewEventDialog extends JDialog implements ActionListener, NewDescriptorDialogInterface {
    Element eventDescriptor = null;
    private EventPanel eventPanel;
    private Frame owner = null;

    public NewEventDialog(Frame owner) {
        super(owner, true);
        this.owner = owner;
        init();
        // If we got a date stamp from the current file then we can use this as recommended date for a new event object.
        String date = (String) ((AnnotationFrame) owner).getCurrentFileProperties().get("exif.time.full.string");
        if (date != null) {
            eventPanel.setDate(date);
        } else {
            Calendar c = Calendar.getInstance();
            String time = c.get(Calendar.YEAR)
                    + "-" + AnnotationToolkit.to2letters(c.get(Calendar.MONTH) + 1)
                    + "-" + AnnotationToolkit.to2letters(c.get(Calendar.DAY_OF_MONTH))
                    + "T" + AnnotationToolkit.to2letters(c.get(Calendar.HOUR_OF_DAY))
                    + ":" + AnnotationToolkit.to2letters(c.get(Calendar.MINUTE))
                    + ":" + AnnotationToolkit.to2letters(c.get(Calendar.SECOND));

            eventPanel.setDate(time);
        }
        // set place if already known ...
        String placeLabel = (String) ((AnnotationFrame) owner).getCurrentFileProperties().get("last.place.label");
        String placeAddress = (String) ((AnnotationFrame) owner).getCurrentFileProperties().get("last.place.address");
        if (placeAddress != null) eventPanel.setPlaceAddress(placeAddress);
        if (placeLabel != null) eventPanel.setPlaceLabel(placeLabel);
    }

    private void init() {
        this.setTitle("Create new event object");

        JPanel bpane = new JPanel(new FlowLayout());

        JButton ok = new JButton("OK");
        ok.addActionListener(this);
        ok.setActionCommand("ok");
        ok.setMnemonic('o');
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel");
        bpane.add(ok);
        bpane.add(cancel);

        eventPanel = new EventPanel();
        this.getContentPane().add(eventPanel, BorderLayout.CENTER);
        this.getContentPane().add(bpane, BorderLayout.SOUTH);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            if (createDocument()) {
                setVisible(false);
            }
        } else if (e.getActionCommand().equals("cancel")) {
            setVisible(false);
        }
    }

    private boolean createDocument() {

        boolean eventValid = false;
        String event = eventPanel.getEvent();
        String date = eventPanel.getDate();
        String place = eventPanel.getPlace();
        // storing the place for latter use:
        ((AnnotationFrame) owner).getCurrentFileProperties().put("last.place.label", place);
        String address = eventPanel.getAddress();
        ((AnnotationFrame) owner).getCurrentFileProperties().put("last.place.address", address);
        String description = eventPanel.getDescription();
        Vector addressLines = null;
        if (address.length() > 0) {
            addressLines = new Vector();
            StringTokenizer t = new StringTokenizer(address, "\n", false);
            while (t.hasMoreElements()) {
                String s = t.nextToken();
                addressLines.add(s.trim());
            }
        }
        if (event.length() < 1) event = null;
        if (date.length() < 1) date = null;
        if (place.length() < 1) place = null;

        if (event == null || place == null || date == null) {
            JOptionPane.showMessageDialog(this, "Event name, date and place are mandatory!");
            return false;
        }

        if (place != null) {
            System.out.println("[at.lux.fotoannotation.NewEventDialog] Generating MPEG-7");
            Namespace mpeg7, xsi;
            mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
            xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            eventDescriptor = new Element("SemanticBase", mpeg7).setAttribute("type", "EventType", xsi);
            Element label = new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(event));
            Element definition = null;
            if (description.length() > 0) {
                definition = new Element("Definition", mpeg7).addContent(new Element("FreeTextAnnotation", mpeg7).addContent(description));
            }
            eventDescriptor.addContent(label);
            if (definition != null) {
                eventDescriptor.addContent(definition);
            }

            // Place && Time:
            Element elem_place = new Element("SemanticPlace", mpeg7);
            Element elem_time = new Element("SemanticTime", mpeg7);
            Element elem_event = new Element("Event", mpeg7);
            elem_event.addContent((Element) label.clone());
            if (place != null) {
                elem_place.addContent(new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(place)));
                if (addressLines != null) {
                    Element elem_address = new Element("PostalAddress", mpeg7);
                    for (Iterator it1 = addressLines.iterator(); it1.hasNext();) {
                        String s = (String) it1.next();
                        elem_address.addContent(new Element("AddressLine", mpeg7).addContent(s));
                    }
                    elem_place.addContent(new Element("Place", mpeg7).addContent(elem_address));
                }
                elem_event.addContent(elem_place);
            }
            if (date != null) {
                elem_time.addContent(new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent("Time of " + event)));
                elem_time.addContent(new Element("Time", mpeg7).addContent(new Element("TimePoint", mpeg7).addContent(date)));
                elem_event.addContent(elem_time);
            }
            eventDescriptor.addContent(elem_event);
            eventValid = true;
        } else {
            JOptionPane.showMessageDialog(this, "At least the event name is needed!");
        }
        return eventValid;
    }

    public Element createXML() {
        return eventDescriptor;
    }
}

