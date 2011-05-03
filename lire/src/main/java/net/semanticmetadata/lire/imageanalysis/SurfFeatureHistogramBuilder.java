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
package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.sift.Cluster;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.imageanalysis.sift.KMeans;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * ...
 * Date: 24.09.2008
 * Time: 09:38:53
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SurfFeatureHistogramBuilder {
    IndexReader reader;
    // number of documents used to build the vocabulary / clusters.
    private int numDocsForVocabulary = 100;
    private int numClusters = 256;
    private Cluster[] clusters = null;
    DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();

    public SurfFeatureHistogramBuilder(IndexReader reader) {
        this.reader = reader;
    }

    /**
     * Creates a new instance of the SiftFeatureHistogramBuilder using the given reader. The numDocsForVocabulary
     * indicates how many documents of the index are used to build the vocabulary (clusters).
     *
     * @param reader               the reader used to open the Lucene index,
     * @param numDocsForVocabulary gives the number of documents for building the vocabulary (clusters).
     */
    public SurfFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary) {
        this.reader = reader;
        this.numDocsForVocabulary = numDocsForVocabulary;
    }

    public SurfFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        this.numDocsForVocabulary = numDocsForVocabulary;
        this.numClusters = numClusters;
        this.reader = reader;
    }

    /**
     * Uses an existing index, where each and every document should have a set of SURF features. A number of
     * random images (numDocsForVocabulary) is selected and clustered to get a vocabulary of visual words
     * (the cluster means). For all images a histogram on the visual words is created and added to the documents.
     * Pre-existing histograms are deleted, so this method can be used for re-indexing.
     *
     * @throws java.io.IOException
     */
    public void index() throws IOException {
        df.setMaximumFractionDigits(3);
        // find the documents for building the vocabulary:
        HashSet<Integer> docIDs = selectVocabularyDocs();
        KMeans k = new KMeans(numClusters);
        // fill the KMeans object:
        LinkedList<Histogram> features;
        for (Iterator<Integer> iterator = docIDs.iterator(); iterator.hasNext();) {
            int nextDoc = iterator.next();
            if (!reader.isDeleted(nextDoc)) {
                Document d = reader.document(nextDoc);
                features = new LinkedList<Histogram>();
                byte[][] binaryValues = d.getBinaryValues(DocumentBuilder.FIELD_NAME_SURF);
                String file = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                for (int j = 0; j < binaryValues.length; j++) {
                    SurfFeature f = new SurfFeature();
                    f.setByteArrayRepresentation(binaryValues[j]);
                    features.add(f);
                }
                k.addImage(file, features);
            }
        }
        // do the clustering:
        System.out.println("k.getFeatureCount() = " + k.getFeatureCount());
        System.out.println("Starting clustering ...");
        k.init();
        System.out.println("Step.");
        double time = System.currentTimeMillis();
        double laststress = k.clusteringStep();

        System.out.println(df.format((System.currentTimeMillis() - time) / (1000*60)) + " min. -> Next step.");
        time = System.currentTimeMillis();
        double newstress = k.clusteringStep();
        // critical part: Give the difference in between steps as a co,nstraint for accuracy vs. runtime trade off.
        double treshold = Math.max(20d, (double) k.getFeatureCount()/1000d);
        System.out.println("Treshold = " + treshold);
        while (Math.abs(newstress - laststress) > treshold) {
            System.out.println(df.format((System.currentTimeMillis() - time) / (1000*60)) + " min. -> Next step. Stress difference ~ |" + (int) newstress + " - " + (int) laststress +"| = "+ df.format(Math.abs(newstress - laststress)));
            time = System.currentTimeMillis();
            laststress = newstress;
            newstress = k.clusteringStep();
        }
        // TODO: Serialize and store the clusters somewhere to re-use them if the index ever gets updated.
        clusters = k.getClusters();
        Cluster.writeClusters(clusters, "./clusters.dat");
        //  create & store histograms:
        System.out.println("Creating histograms ...");
        int[] tmpHist = new int[numClusters];
        Feature f = new Feature();
        IndexWriter iw = new IndexWriter(reader.directory(), new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (!reader.isDeleted(i)) {
                for (int j = 0; j < tmpHist.length; j++) {
                    tmpHist[j] = 0;
                }
                Document d = reader.document(i);
                features = new LinkedList<Histogram>();
//                String[] fs = d.getValues(DocumentBuilder.FIELD_NAME_SIFT);
                byte[][] binaryValues = d.getBinaryValues(DocumentBuilder.FIELD_NAME_SURF);

                String file = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                // remove the fields if they are already there ...
                d.removeField(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
                d.removeField(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM);
                // find the appropriate cluster for each feature:
                for (int j = 0; j < binaryValues.length; j++) {
                    f.setByteArrayRepresentation(binaryValues[j]);
                    tmpHist[clusterForFeature(f)]++;
                }
                d.add(new Field(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS, arrayToVisualWordString(tmpHist), Field.Store.YES, Field.Index.ANALYZED));
                d.add(new Field(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM, SerializationUtils.arrayToString(tmpHist), Field.Store.YES, Field.Index.ANALYZED));
                // now write the new one. we use the identifier to update ;)
                iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), d);
            }
        }
        iw.optimize();
        iw.close();
        System.out.println("Finished.");
        /*
        List<Image> imgs = k.getImages();
        for (Iterator<Image> imageIterator = imgs.iterator(); imageIterator.hasNext();) {
            Image image = imageIterator.next();
            image.initHistogram(k.getNumClusters());
            for (Iterator<Feature> iterator = image.features.iterator(); iterator.hasNext();) {
                Feature feat = iterator.next();
                image.getLocalFeatureHistogram()[k.getClusterOfFeature(feat)]++;
                // we might cut this out here ... no quantization needed:
                // image.normalizeFeatureHistogram();
            }
        }
        System.out.println("Finished, now writing into index ...");
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
                    // stores visual words, something like "v0 v0 v1 v3 v4 ..."
                    d.add(new Field(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS, arrayToVisualWordString(hist), Field.Store.YES, Field.Index.ANALYZED));
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
        // close reader to let IndexWriter work.
        reader.close(); 
        // add new ones ...
        IndexWriter iw = new IndexWriter(reader.directory(), new WhitespaceAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        for (Iterator<Document> documentIterator = toAdd.iterator(); documentIterator.hasNext();) {
            iw.addDocument( documentIterator.next());
        }

        */
    }



    /**
     * Find the appropriate cluster for a given feature.
     *
     * @param f
     * @return the index of the cluster.
     */
    private int clusterForFeature(Feature f) {
        double d = clusters[0].getDistance(f);
        double tmp = d;
        int result = 0;
        for (int i = 1; i < clusters.length; i++) {
            tmp = clusters[i].getDistance(f);
            if (tmp < d) {
                d = tmp;
                result = i;
            }
        }
        return result;
    }

    private String arrayToVisualWordString(int[] hist) {
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < hist.length; i++) {
            int visualWordIndex = hist[i];
            for (int j = 0; j < visualWordIndex; j++) {
                sb.append('v');
                sb.append(i);
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private String arrayToString(int[] hist) {
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < hist.length; i++) {
            sb.append(hist[i]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private HashSet<Integer> selectVocabularyDocs() throws IOException {
        // need to make sure that this is not running forever ...
        int loopCount = 0;
        float maxDocs = reader.maxDoc();
        int capacity = (int) Math.min(numDocsForVocabulary, maxDocs - 5);
        HashSet<Integer> result = new HashSet<Integer>(capacity);
        int tmpDocNumber;
        for (int r = 0; r < capacity; r++) {
            boolean worksFine = false;
            do {
                tmpDocNumber = (int) (Math.random() * maxDocs);
                // check if the selected doc number is valid: not null, not deleted and not already chosen.
                worksFine = (reader.document(tmpDocNumber) != null) && !reader.isDeleted(tmpDocNumber) && !result.contains(tmpDocNumber);
            } while (!worksFine);
            result.add(tmpDocNumber);
            // need to make sure that this is not running forever ...
            if (loopCount++ > numDocsForVocabulary * 100)
                throw new UnsupportedOperationException("Could not get the documents, maybe there are not enough documents in the index?");
        }
        return result;
    }
}
