package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.JCD;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.CorrelogramDocumentBuilder;
import net.semanticmetadata.lire.impl.SiftLocalFeatureHistogramImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ...
 * Date: 18.09.2008
 * Time: 12:09:17
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TestWang extends TestCase {
    private String indexPath = "wang-index";
    private String testExtensive = "./lire/wang-data-1000";
    private ChainedDocumentBuilder builder;
    private int[] sampleQueries = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10, 607, 165, 343, 973, 591, 659, 812, 231, 261, 224, 227, 914, 427, 810, 979, 716, 253, 708, 751, 269, 531, 699, 835, 370, 642, 504, 297, 970, 929, 20, 669, 434, 201, 9, 575, 631, 730, 7, 546, 816, 431, 235, 289, 111, 862, 184, 857, 624, 323, 393, 465, 905, 581, 626, 212, 459, 722, 322, 584, 540, 194, 704, 410, 267, 349, 371, 909, 403, 724, 573, 539, 812, 831, 600, 667, 672, 454, 873, 452, 48, 322, 424, 952, 277, 565, 388, 149, 966, 524, 36, 528, 75, 337, 655, 836, 698, 230, 259, 897, 652, 590, 757, 673, 937, 676, 650, 297, 434, 358, 789, 484, 975, 318, 12, 506, 38, 979, 732, 957, 904, 852, 635, 620, 28, 59, 732, 84, 788, 562, 913, 173, 508, 32, 16, 882, 847, 320, 185, 268, 230, 259, 931, 653, 968, 838, 906, 596, 140, 880, 847, 297, 77, 983, 536, 494, 530, 870, 922, 467, 186, 254, 727, 439, 241, 12, 947, 561, 160, 740, 705, 619, 571, 745, 774, 845, 507, 156, 936, 473, 830, 88, 66, 204, 737, 770, 445, 358, 707, 95, 349};

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        // Setting up DocumentBuilder:
        builder = new ChainedDocumentBuilder();
//        builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        builder.addBuilder(new SimpleDocumentBuilder(true, true, true));
//        builder.addBuilder(new SiftDocumentBuilder());
        builder.addBuilder(new CorrelogramDocumentBuilder(AutoColorCorrelogram.Mode.SuperFast));
    }

    public void testIndexWang() throws IOException {
        // indexing
        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        indexFiles(images, builder, indexPath);
        // in case of sift ...
//        SiftFeatureHistogramBuilder sh = new SiftFeatureHistogramBuilder(IndexReader.open(indexPath));
//        sh.index();
        System.out.println("-< Indexing finished >--------------");
    }

    public void testSiftLocalFeatureHistogram() {

    }

    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
//        DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
//        DocumentBuilder builder = DocumentBuilderFactory.getFastDocumentBuilder();
        IndexWriter iw = new IndexWriter(indexPath, new SimpleAnalyzer(), true);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 100 == 0) System.out.println(count + " files indexed.");
