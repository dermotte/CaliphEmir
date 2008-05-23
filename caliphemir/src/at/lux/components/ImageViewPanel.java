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
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * Description
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class ImageViewPanel extends JPanel {
    private static int EMPTY_BORDER = 10;
    private BufferedImage img = null;
    private BufferedImageOp op = null;

    public ImageViewPanel(BufferedImage img) {
        this.img = img;
        this.setPreferredSize(new Dimension(img.getWidth(null) + 2 * EMPTY_BORDER, img.getHeight(null) + 2 * EMPTY_BORDER));
    }

    public ImageViewPanel() {
        this.img = null;
        this.setPreferredSize(new Dimension(64 + 2 * EMPTY_BORDER, 64 + 2 * EMPTY_BORDER));
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());
        if (img != null) {
            if (op != null) {
                g2.drawImage(img, op, img.getHeight() + EMPTY_BORDER, EMPTY_BORDER);
            } else {
                g2.drawImage(img, EMPTY_BORDER, EMPTY_BORDER, null);
            }
        }
    }

    public void setImg(BufferedImage img) {
        this.img = img;
        this.setPreferredSize(new Dimension(img.getWidth(null) + 2 * EMPTY_BORDER, img.getHeight(null) + 2 * EMPTY_BORDER));
    }

    public BufferedImageOp getOp() {
        return op;
    }

    public void setOp(BufferedImageOp op) {
        this.op = op;
        if (op != null)
            this.setPreferredSize(new Dimension(img.getHeight(null) + 2 * EMPTY_BORDER, img.getWidth(null) + 2 * EMPTY_BORDER));
        else
            this.setPreferredSize(new Dimension(img.getWidth(null) + 2 * EMPTY_BORDER, img.getHeight(null) + 2 * EMPTY_BORDER));
    }
}
