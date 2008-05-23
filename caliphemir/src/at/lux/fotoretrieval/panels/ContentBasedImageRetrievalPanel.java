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

import at.lux.components.ColorLayoutImageViewPanel;
import at.lux.components.ImageThumbPanel;
import at.lux.fotoannotation.panels.ComponentFactory;
import at.lux.fotoretrieval.RetrievalFrame;
import at.lux.fotoretrieval.RetrievalOperations;
import at.lux.imageanalysis.ColorLayout;
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.ScalableColor;
import at.lux.imaging.BmpReader;
import at.lux.imaging.PpmReader;
import org.jdom.Element;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class ContentBasedImageRetrievalPanel extends JPanel implements ActionListener {
    private ColorLayout cl;
    private ScalableColor scc;
    private EdgeHistogram eh;
    private ImageThumbPanel imgThumb;
    private ColorLayoutImageViewPanel layoutPreview;
    private JPanel leftPanel, rightPanel, selectDescPanel, buttonPanel;
    private JButton search, browse;
    private JTextField imgPath;
    private RetrievalOperations retrievalOps;
//    private JCheckBox incSubDirs;

    private JRadioButton radio_cl, radio_scc, radio_eh;
    private JRadioButton radio_cl_scc, radio_cl_eh, radio_scc_eh;
    private JRadioButton radio_cl_scc_eh;

    public ContentBasedImageRetrievalPanel(RetrievalOperations retrievalOps) {
        super();
        this.retrievalOps = retrievalOps;
        init();
    }

    private void init() {
        cl = null;
        scc = null;
        setLayout(new BorderLayout());

        // init buttons
        search = new JButton("Search");
        search.addActionListener(this);
        search.setActionCommand("search");
        browse = new JButton("Browse ...");
        browse.addActionListener(this);
        browse.setActionCommand("browse");
        browse.setMnemonic(KeyEvent.VK_B);
        browse.setDisplayedMnemonicIndex(0);
//        selDir = new JButton("Directory ...");
//        selDir.addActionListener(this);
//        selDir.setActionCommand("selectDirectory");
//        incSubDirs = new JCheckBox("Include subdirectories", true);

        radio_cl = new JRadioButton("Use ColorLayout", true);
        radio_scc = new JRadioButton("Use ScalableColor", false);
        radio_eh = new JRadioButton("Use EdgeHistogram", false);
        radio_cl_scc = new JRadioButton("Use ColorLayout + ScalableColor", false);
        radio_cl_eh = new JRadioButton("Use ColorLayout + EdgeHistogram", false);
        radio_scc_eh = new JRadioButton("Use ScalableColor + EdgeHistogram", false);
        radio_cl_scc_eh = new JRadioButton("Use ColorLayout + ScalableColor + EdgeHistogram", false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(radio_cl);
        bg.add(radio_scc);
        bg.add(radio_eh);
        bg.add(radio_cl_scc);
        bg.add(radio_cl_eh);
        bg.add(radio_scc_eh);
        bg.add(radio_cl_scc_eh);

        selectDescPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        selectDescPanel.add(radio_cl);
        selectDescPanel.add(radio_scc);
        selectDescPanel.add(radio_eh);
        selectDescPanel.add(radio_cl_scc);
        selectDescPanel.add(radio_cl_eh);
        selectDescPanel.add(radio_scc_eh);
        selectDescPanel.add(radio_cl_scc_eh);
//        selectDescPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select search mode"));

//        sPath = new JTextField(RetrievalFrame.BASE_DIRECTORY);
//        sPath.setEnabled(false);
//        JPanel _dirButtons = new JPanel();
//        _dirButtons.add(selDir);
//        _dirButtons.add(incSubDirs);
//        JPanel _dirPanel = new JPanel(new BorderLayout());
//        _dirPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Search in directory"));
//        _dirPanel.add(new JLabel("Search in directory:"), BorderLayout.NORTH);
//        _dirPanel.add(sPath, BorderLayout.SOUTH);
//        _dirPanel.add(_dirButtons, BorderLayout.CENTER);

        imgPath = new JTextField();
        imgPath.setEnabled(false);
        JPanel _selectionPanel = new JPanel(new BorderLayout(5, 5));
        JPanel _imgSelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        _imgSelButtons.add(browse);
        _selectionPanel.add(_imgSelButtons, BorderLayout.CENTER);
        _selectionPanel.add(imgPath, BorderLayout.NORTH);
//        _selectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select image"));
        buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(search);

        imgThumb = new ImageThumbPanel();
        // fuer einen Rand um den img-thumbnail ...
        JPanel _thumbBorder = new JPanel(new BorderLayout());
//        _thumbBorder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Image preview"));
        _thumbBorder.add(imgThumb, BorderLayout.CENTER);
        layoutPreview = new ColorLayoutImageViewPanel();
        JPanel _lpBorder = new JPanel(new BorderLayout());
//        _lpBorder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "ColorLayout preview"));
        _lpBorder.add(layoutPreview, BorderLayout.CENTER);
        leftPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        leftPanel.add(ComponentFactory.createTitledPanel("ColorLayout preview", _lpBorder));
        leftPanel.add(ComponentFactory.createTitledPanel("Image preview", _thumbBorder));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

        JPanel _wrapper = new JPanel(new BorderLayout());
        _wrapper.add(ComponentFactory.createTitledPanel("Select search mode", selectDescPanel), BorderLayout.NORTH);
//        _wrapper.add(_dirPanel, BorderLayout.NORTH);


        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        rightPanel.add(ComponentFactory.createTitledPanel("Select image", _selectionPanel), BorderLayout.NORTH);
//        rightPanel.add(_dirPanel, BorderLayout.CENTER);
        rightPanel.add(_wrapper, BorderLayout.CENTER);

        JPanel _mainPanel = new JPanel(new GridLayout(1, 0, 5, 5));
        _mainPanel.add(leftPanel);
        _mainPanel.add(rightPanel);

        this.add(_mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void openImage() {
        JFileChooser jfc = new JFileChooser(".");
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                String s = f.getName().toLowerCase();
                if (s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png") ||
                        s.endsWith(".ppm") || s.endsWith(".bmp") || f.isDirectory()) {
                    return true;
                } else
                    return false;
            }

            public String getDescription() {
                return "Images (*.jpg, *.png, *.ppm, *.bmp)";
            }
        });
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img;
                if (jfc.getSelectedFile().getName().toLowerCase().endsWith(".ppm")) {
                    img = PpmReader.read(new FileInputStream(jfc.getSelectedFile()));
                } else if (jfc.getSelectedFile().getName().toLowerCase().endsWith(".bmp")) {
                    img = BmpReader.read(new FileInputStream(jfc.getSelectedFile()));
                } else {
                    img = ImageIO.read(new FileInputStream(jfc.getSelectedFile()));
                }
                // preview
                imgThumb.setImage(img);
                // ColorLayout:
                cl = new ColorLayout(64, 64, img);
                scc = new ScalableColor(img, 64, 0);
                eh = new EdgeHistogram(img);
                layoutPreview.setImg(cl.getColorLayoutImage());
                // display filename:
                imgPath.setText(jfc.getSelectedFile().getCanonicalPath());
            } catch (IOException e) {
                System.err.println("Error reading image: IOException - " + e.getMessage());
            }
        }
    }

    private void startSearch() {
        if (cl != null) {
            Set<Element> s = Collections.synchronizedSet(new HashSet());
            if (radio_cl.isSelected()) {
                s.add(cl.getDescriptor());
                retrievalOps.searchForImage(s, RetrievalFrame.BASE_DIRECTORY, true);
            } else if (radio_scc.isSelected()) {
                s.add(scc.getDescriptor());
                retrievalOps.searchForImage(s, RetrievalFrame.BASE_DIRECTORY, true);
            } else if (radio_eh.isSelected()) {
                s.add(eh.getDescriptor());
                retrievalOps.searchForImage(s, RetrievalFrame.BASE_DIRECTORY, true);
            } else if (radio_cl_scc.isSelected()) {
                s.add(cl.getDescriptor());
                s.add(scc.getDescriptor());
                retrievalOps.searchForImage(s, RetrievalFrame.BASE_DIRECTORY, true);
            } else if (radio_cl_eh.isSelected()) {
                s.add(cl.getDescriptor());
                s.add(eh.getDescriptor());
                retrievalOps.searchForImage(s, RetrievalFrame.BASE_DIRECTORY, true);
            } else if (radio_scc_eh.isSelected()) {
                s.add(scc.getDescriptor());
                s.add(eh.getDescriptor());
                retrievalOps.searchForImage(s, RetrievalFrame.BASE_DIRECTORY, true);
            } else {
                s.add(cl.getDescriptor());
                s.add(scc.getDescriptor());
                s.add(eh.getDescriptor());
                retrievalOps.searchForImage(s, RetrievalFrame.BASE_DIRECTORY, true);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Select Image first!");
        }
    }

    /*
    // not needed anymore, this happens now in RetrievalFrame menu
    private void selectDirectory() {
        JFileChooser jfc = new JFileChooser(".");
        jfc.setMultiSelectionEnabled(false);

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        jfc.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else
                    return false;
            }

            public String getDescription() {
                return "Directories";
            }
        });
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                sPath.setText(jfc.getSelectedFile().getCanonicalPath());
                RetrievalFrame.BASE_DIRECTORY = jfc.getSelectedFile().getCanonicalPath();
            } catch (IOException e) {
                System.err.println("Error reading directory: IOException - " + e.getMessage());
            }
        }
    }  */

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("search")) {
            startSearch();
        } else if (e.getActionCommand().equals("browse")) {
            openImage();
        } else {
            JOptionPane.showMessageDialog(this, "Not implemented yet!");
        }
    }
}
