package net.semanticmetadata.lire.imageanalysis.mser.fourier.utils;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 10.01.11
 * Time: 04:11
 * To change this template use File | Settings | File Templates.
 */
public class PolygonUtils
{
    /** METHODS TO CALCULATE THE AREA AND CENTROID OF A POLYGON
        INSERT THEM INTO THE CORRESPONDING CLASS **/
    public static double signedPolygonArea(Point2D.Double[] polygon)
    {
        int N = polygon.length;
        Polygon P;
        int i,j;
        double area = 0;

        for (i=0;i<N;i++) {
            j = (i + 1) % N;
            area += polygon[i].x * polygon[j].y;
            area -= polygon[i].y * polygon[j].x;
        }
        area /= 2.0;

       return(area);
        //return(area < 0 ? -area : area); for unsigned
    }

    public static double unsignedPolygonArea(Point2D.Double[] polygon)
    {
        double area = signedPolygonArea(polygon);
        return(area < 0 ? -area : area);
    }

/* CENTROID */
    public static Point2D.Double polygonCenterOfMass(Point2D.Double[] polygon)
    {
        int N = polygon.length-1;

        double cx=0,cy=0;
//        double A = signedPolygonArea(polygon);
        Point2D.Double res=new Point2D.Double();
        int i,j;
        double sumDet = 0;


        double factor=0;
        for (i=0;i<N;i++)
        {
            j = i + 1;
            factor=(polygon[i].x*polygon[j].y-polygon[j].x*polygon[i].y);
            cx+=(polygon[i].x+polygon[j].x)*factor;
            cy+=(polygon[i].y+polygon[j].y)*factor;

            sumDet += factor;
        }
        factor = 1/(3*sumDet);

//        A*=6.0;
//        factor=1/A;
        cx*=factor;
        cy*=factor;
        res.x=cx;
        res.y=cy;
        return res;
    }

    public static void applyCoG(Point2D.Double[] points, Point2D.Double cog)
    {
        for (int i = 0; i < points.length; i++)
        {
            points[i].setLocation(points[i].x - cog.x, points[i].y - cog.y);
        }
    }

    public static void main (String[] args)
    {
        Point2D.Double[] test =
        {
            new Point2D.Double(-1, -4),
            new Point2D.Double(-4, 2),
            new Point2D.Double(-1, 5),
            new Point2D.Double(2, 2)
        };

        Point2D cog = polygonCenterOfMass(test);
        System.out.println(cog.getX() + "/" + cog.getY());

        Point.Double[] test2 =
        {
            new Point2D.Double(1, 2),
            new Point2D.Double(4, 2),
            new Point2D.Double(5, 3),
            new Point2D.Double(2, 3)
        };

        cog = polygonCenterOfMass(test2);
        System.out.println(cog.getX() + "/" + cog.getY());

    }
}

