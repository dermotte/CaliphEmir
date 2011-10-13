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
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */
package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.clustering.Cluster;
import net.semanticmetadata.lire.clustering.KMeans;
import net.semanticmetadata.lire.clustering.ParallelKMeans;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
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
    private int numClusters = 1000;
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
        KMeans k = new ParallelKMeans(numClusters);
        // fill the KMeans object:
        LinkedList<Histogram> features;
        for (Iterator<Integer> iterator = docIDs.iterator(); iterator.hasNext(); ) {
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

        System.out.println(df.format((System.currentTimeMillis() - time) / (1000 * 60)) + " min. -> Next step.");
        time = System.currentTimeMillis();
        double newStress = k.clusteringStep();
        // critical part: Give the difference in between steps as a constraint for accuracy vs. runtime trade off.
        double threshold = Math.max(20d, (double) k.getFeatureCount() / 1000d);
        System.out.println("Threshold = " + threshold);
        while (Math.abs(newStress - laststress) > threshold) {
            System.out.println(df.format((System.currentTimeMillis() - time) / (1000 * 60)) + " min. -> Next step. Stress difference ~ |" + (int) newStress + " - " + (int) laststress + "| = " + df.format(Math.abs(newStress - laststress)));
            time = System.currentTimeMillis();
            laststress = newStress;
            newStress = k.clusteringStep();
        }
        // Serializing clusters to a file on the disk ...
        clusters = k.getClusters();
        Cluster.writeClusters(clusters, "./clusters.dat");
        //  create & store histograms:
        System.out.println("Creating histograms ...");
        int[] tmpHist = new int[numClusters];
        Feature f = new Feature();
        IndexWriter iw = LuceneUtils.createIndexWriter(reader.directory(), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (!reader.isDeleted(i)) {
                for (int j = 0; j < tmpHist.length; j++) {
                    tmpHist[j] = 0;
                }
                Document d = reader.document(i);
                byte[][] binaryValues = d.getBinaryValues(DocumentBuilder.FIELD_NAME_SURF);
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
    }


    public void indexMissing() throws IOException {
        // Reading clusters from disk:
        clusters = Cluster.readClusters("./clusters.dat");
        //  create & store histograms:
        System.out.println("Creating histograms ...");
        int[] tmpHist = new int[numClusters];
        Feature f = new Feature();
        IndexWriter iw = LuceneUtils.createIndexWriter(reader.directory(), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (!reader.isDeleted(i)) {
                for (int j = 0; j < tmpHist.length; j++) {
                    tmpHist[j] = 0;
                }
                Document d = reader.document(i);
                // Only if there are no values yet:
                if (d.getValues(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS) == null) {
                    byte[][] binaryValues = d.getBinaryValues(DocumentBuilder.FIELD_NAME_SURF);
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
        }
        iw.optimize();
        iw.close();
        System.out.println("Finished.");
    }

    /**
     * Find the appropriate cluster for a given feature.
     *
     * @param f
     * @return the index of the cluster.
     */
    private int clusterForFeature(Feature f) {
        double d = clusters[0].getDistance(f);
        double tmp;
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
