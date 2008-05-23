package at.lux.retrieval.evaluation;

import at.lux.fotoretrieval.lucene.Graph;
import at.lux.retrieval.graphisomorphism.FastSubgraphIsomorphism;
import at.lux.retrieval.graphisomorphism.NodeDistanceFunction;
import at.lux.retrieval.graphisomorphism.metrics.SimpleEdgeDistanceFunction;
import at.lux.retrieval.graphisomorphism.metrics.TermVectorNodeDistanceFunction;
import at.lux.retrieval.suffixtreemodel.SuffixTree;
import at.lux.retrieval.vectorspace.ElementTextVectorSimilarity;
import at.lux.retrieval.vectorspace.GraphVectorSimilarity;
import junit.framework.TestCase;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * <p/>
 * Date: 09.03.2006 <br>
 * Time: 10:55:47 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class SuffixTreeEvaluation extends TestCase {
    DecimalFormat df;
    private String[] testfiles = new String[]{
            "P1040584",   // 0
            "P1040587",   // 1
            "P1040588",   // 2
            "P1040589",   // 3
            "P1040599",   // 4
            "P1040601",   // 5
            "P1040615",   // 6
            "P1040629",   // 7
            "iknow_003",  // 8
            "iknow_005",  // 9
            "iknow_012",  // 10
            "iknow_018",  // 11
            "iknow_019",  // 12
            "iknow_044"   // 13
    };

    private List<String[]> pairs = new ArrayList<String[]>(20);

    private HashMap<String, Graph> file2Graph;
    SuffixTree st = new SuffixTree(SuffixTree.RelationType.NoRelations);

    LinkedList<Double> col2 = new LinkedList<Double>();
    LinkedList<Double> col3 = new LinkedList<Double>();
    LinkedList<Double> col1 = new LinkedList<Double>();
    LinkedList<Double> col4 = new LinkedList<Double>();
    LinkedList<Double> col5 = new LinkedList<Double>();
    protected SAXBuilder saxBuilder;
    private SuffixTree.PathType pathType = SuffixTree.PathType.BothDirections;
    private int maxLength = -1;
//    private int maxLength = -1;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        file2Graph = new HashMap<String, Graph>(100);
        IndexReader reader = IndexReader.open("testdata/idx_paths");
        System.out.println("Reading graphs from index ...");
        for (int i = 0; i < reader.numDocs(); i++) {
            Graph g_idx = new Graph(reader.document(i).getField("graph").stringValue());
            Field[] files = reader.document(i).getFields("file");
            for (int j = 0; j < files.length; j++) {
                String file = files[j].stringValue();
                file2Graph.put(file, g_idx);
                st.addCorpusDocument(SuffixTree.createSuffixTreeDocument(g_idx, pathType, maxLength));
            }
        }

        // create pairs:
        pairs.add(new String[]{testfiles[9], testfiles[6]});
        pairs.add(new String[]{testfiles[10], testfiles[1]});
        pairs.add(new String[]{testfiles[7], testfiles[11]});
        pairs.add(new String[]{testfiles[0], testfiles[1]});
        pairs.add(new String[]{testfiles[8], testfiles[9]});

        pairs.add(new String[]{testfiles[4], testfiles[1]});
//        pairs.add(new String[]{testfiles[2], testfiles[10]});    // old 7th pair
        pairs.add(new String[]{testfiles[12], testfiles[10]});    // new 7th pair
        pairs.add(new String[]{testfiles[13], testfiles[5]});
        pairs.add(new String[]{testfiles[0], testfiles[9]});
        pairs.add(new String[]{testfiles[3], testfiles[7]});

        pairs.add(new String[]{testfiles[13], testfiles[1]});
        pairs.add(new String[]{testfiles[8], testfiles[10]});
        pairs.add(new String[]{testfiles[7], testfiles[1]});
        pairs.add(new String[]{testfiles[4], testfiles[5]});
        pairs.add(new String[]{testfiles[0], testfiles[11]});

        pairs.add(new String[]{testfiles[13], testfiles[8]});
        pairs.add(new String[]{testfiles[3], testfiles[11]});
        pairs.add(new String[]{testfiles[0], testfiles[5]});
        pairs.add(new String[]{testfiles[12], testfiles[11]});
        pairs.add(new String[]{testfiles[4], testfiles[6]});

        saxBuilder = new SAXBuilder();

        df = (DecimalFormat) DecimalFormat.getInstance(Locale.GERMANY);
        df.setMaximumFractionDigits(18);
    }

    public void testSuffixTreeSimilarities() {
        for (String[] strings : pairs) {
            Graph g1 = file2Graph.get(getFile(strings[0]));
            Graph g2 = file2Graph.get(getFile(strings[1]));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g1, pathType, maxLength));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g2, pathType, maxLength));

            col1.add(st.getSimilarity(SuffixTree.SimilarityType.Unweighted));
            col2.add(st.getSimilarity(SuffixTree.SimilarityType.TermFrequency));
            col3.add(st.getSimilarity(SuffixTree.SimilarityType.TFIDF));
            col4.add(st.getSimilarity(SuffixTree.SimilarityType.IDF));
            st.resetSimilarity();
        }
        for (int i = 0; i < 20; i++) {
            System.out.println(df.format(col1.get(i)) + "\t" + df.format(col2.get(i)) + "\t" + df.format(col3.get(i)) + "\t" + df.format(col4.get(i)));
        }
    }

    public void testMcsSimilarities() {
        long ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (String[] strings : pairs) {
                Graph g1 = file2Graph.get(getFile(strings[0]));
                Graph g2 = file2Graph.get(getFile(strings[1]));

                g1.getMcsSimilarity(g2);
//            System.out.println(df.format(g1.getMcsSimilarity(g2)));
//            col2.add(st.getSimilarity(SuffixTree.SimilartyType.TermFrequency));
//            col3.add(st.getSimilarity(SuffixTree.SimilartyType.TFIDF));
//            col1.add(st.getSimilarity(SuffixTree.SimilartyType.Unweighted));

//            st.resetSimilarity();
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);
//        for (int i = 0; i<20; i++) {
//            System.out.println(df.format(col1.get(i)) + "\t"+ col2.get(i) + "\t" + col3.get(i));
//        }
    }

    public void testVectorSpace() throws IOException, JDOMException {
//        GraphVectorSimilarity vs0 = new GraphVectorSimilarity(GraphVectorSimilarity.Type.BM25, 0);
//        GraphVectorSimilarity vs1 = new GraphVectorSimilarity(GraphVectorSimilarity.Type.BM25, 1);
//        GraphVectorSimilarity vsAll = new GraphVectorSimilarity(GraphVectorSimilarity.Type.BM25, -1);

        GraphVectorSimilarity.Type weightingType = GraphVectorSimilarity.Type.BM25;
        GraphVectorSimilarity vs0 = new GraphVectorSimilarity(weightingType, 0);
        GraphVectorSimilarity vs1 = new GraphVectorSimilarity(weightingType, 1);
        GraphVectorSimilarity vs2 = new GraphVectorSimilarity(weightingType, 2);
        GraphVectorSimilarity vs3 = new GraphVectorSimilarity(weightingType, 2);
        GraphVectorSimilarity vsAll = new GraphVectorSimilarity(weightingType, 2);
//
        IndexReader reader = IndexReader.open("testdata/idx_paths");
        System.out.println("Reading graphs from index ...");
        for (int i = 0; i < reader.numDocs(); i++) {
            Graph g_idx = new Graph(reader.document(i).getField("graph").stringValue());
            Field[] files = reader.document(i).getFields("file");
            for (Field file : files) {
                vs0.addToCorpus(g_idx);
                vs1.addToCorpus(g_idx);
                vs2.addToCorpus(g_idx);
                vs3.addToCorpus(g_idx);
                vsAll.addToCorpus(g_idx);
            }
        }

        int count = 0;
        for (String[] strings : pairs) {
            count++;
            Graph g1 = file2Graph.get(getFile(strings[0]));
            Graph g2 = file2Graph.get(getFile(strings[1]));

            col1.add(vs0.getSimilarity(g1, g2));
            col2.add(vs1.getSimilarity(g1, g2));
            col3.add(vs2.getSimilarity(g1, g2));
            col4.add(vs3.getSimilarity(g1, g2));
            col5.add(vsAll.getSimilarity(g1, g2));
            st.resetSimilarity();
        }
        for (int i = 0; i < 20; i++) {
            System.out.println(df.format(col1.get(i)) + "\t" + df.format(col2.get(i)) + "\t" + df.format(col3.get(i)) + "\t" + df.format(col4.get(i)) + "\t" + df.format(col5.get(i)));
        }
    }

    public void testElementTextVectorSimilarity() throws IOException, JDOMException {
        IndexReader reader = IndexReader.open("testdata/idx_paths");

        System.out.println("Loading documents and adding them to corpus ...");
        ElementTextVectorSimilarity sim = new ElementTextVectorSimilarity();
        for (int i = 0; i < reader.numDocs(); i++) {
//            Graph g_idx = new Graph(reader.document(i).getField("graph").stringValue());
            Field[] files = reader.document(i).getFields("file");
            for (Field file : files) {
                Document d = null;
                try {
                    d = saxBuilder.build(file.stringValue());
                } catch (Exception e) {
                    System.err.println(file.stringValue());
                    e.printStackTrace();
                }
                sim.addToCorpus(d);
            }
        }
        for (String[] strings : pairs) {
            Document d1 = saxBuilder.build(getFile(strings[0]));
            Document d2 = saxBuilder.build(getFile(strings[1]));
            col1.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.Unweighted));
            col2.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.TfIdf));
            col3.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.BM25));
        }
        for (int i = 0; i < 20; i++) {
            System.out.println(df.format(col1.get(i)) + "\t" + df.format(col2.get(i)) + "\t" + df.format(col3.get(i)));
        }

    }

    public void testSubgraphIsomorphism() throws IOException, JDOMException {
        NodeDistanceFunction nDist = new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.CosinusCoefficient);
        getSubgraphIsomorphism(nDist, 0.4f, 1.0f);
        getSubgraphIsomorphism(nDist, 0.5f, 1.0f);
        getSubgraphIsomorphism(nDist, 0.5f, 1.5f);
        getSubgraphIsomorphism(nDist, 0.5f, 2.0f);
        getSubgraphIsomorphism(nDist, 0.5f, 2.5f);
