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
package at.lux.fotoretrieval;

import at.lux.fotoretrieval.panels.GraphConstructionPanel;
import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Date: 06.01.2005
 * Time: 15:50:49
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GraphConstructionTest extends TestCase{
    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        frame.addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        GraphConstructionPanel panel = new GraphConstructionPanel();
        frame.setSize(320, 480);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.addNode("Mathias");
        panel.addNode("Zuhause");
        panel.addNode("Computer");
        panel.addRelation("locationOf", "Computer", "Zuhause");
//        panel.addRelation("locationOf", "Mathias", "Zuhause");
        panel.addRelation("locationOf", "Mathias", "Computer");
        frame.setVisible(true);
        panel.embedGraph();
    }
}
