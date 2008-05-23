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

public class NewTimeDialog extends JDialog implements ActionListener, NewDescriptorDialogInterface {
    //    JTextField name, date;
    //    JTextArea freeText;
    TimePanel panel;
    JButton ok, cancel;
    Element timeDescriptor = null;

    public NewTimeDialog(Frame owner) {
        super(owner, true);
        init();
        // If we got a date stamp from the current file then we can use this as recommended date for a new time object.
        String date = (String) ((AnnotationFrame) owner).getCurrentFileProperties().get("exif.time.full.string");
        if (date != null) {
            panel.setDate(date);
        } else {
            Calendar c = Calendar.getInstance();
            String time = c.get(Calendar.YEAR)
                    + "-" + AnnotationToolkit.to2letters(c.get(Calendar.MONTH) + 1)
                    + "-" + AnnotationToolkit.to2letters(c.get(Calendar.DAY_OF_MONTH))
                    + "T" + AnnotationToolkit.to2letters(c.get(Calendar.HOUR_OF_DAY))
                    + ":" + AnnotationToolkit.to2letters(c.get(Calendar.MINUTE))
                    + ":" + AnnotationToolkit.to2letters(c.get(Calendar.SECOND));

            panel.setDate(time);
        }
    }

    private void init() {
        setTitle("Create new time object");
        panel = new TimePanel();
        JPanel buttonPane = new JPanel(new FlowLayout());

        ok = new JButton("OK");
        ok.setActionCommand("ok");
        ok.addActionListener(this);
        ok.setMnemonic('o');
        cancel = new JButton("Cancel");
        cancel.setActionCommand("cancel");
        cancel.addActionListener(this);

        buttonPane.add(ok);
        buttonPane.add(cancel);

        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPane, BorderLayout.SOUTH);

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            if (createDocument())
                this.setVisible(false);
        } else if (e.getActionCommand().equals("cancel")) {
            this.setVisible(false);
        }

    }


    private boolean createDocument() {
        boolean desc_created = false;
        if (panel.getLabel().length() > 0) {
            Namespace mpeg7, xsi;
            mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
            xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            timeDescriptor = new Element("SemanticBase", mpeg7).setAttribute("type", "SemanticTimeType", xsi);
            Element label = new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(panel.getLabel()));
            timeDescriptor.addContent(label);
            if (panel.getDescription().length() > 0) {
                timeDescriptor.addContent(new Element("Definition", mpeg7).addContent(new Element("FreeTextAnnotation", mpeg7).addContent(panel.getDescription())));
            }
            if (panel.getDate().length() > 0) {
                timeDescriptor.addContent(new Element("Time", mpeg7).addContent(new Element("TimePoint", mpeg7).addContent(panel.getDate())));
            }
            desc_created = true;
        } else {
            JOptionPane.showMessageDialog(this, "At least a name is required!");
        }

        return desc_created;
    }

    public Element createXML() {
        return timeDescriptor;
    }
}
