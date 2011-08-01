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
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * TODO: Delete and refactor use to VisualWOrdsImageSearcher
 * Date: 28.09.2010
 * Time: 13:58:33
 * Mathias Lux, mathias@juggle.at
 */
public class SurfVisualWordsImageSearcher extends AbstractImageSearcher {
    private QueryParser qp;
    private int numMaxHits;
    private Similarity similarity = new TfIdfSimilarity();

    public SurfVisualWordsImageSearcher(int numMaxHits, Similarity similarity) {
        this.similarity = similarity;
        this.numMaxHits = numMaxHits;
        qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS, new WhitespaceAnalyzer(Version.LUCENE_33));
    }

    public SurfVisualWordsImageSearcher(int numMaxHits) {
        this.numMaxHits = numMaxHits;
        qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS, new WhitespaceAnalyzer(Version.LUCENE_33));
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits sh = null;
        IndexSearcher isearcher = new IndexSearcher(reader);
        isearcher.setSimilarity(similarity);
        String queryString = doc.getValues(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS)[0];
        //            Query query = qp.parse(queryString);
        Query tq = createQuery(queryString);

        TopDocs docs = isearcher.search(tq, numMaxHits);
        LinkedList<SimpleResult> res = new LinkedList<SimpleResult>();
        float maxDistance = 0;
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            float d = 1f / docs.scoreDocs[i].score;
            maxDistance = Math.max(d, maxDistance);
            SimpleResult sr = new SimpleResult(d, reader.document(docs.scoreDocs[i].doc));
            res.add(sr);
        }
        sh = new SimpleImageSearchHits(res, maxDistance);
        return sh;
    }

    private Query createQuery(String queryString) {
        String fieldName = DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS;
        StringTokenizer st = new StringTokenizer(queryString);
        BooleanQuery query = new BooleanQuery();
        HashSet<String> terms = new HashSet<String>();
        String term;
        while (st.hasMoreTokens()) {
            term = st.nextToken();
            if (!terms.contains(term)) {
                query.add(new BooleanClause(new TermQuery(new Term(fieldName, term)), BooleanClause.Occur.SHOULD));
                terms.add(term);
            }
        }
        return query;
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }

    private static class TfIdfSimilarity extends Similarity {

        @Override
        public float computeNorm(String s, FieldInvertState fieldInvertState) {
            // TODO: check ...
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
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
//            return (float) (v);
            return 1;
        }

        @Override
        public float idf(int docfreq, int numdocs) {
            return 1f;
//            return (float) (Math.log((double) numdocs / ((double) docfreq)));
        }

        @Override
        public float coord(int i, int i1) {
            return 1;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
