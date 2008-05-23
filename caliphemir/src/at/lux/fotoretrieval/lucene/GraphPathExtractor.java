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
 * Date: 25.03.2005
 * Time: 22:42:05
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GraphPathExtractor {
    /**
     * Extracts all paths of a certain length
     *
     * @param graph
     * @param length if length is -1 all paths are returned
     * @return
     */
    public static Path[] extractPaths(String graph, int length) {
        ArrayList<Path> list = new ArrayList<Path>();
        if (length == 0) pathLength0(new Graph(graph), list);
        else if (length == 1) pathLength1(new Graph(graph), list);
        else if (length > 0) {
            getPaths(new Graph(graph), length, list);
        } else {
            boolean added = false;
            Graph graphObj = new Graph(graph);
            pathLength0(new Graph(graph), list);
            pathLength1(new Graph(graph), list);
            int pathLenght = 2;
            do {
                added = false;
                int listSize = list.size();
                getPaths(graphObj, length, list);
                if (listSize < list.size()) added = true;
                length++;
            } while (added);
        }
        return (Path[]) list.toArray(new Path[list.size()]);
    }

    public static String[] extractPathsAsStrings(String graph, int length) {
        Graph g = new Graph(graph);
        return null;
    }

    /**
     * Node extractor
     *
     * @param graph
     * @param list
     */
    private static void pathLength0(Graph graph, ArrayList<Path> list) {
        List<Node> nodes = graph.getNodes();
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            Path p = new Path(node.getNodeID() + "");
            list.add(p);
        }
    }

    /**
     * Triple extractor
     *
     * @param graph
     * @param list
     */
    private static void pathLength1(Graph graph, ArrayList<Path> list) {
        List<Relation> relations = graph.getRelations();
        for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
            Relation relation = iterator.next();
            Path p = new Path(relation.getSource() + "");
            p.addRelation(relation.getType(), relation.getTarget() + "");
            list.add(p);
        }
    }

    private static Set followPath(Path p, Node node, Graph g, int length) {
        Set<Relation> relations = g.getRelations(Integer.parseInt(p.getNodes().getLast()));
        HashSet<Path> paths = new HashSet<Path>();
        if (length > 0) {
            for (Iterator<Relation> iterator = relations.iterator(); iterator.hasNext();) {
                Relation relation = iterator.next();
                // check which node is not the one we come from...
                int nodeID = relation.getSource();
                if (nodeID == node.getNodeID()) nodeID = relation.getTarget();
                if (p.containsNode(nodeID + "")) {
                    // do nothing if the new node is already in the path ...
                    // so we get all acyclic paths.
                } else {
                    Path path = p.clone();
                    String target = relation.getTarget() + "";
                    if (!path.containsNode(target)) {
                        path.addRelation(relation.getType(), target);
                    } else {
                        path.addRelation(Relation.invertRelationType(relation.getType()),
                                relation.getSource() + "");
                    }
                    paths.addAll(followPath(path, node, g, length - 1));
                }
            }
        } else {
            paths.add(p);
        }
        return paths;
    }

    private static void getPaths(Graph graph, int length, ArrayList<Path> list) {
        List<Node> nodes = graph.getNodes();
        HashSet<Path> paths = new HashSet<Path>();
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            Set<Relation> relations = graph.getRelations(node);
            for (Iterator<Relation> iterator1 = relations.iterator(); iterator1.hasNext();) {
                Relation relation = iterator1.next();
                if (relation.getTarget() == node.getNodeID()) {
                    // invert the relation.
                    relation = relation.clone();
                    relation.invert();
                }
                Path p = new Path(relation);
                Set newPaths = followPath(p, node, graph, length - 1);
                paths.addAll(newPaths);
            }
        }
        list.addAll(paths);
    }
}
