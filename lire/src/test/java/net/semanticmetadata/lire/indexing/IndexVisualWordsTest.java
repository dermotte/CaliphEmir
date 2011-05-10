package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.MSERFeatureHistogramBuilder;
import net.semanticmetadata.lire.imageanalysis.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.imageanalysis.sift.SiftFeatureHistogramBuilder;
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
import java.util.List;

/**
 * User: mlux
 * Date: 03.05.2011
 * Time: 10:28:16
 */
public class IndexVisualWordsTest extends TestCase {
    public void testIndexSurfHistogram() throws IOException {
        String directory = "./imageCLEF2011"; // Where are the photos?
        String index = "./imageClefIndex"; // Where is the index?
        int clusters = 2000; // number of visual words
        int numSamples = 2000; // number of samples used for visual words vocabulary building

        // index all files
        System.out.println("-< Getting files to index >--------------");
        List<String> images = FileUtils.getAllImages(new File(directory), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        indexFiles(images, index);

        // create histograms
        System.out.println("-< Creating SIFT based histograms >--------------");
        SiftFeatureHistogramBuilder siftFeatureHistogramBuilder = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(index))), numSamples, clusters);
        siftFeatureHistogramBuilder.index();
        System.out.println("-< Creating SURF based histograms >--------------");
        SurfFeatureHistogramBuilder surfFeatureHistogramBuilder = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(index))), numSamples, clusters);
        surfFeatureHistogramBuilder.index();
        System.out.println("-< Creating MSER based histograms >--------------");
        MSERFeatureHistogramBuilder mserFeatureHistogramBuilder = new MSERFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(index))), numSamples, clusters);
        mserFeatureHistogramBuilder.index();
        System.out.println("-< Finished >--------------");

    }

    private void indexFiles(List<String> images, String index) throws IOException {
        ChainedDocumentBuilder documentBuilder = new ChainedDocumentBuilder();
        documentBuilder.addBuilder(new CEDDDocumentBuilder());
        documentBuilder.addBuilder(new SurfDocumentBuilder());
        documentBuilder.addBuilder(new MSERDocumentBuilder());
        documentBuilder.addBuilder(new SiftDocumentBuilder());

//        documentBuilder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        documentBuilder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
//        documentBuilder.addBuilder(new SimpleDocumentBuilder(false, false, true));
//        documentBuilder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//        documentBuilder.addBuilder(DocumentBuilderFactory.getDefaultAutoColorCorrelationDocumentBuilder());


        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(index)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            Document doc = documentBuilder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 500 == 0) System.out.println(count + " files indexed.");
//            if (count == 500) break;
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.optimize();
        iw.close();

    }
}
