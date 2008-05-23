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

import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.utils.TextChangesListener;
import org.jdom.Element;
import org.jdom.Namespace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TextDescriptionPanel extends JPanel implements ActionListener, AnnotationPanel {
    private JTextArea freeText;
    private JTextField whoField;
    private JTextField whereField;
    private JTextField whenField;
    private JTextField whatObjectField;
    private JTextField whatActionField;
    private JTextField whyField;
    private JTextField howField;

    private JTextField[] textFieldArray;

    private AnnotationFrame parent;
    private TextFieldKeyListener tfKeyListener;

    private static final String TEXTFIELD_TOOLTIP =
            "Press F8 to copy the structured text annotation " +
            "to the free text annotation field.";

    public TextDescriptionPanel(AnnotationFrame parent) {
        super(new BorderLayout());
        this.parent = parent;
        tfKeyListener = new TextFieldKeyListener(parent, this);

        JPanel structuredTextPanel = new JPanel(new BorderLayout());
        JPanel structuredTextPanelLabels = new JPanel(new GridLayout(0, 1));
        JPanel structuredTextPanelFields = new JPanel(new GridLayout(0, 1));
        structuredTextPanelLabels.add(new JLabel("Who:"));
        structuredTextPanelLabels.add(new JLabel("Where:"));
        structuredTextPanelLabels.add(new JLabel("When:"));
        structuredTextPanelLabels.add(new JLabel("What Object:"));
        structuredTextPanelLabels.add(new JLabel("What Action:"));
        structuredTextPanelLabels.add(new JLabel("Why:"));
        structuredTextPanelLabels.add(new JLabel("How:"));
        whoField = new JTextField();
        whoField.addKeyListener(TextChangesListener.getInstance());
        JPanel whoPanel = new JPanel(new BorderLayout());
//        JButton takeWhoFromSematics = new JButton(new ImageIcon(AnnotationFrame.class.getResource("data/PlainPeople.gif")));
//        takeWhoFromSematics.setToolTipText("Takes the persons, locations and events names from the semantic annotation");
//        takeWhoFromSematics.setActionCommand("extract");
//        takeWhoFromSematics.addActionListener(this);
        whoPanel.add(whoField, BorderLayout.CENTER);
//        whoPanel.add(takeWhoFromSematics, BorderLayout.EAST);
        structuredTextPanelFields.add(whoPanel);
        whereField = new JTextField();
        whereField.addKeyListener(TextChangesListener.getInstance());
        structuredTextPanelFields.add(whereField);
        whenField = new JTextField();
        whenField.addKeyListener(TextChangesListener.getInstance());
        structuredTextPanelFields.add(whenField);
        whatObjectField = new JTextField();
        whatObjectField.addKeyListener(TextChangesListener.getInstance());
        structuredTextPanelFields.add(whatObjectField);
        whatActionField = new JTextField();
        whatActionField.addKeyListener(TextChangesListener.getInstance());
        structuredTextPanelFields.add(whatActionField);
        whyField = new JTextField();
        whyField.addKeyListener(TextChangesListener.getInstance());
        structuredTextPanelFields.add(whyField);
        howField = new JTextField();
        howField.addKeyListener(TextChangesListener.getInstance());
        structuredTextPanelFields.add(howField);
        structuredTextPanel.add(structuredTextPanelLabels, BorderLayout.WEST);
        structuredTextPanel.add(structuredTextPanelFields, BorderLayout.CENTER);

        textFieldArray = new JTextField[]{
                whoField, whereField, whenField, whatObjectField, whyField,
                whatActionField, howField
        };
        for (int i = 0; i < textFieldArray.length; i++) {
            JTextField field = textFieldArray[i];
            field.addKeyListener(tfKeyListener);
            field.setToolTipText(TEXTFIELD_TOOLTIP);
        }

        JPanel freeTextPanel = new JPanel(new BorderLayout());
        freeText = new JTextArea();
        freeText.setLineWrap(true);
        freeText.setWrapStyleWord(true);
        freeText.addKeyListener(tfKeyListener);
        freeText.addKeyListener(TextChangesListener.getInstance());
        freeText.setToolTipText(TEXTFIELD_TOOLTIP);
        freeTextPanel.add(new JScrollPane(freeText), BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        add(ComponentFactory.createTitledPanel("Structured Text Description:", structuredTextPanel), BorderLayout.NORTH);
        add(ComponentFactory.createTitledPanel("Free Text Description:", freeTextPanel), BorderLayout.CENTER);
    }

    public Element createXML() {
        Element root, textElement = null;
        Namespace mpeg7, xsi;
        String ft = freeText.getText().trim();

        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root = new Element("TextAnnotation", mpeg7);

        // create structured text annotation element
        Element structTextElement = new Element("StructuredAnnotation", mpeg7);
        boolean hasStructuredTextAnnotation = false;
        boolean hasFreeTextAnnotation = false;

        // create and add subelements:
        hasStructuredTextAnnotation = addStructuredTExtElement(structTextElement, whoField, "Who", mpeg7) || hasStructuredTextAnnotation;
        hasStructuredTextAnnotation = addStructuredTExtElement(structTextElement, whatObjectField, "WhatObject", mpeg7) || hasStructuredTextAnnotation;
        hasStructuredTextAnnotation = addStructuredTExtElement(structTextElement, whatActionField, "WhatAction", mpeg7) || hasStructuredTextAnnotation;
        hasStructuredTextAnnotation = addStructuredTExtElement(structTextElement, whereField, "Where", mpeg7) || hasStructuredTextAnnotation;
        hasStructuredTextAnnotation = addStructuredTExtElement(structTextElement, whenField, "When", mpeg7) || hasStructuredTextAnnotation;
        hasStructuredTextAnnotation = addStructuredTExtElement(structTextElement, whyField, "Why", mpeg7) || hasStructuredTextAnnotation;
        hasStructuredTextAnnotation = addStructuredTExtElement(structTextElement, howField, "How", mpeg7) || hasStructuredTextAnnotation;

        // create free text annotation element
        if (ft.length() > 0) {
            textElement = new Element("FreeTextAnnotation", mpeg7);
            hasFreeTextAnnotation = true;
            textElement.setText(ft);

        }
        if (hasFreeTextAnnotation || hasStructuredTextAnnotation) {
            if (hasFreeTextAnnotation) root.addContent(textElement);
            if (hasStructuredTextAnnotation) root.addContent(structTextElement);
            return root;
        } else {
            return null;
        }

    }

    /**
     * Copies the contents from the structured text fields to the
     * free text field.
     */
    public void copyStructuredTextToFreeText() {
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < textFieldArray.length; i++) {
            JTextField field = textFieldArray[i];
            String text = field.getText();
            if (text.trim().length()>0) {
                sb.append(text);
                sb.append(',');
                sb.append(' ');
            }
        }
        String text = sb.toString();
        if (text.length()>1) {
            text = text.substring(0, text.length()-2);
            freeText.setText(text);
        }
    }

    public void setDescriptionText(String descriptionText) {
        freeText.setText(descriptionText);
    }

    public void setHow(String text) {
        this.howField.setText(text);
    }

    public void setWhatAction(String text) {
        this.whatActionField.setText(text);
    }

    public void setWhatObject(String text) {
        this.whatObjectField.setText(text);
    }

    public void setWhen(String text) {
        this.whenField.setText(text);
    }

    public void setWhere(String text) {
        this.whereField.setText(text);
    }

    public void setWho(String text) {
        this.whoField.setText(text);
    }

    public void setWhy(String text) {
        this.whyField.setText(text);
    }

    private boolean addStructuredTExtElement(Element rootElement, JTextField field, String elementName, Namespace namespace) {
        boolean hasText = false;

        if (field.getText().trim().length() > 0) {
            Element where = new Element(elementName, namespace);
            Element name = new Element("Name", namespace);
            name.addContent(field.getText().trim());
            where.addContent(name);
            rootElement.addContent(where);
            hasText = true;
        }

        return hasText;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.startsWith("extract")) {
            String semanticAgentsNames = parent.getSemanticAgentsNames();
            if (whoField.getText().length() > 1 || whereField.getText().length() > 1 || whenField.getText().length() > 1 || whatActionField.getText().length() > 1)
            {
                int returnVal = JOptionPane.showConfirmDialog(this, "All existing data in the Who, Where, When and What Action field\n" +
                        "will be replaced. Are you sure?", "Replace existing data?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (returnVal == JOptionPane.OK_OPTION) {
                    whoField.setText(semanticAgentsNames);
                    whereField.setText(parent.getSemanticPlacesNames());
                    whenField.setText(parent.getSemanticTimesNames());
                    whatActionField.setText(parent.getSemanticEventsNames());
                    freeText.append(semanticAgentsNames + ((semanticAgentsNames.contains(",")) ? " are at " : " is at ") + parent.getSemanticPlacesNames() + " for " + parent.getSemanticEventsNames());
                }
            }
        }
    }

    public void clearTextFields() {
        for (int i = 0; i < textFieldArray.length; i++) {
            JTextField jTextField = textFieldArray[i];
            jTextField.setText("");
        }
        freeText.setText("");
    }
}

class TextFieldKeyListener extends KeyAdapter {
    TextDescriptionPanel tdPanel;
    AnnotationFrame parent;

    public TextFieldKeyListener(AnnotationFrame parent, TextDescriptionPanel tdPanel) {
        this.parent = parent;
        this.tdPanel = tdPanel;
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_F8) {
            tdPanel.copyStructuredTextToFreeText();
        }
    }
}
