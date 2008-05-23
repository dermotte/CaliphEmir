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
 * Time: 20:50:17
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface Node {
    /**
     * X has to be in normalized [0,1]
     *
     * @return x from [0,1]
     */
    public double getX();

    /**
     * Y has to be in normalized [0,1]
     *
     * @return y from [0,1]
     */
    public double getY();

    /**
     * X has to be in normalized [0,1]
     *
     * @param x from [0,1]
     */
    public void setX(double x);

    /**
     * Y has to be in normalized [0,1]
     *
     * @param y from [0,1]
     */
    public void setY(double y);

    /**
     * Calculates the distance between two nodes.
     *
     * @param node
     * @return a double telling the distance in euclidean metrics.
     */
    public double distance(Node node);

    /**
     * returns a pair of doubles (x,y) telling the direction vector from this node to
     * parameter node
     *
     * @param node
     * @return
     */
    public Vector2D direction(Node node);

    /**
     * Moves node along given vector
     *
     * @param whereToMove
     */
    public void move(Vector2D whereToMove);
}
