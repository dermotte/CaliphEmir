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
package at.lux.fotoretrieval.lucene;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Date: 25.03.2005
 * Time: 23:40:39
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LabeledGraphTest extends TestCase {
    public void testLabeledGraph() {
        LabeledGraph g = new LabeledGraph(new Graph("[0] [1] [2] [10] [27] [locationOf 1 0] [locationOf 1 10] [locationOf 10 27] [timeOf 2 0]"));
        List<Path> p = g.get2Paths();
        Collections.sort(p);
        for (Iterator<Path> iterator = p.iterator(); iterator.hasNext();) {
            Path path = iterator.next();
            System.out.println(path.toString());
        }
        System.out.println("");
    }

    public void testLabeledGraphFromFile() {
        LabeledGraph g = new LabeledGraph(new Graph("[10] [16] [17] [28] [32] [33] [34] [agentOf 16 17] [agentOf 28 17] [contextFor 32 17] [locationOf 10 16] [locationOf 10 28] [locationOf 32 10] [locationOf 34 32] [timeOf 33 32]"));
        List<Path> p = g.get2Paths();
        Collections.sort(p);
        for (Iterator<Path> iterator = p.iterator(); iterator.hasNext();) {
            Path path = iterator.next();
            System.out.println(path.toString());
        }
    }
}
