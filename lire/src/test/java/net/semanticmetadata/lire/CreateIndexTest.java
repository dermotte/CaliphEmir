package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/*
 * This file is part of LIRe.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
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
 * Copyright statement:
 * --------------------
 * (c) 2002-2010 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of LIRe
 * Date: 31.01.2006
 * Time: 23:59:45
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class CreateIndexTest extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG", "error.jpg", "P�ginas de 060305_b_P�gina_1_Imagem_0004_P�gina_08_Imagem_0002.jpg"};
    private String testFilesPath = "./lire/src/test/resources/images/";
    private String indexPath = "test-index";
    private String testExtensive = "C:\\Temp\\images";
//    private String testExtensive = "./lire/wang-data-1000";

    public void testCreateIndex() throws IOException {
        DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath + "-small")), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        for (String identifier : testFiles) {
            System.out.println("Indexing file " + identifier);
            Document doc = builder.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
            iw.addDocument(doc);
        }
        iw.optimize();
        iw.close();
    }

    public void testCreateCorrelogramIndex() throws IOException {
        String[] testFiles = new String[]{"img01.jpg", "img02.jpg", "img03.jpg", "img04.jpg", "img05.jpg", "img06.jpg", "img07.jpg", "img08.jpg", "img09.jpg", "img10.jpg"};
        String testFilesPath = "./lire/src/test/resources/small/";

        DocumentBuilder builder = DocumentBuilderFactory.getDefaultAutoColorCorrelationDocumentBuilder();
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath + "-small")), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        long ms = System.currentTimeMillis();
        for (String identifier : testFiles) {
            Document doc = builder.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
            iw.addDocument(doc);
        }
        System.out.println("Time taken: " + ((System.currentTimeMillis() - ms) / testFiles.length) + " ms");
        iw.optimize();
        iw.close();
    }

    public void testCreateCEDDIndex() throws IOException {
        String[] testFiles = new String[]{"img01.jpg", "img02.jpg", "img03.jpg", "img04.jpg", "img05.jpg", "img06.jpg", "img07.jpg", "img08.jpg", "img09.jpg", "img10.jpg"};
        String testFilesPath = "wang-data-1000";
//        String testFilesPath = "./lire/src/test/resources/small/";
        ArrayList<String> images = FileUtils.getAllImages(new File(testFilesPath), true);
        DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
//        IndexWriter iw = new IndexWriter("wang-cedd", new SimpleAnalyzer(), true);
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File("wang-cedd")), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
//        IndexWriter iw = new IndexWriter(indexPath + "-cedd", new SimpleAnalyzer(), true);
        long ms = System.currentTimeMillis();
        int count = 0;
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            // if (++count>250) break;
        }
        iw.optimize();
        iw.close();
    }

//    public void testCreateExtensiveIndex() throws IOException {
//        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
//        indexFiles(images);
//    }

    public void testCreateBigIndex() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
//        ArrayList<String> images = FileUtils.getAllImages(new File("C:\\Dokumente und Einstellungen\\All Users\\Dokumente\\Eigene Bilder\\2003"), true);
//        System.out.println(">> Fast DocumentBuilder:");
//        indexFiles(images, DocumentBuilderFactory.getFastDocumentBuilder(), indexPath + "-fast");
//        System.out.println(">> Default DocumentBuilder:");
//        indexFiles(images, DocumentBuilderFactory.getDefaultDocumentBuilder(), indexPath + "-default");
//        System.out.println(">> Extensive DocumentBuilder:");
//        indexFiles(images, DocumentBuilderFactory.getDefaultAutoColorCorrelationDocumentBuilder(), indexPath + "-extensive");
        indexFiles(images, DocumentBuilderFactory.getCEDDDocumentBuilder(), indexPath + "-cedd-flickr");
//        indexFiles(images, DocumentBuilderFactory.getFullDocumentBuilder(), indexPath + "-extensive");
    }

    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
//        DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
//        DocumentBuilder builder = DocumentBuilderFactory.getFastDocumentBuilder();
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            try {
                Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
                iw.addDocument(doc);
            } catch (Exception e) {
                System.err.println("Error indexing file: " + identifier + "(" + e.getMessage() + ")");
                // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            count++;
            if (count % 100 == 0) {
                System.out.print(count + " files indexed. ");
                float pct = (float) count / (float) images.size();
                float tmp = (float) (System.currentTimeMillis() - time) / 1000;
                float remain = (tmp / pct) * (1f - pct);
                System.out.println("Remining: <" + ((int) (remain / 60) + 1) + " minutes of <" + ((int) ((tmp / pct) / 60) + 1) + " minutes");
            }
            // if (count == 200) break;
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.optimize();
        iw.close();
    }

    private void indexFilesMultithreaded(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
//        DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
//        DocumentBuilder builder = DocumentBuilderFactory.getFastDocumentBuilder();
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        SynchronizedWriter sw = new SynchronizedWriter(iw);
        ExecutorService pool = Executors.newFixedThreadPool(4);

        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            pool.execute(new IndexingThread(identifier, sw));
            count++;
            if (count % 1000 == 0) System.out.println(count + " files added.");
            // if (count == 200) break;
        }
        while (!pool.isTerminated()) {
            try {
                pool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            System.out.println("indexed: " + iw.maxDoc());
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.optimize();
        iw.close();
    }

    class SynchronizedWriter {
        IndexWriter iw;

        SynchronizedWriter(IndexWriter iw) {
            this.iw = iw;
        }

        public synchronized void addDocument(Document d) throws IOException {
            iw.addDocument(d);
        }
    }

    class IndexingThread implements Runnable {
        DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
        String file = null;
        private SynchronizedWriter synchronizedWriter;

        IndexingThread(String img, SynchronizedWriter sw) {
            this.file = img;
            synchronizedWriter = sw;
        }

        public void run() {
            try {
                Document doc = builder.createDocument(new FileInputStream(file), file);
                synchronizedWriter.addDocument(doc);
            } catch (Exception e) {
                System.err.println("Error indexing file: " + file + "(" + e.getMessage() + ")");
            }
        }
    }
}
