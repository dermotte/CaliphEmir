/*
 * This file is part of Lire (Lucene Image Retrieval).
 *
 * Lire is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Lire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lire; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2008 by Mathias Lux (mathias@juggle.at)
 * http://www.semanticmetadata.net
 */
package net.semanticmetadata.lire.indexing.fastmap;

import at.lux.imageanalysis.ColorLayoutImpl;
import at.lux.imageanalysis.EdgeHistogramImplementation;
import at.lux.imageanalysis.ScalableColorImpl;
import at.lux.imageanalysis.VisualDescriptor;
import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import org.apache.lucene.document.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Test class for FastMap. Trying to map some images ...
 * User: Mathias Lux
 * Date: 21.04.2008
 * Time: 11:14:03
 */
public class FastMapTest extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG", "error.jpg"};
    private String testFilesPath = "./src/test/resources/images/";
    private String indexPath = "test-index";
    private String testExtensive = "../Caliph/testdata";
    private DocumentBuilder db;
    private LinkedList<Document> docs;


    public void setUp() {
        // creating all descriptors ..
        db = DocumentBuilderFactory.getFullDocumentBuilder();
        docs = new LinkedList<Document>();
        for (String file : testFiles) {
            try {
                docs.add(db.createDocument(new FileInputStream(testFilesPath + file), file));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }

    public void testColorLayoutFastMap() {
        // creating the list of user objects ...
        LinkedList<VisualDescriptor> objs = new LinkedList<VisualDescriptor>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext();) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_COLORLAYOUT);
            if (cls.length > 0) {
                objs.add(new ColorLayoutImpl(cls[0]));
            }
        }
        System.out.println("--------------- < COLORLAYOUT > ---------------");
        long nano = System.nanoTime();
        createFastMapForObjects(objs);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000 * 1000)) + " s");
    }

    public void testScalableColorFastMap() {
        // creating the list of user objects ...
        LinkedList<VisualDescriptor> objs = new LinkedList<VisualDescriptor>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext();) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
            if (cls.length > 0) {
                objs.add(new ScalableColorImpl(cls[0]));
            }
        }
        System.out.println("--------------- < ScalableColor > ---------------");
        long nano = System.nanoTime();
        createFastMapForObjects(objs);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000)) + " ms");
    }

    public void testEdgeHistogramFastMap() {
        // creating the list of user objects ...
        LinkedList<VisualDescriptor> objs = new LinkedList<VisualDescriptor>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext();) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
            if (cls.length > 0) {
                objs.add(new EdgeHistogramImplementation(cls[0]));
            }
        }
        System.out.println("--------------- < EdgeHistogram > ---------------");
        long nano = System.nanoTime();
        createFastMapForObjects(objs);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000)) + " ms");
    }

    public void testAutoColorCorrelogramFastMap() {
        // creating the list of user objects ...
        LinkedList<VisualDescriptor> objs = new LinkedList<VisualDescriptor>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext();) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
            if (cls.length > 0) {
                AutoColorCorrelogram acc = new AutoColorCorrelogram();
                acc.setStringRepresentation(cls[0]);
                objs.add(acc);
            }
        }
        System.out.println("--------------- < AutoColorCorrelogram > ---------------");
        long nano = System.nanoTime();
        createFastMapForObjects(objs);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000)) + " ms");
    }

    private void createFastMapForObjects(LinkedList<VisualDescriptor> objs) {
        ArrayFastmapDistanceMatrix fdm = new ArrayFastmapDistanceMatrix(objs, new VisualDescriptorDistanceCalculator());
        // note that fastmap needs at least dimensions*2 objects as it needs enough pivots :)
        FastMap fm = new FastMap(fdm, 4);
        fm.run();
        for (int i = 0; i < fm.getPoints().length; i++) {
            double[] pts = fm.getPoints()[i];
            System.out.print("Obj " + i + ": ( ");
            for (int j = 0; j < pts.length; j++) {
                System.out.print(pts[j] + " ");
            }
            System.out.println(")");
        }
    }
}
