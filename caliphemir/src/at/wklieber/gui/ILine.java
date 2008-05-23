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

import at.wklieber.Settings;
import at.wklieber.tools.Console;



import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * connects two IComponents (Images).
 * Display "and", "or" and "and not": the text changes on doubleclick.
 * Is used as group operator
 */
public class ILine
        extends IComponent {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(ILine.class.getName());
    private static Console console = Console.getReference();

    public static final String TYPE_AND = "AND";
    public static final String TYPE_OR = "OR";
    //public static final String TYPE_AND_NOT = "AND NOT";

    // draw a line from parentComponent01 to parentComponent02
    private IComponent parentComponent01 = null;
    private IComponent parentComponent02 = null;

    public ILine(JPanel drawPanel1, IComponent component01, IComponent component02) {
        init(drawPanel1, component01, component02, Color.RED, Color.WHITE,
                true, true, true, false, TYPE_AND);
    }

    private void init(JPanel drawPanel1, IComponent component01, IComponent component02,
                      Color drawColor1, Color backgroundColor1, boolean resizeable1,
                      boolean isMoveable1, boolean selected1, boolean doDnd1,
                      String name1) {
        super.init(drawPanel1, null, 0, 0, 0, 0, drawColor1, backgroundColor1, resizeable1, isMoveable1,
                selected1, doDnd1, name1);
        parentComponent01 = component01;
        parentComponent02 = component02;
    }

    public String toString() {
        return "nothing to report";
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }


    protected void drawComponent(Graphics2D g2) {
        drawComponent(g2, false);
    }

    // this method is for visualizing when the component is under the mouse-pointer
    protected void drawMouseOver(Graphics2D g2) {
        if (isMouseOverComponent) {
            drawComponent(g2, true);
            //setBorder(BorderFactory.createLineBorder(Color.black));
            Color borderColor;
            borderColor = java2dTools.getContrastColor(drawColor);
            //borderColor = Color.RED;
            g2.setPaint(borderColor);
            //cat.fine("Draw brand new visual effect in " + borderColor.toString());
            g2.draw3DRect((int) boundary.getX(), (int) boundary.getY(), (int) boundary.getWidth() - 1, (int) boundary.getHeight() - 1, false);
            g2.draw3DRect((int) boundary.getX() + 1, (int) boundary.getY() + 1, (int) boundary.getWidth() - 3, (int) boundary.getHeight() - 3, false);
            g2.draw3DRect((int) boundary.getX() + 2, (int) boundary.getY() + 2, (int) boundary.getWidth() - 5, (int) boundary.getHeight() - 5, false);
        }
    }


    // use it for noraml draw and for mouse over
    protected void drawComponent(Graphics2D g2, boolean drawBold1) {


        //------------------ get the line data
        // get middle points of the rectangles
        Rectangle rect01 = parentComponent01.getComponentBounds();
        int x1 = (int) (rect01.getX() + (rect01.getWidth() / 2));
        int y1 = (int) (rect01.getY() + (rect01.getHeight() / 2));

        Rectangle rect02 = parentComponent02.getComponentBounds();
        int x2 = (int) (rect02.getX() + (rect02.getWidth() / 2));
        int y2 = (int) (rect02.getY() + (rect02.getHeight() / 2));

        // get start and end point to draw the line (outside the rectangels)
        Point startPoint = java2dTools.getRectangleLineCut(rect01, new Point(x2, y2));
        Point endPoint = java2dTools.getRectangleLineCut(rect02, new Point(x1, y1));
        //---------------------------------

        // special behaviour if mouse is over the component
        if (drawBold1) {
            Stroke oldStroke = g2.getStroke();
            int width = 4;
            g2.setStroke(new BasicStroke(width));
            g2.setColor(this.drawColor);
            g2.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            g2.setStroke(oldStroke);
        } else {
            g2.setColor(this.drawColor);
            g2.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }

        // draw the component name in the middle of the line

        //int middleX = (int) (x1 + ((x2 - x1) / 2));
        //int middleY = (int) (y1 + ((y2 - y1) / 2));

        int middleX = (int) (startPoint.getX() + ((endPoint.getX() - startPoint.getX()) / 2));
        int middleY = (int) (startPoint.getY() + ((endPoint.getY() - startPoint.getY()) / 2));

        String label = this.getName();
        int width = g2.getFontMetrics().stringWidth(label);
        int height = g2.getFontMetrics().getFont().getSize();

        // draw rectangle
        int borderSize = 5;
        x1 = (middleX - (width / 2)) - borderSize;
        y1 = (middleY - (height / 2)) - borderSize;

        int w1 = width + (2 * borderSize);
        int h1 = height + (2 * borderSize);

        g2.setColor(Color.WHITE);
        g2.fill3DRect(x1, y1, w1, h1, true);

        g2.setColor(Color.BLUE);
        g2.draw3DRect(x1, y1, w1, h1, true);
        this.setComponentBounds(x1, y1, w1, h1);

        // draw text
        x1 = middleX - (width / 2);
        y1 = middleY + (height / 2);
        g2.setColor(drawColor);
        g2.drawString(label, x1, y1);

        if (drawBold1) {
            Color borderColor;
            borderColor = java2dTools.getContrastColor(drawColor);
            g2.setPaint(borderColor);
            g2.draw3DRect((int) boundary.getX(), (int) boundary.getY(), (int) boundary.getWidth() - 1, (int) boundary.getHeight() - 1, false);
            g2.draw3DRect((int) boundary.getX() + 1, (int) boundary.getY() + 1, (int) boundary.getWidth() - 3, (int) boundary.getHeight() - 3, false);
            g2.draw3DRect((int) boundary.getX() + 2, (int) boundary.getY() + 2, (int) boundary.getWidth() - 5, (int) boundary.getHeight() - 5, false);
        }


    }

    //--------------------- Mouse Events ----------------------------------------
    // ovveridden from icomponent
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() >= 1)) { // left click
            // rotate the name
            if (this.getName().equalsIgnoreCase(TYPE_AND))
                this.setName(TYPE_OR);
            else
                this.setName(TYPE_AND);

            repaint();

        } else {
            super.mouseClicked(e);
        }
    }

    public String getOperatorName() {
        String returnValue = "";
        if (this.getName().equalsIgnoreCase(TYPE_AND))
            returnValue = Settings.OPERATOR.AND.name();
        else
            returnValue = Settings.OPERATOR.OR.name();
        return returnValue;
    }

    /**
         * if false the entry "delete" is disabled in the popupmenu
         *
         * @param isRemovable
         */
    public void setRemoveAble(boolean isRemovable) {
        JPopupMenu pop = menuTools.getPopupMenu();
        Component deleteEntry = pop.getComponent(0);
        deleteEntry.setEnabled(isRemovable);
    }

    public IComponent getParentComponent01() {
        return parentComponent01;
    }

    public void setParentComponent01(IComponent parentComponent01) {
        this.parentComponent01 = parentComponent01;
    }

    public IComponent getParentComponent02() {
        return parentComponent02;
    }

    public void setParentComponent02(IComponent parentComponent02) {
        this.parentComponent02 = parentComponent02;
    }

} // end class ILine

