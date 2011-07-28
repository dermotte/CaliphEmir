/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This file is part of Caliph & Emir
 * Date: 03.02.2006
 * Time: 00:29:56
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TestImageSearcherWeighted extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG"};
    private String testFilesPath = "../Lire/src/test/resources/images/";
    private String indexPath = "test-index-extensive";
    private int numsearches = 5;

    public void testSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher1 = ImageSearcherFactory.createWeightedSearcher(10, 0.2f, 0.8f, 1.0f);
        ImageSearcher searcher2 = ImageSearcherFactory.createWeightedSearcher(10, 0.8f, 0.0f, 1.0f);
        ImageSearcher searcher3 = ImageSearcherFactory.createWeightedSearcher(10, 0.0f, 1.0f, 0.0f);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        hits = searcher1.search(bimg, reader);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        System.out.println("-");
        hits = searcher2.search(bimg, reader);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        System.out.println("-");
        hits = searcher3.search(bimg, reader);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
    }
}
