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

import at.lux.components.ColorLayoutImageViewPanel;
import at.lux.fotoannotation.utils.TextChangesListener;
import at.lux.imageanalysis.ColorLayout;
import at.lux.imageanalysis.DominantColor;
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.ScalableColor;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorLayoutPanel extends JPanel implements AnnotationPanel {
    private ColorLayout cl;
    private ScalableColor scc;
    private EdgeHistogram eh;
    private DominantColor dc;
    private JTextArea clDesc, sccDesc;
    private JComboBox numY, numC, coeffs, bitplanes;
    private String[] nums = {"1", "3", "6", "10", "15", "21", "28", "64"};
    private String[] bitplaneVals = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
    private String[] coeffVals = {"16", "32", "64", "128", "256"};
    ColorLayoutImageViewPanel imageView;
    private JTextField edgeHistogramValues;
    private JTextField dominantColorValues;

    public ColorLayoutPanel() {
        super(new BorderLayout());
        cl = null;
        scc = null;
        eh = null;
        init();
    }

    private void init() {
        JPanel clPanel = new JPanel(new BorderLayout());
        JPanel sccPanel = new JPanel(new BorderLayout());

        JPanel sccParamPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bitplanes = new JComboBox(bitplaneVals);
        coeffs = new JComboBox(coeffVals);
        coeffs.setSelectedIndex(4);
        bitplanes.setSelectedIndex(0);
        sccParamPanel.add(new JLabel("Bitplanes discarded: "));
        sccParamPanel.add(bitplanes);
        sccParamPanel.add(new JLabel("Coefficients: "));
        sccParamPanel.add(coeffs);

        imageView = new ColorLayoutImageViewPanel();
        numY = new JComboBox(nums);
        numC = new JComboBox(nums);
        clDesc = new JTextArea(4, 20);
        clDesc.setEditable(false);
        sccDesc = new JTextArea(5, 20);
        sccDesc.setEditable(false);
        JPanel npane = new JPanel();
        npane.setLayout(new BoxLayout(npane, BoxLayout.X_AXIS));
//        clPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "ColorLayout Descriptor"));
//        sccPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "ScalableColor Descriptor"));
        npane.add(new JLabel("Y: "));
        npane.add(numY);
        npane.add(new JLabel("C: "));
        npane.add(numC);

        numY.setSelectedIndex(7);
        numC.setSelectedIndex(7);

        numC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refillTextArea();
            }
        });
        numY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refillTextArea();
            }
        });

        bitplanes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recalcScc();
            }
        });
        coeffs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recalcScc();
            }
        });

        numC.addActionListener(TextChangesListener.getInstance());
        numY.addActionListener(TextChangesListener.getInstance());
        bitplanes.addActionListener(TextChangesListener.getInstance());
        coeffs.addActionListener(TextChangesListener.getInstance());

//        JPanel desc = new JPanel(new BorderLayout());
//        desc.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "XML Descriptor"));
//        desc.add(new JScrollPane(clDesc));
        clPanel.add(npane, BorderLayout.NORTH);
        clPanel.add(imageView, BorderLayout.CENTER);
//        clPanel.add(desc, BorderLayout.SOUTH);
        sccPanel.add(new JScrollPane(sccDesc), BorderLayout.CENTER);
        sccPanel.add(sccParamPanel, BorderLayout.SOUTH);

        JPanel ehPanel = new JPanel(new BorderLayout());
//        ehPanel.add(new JLabel("EdgeHistogram values: "), BorderLayout.WEST);
        edgeHistogramValues = new JTextField();
        ehPanel.add(edgeHistogramValues, BorderLayout.CENTER);

        JPanel dominantColorPanel = new JPanel(new BorderLayout());
//        dominantColorPanel.add(new JLabel("DominantColor values: "), BorderLayout.WEST);
        dominantColorValues = new JTextField();
        dominantColorPanel.add(dominantColorValues, BorderLayout.CENTER);

        ehPanel.add(ComponentFactory.createTitledPanel("MPEG-7 DominantColor Descriptor: ", dominantColorPanel), BorderLayout.SOUTH);

        this.add(ComponentFactory.createTitledPanel("MPEG-7 EdgeHistogram Descriptor Values: ", ehPanel), BorderLayout.NORTH);
        this.add(ComponentFactory.createTitledPanel("MPEG-7 ColorLayout Descriptor Visualization: ", clPanel), BorderLayout.CENTER);
        this.add(ComponentFactory.createTitledPanel("MPEG-7 ScalableColor Descriptor: ", sccPanel), BorderLayout.SOUTH);
    };

    public ColorLayout getColorLayout() {
        return cl;
    }

    public void setColorLayout(ColorLayout cl) {
        this.cl = null;
        this.cl = cl;
        cl.setNumberOfYCoeff(Integer.parseInt(numY.getSelectedItem().toString()));
        cl.setNumberOfCCoeff(Integer.parseInt(numC.getSelectedItem().toString()));
        clDesc.setText(new XMLOutputter(Format.getPrettyFormat()).outputString(cl.getDescriptor()));
        imageView.setImg(cl.getColorLayoutImage());
    }

    public void setScalableColor(ScalableColor scc) {
        this.scc = null;
        this.scc = scc;
        scc.setNumberOfCoefficients(Integer.parseInt(coeffs.getSelectedItem().toString()));
        scc.setNumberOfBitplanesDiscarded(Integer.parseInt(bitplanes.getSelectedItem().toString()));
        scc.recalc();
        sccDesc.setText(new XMLOutputter(Format.getPrettyFormat()).outputString(scc.getDescriptor()));
    }

    public void setEdgeHistogram(EdgeHistogram eHistogram) {
        eh = eHistogram;
        StringBuilder sb = new StringBuilder();
        int[] hist = eh.getHistogram();
        sb.append(hist[0]);
        for (int i = 1; i < hist.length; i++) {
            sb.append(' ');
            sb.append(hist[i]);
        }
        edgeHistogramValues.setText(sb.toString());
    }


    public void setDominantColor(DominantColor dcolor) {
        dc = dcolor;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        dominantColorValues.setText(out.outputString(dc.getDescriptor()));
    }

    public DominantColor getDominantColor() {
        return dc;
    }


    public Element createXML() {
        if (cl != null && scc != null) {
            Element elem_cl = cl.getDescriptor();
            Element elem_scc = scc.getDescriptor();
            Element tmp = new Element("DescriptorBag", elem_scc.getNamespace());
            tmp.addContent(elem_cl);
            tmp.addContent(elem_scc);
            tmp.addContent(eh.getDescriptor());
            tmp.addContent(dc.getDescriptor());
            return tmp;
        } else
            return null;
    }

    private void refillTextArea() {
        cl.setNumberOfYCoeff(Integer.parseInt(numY.getSelectedItem().toString()));
        cl.setNumberOfCCoeff(Integer.parseInt(numC.getSelectedItem().toString()));
        clDesc.setText(new XMLOutputter(Format.getPrettyFormat()).outputString(cl.getDescriptor()));
    }

    private void recalcScc() {
        scc.setNumberOfCoefficients(Integer.parseInt(coeffs.getSelectedItem().toString()));
        scc.setNumberOfBitplanesDiscarded(Integer.parseInt(bitplanes.getSelectedItem().toString()));
        scc.recalc();
        sccDesc.setText(new XMLOutputter(Format.getPrettyFormat()).outputString(scc.getDescriptor()));
    }
}
