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
package at.lux.graphviz;

import java.util.*;

/**
 * Date: 14.09.2004
 * Time: 21:05:09
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SpringEmbedder {
    HashMap<Node, HashSet<Node>> node2nodes;
    List<? extends Node> nodeList;
    List<? extends Edge> edgeList;

    // -----------------------------------------------
    // Parameters
    // -----------------------------------------------
    // force function params
    private double c1, c2, c3, c4;
    // when to stop:
    private double stopCondition = 0.00005;
    // or maximum steps:
    private int maxSteps = 5000;
    // with additional steps for finetuning:
    private int additionalSteps = 25;
    // has attracting force in the center
    private boolean hasInvisibleCenterNode = true;
    private boolean edgesRepelNodes = false;

    // Should the space be scaled to [0,1]^2 after each step?
    private boolean scaleDownSpace = false;
    // Should the movement vector for each node be normalized?
    private boolean normalizeMovementVector = false;

    // -----------------------------------------------
    // internal fields for calculation
    // -----------------------------------------------
    private int countSteps, additionalStepCountdown;
    private double overallMovement;

    public SpringEmbedder(List<? extends Node> nodeList, List<? extends Edge> edgeList) {
        this.nodeList = nodeList;
        this.edgeList = edgeList;
        overallMovement = Double.MAX_VALUE;
        additionalStepCountdown = additionalSteps;
        countSteps = 0;
        init();
    }

    private void init() {
        // init params:
        c1 = 2.0;
        c2 = 1.0;
        c3 = 1.0;
        c4 = 0.1;

        countSteps = 0;

        // init lookup:
        node2nodes = new HashMap<Node, HashSet<Node>>(nodeList.size());
        for (Iterator<? extends Node> iterator = nodeList.iterator(); iterator.hasNext();) {
            node2nodes.put(iterator.next(), new HashSet<Node>());
        }
        for (Iterator<? extends Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
            Edge e = iterator.next();
            node2nodes.get(e.getStartNode()).add(e.getEndNode());
            node2nodes.get(e.getEndNode()).add(e.getStartNode());
        }
    }

    private HashMap<Node, Vector2D> calculateForces() {
        HashMap<Node, Vector2D> result = new HashMap<Node, Vector2D>(nodeList.size());
        overallMovement = 0.0;
        for (Iterator<? extends Node> iterator = nodeList.iterator(); iterator.hasNext();) {
            double force = 0.0;
            Node nodeA = iterator.next();
            Vector2D f = new Vector2D(0.0, 0.0);

            DefaultNode centerNode = new DefaultNode(0.0, 0.0);

            if (hasInvisibleCenterNode) {
                // attracting force from center ...
                double v = (nodeA.distance(centerNode) / c2);
                force = v * v;
                Vector2D toCenter = nodeA.direction(centerNode);
                toCenter.normalize();
                toCenter.multiply(force);
                f.addVector2D(toCenter);
            }

            if (edgesRepelNodes) {
                List<Node> border = new LinkedList<Node>();
                border.add(new DefaultNode(0.0, nodeA.getY()));
                border.add(new DefaultNode(1.0, nodeA.getY()));
                border.add(new DefaultNode(nodeA.getX(), 0.0));
                border.add(new DefaultNode(nodeA.getX(), 1.0));

                for (Iterator<Node> itBorder = border.iterator(); itBorder.hasNext();) {
                    Node nodeB = itBorder.next();
                    force = 0.0;
                    // these two nodes repel each other
                    force = -c3 / Math.sqrt(nodeA.distance(nodeB));
                    Vector2D vector = nodeA.direction(centerNode);
                    vector.normalize();
                    vector.multiply(force);
                    f.addVector2D(vector);
                }
            }

            for (Iterator<? extends Node> it1 = nodeList.iterator(); it1.hasNext();) {
                Node nodeB = it1.next();
                if (!nodeA.equals(nodeB)) {
                    force = 0.0;
                    if (node2nodes.get(nodeA).contains(nodeB)) {
                        // these two nodes attract each other
                        force = c1 * Math.log(nodeA.distance(nodeB) / c2);
                    } else {
                        // these two nodes repel each other
                        force = -c3 / Math.sqrt(nodeA.distance(nodeB));
                    }
                    Vector2D vector = nodeA.direction(nodeB);
                    vector.normalize();
                    vector.multiply(force);
                    f.addVector2D(vector);
                }
            }
            if (normalizeMovementVector) {
                f.normalize();
            }
            // here goes the step into the right direction:
            f.multiply(c4);
            overallMovement += f.getLength();
            result.put(nodeA, f);
        }
//        System.out.println(overallMovement);
        return result;
    }

    private void moveVertices(HashMap<Node, Vector2D> forces) {
        for (Iterator<Node> iterator = forces.keySet().iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            node.move(forces.get(node));
        }
    }

    public int step() {
        double lastMovement = overallMovement;
        moveVertices(calculateForces());
        if (scaleDownSpace) {
            scaleDown();
        }
        countSteps++;
        // stop when movement is small enough, there are additional steps for
        // for hopping over local minima
        if (Math.abs(overallMovement - lastMovement) < stopCondition) {
            additionalStepCountdown--;
        } else {
            additionalStepCountdown = additionalSteps;
        }
        if (additionalStepCountdown < 0 || countSteps > maxSteps) return -1;
        return countSteps;
    }

    private void scaleDown() {
        double xMin = 1.0, xMax = 0.0, yMin = 1.0, yMax = 0.0;
        for (Iterator<? extends Node> iterator = nodeList.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            if (node.getX() < xMin) xMin = node.getX();
            if (node.getX() > xMax) xMax = node.getX();
            if (node.getY() < yMin) yMin = node.getY();
            if (node.getY() > yMax) yMax = node.getY();
        }
        for (Iterator<? extends Node> iterator = nodeList.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            node.setX((node.getX() - xMin) / (xMax - xMin));
            node.setY((node.getY() - yMin) / (yMax - yMin));
            assert(node.getX() >= 0.0);
            assert(node.getX() <= 1.0);
            assert(node.getY() >= 0.0);
            assert(node.getY() <= 1.0);
        }
    }

    public List<? extends Node> getNodeList() {
        return nodeList;
    }

    public List<? extends Edge> getEdgeList() {
        return edgeList;
    }

    /**
     * Initial values: c1 = 2.0; c2 = 1.0; c3 = 1.0; c4 = 0.1;
     * @param c1 is multiplied with the logarithmic attraction force: force = c1 * Math.log(nodeA.distance(nodeB) / c2);
     * @param c2 defines how much nodes attract each other: divisor of the distance: force = c1 * Math.log(nodeA.distance(nodeB) / c2);
     * @param c3 defines how much nodes repel each other: force = -c3 / Math.sqrt(nodeA.distance(nodeB));
     * @param c4 defines how wide to step (the length of the movement vector)
     * @param hasInvisibleCenterNode
     */
    public void setEmbeddingParameters(double c1, double c2, double c3, double c4, boolean hasInvisibleCenterNode) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
        this.hasInvisibleCenterNode = hasInvisibleCenterNode;
    }

    public void setHasInvisibleCenterNode(boolean hasInvisibleCenterNode) {
        this.hasInvisibleCenterNode = hasInvisibleCenterNode;
    }

    public void setNormalizeMovementVector(boolean normalizeMovementVector) {
        this.normalizeMovementVector = normalizeMovementVector;
    }

    public void setScaleDownSpace(boolean scaleDownSpace) {
        this.scaleDownSpace = scaleDownSpace;
    }

    public double getC4() {
        return c4;
    }

    public void setC4(double c4) {
        this.c4 = c4;
    }
}
