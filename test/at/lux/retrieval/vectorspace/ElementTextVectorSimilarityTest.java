package at.lux.retrieval.vectorspace;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Field;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
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
 * Date: 16.03.2006
 * Time: 22:03:46
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ElementTextVectorSimilarityTest extends TestCase {
    Document d1, d2;
    String doc1 = "testdata/I-Know 02/iknow_008.mp7.xml";
    String doc2 = "testdata/I-Know 02/iknow_010.mp7.xml";
    private SAXBuilder saxBuilder;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        saxBuilder = new SAXBuilder();
        d1 = saxBuilder.build(new File(doc1));
        d2 = saxBuilder.build(new File(doc2));
    }

    public void testSimilarity() throws IOException, JDOMException {
        ElementTextVectorSimilarity sim = new ElementTextVectorSimilarity();
        double distance = sim.getSimilarity(d1, d1);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d1, d2);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d2, d1);
        System.out.println("distance = " + distance);

        IndexReader reader = IndexReader.open("testdata/idx_paths");

        System.out.println("Loading documents and adding them to corpus ...");
        for (int i = 0; i < reader.numDocs(); i++) {
//            Graph g_idx = new Graph(reader.document(i).getField("graph").stringValue());
            Field[] files = reader.document(i).getFields("file");
            for (Field file : files) {
                Document d = saxBuilder.build(file.stringValue());
                sim.addToCorpus(d);
            }
        }

        System.out.println("");

        distance = sim.getSimilarity(d1, d1, ElementTextVectorSimilarity.WeightType.TfIdf);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.TfIdf);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d2, d1, ElementTextVectorSimilarity.WeightType.TfIdf);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d2, d2, ElementTextVectorSimilarity.WeightType.TfIdf);
        System.out.println("distance = " + distance);

        System.out.println("");

        distance = sim.getSimilarity(d1, d1, ElementTextVectorSimilarity.WeightType.BM25);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.BM25);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d2, d1, ElementTextVectorSimilarity.WeightType.BM25);
        System.out.println("distance = " + distance);
        distance = sim.getSimilarity(d2, d2, ElementTextVectorSimilarity.WeightType.BM25);
        System.out.println("distance = " + distance);

    }

}
