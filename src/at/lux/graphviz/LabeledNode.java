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
 * Date: 06.01.2005
 * Time: 15:32:05
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LabeledNode implements Node {
    double x, y;
    String label;

    public LabeledNode(double x, double y) {
        this.x = x;
        this.y = y;
        this.label = null;
    }

    public LabeledNode(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double distance(Node node) {
        double dx = x - node.getX();
        double dy = y - node.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Vector2D direction(Node node) {
        return new Vector2D(node.getX() - x, node.getY() - y);
    }

    public void move(Vector2D whereToMove) {
        x += whereToMove.getX();
        y += whereToMove.getY();
    }

}