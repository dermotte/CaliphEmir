package at.lux.retrieval;

import junit.framework.TestCase;
import at.lux.fotoretrieval.lucene.Graph;
import at.lux.fotoretrieval.lucene.GraphPathExtractor;
import at.lux.fotoretrieval.lucene.Path;
import at.lux.retrieval.clustering.PathSuffixTree;
import at.lux.retrieval.clustering.suffixtree.StcDocument;
import at.lux.retrieval.clustering.suffixtree.FinalCluster;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Set;
import java.util.Iterator;

/**
 * <p/>
 * Date: 18.10.2005 <br>
 * Time: 09:53:41 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class StcTest extends TestCase {
    final static String GRAPH1 = "[9] [15] [26] [30] [31] [32] [locationOf 30 9] [locationOf 9 32] [locationOf 9 31] [locationOf 32 30] [locationOf 9 15] [locationOf 9 26] [timeOf 31 30]";

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

    public void testStcInsert() {
        Graph g = new Graph(GRAPH1);
        System.out.println("Creating tree.");
        PathSuffixTree t = new PathSuffixTree(PathSuffixTree.Type.NoRelations);
        System.out.println("Inserting document.");
        t.insert(createSTCDocument(g), new StcDocument(GRAPH1));
    }

    private String createSTCDocument(Graph g) {
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

    public void testStcClustering() {
        try {
            IndexReader reader = IndexReader.open("testdata/idx_paths");
            PathSuffixTree t = new PathSuffixTree(PathSuffixTree.Type.UndirectedRelation);
            for (int i = 0; i < reader.numDocs(); i++) {
                Graph g = new Graph(reader.document(i).get("graph"));
                t.insert(createSTCDocument(g), new StcDocument(g.toString()));
            }
            Set<FinalCluster> clusters = t.getFinalClusters();
            for (Iterator<FinalCluster> iterator = clusters.iterator(); iterator.hasNext();) {
                FinalCluster finalCluster = iterator.next();
                System.out.println(finalCluster.getScore() + ": " + finalCluster.getDominantPhrase() +
                        " (" + finalCluster.getPhrases() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }

    }


}
