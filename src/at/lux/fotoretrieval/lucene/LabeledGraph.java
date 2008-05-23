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
 * Time: 22:44:56
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LabeledGraph {
    public HashMap<String, HashMap<String, String>> edgeList;
    public HashMap<String, HashMap<String, String>> inverseEdgeList;

    /**
     * Creates a LbeledGRaph from a Graph.
     * @param g
     */
    public LabeledGraph(Graph g) {
        List<Node> nodeList = g.getNodes();
        edgeList = new HashMap<String, HashMap<String, String>>(nodeList.size());
        inverseEdgeList = new HashMap<String, HashMap<String, String>>(nodeList.size());
        // add nodes to edgeList:
        for (Iterator<Node> iterator = nodeList.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            edgeList.put(node.getNodeID() + "", new HashMap<String, String>());
            inverseEdgeList.put(node.getNodeID() + "", new HashMap<String, String>());
        }

        List<Relation> relationsList = g.getRelations();
        for (Iterator<Relation> iterator = relationsList.iterator(); iterator.hasNext();) {
            Relation relation = iterator.next();
            edgeList.get(relation.getSource() + "").put(relation.getType(), relation.getTarget() + "");

            // invert the relation type ...
            String inverseRelationType = relation.getType();
            if (Relation.relationMapping.containsKey(inverseRelationType)) {
                inverseRelationType = Relation.relationMapping.get(inverseRelationType);
            } else if (Relation.relationMappingInverse.containsKey(inverseRelationType)) {
                inverseRelationType = Relation.relationMappingInverse.get(inverseRelationType);
            }
            inverseEdgeList.get(relation.getTarget() + "").put(inverseRelationType, relation.getSource() + "");
        }
    }

    /**
     * Extracts all available paths with two hops. The nodes are orders
     * by their lexical order.
     * @return a list of paths with length 2.
     */
    public List<Path> get2Paths() {
        LinkedList<Path> pathList = new LinkedList<Path>();
        Set<String> nodes = edgeList.keySet();
        for (Iterator<String> sourceIterator = nodes.iterator(); sourceIterator.hasNext();) {
            String source = sourceIterator.next();
            LinkedList<Path> tmpPathList = new LinkedList<Path>();
            Path p = new Path(source);
            HashMap<String, String> edges = getEdges(source);
            for (Iterator<String> iterator = edges.keySet().iterator(); iterator.hasNext();) {
                String s = iterator.next();
                Path pp = p.clone();
                pp.addRelation(s, edges.get(s));
                tmpPathList.add(pp);
            }
            for (Iterator<Path> iterator = tmpPathList.iterator(); iterator.hasNext();) {
                Path path = iterator.next();
                String node = path.getEndPoint();
                edges = getEdges(node);
                for (Iterator<String> iterator1 = edges.keySet().iterator(); iterator1.hasNext();) {
                    String s = iterator1.next();
                    Path pp = path.clone();
                    if (pp.addRelation(s, edges.get(s))) {
                        pathList.add(pp);
                    }
                }
            }
        }

        Collections.sort(pathList);
        Path last = null;
        List<Path> toRemove = new LinkedList<Path>();
        for (Iterator<Path> iterator = pathList.iterator(); iterator.hasNext();) {
            Path path = iterator.next();
            if (last != null) {
                if (last.isTheSamePath(path)) {
                    toRemove.add(last);
                }
            }
            last = path;
        }
        pathList.removeAll(toRemove);
        return pathList;
    }

    private HashMap<String, String> getEdges(String node) {
        HashMap<String, String> originalEdges = edgeList.get(node);
        HashMap<String, String> invertedEdges = inverseEdgeList.get(node);
        HashMap<String, String> edges = new HashMap<String, String>(originalEdges.size() + invertedEdges.size());

        for (Iterator<String> iterator = originalEdges.keySet().iterator(); iterator.hasNext();) {
            String s = iterator.next();
            edges.put(s, originalEdges.get(s));
        }
        for (Iterator<String> iterator = invertedEdges.keySet().iterator(); iterator.hasNext();) {
            String s = iterator.next();
            edges.put(s, invertedEdges.get(s));
        }
        return edges;
    }

}
