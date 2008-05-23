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
 * Time: 21:28:38
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Vector2D {
    private double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void addVector2D(Vector2D vector2D) {
        x += vector2D.getX();
        y += vector2D.getY();
    }

    public void normalize() {
        double length = getLength();
        if (length != 0.0) {
            x = x / length;
            y = y / length;
        }
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public void multiply(double scalar) {
        if (scalar != 0.0) {
            x *= scalar;
            y *= scalar;
        } else {
            x = 0.0;
            y = 0.0;
        }
    }
}
