package at.lux.retrieval.graphisomorphism;

import org.jdom.Document;
import org.jdom.Element;

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
 * This implementation features a suboptimal approach to
 * the error correcting subgraph isomorphism problem.
 * Using discrete relaxation the problem is solved with
 * better runtime performance.
 * <p/>
 * This file is part of Caliph & Emir
 * Date: 16.02.2006
 * Time: 22:00:57
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SubOptimalSubgraphIsomorphism extends AbstractSubgraphIsomorphism {
    private EdgeDistanceFunction eDist;
    private NodeDistanceFunction nDist;
    private float lambda = 0.5f;

    public SubOptimalSubgraphIsomorphism(NodeDistanceFunction nDist, EdgeDistanceFunction eDist) {
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
    public SubOptimalSubgraphIsomorphism(NodeDistanceFunction nDist, EdgeDistanceFunction eDist, float lambda) {
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
        ArrayList<SubOptimalSubgraphIsomorphism.Relation> edgeCache2 = createEdgesCache(semanticRelationsDoc2);


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

        HashMap<Element, HashSet<Element>> bestFitAssignment = getBestFitAssignment(semanticBaseDoc1, semanticBaseDoc2, nodeMatrix);

        float[][] v = new float[semanticBaseDoc2.size()][semanticBaseDoc1.size()];
        for (int i = 0; i < semanticBaseDoc1.size(); i++) {
            float minimum = Float.MAX_VALUE;
            for (int j = 0; j < semanticBaseDoc2.size(); j++) {
                v[i][j] = nodeMatrix.getDistance(((Element) semanticBaseDoc2.get(i)), ((Element) semanticBaseDoc1.get(j)));
                if (v[i][j] < minimum) minimum = v[i][j];
            }
            for (int j = 0; j < semanticBaseDoc2.size(); j++) {
                v[i][j] = v[i][j] - minimum;
            }
        }
        // TODO: finish eventually ...
//        while (isOverAssigned(bestFitAssignment)) {
//            getShortestPath(bestFitAssignment, v, semanticBaseDoc1, semanticBaseDoc2);
//        }
        float distance = -1f;
        return distance;
    }

    private static float getMinimumInRow(float[] floats) {
        float min = Float.MAX_VALUE;
        for (float aFloat : floats) {
            if (aFloat > 0 && aFloat < min) min = aFloat;
        }
        return min;
    }

    private boolean isOverAssigned(HashMap<Element, HashSet<Element>> assignment) {
        boolean result = false;
        for (Element e : assignment.keySet()) {
            if (assignment.get(e).size() > 1) {
                result = true;
                break;
            }
        }
        return result;
    }

    private HashMap<Element, HashSet<Element>> getBestFitAssignment(List nodes1, List nodes2, DistanceMatrix nodeMatrix) {
        // todo: this is absolutely no performance ... change that in time ?!?
        HashMap<Element, HashSet<Element>> results = new HashMap<Element, HashSet<Element>>(nodes1.size());
        for (Object o1 : nodes1) {
            Element e1 = (Element) o1;
            results.put(e1, new HashSet<Element>());
        }
        for (Object o2 : nodes2) {
            Element e2 = (Element) o2;
            Element bestFit = null;
            float bestDistance = -1;
            for (Object o1 : nodes1) {
                Element e1 = (Element) o1;
                float distance = nodeMatrix.getDistance(e1, e2);
                if (bestFit == null) {
                    bestFit = e1;
                    bestDistance = distance;
                } else {
                    if (distance < bestDistance) {
                        bestFit = e1;
                        bestDistance = distance;
                    }
                }
            }
            results.get(bestFit).add(e2);
        }
        return results;

    }

}
