package at.lux.retrieval.graphisomorphism;

import org.jdom.Document;
import org.jdom.Element;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

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
public class FastSubgraphIsomorphism extends AbstractSubgraphIsomorphism {
    private EdgeDistanceFunction eDist;
    private NodeDistanceFunction nDist;
    /**
     * The higher lamba, the more weight is on the nodes.
     */
    private float lambda = 0.5f;

    /**
     * this float decides when to stop the search in the A* algorithm for
     * finding the minimum distance
     */
    private float maxDistance = 1.2f;

    public FastSubgraphIsomorphism(NodeDistanceFunction nDist, EdgeDistanceFunction eDist) {
        this.eDist = eDist;
        this.nDist = nDist;
    }

    public FastSubgraphIsomorphism(NodeDistanceFunction nDist, EdgeDistanceFunction eDist, float lambda, float maxDistance) {
        this.eDist = eDist;
        this.maxDistance = maxDistance;
        this.nDist = nDist;
        this.lambda = lambda;
    }

    /**
     * Creates a new SubgraphIsomorphism calculator. Lambda measures how much weight
     * is put on the nodes. The rest of the weight is put on the edges. Default is 0.5.
     *
     * @param nDist  node distance function
     * @param eDist  edge distance function
     * @param lambda measures how much weight is put on the nodes. The rest of the weight is put on the edges. Default is 0.5.
     */
    public FastSubgraphIsomorphism(NodeDistanceFunction nDist, EdgeDistanceFunction eDist, float lambda) {
        this.eDist = eDist;
        this.lambda = lambda;
        this.nDist = nDist;
    }

    public float getDistance(Document mpeg7Document1, Document mpeg7Document2) {
        List semanticBaseDoc1 = RetrievalToolkit.xpathQuery(mpeg7Document1.getRootElement(), "//Semantic/SemanticBase", null);
        List semanticBaseDoc2 = RetrievalToolkit.xpathQuery(mpeg7Document2.getRootElement(), "//Semantic/SemanticBase", null);

        List semanticRelationsDoc1 = RetrievalToolkit.xpathQuery(mpeg7Document1.getRootElement(), "//Semantic/Graph/Relation", null);
        List semanticRelationsDoc2 = RetrievalToolkit.xpathQuery(mpeg7Document2.getRootElement(), "//Semantic/Graph/Relation", null);

        ArrayList<Relation> edgeCache1 = createEdgesCache(semanticRelationsDoc1);
        ArrayList<FastSubgraphIsomorphism.Relation> edgeCache2 = createEdgesCache(semanticRelationsDoc2);


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
        return getAllMappings(semanticBaseDoc1, semanticBaseDoc2, nodeMatrix, edgeMatrix, edgeCache1, edgeCache2);
    }

    private float getAllMappings(Collection nodes1, Collection nodes2,
                                 DistanceMatrix nodeMatrix, DistanceMatrix edgeMatrix,
                                 ArrayList<FastSubgraphIsomorphism.Relation> edges1,
                                 ArrayList<FastSubgraphIsomorphism.Relation> edges2) {
        float distance = -1f;
        float d;
        for (Object aNode1 : nodes1) {
            Element e1 = (Element) aNode1;
            for (Object aNode2 : nodes2) {
                Element e2 = (Element) aNode2;
                // calculate distance of these two nodes including the distance of the edges ...
                float nodeDistance = nodeMatrix.getDistance(e1, e2);
                float edgeDistance = getEdgeDistance(e1, e2, edges1, edges2, edgeMatrix);

                ArrayList list1 = cloneAndRemoveObject(nodes1, e1);
                ArrayList list2 = cloneAndRemoveObject(nodes2, e2);
                d = getAllSubMappings(nodeDistance, edgeDistance, list1, list2, nodeMatrix, edgeMatrix, edges1, edges2);
                // get minimum ...
                if (d < distance || distance < 0) distance = d;
            }
        }
        return distance;
    }

    private float getEdgeDistance(Element e1, Element e2, ArrayList<Relation> edges1, ArrayList<Relation> edges2, DistanceMatrix edgeMatrix) {
        float edgeDistance = 0f;
        Relation rel1 = null;
        Relation rel2 = null;

        String id1 = e1.getAttributeValue("id");
        String id2 = e2.getAttributeValue("id");

        for (Relation relation : edges1) {
            if (relation.connectsNodes(id1, id2)) {
                rel1 = relation;
            }
        }

        id1 = e1.getAttributeValue("id");
        id2 = e2.getAttributeValue("id");

        for (Relation relation : edges2) {
            if (relation.connectsNodes(id1, id2)) {
                rel2 = relation;
            }
        }

        if ((rel1 != null && rel2 == null) || (rel1 == null && rel2 != null)) {
        } else if (rel1 != null && rel2 != null) {
            edgeDistance = edgeMatrix.getDistance(rel1.getElement(), rel2.getElement());
        }
        return edgeDistance;
    }

    private float getAllSubMappings(float nodeDistance, float edgeDistance,
                                    Collection nodes1, Collection nodes2,
                                    DistanceMatrix nodeMatrix, DistanceMatrix edgeMatrix,
                                    ArrayList<FastSubgraphIsomorphism.Relation> edges1,
                                    ArrayList<FastSubgraphIsomorphism.Relation> edges2) {
        float tmpDistance = lambda * nodeDistance + (1f - lambda) * edgeDistance;
        if (tmpDistance > maxDistance) return tmpDistance;
        float distance = -1f;
        float d;
        if (!(nodes1.isEmpty() || nodes2.isEmpty())) {
            for (Object aNode1 : nodes1) {
                Element e1 = (Element) aNode1;
                for (Object aNode2 : nodes2) {
                    Element e2 = (Element) aNode2;

                    float nodeDistance1 = nodeMatrix.getDistance(e1, e2) + nodeDistance;
                    float edgeDistance1 = getEdgeDistance(e1, e2, edges1, edges2, edgeMatrix) + edgeDistance;

                    ArrayList list1 = cloneAndRemoveObject(nodes1, e1);
                    ArrayList list2 = cloneAndRemoveObject(nodes2, e2);
                    d = getAllSubMappings(nodeDistance1, edgeDistance1, list1, list2, nodeMatrix, edgeMatrix, edges1, edges2);
                    // get minimum:
                    if (d < distance || distance < 0) distance = d;
                }
            }
        } else {
            distance = tmpDistance;
        }
        return distance;

    }


    private ArrayList cloneAndRemoveObject(Collection objects, Object obj) {
        ArrayList result = new ArrayList(objects.size());
        result.addAll(objects);
        result.remove(obj);
        return result;
    }
}
