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
package at.lux.fotoretrieval;

import at.lux.components.ProgressWindow;
import at.lux.fotoretrieval.lucene.Graph;
import at.lux.fotoretrieval.panels.Visualization2DPanelWithFdp;
import at.lux.fotoretrieval.retrievalengines.LucenePathIndexRetrievalEngine;
import at.lux.retrieval.fastmap.ArrayFastmapDistanceMatrix;
import at.lux.retrieval.fastmap.DistanceCalculator;
import at.lux.retrieval.fastmap.FastMap;
import at.lux.retrieval.fastmap.FastmapDistanceMatrix;
import at.lux.retrieval.suffixtreemodel.SuffixTree;
import at.lux.retrieval.graphisomorphism.FastSubgraphIsomorphism;
import at.lux.retrieval.graphisomorphism.metrics.TermVectorNodeDistanceFunction;
import at.lux.retrieval.graphisomorphism.metrics.SimpleEdgeDistanceFunction;
import at.lux.retrieval.vectorspace.GraphVectorSimilarity;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Field;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;

/**
 * SimilarImageSearchThread
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GraphDistanceVisualizationThread extends Thread {
    String dir;
    RetrievalFrame parent;
    JProgressBar progress;
    DecimalFormat df;
    LinkedList<String> fileList;
    private boolean autoStartFDP;
    private LinkedList<Graph> graphList;
    SuffixTree st = null;
    private GraphVectorSimilarity gvs = null;

    public GraphDistanceVisualizationThread(String directory,
                                            RetrievalFrame frame, JProgressBar progress) {
        this.dir = directory;
        if (!this.dir.endsWith(File.separator)) {
            this.dir += File.separator;
        }
        this.parent = frame;
        this.progress = progress;
        df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(2);
        fileList = new LinkedList<String>();
    }

    public GraphDistanceVisualizationThread(String[] fileList, RetrievalFrame parent, JProgressBar progress) {
        this.progress = progress;
        this.parent = parent;
        this.fileList = new LinkedList<String>();
        for (String file: fileList)
            this.fileList.add(file);
        df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(2);
    }

    public void run() {
        // TODO: make this based on files to support the visualization of result lists ...
        try {
            IndexReader ir = null;
            if (fileList.isEmpty()) {
                String indexDirectory = LucenePathIndexRetrievalEngine.parsePathIndexDirectory(dir);
                if (!IndexReader.indexExists(indexDirectory)) {
                    JOptionPane.showMessageDialog(parent, "Chosen repositors directory does not exist.");
                    return;
                } else {
                    ir = IndexReader.open(indexDirectory);
                    st = new SuffixTree(SuffixTree.RelationType.FullRelations);
                    // a vector space model for nodes and triples ...
                    gvs = new GraphVectorSimilarity(GraphVectorSimilarity.Type.BM25, 1);
                    for (int i = 0; i < ir.numDocs(); i++) {
                        Graph g_idx = new Graph(ir.document(i).getField("graph").stringValue());
                        Field[] files = ir.document(i).getFields("file");
                        for (Field file1 : files) {
                            st.addCorpusDocument(SuffixTree.createSuffixTreeDocument(g_idx));
                            gvs.addToCorpus(g_idx);
                        }
                    }

                }
            }

            parent.setEnabled(false);
            ProgressWindow pw;
            pw = new ProgressWindow(parent, progress);
            pw.pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            pw.setLocation((d.width - pw.getWidth()) / 2, (d.height - pw.getHeight()) / 2);
            pw.setVisible(true);
            long stime, ftime;
            stime = System.currentTimeMillis();
            parent.setStatus("Loading Graphs");

            progress.setMinimum(0);
            progress.setMaximum(3);
            progress.setValue(0);

            if (fileList.size() > 0) prepareGraphAndFileLists();
            else if (ir != null) prepareGraphAndFileLists(ir);
            else {
                JOptionPane.showMessageDialog(parent, "No data found!");
                pw.setVisible(false);
                parent.setEnabled(true);
            }
            progress.setValue(1);

            try {
                // create matrixFastmapFastmap with mcs distance for graphs.
                FastmapDistanceMatrix matrixFastmap = createDistanceMatrix(graphList);

                FastMap fm = new FastMap(matrixFastmap, 2);
                progress.setValue(2);
                long ms = System.currentTimeMillis();
                fm.run();
                ms = System.currentTimeMillis() - ms;
                progress.setValue(3);
                System.out.println("Time for " + matrixFastmap.getDimension() + " images: " + ms + " ms");

//        Vector results = engine.getSimilarImages(visualDescriptor, dir, recursive, progress);
                stime = System.currentTimeMillis() - stime;
                ftime = System.currentTimeMillis();
                parent.setStatus("Formatting results ...");
                Visualization2DPanelWithFdp panel = new Visualization2DPanelWithFdp(fm.getPoints(), matrixFastmap, fileList, autoStartFDP);
//        ResultsPanel rp = new ResultsPanel(results, progress);
                ftime = System.currentTimeMillis() - ftime;
                parent.addVisualization(panel);
                parent.setStatus("Searched for " + df.format(stime / 1000.0) + " s, formatting lasted " + df.format(ftime / 1000.0) + " s");
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                pw.setVisible(false);
                parent.setEnabled(true);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void prepareGraphAndFileLists() {
        // todo: something to load graphs from a file list ...
    }

    private void prepareGraphAndFileLists(IndexReader ir) throws IOException {
        graphList = new LinkedList<Graph>();
        for (int i = 0; i < ir.numDocs(); i++) {
            Graph g = new Graph(ir.document(i).getValues("graph")[0]);
            String graphFile = ir.document(i).getValues("file")[0];
            graphList.add(g);
            fileList.add(graphFile);
        }
    }

    private FastmapDistanceMatrix createDistanceMatrix(LinkedList<Graph> graphList) throws JDOMException, IOException {
        FastmapDistanceMatrix matrixFastmap = null;
        if ((EmirConfiguration.getInstance().getInt("Metric.FDP") == 6)) {
            // berretti:
            LinkedList<Document> docs = new LinkedList<Document>();
            SAXBuilder builder = new SAXBuilder();
            for (String file : fileList) {
                docs.add(builder.build(new File(file)));
            }
            matrixFastmap = new ArrayFastmapDistanceMatrix(docs, new DistanceCalculator() {
                public float getDistance(Object o1, Object o2) {
                    Document d1 = (Document) o1;
                    Document d2 = (Document) o2;
                    FastSubgraphIsomorphism i = new FastSubgraphIsomorphism(new TermVectorNodeDistanceFunction(TermVectorNodeDistanceFunction.Type.CosinusCoefficient), new SimpleEdgeDistanceFunction(SimpleEdgeDistanceFunction.EdgeInversionType.Allow));
                    return i.getDistance(d1, d2);
                }
            });
        } else if ((EmirConfiguration.getInstance().getInt("Metric.FDP") == 7)) {
            // Vector Space
            matrixFastmap = new ArrayFastmapDistanceMatrix(graphList, new VectorSpaceDistanceCalculator(gvs));
            autoStartFDP = false;
        } else if ((EmirConfiguration.getInstance().getInt("Metric.FDP") == 5)) {
            // Suffix Tree Model
            matrixFastmap = new ArrayFastmapDistanceMatrix(graphList, new SuffixTreeDistanceCalculator(st));
            autoStartFDP = false;
        } else {
            // bunke's mcs:
            matrixFastmap = new ArrayFastmapDistanceMatrix(graphList, new DistanceCalculator() {
                public float getDistance(Object o1, Object o2) {
                    Graph g1 = (Graph) o1;
                    Graph g2 = (Graph) o2;
                    return g1.getMcsDistance(g2);
                }
            });
            autoStartFDP = true;
        }
        return matrixFastmap;
    }

    class SuffixTreeDistanceCalculator extends DistanceCalculator {
        SuffixTree st;

        public SuffixTreeDistanceCalculator(SuffixTree st) {
            this.st = st;
        }

        public float getDistance(Object o1, Object o2) {
            Graph g1 = (Graph) o1;
            Graph g2 = (Graph) o2;

            st.addDocument(SuffixTree.createSuffixTreeDocument(g1));
            st.addDocument(SuffixTree.createSuffixTreeDocument(g2));

            //                                double similarity = st.getSimilarity(SuffixTree.SimilartyType.Unweighted);
            double similarity = st.getSimilarity(SuffixTree.SimilarityType.IDF);
            st.resetSimilarity();
            return (float) (1 - similarity);
        }

    }

    class VectorSpaceDistanceCalculator extends DistanceCalculator {
        private GraphVectorSimilarity gvs;

        public VectorSpaceDistanceCalculator(GraphVectorSimilarity gvs) {
            this.gvs = gvs;
        }

        public float getDistance(Object o1, Object o2) {
            Graph g1 = (Graph) o1;
            Graph g2 = (Graph) o2;
            double similarity = gvs.getSimilarity(g1, g2);
            return (float) (1 - similarity);
        }

    }
}