//        getSubgraphIsomorphism(new BooleanNodeDistanceFunction(), 0.5f, 3.0f);

        getSubgraphIsomorphism(nDist, 0.4f, 1.0f);
        getSubgraphIsomorphism(nDist, 0.4f, 1.5f);
        getSubgraphIsomorphism(nDist, 0.4f, 2.0f);
        getSubgraphIsomorphism(nDist, 0.4f, 2.5f);
//        getSubgraphIsomorphism(new BooleanNodeDistanceFunction(), 0.4f, 3.0f);

        getSubgraphIsomorphism(nDist, 0.6f, 1.0f);
        getSubgraphIsomorphism(nDist, 0.6f, 1.5f);
        getSubgraphIsomorphism(nDist, 0.6f, 2.0f);
        getSubgraphIsomorphism(nDist, 0.6f, 2.5f);
//        getSubgraphIsomorphism(new BooleanNodeDistanceFunction(), 0.6f, 3.0f);
    }

    private void getSubgraphIsomorphism(NodeDistanceFunction nDist, float lambda, float maxDistance) throws JDOMException, IOException {
        System.out.println(nDist.getClass().getName() + ", lambda=" + lambda + ", maxdistance=" + maxDistance);
        FastSubgraphIsomorphism fsi1 = new FastSubgraphIsomorphism(
                nDist,
                new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Allow),
                lambda, maxDistance);
        FastSubgraphIsomorphism fsi2 = new FastSubgraphIsomorphism(
                nDist,
                new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Forbid),
                lambda, maxDistance);
        FastSubgraphIsomorphism fsi3 = new FastSubgraphIsomorphism(
                nDist,
                new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.WeightInverted),
                lambda, maxDistance);
        long ms = System.currentTimeMillis();
        for (String[] strings : pairs) {
            Document d1 = saxBuilder.build(getFile(strings[0]));
            Document d2 = saxBuilder.build(getFile(strings[1]));
            col1.add((double) fsi2.getDistance(d1, d2));
            System.out.print(".");
            col2.add((double) fsi1.getDistance(d1, d2));
            System.out.print(",");
            col3.add((double) fsi3.getDistance(d1, d2));
            System.out.print(";");
        }
        System.out.println("");
        for (int i = 0; i < 20; i++) {
            System.out.println(df.format(col1.get(i)) + "\t" + df.format(col2.get(i)) + "\t" + df.format(col3.get(i)));
        }
        System.out.println("time taken: " + (System.currentTimeMillis() - ms) / 1000f + " sec");
        System.out.println("");
    }


    private String getFile(String id) {
        for (Iterator<String> iterator = file2Graph.keySet().iterator(); iterator.hasNext();) {
            String s = iterator.next();
            if (s.indexOf(id) > 0) return s;
        }
        return null;
    }

    public void testSuffixTreeCalculationTime() {
        long ms = System.currentTimeMillis();
        for (String[] strings : pairs) {
            Graph g1 = file2Graph.get(getFile(strings[0]));
            Graph g2 = file2Graph.get(getFile(strings[1]));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g1, pathType, maxLength));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g2, pathType, maxLength));

            col1.add(st.getSimilarity(SuffixTree.SimilarityType.Unweighted));
