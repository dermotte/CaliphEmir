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
import at.lux.fotoretrieval.lucene.Graph;
import at.lux.fotoretrieval.lucene.Node;
import at.lux.fotoretrieval.lucene.Relation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Date: 13.10.2004
 * Time: 21:47:58
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LuceneRetrievalEngine extends AbstractRetrievalEngine {
    private int maxResults = 40;
    private static Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final HashMap<String, String> relationMapping;

    public LuceneRetrievalEngine(int maxResults) {
        this.maxResults = maxResults;
    }

    static {
        relationMapping = new HashMap<String, String>(27);
        relationMapping.put("key", "keyFor");
        relationMapping.put("annotates", "annotatedBy");
        relationMapping.put("shows", "appearsIn");
        relationMapping.put("references", "referencedBy");
        relationMapping.put("quality", "qualityOf");
        relationMapping.put("symbolizes", "symbolizedBy");
        relationMapping.put("location", "locationOf");
        relationMapping.put("source", "sourceOf");
        relationMapping.put("destination", "destinationOf");
        relationMapping.put("path", "pathOf");
        relationMapping.put("time", "timeOf");
        relationMapping.put("depicts", "depictedBy");
        relationMapping.put("represents", "representedBy");
        relationMapping.put("context", "contextFor");
        relationMapping.put("interprets", "interpretedBy");
        relationMapping.put("agent", "agentOf");
        relationMapping.put("patient", "patientOf");
        relationMapping.put("experiencer", "experiencerOf");
        relationMapping.put("stimulus", "stimulusOf");
        relationMapping.put("causer", "causerOf");
        relationMapping.put("goal", "goalOf");
        relationMapping.put("beneficiary", "beneficiaryOf");
        relationMapping.put("theme", "themeOf");
        relationMapping.put("result", "resultOf");
        relationMapping.put("instrument", "instrumentOf");
        relationMapping.put("accompanier", "accompanierOf");
        relationMapping.put("summarizes", "summarizedBt");
        relationMapping.put("specializes", "generalizes");
        relationMapping.put("exemplifies", "exemplifiedBy");
        relationMapping.put("part", "partOf");
        relationMapping.put("property", "propertyOf");
        relationMapping.put("user", "userOf");
        relationMapping.put("component", "componentOf");
        relationMapping.put("substance", "substanceOf");
        relationMapping.put("entails", "entailedBy");
        relationMapping.put("manner", "mannerOf");
        relationMapping.put("state", "stateOf");
        relationMapping.put("influences", "dependsOn");
    }

    /**
     * In this case we can search for images with a String query deinfing
     * a graph, where the nodes are build by search queries in square brackets
     * and are referenced in relations by their postion starting with number 1.
     * relations follow the nodes starting with the type followed by the
     * position of the source node in the List and the target node:
     * <code>
     * query := node+ relation+
     * node := [term+]
     * relation := type source target
     * term := String
     * type := String
     * source := Integer
     * target := Integer
     * </code>
     * e.g."[Mathias Lux] [Talking] agentOf 1 2"
     *
     * @param xPath
     * @param objects       can be set to <code>null</code>
     * @param whereToSearch
     * @param recursive
     * @param progress
     */
    public List<ResultListEntry> getImagesBySemantics(String xPath, Vector objects, String whereToSearch, boolean recursive, JProgressBar progress) {
        List<String> nodeQueries = new LinkedList<String>();
        StringTokenizer st = new StringTokenizer(xPath, "]");
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
            List<Node> nodes = getNodes(queryString, whereToSearch);
            nodeResults.add(nodes);
        }

        // now we can expand our query on retrieved nodes:
        List<Graph> graphList = getExpandedGraphsFromResults(nodeResults, relations, 3);
        LinkedList<ResultListEntry> results = new LinkedList<ResultListEntry>();
        if (progress != null) {
            progress.setMinimum(0);
            progress.setMaximum(graphList.size());
            progress.setValue(0);
            progress.setString("Querying expanded graphs");
        }
        int countGraph = 0;
        for (Iterator<Graph> iterator = graphList.iterator(); iterator.hasNext();) {
            Graph graph = iterator.next();
            results.addAll(searchForGraph(graph, whereToSearch));
            countGraph++;
            if (progress != null) progress.setValue(countGraph);
        }

        // for now eliminate the doublettes
        if (progress != null) {
            progress.setMinimum(0);
            progress.setMaximum(results.size());
            progress.setValue(0);
            progress.setString("Removing double entries");
        }
        countGraph = 0;
        HashMap<String, ResultListEntry> gegencheck = new HashMap<String, ResultListEntry>();
        for (Iterator<ResultListEntry> iterator = results.iterator(); iterator.hasNext();) {
            ResultListEntry resultListEntry = iterator.next();
            String file = resultListEntry.getFilePath();
            double relevance = resultListEntry.getRelevance();
            if (gegencheck.containsKey(file)) {
                double rel = gegencheck.get(file).getRelevance();
                if (rel < relevance) {
                    gegencheck.put(file, resultListEntry);
                }
            } else {
                gegencheck.put(file, resultListEntry);
            }
            countGraph++;
            if (progress != null) progress.setValue(countGraph);
        }
        results.clear();
        results.addAll(gegencheck.values());
        Collections.sort(results);
        return results;
    }

    private List<Graph> getExpandedGraphsFromResults(List<List<Node>> nodeResults, List<Relation> relations, int depth) {
        List<List<Node>> expanded = getExpandedSets(nodeResults, depth);
//        System.out.println("Expanding to " + expanded.size() + " graphs");
        List<Graph> results = new LinkedList<Graph>();
        for (Iterator<List<Node>> iterator = expanded.iterator(); iterator.hasNext();) {
            List<Node> nodes = iterator.next();
            Graph g = getGraphFromResults(nodes, relations);
            results.add(g);
        }
        // if there are any relations without type we have to
        // create a reverse relation each, otherwise we won't
        // get all our results:
//        List<Graph> additionalResults = new LinkedList<Graph>();
//        for (Iterator<Graph> iterator = results.iterator(); iterator.hasNext();) {
//            Graph graph = iterator.next();
//            expandUntypedRelations(graph, additionalResults);
//        }
        return results;
    }

    private List<List<Node>> getExpandedSets(List<List<Node>> nodeResults, int depth) {
        if (nodeResults.size() > 1) {
            List<Node> firstNodesResults = nodeResults.get(0);
            int numLevels = 0;
            for (Iterator<Node> iterator = firstNodesResults.iterator(); iterator.hasNext();) {
                Node node = iterator.next();
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
                for (int j = 0; j < results.size(); j++) {
                    List<Node> nodeList = new LinkedList<Node>(results.get(j));
                    nodeList.add(0, firstNodesResults.get(i));
                    endResult.add(nodeList);
                }
            }
            return endResult;
        } else {
            List<List<Node>> endResult = new LinkedList<List<Node>>();
            List<Node> firstNodesResults = nodeResults.get(0);
            int numLevels = 0;
            for (Iterator<Node> iterator = firstNodesResults.iterator(); iterator.hasNext();) {
                Node node = iterator.next();
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
        for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
            Relation r = iterator.next();
            int src = (idReplacementTable.get(r.getSource()));
            int tgt = (idReplacementTable.get(r.getTarget()));
            myRelations.add(new Relation(src, tgt, r.getType()));
        }
        // now we can create the graph we want to search for:
        Graph g = new Graph(nodes, myRelations);
        return g;
    }

    private List<ResultListEntry> searchForGraph(Graph g, String whereToSearch) {
//        System.out.println("Querying for graph: " + g.toString());
//        for (Iterator<Node> iterator = g.getNodes().iterator(); iterator.hasNext();) {
//            Node node = iterator.next();
//            System.out.println(node.getLabel() + ": " + node.getNodeID() + " (" + node.getWeight() + ") ");
//        }
        // and we search for it ion the text file:
        String indexFile;

        // create regex string:
        // as there are all nodes and relations surrounded with square brackets this is easy
        // between the relations there may various literals: '.*'
        String regexInsert = ".*";
        StringBuilder graphSearch = new StringBuilder(g.toString().length() * 2);
        StringTokenizer stok = new StringTokenizer(g.toString(), "[");
        String s = "";
        graphSearch.append(regexInsert);
        LinkedList<String> graphSearchList = new LinkedList<String>();
        while (stok.hasMoreTokens()) {
            StringBuilder regexItem = new StringBuilder(32);
            s = stok.nextToken().trim();
            s = s.substring(0, s.length() - 1);

            // and there may be other nodes & relations:
            regexItem.append(regexInsert);
            // opening bracket '['
            regexItem.append("\\x5B");
            // the actual content (node or relation)
            regexItem.append(s);
            // closing bracket ']'
            regexItem.append("\\x5D");
            // and there may be other nodes & relations:
            regexItem.append(regexInsert);

            String regex = regexItem.toString();

            if (regex.indexOf("\\w*") > -1) {
                // here we create support for relation wildcards:
                regex = expandUntypedRelation(regex);
            }

            graphSearchList.add(regex);

/*
            // opening bracket '['
            graphSearch.append("\\x5B");
            // the actual content (node or relation)
            graphSearch.append(s);
            // closing bracket ']'
            graphSearch.append("\\x5D");
            // and there may be other nodes & relations:
            graphSearch.append(regexInsert);
*/
        }

        List<ResultListEntry> resultList = new LinkedList<ResultListEntry>();
        SAXBuilder builder = new SAXBuilder();

        if (!whereToSearch.endsWith(File.separator)) {
            indexFile = whereToSearch + File.separator + "idx_graphs.list";
        } else {
            indexFile = whereToSearch + "idx_graphs.list";
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(indexFile))));
            String line = null;
