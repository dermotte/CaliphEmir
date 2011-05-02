package net.semanticmetadata.lire.imageanalysis.mser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The GrowthHistory holds the information for ONE Extremal Region!!
 *
 * User: Shotty
 * Date: 28.06.2010
 * Time: 11:04:17
 */
public class MSERGrowthHistory implements Comparable
{
    int index;
    int size;
    int maxGreyValue;
    LinkedImagePoint head;
    ImagePoint[] points = null;
    ImagePoint[] borderPoints = null;

    MSERGrowthHistory parent;


    public MSERGrowthHistory(int size, int value, LinkedImagePoint head)
    {
        this.size = size;
        this.maxGreyValue = value;

        this.head = head;
        this.parent = this;
    }

    public int getSize()
    {
        return size;
    }

    public ImagePoint[] getPoints()
    {
        if (points == null)
        {
            points = new ImagePoint[size];
            LinkedImagePoint temp = head;
            for (int i = 0; i < size; i++)
            {
                points[i] = temp.getPoint();
                temp = temp.getNext();

            }
        }
        return points;
    }

    /**
     * Calculate the border from all the points in the shape
     *
     * @return only the points on the border of the shape
     */
    public ImagePoint[] getBorderPoints(int width, int height)
    {
        if (borderPoints == null)
        {
            // point with the smallest index is topLeft
            ImagePoint topLeft = null;

            HashMap<String, ImagePoint> imagePoints = new HashMap<String, ImagePoint>();

            // fill the hash map
            for (ImagePoint p : getPoints())
            {
                if (topLeft == null || topLeft.getIndex() > p.getIndex())
                {
                    topLeft = p;
                }
                imagePoints.put(p.getX() + "_" + p.getY(), p);
            }

            List<ImagePoint> boundary = new ArrayList<ImagePoint>();

            // begin with topLeft, which is obviously part of the boundary
            boundary.add(topLeft);

            //examine neighbours in a counterclockwise direction
            BoundaryPixel8Edge currentPoint = new BoundaryPixel8Edge(topLeft, width, height, false, imagePoints);

            BoundaryPixel8Edge neighbor = currentPoint.getNextBoundary();

            // set the stop conditions
            ImagePoint stopConditionCurrentPoint = currentPoint.getPoint();
            ImagePoint stopConditionNeighborPoint = neighbor.getPoint();

            // add the neighbor to the boundary pixels
            boundary.add(imagePoints.get(neighbor.getX() + "_" + neighbor.getY()));
            // where did the edge come from
            neighbor.setCurrentEdge(currentPoint.getNeighbourEdge());
            // neighbor is now current point
            currentPoint = neighbor;
            // get new neighbor
            neighbor = currentPoint.getNextBoundary();

            while (stopConditionCurrentPoint.getIndex() != currentPoint.getIndex() &&
                    stopConditionNeighborPoint.getIndex() != neighbor.getIndex())
            {
                // add the neighbor to the boundary pixels
                boundary.add(imagePoints.get(neighbor.getX() + "_" + neighbor.getY()));

                // where did the edge come from
                neighbor.setCurrentEdge(currentPoint.getNeighbourEdge());
                // neighbor is now current point
                currentPoint = neighbor;
                // get new neighbor
                neighbor = currentPoint.getNextBoundary();
            }

            // -1 because start == last
            borderPoints = new ImagePoint[boundary.size()];
            boundary.toArray(borderPoints);
        }


        return borderPoints;

    }

    public MSERGrowthHistory getParent()
    {
        return parent;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }

    public int compareTo(Object o)
    {
        if (maxGreyValue < ((MSERGrowthHistory) o).maxGreyValue)
        {
            return -1;
        }
        else if (maxGreyValue > ((MSERGrowthHistory) o).maxGreyValue)
        {
            return 1;
        }
        return 0;
    }


    public static void main (String[] args)
    {
        int[] points = new int[] {12,
                21, 22, 23, 25,
                30, 31, 32, 33, 34, 35, 36, 38, 39,
                40, 41, 42, 43, 44, 45, 48,
                50, 51, 53, 54, 55, 56, 58, 59,
                63, 64, 65, 66, 67, 68,
                71, 72, 73, 74, 75, 76,
                80, 81, 82, 83, 84,
                91, 92, 93
        };

        int width = 10;

        LinkedImagePoint head = new LinkedImagePoint(new ImagePoint(points[0], width));
        LinkedImagePoint last = head;
        LinkedImagePoint current;

        for (int i = 1; i < points.length; i++)
        {
            current = new LinkedImagePoint(new ImagePoint(points[i], width));
            last.setNext(current);
            current.setPrev(last);
            last = current;
        }
        MSERGrowthHistory test = new MSERGrowthHistory(49, 36, head);

        ImagePoint[] border = test.getBorderPoints(10,10);

        String borderPoints = "";
        for (ImagePoint p : border)
        {
            borderPoints += " " + p.getIndex();
        }

//        System.out.println(borderPoints);

        // expected border:
        String expectedBorderPoints = " 12 21 30 40 50 51 42 53 63 72 71 80 91 92 93 84 75 76 67 68 59 48 39 38 48 58 67 56 45 36 25 34 23 12";

//        System.out.println(expectedBorderPoints);

//        System.out.println("SAME =" + ((borderPoints.equals(expectedBorderPoints)? "true" : "false")));
    }
}
