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

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

/**
 * Date: 14.09.2004
 * Time: 21:49:11
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SpringEmbedderTest extends TestCase {
    public void testSpringEmbedderLocal() {
        int numNodes = 12;
        LinkedList<Node> nodes = new LinkedList<Node>();
        LinkedList<Edge> edges = new LinkedList<Edge>();
        nodes.add(new DefaultNode(Math.random(), Math.random()));
        for (int i = 1; i < 12; i++) {
            DefaultNode node = new DefaultNode(Math.random(), Math.random());
            nodes.add(node);
            DefaultEdge edge = new DefaultEdge(nodes.get(0), node);
            edges.add(edge);
        }

        SpringEmbedder se = new SpringEmbedder(nodes, edges);
        for (int i = 0; i < 100; i++) {
            se.step();
        }
    }

    public static void main(String[] agrs) {
        int numNodes = 40;
        LinkedList<Node> nodes = new LinkedList<Node>();
        LinkedList<Edge> edges = new LinkedList<Edge>();
        nodes.add(new DefaultNode(Math.random(), Math.random()));
        for (int i = 1; i < numNodes; i++) {
            DefaultNode node = new DefaultNode(Math.random(), Math.random());
            nodes.add(node);
            if (i<20) edges.add(new DefaultEdge(nodes.get(0), node));
            else edges.add(new DefaultEdge(nodes.get(19), node));
        }
//        edges.add(new DefaultEdge(nodes.get(0), nodes.get(1)));
//        edges.add(new DefaultEdge(nodes.get(1), nodes.get(2)));
//        edges.add(new DefaultEdge(nodes.get(2), nodes.get(3)));
//        edges.add(new DefaultEdge(nodes.get(3), nodes.get(4)));
//        edges.add(new DefaultEdge(nodes.get(4), nodes.get(0)));
//        edges.add(new DefaultEdge(nodes.get(0), nodes.get(2)));

        SpringEmbedder se = new SpringEmbedder(nodes, edges);
        JFrame frame = new JFrame("Test SpringEmbedder");
        frame.setSize(800, 600);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        final SpringEmbedderVis spanel = new SpringEmbedderVis(se);
        frame.getContentPane().add(spanel, BorderLayout.CENTER);
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(spanel);
                t.start();
            }
        });
        frame.getContentPane().add(startButton, BorderLayout.SOUTH);
        frame.setVisible(true);
    }


}
