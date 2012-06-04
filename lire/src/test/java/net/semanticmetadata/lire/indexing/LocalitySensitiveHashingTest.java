package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ...
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 04.06.12, 14:19
 */
public class LocalitySensitiveHashingTest extends TestCase{
    String testExtensive = "./wang-1000";
    private String indexPath = "index-hashed";

    public void testIndexing() throws IOException {
        LocalitySensitiveHashing.generateHashFunctions();
        LocalitySensitiveHashing.readHashFunctions();
        DocumentBuilder builder = new ChainedDocumentBuilder();
        ((ChainedDocumentBuilder) builder).addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());

        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            CEDD cedd = new CEDD();
            cedd.extract(ImageIO.read(new FileInputStream(identifier)));
            Document doc = new Document();
            doc.add(new Field(DocumentBuilder.FIELD_NAME_CEDD, cedd.getByteArrayRepresentation()));
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
            int[] hashes = LocalitySensitiveHashing.generateHashes(cedd.getDoubleHistogram());
            StringBuilder hash = new StringBuilder(512);
            for (int i = 0; i < hashes.length; i++) {
                hash.append(hashes[i]);
                hash.append(' ');
            }
            System.out.println("hash = " + hash);
            doc.add(new Field("hash", hash.toString(), Field.Store.YES, Field.Index.ANALYZED));
            iw.addDocument(doc);
            count++;
            if (count % 100 == 0) System.out.println(count + " files indexed.");
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.optimize();
        iw.close();


    }
}
