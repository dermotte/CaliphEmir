package at.lux.retrieval.graphisomorphism.metrics;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.FileInputStream;
import java.util.List;

import at.lux.fotoretrieval.RetrievalToolkit;
import at.lux.retrieval.graphisomorphism.NodeDistanceFunction;
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
 * Time: 22:35:05
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TermVectorNodeDistanceFunctionTest extends TestCase {
    private static final String FILE_DOC_1 = "testdata/I-Know 02/iknow_001.mp7.xml";
    private static final String FILE_DOC_2 = "testdata/I-Know 02/iknow_002.mp7.xml";
    private Document d1;
    private Document d2;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        SAXBuilder sb = new SAXBuilder();
        d1 = sb.build(new FileInputStream(TermVectorNodeDistanceFunctionTest.FILE_DOC_1));
        d2 = sb.build(new FileInputStream(TermVectorNodeDistanceFunctionTest.FILE_DOC_2));
    }

    public void testGetDistance() {
        List semanticBase = RetrievalToolkit.xpathQuery(d1.getRootElement(), "//Semantic/SemanticBase", null);

        NodeDistanceFunction dist1 = new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.BagOfWords);
        NodeDistanceFunction dist2 = new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.CosinusCoefficient);

        for (Object o1 : semanticBase) {
            Element e1 = (Element) o1;
            for (Object o2 : semanticBase) {
                Element e2 = (Element) o2;
                float distance1 = dist1.getDistance(e1, e2);
                float distance2 = dist2.getDistance(e1, e2);
                System.out.println("BagOfWords, Cosinus = " + distance1 + ", " + distance2);
            }
        }

    }
}
