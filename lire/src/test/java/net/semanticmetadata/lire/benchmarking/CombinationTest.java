package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.*;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mlux
 * Date: 30.06.2011
 * Time: 15:18:41
 * To change this template use File | Settings | File Templates.
 */
public class CombinationTest extends TestCase {
    String indexPath = "combi-index";
    String dataPath = "wang-1000";
        private int[] sampleQueries = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10, 607, 165, 343, 973, 591, 659, 812, 231, 261, 224, 227, 914, 427, 810, 979, 716, 253, 708, 751, 269, 531, 699, 835, 370, 642, 504, 297, 970, 929, 20, 669, 434, 201, 9, 575, 631, 730, 7, 546, 816, 431, 235, 289, 111, 862, 184, 857, 624, 323, 393, 465, 905, 581, 626, 212, 459, 722, 322, 584, 540, 194, 704, 410, 267, 349, 371, 909, 403, 724, 573, 539, 812, 831, 600, 667, 672, 454, 873, 452, 48, 322, 424, 952, 277, 565, 388, 149, 966, 524, 36, 528, 75, 337, 655, 836, 698, 230, 259, 897, 652, 590, 757, 673, 937, 676, 650, 297, 434, 358, 789, 484, 975, 318, 12, 506, 38, 979, 732, 957, 904, 852, 635, 620, 28, 59, 732, 84, 788, 562, 913, 173, 508, 32, 16, 882, 847, 320, 185, 268, 230, 259, 931, 653, 968, 838, 906, 596, 140, 880, 847, 297, 77, 983, 536, 494, 530, 870, 922, 467, 186, 254, 727, 439, 241, 12, 947, 561, 160, 740, 705, 619, 571, 745, 774, 845, 507, 156, 936, 473, 830, 88, 66, 204, 737, 770, 445, 358, 707, 95, 349};

    public void testIndexing() throws IOException {
        ChainedDocumentBuilder cb = new ChainedDocumentBuilder();
        cb.addBuilder(new SurfDocumentBuilder());
        cb.addBuilder(new ColorLayoutDocumentBuilder());

        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(dataPath), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");

        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            Document doc = cb.createDocument(new FileInputStream(identifier), identifier);
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

        System.out.println("-< Local features are getting clustered >--------------");

        SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
        sh.index();

        System.out.println("-< Indexing finished >--------------");


    }

    public void testSearch() throws IOException {
        int numHitsFiltering = 50;
        IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        float eval = 0f;
        for (int i = 0; i < sampleQueries.length; i++) {
            Document query = indexReader.document(sampleQueries[i]);
            eval += getResults(numHitsFiltering, indexReader, query);
        }
        eval = eval / (float) sampleQueries.length;
        System.out.println("precision @ ten = " + eval);
    }

    private float getResults(int numHitsFiltering, IndexReader indexReader, Document query) throws IOException {
        SurfVisualWordsImageSearcher searcher = new SurfVisualWordsImageSearcher(numHitsFiltering);

        ColorLayout clQuery = new ColorLayout();
        clQuery.setByteArrayRepresentation(query.getBinaryValue(DocumentBuilder.FIELD_NAME_COLORLAYOUT_FAST));
        ImageSearchHits searchHits = searcher.search(query, indexReader);

        // ---- ranking:
        ArrayList<SimpleResult> results = new ArrayList<SimpleResult>(numHitsFiltering);
        for (int i = 0; i < searchHits.length(); i++) {
            Document d = searchHits.doc(i);
            ColorLayout cl = new ColorLayout();
            cl.setByteArrayRepresentation(d.getBinaryValue(DocumentBuilder.FIELD_NAME_COLORLAYOUT_FAST));
            float distance = clQuery.getDistance(cl);
            SimpleResult s = new SimpleResult(distance, d);
            results.add(s);
        }
        Collections.sort(results);

        int count = 0;
        int goodOnes = 0;
        String queryFile = query.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].split("\\\\")[5].replace(".jpg", "");
        int queryClass = Integer.parseInt(queryFile) / 100;

        for (Iterator<SimpleResult> simpleResultIterator = results.iterator(); simpleResultIterator.hasNext();) {
            SimpleResult simpleResult = simpleResultIterator.next();
            String file = simpleResult.getDocument().getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].split("\\\\")[5].replace(".jpg", "");
            int wangClass = Integer.parseInt(file) / 100;
//            System.out.println(simpleResult.getDistance() + " -- " + file + "(" + wangClass + ")");
            if (!(simpleResult.getDistance()<=0f) && count < 10) {
                if (wangClass == queryClass) goodOnes++;
                count++;
            }
        }
        return (float)goodOnes/10f;
    }
}
