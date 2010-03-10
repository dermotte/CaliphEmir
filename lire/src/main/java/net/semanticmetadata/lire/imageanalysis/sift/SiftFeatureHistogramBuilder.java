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
package net.semanticmetadata.lire.imageanalysis.sift;

import net.semanticmetadata.lire.DocumentBuilder;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ...
 * Date: 24.09.2008
 * Time: 09:38:53
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SiftFeatureHistogramBuilder {
    IndexReader reader;

    public SiftFeatureHistogramBuilder(IndexReader reader) {
        this.reader = reader;
    }

    public void index() throws IOException {
        KMeans k = new KMeans();
        // fill the KMeans object:
        LinkedList<Feature> features;
        for (int i = 0; i < reader.numDocs(); i++) {
            if (!reader.isDeleted(i)) {
                Document d = reader.document(i);
                features = new LinkedList<Feature>();
                String[] fs = d.getValues(DocumentBuilder.FIELD_NAME_SIFT);
                String file = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                for (int j = 0; j < fs.length; j++) {
                    Feature f = new Feature();
                    f.setStringRepresentation(fs[j]);
                    features.add(f);
                }
                k.addImage(file, features);
            }
        }
        // do the clustering:
        System.out.println("Start clustering.");
        k.init();
        double laststress = k.clusteringStep();
        double newstress = k.clusteringStep();
        while (newstress > laststress) {
            laststress = newstress;
            newstress = k.clusteringStep();
        }
        //  ccreate histograms:
        System.out.println("Create histogram.");
        List<Image> imgs = k.getImages();
        for (Iterator<Image> imageIterator = imgs.iterator(); imageIterator.hasNext();) {
            Image image = imageIterator.next();
            image.initHistogram(k.getNumClusters());
            for (Iterator<Feature> iterator = image.features.iterator(); iterator.hasNext();) {
                Feature feat = iterator.next();
                image.getLocalFeatureHistogram()[k.getClusterOfFeature(feat)]++;
                image.normalizeFeatureHistogram();
            }
        }
        // store histograms in index:
        LinkedList<Integer> toDelete = new LinkedList<Integer>();
        LinkedList<Document> toAdd = new LinkedList<Document>();
        for (int i = 0; i < reader.numDocs(); i++) {
            if (!reader.isDeleted(i)) {
                Document d = reader.document(i);
                String file = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                int[] hist = null;
                for (Iterator<Image> imageIterator = imgs.iterator(); imageIterator.hasNext();) {
                    Image tmp = imageIterator.next();
                    if (tmp.identifier.equals(file)) hist = tmp.getLocalFeatureHistogram();
                }
                if (hist != null) {
                    d.add(new Field(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM, arrayToString(hist), Field.Store.YES, Field.Index.NO));
                    toAdd.add(d);
                    toDelete.add(i);
                } else
                    System.err.println("Oops, no global histogram found.");
            }
        }
        // delete old ones from index
        for (Iterator<Integer> documentIterator = toDelete.iterator(); documentIterator.hasNext();) {
            reader.deleteDocument(documentIterator.next());
        }
        // add new ones ...
        IndexWriter iw = new IndexWriter(reader.directory(), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        for (Iterator<Document> documentIterator = toAdd.iterator(); documentIterator.hasNext();) {
            iw.addDocument(documentIterator.next());
        }
        iw.optimize();
        iw.close();
    }

    private String arrayToString(int[] hist) {
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < hist.length; i++) {
            sb.append(hist[i]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}
