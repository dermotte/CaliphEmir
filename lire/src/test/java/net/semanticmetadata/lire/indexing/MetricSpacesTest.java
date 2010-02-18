package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import javax.imageio.ImageIO;
import java.io.*;

/**
 * User: mlux
 * Date: 14.05.2009
 * Time: 15:07:43
 */
public class MetricSpacesTest extends TestCase {
    String indexPath = "./test-index-cedd-flickr";
    // String imagePath = "";

    public void testIndexing() throws IOException {
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 100;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 1000;
        ms.setProgress(new ProgressIndicator() {
            @Override
            public void setCurrentState(MetricSpacesInvertedListIndexing.State currentState) {
                super.setCurrentState(currentState);
                System.out.println("currentState = " + currentState);
            }

            @Override
            public void setNumDocsProcessed(int numDocsProcessed) {
                super.setNumDocsProcessed(numDocsProcessed);
                if (numDocsProcessed % 1000 == 0) System.out.println("numDocsProcessed = " + numDocsProcessed);
            }
        });
        ms.createIndex(indexPath);
    }

    public void testSearch() throws IOException {
        int docNumber = 1;
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 10;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 1000;
        IndexReader reader = ms.getIndexReader(indexPath);
        TopDocs docs = ms.search(reader.document(docNumber), indexPath);

        // print the results
        BufferedWriter bw = new BufferedWriter(new FileWriter("out.html"));
        bw.write("<html><body>");
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = docs.scoreDocs[i];
            bw.write("<img title=\"ID: " + scoreDoc.doc + ", " +
                    "Score: " + scoreDoc.score + "\" src=\"file:///" + reader.document(scoreDoc.doc).getValues("descriptorImageIdentifier")[0] + "\"> ");
        }
        bw.write("</body></html>");
        bw.close();
        showUrl("out.html");

    }

    public void testPerformance() throws IOException {
        MetricSpacesInvertedListIndexing mes = MetricSpacesInvertedListIndexing.getDefaultInstance();
        int numSearches = 10;
        IndexReader reader = mes.getIndexReader(indexPath);
        System.out.println(reader.maxDoc() + " documents");
        TopDocs docs;

        long ms = System.currentTimeMillis();
        for (int i = 0; i < numSearches; i++) {
            docs = mes.search(reader.document(i), indexPath);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);


        ImageSearcher ceddSearcher = ImageSearcherFactory.createCEDDImageSearcher(100);
        ms = System.currentTimeMillis();
        for (int i = 0; i < numSearches; i++) {
            ceddSearcher.search(reader.document(i), reader);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);
    }

    private void showUrl(String url) {
        String osName = System.getProperty("os.name");
        // take linux settings
        String browserCmd = "firefox {url}";
        // or windows in case of windows :)
        if (osName.toLowerCase().indexOf("windows") > -1) {
            browserCmd = "cmd.exe /c start \"\" \"{url}\"";
        }
        browserCmd = browserCmd.replace("{url}", new File(url).getAbsolutePath());
        try {
            System.out.println("browserCmd = " + browserCmd);
            Runtime.getRuntime().exec(browserCmd);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Test routine for creation and update.
     * @throws IOException
     */
    public void testIndexSmall() throws IOException {
        String smallIdx = "wang-cedd";
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 10;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 50;
        ms.setProgress(new ProgressIndicator() {
            @Override
            public void setCurrentState(MetricSpacesInvertedListIndexing.State currentState) {
                super.setCurrentState(currentState);
                System.out.println("currentState = " + currentState);
            }

            @Override
            public void setNumDocsProcessed(int numDocsProcessed) {
                super.setNumDocsProcessed(numDocsProcessed);
                if (numDocsProcessed % 100 == 0) System.out.println("numDocsProcessed = " + numDocsProcessed);
            }
        });
        ms.createIndex(smallIdx);
    }

    public void testMetrics() throws IOException {
        String smallIdx = "wang-cedd";
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 10;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 50;

        TopDocs docs = ms.search(ImageIO.read(new FileInputStream("./lire/wang-data-1000/0.jpg")), smallIdx);
        int docId = docs.scoreDocs[0].doc;
        System.out.println("docId = " + docId);
        IndexReader ir = IndexReader.open(smallIdx);
        String identifier = ir.document(docId).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
        System.out.println("identifier = " + identifier);
    }
}