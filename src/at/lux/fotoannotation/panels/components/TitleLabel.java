package at.lux.fotoannotation.panels.components;

import javax.swing.*;
import java.awt.*;
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

/**
 * This file is part of Caliph & Emir
 * Date: 04.11.2005
 * Time: 23:00:56
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TitleLabel extends JPanel {
    private String label;
    private Font font;
    private final int rightMargin;
    private final int bottomMargin;

    public TitleLabel(String label, Font font) {
        this.label = label;
        this.font = font;
        this.setPreferredSize(new Dimension(10, 22));
        rightMargin = 3;
        bottomMargin = 6;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);
        int width = g2.getFontMetrics().stringWidth(label);
        int height = (int) font.getSize2D();
        g2.drawString(label, 0, getHeight() - bottomMargin);
        int y1 = getHeight() - bottomMargin - height / 2;
        g2.setColor(Color.decode("#808080"));
        g2.drawLine(width + 6, y1, getWidth()-rightMargin, y1);
        g2.setColor(Color.decode("#FFFFFF"));
        g2.drawLine(width + 6, y1+1, getWidth()-rightMargin, y1+1);
    }
}
