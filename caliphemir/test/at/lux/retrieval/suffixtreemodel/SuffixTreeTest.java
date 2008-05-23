package at.lux.retrieval.suffixtreemodel;

import at.lux.fotoretrieval.lucene.Path;
import at.lux.fotoretrieval.lucene.Graph;
import at.lux.fotoretrieval.lucene.GraphPathExtractor;
import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * <p/>
 * Date: 15.02.2006 <br>
 * Time: 20:47:52 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class SuffixTreeTest extends TestCase {
    final static String GRAPH1 = "[9] [15] [26] [30] [31] [32] [locationOf 30 9] [locationOf 9 32] [locationOf 9 31] [locationOf 32 30] [locationOf 9 15] [locationOf 9 26] [timeOf 31 30]";
    final static String GRAPH2 = "[9] [15] [26] [30] [31] [locationOf 30 9] [locationOf 9 31] [locationOf 9 15] [locationOf 9 26] [timeOf 31 30]";

    public void testPathExtraction() {
        Graph g = new Graph(GRAPH1);
        for (int j = 1; j < 7; j++) {
            Path[] paths = GraphPathExtractor.extractPaths(g.toString(), j);
            System.out.println("Path length " + j);
            for (int i = 0; i < paths.length; i++) {
                Path path = paths[i];
                System.out.println(path);
            }

            System.out.println("---");
        }

        Path[] paths = GraphPathExtractor.extractPaths(g.toString(), -1);
        System.out.println("Path of all lengths");
        for (int i = 0; i < paths.length; i++) {
            Path path = paths[i];
            System.out.println(path);
        }

        System.out.println("---");
    }

    public void testStcOnSameGraph() {
        Graph g = new Graph(GRAPH1);
        SuffixTree st = new SuffixTree();
        st.addDocument(createSuffixTreeDocument(g));
        st.addDocument(createSuffixTreeDocument(g));

        double similarity = st.getSimilarity(SuffixTree.SimilarityType.Unweighted);
        System.out.println("Unweighted similarity = " + similarity);
        assertTrue(similarity == 1);

        st.resetSimilarity();
        st.addDocument(createSuffixTreeDocument(g));
        st.addDocument(createSuffixTreeDocument(g));
        assertTrue(similarity == st.getSimilarity());

        similarity = st.getSimilarity(SuffixTree.SimilarityType.TermFrequency);
        System.out.println("TermFrequency similarity = " + similarity);
        assertTrue(similarity == 1);

    }

    public void testStcOnTwoGraphs() {

        SuffixTree st = new SuffixTree();

        st.addDocument(createSuffixTreeDocument(new Graph(GRAPH1)));
        st.addDocument(createSuffixTreeDocument(new Graph(GRAPH2)));

        double similarity = st.getSimilarity(SuffixTree.SimilarityType.Unweighted);
        System.out.println("Unweighted similarity = " + similarity);
//        assertTrue(similarity == 1);

        similarity = st.getSimilarity(SuffixTree.SimilarityType.TermFrequency);
        System.out.println("TermFrequency similarity = " + similarity);

//        similarity = st.getSimilarity(SuffixTree.SimilartyType.TFIDF);
//        System.out.println("TermFrequency similarity = " + similarity);
//        assertTrue(similarity == 1);
    }

    private String createSuffixTreeDocument(Graph g) {
        Path[] paths = GraphPathExtractor.extractPaths(g.toString(), -1);
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < paths.length; i++) {
            Path path = paths[i];
            String pathString = path.toString();
            sb.append(pathString.substring(1, pathString.length()-1));
            sb.append('\n');
        }
        return sb.toString();
    }

    public void testTfIDF() throws IOException {
        Graph g = new Graph(GRAPH1);
        SuffixTree st = new SuffixTree();

        st.addCorpusDocument(createSuffixTreeDocument(new Graph(GRAPH1)));
        st.addCorpusDocument(createSuffixTreeDocument(new Graph(GRAPH2)));
/*

        st.addDocument(createSuffixTreeDocument(g));
        st.addDocument(createSuffixTreeDocument(g));

        double similarity = st.getSimilarity(SuffixTree.SimilartyType.TFIDF);
        System.out.println("similarity = " + similarity);

        st.resetSimilarity();

        st.addDocument(createSuffixTreeDocument(g));
        st.addDocument(createSuffixTreeDocument(new Graph(GRAPH2)));

        similarity = st.getSimilarity(SuffixTree.SimilartyType.TFIDF);
        System.out.println("similarity = " + similarity);

        st.resetSimilarity();
*/

        // train on corpus:
        IndexReader reader = IndexReader.open("testdata/idx_paths");
        System.out.println("Reading graphs from index ...");
        for (int i = 0; i< reader.numDocs(); i++) {
            Graph g_idx = new Graph(reader.document(i).getField("graph").stringValue());
            st.addCorpusDocument(createSuffixTreeDocument(g_idx));
        }
        System.out.println("Adding docs ...");
        st.addDocument(createSuffixTreeDocument(new Graph(reader.document(0).getField("graph").stringValue())));
        st.addDocument(createSuffixTreeDocument(new Graph(reader.document(1).getField("graph").stringValue())));
        System.out.println("Getting similarity ...");
        double similarity = st.getSimilarity(SuffixTree.SimilarityType.TFIDF);
        System.out.println("similarity = " + similarity);

        double dist[][] = new double[5][5];
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        for (int i = 0; i < dist.length; i++) {
            double[] doubles = dist[i];
            for (int j = 0; j < doubles.length; j++) {
                st.resetSimilarity();
                st.addDocument(createSuffixTreeDocument(new Graph(reader.document(i).getField("graph").stringValue())));
                st.addDocument(createSuffixTreeDocument(new Graph(reader.document(j).getField("graph").stringValue())));
                doubles[j] = st.getSimilarity(SuffixTree.SimilarityType.TermFrequency);
            }
        }
        for (int i = 0; i < dist.length; i++) {
            double[] doubles = dist[i];

            for (int j = 0; j < doubles.length; j++) {
                double aDouble = doubles[j];
                System.out.print(df.format(aDouble) + "\t");
            }
            System.out.println("");
        }
        System.out.println("--");
        for (int i = 0; i < dist.length; i++) {
            double[] doubles = dist[i];
            for (int j = 0; j < doubles.length; j++) {
                st.resetSimilarity();
                st.addDocument(createSuffixTreeDocument(new Graph(reader.document(i).getField("graph").stringValue())));
                st.addDocument(createSuffixTreeDocument(new Graph(reader.document(j).getField("graph").stringValue())));
                doubles[j] = st.getSimilarity(SuffixTree.SimilarityType.Unweighted);
            }
        }
        for (int i = 0; i < dist.length; i++) {
            double[] doubles = dist[i];

            for (int j = 0; j < doubles.length; j++) {
                double aDouble = doubles[j];
                System.out.print(df.format(aDouble) + "\t");
            }
            System.out.println("");
        }
        System.out.println("--");
        for (int i = 0; i < dist.length; i++) {
            double[] doubles = dist[i];
            for (int j = 0; j < doubles.length; j++) {
                st.resetSimilarity();
                st.addDocument(createSuffixTreeDocument(new Graph(reader.document(i).getField("graph").stringValue())));
                st.addDocument(createSuffixTreeDocument(new Graph(reader.document(j).getField("graph").stringValue())));
                doubles[j] = st.getSimilarity(SuffixTree.SimilarityType.TFIDF);
            }
        }
        for (int i = 0; i < dist.length; i++) {
            double[] doubles = dist[i];

            for (int j = 0; j < doubles.length; j++) {
                double aDouble = doubles[j];
                System.out.print(df.format(aDouble) + "\t");
            }
            System.out.println("");
        }
    }


}