//            if (count == 200) break;
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.optimize();
        iw.close();
    }

    public void testMAP() throws IOException {
        int maxSearches = 200;
        int maxHits = 100;
        IndexReader reader = IndexReader.open(indexPath);
        ImageSearcher searcher;
//        searcher = new SimpleImageSearcher(maxHits, 0f, 1f, 0f);
//        searcher = ImageSearcherFactory.createColorHistogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createCEDDImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createFCTHImageSearcher(maxHits);
        searcher = ImageSearcherFactory.createFastCorrelogramImageSearcher(maxHits);
        Pattern p = Pattern.compile("\\\\\\d+\\.jpg");
        double map = 0;
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
            System.out.print("id = " + id + ": ");
            String file = testExtensive + "/" + id + ".jpg";
//            Document document = builder.createDocument(new FileInputStream(file), file);
            ImageSearchHits hits = searcher.search(findDoc(reader, id + ".jpg"), reader);
            int goodOnes = 0;
            double avgPrecision = 0;
            for (int j = 0; j < hits.length(); j++) {
                Document d = hits.doc(j);
                String hitsId = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                Matcher matcher = p.matcher(hitsId);
                if (matcher.find())
                    hitsId = hitsId.substring(matcher.start() + 1, hitsId.lastIndexOf("."));
                else
                    fail("Did not get the number ...");
                int testID = Integer.parseInt(hitsId);
                if ((int) Math.floor(id / 100) == (int) Math.floor(testID / 100)) {
                    goodOnes++;
                    System.out.print("x");
                } else {
                    System.out.print("o");
                }
//                System.out.print(" (" + testID + ") ");
                avgPrecision += (double) goodOnes / (double) (j + 1);
            }
            avgPrecision = avgPrecision / hits.length();
            map += avgPrecision;
            System.out.println(" " + avgPrecision + " (" + map / (i + 1) + ")");
        }
        map = map / sampleQueries.length;
        System.out.println("map = " + map);
    }

    public void testMAPLocalFeatureHistogram() throws IOException {
        int maxSearches = 200;
        int maxHits = 100;
        IndexReader reader = IndexReader.open(indexPath);
        IndexSearcher is = new IndexSearcher(reader);
        ImageSearcher searcher;
        searcher = new SiftLocalFeatureHistogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createColorHistogramImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createCEDDImageSearcher(maxHits);
//        searcher = ImageSearcherFactory.createFCTHImageSearcher(maxHits);
        Pattern p = Pattern.compile("\\\\\\d+\\.jpg");
        double map = 0;
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
            System.out.print("id = " + id + ": ");
            String file = testExtensive + "/" + id + ".jpg";

            ImageSearchHits hits = searcher.search(findDoc(reader, id + ".jpg"), reader);
            int goodOnes = 0;
            double avgPrecision = 0;
            for (int j = 0; j < hits.length(); j++) {
                Document d = hits.doc(j);
                String hitsId = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                Matcher matcher = p.matcher(hitsId);
                if (matcher.find())
                    hitsId = hitsId.substring(matcher.start() + 1, hitsId.lastIndexOf("."));
                else
                    fail("Did not get the number ...");
                int testID = Integer.parseInt(hitsId);
//                System.out.print(". " + hitsId + "/"  + d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]+ " ");
                if ((int) Math.floor(id / 100) == (int) Math.floor(testID / 100)) {
                    goodOnes++;
                    System.out.print("x");
                } else {
                    System.out.print("o");
                }
//                System.out.print(" (" + testID + ") ");
                avgPrecision += (double) goodOnes / (double) (j + 1);
            }
            avgPrecision = avgPrecision / hits.length();
            map += avgPrecision;
            System.out.println(" " + avgPrecision + " (" + map / (i + 1) + ")");
        }
        map = map / sampleQueries.length;
        System.out.println("map = " + map);
    }

    private Document findDoc(IndexReader reader, String file) throws IOException {
        for (int i = 0; i < reader.numDocs(); i++) {
            Document document = reader.document(i);
            String s = document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            if (s.endsWith("\\" + file)) {
//                System.out.println("s = " + s);
                return document;
            }
        }
        return null;
    }

    public void testGetDistribution() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("data.csv"));
        IndexReader reader = IndexReader.open(indexPath);
        // get the first document:
        if (!IndexReader.indexExists(reader.directory()))
            throw new FileNotFoundException("No index found at this specific location.");

        CEDD cedd1 = new CEDD();
        FCTH fcth1 = new FCTH();

        CEDD cedd2 = new CEDD();
        FCTH fcth2 = new FCTH();

        JCD jcd1 = new JCD();
        JCD jcd2 = new JCD();
        String[] cls;


        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            if (reader.isDeleted(i)) {
                continue;
            }
            Document doc = reader.document(i);
            cls = doc.getValues(DocumentBuilder.FIELD_NAME_CEDD);
            if (cls != null && cls.length > 0)
                cedd1.setStringRepresentation(cls[0]);
            cls = doc.getValues(DocumentBuilder.FIELD_NAME_FCTH);
            if (cls != null && cls.length > 0)
                fcth1.setStringRepresentation(cls[0]);

            for (int j = i + 1; j < docs; j++) {
                if (reader.isDeleted(j)) {
                    continue;
                }
                Document doc2 = reader.document(j);
                cls = doc2.getValues(DocumentBuilder.FIELD_NAME_CEDD);
                if (cls != null && cls.length > 0)
                    cedd2.setStringRepresentation(cls[0]);
                cls = doc2.getValues(DocumentBuilder.FIELD_NAME_FCTH);
                if (cls != null && cls.length > 0)
                    fcth2.setStringRepresentation(cls[0]);
                jcd1.init(cedd1, fcth1);
                jcd2.init(cedd2, fcth2);
                bw.write(cedd1.getDistance(cedd2) + ";" + fcth1.getDistance(fcth2) + ";" + jcd1.getDistance(jcd2) + "\n");
            }
            if (i % 100 == 0) System.out.println(i + " entries processed ... ");
        }
        bw.close();
    }

    public void testGetSampleQueries() {
        for (int i = 0; i < 200; i++) {
            System.out.print((int) (Math.random() * 1000) + ", ");
        }
    }

}
