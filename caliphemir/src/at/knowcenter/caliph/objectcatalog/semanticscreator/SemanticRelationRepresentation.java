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

package at.knowcenter.caliph.objectcatalog.semanticscreator;

import at.knowcenter.caliph.objectcatalog.graphics.Arrow;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Iterator;
import java.util.Vector;

/**
 * This file is part of Caliph & Emir.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SemanticRelationRepresentation {
    private SemanticObjectRepresentation source, target;
    private String label;
    private Line2D line;
    private boolean highlighted;
    private RoundRectangle2D.Double labelBackGround = null;
    public static final Color COLOR_ARROW_HIGHLIGHT = Color.red;
    public static final Color COLOR_ARROW = new Color(36, 74, 200);

    public SemanticRelationRepresentation(SemanticObjectRepresentation source, SemanticObjectRepresentation target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
        line = new Line2D.Double();
        highlighted = false;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SemanticObjectRepresentation getTarget() {
        return target;
    }

    public void setTarget(SemanticObjectRepresentation target) {
        this.target = target;
    }

    public SemanticObjectRepresentation getSource() {
        return source;
    }

    public void setSource(SemanticObjectRepresentation source) {
        this.source = source;
    }

    public void drawRelation(Graphics2D g2) {
        Vector sPoints = new Vector();
        Vector tPoints = new Vector();
        Point sourcePoint, targetPoint;
        Point sp = source.getP();
        Point tp = target.getP();
        sourcePoint = null;
        targetPoint = null;

        sPoints.add(new Point(sp.x + 2 * SemanticObjectRepresentation.WIDTH / 3, sp.y));
        sPoints.add(new Point(sp.x + SemanticObjectRepresentation.WIDTH / 3, sp.y));
        sPoints.add(new Point(sp.x + 2 * SemanticObjectRepresentation.WIDTH / 3, sp.y + SemanticObjectRepresentation.HEIGHT));
        sPoints.add(new Point(sp.x + SemanticObjectRepresentation.WIDTH / 3, sp.y + SemanticObjectRepresentation.HEIGHT));

        sPoints.add(new Point(sp.x, sp.y + 2 * SemanticObjectRepresentation.HEIGHT / 3));
        sPoints.add(new Point(sp.x, sp.y + SemanticObjectRepresentation.HEIGHT / 3));
        sPoints.add(new Point(sp.x + SemanticObjectRepresentation.WIDTH, sp.y + 2 * SemanticObjectRepresentation.HEIGHT / 3));
        sPoints.add(new Point(sp.x + SemanticObjectRepresentation.WIDTH, sp.y + SemanticObjectRepresentation.HEIGHT / 3));

        sPoints.add(new Point(sp.x, sp.y));
        sPoints.add(new Point(sp.x, sp.y + SemanticObjectRepresentation.HEIGHT));
        sPoints.add(new Point(sp.x + SemanticObjectRepresentation.WIDTH, sp.y));
        sPoints.add(new Point(sp.x + SemanticObjectRepresentation.WIDTH, sp.y + SemanticObjectRepresentation.HEIGHT));

        tPoints.add(new Point(tp.x + 2 * SemanticObjectRepresentation.WIDTH / 3, tp.y));
        tPoints.add(new Point(tp.x + SemanticObjectRepresentation.WIDTH / 3, tp.y));
        tPoints.add(new Point(tp.x + 2 * SemanticObjectRepresentation.WIDTH / 3, tp.y + SemanticObjectRepresentation.HEIGHT));
        tPoints.add(new Point(tp.x + SemanticObjectRepresentation.WIDTH / 3, tp.y + SemanticObjectRepresentation.HEIGHT));

        tPoints.add(new Point(tp.x, tp.y + 2 * SemanticObjectRepresentation.HEIGHT / 3));
        tPoints.add(new Point(tp.x, tp.y + SemanticObjectRepresentation.HEIGHT / 3));
        tPoints.add(new Point(tp.x + SemanticObjectRepresentation.WIDTH, tp.y + 2 * SemanticObjectRepresentation.HEIGHT / 3));
        tPoints.add(new Point(tp.x + SemanticObjectRepresentation.WIDTH, tp.y + SemanticObjectRepresentation.HEIGHT / 3));

        tPoints.add(new Point(tp.x, tp.y));
        tPoints.add(new Point(tp.x, tp.y + SemanticObjectRepresentation.HEIGHT));
        tPoints.add(new Point(tp.x + SemanticObjectRepresentation.WIDTH, tp.y));
        tPoints.add(new Point(tp.x + SemanticObjectRepresentation.WIDTH, tp.y + SemanticObjectRepresentation.HEIGHT));


        double minDistance = -1.0;
        for (Iterator i1 = sPoints.iterator(); i1.hasNext();) {
            Point point = (Point) i1.next();
            for (Iterator it2 = tPoints.iterator(); it2.hasNext();) {
                Point pt = (Point) it2.next();
                if (minDistance > pt.distance(point) || minDistance < 0) {
                    minDistance = pt.distance(point);
                    sourcePoint = point;
                    targetPoint = pt;
                }
            }
        }
        if (highlighted)
            g2.setColor(COLOR_ARROW_HIGHLIGHT);
        else
            g2.setColor(COLOR_ARROW);
        g2.fill(new Arrow(new Line2D.Double((double) sourcePoint.x, (double) sourcePoint.y, (double) targetPoint.x, (double) targetPoint.y), 3.0));
        g2.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y);
        line.setLine((double) sourcePoint.x, (double) sourcePoint.y, (double) targetPoint.x, (double) targetPoint.y);
        g2.fillOval(sourcePoint.x - 3, sourcePoint.y - 3, 6, 6);

        // Arrowheads
        Composite comp = g2.getComposite();
        float alpha = 0.75f;
        int type = AlphaComposite.SRC_OVER;
        AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
        g2.setComposite(composite);

        g2.setColor(Color.white);
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(label, g2);
        int fillRectX = ((sourcePoint.x + targetPoint.x) >> 1) - (((int) bounds.getWidth()) >> 1) - 4;
        int fillRectY = ((sourcePoint.y + targetPoint.y) >> 1) - ((int) bounds.getHeight() + 1);
        int fillRectWidth = (int) bounds.getWidth() + 8;
        int fillRectHeight = (int) bounds.getHeight() + 8;
        labelBackGround = new RoundRectangle2D.Double(fillRectX, fillRectY, fillRectWidth, fillRectHeight, 12.0, 12.0);
        g2.fill(labelBackGround);
//        g2.fillRect(fillRectX, fillRectY, fillRectWidth, fillRectHeight);

        g2.setComposite(comp);

        g2.setColor(Color.black);
        int x = ((sourcePoint.x + targetPoint.x) >> 1) - (g2.getFontMetrics().stringWidth(label) >> 1);
        g2.drawString(label, x, (sourcePoint.y + targetPoint.y) >> 1);
    }

    public boolean contains(Point p) {
        Arrow t = new Arrow(line, 3.0);
        boolean b = t.contains(p);
        if (labelBackGround != null && labelBackGround.contains(p)) {
            b = true;
        }
        if (b) {
            highlighted = true;
        } else {
            highlighted = false;
        }
        return highlighted;
    }
}
