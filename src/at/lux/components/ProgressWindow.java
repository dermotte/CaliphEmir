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

/**
 * ProgressWindow
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ProgressWindow extends JWindow {
    private JProgressBar progress;

    public ProgressWindow(Frame owner, JProgressBar progress) {
        super(owner);
        this.progress = progress;
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBorder(BorderFactory.createEtchedBorder());
        p2.add(new JLabel("Please stand by while searching ...", JLabel.CENTER), BorderLayout.CENTER);
        p2.add(progress, BorderLayout.SOUTH);
        p.add(p2, BorderLayout.CENTER);
        this.getContentPane().add(p);
    }

    public ProgressWindow(Frame owner, JProgressBar progress, String message) {
        super(owner);
        this.progress = progress;
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBorder(BorderFactory.createEtchedBorder());
        p2.add(new JLabel(message, JLabel.CENTER), BorderLayout.CENTER);
        p2.add(progress, BorderLayout.SOUTH);
        p.add(p2, BorderLayout.CENTER);
        this.getContentPane().add(p);
    }
}
