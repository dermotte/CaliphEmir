package at.lux.fotoannotation.panels;

import at.lux.fotoannotation.panels.components.TitleLabel;

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
 * Time: 22:39:50
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ComponentFactory {
    public static JPanel createTitledPanel(String title, JPanel panel) {
        JPanel result = new JPanel(new BorderLayout());
        result.add(panel, BorderLayout.CENTER);
//        JLabel titleLabel = new JLabel(title);
//        Font font = titleLabel.getFont();
//        titleLabel.setFont(font.deriveFont(Font.BOLD, font.getSize2D() + 1f));
//        titleLabel.setBorder(BorderFactory.createEmptyBorder(9, 0, 3, 0));
//        ((Graphics2D) titleLabel.getGraphics()).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        result.add(new TitleLabel(title, new JLabel().getFont().deriveFont(Font.BOLD)), BorderLayout.NORTH);
        return result;
    }
}
