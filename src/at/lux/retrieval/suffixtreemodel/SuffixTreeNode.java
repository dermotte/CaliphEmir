package at.lux.retrieval.suffixtreemodel;

import java.util.*;
import java.util.logging.Logger;

/**
 * <p/>
 * Date: 15.02.2006 <br>
 * Time: 20:31:57 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class SuffixTreeNode {
    private Logger log = Logger.getLogger(SuffixTreeNode.class.getName());
    private String token;
    /**
     * Stores how often a single document has traversed given edge.
     * Used for similarity calculation
     */
    private HashMap<String, HashMap<Integer, Integer>> edge2document = new HashMap<String, HashMap<Integer, Integer>>();
    /**
     * Stores which documents have traversed the edge.
     * This is only created to allow calculation of the IDF value
     * based on a corpus.
     */
    private HashMap<String, HashSet<Integer>> idfCounter = new HashMap<String, HashSet<Integer>>();
    private HashMap<String, SuffixTreeNode> edges = new HashMap<String, SuffixTreeNode>();

    public SuffixTreeNode(String token) {
        this.token = token;
    }

    /**
     * @param tokens the suffix
     * @param id     the document id
     */
    public void addSuffix(List<String> tokens, int id) {
//        System.out.println("tokens = " + tokens);
        String t = tokens.get(0);
        // add edge if not here
        if (!edges.containsKey(t)) {
            edges.put(t, new SuffixTreeNode(t));
        }
        if (!edge2document.containsKey(t)) {
            HashMap<Integer, Integer> docs = new HashMap<Integer, Integer>(3);
            edge2document.put(t, docs);
        }
        if (!edge2document.get(t).containsKey(id)) {
            edge2document.get(t).put(id, 0);
        }
        // add id to edge:
        edge2document.get(t).put(id, edge2document.get(t).get(id) + 1);
        // add suffices if there is more than one token left:
        if (tokens.size() > 1) {
            ArrayList<String> tokensLeft = new ArrayList<String>(tokens.size());
            tokensLeft.addAll(tokens);
            tokensLeft.remove(0);
            edges.get(t).addSuffix(tokensLeft, id);
        }
    }

    public boolean isLeaf() {
        return (edges.isEmpty());
    }

    /**
     * Calculates how many edges have been created or traversed by document with id.
     * stores the edges which were traversed by both in doc id "-1";
     *
     * @param document2edgeCount
     */
    public void getEdgesTraversed(HashMap<Integer, Integer> document2edgeCount) {
        // go through all local edges ...
        for (String edge : edge2document.keySet()) {
            Set<Integer> documents = edge2document.get(edge).keySet();
            for (Integer docID : documents) {
                // increment for document
                document2edgeCount.put(docID, document2edgeCount.get(docID) + 1);
            }
            if (documents.size() > 1) {
                // increment count for doc with id -1 (the edge count).
                document2edgeCount.put(-1, document2edgeCount.get(-1) + 1);
            }
        }
        // ask all children, which are not leafs ...
        for (String token : edges.keySet()) {
            SuffixTreeNode node = edges.get(token);
            if (!node.isLeaf() && edge2document.containsKey(token)) {
                node.getEdgesTraversed(document2edgeCount);
            }
        }
    }

    public void traverseEdges(TermFrequencyWalker walker, SuffixTree.SimilarityType similarityType) {
        // go through all local edges ...
        for (String s : edge2document.keySet()) {
            Set<Integer> documents = edge2document.get(s).keySet();
            int min = Integer.MAX_VALUE;
            int max = 0;
            if (edge2document.get(s).size() >1) {
                for (Integer docID : documents) {
                    Integer travCount = edge2document.get(s).get(docID);
                    min = Math.min(min, travCount);
                    max = Math.max(max, travCount);
                }
            } else {
                min = 0;
                max = 1;
            }
            if (similarityType == SuffixTree.SimilarityType.TermFrequency) {
                // its term frequeny:
                walker.addToSum((double) min / (double) max);
            } else if (similarityType == SuffixTree.SimilarityType.IDF) {
                int documentFrequency;
                if (!idfCounter.containsKey(s)) {
                    // Note: the documents to check against were obviously not in the corpus!!
                    documentFrequency = edge2document.get(s).keySet().size();
                    log.warning("Note: the documents for similarity calculation were obviously not in the corpus!!");
                } else {
                    documentFrequency = idfCounter.get(s).size();
                }
                double idf = Math.log((double) walker.getCountCorpusDocuments() / (double) documentFrequency);
                // IDF factor is normalized to stay in the [0,1] area.
                if (min > 0)
                    walker.addToSum(idf);
            } else if (similarityType == SuffixTree.SimilarityType.TFIDF) {
                // its TF*IDF:
                double factor = (double) min / (double) max;
                int documentFrequency;
                if (!idfCounter.containsKey(s)) {
                    // Note: the documents to check against were obviously not in the corpus!!
                    documentFrequency = edge2document.get(s).keySet().size();
                    log.warning("Note: the documents for similarity calculation were obviously not in the corpus!!");
                } else {
                    documentFrequency = idfCounter.get(s).size();
                }
//                double maxIdf = Math.log((double) walker.getCountCorpusDocuments());
                double idf = Math.log((double) walker.getCountCorpusDocuments() / (double) documentFrequency);
//                assert(factor <= 1.0);
//                assert(idf / maxIdf <= 1.0);
                // IDF factor is normalized to stay in the [0,1] area.
                walker.addToSum(factor * idf);
//                walker.addToSum(factor * (idf / maxIdf));
            } else
                throw new UnsupportedOperationException("Not implemened yet!");
            walker.incrementCountEdges();
        }
        // ask all children, which are not leafs and have edges for given docs ...
        for (String token : edges.keySet()) {
            SuffixTreeNode node = edges.get(token);
            if (!node.isLeaf() && edge2document.containsKey(token)) {
                node.traverseEdges(walker, similarityType);
            }
        }
    }

    /**
     * Allows to train the tree for a specific document corpus on the document frequency for
     * later idf calculation
     *
     * @param tokens
     * @param id
     */
    public void prepareDocumentFrequency(List<String> tokens, int id) {
        String t = tokens.get(0);
        // add edge if not here
        if (!edges.containsKey(t)) {
            edges.put(t, new SuffixTreeNode(t));
        }
        if (!idfCounter.containsKey(t)) {
            idfCounter.put(t, new HashSet<Integer>());
        }
        idfCounter.get(t).add(id);
        if (tokens.size() > 1) {
            ArrayList<String> tokensLeft = new ArrayList<String>(tokens.size());
            tokensLeft.addAll(tokens);
            tokensLeft.remove(0);
            edges.get(t).prepareDocumentFrequency(tokensLeft, id);
        }
    }

    public void resetSimilarity() {
        // reset edge2document
        edge2document = new HashMap<String, HashMap<Integer, Integer>>();
        // reset all childs:
        for (String edge : edges.keySet()) {
            SuffixTreeNode node = edges.get(edge);
            if (!node.isLeaf()) node.resetSimilarity();
        }
    }
}
