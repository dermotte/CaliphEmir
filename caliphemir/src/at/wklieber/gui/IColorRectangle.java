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

public class IColorRectangle
        extends IRectangle {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IColorRectangle.class.getName());
    //private static Console console = Console.getReference();

    private boolean doDisplayPercentage = false; // if true, in the middle of the box the persantage is drawn

    /**
     * use this just for getting an instance. Object is not ready to use
     */
    public IColorRectangle() {

    }

    // for use in drawing resizable rectangles
    public IColorRectangle(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                           Color drawColor1, Color backgroundColor1, boolean selected1) {
        init(drawPanel1, posX1, posY1, width1, heigh1, drawColor1,
                backgroundColor1, true, true, selected1, false, true, "");
    }

    // for use in ComponentPalette when the instanciaton class determines the size
    public IColorRectangle(JPanel drawPanel1,
                           Color drawColor1, Color backgroundColor1, String name1) {
        init(drawPanel1, 0, 0, 0, 0, drawColor1, backgroundColor1,
                false, false, false, true, false, name1);
    }

    // all parameters for the clone method
    public IColorRectangle(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                           Color drawColor1, Color backgroundColor1, boolean resizeable1,
                           boolean isMoveable1, boolean selected1, boolean doDnd1,
                           boolean doDisplayPercentage1, String name1) {
        init(drawPanel1, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, doDisplayPercentage1, name1);
    }

    public boolean isDoDisplayPercentage() {
        return doDisplayPercentage;
    }

    public void setDoDisplayPercentage(boolean doDisplayPercentage) {
        this.doDisplayPercentage = doDisplayPercentage;
    }

    protected void init(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                        Color drawColor1, Color backgroundColor1, boolean resizeable1,
                        boolean isMoveable1, boolean selected1, boolean doDnd1,
                        boolean doDisplayPercentage1, String name1) {

        super.init(drawPanel1, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, name1);

        //setName(name1);
        doDisplayPercentage = doDisplayPercentage1;

        validate();
        repaint();
    }

    public Object clone() {
        IColorRectangle returnValue = null;
        //cat.fine("make a ColorRectangle clone");
        returnValue = new IColorRectangle(drawPanel, (int) boundary.getX(), (int) boundary.getY(),
                        (int) boundary.getWidth(), (int) boundary.getHeight(),
                        drawColor, backgroundColor, isResizeable,
                        isMoveable, isSelected, doDnd, doDisplayPercentage, getName());

        return returnValue;
    }


    protected void drawComponent(Graphics2D g2) {
        //Graphics2D g2 = (Graphics2D) g;
        //g2.transform(t);
        g2.setPaint(drawColor);

        //Rectangle thisRect = this.getBounds(); // the c
        //cat.fine("REPAINT: " + toString());
        g2.fill3DRect((int) boundary.getX(), (int) boundary.getY(), (int) boundary.getWidth() - 1, (int) boundary.getHeight() - 1, true);

        Color contrastColor = java2dTools.getContrastColor(drawColor);
        g2.setColor(contrastColor);

        // calculate and display percentage
        if (doDisplayPercentage) {
            double thisArea = boundary.getWidth() * boundary.getHeight();
            double panelSize = drawPanel.getHeight() * drawPanel.getWidth(); //pad.getDrawAreaSize();

            int thisAreaPercent = 0;
            if (panelSize > 0) {
                thisAreaPercent = (int) ((thisArea / panelSize) * 100);
            }
            String text = thisAreaPercent + "%";

            drawText(g2, text, TYPE_VERT_MIDDLE, TYPE_HORIZ_MIDDLE, true, false);
            //double middleX = boundary.getX() + boundary.getWidth() / 2;
            //double middleY = boundary.getY() + boundary.getHeight() / 2;
            //java2dTools.drawCenteredText(g2, text, (int) middleX, (int) middleY);
        }

        //      super.paintComponent(g); // draw extra visual and so on
    } // end method paint


    /**
     * get all persistent dato of this class
     */
    public IComponentData getComponentData() {
        IComponentData data = new IComponentData(this);
        //data.setComponentName("IColorRectangle");

        data.setBackGround(this.backgroundColor);
        data.setForeGround(this.drawColor);

        data.setBoundary(this.boundary);

        return data;
    }


    /**
     * set all persistent data of this class
     */
    public void setComponentData(IComponentData data1) {
        super.setComponentData(data1);
        drawColor = data1.getForeGround();
        if (drawPanel != null) {
            validate();

            //cat.fine(toString());
            repaint();
        }

    }


    public String toString() {
        return "Color: " + drawColor.toString() + ", pos: " + boundary.toString();
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
} // end class IColorRectangle

