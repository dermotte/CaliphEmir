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
package at.wklieber.mpeg7;

import at.wklieber.tools.Mpeg7DateFormat;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;


public class Mpeg7ConversionTools {
    static Logger cat = Logger.getLogger(Mpeg7ConversionTools.class.getName());
    private static Mpeg7ConversionTools java2dTools = null;

    public static Mpeg7ConversionTools getReference() {
        if (java2dTools == null) {
            java2dTools = new Mpeg7ConversionTools();
        }

        return java2dTools;
    }


    private Mpeg7ConversionTools() {
    } // end constructor


    /**
     * converts Rectangle to "x y width heigh"
     */
    public String box2String(Rectangle rect1) {
        String returnValue = "";

        if (rect1 == null) {
            return returnValue;
        }

        returnValue = rect1.x + " " + rect1.y + " " + rect1.width + " " + rect1.height;

        return returnValue;
    }

    /**
     * converts Rectangle to "x y width heigh"
     */
    public Rectangle string2Rectangle(String box1, Rectangle defaultValue1) {
        Rectangle returnValue = defaultValue1;

        if (box1 == null) {
            return returnValue;
        }

        try {
            int x = 0;
            int y = 0;
            int w = 0;
            int h = 0;

            String box = box1.trim();
            StringTokenizer tokens = new StringTokenizer(box, " ", false);
            if (tokens.hasMoreTokens()) {
                x = Integer.parseInt(tokens.nextToken());
            }

            if (tokens.hasMoreTokens()) {
                y = Integer.parseInt(tokens.nextToken());
            }

            if (tokens.hasMoreTokens()) {
                w = Integer.parseInt(tokens.nextToken());
            }

            if (tokens.hasMoreTokens()) {
                h = Integer.parseInt(tokens.nextToken());
            }

            //logger.fine("set to: " + x + ", " + y + ", " + w + ", " + h);
            returnValue = new Rectangle(x, y, w, h);
        } catch (Exception e) {
            e.printStackTrace();
            cat.severe(e.toString());
        }

        //logger.fine("Return: " + returnValue.toString());
        return returnValue;
    }

    /**
     * converts a color to a mpeg7 rgb-color string -value
     * ouput are normailzed values
     */
    public String color2String(Color color1, String defaultValue1) {
        String returnValue = defaultValue1;

        if (color1 == null) {
            return returnValue;
        }

        try {
            /*float[] data = null;
            data = color1.getColorComponents(data);

            for(int i = 0; i < data.length; i++) {
               logger.fine("" + i + ": " + data[i]);
            }
            returnValue = data[0] + " " + data[1] + " " + data[2];
            //System.exit(1); */

            int r = color1.getRed();
            int g = color1.getGreen();
            int b = color1.getBlue();
            returnValue = r + " " + g + " " + b;

        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * converts a mpeg7 rgb-color string -value to a java-Color
     * The colo-values must be in rgb-space in the range [0, 255].
     */
    public Color string2Color(String colorString1, Color defaultValue1) {
        Color returnValue = defaultValue1;

        if (colorString1 == null) {
            return returnValue;
        }

        try {
            int r = returnValue.getRed();
            int g = returnValue.getGreen();
            int b = returnValue.getBlue();

            StringTokenizer tokens = new StringTokenizer(colorString1.trim(), " ", false);
            if (tokens.hasMoreTokens()) {
                r = Integer.parseInt(tokens.nextToken());
            }

            if (tokens.hasMoreTokens()) {
                g = Integer.parseInt(tokens.nextToken());
            }

            if (tokens.hasMoreTokens()) {
                b = Integer.parseInt(tokens.nextToken());
            }

            returnValue = new Color((int) (r), (int) (g), (int) (b));
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * convert {"100 12", "0 0"} to {Point(100, 12), Point(0,0)}
     */
    public List stringArray2PointList(String[] shapePoints1) {
        List returnValue = new Vector();

        for (int i = 0; i < shapePoints1.length; i++) {
            String shape = shapePoints1[i].trim();
            //logger.fine("Point: " + shape);

            int index = shape.indexOf(" ");

            if (index == -1) {
                cat.severe("Point invalid: \"" + shape + "\". should be something like \"10 20\"");
            }

            Point p = new Point(Integer.parseInt(shape.substring(0, index)),
                    Integer.parseInt(shape.substring(index + 1)));

            //logger.fine("Point: " + shape + ", " + p.toString());
            returnValue.add(p);

        }

        return returnValue;
    }

    /**
     * converts "1 2 3 4" to Point(1,2), Point(3,4)
     */
    public List string2Pointlist(String pointString1) {
        List returnValue = new Vector();

        if (pointString1 == null) {
            return returnValue;
        }

        try {
            StringTokenizer tokens = new StringTokenizer(pointString1.trim(), " ", false);
            while (tokens.hasMoreTokens()) {
                int x = 0;
                int y = 0;

                if (tokens.hasMoreTokens()) {
                    x = Integer.parseInt(tokens.nextToken());
                } else
                    break;

                if (tokens.hasMoreTokens()) {
                    y = Integer.parseInt(tokens.nextToken());
                } else
                    break;

                returnValue.add(new Point(x, y));
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }


    public String pointList2String(List pointList1) {
        String returnValue = "";

        if (pointList1 == null) {
            return returnValue;
        }

        try {
            StringBuffer strB = new StringBuffer();
            for (Iterator it = pointList1.iterator(); it.hasNext();) {
                Point p = (Point) it.next();
                if (strB.length() > 0) {
                    strB.append(" ");
                }
                strB.append(p.x + " " + p.y);
            }
            returnValue = strB.toString();
        } catch (Exception e) {
            cat.severe(e.toString());
        }
        return returnValue;
    }

    // todo: support if just year is given.
    public Date timePointToDate(String dateString) {
        Date returnValue = null;

        try {
            returnValue = Mpeg7DateFormat.format(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    // todo: support if just year is given.
    public String dateTotimePoint(Date date) {
        String returnValue = Mpeg7DateFormat.date2Timepoint(date);
        return returnValue;
    }
} // end class
