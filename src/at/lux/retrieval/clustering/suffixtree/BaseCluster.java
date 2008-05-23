/*
 * This file is part of Caliph & Emir.
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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
package at.lux.retrieval.clustering.suffixtree;

import at.lux.retrieval.clustering.TextSuffixTree;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 03.06.2004
 *         Time: 15:33:45
 */

public class BaseCluster implements Comparable {
    private StcNode node = null;
    private HashSet<StcDocument> documents = null;
    private String phrase = null;
    private double score = Double.MIN_VALUE;

    private double phraseScore = 0d;

    public BaseCluster(HashSet<StcDocument> documents, StcNode node, String phrase, WordIndex index, HashSet<String> stopwords) {
        this.documents = documents;
        this.node = node;
        this.phrase = phrase;
        score = calculateScore(index, stopwords);
        assert documents.size() > 1;
    }

    /**
     * Calculates the score as mentioned in paper.
     *
     * @param index
     * @param stopwords
     * @return
     */
    private double calculateScore(WordIndex index, HashSet<String> stopwords) {
        double numberOfDocuments = (double) documents.size();
        String[] tokens = phrase.trim().split("\\W");
        int numberOfWords = tokens.length;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (stopwords.contains(token))
                numberOfWords--;
            else if (index.getDocumentCount(token) < TextSuffixTree.CONFIGURATION_MINIMUM_TERM_FREQUENCY_BORDER)
                numberOfWords--;
            else if (index.getDocumentFrequency(token) > TextSuffixTree.CONFIGURATION_DOCUMENT_FREQUENCY_BORDER)
                numberOfWords--;
        }
        if (numberOfWords < 0)
            numberOfWords = 0;
        else if (numberOfWords > TextSuffixTree.CONFIGURATION_MAXIMUM_PHRASE_LENGTH)
            numberOfWords = TextSuffixTree.CONFIGURATION_MAXIMUM_PHRASE_LENGTH;

        double wordweighting = 0.0;
        if (numberOfWords == 1) {
            wordweighting = TextSuffixTree.CONFIGURATION_SINGLE_WORD_PENALTY;
        } else
            wordweighting = (double) numberOfWords;
        phraseScore = wordweighting;
        return (numberOfDocuments * wordweighting);
    }

    public String toString() {
        String result = score + ": \"" + phrase + "\"";
        for (Iterator<StcDocument> iterator = documents.iterator(); iterator.hasNext();) {
            StcDocument stcDocument = iterator.next();
            result += " " + stcDocument.toString();
        }
        return result;
    }

    public double getScore() {
        return score;
    }

    public HashSet<StcDocument> getDocuments() {
        return documents;
    }

    public StcNode getNode() {
        return node;
    }

    public String getPhrase() {
        return phrase;
    }

    /**
     * Makes Base Clusters comparable.
     *
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        int result = 0;
        if (o.equals(this))
            return 0;
        else if (o instanceof BaseCluster) {
            BaseCluster b = (BaseCluster) o;
            if (score > b.score)
                return -1;
            else if (score < b.score)
                return 1;
            else
                return System.identityHashCode(this) - System.identityHashCode(o);
        } else {
            result = System.identityHashCode(this) - System.identityHashCode(o);
        }
        return result;
    }

    /**
     * Calculates the binary similarity between 2 base clusters.
     *
     * @param cluster
     * @return
     */
    public int binarySimilarity(BaseCluster cluster) {
        HashSet<StcDocument> mergedDocuments = new HashSet<StcDocument>();
        HashSet<StcDocument> toIterate, toCheck;
        int thisDocumentsSize = this.documents.size();
        int clusterDocumentsSize = cluster.documents.size();

        // iterate using the smaller set!
        if (thisDocumentsSize < clusterDocumentsSize) {
            toIterate = this.documents;
            toCheck = cluster.documents;
        } else {
            toCheck = this.documents;
            toIterate = cluster.documents;
        }

        for (Iterator<StcDocument> iterator = toIterate.iterator(); iterator.hasNext();) {
            StcDocument stcDocument = iterator.next();
            if (toCheck.contains(stcDocument)) mergedDocuments.add(stcDocument);
        }

        double tmp1 = ((double) mergedDocuments.size()) / ((double) thisDocumentsSize);
        double tmp2 = ((double) mergedDocuments.size()) / ((double) clusterDocumentsSize);

        if (tmp1 > 0.5d && tmp2 > 0.5d)
            return 1;
        else
            return 0;
    }


    /**
     * Returns the score of the base cluster without multiplication with document count,
     * eventually needed for final cluster score :)
     *
     * @return
     */
    public double getPhraseScore() {
        return phraseScore;
    }


}
