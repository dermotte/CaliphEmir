package at.lux.retrieval.graphisomorphism.metrics;

import at.lux.fotoretrieval.RetrievalToolkit;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import junit.framework.TestCase;

import java.io.FileInputStream;
import java.util.List;
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
 * Time: 22:34:31
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleEdgeDistanceFunctionTest extends TestCase {
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
        d1 = sb.build(new FileInputStream(FILE_DOC_1));
        d2 = sb.build(new FileInputStream(FILE_DOC_2));
    }

    public void testGetDistance() {
        SimpleEdgeDistanceFunction dist = new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Allow);
        List relations = RetrievalToolkit.xpathQuery(d1.getRootElement(), "//Semantic/Graph/Relation", null);
        System.out.println("Allowing edge inversion.");
        for (Object obj : relations) {
            Element relation = (Element) obj;
            for (Object o : relations) {
                Element e = (Element) o;
                float distance = dist.getDistance(relation, e);
                System.out.println(distance + ": " + e.getAttributeValue("type").substring(e.getAttributeValue("type").lastIndexOf(':')+1) + " <-> " + relation.getAttributeValue("type").substring(relation.getAttributeValue("type").lastIndexOf(':')+1));
            }
        }
        System.out.println("No edge inversion.");
        dist = new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Forbid);
        for (Object obj : relations) {
            Element relation = (Element) obj;
            for (Object o : relations) {
                Element e = (Element) o;
                float distance = dist.getDistance(relation, e);
                System.out.println(distance + ": " + e.getAttributeValue("type").substring(e.getAttributeValue("type").lastIndexOf(':')+1) + " <-> " + relation.getAttributeValue("type").substring(relation.getAttributeValue("type").lastIndexOf(':')+1));
            }
        }
        System.out.println("Weighted edge inversion.");
        dist = new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.WeightInverted);
        for (Object obj : relations) {
            Element relation = (Element) obj;
            for (Object o : relations) {
                Element e = (Element) o;
                float distance = dist.getDistance(relation, e);
                System.out.println(distance + ": " + e.getAttributeValue("type").substring(e.getAttributeValue("type").lastIndexOf(':')+1) + " <-> " + relation.getAttributeValue("type").substring(relation.getAttributeValue("type").lastIndexOf(':')+1));
            }
        }
    }
}
