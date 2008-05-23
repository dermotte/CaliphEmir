package at.knowcenter.caliph.objectcatalog.semanticscreator;

import at.lux.graphviz.DefaultNode;
import at.lux.graphviz.Node;
import at.lux.graphviz.SpringEmbedder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.sleep;

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
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */

/**
 * Date: 06.01.2005
 * Time: 15:06:06
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class EmbedderThread implements Runnable {
    BeePanel panel;
    SpringEmbedder se;
    double x, y;
    boolean running = true;
    LinkedList<EmbedderThread> currentlyRunning = null;

    public EmbedderThread(BeePanel panel, SpringEmbedder se, LinkedList<EmbedderThread> running) {
        for (Iterator<EmbedderThread> iterator = running.iterator(); iterator.hasNext();) {
            iterator.next().endEmbedding();
        }
        currentlyRunning = running;
        running.add(this);
        this.panel = panel;
        this.se = se;
        x = panel.getSize().getWidth() - (SemanticObjectRepresentation.WIDTH << 1);
        y = panel.getSize().getHeight() - SemanticObjectRepresentation.HEIGHT * 3;
    }

    public void run() {
        while (se.step() > 0 && running) {
            List<? extends Node> list = se.getNodeList();

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

                SemanticObjectRepresentation sor = (SemanticObjectRepresentation) ((DefaultNode) node).getNodeObject();
//                sor.getP().setLocation(x * nx , y * ny );
                sor.getP().setLocation(x * nx + (SemanticObjectRepresentation.WIDTH >> 1), y * ny + (SemanticObjectRepresentation.HEIGHT >> 1));
            }
            panel.repaint();
            try {
                sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (currentlyRunning.contains(this)) {
            currentlyRunning.remove(this);
        }
    }

    public void endEmbedding() {
        running = false;
    }
}
