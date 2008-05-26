package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 31.01.2006
 * <br>Time: 23:02:52
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleColorHistogramDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final int MAX_IMAGE_DIMENSION = 2048;

    /**
     * Creates a new CorrelogramDocumentBuilder using the AutoColorCorrelogram feature vector.
     *
     * @see net.semanticmetadata.lire.DocumentBuilderFactory
     * @see net.semanticmetadata.lire.imageanalysis.CEDD
     */
    public SimpleColorHistogramDocumentBuilder() {
        // nothing to do here for now ...
    }

    public Document createDocument(BufferedImage image, String identifier) {
        String featureString = "";
        assert (image != null);
        BufferedImage bimg = image;
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to garuantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > MAX_IMAGE_DIMENSION) {
            bimg = ImageUtils.scaleImage(image, MAX_IMAGE_DIMENSION);
        }

        logger.finer("Starting extraction of SimpleColorHistogram from image");
        SimpleColorHistogram cedd = new SimpleColorHistogram();
        cedd.extract(bimg);
        featureString = cedd.getStringRepresentation();
        logger.fine("Extraction from image finished");

        Document doc = new Document();
        doc.add(new Field(DocumentBuilder.FIELD_NAME_COLORHISTOGRAM, featureString, Field.Store.YES, Field.Index.NO));
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.UN_TOKENIZED));
        return doc;
    }
}