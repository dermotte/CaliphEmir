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
package at.lux.fotoretrieval.lucene;

import java.util.*;

/**
 * Date: 01.11.2004
 * Time: 11:56:22
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Graph implements Comparable {
    LinkedList<Node> nodes;
    LinkedList<Relation> relations;
    private static final boolean MCS_DISTANCE_TAKE_RELATIONS_INTO_ACCOUNT = false;

    public Graph(List<Node> nodes, List<Relation> relations) {
        this.nodes = new LinkedList<Node>();
        this.relations = new LinkedList<Relation>();
        this.nodes.addAll(nodes);
        this.relations.addAll(relations);
    }

    /**
     * Creates a graph from its String representation
     *
     * @param graph as String like output from toString method
     */
    public Graph(String graph) {
        StringTokenizer st = new StringTokenizer(graph, "[");
        this.nodes = new LinkedList<Node>();
        this.relations = new LinkedList<Relation>();
        Relation currentRelation = null;
        boolean nodesFinished = false;
        while (st.hasMoreTokens()) {
            String s = st.nextToken().trim();
            s = s.substring(0, s.length() - 1);
            try {
                if (s.matches("\\d+")) {
                    // String defines a node
                    int node = Integer.parseInt(s);
                    nodes.add(new Node(node));
                } else {
                    // String defines a relation
                    StringTokenizer relationTokenizer = new StringTokenizer(s);
                    String relation = relationTokenizer.nextToken();
                    String sourceNode = relationTokenizer.nextToken();
                    String targetNode = relationTokenizer.nextToken();
                    relations.add(new Relation(Integer.parseInt(sourceNode), Integer.parseInt(targetNode), relation));
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing!");
            }
        }
    }

    /**
     * Returns the power set of all possible subgraphs
     *
     * @return the power set of all possible sub graphs
     */
    public List<Graph> getPowerSet() {
        List<Graph> pws1 = getPowerSetOfNodes();
        List<Graph> resultList = new LinkedList<Graph>();
        for (Iterator<Graph> iterator = pws1.iterator(); iterator.hasNext();) {
            Graph graph = iterator.next();
            resultList.addAll(graph.getPowerSetOfRelations());
        }

        // remove redundant entries:
        Collections.sort(resultList);
        Graph lastGraph = null;
        List<Graph> newResultList = new LinkedList<Graph>();
        for (Iterator<Graph> iterator = resultList.iterator(); iterator.hasNext();) {
            Graph graph = iterator.next();
            if (lastGraph != null && !graph.toString().equals(lastGraph.toString())) {
                newResultList.add(graph);
            }
            lastGraph = graph;
        }
        return resultList;
    }

    /**
     * Returns the power set of all possible subgraphs only taking the nodes into account
     *
     * @return
     */
    public List<Graph> getPowerSetOfNodes() {
        if (nodes.size() > 1) {
            LinkedList<Graph> powerSet = new LinkedList<Graph>();

            LinkedList<Node> nodeList = new LinkedList<Node>();
            LinkedList<Relation> relationList = new LinkedList<Relation>();
            LinkedList<Relation> removedRelationList = new LinkedList<Relation>();
            nodeList.addAll(nodes);
            Node first = nodeList.removeFirst();

            for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
                Relation relation = iterator.next();
                if (!relation.isSourceOrTarget(first.getNodeID())) {
                    relationList.add(relation);
                } else {
                    removedRelationList.add(relation);
                }
            }

            Graph g = new Graph(nodeList, relationList);
            // add all available from the smaller graphs powerset:
            List<Graph> pwSet = g.getPowerSet();
            powerSet.addAll(pwSet);
            // add all from the smaller graphs powerset in union with removed node:
            for (Iterator<Graph> iterator = pwSet.iterator(); iterator.hasNext();) {
                Graph graph = iterator.next().clone();
                for (Iterator<Relation> iterator1 = removedRelationList.iterator(); iterator1.hasNext();) {
                    Relation relation = iterator1.next();
                    for (Iterator<Node> iterator2 = graph.getNodes().iterator(); iterator2.hasNext();) {
                        Node node = iterator2.next();
                        if (relation.isSourceOrTarget(node.getNodeID())) {
                            if (!graph.getRelations().contains(relation)) {
                                graph.getRelations().add(relation);
                            } else {
//                                System.out.println("Oops! Relation was already there!");
                            }
                        }
                    }
                }
                // check if the node is not already inside :)
                boolean doNotAdd = false;
                for (Iterator<Node> iterator1 = graph.getNodes().iterator(); iterator1.hasNext();) {
                    Node node = iterator1.next();
                    if (node.getNodeID() == first.getNodeID()) {
                        doNotAdd = true;
//                        System.out.println("Oops!");
                    }
                }
                // add it if not already there
                if (!doNotAdd) {
                    graph.getNodes().add(first);
                }
                powerSet.add(graph);
            }
            return powerSet;
        } else {
            // if there is only one element:
            LinkedList<Node> nodeList = new LinkedList<Node>();
            nodeList.addAll(nodes);
            Graph gr = new Graph(nodes, new LinkedList<Relation>());
            LinkedList<Graph> graphs = new LinkedList<Graph>();
            // Graph with one element
            graphs.add(gr);
            // empty graph:
            graphs.add(new Graph(new LinkedList<Node>(), new LinkedList<Relation>()));
            return graphs;
        }
    }

    /**
     * Returns the power set taking relations into account
     *
     * @return
     */
    public List<Graph> getPowerSetOfRelations() {
        if (relations.size() > 1) {
            LinkedList<Relation> rels = new LinkedList<Relation>(relations);
            Relation firstRel = rels.removeFirst();
            Graph g = new Graph(nodes, rels);

            List<Graph> tmpResults = g.getPowerSetOfRelations();
            LinkedList<Graph> results = new LinkedList<Graph>();
            for (Iterator<Graph> iterator = tmpResults.iterator(); iterator.hasNext();) {
                Graph graph = iterator.next();
                results.add(graph);
                LinkedList<Relation> tmp = new LinkedList<Relation>(graph.getRelations());
                tmp.add(firstRel);
                results.add(new Graph(graph.nodes, tmp));
            }
            return results;
        } else {
            LinkedList<Graph> result = new LinkedList<Graph>();
            if (relations.size() > 0) {
                // there is one single relation:
                // creating a graph without relation
                Graph g1 = new Graph(nodes, new LinkedList<Relation>());
                // adding the one with one relation
                result.add(this);
                // adding the one with zero relations
                result.add(g1);
            } else {
                result.add(this);
            }
            return result;
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public Graph clone() {
        LinkedList<Node> nList = new LinkedList<Node>();
        LinkedList<Relation> rList = new LinkedList<Relation>();
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            nList.add(iterator.next().clone());
        }
        for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
            rList.add(iterator.next().clone());
        }
        return new Graph(nList, rList);
    }

    /**
     * Creates a String representation from a Graph
     *
     * @return a String representation
     */
    public String toString() {
//        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
//        df.setMinimumIntegerDigits(5);
//        df.setMaximumFractionDigits(0);
//        df.setGroupingUsed(false);
        StringBuilder sb = new StringBuilder(64);
        Collections.sort(nodes);
        Collections.sort(relations);
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node n = iterator.next();
            sb.append("[");
            sb.append(n.getNodeID());
            sb.append("] ");
        }
        for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
            Relation relation = iterator.next();
            sb.append("[");
            sb.append(relation.toString());
            sb.append("] ");
        }
        return sb.toString().trim();
    }

    /**
     * Returns true if the graph contains the same relations and nodes,
     * that is when the string representation is equal.
     *
     * @param o
     * @return true if the graph contains the same relations and nodes
     */
    public boolean equals(Object o) {
        if (o instanceof Graph) {
            Graph g = (Graph) o;
            if (g.toString().equals(toString())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public float getMcsSimilarity(Graph g) {
        return getMcsSimilarity(g, false);
    }

    public float getMcsSimilarity(Graph g, boolean useRelations) {
        float similarity = 1f;
        boolean matched = false;
        LinkedList<Integer> matchedNodes = new LinkedList<Integer>();
        LinkedList<Relation> matchedRelations = new LinkedList<Relation>();
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            for (Iterator<Node> iterator1 = g.getNodes().iterator(); iterator1.hasNext();) {
                Node n = iterator1.next();
                if (node.equals(n)) {
                    similarity = similarity * node.getWeight() * n.getWeight();
                    matched = true;
                    matchedNodes.add(node.getNodeID());
                }
            }
        }

        if (useRelations) {
            for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
                Relation relation = iterator.next();
                if (matchedNodes.contains(relation.getSource()) && matchedNodes.contains(relation.getTarget())) {
                    for (Iterator<Relation> it = g.getRelations().iterator(); it.hasNext();) {
                        Relation r = it.next();
                        int tgt1 = relation.getTarget();
                        int tgt2 = r.getTarget();
                        int src1 = relation.getSource();
                        int src2 = r.getSource();
                        if ( (Math.min(tgt1, src1) == Math.min(tgt2, src2)) && (Math.max(tgt1, src1) == Math.max(tgt2, src2))) {
                            // relation matches!
                            matchedRelations.add(relation);
                        }
                    }
                }
            }
        }
        if (!matched)
            similarity = 0f;
        else {
            if (useRelations) {
                float thisSize = (float) (nodes.size());
                float gSize = (float) (g.getNodes().size());
                similarity = similarity / Math.max(thisSize, gSize);
            } else {
                float thisSize = (float) (nodes.size() + relations.size());
                float gSize = (float) (g.getNodes().size() + g.getRelations().size());

                float mcsSize = (float) (matchedNodes.size() + matchedRelations.size());

                similarity = similarity * mcsSize / Math.max(thisSize, gSize);
            }
        }
        return similarity;
    }

    /**
     * Calculates the maximum common subgraph metric: The maximum common
     * subgraph is searched and its size compared to the max size of either of
     * the input graphs is the similarity.
     * @param g defines which graph has to be compared to this one
     * @return a distance in [0,1]
     */
    public float getMcsDistance(Graph g) {
        int matched = 0;
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            if (g.nodes.contains(node)) {
                matched++;
            }
        }
        for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
            Relation relation = iterator.next();
            if (g.relations.contains(relation)) {
                matched++;
            }
        }
        float similarity = (float) matched / (float) Math.max(nodes.size() + relations.size(), g.nodes.size() + g.relations.size());
        return 1-similarity;
    }

    public float getSuffixTreeDistance() {
        float distance = 0;
        
        return distance;
    }

    public int compareTo(Object o) {
        return toString().compareTo(((Graph) o).toString());
    }

    /**
     * Returns all relations where the given node is involveld (target or source)
     * @param node
     * @return
     */
    public Set<Relation> getRelations(Node node) {
        return getRelations(node.getNodeID());
    }

    public Set<Relation> getRelations(int nodeID) {
        HashSet<Relation> result = new HashSet<Relation>(relations.size()/2);
        for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
            Relation relation = iterator.next();
            if (relation.getSource() == nodeID || relation.getTarget() == nodeID) {
                result.add(relation);
            }
        }
        return result;
    }
}