//            String regex = graphSearch.toString();
//            String oldRegex = regex;
//            if (regex.indexOf("\\w*") > -1) {
//                 here we create support for relation wildcards:
//                regex = expandUntypedRelation(regex);
//
//            }
//            System.out.println("REGEX: " + regex);
            while ((line = br.readLine()) != null) {
                boolean match = true;
                for (Iterator<String> iterator = graphSearchList.iterator(); iterator.hasNext();) {
                    String regex = iterator.next();
                    if (!line.matches(regex)) {
                        match = false;
                        continue;
                    }
                }
                if (match) {
                    // we found a graph:
                    System.out.println("FOUND: " + line);
                    StringTokenizer st = new StringTokenizer(line, "|");
                    String graphString = st.nextToken();
                    Graph theGraph = new Graph(graphString);
                    float similarity = g.getMcsSimilarity(theGraph);
                    while (st.hasMoreTokens()) {
                        String fileName = st.nextToken();
                        Element e = builder.build(new FileInputStream(fileName)).getRootElement();
                        ResultListEntry entry = new ResultListEntry((double) similarity, e, fileName);
                        resultList.add(entry);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    private String expandUntypedRelation(String regex) {
        String behind = regex.substring(regex.indexOf("\\w*") + 4);
        String before = regex.substring(0, regex.indexOf("\\w*") + 4);

        String firstNum = behind.substring(0, behind.indexOf(' '));
        String secondNum = behind.substring(behind.indexOf(' ') + 1, behind.indexOf('\\'));

        behind = behind.substring(behind.indexOf('\\'));

        regex = before + "((" + firstNum + " " + secondNum + ")|(" + secondNum + " " + firstNum + "))" + behind;
        return regex;
    }

    /**
     * Not implemented ... please use the method of the engine
     * {@link at.lux.fotoretrieval.retrievalengines.FileSystemRetrieval}
     * @param VisualDescriptor
     * @param whereToSearch
     * @param recursive
     * @param progress
     */
    public List<ResultListEntry> getSimilarImages(Element VisualDescriptor, String whereToSearch, boolean recursive, JProgressBar progress) {
        return null;
    }
	public List<ResultListEntry> getSimilarImages_fromSet(Set<Element> VisualDescriptorSet, String whereToSearch, boolean recursive, JProgressBar progress) {
        return null;
    }

    public List<ResultListEntry> getImagesByXPathSearch(String xPath, String whereToSearch, boolean recursive, JProgressBar progress) {
        ArrayList<ResultListEntry> results = new ArrayList<ResultListEntry>(maxResults);
        if (progress != null)
            progress.setString("Searching through index");
        SAXBuilder builder = new SAXBuilder();
        try {
            QueryParser qParse = new QueryParser("all", new StandardAnalyzer());
            IndexSearcher searcher = new IndexSearcher(parseFulltextIndexDirectory(whereToSearch));
            Query query = qParse.parse(xPath);
            Hits hits = searcher.search(query);
            int hitsCount = hits.length();
            if (hitsCount > maxResults) hitsCount = maxResults;
            if (progress != null) {
                progress.setMinimum(0);
                progress.setMaximum(hitsCount);
                progress.setValue(0);
                progress.setString("Reading results from disk");
            }

            for (int i = 0; i < hitsCount; i++) {
                Document d = hits.doc(i);
                Element e = builder.build(new FileInputStream(d.get("file"))).getRootElement();
                results.add(new ResultListEntry(hits.score(i), e, d.get("file")));
                if (progress != null) progress.setValue(i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("XPath was: " + xPath);
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return results;

    }

    /**
     * In general we take the base path for our search for the pathToIndex parameter.
     * we then add the directory "index" and create it there.
     *
     * @param pathToIndex
     * @param statusBar
     */
    public void indexFiles(String pathToIndex, StatusBar statusBar) {
        // parsing and eventually creating the directory for the index ...
        String indexDir = parseFulltextIndexDirectory(pathToIndex);

        Analyzer analyzer = new StandardAnalyzer();
        boolean createFlag = true;
        SAXBuilder builder = new SAXBuilder();
        String prefix = "Creating fulltext index: ";
        try {
            IndexWriter writer = new IndexWriter(indexDir, analyzer, createFlag);
            String[] descriptions = FileOperations.getAllDescriptions(new File(pathToIndex), true);
            if (descriptions == null) return;
            float numAllDocsPercent = (float) descriptions.length / 100f;
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            df.setMaximumFractionDigits(1);

            for (int i = 0; i < descriptions.length; i++) {
                try {
                    Element e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
                    Document idxDocument = new Document();
                    // adding the file itself ...
                    idxDocument.add(new Field("file", descriptions[i], Field.Store.YES, Field.Index.NO));
                    // adding all given names
                    StringBuilder all = new StringBuilder(255);

                    List l = RetrievalToolkit.xpathQuery(e, "//Graph/Relation", null);
//                    System.out.println("NumberOfRelations: " + l.size());

                    addToDocument(idxDocument, e, "//Agent/Name/GivenName", "GivenName", all);
                    addToDocument(idxDocument, e, "//Agent/Name/FamilyName", "FamilyName", all);
                    addToDocument(idxDocument, e, "//Label/Name", "Label", all);
                    addToDocument(idxDocument, e, "//FreeTextAnnotation", "FreeTextAnnotation", all);
                    addToDocument(idxDocument, e, "//StructuredAnnotation/Who/Name", "Who", all);
                    addToDocument(idxDocument, e, "//StructuredAnnotation/Where/Name", "Where", all);
                    addToDocument(idxDocument, e, "//StructuredAnnotation/How/Name", "How", all);
                    addToDocument(idxDocument, e, "//StructuredAnnotation/Why/Name", "Why", all);
                    addToDocument(idxDocument, e, "//StructuredAnnotation/When/Name", "When", all);
                    addToDocument(idxDocument, e, "//StructuredAnnotation/WhatObject/Name", "WhatObjects", all);
                    addToDocument(idxDocument, e, "//StructuredAnnotation/WhatAction/Name", "WhatAction", all);

                    idxDocument.add(new Field("all", all.toString(), Field.Store.NO, Field.Index.TOKENIZED));

                    writer.addDocument(idxDocument);

                    if (statusBar != null) {
                        StringBuilder status = new StringBuilder(13).append(prefix);
                        status.append(df.format(((float) i) / numAllDocsPercent));
                        status.append('%');
                        statusBar.setStatus(status.toString());
                    }

                } catch (Exception e1) {
                    System.err.println("Error with file " + descriptions[i] + " (" + e1.getMessage() + ")");
                }
            }
            writer.optimize();
            writer.close();
            if (statusBar != null) {
                statusBar.setStatus("Indexing finished");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a path from the base directory to a index directory for storing
     * the fulltext index
     *
     * @param pathToIndex directory where the index dir should be created
     * @return path to index directory for with Lucene
     */
    public static String parseFulltextIndexDirectory(String pathToIndex) {
        String indexDir = pathToIndex;
        if (!indexDir.endsWith(System.getProperty("file.separator"))) indexDir += System.getProperty("file.separator");
        indexDir += "idx_fulltext";
        File indexDirFile = new File(indexDir);
        if (!indexDirFile.exists()) indexDirFile.mkdir();
        return indexDir;
    }

    /**
     * Creates a path from the base directory to a index directory for storing
     * the index of semantic objects
     *
     * @param pathToIndex directory where the index dir should be created
     * @return path to index directory for with Lucene
     */
    public static String parseSemanticIndexDirectory(String pathToIndex) {
        String indexDir = pathToIndex;
        if (!indexDir.endsWith(System.getProperty("file.separator"))) indexDir += System.getProperty("file.separator");
        indexDir += "idx_semantic";
        File indexDirFile = new File(indexDir);
        if (!indexDirFile.exists()) indexDirFile.mkdir();
        return indexDir;
    }

    private void addToDocument(Document document, Element root, String xPath, String fieldName, StringBuilder allContents) {
        List l = RetrievalToolkit.xpathQuery(root, xPath, null);
        StringWriter sw = new StringWriter(128);
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            Element e = (Element) iterator.next();
            sw.append(e.getTextTrim());
            sw.append(" ");
            allContents.append(e.getTextTrim());
            allContents.append(" ");
        }
        document.add(new Field(fieldName, sw.toString(), Field.Store.YES, Field.Index.TOKENIZED));
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
                    for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                        Element semanticElement = (Element) iterator.next();
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
            for (Iterator<Element> iterator = element2document.keySet().iterator(); iterator.hasNext();) {
                Element semElement = iterator.next();
                // needed for later XPath :( otherwise everthing in the whole document is retrieved.

                String fileList = getFileListFromNode(element2document.get(semElement));
                Document idxDocument = new Document();
                // adding the file itself ...
                idxDocument.add(new Field("files", fileList, Field.Store.YES, Field.Index.NO));

//                System.out.println(((Element) o).getTextTrim());

                StringBuilder all = new StringBuilder(255);
                // adding the label
//                addToDocument(idxDocument, semElement, "//Label/Name", "label", all);
                String elementLabel = semElement.getChild("Label", semElement.getNamespace()).getChildTextTrim("Name", semElement.getNamespace());
                idxDocument.add(new Field("label", elementLabel, Field.Store.YES, Field.Index.TOKENIZED));

                // adding the type:
                String elementType = semElement.getAttribute("type", xsi).getValue().trim();
                idxDocument.add(new Field("type", elementType, Field.Store.YES, Field.Index.NO));
                // adding the XML contents:
                String xmlString = outputter.outputString(semElement);
                idxDocument.add(new Field("xml", xmlString, Field.Store.YES, Field.Index.NO));
                // adding the id:
                idxDocument.add(new Field("id", elementMap.get(xmlString.trim().replaceAll("id=\"id_[0-9]*\"", "")).id + "", Field.Store.YES, Field.Index.NO));
                // adding all, unstored for retrieval only
                List l = RetrievalToolkit.xpathQuery(semElement, "*//*", null);
                for (Iterator it3 = l.iterator(); it3.hasNext();) {
                    Element e = (Element) it3.next();
                    all.append(e.getTextTrim());
                    all.append(" ");
                }
                idxDocument.add(new Field("all", all.toString(), Field.Store.NO, Field.Index.TOKENIZED));

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
            if (statusBar != null) statusBar.setStatus("Creating and merging powersets of available graphs");
            HashMap<Graph, HashSet<String>> graph2document = new HashMap<Graph, HashSet<String>>(descriptions.length);
            for (int i = 0; i < descriptions.length; i++) {
                try {
                    Element e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
                    List l = RetrievalToolkit.xpathQuery(e, "//Semantic/SemanticBase", null);
                    HashMap<String, Integer> docID2overallID = new HashMap<String, Integer>(l.size());
                    LinkedList<Relation> relations = new LinkedList<Relation>();
                    LinkedList<Integer> nodes = new LinkedList<Integer>();
                    for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                        Element semanticElement = (Element) iterator.next();
                        String xmlString = outputter.outputString(semanticElement);
                        int id = elementMap.get(xmlString.trim().replaceAll("id=\"id_[0-9]*\"", "")).id;
                        String docID = semanticElement.getAttribute("id").getValue();
                        docID2overallID.put(docID, id);
                        nodes.add(id);
                    }
                    // get all relations with global ids and eliminate inverse relations
                    l = RetrievalToolkit.xpathQuery(e, "//Graph/Relation", null);
                    for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                        Element relation = (Element) iterator.next();
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
                    for (Iterator<Integer> iterator = nodes.iterator(); iterator.hasNext();) {
                        nodeList.add(new Node(iterator.next()));
                    }
                    Graph g = new Graph(nodeList, relations);
//                    List<Graph> powerSet = new LinkedList<Graph>();
//                    powerSet.add(g);
                    HashSet<String> docs = new HashSet<String>(1);
                    docs.add(descriptions[i]);
                    graph2document.put(g, docs);
/*

                    // add all these subgraphs and the reference to the document to
                    // a data structure:
                    for (Iterator<Graph> iterator = powerSet.iterator(); iterator.hasNext();) {
                        Graph graph = iterator.next();
//                        List<Graph> relationsPowerSet = graph.getPowerSetOfRelations();
//                        for (Iterator<Graph> iterator1 = relationsPowerSet.iterator(); iterator1.hasNext();) {
//                            Graph graph1 = iterator1.next();
//                        }
                        // add graph if not trivial:
                        if (graph.getNodes().size() > 1) {
                            // containsKey for Graph does not match my needs -
                            // different graph objects reference the same graph!
                            if (string2graph.containsKey(graph.toString())) {
                                graph = string2graph.get(graph.toString());
                                graph2document.get(graph).add(descriptions[i]);
                            } else {
                                HashSet<String> docs = new HashSet<String>(1);
                                docs.add(descriptions[i]);
                                graph2document.put(graph, docs);
                            }
                        }
                    }
*/
                } catch (JDOMException e1) {
                    System.err.println("Exception in document #" + i + ": " + e1.getMessage());
                }
            }

            HashMap<String, Graph> str2graph = new HashMap<String, Graph>(graph2document.size() / 2);
            HashMap<Graph, HashSet<String>> g2d = new HashMap<Graph, HashSet<String>>(descriptions.length);

            /*
                For now we reduce the number of graphs by identifiying and merging duplicates and
                remove redundant entries:
            */
            for (Iterator<Graph> iterator = graph2document.keySet().iterator(); iterator.hasNext();) {
                Graph g = iterator.next();
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
            // todo: create real fast storable index of subgraphs instead of file :-) possible candidate a trie

            // for now we will store a simple text file:
            if (statusBar != null) statusBar.setStatus("Storing powersets of available graphs as file");
            String indexFile;
            if (!pathToIndex.endsWith(File.separator)) {
                indexFile = pathToIndex + File.separator + "idx_graphs.list";
            } else {
                indexFile = pathToIndex + "idx_graphs.list";
            }
            File f = new File(indexFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(f, false))));
            for (Iterator<Graph> iterator = graph2document.keySet().iterator(); iterator.hasNext();) {
                Graph g = iterator.next();
                bw.write(g.toString());
                for (Iterator<String> iterator1 = graph2document.get(g).iterator(); iterator1.hasNext();) {
                    String s = iterator1.next();
                    bw.write("|" + s);
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Searches for all available nodes with given query String
     *
     * @param queryString   query like "Mathias Lux" or some text inside a node.
     * @param whereToSearch defines the base directory for the search
     * @return a List of Matching nodes with their associated weights
     */
    public static List<Node> getNodes(String queryString, String whereToSearch) {
        return ((LucenePathIndexRetrievalEngine) RetrievalEngineFactory.getPathIndexRetrievalEngine()).getNodes(queryString, whereToSearch);
    }

    private String getFileListFromNode(List<String> list) {
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
        if (relationMapping.containsKey(relation.getType())) {
            result = new Relation(relation.getTarget(), relation.getSource(), relationMapping.get(relation.getType()));
        }
        return result;
    }

}



