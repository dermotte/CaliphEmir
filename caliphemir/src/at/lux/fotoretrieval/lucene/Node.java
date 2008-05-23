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

/**
 * Date: 01.11.2004
 * Time: 11:59:54
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Node implements Comparable {
    private int nodeID;
    private float weight = 0f;
    private String label = null;
    private String type = null;

    /**
     * Creates a new Node with given ID and weight from [0,1]
     *
     * @param nodeID
     * @param weight has to be from [0,1]
     */
    public Node(int nodeID, float weight) {
        this.nodeID = nodeID;
        this.weight = weight;
        label = null;
    }

    public Node(int nodeID, float weight, String label) {
        this.nodeID = nodeID;
        this.weight = weight;
        this.label = label;
    }

    /**
     * Creates a new Node with weight 1.0f
     *
     * @param nodeID
     */
    public Node(int nodeID) {
        this.nodeID = nodeID;
        this.weight = 1.0f;
        label = null;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Checks if o is the same node as this Object.
     *
     * @param o Object (Node) to compare to this Node to
     * @return true if ID is the same number, false otherwise
     */
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Node) {
            Node n = (Node) o;
            if (n.getNodeID() == nodeID) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this Object.
     */
    public int compareTo(Object o) {
        Node n = (Node) o;
        return nodeID - n.getNodeID();
    }

    public Node clone() {
        Node node = new Node(nodeID, weight, label);
        node.setType(type);
        return node;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
