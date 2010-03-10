/*
 * This file is part of the LIRE project: http://www.SemanticMetadata.net/lire.
 *
 * Lire is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Lire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lire; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * Note, that the SIFT-algorithm is protected by U.S. Patent 6,711,293: "Method and
 * apparatus for identifying scale invariant features in an image and use of same for
 * locating an object in an image" by the University of British Columbia. That is, for
 * commercial applications the permission of the author is required.
 *
 * (c) 2008 by Mathias Lux, mathias@juggle.at
 */
package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.sift.Extractor;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * ...
 * Date: 23.09.2008
 * Time: 12:05:08
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SiftDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(getClass().getName());
    private Extractor extractor;

    public SiftDocumentBuilder() {
        extractor = new Extractor();
    }

    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = null;
        try {
            // extract features from image:
            List<Feature> features = extractor.computeSiftFeatures(image);
            // create new document:
            doc = new Document();
            for (Iterator<Feature> fit = features.iterator(); fit.hasNext();) {
                Feature f = fit.next();
                // add each feature to the document:
                doc.add(new Field(DocumentBuilder.FIELD_NAME_SIFT, f.getStringRepresentation(), Field.Store.YES, Field.Index.NO));
            }
            if (identifier != null)
                doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return doc;
    }
}
