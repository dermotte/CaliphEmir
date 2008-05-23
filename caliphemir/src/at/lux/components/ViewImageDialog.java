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
package at.lux.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Description
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class ViewImageDialog extends JDialog {
    private BufferedImage img;
    private ImageViewPanel panel;
    private JButton rotateCw, rotateCcw, reset;
    private static final String rotateCwButtonText  = "Rotate 90° CW";
    private static final String rotateCcwButtonText  = "Rotate 90° CCW";
    private static final String resetButtonText  = "Reset";
    private double theta = 0d;

    public ViewImageDialog(BufferedImage img) {
        this.img = img;
        this.setTitle("View Image: Zoom 100%");
        rotateCw = new JButton(rotateCwButtonText);
        rotateCw.setMnemonic(KeyEvent.VK_W);
        rotateCw.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rotateImage(-Math.PI / 2.0);
            }
        });
        rotateCcw = new JButton(rotateCcwButtonText);
        rotateCcw.setMnemonic(KeyEvent.VK_C);
        rotateCcw.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rotateImage(Math.PI / 2.0);
            }
        });
        reset = new JButton(resetButtonText);
        reset.setMnemonic(KeyEvent.VK_R);
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rotateImage(0);
            }
        });
        CloseDialogListener closeDialogListener = new CloseDialogListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout());
//        buttonPanel.add(rotateCcw);
        buttonPanel.add(reset);
        buttonPanel.add(rotateCw);
        panel = new ImageViewPanel(img);

        addKeyListener(closeDialogListener);
        buttonPanel.addKeyListener(closeDialogListener);
        panel.addKeyListener(closeDialogListener);
        rotateCw.addKeyListener(closeDialogListener);

        // this.setSize(640, 480);
        this.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
    }

    private void rotateImage(double delta) {
        if (delta == 0) {
            panel.setOp(null);
            rotateCw.setEnabled(true);
        } else {
            AffineTransform transform = AffineTransform.getRotateInstance(-Math.PI / 2.0);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            panel.setOp(op);
            rotateCw.setEnabled(false);
//            rotateCw.setText("Unrotate");
        }
        this.pack();
        panel.repaint();
    }
}

class CloseDialogListener extends KeyAdapter {
    JDialog dialog;
    public CloseDialogListener(JDialog dialog) {
        this.dialog = dialog;
    }

    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
       if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
           dialog.setVisible(false);
       }
    }

    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
    }
}
