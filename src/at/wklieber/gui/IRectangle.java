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
import java.util.Iterator;
import java.util.Vector;

public class IRectangle
        extends IComponent {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IRectangle.class.getName());

    private java.util.List dotList = null;

    /**
     * use this just for getting an instance. Object is not ready to use
     */
    public IRectangle() {

    }

    public IRectangle(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                      Color drawColor1, Color backgroundColor1, boolean resizeable1,
                      boolean isMoveable1, boolean selected1, boolean doDnd1) {
        init(drawPanel1, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, "");
    }

    protected void init(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                        Color drawColor1, Color backgroundColor1, boolean resizeable1,
                        boolean isMoveable1, boolean selected1, boolean doDnd1, String name1) {

        super.init(drawPanel1, null, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, name1);

        componentMinSize = IDot.DEFAULT_DOT_SIZE * 4;

        //if (isResizeable) {
        checkResizeDots(); // display or not display
        //}



        revalidate();
        repaint();
    }

    private void checkResizeDots() {
        //cat.fine("dotlist null: " + (dotList == null) + ", isResizeable: " + isResizeable);
        if (dotList == null) {

            dotList = new Vector();

            /*Color contrastColor = java2dTools.getContrastColor(drawColor);
            if (contrastColor.equals(backgroundColor)) contrastColor = Color.GRAY;
            */
            //cat.fine("Rectangle Width: " + boundary.getWidth() + ", Heigh: " + boundary.getHeight());

            addResizeElement(IDot.DOT_UP_LEFT);
            addResizeElement(IDot.DOT_UP);
            addResizeElement(IDot.DOT_UP_RIGHT);
            addResizeElement(IDot.DOT_RIGHT);
            addResizeElement(IDot.DOT_DOWN_RIGHT);
            addResizeElement(IDot.DOT_DOWN);
            addResizeElement(IDot.DOT_DOWN_LEFT);
            addResizeElement(IDot.DOT_LEFT);
        }

        Iterator it = dotList.iterator();
        while (it.hasNext()) {
            IDot dot = (IDot) it.next();

            //if (isResizeable) {
            dot.setDoShow(isResizeable);
        }
    }

    private void addResizeElement(int dotPos1) {
        Color contrastColor = java2dTools.getContrastColor(drawColor);
        IDot dot = new IDot(drawPanel, this, contrastColor, backgroundColor, dotPos1);
        addOtherComponent(dot);
        revalidate();
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


    protected void addOtherComponent(IComponent component1) {
        super.addOtherComponent(component1);

        dotList.add(component1);

    }


    /*private void removeResizeDots() {
       // exit if they do not exist
       if (dotList == null) {
          return;
       }

       Iterator it = dotList.iterator();
       while (it.hasNext()) {
          Component comp = (Component) it.next();
          this.remove(comp);
          comp.invalidate();
       }

       dotList = null;
    }*/

    public void setResizeable(boolean resizeable) {
        super.setResizeable(resizeable);
        checkResizeDots();
        /*if (isResizeable) {
           addResizeDots();
           cat.fine("resizeDots added");
        } else {
           removeResizeDots();
        }*/
    }

    /**
     * a dot has changed -now a shape resize is necessary
     */
    protected void updateParent(IComponent component1) {
        IDot dot1 = (IDot) component1;
        int dotPos = dot1.getDotPosInParent();

        Point pos = dot1.getComponentPoint();
        int posX = (int) pos.getX();
        int posY = (int) pos.getY();

        //cat.fine("update: " + posX + ", " + posY);

        int sf_width = (int) (posX - boundary.getX());
        int sf_height = (int) (posY - boundary.getY());
        int ef_width = (int) (boundary.getX() - posX + boundary.getWidth());
        int ef_height = (int) (boundary.getY() - posY + boundary.getHeight());

        // limit size according to minsize
        if (sf_width < componentMinSize) sf_width = componentMinSize;
        if (sf_height < componentMinSize) sf_height = componentMinSize;
        if (ef_width < componentMinSize) {
            ef_width = componentMinSize;
            posX = boundary.x + boundary.width - componentMinSize;
        }
        if (ef_height < componentMinSize) {
            ef_height = componentMinSize;
            posY = boundary.y + boundary.height - componentMinSize;
        }


        switch (dotPos) {
            case IDot.DOT_UP_LEFT:
                {
                    boundary.setBounds(posX, posY, ef_width, ef_height);
                    break;
                }
            case IDot.DOT_UP:
                {
                    boundary.setBounds(boundary.x, posY, boundary.width, ef_height);
                    break;
                }
            case IDot.DOT_UP_RIGHT:
                {
                    boundary.setBounds(boundary.x, posY, sf_width, ef_height);
                    break;
                }
            case IDot.DOT_RIGHT:
                {
                    boundary.setBounds(boundary.x, boundary.y, sf_width, boundary.height);
                    break;
                }
            case IDot.DOT_DOWN_RIGHT:
                {
                    boundary.setSize(sf_width, sf_height);
                    break;
                }
            case IDot.DOT_DOWN:
                {
                    boundary.setBounds(boundary.x, boundary.y, boundary.width, sf_height);
                    break;
                }
            case IDot.DOT_DOWN_LEFT:
                {
                    boundary.setBounds(posX, boundary.y, ef_width, sf_height);
                    break;
                }
            case IDot.DOT_LEFT:
                {
                    boundary.setBounds(posX, boundary.y, ef_width, boundary.height);
                    break;
                }
        } // end switch

        repaint();
    }

    // by default paint a rectangle
    protected void drawComponent(Graphics2D g2) {
        //Graphics2D g2 = (Graphics2D) g;

        g2.setPaint(drawColor);

        Rectangle thisRect = this.getBounds();
        g2.draw3DRect((int) boundary.getX(), (int) boundary.getY(), (int) boundary.getWidth() - 1, (int) boundary.getHeight() - 1, true);


        //super.paintComponent(g); // draw extra visual and so on
        //validate();
    } // end method paint

    // use this method is for drawing the component when it is selected
    protected void drawSelected(Graphics2D g2) {
        g2.setPaint(Color.RED);
        //g2.setStroke(Stroke.);
        g2.draw3DRect(boundary.x, boundary.y, boundary.width - 1, boundary.height - 1, true);
    }

    public String toString() {
        return "nothing to report";
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

} // end class IRectangle

