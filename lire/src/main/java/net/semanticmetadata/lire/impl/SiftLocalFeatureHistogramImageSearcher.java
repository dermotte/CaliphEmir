/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TreeSet;

/**
 * ...
 * Date: 24.09.2008
 * Time: 10:17:18
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SiftLocalFeatureHistogramImageSearcher extends AbstractImageSearcher {
    private TreeSet<SimpleResult> docs;
    private int maxHits;

    public SiftLocalFeatureHistogramImageSearcher(int maxHits) {
        this.maxHits = maxHits;
        docs = new TreeSet<SimpleResult>();
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not feasible for local feature histograms.");
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits searchHits = null;

        String[] cls = doc.getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM);
        int[] hist = createHistogram(cls[0]);
        float maxDistance = findSimilar(reader, hist);

        searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        return searchHits;
    }

    private int[] createHistogram(String cls) {
        String[] tmp = cls.split(" ");
        int[] hist = new int[tmp.length];
        for (int i = 0; i < hist.length; i++) {
            hist[i] = Integer.parseInt(tmp[i]);
        }
        return hist;
    }

    private float findSimilar(IndexReader reader, int[] hist) throws IOException {
        float maxDistance = -1f, overallMaxDistance = -1f;
        boolean hasDeletions = reader.hasDeletions();

        // clear result set ...
        docs.clear();

        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            // bugfix by Roman Kern
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }

            Document d = reader.document(i);
            float distance = getDistance(d, hist);
            assert (distance >= 0);
            // calculate the overall max distance to normalize score afterwards
            if (overallMaxDistance < distance) {
                overallMaxDistance = distance;
            }
            // if it is the first document:
            if (maxDistance < 0) {
                maxDistance = distance;
            }
            // if the array is not full yet:
            if (this.docs.size() < maxHits) {
                this.docs.add(new SimpleResult(distance, d));
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                this.docs.remove(this.docs.last());
                // add the new one ...
                this.docs.add(new SimpleResult(distance, d));
                // and set our new distance border ...
                maxDistance = this.docs.last().getDistance();
            }
        }
        return maxDistance;
    }

    private float getDistance(Document d, int[] hist) {
        String[] cls = d.getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM);
        int[] hist2 = createHistogram(cls[0]);
        float result = 0f;
        for (int i = 0; i < hist2.length; i++) {
            int i1 = hist2[i] - hist[i];
            result += i1 * i1;

        }
        return (float) Math.sqrt(result);
    }


    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not feasible for local feature histograms.");
    }
}
