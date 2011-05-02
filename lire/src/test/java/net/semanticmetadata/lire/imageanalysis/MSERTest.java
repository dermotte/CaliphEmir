package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.mser.MSERFeature;
import net.semanticmetadata.lire.impl.MSERDocumentBuilder;
import org.apache.lucene.document.Document;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mlux
 * Date: 02.05.2011
 * Time: 10:42:00
 * To change this template use File | Settings | File Templates.
 */
public class MSERTest extends TestCase {
    public void testMSERExtraction() throws IOException {
        MSERDocumentBuilder db = new MSERDocumentBuilder();
        String file = "./wang-1000/22.jpg";
        Document document = db.createDocument(ImageIO.read(new FileInputStream(file)), file);
        byte[][] binaryValues = document.getBinaryValues(DocumentBuilder.FIELD_NAME_MSER);
        System.out.println("binaryValues.length = " + binaryValues.length);

        for (int i = 0; i < binaryValues.length; i++) {
            byte[] binaryValue = binaryValues[i];
            MSERFeature feat = new MSERFeature();
            feat.setByteArrayRepresentation(binaryValues[i]);
            for (int j = 0; j < feat.descriptor.length; j++) {
                if (Float.isNaN(feat.descriptor[j])) System.out.println("feat " + i + " = " + feat);
                break;
            }
        }
    }
}
