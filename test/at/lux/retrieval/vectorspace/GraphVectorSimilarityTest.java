package at.lux.retrieval.vectorspace;

import junit.framework.TestCase;
import at.lux.fotoretrieval.lucene.Graph;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of Caliph & Emir
 * Date: 09.03.2006
 * Time: 22:24:09
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GraphVectorSimilarityTest extends TestCase {
    final static String GRAPH1 = "[9] [15] [26] [30] [31] [32] [locationOf 30 9] [locationOf 9 32] [locationOf 9 31] [locationOf 32 30] [locationOf 9 15] [locationOf 9 26] [timeOf 31 30]";
    final static String GRAPH2 = "[9] [15] [26] [30] [31] [locationOf 30 9] [locationOf 9 31] [locationOf 9 15] [locationOf 9 26] [timeOf 31 30]";
    private Graph g1;
    private Graph g2;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        g1 = new Graph(GRAPH1);
        g2 = new Graph(GRAPH2);
    }

    public void testSimilarity() {
        GraphVectorSimilarity vs = new GraphVectorSimilarity();
        atomicTest(vs);
        vs = new GraphVectorSimilarity(0);
        atomicTest(vs);
        vs = new GraphVectorSimilarity(1);
        atomicTest(vs);
        vs = new GraphVectorSimilarity(2);
        atomicTest(vs);
    }

    private void atomicTest(GraphVectorSimilarity vs) {
        double similarity = vs.getSimilarity(g1, g1);
        assertTrue(similarity==1.0);
        assertTrue(vs.getSimilarity(g2, g2)==1.0);
        assertTrue(vs.getSimilarity(g1, g2)==vs.getSimilarity(g2, g1));
    }
}
