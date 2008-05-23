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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at) and the Know-Center Graz
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */

/*
 * Date: 31.07.2002
 * Time: 09:49:20
 */
package at.knowcenter.caliph.objectcatalog.graphics;

import java.awt.*;
import java.awt.geom.*;

/**
 * Class derived from Shape to create an arrow.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Arrow implements Shape {
    private double ath = 4.0;
    private Line2D line;
    private double thickness;
    private GeneralPath gp;

    /**
     * Main Constructor
     *
     * @param line      is the line the arrow is painted along, Point 1 of the Line is the source, Point 2 of the line the target of the arrow.
     * @param thickness gives a hint how bold the arrow is.
     */
    public Arrow(Line2D line, double thickness) {
        this.line = line;
        this.thickness = thickness;
        GeneralPath generalPath = new GeneralPath();

        Point2D sp = line.getP1();
        Point2D tp = line.getP2();

        double vx = (sp.getX() - tp.getX());
        double vy = (sp.getY() - tp.getY());

        double length = Math.sqrt((vx * vx) + (vy * vy));
        vx = vx / length;
        vy = vy / length;

        double nx = vy * thickness / 2;
        double ny = -vx * thickness / 2;

        double athVal = ath;
        double bx = (double) tp.getX() + vx * athVal * 3.0;
        double by = (double) tp.getY() + vy * athVal * 3.0;

        Point2D.Double p1 = new Point2D.Double(sp.getX() + nx, sp.getY() + ny);
        Point2D.Double p2 = new Point2D.Double(bx + nx, by + ny);
        Point2D.Double p3 = new Point2D.Double(bx + athVal * nx, by + athVal * ny);
        Point2D.Double p4 = new Point2D.Double(tp.getX(), tp.getY());
        Point2D.Double p5 = new Point2D.Double(bx - athVal * nx, by - athVal * ny);
        Point2D.Double p6 = new Point2D.Double(bx - nx, by - ny);
        Point2D.Double p7 = new Point2D.Double(sp.getX() - nx, sp.getY() - ny);

        generalPath.moveTo((float) p1.getX(), (float) p1.getY());
        generalPath.lineTo((float) p2.getX(), (float) p2.getY());
        generalPath.lineTo((float) p3.getX(), (float) p3.getY());
        generalPath.lineTo((float) p4.getX(), (float) p4.getY());
        generalPath.lineTo((float) p5.getX(), (float) p5.getY());
        generalPath.lineTo((float) p6.getX(), (float) p6.getY());
        generalPath.lineTo((float) p7.getX(), (float) p7.getY());
        generalPath.closePath();

        gp = generalPath;

    }

    /**
     * Tests if the specified <code>Point2D</code> is inside the boundary
     * of this <code>Shape</code>.
     *
     * @param p the specified <code>Point2D</code>
     * @return <code>true</code> if this <code>Shape</code> contains the
     *         specified <code>Point2D</code>, <code>false</code> otherwise.
     */
    public boolean contains(Point2D p) {
        return gp.contains(p);
    }


    /**
     * Tests if the specified <code>Rectangle2D</code>
     * is inside the boundary of this <code>Shape</code>.
     *
     * @param r a specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Shape</code> bounds the
     *         specified <code>Rectangle2D</code>; <code>false</code> otherwise.
     */
    public boolean contains(Rectangle2D r) {
        return gp.contains(r);
    }

    /**
     * Tests if the specified coordinates are inside the boundary of
     * this <code>Shape</code>.
     *
     * @param x the specified x coordinates
     * @param y the specified y coordinates
     * @return <code>true</code> if the specified coordinates are inside this
     *         <code>Shape</code>; <code>false</code> otherwise
     */
    public boolean contains(double x, double y) {
        return gp.contains(x, y);
    }

    /**
     * Tests if the specified rectangular area is inside the boundary of
     * this <code>Shape</code>.
     *
     * @param x,&nbsp;y the specified coordinates
     * @param w         the width of the specified rectangular area
     * @param h         the height of the specified rectangular area
     * @return <code>true</code> if this <code>Shape</code> contains
     *         the specified rectangluar area; <code>false</code> otherwise.
     */
    public boolean contains(double x, double y, double w, double h) {
        return gp.contains(x, y, w, h);
    }

    /**
     * Return the bounding box of the path.
     *
     * @return a {@link java.awt.Rectangle} object that
     *         bounds the current path.
     */
    public Rectangle getBounds() {
        return gp.getBounds();
    }

    /**
     * Returns the bounding box of the path.
     *
     * @return a {@link Rectangle2D} object that
     *         bounds the current path.
     */
    public Rectangle2D getBounds2D() {
        return gp.getBounds2D();
    }

    /**
     * Returns a <code>PathIterator</code> object that iterates along the
     * boundary of this <code>Shape</code> and provides access to the
     * geometry of the outline of this <code>Shape</code>.
     * The iterator for this class is not multi-threaded safe,
     * which means that this <code>GeneralPath</code> class does not
     * guarantee that modifications to the geometry of this
     * <code>GeneralPath</code> object do not affect any iterations of
     * that geometry that are already in process.
     *
     * @param at an <code>AffineTransform</code>
     * @return a new <code>PathIterator</code> that iterates along the
     *         boundary of this <code>Shape</code> and provides access to the
     *         geometry of this <code>Shape</code>'s outline
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return gp.getPathIterator(at);
    }

    /**
     * Returns a <code>PathIterator</code> object that iterates along the
     * boundary of the flattened <code>Shape</code> and provides access to the
     * geometry of the outline of the <code>Shape</code>.
     * The iterator for this class is not multi-threaded safe,
     * which means that this <code>GeneralPath</code> class does not
     * guarantee that modifications to the geometry of this
     * <code>GeneralPath</code> object do not affect any iterations of
     * that geometry that are already in process.
     *
     * @param at       an <code>AffineTransform</code>
     * @param flatness the maximum distance that the line segments used to
     *                 approximate the curved segments are allowed to deviate
     *                 from any point on the original curve
     * @return a new <code>PathIterator</code> that iterates along the flattened
     *         <code>Shape</code> boundary.
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return gp.getPathIterator(at, flatness);
    }

    /**
     * Tests if the interior of this <code>Shape</code> intersects the
     * interior of a specified <code>Rectangle2D</code>.
     *
     * @param r the specified <code>Rectangle2D</code>
     * @return <code>true</code> if this <code>Shape</code> and the interior
     *         of the specified <code>Rectangle2D</code> intersect each
     *         other; <code>false</code> otherwise.
     */
    public boolean intersects(Rectangle2D r) {
        return gp.intersects(r);
    }

    /**
     * Tests if the interior of this <code>Shape</code> intersects the
     * interior of a specified set of rectangular coordinates.
     *
     * @param x,&nbsp;y the specified coordinates
     * @param w         the width of the specified rectangular coordinates
     * @param h         the height of the specified rectangular coordinates
     * @return <code>true</code> if this <code>Shape</code> and the
     *         interior of the specified set of rectangular coordinates intersect
     *         each other; <code>false</code> otherwise.
     */
    public boolean intersects(double x, double y, double w, double h) {
        return gp.intersects(x, y, w, h);
    }


}
