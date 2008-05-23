package at.lux.retrieval.graphisomorphism;

import at.lux.retrieval.graphisomorphism.metrics.SimpleEdgeDistanceFunction;
import at.lux.retrieval.graphisomorphism.metrics.TermVectorNodeDistanceFunction;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.io.FileInputStream;
import java.util.ArrayList;
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
public class FastSubgraphIsomorphismTest extends TestCase {
    private static final String FILE_DOC_1 = "testdata/I-Know 02/iknow_001.mp7.xml";
    private static final String FILE_DOC_2 = "testdata/I-Know 02/iknow_002.mp7.xml";
    private static final String FILE_DOC_3 = "testdata/I-Know 02/iknow_003.mp7.xml";
    private static final String FILE_DOC_4 = "testdata/I-Know 02/iknow_004.mp7.xml";
    private static final String FILE_DOC_5 = "testdata/I-Know 02/iknow_005.mp7.xml";
    private static final String FILE_DOC_6 = "testdata/I-Know 02/iknow_006.mp7.xml";
    private static final String FILE_DOC_7 = "testdata/I-Know 02/iknow_007.mp7.xml";
    private static final String FILE_DOC_8 = "testdata/I-Know 02/iknow_008.mp7.xml";
    private static final String FILE_DOC_9 = "testdata/I-Know 02/iknow_009.mp7.xml";

    private Document d1, d2, d3, d4, d5;

    private ArrayList<Document> docs = new ArrayList<Document>(10);

    private long overallTime = 0;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        SAXBuilder sb = new SAXBuilder();
        d1 = sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_1));
        d2 = sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_2));
        d3 = sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_3));
        d4 = sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_4));
        d5 = sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_5));

        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        docs.add(d4);
        docs.add(d5);
//        docs.add(sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_6)));
//        docs.add(sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_7)));
//        docs.add(sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_8)));
//        docs.add(sb.build(new FileInputStream(FastSubgraphIsomorphismTest.FILE_DOC_9)));
    }

    public void testGetDistance() {
        FastSubgraphIsomorphism i = new FastSubgraphIsomorphism(new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.CosinusCoefficient), new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Allow));
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

        for (Document doc1 : docs) {
            for (Document doc2 : docs) {
                getDistance(i, doc1, doc2, docs.indexOf(doc1) + "<->" +
                        docs.indexOf(doc2));
            }
        }
        System.out.println("overallTime = " + overallTime);

//        getSimilarity(i, d1, d5, "d1 <-> d5");
//        getSimilarity(i, d2, d5, "d2 <-> d5");
//        getSimilarity(i, d3, d5, "d3 <-> d5");
//        getSimilarity(i, d4, d5, "d4 <-> d5");
//        getSimilarity(i, d5, d5, "d5 <-> d5");
    }

    private float getDistance(FastSubgraphIsomorphism i, Document d1, Document d2, String label) {
        float distance1;
        long time = System.currentTimeMillis();
        distance1 = i.getDistance(d1, d2);
        time = System.currentTimeMillis() - time;
        overallTime += time;
        System.out.println(label + ": " + distance1 + " " + time + " ms");
        return distance1;
    }
}
