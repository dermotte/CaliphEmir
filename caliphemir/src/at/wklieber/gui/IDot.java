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
package at.wklieber.gui;




import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class IDot
        extends IComponent {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IDot.class.getName());
    public static final int DEFAULT_DOT_SIZE = 10;

    //private static Console console = Console.getReference();
    // By default the point position is seen as the center of the box.
    // these constansts can be used to move this position to a border.
    // e.g. MOVE_UP_LEFT means the upper left corner

    public static final int DOT_CENTER = 0;
    public static final int DOT_UP = 1;
    public static final int DOT_DOWN = 2;
    public static final int DOT_LEFT = 3;
    public static final int DOT_RIGHT = 4;
    public static final int DOT_UP_LEFT = 5;
    public static final int DOT_UP_RIGHT = 6;
    public static final int DOT_DOWN_LEFT = 7;
    public static final int DOT_DOWN_RIGHT = 8;
    public static final int DOT_NONE = 9;  // use this do avoid setting the position automatically

    //private int componentSize_ = DEFAULT_DOT_SIZE;   // default size of the dot
    private int dotPosInParent = DOT_CENTER; // define to which positio in the shape the dot corresponds to
    private int dotPos = DOT_CENTER;    // on which border side does this dot lie on on the parent rectangle

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // optional id that can be set from the parent.
    // used in IShape to identify which point has been moved
    private int id = 0;


    /**
     * Constructor for IColorRectangle as parent
     */
    public IDot(JPanel drawPanel1, IComponent parent1,
                Color drawColor1, Color backgroundColor1,
                int dotPosInParent1) {
        init(drawPanel1, parent1, 0, 0, drawColor1, backgroundColor1,
                false, true, false, false, dotPosInParent1, dotPosInParent1);
    }


    /**
     * Constructor for IShape as parent: set the position
     */
    public IDot(JPanel drawPanel1, IComponent parent1, int posX1, int posY1,
                Color drawColor1, Color backgroundColor1) {
        init(drawPanel1, parent1, posX1, posY1, drawColor1, backgroundColor1,
                false, true, false, false, DOT_CENTER, DOT_NONE);
    }


    private void init(JPanel drawPanel1, IComponent parent1, int posX1, int posY1,
                      Color drawColor1, Color backgroundColor1,
                      boolean resizeable1, boolean isMoveable1,
                      boolean selected1, boolean doDnd1,
                      int dotPos1, int dotPosInParent1) {

        dotPos = dotPos1;
        Point pos = correctPointPos(posX1, posY1);
        componentMinSize = DEFAULT_DOT_SIZE;
        super.init(drawPanel1, parent1, (int) pos.getX(), (int) pos.getY(), DEFAULT_DOT_SIZE, DEFAULT_DOT_SIZE,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, "");
        componentMinSize = DEFAULT_DOT_SIZE;

        //parent = parent1;  // DOTO : if parent is null, an exception should
        dotPosInParent = dotPosInParent1;


        //cat.fine("Dot: " + dotPos + ", in Parent: " + dotPosInParent + ", " + posX1 + ", " + posY1);
        // set the boundary relative to the edge
        setBoundaryByParent();

        validate();
        repaint();
    }

    // override
    public void setDoShow(boolean doShow1) {
        super.setDoShow(doShow1);
        if (doShow1) {
            this.boundary.setSize(MIN_SIZE, MIN_SIZE);
        } else {
            this.boundary.setSize(0, 0);
        }
    }

    // get the coordinate of the dot from the parent border
    // if parent is null, nothing is done
    // if dotPosInPartent is one of the recangle postion (DOT_LEFT, ...) the bounds are set
    //    relative to it (used in IRectangle)
    // if dotPosInPartent = DOT_NONE, the origin of the coordinate system is the upper
    // left corner of the parent (0,0)
    private void setBoundaryByParent() {
        Point dot = new Point(0, 0);

        if ((parent == null) || (dotPosInParent == DOT_NONE)) return;


        Rectangle rect = parent.getComponentBounds();

        int x = (int) rect.getX();
        int xMid = (int) (x + rect.getWidth() / 2);
        int xFull = (int) (x + rect.getWidth());
        int y = (int) rect.getY();
        int yMid = (int) (y + rect.getHeight() / 2);
        int yFull = (int) (y + rect.getHeight());


        switch (dotPosInParent) {
            case DOT_CENTER:
                dot.setLocation(xMid, yMid);
                break;
            case DOT_UP_LEFT:
                dot.setLocation(x, y);
                break;
            case DOT_UP:
                dot.setLocation(xMid, y);
                break;
            case DOT_UP_RIGHT:
                dot.setLocation(xFull, y);
                break;
            case DOT_RIGHT:
                dot.setLocation(xFull, yMid);
                break;
            case DOT_DOWN_RIGHT:
                dot.setLocation(xFull, yFull);
                break;
            case DOT_DOWN:
                dot.setLocation(xMid, yFull);
                break;
            case DOT_DOWN_LEFT:
                dot.setLocation(x, yFull);
                break;
            case DOT_LEFT:
                dot.setLocation(x, yMid);
                break;
        } // end switch

        Point correctedPoint = correctPointPos((int) dot.getX(), (int) dot.getY());
        boundary = new Rectangle((int) correctedPoint.getX(), (int) correctedPoint.getY(),
                        DEFAULT_DOT_SIZE, DEFAULT_DOT_SIZE);
        //cat.fine("after corr: " + boundary.toString());
    }

    private Point correctPointPos(int x1, int y1) {

        //cat.fine("IDOT pos x: " + x1 + ", y: " + y1 + ", move: " + dotPosInParent);
        if ((dotPos == DOT_UP) || (dotPos == DOT_DOWN) || (dotPos == DOT_CENTER)) {
            x1 -= DEFAULT_DOT_SIZE / 2;
        } else if ((dotPos == DOT_UP_RIGHT) || (dotPos == DOT_RIGHT) || (dotPos == DOT_DOWN_RIGHT)) {
            x1 -= DEFAULT_DOT_SIZE;
        }

        if ((dotPos == DOT_LEFT) || (dotPos == DOT_CENTER) || (dotPos == DOT_RIGHT)) {
            y1 -= DEFAULT_DOT_SIZE / 2;
        } else if ((dotPos == DOT_DOWN_LEFT) || (dotPos == DOT_DOWN) || (dotPos == DOT_DOWN_RIGHT)) {
            y1 -= DEFAULT_DOT_SIZE;
        }

        //cat.fine("new pos x: " + x1 + ", y: " + y1);

        return new Point(x1, y1);
    }

    private Point unCorrectPointPos(int x1, int y1) {

        //cat.fine("IDOT pos x: " + x1 + ", y: " + y1 + ", move: " + cursorPos);
        if ((dotPos == DOT_UP) || (dotPos == DOT_DOWN) || (dotPos == DOT_CENTER)) {
            x1 += DEFAULT_DOT_SIZE / 2;
        } else if ((dotPos == DOT_UP_RIGHT) || (dotPos == DOT_RIGHT) || (dotPos == DOT_DOWN_RIGHT)) {
            x1 += DEFAULT_DOT_SIZE;
        }

        if ((dotPos == DOT_LEFT) || (dotPos == DOT_CENTER) || (dotPos == DOT_RIGHT)) {
            y1 += DEFAULT_DOT_SIZE / 2;
        } else if ((dotPos == DOT_DOWN_LEFT) || (dotPos == DOT_DOWN) || (dotPos == DOT_DOWN_RIGHT)) {
            y1 += DEFAULT_DOT_SIZE;
        }

        //cat.fine("new pos x: " + x1 + ", y: " + y1);

        return new Point(x1, y1);
    }

    protected void drawComponent(Graphics2D g2) {
        //Graphics2D g2 = (Graphics2D) g;

        g2.setPaint(drawColor);
        setBoundaryByParent();

        Rectangle thisRect;
        thisRect = this.getBounds();
        g2.fill3DRect((int) boundary.getX(), (int) boundary.getY(), (int) boundary.getWidth() - 1, (int) boundary.getHeight() - 1, true);
    } // end method paint

    // this method is for visualizing when the component is under the mouse-pointer
    protected void drawMouseOver(Graphics2D g2) {
        if (isMouseOverComponent) {
            Color borderColor;
            borderColor = java2dTools.getContrastColor(backgroundColor);
            g2.setPaint(borderColor);
            g2.draw3DRect((int) boundary.getX(), (int) boundary.getY(), (int) boundary.getWidth() - 1, (int) boundary.getHeight() - 1, false);
            g2.draw3DRect((int) boundary.getX() - 1, (int) boundary.getY() - 1, (int) boundary.getWidth() + 1, (int) boundary.getHeight() + 1, false);
            //g2.draw3DRect((int) boundary.getX() - 2, (int) boundary.getY() - 2, (int) boundary.getWidth() + 3, (int) boundary.getHeight() + 3, false);
        }
    }

    /*   public int getComponentSize_() {
           return DEFAULT_DOT_SIZE;
       }

       public void setComponentSize_(int componentSize_) {
           this.DEFAULT_DOT_SIZE = componentSize_;
       }*/

    // end building a shape on a right-click
    public void mouseClicked(MouseEvent e) {
        // if a right mouse button in a shape is clicked
        // and shape is in build mode, the shape has to be informed
        if (SwingUtilities.isRightMouseButton(e)) {
            if ((parent != null) && (IShape.class.isInstance(parent))) {
                IShape shape = (IShape) parent;
                shape.endDoBuildShape();
                return;
            }
        }

        super.mouseClicked(e);
    }

    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            // inform the parent of the update
            //cat.fine(" update parent" + this.getComponentBounds().toString());
            if (parent != null) {
                parent.updateParent(this);
            }
        }
    }

    public Point getComponentPoint() {
        return unCorrectPointPos((int) boundary.getX(), (int) boundary.getY());
    }

    public void setComponentPoint(Point point1) {
        Point newPoint = correctPointPos((int) point1.getX(), (int) point1.getY());
        boundary.setBounds((int) newPoint.getX(), (int) newPoint.getY(), DEFAULT_DOT_SIZE, DEFAULT_DOT_SIZE);
    }


    public int getDotPosInParent() {
        return dotPosInParent;
    }

    public String toString() {
        return "Pos: " + boundary.toString();
    }
}
