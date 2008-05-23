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
package at.lux.graphviz;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

/**
 * Date: 14.09.2004
 * Time: 22:00:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SpringEmbedderVis extends JPanel implements Runnable {
    SpringEmbedder se;
    int count = 0;

    public SpringEmbedderVis(SpringEmbedder se) {
        this.se = se;
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        double x = this.getSize().getWidth();
        double y = this.getSize().getHeight();

        g2.clearRect(0, 0, (int) x, (int) y);

        List<? extends Node> list =  se.getNodeList();

        double xMin = 1.0, xMax = 0.0, yMin = 1.0, yMax = 0.0;
        for (Iterator<? extends Node> iterator = list.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            if (node.getX() < xMin) xMin = node.getX();
            if (node.getX() > xMax) xMax = node.getX();
            if (node.getY() < yMin) yMin = node.getY();
            if (node.getY() > yMax) yMax = node.getY();
        }


        for (Iterator<? extends Node> it = list.iterator(); it.hasNext();) {
            Node node = it.next();
            double nx = (node.getX() - xMin) / (xMax - xMin);
            double ny = (node.getY() - yMin) / (yMax - yMin);
            g2.fillOval((int) (nx * x) - 2, (int) (ny * y) - 2, 5, 5);
        }

        java.util.List<? extends Edge> l = se.getEdgeList();
        for (Iterator<? extends Edge> iterator = l.iterator(); iterator.hasNext();) {
            Edge edge = iterator.next();
            double nx1 = (edge.getStartNode().getX() - xMin) / (xMax - xMin);
            double ny1 = (edge.getStartNode().getY() - yMin) / (yMax - yMin);
            double nx2 = (edge.getEndNode().getX() - xMin) / (xMax - xMin);
            double ny2 = (edge.getEndNode().getY() - yMin) / (yMax - yMin);
            g2.drawLine((int) (nx1 * x), (int) (ny1 * y),
                    (int) (nx2 * x), (int) (ny2 * y));
        }
//        System.out.println(xMin + "," + yMin + " - " + xMax +","+ yMax);
    }

    public SpringEmbedder getSe() {
        return se;
    }


    public void run() {
        do {
            se.step();
            repaint();
            count++;
            try {
                Thread.currentThread().sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (count < 1000);
    }

}