//            col2.add(st.getSimilarity(SuffixTree.SimilarityType.TermFrequency));
//            col3.add(st.getSimilarity(SuffixTree.SimilarityType.TFIDF));
//            col4.add(st.getSimilarity(SuffixTree.SimilarityType.IDF));
            st.resetSimilarity();
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("Unweighted: ms = " + ms);
        ms = System.currentTimeMillis();
        for (String[] strings : pairs) {
            Graph g1 = file2Graph.get(getFile(strings[0]));
            Graph g2 = file2Graph.get(getFile(strings[1]));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g1, pathType, maxLength));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g2, pathType, maxLength));

//            col1.add(st.getSimilarity(SuffixTree.SimilarityType.Unweighted));
            col2.add(st.getSimilarity(SuffixTree.SimilarityType.TermFrequency));
//            col3.add(st.getSimilarity(SuffixTree.SimilarityType.TFIDF));
//            col4.add(st.getSimilarity(SuffixTree.SimilarityType.IDF));
            st.resetSimilarity();
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("TF: ms = " + ms);
        ms = System.currentTimeMillis();
        for (String[] strings : pairs) {
            Graph g1 = file2Graph.get(getFile(strings[0]));
            Graph g2 = file2Graph.get(getFile(strings[1]));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g1, pathType, maxLength));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g2, pathType, maxLength));

