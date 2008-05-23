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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.gui.dominantcolor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.StringTokenizer;

public class DominantColorPlugin extends JDialog implements ActionListener {
    public static String DIALOG_TITLE = "Dominant Color Plugin";
    public static String TOOLBAR_NAME = "Main Toolbar";

    private boolean hideWhenFinished = false;

    private BufferedImage img;
    private DCImagePanel imgPanel;

    private JToolBar toolbar;
    private JButton calcDC, viewDC, cancel, okay, setRect, setManualRegion;
    private JProgressBar pb;
    private JPanel statusPanel, buttonPanel, southPanel;
    private JLabel status;
    private JComboBox rects;
    private RGBColorPercentagePairList list;

    public DominantColorPlugin(BufferedImage image) {
        super();
        this.setTitle(DIALOG_TITLE);
        init(image);
    }

    public DominantColorPlugin(BufferedImage image, boolean hideWhenFinished) {
        super();
        this.setTitle(DIALOG_TITLE);
        this.hideWhenFinished = hideWhenFinished;
        init(image);
    }


    public DominantColorPlugin(Frame owner, BufferedImage image) {
        super(owner, DIALOG_TITLE, true);
        init(image);
    }

    public DominantColorPlugin(Frame owner, BufferedImage image, boolean hideWhenFinished) {
        super(owner, DIALOG_TITLE, true);
        this.hideWhenFinished = hideWhenFinished;
        init(image);
    }

    public DominantColorPlugin(Frame owner, boolean modal, BufferedImage image) {
        super(owner, DIALOG_TITLE, modal);
        init(image);
    }

    public DominantColorPlugin(Frame owner, boolean modal, BufferedImage image, boolean hideWhenFinished) {
        super(owner, DIALOG_TITLE, modal);
        this.hideWhenFinished = hideWhenFinished;
        init(image);
    }

