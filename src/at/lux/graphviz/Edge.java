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


/**
 * Date: 14.09.2004
 * Time: 20:50:23
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface Edge {
    /**
     * Returns the starting node of the edge
     *
     * @return the starting node
     */
    public Node getStartNode();

    /**
     * Returns the ending node of the edge
     *
     * @return the ending node
     */
    public Node getEndNode();

    /**
     * Setter for the Nodes which are connected through the edge. Start and end are only
     * interesting in case of directed graphs.
     *
     * @param start
     * @param end
     */
    public void setNodes(Node start, Node end);
}
