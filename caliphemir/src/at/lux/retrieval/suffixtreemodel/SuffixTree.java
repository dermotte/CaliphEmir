package at.lux.retrieval.suffixtreemodel;

import at.lux.fotoretrieval.lucene.Relation;
import at.lux.fotoretrieval.lucene.Graph;
import at.lux.fotoretrieval.lucene.Path;
import at.lux.fotoretrieval.lucene.GraphPathExtractor;

import java.util.*;

/**
 * <p/>
 * Date: 15.02.2006 <br>
 * Time: 20:31:45 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class SuffixTree {
    /**
     * Defines the way relations are handled in this tree. Either they are left out or they
     * are only used as types with no direction indicator, or they are used as they appear in
     * the path.
     */
    public enum RelationType {NoRelations, UndirectedRelation, FullRelations}

    /**
     * Defines the relationType of similarity being used.
     */
    public enum SimilarityType {Unweighted, TermFrequency, TFIDF, IDF }

    /**
     * Defines if paths should be added only in one direction or in both directions.
     */
    public enum PathType {SingleDirection, BothDirections}

    private RelationType relationType = RelationType.NoRelations;
    private PathType pathType = PathType.SingleDirection;

    private SuffixTreeNode rootNode;

    private int docCount = 0;

    private int corpusDocCount = 0;

    public SuffixTree() {
        init();
    }

    public SuffixTree(RelationType relationType) {
        this.relationType = relationType;
        init();
    }

    private void init() {
        rootNode = new SuffixTreeNode("_root");
    }

    /**
     * Adds a document with given id
     * @param text the document content
     */
    public void addDocument(String text) {
        if (docCount<2) {
            docCount++;
            addDocument(text, docCount);
        } else {
            throw new UnsupportedOperationException("Only two documents can be added.");
        }
    }

    public void addDocument(String text, int id) {
        if (!(id == 1 || id == 2)) {
            throw new UnsupportedOperationException("This is not meant to work like this ...");
        }
        String[] sentences = getSentences(text);
        for (int i = 0; i < sentences.length; i++) {
            List<String> tokens = getTokens(sentences[i]);
            while (tokens.size()>0) {
                rootNode.addSuffix(tokens, id);
                tokens.remove(0);
            }
        }
    }

    /**
     * Calculates and returns the silimilarity with default relationType
     * @return the similarity with default relationType.
     */

    public double getSimilarity() {
        return getSimilarity(SimilarityType.Unweighted);
    }

    public double getSimilarity(SimilarityType type) {
        if (docCount!=2) {
            throw new UnsupportedOperationException("Excactly 2 documents have to be added!");
        }
        double result = 0;
        if (type == SimilarityType.Unweighted) {
            HashMap<Integer, Integer> doc2docEdgeCount = new HashMap<Integer, Integer>(2);
            doc2docEdgeCount.put(-1,0);
            doc2docEdgeCount.put(1,0);
            doc2docEdgeCount.put(2,0);
            rootNode.getEdgesTraversed(doc2docEdgeCount);
            double bothDocsTraversed = ((double) doc2docEdgeCount.get(-1));
            double doc1Traversed = ((double) doc2docEdgeCount.get(1));
            double doc2Traversed = ((double) doc2docEdgeCount.get(2));
            result = bothDocsTraversed / Math.max(doc2Traversed, doc1Traversed);
        } else if (type == SimilarityType.TermFrequency) {
            TermFrequencyWalker walker = new TermFrequencyWalker();
            rootNode.traverseEdges(walker, SimilarityType.TermFrequency);
            result = walker.getSum() * (1d / (double) walker.getCountEdges());
        } else if (type == SimilarityType.TFIDF || type == SimilarityType.IDF) {
            if (getSimilarity(SimilarityType.Unweighted)==1) return 1;
            TermFrequencyWalker walker = new TermFrequencyWalker();
            if (corpusDocCount<1)
                throw new UnsupportedOperationException("For TF*IDF a corpus has to be applied to the Suffix Tree!");
            walker.setCountCorpusDocuments(corpusDocCount);
            rootNode.traverseEdges(walker, type);
            result = walker.getSum() * (1d / (double) walker.getCountEdges());
        }
        return result;
    }

    /**
     * Use this method to train a suffix tree for a corpus. This allows the usage of TF*IDF,
     * as the inverse document frequecy can be calculated.
     * @param text
     */
    public void addCorpusDocument(String text) {
        corpusDocCount++;
        String[] sentences = getSentences(text);
        for (String sentence : sentences) {
            List<String> tokens = getTokens(sentence);
            while (!tokens.isEmpty()) {
                rootNode.prepareDocumentFrequency(tokens, corpusDocCount);
                tokens.remove(0);
            }
        }
    }

    protected String[] getSentences(String phrase) {
        String[] result = phrase.split("\\n");
        return result;
    }

    protected List<String> getTokens(String sentence) {
        String[] result = sentence.split("\\s");
        List<String> resultList = null;
        if (relationType == RelationType.NoRelations) {
            // strip all relations from the array:
            ArrayList<String> r = new ArrayList<String>(result.length / 2 + 1);
            for (int i = 0; i < result.length; i++) {
                String node = result[i];
                // if the node is a number in square brackets
                if (node.matches("\\d+")) {
                    r.add(node);
                }
            }
            resultList = r;
        } else if (relationType == RelationType.FullRelations) {
            ArrayList<String> r = new ArrayList<String>(result.length);
            for (int i = 0; i < result.length; i++) {
                String node = result[i];
                // if the node is a number in square brackets
                if (node!=null) {
                    r.add(node);
                }
            }
            resultList = r;
        } else if (relationType == RelationType.UndirectedRelation) {
            // strip all relations from the array:
            ArrayList<String> r = new ArrayList<String>(result.length);
            for (int i = 0; i < result.length; i++) {
                String node = result[i];
                // if the node is a number in square brackets
                if (node != null && node.matches("\\d+")) {
                    r.add(node);
                } else {
                    // invert the relation if it is no key of the mapping table.
                    if (!Relation.relationMapping.containsKey(node))
                        node = Relation.invertRelationType(node);
                    r.add(node);
                }
            }
            resultList = r;
        }
        return resultList;
    }

    /**
     * Used to create a SuffixTree document from a Graph object
     * @param g the graph
     * @return a string representing the document built from the paths.
     */
    public static String createSuffixTreeDocument(Graph g) {
        return createSuffixTreeDocument(g, PathType.SingleDirection, -1);
    }

    /**
     * Used to create a SuffixTree document from a Graph object
     * @param g the graph
     * @return a string representing the document built from the paths.
     */
    public static String createSuffixTreeDocument(Graph g, PathType pathType, int maxLength) {
        Path[] paths = GraphPathExtractor.extractPaths(g.toString(), maxLength);
        HashSet<String> tmp = new HashSet<String>(paths.length);
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < paths.length; i++) {
            Path path = paths[i];
            String pathString = path.toString();
            if (!tmp.contains(pathString)) {
                sb.append(pathString.substring(1, pathString.length() - 1));
                sb.append('\n');
                tmp.add(pathString);
            }
            if (pathType == PathType.BothDirections) {
                pathString = path.toString(true);
                if (!tmp.contains(pathString)) {
                    sb.append(pathString.substring(1, pathString.length() - 1));
                    sb.append('\n');
                    tmp.add(pathString);
                }
            }
        }
        return sb.toString();
    }
    public static String createSuffixTreeDocument(Graph g, PathType pathType) {
        return createSuffixTreeDocument(g, pathType, -1);
    }

    /**
     * Used to create a SuffixTree document from a Graph object
     * @param g the graph
     * @return a string representing the document built from the paths.
     */
    public static String createSuffixTreeDocument(Graph g, int maxLength) {
        return createSuffixTreeDocument(g, PathType.SingleDirection, maxLength);
    }

    /**
     * Allows to use the very same suffix tree for another similarity
     * calculation by deleting the inserted documents.
     */
    public void resetSimilarity() {
        rootNode.resetSimilarity();
        docCount = 0;
    }

    private static Path[] getPaths(Graph g, int maxLength) {
        ArrayList<Path> paths = new ArrayList<Path>();
        HashSet<String> test = new HashSet<String>();
        if (maxLength < 0) {
            Path[] tmpPaths = GraphPathExtractor.extractPaths(g.toString(), -1);
            for (int i = 0; i < tmpPaths.length; i++) {
                if (!test.contains(tmpPaths[i].toString())) {
                    test.add(tmpPaths[i].toString());
                    paths.add(tmpPaths[i]);
                } else {
//                    System.out.println("tmpPaths["+i+"] = " + tmpPaths[i].toString());
                }
            }
        } else {
            for (int i = 0; i <= maxLength; i++) {
                Path[] tmpPaths = GraphPathExtractor.extractPaths(g.toString(), i);
                for (int j = 0; j < tmpPaths.length; j++) {
                    if (!test.contains(tmpPaths[j].toString())) {
                        test.add(tmpPaths[j].toString());
                        paths.add(tmpPaths[j]);
                    } else {
//                        System.out.println("tmpPaths["+j+"] = " + tmpPaths[j].toString());
                    }
                }
            }
        }
        return (Path[]) paths.toArray(new Path[1]);
    }

}
