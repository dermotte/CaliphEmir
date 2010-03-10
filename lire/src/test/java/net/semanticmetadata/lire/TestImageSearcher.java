package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.impl.DocumentFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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
 * Date: 03.02.2006
 * Time: 00:29:56
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TestImageSearcher extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG"};
    private String testFilesPath = "./lire/src/test/resources/images/";
    private String indexPath = "test-index-extensive";
    private int numsearches = 5;

    public void testSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(bimg, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(0);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

        document = DocumentBuilderFactory.getFastDocumentBuilder().createDocument(bimg, testFilesPath + testFiles[0]);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
    }

    public void testSearchWithDuplicates() throws Exception {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        hits = searcher.search(bimg, reader);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
    }

    public void testFindDuplicates() throws Exception {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
        ImageDuplicates imageDuplicates = searcher.findDuplicates(reader);
        if (imageDuplicates == null) {
            System.out.println("No duplicates found");
            return;
        }
        for (int i = 0; i < imageDuplicates.length(); i++) {
            System.out.println(imageDuplicates.getDuplicate(i).toString());
        }
    }

    /**
     * Tests the search for color ...
     * Please note that the index has to be built with a DocumentBuilder
     * obtained by DocumentBuilderFactory.getExtensiveDocumentBuilder() or
     * DocumentBuilderFactory.getColorOnlyDocumentBuilder().
     *
     * @throws Exception in case of exceptions *gg*
     */
    public void testColorSearch() throws Exception {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        // PLEASE NOTE: The index has to be built with a DocumentBuilder
        // obtained by DocumentBuilderFactory.getExtensiveDocumentBuilder() or
        // DocumentBuilderFactory.getColorOnlyDocumentBuilder(). Otherwise no color
        // histogram will be included.
        ImageSearcher searcher = ImageSearcherFactory.createColorOnlySearcher(15);
        Document document = DocumentFactory.createColorOnlyDocument(Color.green);
        ImageSearchHits hits = searcher.search(document, reader);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        System.out.println("");
        document = DocumentFactory.createColorOnlyDocument(Color.red);
        hits = searcher.search(document, reader);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
    }

    public void testCorrelationSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createDefaultCorrelogramImageSearcher(10);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
//        for (int i = 0; i < numsearches; i++) {
        hits = searcher.search(bimg, reader);
//        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testCEDDSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(30);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
//        for (int i = 0; i < numsearches; i++) {
        hits = searcher.search(bimg, reader);
//        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testSimpleColorHistogramSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createColorHistogramImageSearcher(30);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
//        for (int i = 0; i < numsearches; i++) {
        hits = searcher.search(bimg, reader);
//        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }
}
