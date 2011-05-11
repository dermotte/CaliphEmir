package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.CEDDDocumentBuilder;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main test class for testing algorithms on the PASCAL VOC 2007 data set,
 * which is available at http://pascallin.ecs.soton.ac.uk/challenges/VOC/voc2007/index.html#testdata
 * Created: 11.05.2011 09:04:25
 */
public class TestPascalVOC2007 extends TestCase {
    private ChainedDocumentBuilder builder;
    private String testExtensive = "VOC2007/JPEGImages";
    private String indexPath = "./pascal-map-test";

    public void setUp() {
        // Setting up DocumentBuilder:
        builder = new ChainedDocumentBuilder();
        builder.addBuilder(new CEDDDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
//        builder.addBuilder(new GenericDocumentBuilder(FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST"));
//        builder.addBuilder(new GenericDocumentBuilder(JpegCoefficientHistogram.class, "FIELD_JPEGCOEFFHIST"));
//        builder.addBuilder(new SimpleDocumentBuilder(true, true, true));
//        builder.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getDefaultAutoColorCorrelationDocumentBuilder());

          // ----- local features ------------------
//        builder.addBuilder(new SiftDocumentBuilder());
//        builder.addBuilder(new SurfDocumentBuilder());
//        builder.addBuilder(new MSERDocumentBuilder());
    }

    public void testIndex() throws IOException {
        // indexing
        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        indexFiles(images, builder, indexPath);

//        in case of "bag of visual words" ...
//        SiftFeatureHistogramBuilder sh1 = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh1.index();
//        SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh.index();
//        MSERFeatureHistogramBuilder sh = new MSERFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh.index();

        System.out.println("-< Indexing finished >--------------");

    }

    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
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

    public void testMAP() {
        computeMAP(ImageSearcherFactory.createColorHistogramImageSearcher(4952), "Color Histogram - JSD");
//        computeMAP(new SiftVisualWordsImageSearcher(4952), "Sift BoVW");

    }

    private void computeMAP(ImageSearcher imageSearcher, String prefix) {
        //To change body of created methods use File | Settings | File Templates.
    }

}
