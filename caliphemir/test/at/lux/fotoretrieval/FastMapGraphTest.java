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

import at.lux.fotoretrieval.lucene.Graph;
import at.lux.retrieval.fastmap.ArrayFastmapDistanceMatrix;
import at.lux.retrieval.fastmap.DistanceCalculator;
import at.lux.retrieval.fastmap.FastMap;
import at.lux.retrieval.fastmap.FastmapDistanceMatrix;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

/**
 * Date: 15.01.2005
 * Time: 00:34:51
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FastMapGraphTest {
    public static void main(String[] args) throws IOException {
        File indexFile = new File("testdata/idx_graphs.list");
        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(indexFile))));
        String line;
        LinkedList<Graph> graphList = new LinkedList<Graph>();
        while ((line = br.readLine())!=null) {
            String graphString = line.split("\\|")[0];
            System.out.println(graphString);
            Graph g = new Graph(graphString);
            graphList.add(g);
        }

        FastmapDistanceMatrix matrixFastmap;
        matrixFastmap = new ArrayFastmapDistanceMatrix(graphList, new DistanceCalculator() {
            public float getDistance(Object o1, Object o2) {
                Graph g1 = (Graph) o1;
                Graph g2 = (Graph) o2;
                return g1.getMcsDistance(g2);
            }
        });

        FastMap fm = new FastMap(matrixFastmap, 2);
        long ms = System.currentTimeMillis();
        fm.run();
        ms = System.currentTimeMillis() - ms;
        System.out.println("Time for " + matrixFastmap.getDimension() + " images: " + ms + " ms");

        JFrame frame = new JFrame("Test FastMap");
        frame.setSize(800, 600);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add(new PointPanel(fm.getPoints(), matrixFastmap));
        frame.setVisible(true);

    }


}
