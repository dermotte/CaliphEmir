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

/**
 * Description
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class ColorLayoutImageViewPanel extends JPanel {
    private static int EMPTY_BORDER = 10;
    private BufferedImage img = null;

    public ColorLayoutImageViewPanel(BufferedImage img) {
        this.img = img;
        this.setPreferredSize(new Dimension(img.getWidth(null) + 2 * EMPTY_BORDER, img.getHeight(null) + 2 * EMPTY_BORDER));
    }

    public ColorLayoutImageViewPanel() {
        this.img = null;
        this.setPreferredSize(new Dimension(128 + 2 * EMPTY_BORDER, 128 + 2 * EMPTY_BORDER));
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        int w = this.getWidth();
        int h = this.getHeight();
        g2.clearRect(0, 0, w, h);
        int min = Math.min(w, h);
        if (img != null) {
            g2.drawImage(img, (w - min) / 2 + EMPTY_BORDER, (h - min) / 2 + EMPTY_BORDER, min - 2 * EMPTY_BORDER, min - 2 * EMPTY_BORDER, null);
        }
    }

    public void setImg(BufferedImage img) {
        this.img = img;
        this.setPreferredSize(new Dimension(img.getWidth(null) + 2 * EMPTY_BORDER, img.getHeight(null) + 2 * EMPTY_BORDER));
    }
}