//            col1.add(st.getSimilarity(SuffixTree.SimilarityType.Unweighted));
//            col2.add(st.getSimilarity(SuffixTree.SimilarityType.TermFrequency));
            col3.add(st.getSimilarity(SuffixTree.SimilarityType.TFIDF));
//            col4.add(st.getSimilarity(SuffixTree.SimilarityType.IDF));
            st.resetSimilarity();
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("TFIDF: ms = " + ms);
        ms = System.currentTimeMillis();
        for (String[] strings : pairs) {
            Graph g1 = file2Graph.get(getFile(strings[0]));
            Graph g2 = file2Graph.get(getFile(strings[1]));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g1, pathType, maxLength));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g2, pathType, maxLength));

//            col1.add(st.getSimilarity(SuffixTree.SimilarityType.Unweighted));
//            col2.add(st.getSimilarity(SuffixTree.SimilarityType.TermFrequency));
//            col3.add(st.getSimilarity(SuffixTree.SimilarityType.TFIDF));
            col4.add(st.getSimilarity(SuffixTree.SimilarityType.IDF));
            st.resetSimilarity();
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("IDF: ms = " + ms);
    }

    public void testVectorSpaceCalculationTime() throws IOException, JDOMException {
        GraphVectorSimilarity.Type weightingType = GraphVectorSimilarity.Type.PlIdfWeighted;
        GraphVectorSimilarity vs0 = new GraphVectorSimilarity(weightingType, 0);
        GraphVectorSimilarity vs1 = new GraphVectorSimilarity(weightingType, 1);
        GraphVectorSimilarity vs2 = new GraphVectorSimilarity(weightingType, 2);
        GraphVectorSimilarity vs3 = new GraphVectorSimilarity(weightingType, 2);
        GraphVectorSimilarity vsAll = new GraphVectorSimilarity(weightingType, 2);
//
        IndexReader reader = IndexReader.open("testdata/idx_paths");
        System.out.println("Reading graphs from index ...");
        for (int i = 0; i < reader.numDocs(); i++) {
            Graph g_idx = new Graph(reader.document(i).getField("graph").stringValue());
            Field[] files = reader.document(i).getFields("file");
            for (Field file : files) {
                vs0.addToCorpus(g_idx);
                vs1.addToCorpus(g_idx);
                vs2.addToCorpus(g_idx);
                vs3.addToCorpus(g_idx);
                vsAll.addToCorpus(g_idx);
            }
        }
        long ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (String[] strings : pairs) {
                Graph g1 = file2Graph.get(getFile(strings[0]));
                Graph g2 = file2Graph.get(getFile(strings[1]));

                col1.add(vs0.getSimilarity(g1, g2));
//            col2.add(vs1.getSimilarity(g1, g2));
//            col3.add(vs2.getSimilarity(g1, g2));
//            col4.add(vs3.getSimilarity(g1, g2));
//            col5.add(vsAll.getSimilarity(g1, g2));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println(weightingType + " path length 0 ms = " + ms);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (String[] strings : pairs) {
                Graph g1 = file2Graph.get(getFile(strings[0]));
                Graph g2 = file2Graph.get(getFile(strings[1]));

//            col1.add(vs0.getSimilarity(g1, g2));
                col2.add(vs1.getSimilarity(g1, g2));
//            col3.add(vs2.getSimilarity(g1, g2));
//            col4.add(vs3.getSimilarity(g1, g2));
//            col5.add(vsAll.getSimilarity(g1, g2));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println(weightingType + " path length 1 ms = " + ms);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (String[] strings : pairs) {
                Graph g1 = file2Graph.get(getFile(strings[0]));
                Graph g2 = file2Graph.get(getFile(strings[1]));

//            col1.add(vs0.getSimilarity(g1, g2));
//            col2.add(vs1.getSimilarity(g1, g2));
                col3.add(vs2.getSimilarity(g1, g2));
//            col4.add(vs3.getSimilarity(g1, g2));
//            col5.add(vsAll.getSimilarity(g1, g2));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println(weightingType + " path length 2 ms = " + ms);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (String[] strings : pairs) {
                Graph g1 = file2Graph.get(getFile(strings[0]));
                Graph g2 = file2Graph.get(getFile(strings[1]));

//            col1.add(vs0.getSimilarity(g1, g2));
//            col2.add(vs1.getSimilarity(g1, g2));
                col3.add(vs2.getSimilarity(g1, g2));
//            col4.add(vs3.getSimilarity(g1, g2));
//            col5.add(vsAll.getSimilarity(g1, g2));
            }
        }

        ms = System.currentTimeMillis() - ms;
        System.out.println(weightingType + " path length 3 ms = " + ms);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (String[] strings : pairs) {
                Graph g1 = file2Graph.get(getFile(strings[0]));
                Graph g2 = file2Graph.get(getFile(strings[1]));

//            col1.add(vs0.getSimilarity(g1, g2));
//            col2.add(vs1.getSimilarity(g1, g2));
//            col3.add(vs2.getSimilarity(g1, g2));
                col4.add(vs3.getSimilarity(g1, g2));
//            col5.add(vsAll.getSimilarity(g1, g2));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println(weightingType + " path length 4 ms = " + ms);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (String[] strings : pairs) {
                Graph g1 = file2Graph.get(getFile(strings[0]));
                Graph g2 = file2Graph.get(getFile(strings[1]));

//            col1.add(vs0.getSimilarity(g1, g2));
//            col2.add(vs1.getSimilarity(g1, g2));
//            col3.add(vs2.getSimilarity(g1, g2));
//            col4.add(vs3.getSimilarity(g1, g2));
                col5.add(vsAll.getSimilarity(g1, g2));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println(weightingType + " path length all ms = " + ms);
    }

    public void testElementTextVectorSimilarityCalculationTime() throws IOException, JDOMException {
        IndexReader reader = IndexReader.open("testdata/idx_paths");

        System.out.println("Loading documents and adding them to corpus ...");
        ElementTextVectorSimilarity sim = new ElementTextVectorSimilarity();
        for (int i = 0; i < reader.numDocs(); i++) {
//            Graph g_idx = new Graph(reader.document(i).getField("graph").stringValue());
            Field[] files = reader.document(i).getFields("file");
            for (Field file : files) {
                Document d = saxBuilder.build(file.stringValue());
                sim.addToCorpus(d);
            }
        }

        long ms = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            for (String[] strings : pairs) {
                Document d1 = saxBuilder.build(getFile(strings[0]));
                Document d2 = saxBuilder.build(getFile(strings[1]));
                col1.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.Unweighted));
