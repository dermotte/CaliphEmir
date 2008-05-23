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
package at.lux.retrieval.clustering;

import at.lux.retrieval.clustering.suffixtree.*;

import java.util.*;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 03.06.2004
 *         Time: 13:42:41
 */

public abstract class AbstractSuffixTree {
    protected StcNode rootNode = null;

    protected HashSet<String> stopwords = new HashSet<String>();

    protected HashSet<StcDocument> documents = null;

    /**
     * Sorted list of base clusters, as we only need the 500 with the highest score ...
     */
    protected TreeSet<BaseCluster> baseClusters = null;

    /**
     * List of cluster that have been touched by the current insertion
     */
    protected HashSet<BaseCluster> touchedClusters = null;

    /**
     * Map of Nodes2Clusters as Index!
     */
    protected HashMap<StcNode, BaseCluster> nodeToClusterIndex = null;

    /**
     * Binary similarity Matrix for merging ...
     */
    protected EdgeIndex similarityMatrix = null;

    protected WordIndex index = null;

    // defines in how many of the documents a term is allowed to occur (in percent: [0,1]).
    // if exceeded a term is not counted in score. Setting this to a lower value will result in
    // smaller clusters. 30% is a good value to start with. Set it higher if you want to cluster
    // a small collection. A smaller value increases speed.
    public static final double CONFIGURATION_DOCUMENT_FREQUENCY_BORDER = 0.9d;
    // defines what is the minimum term frequency, if below it is not counted in score.
    // set it to a higher value to increase the minimum cluster size. For small collections
    // it should be set to 3. For bigger collections eg. >300 5 or more is appropriate
    // a higher value increases speed.
    public static final int CONFIGURATION_MINIMUM_TERM_FREQUENCY_BORDER = 3;
    // defines where we stop to count for the score.
    // it is quite the same to have a score with this count of words or more
    public static final int CONFIGURATION_MAXIMUM_PHRASE_LENGTH = 6;
    // defines where we stop to count for the score.
    public static final double CONFIGURATION_SINGLE_WORD_PENALTY = .05;
    // defines the maximum of update cluster after inserting a new document. Usually it should
    // be set to 50-250 depending on the kind and size of the initial collection. Smaller value
    // gives more speed
    public static final int CONFIGURATION_MAX_SIMILARITY_UPDATES = 300;

    // defines the minimum size for a sentence. Note that for path clustering the min size
    // should be set to 0.
    protected int MIN_SENTENCE_SIZE = 0;

    public AbstractSuffixTree() {
        rootNode = new StcNode();
        stopwords = new HashSet<String>();
        documents = new HashSet<StcDocument>();
        index = new WordIndex();
        touchedClusters = new HashSet<BaseCluster>();
        baseClusters = new TreeSet<BaseCluster>();
        nodeToClusterIndex = new HashMap<StcNode, BaseCluster>();
        similarityMatrix = new EdgeIndex();
    }

    public void insert(String phrase, StcDocument stcDocument) {
        // add documents to the list of documents
        documents.add(stcDocument);
        String[] sentences = getSentences(phrase);
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            if (sentence.length() >= MIN_SENTENCE_SIZE) {
                String[] tokens = getTokens(sentence);
                tokens = filterTokens(tokens);
                // add the tokens to the index
                index.addToIndex(tokens, stcDocument);
                // insert all suffices into the tree:
                for (int j = 0; j < tokens.length; j++) {
                    LinkedList<String> myList = new LinkedList<String>();
                    for (int ii = j; ii < tokens.length; ii++) {
                        String token = tokens[ii];
                        assert(token!=null);
                        myList.add(token);
                    }
                    rootNode.add(myList, stcDocument, j, "", this);
                }
            }
        }

        int maxSimilarityRecalc = baseClusters.size();
        maxSimilarityRecalc = maxSimilarityRecalc < CONFIGURATION_MAX_SIMILARITY_UPDATES ? maxSimilarityRecalc : CONFIGURATION_MAX_SIMILARITY_UPDATES;
        // recalculating the similarity, new cluster have already been added:
        for (Iterator<BaseCluster> iterator = touchedClusters.iterator(); iterator.hasNext();) {
            BaseCluster bc = iterator.next();
            Iterator<BaseCluster> recalcWith = baseClusters.iterator();
            // only update the base clusters with the highest scores:
            int countRecalcs = 0;
            while (recalcWith.hasNext() && countRecalcs < maxSimilarityRecalc) {
                BaseCluster bc2 = recalcWith.next();
                countRecalcs++;
                similarityMatrix.update(bc, bc2);
            }
        }
        touchedClusters.clear();
    }

    /**
     * Override this one if you want to change the way of handling tokens 
     * (or words in this implementation)
     * @param sentence
     * @return
     */
    protected abstract String[] getTokens(String sentence);

    /**
     * Override this method if you want to use another method to create the sentences.
     * @param phrase
     * @return an array of sentences.
     */
    protected abstract String[] getSentences(String phrase);

    /**
     * Override this method if you want to filter your tokens.
     * @param tokens
     * @return
     */
    protected abstract String[] filterTokens(String[] tokens);

    public TreeSet<BaseCluster> getBaseClusters() {
        return baseClusters;
    }

    public WordIndex getIndex() {
        return index;
    }

    public HashSet<String> getStopwords() {
        return stopwords;
    }

    public HashSet<BaseCluster> getTouchedClusters() {
        return touchedClusters;
    }

    public HashMap<StcNode, BaseCluster> getNodeToClusterIndex() {
        return nodeToClusterIndex;
    }

    public EdgeIndex getSimilarityMatrix() {
        return similarityMatrix;
    }

    public Set<FinalCluster> getFinalClusters() {
        // new linear version of base cluster merging:
        HashSet<BaseCluster> bcVisited = new HashSet<BaseCluster>();
        TreeSet<FinalCluster> set = new TreeSet<FinalCluster>();
        for (Iterator<BaseCluster> iterator = baseClusters.iterator(); iterator.hasNext();) {
            BaseCluster bc = iterator.next();
            if (bcVisited.add(bc)) { // not already checked ...
                FinalCluster fc = new FinalCluster(bc);
                Set<BaseCluster> allConnectedBaseClusters = similarityMatrix.getAllConnectedBaseClusters(bc);
                recursiveGraphTraversion(allConnectedBaseClusters, fc, bcVisited);
                set.add(fc);
            }
        }
        return set;
    }

    private void recursiveGraphTraversion(Set<BaseCluster> bcSet, FinalCluster fc, Set<BaseCluster> bcVisited) {
        if (bcSet != null) {
            for (Iterator<BaseCluster> iterator = bcSet.iterator(); iterator.hasNext();) {
                BaseCluster baseCluster = iterator.next();
                if (bcVisited.add(baseCluster)) {
                    fc.addBaseClusterWithoutCheck(baseCluster);
                    recursiveGraphTraversion(similarityMatrix.getAllConnectedBaseClusters(baseCluster), fc, bcVisited);
                }
            }
        } else {
//            System.err.println("Edge set is NULL!");
        }
    }
}