    private void init(BufferedImage image) {
        img = image;
        imgPanel = new DCImagePanel(img);
        toolbar = new JToolBar(TOOLBAR_NAME);
        statusPanel = new JPanel(new BorderLayout());
        buttonPanel = new JPanel(new FlowLayout());
        southPanel = new JPanel(new BorderLayout());
        status = new JLabel("");

        calcDC = new JButton(new ImageIcon(DominantColorPlugin.class.getResource("icon5.gif")));
        calcDC.setActionCommand("calcDC");
        calcDC.addActionListener(this);
        calcDC.setToolTipText("Calculate global dominant color");

        viewDC = new JButton(new ImageIcon(DominantColorPlugin.class.getResource("icon4.gif")));
        viewDC.setActionCommand("view");
        viewDC.addActionListener(this);
        viewDC.setToolTipText("View dominant colors");

        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel");
        cancel.setToolTipText("Cancel operation");

        okay = new JButton("OK");
        okay.addActionListener(this);
        okay.setActionCommand("okay");
        okay.setToolTipText("Calculate dominant colors");

        setRect = new JButton(new ImageIcon(DominantColorPlugin.class.getResource("icon3.gif")));
        setRect.addActionListener(this);
        setRect.setActionCommand("rect");
        setRect.setToolTipText("Set predefined region");

        setManualRegion = new JButton(new ImageIcon(DominantColorPlugin.class.getResource("icon2.gif")));
        setManualRegion.addActionListener(this);
        setManualRegion.setActionCommand("manual");
        setManualRegion.setToolTipText("Set region manually");

        String[] regions = {"no region", "left half", "right half", "upper half", "bottom half", "left third", "middle third", "right third", "upper third", "center third", "bottom third", "center (big)", "center (small)"};
        rects = new JComboBox(regions);
        rects.addActionListener(this);
        rects.setActionCommand("rect");

        pb = new JProgressBar(0, 100);
        pb.setStringPainted(true);

        toolbar.add(calcDC);
        toolbar.addSeparator();
        toolbar.add(viewDC);
        toolbar.addSeparator();
        toolbar.add(rects);
        toolbar.add(setRect);
        toolbar.addSeparator();
        toolbar.add(setManualRegion);

        statusPanel.add(pb, BorderLayout.EAST);
        statusPanel.add(status, BorderLayout.CENTER);

        buttonPanel.add(okay);
        buttonPanel.add(cancel);

        southPanel.add(statusPanel, BorderLayout.SOUTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);

        this.getContentPane().add(imgPanel, BorderLayout.CENTER);
        this.getContentPane().add(toolbar, BorderLayout.NORTH);
        this.getContentPane().add(southPanel, BorderLayout.SOUTH);

        this.pack();
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("calcDC")) {
            calcDC();
        } else if (e.getActionCommand().equals("view")) {
            viewDC();
        } else if (e.getActionCommand().equals("okay")) {
            hideWhenFinished = true;
            calcDC();
        } else if (e.getActionCommand().equals("cancel")) {
            this.setVisible(false);
        } else if (e.getActionCommand().equals("manual")) {
            this.showRegionDialog();
        } else if (e.getActionCommand().equals("rect")) {
            if (rects.getSelectedIndex() == 0) {
                imgPanel.clearRegion();
            } else if (rects.getSelectedIndex() == 1) {
                imgPanel.leftHalf();
            } else if (rects.getSelectedIndex() == 2) {
                imgPanel.rightHalf();
            } else if (rects.getSelectedIndex() == 3) {
                imgPanel.upperHalf();
            } else if (rects.getSelectedIndex() == 4) {
                imgPanel.bottomHalf();
            } else if (rects.getSelectedIndex() == 5) {
                imgPanel.leftThird();
            } else if (rects.getSelectedIndex() == 6) {
                imgPanel.middleThird();
            } else if (rects.getSelectedIndex() == 7) {
                imgPanel.rightThird();
            } else if (rects.getSelectedIndex() == 8) {
                imgPanel.upperThird();
            } else if (rects.getSelectedIndex() == 9) {
                imgPanel.centerThird();
            } else if (rects.getSelectedIndex() == 10) {
                imgPanel.bottomThird();
            } else if (rects.getSelectedIndex() == 11) {
                imgPanel.centerBig();
            } else if (rects.getSelectedIndex() == 12) {
                imgPanel.centerSmall();
            }
        }
    }

    /**
     * For testing purpose only
     */
    public static void main(String[] args) {
        try {
            new DominantColorPlugin(ImageIO.read(DominantColorPlugin.class.getResource("testimage.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calcDC() {
        WritableRaster r = img.getRaster();
        list = new RGBColorPercentagePairList();
        DominantColorFinder df = new DominantColorFinder(r, pb, list, this, status, imgPanel.getPointA(), imgPanel.getPointB());
        df.start();
    }

    private void viewDC() {
        if (list != null && list.size() > 0) {
            JOptionPane.showMessageDialog(this, list.toString());
        } else {
            JOptionPane.showMessageDialog(this, "Calculate dominant colors first!");
        }
    }

    private void showRegionDialog() {
        String tmp = JOptionPane.showInputDialog(this, "Imagesize: " + img.getWidth() + "x" + img.getHeight() + " pixels\nDefine points (like: \"ax,ay,bx,by\"):");
        int ax, ay, bx, by;
        if (tmp != null && tmp.length() > 6) {
            try {
                StringTokenizer st = new StringTokenizer(tmp, ",");
                ax = Integer.parseInt(st.nextToken());
                ay = Integer.parseInt(st.nextToken());
                bx = Integer.parseInt(st.nextToken());
                by = Integer.parseInt(st.nextToken());
                imgPanel.setRect(new Point(ax, ay), new Point(bx, by));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public RGBColorPercentagePairList getDominantColors() {
        return list;
    }

    public RGBColorPercentagePairList getResult() {
        return list;
    }

    public void hideIfToldToDo() {
        if (hideWhenFinished)
            this.setVisible(false);
    }
}
