package at.lux.retrieval.graphisomorphism;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;

import java.util.*;

import at.lux.fotoretrieval.RetrievalToolkit;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of Caliph & Emir
 * Date: 16.02.2006
 * Time: 22:00:57
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SubgraphIsomorphism extends AbstractSubgraphIsomorphism {
    private EdgeDistanceFunction eDist;
    private NodeDistanceFunction nDist;
    private float lambda = 0.5f;

    public SubgraphIsomorphism(NodeDistanceFunction nDist, EdgeDistanceFunction eDist) {
        this.eDist = eDist;
        this.nDist = nDist;
    }

    /**
     * Creates a new SubgraphIsomorphism calculator. Lambda measures how much weight
     * is put on the nodes. The rest of the weight is put on the edges. Default is 0.5.
     *
     * @param nDist  node distance function
     * @param eDist  edge distance function
     * @param lambda measures how much weight is put on the nodes. The rest of the weight is put on the edges. Default is 0.5.
     */
    public SubgraphIsomorphism(NodeDistanceFunction nDist, EdgeDistanceFunction eDist, float lambda) {
        this.eDist = eDist;
        this.lambda = lambda;
        this.nDist = nDist;
    }

    public float getDistance(Document mpeg7Document1, Document mpeg7Document2) {
        List semanticBaseDoc1 = RetrievalToolkit.xpathQuery(mpeg7Document1.getRootElement(), "//Semantic/SemanticBase", null);
        List semanticRelationsDoc1 = RetrievalToolkit.xpathQuery(mpeg7Document1.getRootElement(), "//Semantic/Graph/Relation", null);
        List semanticBaseDoc2 = RetrievalToolkit.xpathQuery(mpeg7Document2.getRootElement(), "//Semantic/SemanticBase", null);
        List semanticRelationsDoc2 = RetrievalToolkit.xpathQuery(mpeg7Document2.getRootElement(), "//Semantic/Graph/Relation", null);

        ArrayList<Relation> edgeCache1 = createEdgesCache(semanticRelationsDoc1);
        ArrayList<Relation> edgeCache2 = createEdgesCache(semanticRelationsDoc2);


        ArrayList<Element> edges = new ArrayList<Element>(semanticRelationsDoc1.size() + semanticRelationsDoc2.size());
        for (Object relation : semanticRelationsDoc1) {
            edges.add((Element) relation);
        }
        for (Object relation : semanticRelationsDoc2) {
            edges.add((Element) relation);
        }

        ArrayList<Element> nodes = new ArrayList<Element>(semanticBaseDoc1.size() + semanticBaseDoc2.size());
        for (Object relation : semanticBaseDoc1) {
            nodes.add((Element) relation);
        }
        for (Object relation : semanticBaseDoc2) {
            nodes.add((Element) relation);
        }

        // ToDo: rows and cols should not get mixed up ...
        DistanceMatrix edgeMatrix = new DistanceMatrix(edges, eDist);
        DistanceMatrix nodeMatrix = new DistanceMatrix(nodes, nDist);

        // ToDo: Combine the min distance check with the lookup for different mappings.
        ArrayList<HashMap<Element, Element>> mappings = getAllMappings(semanticBaseDoc1, semanticBaseDoc2);
        float minDistance = -1f;
        HashMap<Element, Element> minMapping = null;
        for (HashMap<Element, Element> mapping : mappings) {
            float distance = getMappingDistance(mapping, nodeMatrix, edgeMatrix, edgeCache1, edgeCache1);
            if (minDistance < 0 || distance < minDistance) {
                minDistance = distance;
                minMapping = mapping;
            }

        }
        return minDistance;
    }

    private float getMappingDistance(HashMap<Element, Element> mapping, DistanceMatrix nodeMatrix,
                                     DistanceMatrix edgeMatrix, ArrayList<Relation> edges1, ArrayList<Relation> edges2) {
        float nodeDistance = 0f;
        float edgeDistance = 0f;

        for (Element e1 : mapping.keySet()) {
            nodeDistance += nodeMatrix.getDistance(e1, mapping.get(e1));
            Relation rel1 = null;
            Relation rel2 = null;

            for (Element e2 : mapping.keySet()) {
                String id1 = e1.getAttributeValue("id");
                String id2 = e2.getAttributeValue("id");
                for (Relation relation : edges1) {
                    if (relation.connectsNodes(id1, id2)) {
                        rel1 = relation;
                    }
                }

                id1 = mapping.get(e1).getAttributeValue("id");
                id2 = mapping.get(e2).getAttributeValue("id");
                for (Relation relation : edges2) {
                    if (relation.connectsNodes(id1, id2)) {
                        rel2 = relation;
                    }
                }
            }

            if ((rel1 != null && rel2 == null) || (rel1 == null && rel2 != null)) {
                edgeDistance += eDist.getMaxDistance();
            } else if (rel1 != null && rel2 != null) {
                edgeDistance += edgeMatrix.getDistance(rel1.getElement(), rel2.getElement());
            }
        }
        return lambda * nodeDistance + (1f - lambda) * edgeDistance;
    }

    private ArrayList<HashMap<Element, Element>> getAllMappings(Collection nodes1, Collection nodes2) {
        ArrayList<HashMap<Element, Element>> results = new ArrayList<HashMap<Element, Element>>(32);
        for (Object aNode1 : nodes1) {
            Element e1 = (Element) aNode1;
            for (Object aNode2 : nodes2) {
                Element e2 = (Element) aNode2;
                HashMap<Element, Element> start = new HashMap<Element, Element>();
                start.put(e1, e2);
                ArrayList list1 = cloneAndRemoveObject(nodes1, e1);
                ArrayList list2 = cloneAndRemoveObject(nodes2, e2);
                results.addAll(getAllSubMappings(start, list1, list2));
            }
        }
        return results;
    }

    private List<HashMap<Element, Element>> getAllSubMappings(HashMap<Element, Element> mappings, Collection nodes1, Collection nodes2) {
        ArrayList<HashMap<Element, Element>> results = new ArrayList<HashMap<Element, Element>>(32);
        if (!(nodes1.isEmpty() || nodes2.isEmpty())) {
            for (Object aNode1 : nodes1) {
                Element e1 = (Element) aNode1;
                for (Object aNode2 : nodes2) {
                    Element e2 = (Element) aNode2;
                    HashMap<Element, Element> start = new HashMap<Element, Element>();
                    start.putAll(mappings);
                    start.put(e1, e2);
                    ArrayList list1 = cloneAndRemoveObject(nodes1, e1);
                    ArrayList list2 = cloneAndRemoveObject(nodes2, e2);
                    results.addAll(getAllSubMappings(start, list1, list2));
                }
            }
        } else {
            results.add(mappings);
        }
        return results;

    }


    private ArrayList cloneAndRemoveObject(Collection objects, Object obj) {
        ArrayList result = new ArrayList(objects.size());
        result.addAll(objects);
        result.remove(obj);
        return result;
    }

}
