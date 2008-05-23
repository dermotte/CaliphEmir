package at.lux.retrieval.vectorspace;

import at.lux.fotoretrieval.lucene.Graph;
import at.lux.fotoretrieval.lucene.Path;
import at.lux.fotoretrieval.lucene.GraphPathExtractor;

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * A class for calculating the similarity based on a feature
 * vector having the paths of a graph as dimensions.<p/>
 * Date: 09.03.2006 <br>
 * Time: 13:44:07 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class GraphVectorSimilarity {
    private int maxLength = -1;
    private HashMap<String, Integer> idf = new HashMap<String, Integer>(128);
    private int numDocsInCorpus = 0;
    private Type type = Type.Unweighted;
    private int docLengthSum = 0;
    private boolean normalize = false;

    public enum Type {
        IdfWeighted,
        PlIdfWeighted, // Path length normalization ...
        BM25,
        Unweighted
    }

    /**
     * Default constructor, paths of all available lengths are used.
     */
    public GraphVectorSimilarity() {
    }

    /**
     * Use paths up to specified length for calculations
     *
     * @param maxLength maximum path length.
     */
    public GraphVectorSimilarity(int maxLength) {
        this.maxLength = maxLength;
    }

    public GraphVectorSimilarity(Type type, int maxLength) {
        this.type = type;
        this.maxLength = maxLength;
    }

    public GraphVectorSimilarity(Type type, int maxLength, boolean normalize) {
        this.type = type;
        this.maxLength = maxLength;
        this.normalize = normalize;
    }


    /**
     * Calculates and returns the silimilarity with default type
     *
     * @return the similarity with default type.
     */
    public double getSimilarity(Graph g1, Graph g2) {
        Path[] paths1 = getPaths(g1);
        Path[] paths2 = getPaths(g2);
        HashMap<String, Double> termVector1, termVector2;
        termVector1 = new HashMap<String, Double>(paths1.length);
        termVector2 = new HashMap<String, Double>(paths2.length);
        for (Path path : paths1) {
            if (type != Type.PlIdfWeighted) termVector1.put(path.toString(), 1.0);
            else {
//                if (path.getLength() > 0)
                termVector1.put(path.toString(), ((double) path.getLength() + 1));
            }
        }
        for (Path path : paths2) {
            if (type != Type.PlIdfWeighted) termVector2.put(path.toString(), 1.0);
            else {
//                if (path.getLength() > 0)
                termVector2.put(path.toString(), ((double) path.getLength() + 1));
            }
        }
        HashSet<String> terms = new HashSet<String>(Math.max(paths1.length, paths2.length));
        if (normalize) {
            normalizeTermVector(termVector1);
            normalizeTermVector(termVector2);
        }
        if (type != Type.BM25) {
            terms.addAll(termVector1.keySet());
            terms.addAll(termVector2.keySet());
            double sum = 0f;
            double sum1 = 0;
            double sum2 = 0;
            for (String dim : terms) {
                double factor1 = 0f;
                double factor2 = 0f;
                if (termVector1.containsKey(dim)) {
                    double entry = termVector1.get(dim);
                    if (type == Type.IdfWeighted) {
                        if (!idf.containsKey(dim))
                            throw new UnsupportedOperationException("Document has to be added to corpus first!");
                        double idfValue = Math.log((double) numDocsInCorpus / (double) idf.get(dim));
                        entry = entry * idfValue;
                    }
                    factor1 = entry;
                    sum1 += entry * entry;
                }
                if (termVector2.containsKey(dim)) {
                    double entry = termVector2.get(dim);
                    if (type == Type.IdfWeighted) {
                        if (!idf.containsKey(dim))
                            throw new UnsupportedOperationException("Document has to be added to corpus first!");
                        double idfValue = Math.log((double) numDocsInCorpus / (double) idf.get(dim));
                        entry = entry * idfValue;
                    }
                    factor2 = entry;
                    sum2 += entry * entry;
                }
                sum += factor1 * factor2;
            }
            double upper = sum;
            double lower = (float) Math.sqrt((float) sum1 * (float) sum2);
            return upper / lower;
        } else {
            double avdl = (double) docLengthSum / (double) numDocsInCorpus;
            double dl1 = termVector1.size();
            double dl2 = termVector2.size();
            double k1 = 1.5;
            double b = 0.5;
            double sum = 0.0;
/*
            for (String term : termVector1.keySet()) {
                if (termVector2.containsKey(term)) {
                    terms.add(term);
                }
            }
            for (String term : terms) {
                if (numDocsInCorpus < idf.get(term)) {
                    System.out.println(term + " -> " + idf.get(term));
                }
                assert(numDocsInCorpus >= idf.get(term));
                sum += getBm25Weight(k1, b, termVector1.get(term), idf.get(term), avdl, dl1) *
                        getBm25Weight(k1, b, termVector2.get(term), idf.get(term), avdl, dl2);
            }
            return sum;
*/
            terms.addAll(termVector1.keySet());
            terms.addAll(termVector2.keySet());
            double sum1 = 0;
            double sum2 = 0;
            for (String dim : terms) {
                double factor1 = 0f;
                double factor2 = 0f;
                if (termVector1.containsKey(dim)) {
                    double entry = termVector1.get(dim);
                    if (!idf.containsKey(dim))
                        throw new UnsupportedOperationException("Document has to be added to corpus first!");
//                    double idfValue = Math.log((double) numDocsInCorpus / (double) idf.get(dim));
                    entry = getBm25Weight(k1, b, termVector1.get(dim), idf.get(dim), avdl, dl1);
                    factor1 = entry;
                    sum1 += entry * entry;
                }
                if (termVector2.containsKey(dim)) {
                    double entry = termVector2.get(dim);
                    if (!idf.containsKey(dim))
                        throw new UnsupportedOperationException("Document has to be added to corpus first!");
//                    double idfValue = Math.log((double) numDocsInCorpus / (double) idf.get(dim));
                    entry = getBm25Weight(k1, b, termVector2.get(dim), idf.get(dim), avdl, dl2);
                    factor2 = entry;
                    sum2 += entry * entry;
                }
                sum += factor1 * factor2;
            }
            double upper = sum;
            double lower = (float) Math.sqrt((float) sum1 * (float) sum2);
            return upper / lower;
        }
    }

    private void normalizeTermVector(HashMap<String, Double> termVector) {
        double vectorLength = 0;
        for (String path: termVector.keySet()) {
            vectorLength += termVector.get(path) * termVector.get(path);
        }
        vectorLength = Math.sqrt(vectorLength);
        for (String path: termVector.keySet()) {
            termVector.put(path, termVector.get(path) / vectorLength);
        }
    }

    private double getBm25Weight(double k1, double b, double termFreq, double docFreq, double avgDocLength, double docLength) {
        assert(numDocsInCorpus >= docFreq);
        return ((k1 + 1.0) * termFreq) / (k1 * ((1 - b) + b * docLength / avgDocLength) + termFreq) * Math.log((numDocsInCorpus - docFreq + 0.5) / (docFreq + 0.5));
    }

    /**
     * Adds a Graph to the corpus for Calculation of IDF.
     *
     * @param g
     */
    public void addToCorpus(Graph g) {
        numDocsInCorpus++;
        Path[] paths = getPaths(g);
        docLengthSum += paths.length;
        for (Path path : paths) {
            String key = path.toString();
            if (idf.containsKey(key)) {
                idf.put(key, idf.get(key) + 1);
            } else {
                idf.put(key, 1);
            }
        }

    }

    private Path[] getPaths(Graph g) {
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
