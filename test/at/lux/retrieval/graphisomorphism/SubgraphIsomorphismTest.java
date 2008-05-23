package at.lux.retrieval.graphisomorphism;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.io.FileInputStream;

import at.lux.retrieval.graphisomorphism.metrics.BooleanNodeDistanceFunction;
import at.lux.retrieval.graphisomorphism.metrics.SimpleEdgeDistanceFunction;
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
 * Date: 16.02.2006
 * Time: 22:07:23
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SubgraphIsomorphismTest extends TestCase {
    private static final String FILE_DOC_1 = "testdata/I-Know 02/iknow_001.mp7.xml";
    private static final String FILE_DOC_2 = "testdata/I-Know 02/iknow_002.mp7.xml";
    private static final String FILE_DOC_3 = "testdata/I-Know 02/iknow_003.mp7.xml";
    private static final String FILE_DOC_4 = "testdata/I-Know 02/iknow_004.mp7.xml";
    private static final String FILE_DOC_5 = "testdata/I-Know 02/iknow_005.mp7.xml";
    private Document d1, d2, d3, d4, d5;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        SAXBuilder sb = new SAXBuilder();
        d1 = sb.build(new FileInputStream(FILE_DOC_1));
        d2 = sb.build(new FileInputStream(FILE_DOC_2));
        d3 = sb.build(new FileInputStream(FILE_DOC_3));
        d4 = sb.build(new FileInputStream(FILE_DOC_4));
        d5 = sb.build(new FileInputStream(FILE_DOC_5));
    }

    public void testGetDistance() {
        SubgraphIsomorphism i = new SubgraphIsomorphism(new BooleanNodeDistanceFunction(), new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Allow));
        float distance1 = i.getDistance(d1, d1);
        System.out.println("distance1 = " + distance1);
        float distance2 = i.getDistance(d2, d2);
        System.out.println("distance2 = " + distance2);
        float distance3 = i.getDistance(d1, d2);
        System.out.println("distance3 = " + distance3);
        float distance4 = i.getDistance(d2, d1);
        System.out.println("distance4 = " + distance4);
//        float distance2 = SubgraphIsomorphism.getSimilarity(d2, d2);
//        float distance3 = SubgraphIsomorphism.getSimilarity(d1, d2);
//        float distance4 = SubgraphIsomorphism.getSimilarity(d2, d1);
//
        assertTrue("distance 1 should be 0", distance1 == 0);
        assertTrue("distance 2 should be 0", distance2 == 0);
        assertTrue("distance 3 should be > 0", distance3 > 0);
        assertTrue("distance 4 should be > 0", distance4 > 0);
        assertTrue("distance 3 should equal to distance 4", distance3 == distance4);

        distance1 = i.getDistance(d3, d5);
        System.out.println("d3-d5:" + distance1);
    }
}
