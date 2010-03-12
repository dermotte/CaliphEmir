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
 * (c) 2002-2010 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */
package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a faster way of searching based on byte arrays instead of Strings. The method
 * {@link net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation()} is used
 * to generate the signature of the descriptor much faster.
 * User: Mathias Lux, mathias@juggle.at
 * Date: 12.03.2010
 * Time: 13:21:35
 */
public class CEDDDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final int MAX_IMAGE_DIMENSION = 1024;
    
    public Document createDocument(BufferedImage image, String identifier) {
        assert (image != null);
        BufferedImage bimg = image;
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to guarantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > MAX_IMAGE_DIMENSION) {
            bimg = ImageUtils.scaleImage(image, MAX_IMAGE_DIMENSION);
        }
        Document doc = null;
        logger.finer("Starting extraction from image [CEDD - fast].");
        CEDD vd = new CEDD();
        vd.extract(bimg);
        logger.fine("Extraction finished [CEDD - fast].");

        doc = new Document();
        doc.add(new Field(DocumentBuilder.FIELD_NAME_CEDD_FAST, vd.getByteArrayRepresentation(), Field.Store.YES));
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));

        return doc;
    }
}
