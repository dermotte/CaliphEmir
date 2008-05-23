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
package at.lux.fotoretrieval.retrievalengines;

import at.lux.fotoretrieval.ResultListEntry;
import at.lux.fotoretrieval.lucene.Graph;
import at.lux.fotoretrieval.lucene.Node;
import at.lux.fotoretrieval.lucene.similarity.TermFrequencySimilarity;
import junit.framework.TestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.*;

/**
 * Date: 26.03.2005
 * Time: 00:14:14
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LucenePathIndexRetrievalEngineTest extends TestCase {
    private LucenePathIndexRetrievalEngine engine;
    private final String pathToIndex = "testdata";
//    private final String pathToIndex = "C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\testdata";

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        engine = new LucenePathIndexRetrievalEngine(50);
    }

    public void testCreateIndex() {
        engine.indexFilesSemantically(pathToIndex, null);
        try {
            IndexReader reader = IndexReader.open(pathToIndex + "/idx_paths");
            for (int i = 0; i< reader.numDocs(); i++) {
                System.out.println(reader.document(i).get("graph"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testSearch() {
        try {
            QueryParser qParser = new QueryParser("graph", new WhitespaceAnalyzer());
            IndexSearcher search = new IndexSearcher("C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\testdata\\idx_paths");
            Hits h = search.search(qParser.parse("_*_0_1"));
            for (int i = 0; i < h.length(); i++) {
                System.out.println(h.score(i) + ": " + h.doc(i).get("graph"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testSemanticSearch() {
        List<ResultListEntry> result = engine.getImagesBySemantics("[\"Mathias Lux\"] [Talking] [\"Michael Granitzer\"] patientOf 1 2 agent 2 3", null, pathToIndex, true, null);
        for (Iterator<ResultListEntry> iterator = result.iterator(); iterator.hasNext();) {
            ResultListEntry entry = iterator.next();
            System.out.println(entry.getRelevance() + ": " + entry.getDescriptionPath());
        }
    }

    public void testPrecisionAndRecall() {
        try {
            String repository = "C:\\Java\\JavaProjects\\CaliphEmir\\testdata";
//            String repository = "C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\testdata";
            IndexSearcher is = new IndexSearcher(repository + "\\idx_paths");
            IndexReader ir = IndexReader.open(repository + "\\idx_paths");

            for (int i = 0; i<ir.numDocs(); i++) {
                testQuery(ir, new Graph(ir.document(i).getValues("graph")[0]), is);
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void testPrecisionAndRecallFullText() {
        try {
            String repository = "C:\\Java\\JavaProjects\\CaliphEmir\\testdata";
//            String repository = "C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\testdata";
            IndexSearcher is = new IndexSearcher(repository + "\\idx_paths");
            IndexReader ir = IndexReader.open(repository + "\\idx_paths");

            for (int i = 0; i<ir.numDocs(); i++) {
                testDirectQuery(ir, new Graph(ir.document(i).getValues("graph")[0]), is);
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void testQuery(IndexReader ir, Graph query, IndexSearcher is) throws IOException, ParseException {
        // create results from mcs:
        LinkedList<ResultHolder> resultsMcs = new LinkedList<ResultHolder>();
        for (int j = 0; j < ir.numDocs(); j++) {
            Graph model = new Graph(ir.document(j).getValues("graph")[0]);
            float mcsSimilarity = query.getMcsSimilarity(model);
            resultsMcs.add(new ResultHolder(j, model.toString(), mcsSimilarity));
        }
        Collections.sort(resultsMcs);
//            for (Iterator<ResultHolder> iterator = resultsMcs.iterator(); iterator.hasNext();) {
//                ResultHolder r = iterator.next();
//                System.out.println(r.getDocumentNumber() + ": " + r.getSimilarity());
//            }

        // create results from search:

        // set to another similarity if necessary:
        is.setSimilarity(new TermFrequencySimilarity());
//        is.setSimilarity(new SimpleTfIdfSimilarity());

        LucenePathIndexRetrievalEngine engine = new LucenePathIndexRetrievalEngine(50);
        String gQuery = LucenePathIndexRetrievalEngine.createLucenePathQuery(query);
//        System.out.println(query);
        QueryParser qParse = new QueryParser("paths", new WhitespaceAnalyzer());
        Query q = qParse.parse(gQuery);
        Hits hits = is.search(q);
        LinkedList<ResultHolder> resultsSearch = new LinkedList<ResultHolder>();
        for (int i = 0; i < hits.length(); i++) {
            String graph = hits.doc(i).getValues("graph")[0];
            int docID = -1;
            for (int j = 0; j < ir.numDocs(); j++) {
                Graph model = new Graph(ir.document(j).getValues("graph")[0]);
                if (model.toString().equals(graph)) docID = j;
            }
            resultsSearch.add(new ResultHolder(docID, graph, hits.score(i)));
        }
        Collections.sort(resultsSearch);
        printPrecisionRecallPlot(resultsMcs, resultsSearch);
    }

    private void testDirectQuery(IndexReader ir, Graph query, IndexSearcher is) throws IOException, ParseException {
        IndexReader reader = IndexReader.open("C:\\Java\\JavaProjects\\CaliphEmir\\testdata\\idx_semantic");
        IndexSearcher searcher = new IndexSearcher("C:\\Java\\JavaProjects\\CaliphEmir\\testdata\\idx_fulltext");

        HashMap<Integer, String> node2label = new HashMap<Integer, String>();
        for (int j = 0; j < reader.numDocs(); j++) {
            String id = reader.document(j).getValues("id")[0];
            String label = reader.document(j).getValues("label")[0];
            node2label.put(Integer.parseInt(id), label);
        }
        // create results from mcs:
        LinkedList<ResultHolder> resultsMcs = new LinkedList<ResultHolder>();
        for (int j = 0; j < ir.numDocs(); j++) {
            Graph model = new Graph(ir.document(j).getValues("graph")[0]);
            float mcsSimilarity = query.getMcsSimilarity(model);
            String[] file = ir.document(j).getValues("file");
            for (int i = 0; i < file.length; i++) {
                String s = file[i];
                resultsMcs.add(new ResultHolder(mcsSimilarity, s));
            }
        }
        Collections.sort(resultsMcs);
//            for (Iterator<ResultHolder> iterator = resultsMcs.iterator(); iterator.hasNext();) {
//                ResultHolder r = iterator.next();
//                System.out.println(r.getDocumentNumber() + ": " + r.getSimilarity());
//            }

        // create results from search:
        StringBuilder qBuilder = new StringBuilder(64);
        for (Iterator<Node> iterator = query.getNodes().iterator(); iterator.hasNext();) {
            Node node = iterator.next();
//            qBuilder.append("\"");
            qBuilder.append(node2label.get(node.getNodeID()));
            qBuilder.append(" ");
//            qBuilder.append("\" ");
        }
//        System.out.println(query);
        QueryParser qParse = new QueryParser("all", new WhitespaceAnalyzer());
        Query q = qParse.parse(qBuilder.toString());
        Hits hits = searcher.search(q);
        LinkedList<ResultHolder> resultsSearch = new LinkedList<ResultHolder>();
        for (int i = 0; i < hits.length(); i++) {
            String graph = hits.doc(i).getValues("file")[0];
//            int docID = -1;
//            for (int j = 0; j < ir.numDocs(); j++) {
//                Graph model = new Graph(ir.document(j).getValues("graph")[0]);
//                if (model.toString().equals(graph)) docID = j;
//            }
            resultsSearch.add(new ResultHolder(hits.score(i), graph));
        }
        Collections.sort(resultsSearch);
        printPrecisionRecallPlotFileBased(resultsMcs, resultsSearch);
    }

    public static String printPrecisionRecallPlot(LinkedList<ResultHolder> mcs, LinkedList<ResultHolder> search) {
        int numLevels = 10;
        List<ResultHolder> optimal = mcs.subList(0, numLevels);
        HashSet<Integer> firstOptimalResultsDocIDs = new HashSet<Integer>(numLevels);
        for (Iterator<ResultHolder> iterator = optimal.iterator(); iterator.hasNext();) {
            ResultHolder r = iterator.next();
            firstOptimalResultsDocIDs.add(r.getDocumentNumber());
        }

        LinkedList<Integer> foundInSearch = new LinkedList<Integer>();
        int position = 1;
        for (Iterator<ResultHolder> iterator = search.iterator(); iterator.hasNext();) {
            ResultHolder r = iterator.next();
            if (firstOptimalResultsDocIDs.contains(r.getDocumentNumber())) {
                foundInSearch.add(position);
            }
            position++;
        }
        position = 1;
        StringBuilder sb1 = new StringBuilder(256);
        StringBuilder sb2 = new StringBuilder(256);
        sb1.append("precision 1 ");
        sb2.append("recall 0 ");
        for (Iterator<Integer> iterator = foundInSearch.iterator(); iterator.hasNext();) {
            Integer integer = iterator.next();
            float recall = (1f / (float) numLevels) * ((float) position);
            float precision = ((float) position) / ((float) integer);
            sb1.append(precision);
            sb1.append(" ");
            sb2.append(recall);
            sb2.append(" ");
            position++;
        }
//        System.out.println(sb2.toString().replace('.', ','));
        System.out.println(sb1.toString().replace('.', ','));
        return "";
    }
    public static String printPrecisionRecallPlotFileBased(LinkedList<ResultHolder> mcs, LinkedList<ResultHolder> search) {
        int numLevels = 10;
        List<ResultHolder> optimal = mcs.subList(0, numLevels);
        HashSet<String> firstOptimalResultsDocIDs = new HashSet<String>(numLevels);
        for (Iterator<ResultHolder> iterator = optimal.iterator(); iterator.hasNext();) {
            ResultHolder r = iterator.next();
            firstOptimalResultsDocIDs.add(r.getFile());
        }

        LinkedList<Integer> foundInSearch = new LinkedList<Integer>();
        int position = 1;
        for (Iterator<ResultHolder> iterator = search.iterator(); iterator.hasNext();) {
            ResultHolder r = iterator.next();
            if (firstOptimalResultsDocIDs.contains(r.getFile())) {
                foundInSearch.add(position);
            }
            position++;
        }
        position = 1;
        StringBuilder sb1 = new StringBuilder(256);
        StringBuilder sb2 = new StringBuilder(256);
        sb1.append("precision 1 ");
        sb2.append("recall 0 ");
        for (Iterator<Integer> iterator = foundInSearch.iterator(); iterator.hasNext();) {
            Integer integer = iterator.next();
            float recall = (1f / (float) numLevels) * ((float) position);
            float precision = ((float) position) / ((float) integer);
            sb1.append(precision);
            sb1.append(" ");
            sb2.append(recall);
            sb2.append(" ");
            position++;
        }
//        System.out.println(sb2.toString().replace('.', ','));
        System.out.println(sb1.toString().replace('.', ','));
        return "";
    }

}

class ResultHolder implements Comparable {
    private float similarity;
    private int documentNumber;
    private String graph;
    private String file;

    public ResultHolder(int documentNumber, String graph, float similarity) {
        this.documentNumber = documentNumber;
        this.graph = graph;
        this.similarity = similarity;
    }

    public ResultHolder(float similarity, String file) {
//        this.documentNumber = documentNumber;
        this.file = file;
        this.similarity = similarity;
    }

    public String getFile() {
        return file;
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(int documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public int compareTo(Object o) {
        if (o instanceof ResultHolder) {
            ResultHolder r = (ResultHolder) o;
            return (int) Math.signum(r.similarity - similarity);
        }
        return 0;
    }
}
