package net.semanticmetadata.lire.impl;

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;
import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.SurfFeature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

/**
 * User: mathias@juggle.at
 * Date: 29.09.2010
 * Time: 15:41:28
 */
public class SurfDocumentBuilder extends AbstractDocumentBuilder {
    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = null;
        // test with MSER:
        Surf s = new Surf(image);
        List<SURFInterestPoint> interestPoints = s.getFreeOrientedInterestPoints();
        doc = new Document();
        for (Iterator<SURFInterestPoint> sipi = interestPoints.iterator(); sipi.hasNext();) {
            SURFInterestPoint sip = sipi.next();
            SurfFeature sf = new SurfFeature(sip);
            doc.add(new Field(DocumentBuilder.FIELD_NAME_SURF, sf.getByteArrayRepresentation(), Field.Store.YES));
        }
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }
}
