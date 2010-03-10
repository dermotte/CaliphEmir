package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
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
public class CorrelogramDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(CorrelogramDocumentBuilder.class.getName());

    public static final int MAXIMUM_DISTANCE = 16;
    private static final int MAX_IMAGE_DIMENSION = 320;
    public AutoColorCorrelogram.Mode mode = AutoColorCorrelogram.Mode.FullNeighbourhood;

    /**
     * Creates a new CorrelogramDocumentBuilder using the AutoColorCorrelogram feature vector.
     *
     * @param mode defines the extraction mode. See
     * @see net.semanticmetadata.lire.DocumentBuilderFactory
     */
    public CorrelogramDocumentBuilder(AutoColorCorrelogram.Mode mode) {
        this.mode = mode;
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

        logger.finer("Starting extraction of AutoColorCorrelogram from image");
        AutoColorCorrelogram acc = new AutoColorCorrelogram(MAXIMUM_DISTANCE, mode);
        acc.extract(bimg);
        featureString = acc.getStringRepresentation();
        logger.fine("Extraction from image finished");

        Document doc = new Document();
        doc.add(new Field(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM, featureString, Field.Store.YES, Field.Index.NO));
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }
}
