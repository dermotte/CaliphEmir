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

import at.wklieber.gui.data.IComponentData;



import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class IShape
        extends IComponent
        implements DropTargetListener {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IShape.class.getName());


    private boolean doBuildShape = false;
    //private GeneralPath path = null;
    private java.util.List dotList = null;    // list of IDot elements
    private Point mousePos = null;

    private IRectangle rectangle = null;

    private boolean drawDots = true; // define, wheter to draw or not draw the IDot's
    private Color shapeColor = null;
    private BufferedImage image = null; // image to use when extracting the dominant color
    private Rectangle imageSize = null; // image size on the screen (may be streched)
    private boolean isTransparent = false; // if transparent, just the shape is drawn
    private boolean acceptDnd = true;
    private DropTarget dropTarget = null;

    /**
     * use this just for getting an instance. Object is not ready to use
     */
    public IShape() {

    }

    // use for building a new shape
    public IShape(JPanel drawPanel1, Color drawColor1, Color backgroundColor1,
                  BufferedImage image1, Rectangle imageSize1) {
        init(drawPanel1, 0, 0, 0, 0, drawColor1, backgroundColor1, null, image1, imageSize1,
                false, false, true, false, true);
    }

    // use for restore from a pointlist (uses setComponentData)
    // use in Objectpalette
    public IShape(JPanel drawPanel1, IComponentData data1) {
        init(drawPanel1, 0, 0, 0, 0, Color.BLACK, Color.GRAY, null, null, null,
                false, false, true, true, false);
        setComponentData(data1);
    }


    protected void init(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                        Color drawColor1, Color backgroundColor1, Color shapeColor1,
                        BufferedImage image1, Rectangle imageSize1, boolean resizeable1,
                        boolean isMoveable1, boolean selected1, boolean doDnd1,
                        boolean doBuildShape1) {

        super.init(drawPanel1, null, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, "");

        setDoDrawBorder(true);
        doBuildShape = doBuildShape1;
        shapeColor = shapeColor1;
        image = image1;
        imageSize = imageSize1;

        rectangle = new IRectangle(drawPanel1, posX1, posY1, width1, heigh1,
                        drawColor1, backgroundColor1, resizeable1,
                        isMoveable1, selected1, doDnd1);

        if (!doBuildShape) {
            rectangle.setVisible(false);
        }

        //path = new GeneralPath();
        dotList = new Vector();

        //this.setBounds(drawPanel.getBounds());
        this.setComponentBounds(drawPanel.getBounds());

        //cat.fine(this.getBounds().toString());
        //cat.fine(this.getComponentBounds().toString());

        //--------- dnd trop stuff
        dropTarget = new DropTarget(this, (int) DnDConstants.ACTION_COPY,
                        (DropTargetListener) this, true);
        dropTarget.setActive(true);
        this.setDropTarget(dropTarget);


        validate();
        repaint();
    }

    // makes a deep clone of this IComponent
    // implementing classes should override this
    public Object clone() {
        IComponent returnValue = null;
        //cat.fine("make a IShape clone");
        IComponentData data = this.getComponentData();
        data.setBoundary(new Rectangle(0, 0, 0, 0));  // no size, so the surrounding border of dots is used
        returnValue = new IShape(drawPanel, data);

        return returnValue;
    }

    public Color getShapeColor() {
        return shapeColor;
    }

    public void setShapeColor(Color shapeColor) {
        this.shapeColor = shapeColor;
    }

    public boolean isTransparent() {
        return isTransparent;
    }

    public void setTransparent(boolean transparent) {
        isTransparent = transparent;
    }

    public boolean isAcceptDnd() {
        return acceptDnd;
    }

    public void setAcceptDnd(boolean acceptDnd) {
        this.acceptDnd = acceptDnd;
    }

    /**
     * store persistent componenet data in the external data
     */
    public IComponentData getComponentData() {
        IComponentData returnValue = super.getComponentData();
        returnValue.setFillColor(getShapeColor());
        returnValue.setImage(image);
        returnValue.setImageSize(imageSize);

        java.util.List pointList = new Vector(dotList.size());
        if (dotList != null) {
            for (Iterator it = dotList.iterator(); it.hasNext();) {
                IDot dot = (IDot) it.next();
                cat.fine("write dot: " + dot.toString());
                pointList.add(dot.getComponentPoint());
            }
        }

        returnValue.setDotList(pointList);

        return returnValue;
    }

    /**
     * configure the component from the external data model
     */
    public void setComponentData(IComponentData data1) {
        endDoBuildShape();

        super.setComponentData(data1);

        if (data1.getBoundary() == null) {
            boundary = new Rectangle(0, 0, 0, 0);
        }

        if (dotList != null) {
            while (!dotList.isEmpty()) {
                IDot dot = (IDot) dotList.remove(0);
                this.remove(dot);
            }
        } else {
            dotList = new Vector();
        }

        for (Iterator it = data1.getDotList().iterator(); it.hasNext();) {
            Point p = (Point) it.next();

            IDot dot = new IDot(drawPanel, this, p.x, p.y,
                            drawColor, backgroundColor);
            this.addNewDot(dot);
            //cat.fine("ADD DOT: " + p.x + ", " + p.y + ", " + toString());
        }

        setShapeColor(data1.getFillColor());
        image = data1.getImage();
        imageSize = data1.getImageSize();

        // fit dots to a the border, set in data1 if avalable
        // if boundary == 0, then the dots are unchanged and the border is set around them
        if (boundary.width != 0 && boundary.height != 0) {
            resizeShape();
        }

        boundary = calculateBorder();
        repaint();
    }

    public boolean isDrawDots() {
        return drawDots;
    }

    public void setDrawDots(boolean drawDots1) {
        if (drawDots == drawDots1)
            return;

        for (Iterator it = dotList.iterator(); it.hasNext();) {
            IDot dot = (IDot) it.next();
            dot.setDoShow(drawDots1);
        }

        drawDots = drawDots1;
    }

    public void setDrawPanel(JPanel drawPanel) {
        super.setDrawPanel(drawPanel);
        if (dotList != null) {
            Iterator it = dotList.iterator();
            while (it.hasNext()) {
                IDot dot = (IDot) it.next();

                dot.setDrawPanel(drawPanel);
            }
        }
        //cat.fine("drawPanel set to " + getBounds().toString());
    }

    protected void addNewDot(IDot component1) {
        //super.addOtherComponent(component1);

        this.add(component1);
        component1.addMouseListener(component1);
        component1.addMouseMotionListener(component1);
        this.revalidate();

        //cat.fine("dot added");


        dotList.add(component1);
        component1.setId(dotList.size());
    }


    /**
     * a dot has changed -now a shape resize is necessary
     */
    protected void updateParent(IComponent component1) {
        if (!isResizeable()) {
            return;
        }

        IDot dot1 = (IDot) component1;

        boundary = calculateBorder();
        repaint();
    }

    // by default paint a rectangle
    protected void drawComponent(Graphics2D g2) {
        //Graphics2D g2 = (Graphics2D) g;

        // The IDots draw themself

        g2.setPaint(drawColor);
        //cat.fine("draw lines: " + dotList.size());
        Iterator it = dotList.iterator();
        Point pStart = null;
        if (it.hasNext()) {
            IDot dot = (IDot) it.next();
            pStart = dot.getComponentPoint();
        }

        // draw connecting lines
        Point p1 = pStart;
        while (it.hasNext()) {
            IDot dot = (IDot) it.next();
            Point p2 = dot.getComponentPoint();
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            p1 = p2;
        }

        // draw the last dot depending in the mode. Close shape or draw to mousepointer
        if (doBuildShape) {
            if (mousePos != null) {
                if (p1 != null) {
                    // draw a line from the last dot to the currend mouse position
                    g2.setPaint(backgroundColor);
                    g2.drawLine(p1.x, p1.y, mousePos.x, mousePos.y);
                }
                // draw a dot to indicate the user that something can be done
                int size = IDot.DEFAULT_DOT_SIZE;
                g2.setPaint(drawColor);
                g2.fill3DRect(mousePos.x - size / 2, mousePos.y - size / 2, size, size, true);
            }
        } else {
            // close shape
            if (p1 != null) {
                g2.drawLine(pStart.x, pStart.y, p1.x, p1.y);
            }

            if (!isTransparent) {
                // draw shapeColor
                if (shapeColor != null) {
                    Shape shape = java2dTools.pointListToShape(dotListToPointList());
                    g2.setPaint(shapeColor);
                    g2.fill(shape);
                }
            }

            // draw border;
            if (doDrawBorder) {
                g2.setPaint(Color.RED);
                g2.draw3DRect(boundary.x, boundary.y, boundary.width, boundary.height, true);
            }
        } // end if doBuildShape

        //super.paintComponent(g); // draw extra visual and so on
    } // end method paint


    // override the orginal one. the dots have to be set too
    public void setComponentBounds(int x, int y, int width, int heigh) {
        super.setComponentBounds(x, y, width, heigh);
        resizeShape();
    }

    public void setComponentBounds(Rectangle rect1) {
        super.setComponentBounds(rect1);
        resizeShape();
    }

    // override the orginal one. the dots have to be set too
    public void setComponentLocation(Point point1) {
        super.setComponentLocation(point1);
        resizeShape();
    }

    private Rectangle calculateBorder() {
        Point left = new Point(0, 0);
        Point right = new Point(0, 0);
        Point up = new Point(0, 0);
        Point down = new Point(0, 0);

        boolean isFirst = true;
        List pointList = new Vector();
        for (Iterator it = dotList.iterator(); it.hasNext();) {
            IDot dot = (IDot) it.next();
            Point p = dot.getComponentPoint();
            pointList.add(p);
        }

        int size = IDot.DEFAULT_DOT_SIZE;
        Rectangle returnValue;
        returnValue = (Rectangle) java2dTools.calculateBorder(pointList, size, new Rectangle(0, 0, 0, 0));

        return returnValue;
    }

    // fit the the dots to the border whithout changing the aspect ratio
    private void resizeShape() {
        Rectangle dotBorder = calculateBorder();
        Rectangle compBorder = this.getComponentBounds();

        Rectangle newBorder;
        newBorder = (Rectangle) java2dTools.fitToWindow(compBorder, dotBorder);

        cat.fine("dotBorder: " + dotBorder);
        cat.fine("compBorder: " + compBorder);
        cat.fine("newBorder: " + newBorder);

        double aspectRatio = newBorder.getWidth() / newBorder.getHeight();
        double scaleFactor = newBorder.width / dotBorder.getWidth();

        for (Iterator it = dotList.iterator(); it.hasNext();) {
            IDot dot = (IDot) it.next();
            Point p = dot.getComponentPoint();

            int offsetX = p.x - dotBorder.x;
            int offsetY = p.y - dotBorder.y;

            offsetX *= scaleFactor;
            offsetY *= (scaleFactor * aspectRatio);

            int newX = newBorder.x + offsetX;
            int newY = newBorder.y + offsetY;
            dot.setComponentLocation(new Point(newX, newY));

            //cat.fine("Border: " + newBorder.toString() + ", dot: " + newX + ", " + newY);

        } // end for

    }

    /**
     * add some special menu entries
     * *7
     */
    protected void setPopupMenuEntries() {
        super.setPopupMenuEntries();

        menuTools.setParentClass(this);
        //menuTools.addPopupMenuEntry("&Extract Metadata", "Use this image to extract data", "dominant_color.gif", "actionColorAndShape");
        menuTools.addPopupMenuEntry("&Toggle transparency", "Switch to fill the shape or not", "", "actionToggleTransparency");
    }


    //--------------------- Mouse Events ----------------------------------------
    // set a new dot
    public void mouseClicked(MouseEvent e) {
        //cat.fine("mouseClicked: " + e.getX() + ", " + e.getY());
        if (!doBuildShape) {
            super.mouseClicked(e);
        } else {

            if (SwingUtilities.isLeftMouseButton(e)) {
                IDot dot = new IDot(drawPanel, this, e.getX(), e.getY(),
                                drawColor, backgroundColor);

                //cat.fine("set a new dot at: " + e.getPoint());
                this.addNewDot(dot);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                /*if (path.getCurrentPoint() != null) { // close path if there are at least one point available
                   path.closePath();*/
                endDoBuildShape();
            }
            repaint();
        } // end if doBuildShape


    } // end method

    /**
     * invoked when the shape is build ready.
     * use "null" if shape should be transparent
     */
    public void endDoBuildShape() {
        if (!doBuildShape) {
            return;
        }

        doBuildShape = false;
        setMoveable(true);
        setResizeable(true);
        boundary = calculateBorder();

        // set the shape color automatically
        if (image != null) {
            java.util.List pointList = dotListToPointList();

            Rectangle orginalBorder = imageSize;
            Rectangle newBorder = new Rectangle(0, 0, image.getWidth(), image.getHeight());
            java.util.List fitPointList = java2dTools.fitPointToOtherBorder(pointList, orginalBorder, newBorder);
            /*cat.fine("old: " + orginalBorder.toString() + ", new: " + newBorder.toString());
            cat.fine("old Points: " + pointList.toString());
            cat.fine("new Points: " + fitPointList.toString());*/

            Color color = java2dTools.getDominantColor(image, fitPointList);
            setShapeColor(color);
        }
    }

    // convert the IDot() list to a list of Point()
    private java.util.List dotListToPointList() {
        java.util.List pointList = new Vector(dotList.size());
        for (ListIterator dotIterator = dotList.listIterator(); dotIterator.hasNext();) {
            IDot dot = (IDot) dotIterator.next();
            pointList.add(dot.getComponentPoint());
        }
        return pointList;
    }

    public void mouseMoved(MouseEvent e) {
        //cat.fine("mouseDragged: " + e.getX() + ", " + e.getY());
        if (!doBuildShape) {
            super.mouseMoved(e);
        } else {
            mousePos = e.getPoint();
            repaint();
        }
    }

    public void mouseDragged(MouseEvent e) {
        //cat.fine("mouse dragged");
        Rectangle oldRect = new Rectangle(this.boundary);
        super.mouseDragged(e);

        //cat.fine("check rectangle: " + oldRect.x + ", " + this.boundary.x);
        // move the complete shape to a new positions
        if ((oldRect.x != this.boundary.x) || (oldRect.y != this.boundary.y)) {
            int offsetX = this.boundary.x - oldRect.x;
            int offsetY = this.boundary.y - oldRect.y;

            Iterator it = dotList.iterator();
            while (it.hasNext()) {
                IDot dot = (IDot) it.next();
                Rectangle oldDot = dot.getComponentBounds();
                dot.setComponentBounds(oldDot.x + offsetX, oldDot.y + offsetY, oldDot.width, oldDot.height);
            }

            //cat.fine("Border moved: " + offsetX + ", " + offsetY);
            this.repaint();
        }

    }

    public void actionToggleTransparency(ActionEvent e) {
        isTransparent = !isTransparent;
        repaint();
    }

    //--------------- Drop stuff from Dnd ------------------------------
    public void dragEnter(DropTargetDragEvent event) {
    }

    public void dragOver(DropTargetDragEvent event) {
    }

    public void dropActionChanged(DropTargetDragEvent event) {
    }

    public void dragExit(DropTargetEvent event) {
    }


    public void drop(DropTargetDropEvent dtde) {
        try {
            if (!acceptDnd) {
                dtde.rejectDrop();
                return;
            }

            //cat.fine("dnd dropped, included Flavors:");
            Transferable t = dtde.getTransferable();
            DataFlavor[] flavorList = dtde.getCurrentDataFlavors();

            IComponent component = null; //this object will receive the dropped data

            //------------- ACCEPT own IComponent Data ----------------------------
            if (t.isDataFlavorSupported(IComponentTransferable.localIComponentFlavor)) {
                //cat.info("IMAGE is from Objectpalette");
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                component = (IComponent) t.getTransferData(IComponentTransferable.localIComponentFlavor);
                dtde.getDropTargetContext().dropComplete(true);

                shapeColor = component.getDrawColor();
                isTransparent = false;
                repaint();
            } else {
                cat.fine("Flavor rejected");
                dtde.rejectDrop();
            }

            dtde.getDropTargetContext().dropComplete(true);
        } catch (Exception e) {
            cat.severe(e.toString());
            e.printStackTrace();
        }
    }


    public String toString() {
        String returnValue = "Size: " + boundary.toString();
        returnValue += "\nDots: " + dotList.size();
        return returnValue;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

} // end class IRectangle

