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

import at.lux.components.StatusBar;
import at.lux.fotoretrieval.FileOperations;
import at.lux.fotoretrieval.ResultListEntry;
import at.lux.fotoretrieval.RetrievalToolkit;
import at.lux.fotoretrieval.lucene.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Date: 25.03.2005
 * Time: 23:58:46
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LucenePathIndexRetrievalEngine extends AbstractRetrievalEngine {
    private static Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    private int maxResults = 20;
    private IndexSearcher indexSearcher;

    public LucenePathIndexRetrievalEngine(int maxResults) {
        this.maxResults = maxResults;
    }

    public List<ResultListEntry> getImagesBySemantics(String xPath, Vector objects, String whereToSearch, boolean recursive, JProgressBar progress) {
        if (progress != null) {
            progress.setString("Query expansion running");
        }
        List<Graph> graphList = getExpandedGraphs(xPath, whereToSearch);
        LinkedList<ResultListEntry> results = new LinkedList<ResultListEntry>();
        if (progress != null) {
            progress.setMinimum(0);
            progress.setMaximum(graphList.size());
            progress.setValue(0);
            progress.setString("Querying expanded graphs");
        }
        int countGraph = 0;
        try {
            indexSearcher = new IndexSearcher(parsePathIndexDirectory(whereToSearch));
            for (Graph graph : graphList) {
                results.addAll(searchForGraph(graph, whereToSearch));
                countGraph++;
                if (progress != null) progress.setValue(countGraph);
            }
            indexSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(results);
        LinkedList<ResultListEntry> sorted = new LinkedList<ResultListEntry>();
        HashSet<String> doublettes = new HashSet<String>();
        for (ResultListEntry entry : results) {
            String descriptionPath = entry.getDescriptionPath();
            if (!doublettes.contains(descriptionPath)) {
                doublettes.add(descriptionPath);
                sorted.add(entry);
            }
        }
        return sorted.subList(0, Math.min(sorted.size(), maxResults));
    }

    public List<ResultListEntry> searchForGraph(Graph graph, String whereToSearch) {
        LinkedList<ResultListEntry> results = new LinkedList<ResultListEntry>();
        String query = createLucenePathQuery(graph);
        try {
            QueryParser qParse = new QueryParser("paths", new WhitespaceAnalyzer());
            Query q = qParse.parse(query);
            Hits hits = indexSearcher.search(q);
            SAXBuilder builder = new SAXBuilder();
            int maxResults = Math.min(hits.length(), this.maxResults);
            for (int i = 0; i< maxResults; i++) {
                String[] filenames = hits.doc(i).getValues("file");
                for (String fileName : filenames) {
                    Element e = builder.build(new FileInputStream(fileName)).getRootElement();
                    ResultListEntry entry = new ResultListEntry((double) hits.score(i), e, fileName);
                    results.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return results;
    }

    private List<Graph> getExpandedGraphs(String query, String whereToSearch) {
        List<String> nodeQueries = new LinkedList<String>();
        StringTokenizer st = new StringTokenizer(query, "]");
        String relationString = "";
        List<Relation> relations = new LinkedList<Relation>();
        while (st.hasMoreTokens()) {
            String s = st.nextToken().trim();
            if (s.startsWith("[")) {
                s = s.substring(1);
                nodeQueries.add(s);
            } else {
                relationString = s;
            }
        }
        if (relationString.length() > 1) {
            // there are relations, go ahead and parse them:
            StringTokenizer sr = new StringTokenizer(relationString);
            Relation currentRelation = null;
            while (sr.hasMoreTokens()) {
                String s = sr.nextToken();
                try {
                    int i = Integer.parseInt(s);
                    if (currentRelation.getSource() < 0) {
                        currentRelation.setSource(i);
                    } else if (currentRelation.getTarget() < 0) {
                        currentRelation.setTarget(i);
                        currentRelation.eliminateInverse();
                        relations.add(currentRelation);
                        currentRelation = null;
                    }
                } catch (NumberFormatException e) {
                    // its the type :)
                    currentRelation = new Relation(-1, -1, s.trim());
                }
            }
        }

        // so for now do the retrieval for the nodes:
        int numOfNodes = nodeQueries.size();
        List<List<Node>> nodeResults = new LinkedList<List<Node>>();

        for (int i = 0; i < numOfNodes; i++) {
            String queryString = nodeQueries.get(i);
            List<Node> nodes;
            if (!queryString.equals("*")) {
                nodes = getNodes(queryString, whereToSearch);
            } else {
                nodes = new LinkedList<Node>();
                nodes.add(new Node(-1, 1f, "*"));
            }
            nodeResults.add(nodes);
        }

        // now we can expand our query on retrieved nodes:
        List<Graph> graphList = getExpandedGraphsFromResults(nodeResults, relations, 5);
        return graphList;
    }

    private List<Graph> getExpandedGraphsFromResults(List<List<Node>> nodeResults, List<Relation> relations, int depth) {
        List<List<Node>> expanded = getExpandedSets(nodeResults, depth);
        List<Graph> results = new LinkedList<Graph>();
        for (List<Node> nodes : expanded) {
            Graph g = getGraphFromResults(nodes, relations);
            results.add(g);
        }
        return results;
    }

    private Graph getGraphFromResults(List<Node> nodeResults, List<Relation> relations) {
        HashMap<Integer, Integer> idReplacementTable = new HashMap<Integer, Integer>(nodeResults.size());
        List<Node> nodes = new LinkedList<Node>();
        List<Relation> myRelations = new LinkedList<Relation>();
        for (int i = 0; i < nodeResults.size(); i++) {
            Node node = nodeResults.get(i);
            idReplacementTable.put(i + 1, node.getNodeID());
            nodes.add(node);
        }
        // Create the relations with the real IDs:
        for (Relation r : relations) {
            int src = (idReplacementTable.get(r.getSource()));
            int tgt = (idReplacementTable.get(r.getTarget()));
            myRelations.add(new Relation(src, tgt, r.getType()));
        }
        // now we can create the graph we want to search for:
        return new Graph(nodes, myRelations);
    }

    private List<List<Node>> getExpandedSets(List<List<Node>> nodeResults, int depth) {
        if (nodeResults.size() > 1) {
            List<Node> firstNodesResults = nodeResults.get(0);
            int numLevels = 0;
            for (Node node : firstNodesResults) {
                if (node.getWeight() < 1f) break;
                numLevels++;
            }
            numLevels += depth;
            if (firstNodesResults.size() < depth) {
                numLevels = firstNodesResults.size();
            }
            List<List<Node>> tmpNodeResults = new LinkedList<List<Node>>(nodeResults);
            tmpNodeResults.remove(0);
            List<List<Node>> results = getExpandedSets(tmpNodeResults, depth);
            List<List<Node>> endResult = new LinkedList<List<Node>>();
            for (int i = 0; i < numLevels && i < firstNodesResults.size(); i++) {
                for (List<Node> result : results) {
                    List<Node> nodeList = new LinkedList<Node>(result);
                    nodeList.add(0, firstNodesResults.get(i));
                    endResult.add(nodeList);
                }
            }
            return endResult;
        } else {
            List<List<Node>> endResult = new LinkedList<List<Node>>();
            List<Node> firstNodesResults = nodeResults.get(0);
            int numLevels = 0;
            for (Node node : firstNodesResults) {
                if (node.getWeight() < 1f) break;
                numLevels++;
            }
            numLevels += depth;
            for (int i = 0; i < numLevels && i < firstNodesResults.size(); i++) {
                List<Node> nodeList = new LinkedList<Node>();
                nodeList.add(firstNodesResults.get(i));
                endResult.add(nodeList);
            }
            return endResult;
        }
    }


    /**
     * Searches for all available nodes with given query String
     *
     * @param queryString   query like "Mathias Lux" or some text inside a node.
     * @param whereToSearch defines the base directory for the search
     * @return a List of Matching nodes with their associated weights
     */
    public List<Node> getNodes(String queryString, String whereToSearch) {
        LinkedList<Node> result = new LinkedList<Node>();
        try {
            IndexSearcher searcher = new IndexSearcher(parseSemanticIndexDirectory(whereToSearch));
            String[] fieldsToSearchInForNodes = new String[] {"label", "givenname", "organization", "familyname", "all"};
            MultiFieldQueryParser qParse = new MultiFieldQueryParser(fieldsToSearchInForNodes, new StandardAnalyzer());
            Query query = qParse.parse(queryString);
            Hits hits = searcher.search(query);
            int hitsCount = hits.length();
            if (hitsCount > maxResults) hitsCount = maxResults;

            for (int i = 0; i < hitsCount; i++) {
                Document d = hits.doc(i);
                StringBuilder sb = new StringBuilder(20);
                sb.append(hits.score(i));
                sb.append(": ");
                sb.append(d.get("label"));
                Node node = new Node(Integer.parseInt(d.get("id")), hits.score(i), d.get("label"));
                node.setType(d.get("type"));
                result.add(node);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("QueryString was: " + queryString);
            e.printStackTrace();
        }
        return result;
    }


    public List<ResultListEntry> getSimilarImages(Element VisualDescriptor, String whereToSearch, boolean recursive, JProgressBar progress) {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public List<ResultListEntry> getImagesByXPathSearch(String xPath, String whereToSearch, boolean recursive, JProgressBar progress) {
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public void indexFilesSemantically(String pathToIndex, StatusBar statusBar) {
        if (statusBar != null) statusBar.setStatus("Creating index from semantic annotations");

        SAXBuilder builder = new SAXBuilder();
        XMLOutputter outputter = new XMLOutputter(Format.getRawFormat().setIndent("").setLineSeparator("").setExpandEmptyElements(false));

        try {
            String[] descriptions = FileOperations.getAllDescriptions(new File(pathToIndex), true);
            if (descriptions == null) return;
            float numAllDocsPercent = (float) descriptions.length / 100f;
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            df.setMaximumFractionDigits(1);

            // Preparing objects for the index:
            HashMap<String, ElementEntry> elementMap = new HashMap<String, ElementEntry>(descriptions.length);
            HashMap<Element, LinkedList<String>> element2document = new HashMap<Element, LinkedList<String>>(descriptions.length);

            // in the first run we identify the semantic objects that we want to index and build
            // a table were we can relate them to the documents (identified by their path)
            for (int i = 0; i < descriptions.length; i++) {
                try {
                    Element e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
                    List l = RetrievalToolkit.xpathQuery(e, "//Semantic/SemanticBase", null);
                    for (Object aL : l) {
                        Element semanticElement = (Element) aL;
                        String xmlString = outputter.outputString(semanticElement).trim().replaceAll("id=\"id_[0-9]*\"", "");
                        // check if element is already there, indicator is its string representation.
                        if (!elementMap.keySet().contains(xmlString)) {
                            // its not here, put it in.
                            elementMap.put(xmlString, new ElementEntry(semanticElement, elementMap.size()));
//                            System.out.println(xmlString);
                        }
                        // now get the unified element
                        semanticElement = elementMap.get(xmlString).semanticElement;
                        // and check if there is an entry in the table for where to find the element
                        if (!element2document.keySet().contains(semanticElement)) {
                            element2document.put(semanticElement, new LinkedList<String>());
                        }
                        // and add found document if not already there:
                        List documentList = element2document.get(semanticElement);
                        if (!documentList.contains(descriptions[i])) documentList.add(descriptions[i]);
                    }
                    if (statusBar != null) statusBar.setStatus("Parsing documents for nodes: " + df.format((float) i / numAllDocsPercent));
                } catch (JDOMException e1) {
                    System.err.println("Exception in document #" + i + ": " + e1.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            // read stats:
            // System.out.println("Got " + countOverallElements + " Elements in " + descriptions.length + " descriptions, " + elementMap.size() + " elements are pairwise different.");

            // Now we can add the nodes to a lucene index:
            // fields: label, id, type, files (separated by '|'), xml, all
            // -------------------------------------------

            // opening the index for writing:
            boolean createFlag = true;
            String indexDir = parseSemanticIndexDirectory(pathToIndex);
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriter writer = new IndexWriter(indexDir, analyzer, createFlag);

            if (statusBar != null) statusBar.setStatus("Creating index for " + element2document.size() + " different available nodes");

            // iterating through nodes and storing them:
            for (Element semElement : element2document.keySet()) {
                // needed for later XPath :( otherwise everthing in the whole document is retrieved.

                String fileList = getFileListFromNode(element2document.get(semElement));
                Document idxDocument = new Document();
                // adding the file itself ...
                idxDocument.add(new Field("files", fileList, Field.Store.YES, Field.Index.NO));

//                System.out.println(((Element) o).getTextTrim());

//                StringBuilder all = new StringBuilder(255);
//                 adding the label
//                addToDocument(idxDocument, semElement, "//Label/Name", "label", all);
                String elementLabel = semElement.getChild("Label", semElement.getNamespace()).getChildTextTrim("Name", semElement.getNamespace());
                Field labelField = new Field("label", elementLabel, Field.Store.YES, Field.Index.TOKENIZED);
                labelField.setBoost(1.2f);
                idxDocument.add(labelField);

                // adding the type:
                String elementType = semElement.getAttribute("type", xsi).getValue().trim();
                idxDocument.add(new Field("type", elementType, Field.Store.YES, Field.Index.NO));
                // adding the XML contents:
                String xmlString = outputter.outputString(semElement);
                idxDocument.add(new Field("xml", xmlString, Field.Store.YES, Field.Index.NO));
                // adding the id:
                idxDocument.add(new Field("id", elementMap.get(xmlString.trim().replaceAll("id=\"id_[0-9]*\"", "")).id + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
                // TODO: split the indexing for objects based on type:
                // adding all, unstored for retrieval only
                if (elementType.equals("AgentObjectType")) {
                    createIndexDocumentFromSemanticAgent(semElement, idxDocument);
                } else if (elementType.equals("EventType")) {
                    createIndexDocumentFromSemanticElement(semElement, idxDocument);
                } else if (elementType.equals("SemanticPlaceType")) {
                    createIndexDocumentFromSemanticElement(semElement, idxDocument);
                } else if (elementType.equals("SemanticTimeType")) {
                    createIndexDocumentFromSemanticElement(semElement, idxDocument);
                } else {
                    createIndexDocumentFromSemanticElement(semElement, idxDocument);
                }

                writer.addDocument(idxDocument);

            }
            // now optimize and close the index:
            // todo: open index for appending and/or updating
            writer.optimize();
            writer.close();

            // Now we can create the powerset for each existing graph
            // (based on sorted node ids) and store
            // all resulting graphs within an index.
            // ----------------------------------------------------------
            if (statusBar != null) statusBar.setStatus("Creating and merging of available graphs");
            HashMap<Graph, HashSet<String>> graph2document = new HashMap<Graph, HashSet<String>>(descriptions.length);
            for (int i = 0; i < descriptions.length; i++)
                try {
                    Element e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
                    List l = RetrievalToolkit.xpathQuery(e, "//Semantic/SemanticBase", null);
                    HashMap<String, Integer> docID2overallID = new HashMap<String, Integer>(l.size());
                    LinkedList<Relation> relations = new LinkedList<Relation>();
                    LinkedList<Integer> nodes = new LinkedList<Integer>();
                    for (Object aL : l) {
                        Element semanticElement = (Element) aL;
                        String xmlString = outputter.outputString(semanticElement);
                        int id = elementMap.get(xmlString.trim().replaceAll("id=\"id_[0-9]*\"", "")).id;
                        String docID = semanticElement.getAttribute("id").getValue();
                        docID2overallID.put(docID, id);
                        nodes.add(id);
                    }
                    // get all relations with global ids and eliminate inverse relations
                    l = RetrievalToolkit.xpathQuery(e, "//Graph/Relation", null);
                    for (Object aL1 : l) {
                        Element relation = (Element) aL1;
                        int source = docID2overallID.get(relation.getAttribute("source").getValue().substring(1));
                        int target = docID2overallID.get(relation.getAttribute("target").getValue().substring(1));
                        String type = relation.getAttribute("type").getValue();
                        type = type.substring(type.lastIndexOf(':') + 1);
                        Relation r = eliminateInverse(new Relation(source, target, type));
                        relations.add(r);
                    }

                    // now create a graph object
                    Collections.sort(nodes);
                    Collections.sort(relations);
                    LinkedList<Node> nodeList = new LinkedList<Node>();
                    for (Integer node : nodes) {
                        nodeList.add(new Node(node));
                    }
                    Graph g = new Graph(nodeList, relations);
                    HashSet<String> docs = new HashSet<String>(1);
                    docs.add(descriptions[i]);
                    graph2document.put(g, docs);

                } catch (JDOMException e1) {
                    System.err.println(new StringBuilder().append("Exception in document #").append(i).append(": ").append(e1.getMessage()).toString());
                }

            HashMap<String, Graph> str2graph = new HashMap<String, Graph>(graph2document.size() / 2);
            HashMap<Graph, HashSet<String>> g2d = new HashMap<Graph, HashSet<String>>(descriptions.length);

            /*
                For now we reduce the number of graphs by identifiying and merging duplicates and
                remove redundant entries:
            */
            for (Graph g : graph2document.keySet()) {
                if (str2graph.containsKey(g.toString())) {
                    g2d.get(str2graph.get(g.toString())).addAll(graph2document.get(g));
                } else {
                    str2graph.put(g.toString(), g);
                    g2d.put(g, graph2document.get(g));
                }
            }
            graph2document = g2d;
            System.out.println(graph2document.size() + " non trivial different graphs were found");
            // now put all the available graphs into an index:
            // -----------------------------------------------

            // for now we will store a simple text file:
            if (statusBar != null) statusBar.setStatus("Saving index of paths");

            boolean createPathIndexFlag = true;
            String pathIndexDir = parsePathIndexDirectory(pathToIndex);
            IndexWriter pathIndexWriter = new IndexWriter(pathIndexDir, new GraphAnalyzer(), createPathIndexFlag);

            for (Graph graph : graph2document.keySet()) {
                HashSet<String> files = graph2document.get(graph);
                Document idxDocument = new Document();
                // adding the file itself ...
                for (String s : files) {
                    idxDocument.add(new Field("file", s, Field.Store.YES, Field.Index.NO));
                }
                // adding the graph ...
                idxDocument.add(new Field("graph", graph.toString(), Field.Store.YES, Field.Index.TOKENIZED));
//                idxDocument.add(Field.UnIndexed("graph", graph.toString()));
                // adding the paths
                StringBuilder sb = new StringBuilder(256);
                sb.append(graph.toString());
                List<Path> pathList = (new LabeledGraph(graph)).get2Paths();
                if (!pathList.isEmpty()) sb.append(' ');
                for (Iterator<Path> iterator1 = pathList.iterator(); iterator1.hasNext();) {
                    Path path = iterator1.next();
                    sb.append(path.toString());
                    if (iterator1.hasNext()) sb.append(' ');
                }
                idxDocument.add(new Field("paths", sb.toString(), Field.Store.YES, Field.Index.TOKENIZED));
                pathIndexWriter.addDocument(idxDocument);
            }
            // now optimize and close the index:
            pathIndexWriter.optimize();
            pathIndexWriter.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createIndexDocumentFromSemanticElement(Element semElement, Document idxDocument) {
        StringBuilder all = new StringBuilder(64);
        List l = RetrievalToolkit.xpathQuery(semElement, "*//*", null);
        for (Object aL : l) {
            Element e = (Element) aL;
            all.append(e.getTextTrim());
            all.append(' ');
        }
        Field field = new Field("all", all.toString(), Field.Store.NO, Field.Index.TOKENIZED);
        field.setBoost(0.8f);
        idxDocument.add(field);
    }

    private static void createIndexDocumentFromSemanticAgent(Element e, Document idxDocument) {
        Namespace mpeg7 = e.getNamespace();
        StringBuilder all = new StringBuilder(64);
        Element name = (Element) e.getChild("Agent", mpeg7).getChild("Name", mpeg7);
        idxDocument.add(new Field("givenname", name.getChild("GivenName", mpeg7).getTextTrim(), Field.Store.YES, Field.Index.TOKENIZED));
        idxDocument.add(new Field("familyname", name.getChild("FamilyName", mpeg7).getTextTrim(), Field.Store.YES, Field.Index.TOKENIZED));
        Element affiliation = e.getChild("Agent", mpeg7).getChild("Affiliation", mpeg7);
        if (affiliation != null) {
            String organization = affiliation.getChild("Organization", mpeg7).getChildText("Name", mpeg7);
            if (organization != null && organization.length() > 0) {
                Field field = new Field("organization", organization, Field.Store.YES, Field.Index.TOKENIZED);
                field.setBoost(0.5f);
                idxDocument.add(field);
            }
        }
    }

    /**
     * Creates and returns index directory for node index ...
     *
     * @param pathToIndex
     * @return the directory to the semantic index
     */
    public static String parseSemanticIndexDirectory(String pathToIndex) {
        String indexDir = pathToIndex;
        if (!indexDir.endsWith(System.getProperty("file.separator"))) indexDir += System.getProperty("file.separator");
        indexDir += "idx_semantic";
        File indexDirFile = new File(indexDir);
        if (!indexDirFile.exists()) indexDirFile.mkdir();
        return indexDir;
    }

    /**
     * Creates and returns the index directory for path index ...
     *
     * @param pathToIndex
     * @return the directory to the index
     */
    public static String parsePathIndexDirectory(String pathToIndex) {
        String indexDir = pathToIndex;
        if (!indexDir.endsWith(System.getProperty("file.separator"))) indexDir += System.getProperty("file.separator");
        indexDir += "idx_paths";
        File indexDirFile = new File(indexDir);
        if (!indexDirFile.exists()) indexDirFile.mkdir();
        return indexDir;
    }

    /**
     * Creates and returns the index directory for 2-path index ...
     *
     * @param pathToIndex
     * @return
     * @deprecated
     */
    public static String parse2PathIndexDirectory(String pathToIndex) {
        String indexDir = pathToIndex;
        if (!indexDir.endsWith(System.getProperty("file.separator"))) indexDir += System.getProperty("file.separator");
        indexDir += "idx_2paths";
        File indexDirFile = new File(indexDir);
        if (!indexDirFile.exists()) indexDirFile.mkdir();
        return indexDir;
    }

    private static String getFileListFromNode(Collection<String> list) {
        StringBuilder files = new StringBuilder(64);
        for (Iterator<String> it2 = list.iterator(); it2.hasNext();) {
            files.append(it2.next());
            if (it2.hasNext()) {
                files.append('|');
            }
        }
        return files.toString();
    }



    /**
     * Eliminates all inverse relations to simplify retrieval
     *
     * @param relation
     * @return the normalized relation
     */
    public static Relation eliminateInverse(Relation relation) {
        Relation result = relation;
        if (Relation.relationMapping.containsKey(relation.getType())) {
            result = new Relation(relation.getTarget(), relation.getSource(), Relation.relationMapping.get(relation.getType()));
        }
        return result;
    }

    /**
     * Creates a query String from a graph ...
     * @param g
     * @return a path query for lucene
     */
    public static String createLucenePathQuery(Graph g) {
        StringBuilder sb = new StringBuilder(256);
        List<Node> nodes = g.getNodes();
        Collections.sort(nodes);
        List<Relation> relations = g.getRelations();
        Collections.sort(relations);
        for (Node n : nodes) {
            // if no anonymous node:
            if (n.getNodeID() > -1) {
                sb.append('_');
                sb.append(n.getNodeID());
                sb.append(' ');
            }
        }
        for (Relation relation : relations) {
            sb.append(' ');
            sb.append(createLucenePathQuery(relation));
            sb.append(' ');
        }

        // 2-path generation:
        LabeledGraph lg = new LabeledGraph(g);
        sb.append(create2PathQuery(lg));
        return sb.toString().trim();
    }

    public static String create2PathQuery(LabeledGraph lg) {
        StringBuilder sw = new StringBuilder(64);
        List<Path> paths = lg.get2Paths();
        for (Path p : paths) {
            sw.append(createPathQueryString(p));
            sw.append(' ');
        }
        return sw.toString();
    }

    public static String createPathQueryString(Path p) {
        StringBuilder sw = new StringBuilder(128);
        LinkedList<String> nodes = p.getNodes();
        LinkedList<String> relations = p.getRelations();

        if (nodes.getFirst().equals('*') || nodes.getLast().equals('*')) {
            // we have to implement both directions.
            sw.append('(');
            sw.append('_');
            for (int i = 0; i < nodes.size(); i++) {
                sw.append(deAnonymize(nodes.get(i)));
                if (i < nodes.size() - 1) {
                    sw.append('_');
                    sw.append(relations.get(i));
                    sw.append('_');
                }
            }
            sw.append(" OR _");
            for (int i = nodes.size() - 1; i >= 0; i--) {
                sw.append(deAnonymize(nodes.get(i)));
                if (i > 0) {
                    sw.append('_');
                    sw.append(Relation.invertRelationType(relations.get(i - 1)));
                    sw.append('_');
                }
            }
            sw.append(')');
        } else {
            // we have to implement only one direction :)
            sw.append('_');
            // if the first is smaller then do not change the order
            if (nodes.getFirst().compareTo(nodes.getLast()) < 0) {
                for (int i = 0; i < nodes.size(); i++) {
                    sw.append(deAnonymize(nodes.get(i)));
                    if (i < nodes.size() - 1) {
                        sw.append('_');
                        sw.append(relations.get(i));
                        sw.append('_');
                    }
                }
            } else { // switch order of nodes ..
                for (int i = nodes.size() - 1; i >= 0; i--) {
                    sw.append(deAnonymize(nodes.get(i)));
                    if (i > 0) {
                        sw.append('_');
                        sw.append(Relation.invertRelationType(relations.get(i - 1)));
                        sw.append('_');
                    }
                }
            }
        }
        return sw.toString();
    }

    public static String createLucenePathQuery(Relation r) {
        StringBuilder sb = new StringBuilder(64);
        if (r.getType().equals("*")) {
            String source = deAnonymize(r.getSource());
            String target = deAnonymize(r.getTarget());
            sb.append('(');
            sb.append("_*_");
            sb.append(source);
            sb.append('_');
            sb.append(target);
            sb.append(" OR ");
            sb.append("_*_");
            sb.append(target);
            sb.append('_');
            sb.append(source);
            sb.append(')');
        } else {
            // check if not inverse :)
            r.eliminateInverse();
            sb.append('_');
            sb.append(r.getType());
            sb.append('_');
            sb.append(deAnonymize(r.getSource()));
            sb.append('_');
            sb.append(deAnonymize(r.getTarget()));
        }
        return sb.toString().trim();
    }

    private static String deAnonymize(int nodeID) {
        if (nodeID > -1) return String.valueOf(nodeID);
        else return "*";
    }

    private static String deAnonymize(String nodeID) {
        if (!nodeID.equals("-1")) return (nodeID);
        else return "*";
    }
	public List<ResultListEntry> getSimilarImages_fromSet(Set<Element> VisualDescriptorSet, String whereToSearch, boolean recursive, JProgressBar progress) {
        throw new UnsupportedOperationException("Not Implemented!");
    }
}
