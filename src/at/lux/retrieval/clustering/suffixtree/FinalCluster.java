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

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 04.06.2004
 *         Time: 14:47:14
 */

public class FinalCluster implements Comparable {
    private HashSet<BaseCluster> baseClusters = null;
    private HashSet<StcDocument> documents = null;

    public double getScore() {
        return score;
    }

    private double score = 0.0d;

    private FinalCluster() {
        init();
    }

    public FinalCluster(BaseCluster clusters) {
        init();
        baseClusters.add(clusters);
        documents.addAll(clusters.getDocuments());
        updateScore();
    }

    private void init() {
        baseClusters = new HashSet<BaseCluster>();
        documents = new HashSet<StcDocument>();
        score = 0;
    }

    /**
     * Returns all phrases of alle base clusters
     *
     * @return
     */
    public String getPhrases() {
        Iterator<BaseCluster> clusterIterator = baseClusters.iterator();
        StringBuilder overallPhrase = new StringBuilder(256);
        while (clusterIterator.hasNext()) {
            BaseCluster b = clusterIterator.next();
            overallPhrase.append(b.getPhrase());
            if (clusterIterator.hasNext()) overallPhrase.append(", ");
        }
        return overallPhrase.toString();
    }

    /**
     * Returns the phrase with the maximum score
     *
     * @return
     */
    public String getDominantPhrase() {
        Iterator<BaseCluster> clusterIterator = baseClusters.iterator();
        String overallPhrase = "";
        double maxPhraseScore = 0d;
        while (clusterIterator.hasNext()) {
            BaseCluster b = clusterIterator.next();
            if (b.getPhraseScore() > maxPhraseScore) {
                overallPhrase = b.getPhrase();
                maxPhraseScore = b.getPhraseScore();
            }
        }
        return overallPhrase;
    }

    public String toString() {
        String result = score + ": " + getPhrases() + " -";
        for (Iterator<StcDocument> iterator = documents.iterator(); iterator.hasNext();) {
            result += " " + iterator.next().toString();
        }
        return result;
    }

    /**
     * Using this method you can boost the ranking of the final clusters.
     */
    private void updateScore() {
        Iterator<BaseCluster> clusterIterator = baseClusters.iterator();
        score = 0.0d;
        while (clusterIterator.hasNext()) {
            BaseCluster baseCluster = clusterIterator.next();
            score = score + baseCluster.getScore();
//            score = score + baseCluster.getPhraseScore();
        }
        score = score * ((double) documents.size());
    }

    /**
     * Adds a BaseCluster to the FinalCluster without checking similarity Constraints.
     *
     * @param cluster
     */
    public void addBaseClusterWithoutCheck(BaseCluster cluster) {
        BaseCluster b = cluster;
        baseClusters.add(b);
        documents.addAll(b.getDocuments());
        updateScore();
    }

    /**
     * For implementing interface Comparable
     *
     * @param o gives the Object to compare to
     * @return an int for creating an ordering
     */
    public int compareTo(Object o) {
        int result = System.identityHashCode(this) - System.identityHashCode(o);
        if (o.equals(this))
            return 0;
        else if (o instanceof FinalCluster) {
            FinalCluster b = (FinalCluster) o;
            if (getScore() > b.getScore())
                return -1;
            else if (getScore() < b.getScore())
                return 1;
        }
        return result;
    }

    /**
     * Getter for the BaseClusters
     *
     * @return a set of Baseclusters that were merged for creating this final cluster
     */
    public HashSet<BaseCluster> getBaseClusters() {
        return baseClusters;
    }


    /**
     * Getter for the contained documents
     *
     * @return a set of documernts that are in this final cluster
     */
    public HashSet<StcDocument> getDocuments() {
        return documents;
    }
}

