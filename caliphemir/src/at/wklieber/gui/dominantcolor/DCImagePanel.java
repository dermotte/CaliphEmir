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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.gui.dominantcolor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class DCImagePanel extends JPanel implements MouseListener {
    private BufferedImage img;
    private static int offset = 30;

    private Point start, end, pointA, pointB;
    private boolean clicked = false;

    public DCImagePanel(BufferedImage image) {
        img = image;
        this.setPreferredSize(new Dimension(img.getWidth() + offset, img.getHeight() + offset));
        this.setSize(img.getWidth() + offset, img.getHeight() + offset);
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Find dominant colors in this image "));

        this.addMouseListener(this);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                end = e.getPoint();
                repaint();
            }
        });

        pointA = new Point();
        pointB = new Point();
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(img, null, offset / 2, offset / 2 + 5);
        g2.setColor(Color.green);
        if (start != null && end != null) {
            int sx, sy, ex, ey;
            sx = start.x;
            if (sx < offset / 2) sx = offset / 2;
            if (sx > img.getWidth() + offset / 2) sx = img.getWidth() + offset / 2;
            ex = end.x;
            if (ex < offset / 2) ex = offset / 2;
            if (ex > img.getWidth() + offset / 2) ex = img.getWidth() + offset / 2;
            sy = start.y;
            if (sy < offset / 2 + 5) sy = offset / 2 + 5;
            if (sy > (img.getHeight() + offset / 2 + 5)) sy = img.getHeight() + offset / 2 + 5;
            ey = end.y;
            if (ey < offset / 2 + 5) ey = offset / 2 + 5;
            if (ey > (img.getHeight() + offset / 2 + 5)) ey = img.getHeight() + offset / 2 + 5;

            if (start.x > end.x && start.y > end.y) {
                int tx, ty;
                tx = ex;
                ex = sx;
                sx = tx;
                ty = sy;
                sy = ey;
                ey = ty;
            } else if (start.x > end.x && start.y < end.y) {
                int tmp = sx;
                sx = ex;
                ex = tmp;
            } else if (start.x < end.x && start.y > end.y) {
                int tmp = sy;
                sy = ey;
                ey = tmp;
            }

            g2.setColor(Color.green);
            g2.drawRect(sx, sy, ex - sx, ey - sy);
            g2.setColor(Color.red);
            g2.drawRect(sx - 1, sy - 1, ex - sx + 2, ey - sy + 2);

            pointA.setLocation(sx, sy);
            pointB.setLocation(ex, ey);
        }

    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        start = e.getPoint();
        clicked = true;
        // System.out.println("Mouse clicked ...");
    }

    public void mouseReleased(MouseEvent e) {
        end = e.getPoint();
        clicked = false;
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void clearRegion() {
        start = null;
        end = null;
        repaint();
    }

    public void leftHalf() {
        start = new Point(offset / 2, offset / 2 + 5);  // links oben
        end = new Point(img.getWidth() / 2 + offset / 2, img.getHeight() + offset / 2 + 5); // mitte unten
        repaint();
    }

    public void rightHalf() {
        start = new Point(img.getWidth() / 2 + offset / 2, offset / 2 + 5);   // mitte oben
        end = new Point(img.getWidth() + offset / 2, img.getHeight() + offset / 2 + 5);  // rechts unten
        repaint();
    }

    public void upperHalf() {
        start = new Point(offset / 2, offset / 2 + 5); // links oben
        end = new Point(img.getWidth() + offset / 2, img.getHeight() / 2 + offset / 2 + 5); // mitte rechts
        repaint();
    }

    public void bottomHalf() {
        start = new Point(offset / 2, img.getHeight() / 2 + offset / 2 + 5); // mitte links
        end = new Point(img.getWidth() + offset / 2, img.getHeight() + offset / 2 + 5); // rechts unten
        repaint();
    }

    public void leftThird() {
        start = new Point(offset / 2, offset / 2 + 5);
        end = new Point(img.getWidth() / 3 + offset / 2, img.getHeight() + offset / 2 + 5);
        repaint();
    }

    public void middleThird() {
        start = new Point(img.getWidth() / 3 + offset / 2, offset / 2 + 5);
        end = new Point(2 * img.getWidth() / 3 + offset / 2, img.getHeight() + offset / 2 + 5);
        repaint();
    }

    public void rightThird() {
        start = new Point(2 * img.getWidth() / 3 + offset / 2, offset / 2 + 5);
        end = new Point(img.getWidth() + offset / 2, img.getHeight() + offset / 2 + 5);
        repaint();
    }

    public void upperThird() {
        start = new Point(offset / 2, offset / 2 + 5);
        end = new Point(img.getWidth() + offset / 2, img.getHeight() / 3 + offset / 2 + 5);
        repaint();
    }

    public void centerThird() {
        start = new Point(offset / 2, img.getHeight() / 3 + offset / 2 + 5);
        end = new Point(img.getWidth() + offset / 2, 2 * img.getHeight() / 3 + offset / 2 + 5);
        repaint();
    }

    public void bottomThird() {
        start = new Point(offset / 2, 2 * img.getHeight() / 3 + offset / 2 + 5);
        end = new Point(img.getWidth() + offset / 2, img.getHeight() + offset / 2 + 5);
        repaint();
    }

    public void centerBig() {
        start = new Point(img.getWidth() / 4 + offset / 2, img.getHeight() / 4 + offset / 2 + 5);
        end = new Point(3 * img.getWidth() / 4 + offset / 2, 3 * img.getHeight() / 4 + offset / 2 + 5);
        repaint();
    }

    public void centerSmall() {
        start = new Point(img.getWidth() / 3 + offset / 2, img.getHeight() / 3 + offset / 2 + 5);
        end = new Point(2 * img.getWidth() / 3 + offset / 2, 2 * img.getHeight() / 3 + offset / 2 + 5);
        repaint();
    }

    public void setRect(Point leftUpperCorner, Point rightBottomCorner) {
        start.setLocation(leftUpperCorner.x + offset / 2, leftUpperCorner.y + offset / 2 + 5);
        end.setLocation(rightBottomCorner.x + offset / 2, rightBottomCorner.y + offset / 2 + 5);
        repaint();
    }

    public Point getPointA() {
        Point returnP = null;
        if (start != null)
            returnP = new Point(pointA.x - offset / 2, pointA.y - offset / 2 - 5);
        return returnP;
    }

    public Point getPointB() {
        Point returnP = null;
        if (start != null)
            returnP = new Point(pointB.x - offset / 2, pointB.y - offset / 2 - 5);
        return returnP;
    }
}
