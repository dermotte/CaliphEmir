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

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Provides a faster way of searching based on byte arrays instead of Strings. The method
 * {@link net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation()} is used
 * to generate the signature of the descriptor much faster. First tests have shown that this
 * implementation is up to 4 times faster than the implementation based on strings
 * (for 120,000 images) 
 * 
 * User: Mathias Lux, mathias@juggle.at
 * Date: 12.03.2010
 * Time: 13:21:50
 */
public class CEDDImageSearcher extends GenericImageSearcher {
    public CEDDImageSearcher(int maxHits) {
        super(maxHits, CEDD.class, DocumentBuilder.FIELD_NAME_CEDD_FAST);
    }

    protected float getDistance(Document d, LireFeature lireFeature) {
        float distance = 0f;
        CEDD lf;
        try {
            lf = (CEDD) descriptorClass.newInstance();
            byte[] cls = d.getBinaryValue(fieldName);
            if (cls != null && cls.length > 0) {
                lf.setByteArrayRepresentation(cls);
                distance = lireFeature.getDistance(lf);
            } else {
                logger.warning("No feature stored in this document ...");
            }
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }

        return distance;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits searchHits = null;
        try {
            CEDD lireFeature = (CEDD) descriptorClass.newInstance();

            byte[] cls = doc.getBinaryValue(fieldName);
            if (cls != null && cls.length > 0)
                lireFeature.setByteArrayRepresentation(cls);
            float maxDistance = findSimilar(reader, lireFeature);

            searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }
        return searchHits;
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        // get the first document:
        SimpleImageDuplicates simpleImageDuplicates = null;
        try {
            if (!IndexReader.indexExists(reader.directory()))
                throw new FileNotFoundException("No index found at this specific location.");
            Document doc = reader.document(0);

            CEDD lireFeature = (CEDD) descriptorClass.newInstance();
            byte[] cls = doc.getBinaryValue(fieldName);
            if (cls != null && cls.length > 0)
                lireFeature.setByteArrayRepresentation(cls);

            HashMap<Float, List<String>> duplicates = new HashMap<Float, List<String>>();

            // find duplicates ...
            boolean hasDeletions = reader.hasDeletions();

            int docs = reader.numDocs();
            int numDuplicates = 0;
            for (int i = 0; i < docs; i++) {
                if (hasDeletions && reader.isDeleted(i)) {
                    continue;
                }
                Document d = reader.document(i);
                float distance = getDistance(d, lireFeature);

                if (!duplicates.containsKey(distance)) {
                    duplicates.put(distance, new LinkedList<String>());
                } else {
                    numDuplicates++;
                }
                duplicates.get(distance).add(d.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
            }

            if (numDuplicates == 0) return null;

            LinkedList<List<String>> results = new LinkedList<List<String>>();
            for (float f : duplicates.keySet()) {
                if (duplicates.get(f).size() > 1) {
                    results.add(duplicates.get(f));
                }
            }
            simpleImageDuplicates = new SimpleImageDuplicates(results);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }
        return simpleImageDuplicates;

    }
}