//            col2.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.TfIdf));
//            col3.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.BM25));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("VS unweighted ms = " + ms);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            for (String[] strings : pairs) {
                Document d1 = saxBuilder.build(getFile(strings[0]));
                Document d2 = saxBuilder.build(getFile(strings[1]));
//            col1.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.Unweighted));
                col2.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.TfIdf));
//            col3.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.BM25));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("VS TfIdf ms = " + ms);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            for (String[] strings : pairs) {
                Document d1 = saxBuilder.build(getFile(strings[0]));
                Document d2 = saxBuilder.build(getFile(strings[1]));
//            col1.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.Unweighted));
//            col2.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.TfIdf));
                col3.add(sim.getSimilarity(d1, d2, ElementTextVectorSimilarity.WeightType.BM25));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("VS BM25 ms = " + ms);

    }

    private void getSubgraphIsomorphismCalculationTime(NodeDistanceFunction nDist, float lambda, float maxDistance, SimpleEdgeDistanceFunction.EdgeInversionType edgeInversionType) throws JDOMException, IOException {
        System.out.println(nDist.getClass().getName() + ", lambda=" + lambda + ", maxdistance=" + maxDistance);
        FastSubgraphIsomorphism fsi1 = new FastSubgraphIsomorphism(
                nDist,
                new SimpleEdgeDistanceFunction(edgeInversionType),
                lambda, maxDistance);
//        FastSubgraphIsomorphism fsi2 = new FastSubgraphIsomorphism(
//                nDist,
//                new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Forbid),
//                lambda, maxDistance);
//        FastSubgraphIsomorphism fsi3 = new FastSubgraphIsomorphism(
//                nDist,
//                new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.WeightInverted),
//                lambda, maxDistance);
        long ms = System.currentTimeMillis();
        for (String[] strings : pairs) {
            Document d1 = saxBuilder.build(getFile(strings[0]));
            Document d2 = saxBuilder.build(getFile(strings[1]));
//            col1.add((double) fsi2.getDistance(d1, d2));
//            System.out.print(".");
            col2.add((double) fsi1.getDistance(d1, d2));
//            System.out.print(",");
//            col3.add((double) fsi3.getDistance(d1, d2));
//            System.out.print(";");
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);
    }

    public void testSubgraphIsomorphismCalculationTime() throws IOException, JDOMException {
//        getSubgraphIsomorphismCalculationTime(new BooleanNodeDistanceFunction(), 0.5f, 1.5f, SimpleEdgeDistanceFunction.EdgeInversionType.Allow);
//        getSubgraphIsomorphismCalculationTime(new BooleanNodeDistanceFunction(), 0.5f, 1.5f, SimpleEdgeDistanceFunction.EdgeInversionType.WeightInverted);
//        getSubgraphIsomorphismCalculationTime(new BooleanNodeDistanceFunction(), 0.5f, 1.5f, SimpleEdgeDistanceFunction.EdgeInversionType.Forbid);
//
        getSubgraphIsomorphismCalculationTime(new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.CosinusCoefficient), 0.5f, 1.5f, SimpleEdgeDistanceFunction.EdgeInversionType.Allow);
        getSubgraphIsomorphismCalculationTime(new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.CosinusCoefficient), 0.5f, 1.5f, SimpleEdgeDistanceFunction.EdgeInversionType.WeightInverted);
        getSubgraphIsomorphismCalculationTime(new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.CosinusCoefficient), 0.5f, 1.5f, SimpleEdgeDistanceFunction.EdgeInversionType.Forbid);
    }

}
