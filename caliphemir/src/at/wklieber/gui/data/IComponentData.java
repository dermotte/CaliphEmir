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
package at.wklieber.gui.data;

import at.wklieber.gui.IColorRectangle;
import at.wklieber.gui.IComponent;
import at.wklieber.gui.IShape;
import at.wklieber.tools.Console;
import at.wklieber.tools.MiscTools;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * - contains data of a IComponent class or one of its children.
 * - it has the name of the Icomponent class
 * - it can generate a new component out of the data (a kind of clone)
 * - Icomponent use this data for serialization/deserialization
 */
public class IComponentData
        implements DataInterface {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IComponentData.class.getName());
    private static Console console = Console.getReference();


    private IComponentData() {
        init();
    }

    // component is used to get the name of the comonent: rectangle, shape, ...
    public IComponentData(IComponent iComponent) {
        init();
        this.componentName = iComponent.getClass().getName();
    }

    /**
     * returns the name of the component java class name (.IRectangle, ...)
     */
    public String getComponentName() {
        return componentName;
    }


    private String componentName = ""; // the component this data describes, e.g "IColorRectangle"

    // dot-list for shapes
    private java.util.List dotList = null;    // dot list for shape of Element "Point"
    private Rectangle boundary = null;        // the size of the sourounding box

    private Color foreGround = null;
    private Color backGround = null;
    private Color fillColor = null;  // solid color. used in IShape as shapecolor
    private String name = ""; // the name of the component: filename, colorname, ...
    private BufferedImage image = null;
    private Rectangle imageSize = null;

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public Rectangle getImageSize() {
        return imageSize;
    }

    public void setImageSize(Rectangle imageSize) {
        this.imageSize = imageSize;
    }

    public Color getForeGround() {
        return foreGround;
    }

    public void setForeGround(Color foreGround) {
        this.foreGround = foreGround;
    }

    public Color getBackGround() {
        return backGround;
    }

    public void setBackGround(Color backGround) {
        this.backGround = backGround;
    }

    public Rectangle getBoundary() {
        return boundary;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setBoundary(Rectangle boundary) {
        if (boundary == null) {
            this.boundary = new Rectangle(0, 0, 0, 0);
        } else {
            this.boundary = boundary;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // returns the type of data for the internal to mpeg7 converter.
    //This does not define the IComponent this record belongs to
    // means that it is a icomponent dat instead of e.g. a textquery data
    public int getId() {
        return ICOMPONENT_DATA;
    }


    private void init() {
        dotList = new Vector();
    }

    public void setDotList(List dotList) {
        this.dotList = dotList;
    }

    public List getDotList() {
        return dotList;
    }


    /**
     * Creates a new component from the data in this class.
     * Instance of the class is choosen from getComponentName
     * DrawPanel is need, that the component can be drawn. If it is null, setDrawPanel have to be called manually
     */
    public IComponent getIComponent(JPanel drawPanel1) {
        IComponent returnValue = null;
        try {
            if (getComponentName().equalsIgnoreCase(IColorRectangle.class.getName())) {
                Color drawColor = getForeGround();
                Color backgroundColor = getBackGround();

                IColorRectangle rect = new IColorRectangle(drawPanel1, drawColor, backgroundColor, drawColor.toString());
                rect.setDoDnd(false);
                rect.setMoveable(true);
                rect.setResizeable(true);
                rect.setDoDisplayPercentage(true);

                returnValue = rect;
            } else if (getComponentName().equalsIgnoreCase(IShape.class.getName())) {
                Color drawColor = getFillColor();
                Color backgroundColor = getBackGround();

                IShape shape = new IShape(drawPanel1, drawColor, backgroundColor,
                        this.image, this.imageSize);
                shape.setDoDnd(false);
                shape.setMoveable(true);
                shape.setResizeable(true);

                returnValue = shape;
            } else
                returnValue = (IComponent) MiscTools.loadClassByName(getComponentName());
            returnValue.setComponentData(this);
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    public String toString() {
        String returnValue = "Id: \"" + getComponentName() + "\", ";
        returnValue += " Boundary: \"" + getBoundary() + "\", ";
        returnValue += " Foreground: \"" + getForeGround() + "\", ";
        return "";
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
}
