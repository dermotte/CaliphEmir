package net.semanticmetadata.lire.impl;

import at.lux.imageanalysis.ColorLayoutImpl;
import at.lux.imageanalysis.EdgeHistogramImplementation;
import at.lux.imageanalysis.ScalableColorImpl;
import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 31.01.2006
 * <br>Time: 23:02:52
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(SimpleDocumentBuilder.class.getName());

    private boolean scalableColor = false, colorLayout = false, edgeHistogram = false;

    /**
     * Creates a new SimpleDocumentBuilder using the defined descriptors. Please note that
     * {@link at.lux.imageanalysis.ColorLayoutImpl} and {@link at.lux.imageanalysis.ScalableColorImpl}
     * are quite <i>fast</i> while {@link at.lux.imageanalysis.EdgeHistogramImplementation} is quite
     * <i>slow</i>.  <p>
     * Do not use this constructor yourself, use
     * the {@link net.semanticmetadata.lire.DocumentBuilderFactory} instead.
     *
     * @param scalableColor defines if {@link at.lux.imageanalysis.ScalableColorImpl} should be used.
     * @param colorLayout   defines if {@link at.lux.imageanalysis.ColorLayoutImpl} should be used.
     * @param edgeHistogram defines if {@link at.lux.imageanalysis.EdgeHistogramImplementation} should be used.
     * @see net.semanticmetadata.lire.DocumentBuilderFactory
     */
    public SimpleDocumentBuilder(boolean scalableColor, boolean colorLayout, boolean edgeHistogram) {
        this.colorLayout = colorLayout;
        this.edgeHistogram = edgeHistogram;
        this.scalableColor = scalableColor;
    }

    public Document createDocument(BufferedImage image, String identifier) {
        assert (image != null);
        BufferedImage bimg = image;
        if (Math.max(image.getHeight(), image.getWidth()) > DocumentBuilder.MAX_IMAGE_SIDE_LENGTH) {
            bimg = ImageUtils.scaleImage(image, DocumentBuilder.MAX_IMAGE_SIDE_LENGTH);
        }
        String sc = null, cl = null, eh = null;
        if (scalableColor) {
            logger.finer("Starting extraction of ScalableColor from image");
            ScalableColorImpl scd = new ScalableColorImpl(bimg);
            sc = scd.getStringRepresentation();
        }
        if (colorLayout) {
            logger.finer("Starting extraction of ColorLayout from image");
            ColorLayoutImpl cld = new ColorLayoutImpl(bimg);
            cl = cld.getStringRepresentation();
        }
        if (edgeHistogram) {
            logger.finer("Starting extraction of EdgeHistogram from image");
            EdgeHistogramImplementation ehd = new EdgeHistogramImplementation(bimg);
            eh = ehd.getStringRepresentation();
        }
        logger.fine("Extraction from image finished");
        Document doc = new Document();
        if (sc != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_SCALABLECOLOR, sc, Field.Store.YES, Field.Index.NO));
        if (cl != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_COLORLAYOUT, cl, Field.Store.YES, Field.Index.NO));
        if (eh != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM, eh, Field.Store.YES, Field.Index.NO));
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }
}
