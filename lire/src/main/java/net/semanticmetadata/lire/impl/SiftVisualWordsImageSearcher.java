package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Date: 28.09.2010
 * Time: 13:58:33
 * Mathias Lux, mathias@juggle.at
 */
public class SiftVisualWordsImageSearcher extends AbstractImageSearcher {
    private QueryParser qp;
    private int numMaxHits;

    public SiftVisualWordsImageSearcher(int numMaxHits) {
        this.numMaxHits = numMaxHits;
        qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS, new WhitespaceAnalyzer());
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits sh = null;
        IndexSearcher isearcher = new IndexSearcher(reader);
        isearcher.setSimilarity(new TfIdfSimilarity());
        String query = doc.getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS)[0];
        try {
            TopDocs docs = isearcher.search(qp.parse(query), numMaxHits);
            LinkedList<SimpleResult> res = new LinkedList<SimpleResult>();
            float maxDistance = 0;
            for (int i = 0; i < docs.scoreDocs.length; i++) {
                float d = 1f / docs.scoreDocs[i].score;
                maxDistance = Math.max(d, maxDistance);
                SimpleResult sr = new SimpleResult(d, reader.document(docs.scoreDocs[i].doc));
                res.add(sr);
            }
            sh = new SimpleImageSearchHits(res, maxDistance);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return sh;
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }

    private static class TfIdfSimilarity extends Similarity {
        @Override
        public float lengthNorm(String s, int i) {
            return 1;
        }

        @Override
        public float queryNorm(float v) {
            return 1;
        }

        @Override
        public float sloppyFreq(int i) {
            return 0;
        }

        @Override
        public float tf(float v) {
            return (float) Math.sqrt(v);
            // return v;
        }

        @Override
        public float idf(int docfreq, int numdocs) {
            return 1f;
//             return (float) (Math.log((double) numdocs / ((double) docfreq +  1d))); 
        }

        @Override
        public float coord(int i, int i1) {
            return 1;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
