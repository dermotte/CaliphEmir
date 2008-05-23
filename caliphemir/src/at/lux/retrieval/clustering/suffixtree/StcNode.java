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

import at.lux.retrieval.clustering.AbstractSuffixTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 03.06.2004
 *         Time: 15:30:41
 */

public class StcNode {
    private HashMap<String, StcNode> children = null;
    private HashSet<StcDocument> documents = null;
    // using as cache:
    private HashSet<StcDocument> allChildrensDocuments = null;

    public StcNode() {
        children = new HashMap<String, StcNode>();
        documents = new HashSet<StcDocument>();
        allChildrensDocuments = new HashSet<StcDocument>();
    }

    public void add(List<String> phrase, StcDocument stcDocument, int suffix, String prefix, AbstractSuffixTree tree) {
        // save state of being a base cluster:
        boolean isBaseClusterBeforeInsertion = tree.getNodeToClusterIndex().containsKey(this);
        if (isBaseClusterBeforeInsertion == true) {
            // Cluster has already been BaseCluster, so delete it before changes occur!!
            BaseCluster cluster = tree.getNodeToClusterIndex().remove(this); // remove from index and retrieve BaseCluster
            assert cluster != null;
            tree.getSimilarityMatrix().removeBaseCluster(cluster);
            boolean clusterRemove = tree.getBaseClusters().remove(cluster);
            assert clusterRemove;  // remove from tree
            tree.getTouchedClusters().remove(cluster);
        }

        // add new phrase:
        if (phrase.size() > 0) {
            String temp = phrase.get(0);
            String newPrefix = new String(prefix);
            if (newPrefix.length() > 0) {
                newPrefix += " " + phrase.remove(0).trim();
            } else {
                newPrefix += phrase.remove(0).trim();
            }
            if (children.containsKey(temp)) {
                // edge with string already exists -> add document
                children.get(temp).add(phrase, stcDocument, suffix, newPrefix, tree);
            } else {
                // edge does not exist -> create -> add document
                StcNode n = new StcNode();
                n.add(phrase, stcDocument, suffix, newPrefix, tree);
                children.put(temp, n);
            }
            // afterwards: update the childrensDocuments:
            allChildrensDocuments.addAll(children.get(temp).getAllChildrensDocuments());
//            allChildrensDocuments.addAll(children.get(temp).documents);
        } else { // add new StcDocument ...
            StcDocument doc = stcDocument;
            documents.add(doc);
        }

        // check if being a base cluster has been changed:
        if (isBaseClusterCandidate() == true && prefix.length() > 0) {
            // the node has changed and became a base cluster.
            // so we have to put it into the newly created base cluster treeset :)
            BaseCluster cluster = new BaseCluster(getAllChildrensDocuments(), this, prefix, tree.getIndex(), tree.getStopwords());
            if (cluster.getScore() > 0.0) {
                boolean addSuccessful = tree.getBaseClusters().add(cluster);
                assert addSuccessful;
                BaseCluster previousValue = tree.getNodeToClusterIndex().put(this, cluster);
                assert previousValue == null;
                boolean addToTouchedClusters = tree.getTouchedClusters().add(cluster);
                assert addToTouchedClusters;
                // we have to add it to the similarity matrix, so the values can be recalculated:
                tree.getSimilarityMatrix().addBaseCluster(cluster);
            }
        } else {
            // this is only a consistency check though there shouldn't be changes from being
            // a base cluster to being none.
            BaseCluster wasCluster = tree.getNodeToClusterIndex().get(this);
            assert wasCluster == null;
        }
    }

    /**
     * Returns all documents of itself and all descending nodes.
     *
     * @return
     */
    private HashSet<StcDocument> getAllChildrensDocuments() {
        // obviously we don't want this being null, cause this would kill performance :)
        if (allChildrensDocuments == null) {
            allChildrensDocuments = new HashSet<StcDocument>();
//            allChildrensDocuments.addAll(documents);
            Iterator<String> childrenIterator = children.keySet().iterator();
            while (childrenIterator.hasNext()) {
                String s = childrenIterator.next();
                StcNode n = children.get(s);
                allChildrensDocuments.addAll(n.getAllChildrensDocuments());
//                allChildrensDocuments.addAll(n.documents);
            }
        }
        HashSet<StcDocument> result = new HashSet<StcDocument>(allChildrensDocuments);
        result.addAll(documents);
        return result;
    }

    /**
     * Checks wether a node can be a base cluster
     *
     * @return true if it is a base cluster, false if not.
     */
    private boolean isBaseClusterCandidate() {
        boolean result = false;
        if (allChildrensDocuments.size() + documents.size() > 1 && !allChildrensDocuments.containsAll(documents) && (children.size() + documents.size() > 1)) {
            result = true;
        }
        return result;
    }

}