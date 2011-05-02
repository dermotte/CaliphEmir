package net.semanticmetadata.lire.imageanalysis.mser;

/**
 * Created by IntelliJ IDEA.
 *
 * User: Shotty
 * Date: 28.06.2010
 * Time: 23:53:21
 */
public class BoundaryPixel
{
    public static final int RIGHT_EDGE = 0;
    public static final int BOTTOM_EDGE = 2;
    public static final int LEFT_EDGE = 4;
    public static final int TOP_EDGE = 6;
    public static final int NO_EDGE = 8;

    protected int imageWidth;
    protected int imageHeight;
    protected ImagePoint point;
    protected int nextEdge;

    public BoundaryPixel(ImagePoint point, int imageWidth, int imageHeight)
    {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.point = point;
        this.nextEdge = RIGHT_EDGE;
    }

    public ImagePoint getPoint()
    {
        return point;
    }

    public int getIndex()
    {
        return point.getIndex();
    }

    public int getX()
    {
        return point.getX();
    }

    public int getY()
    {
        return point.getY();
    }

    /**
     * Calculate the next Edge from the current pixel.
     * Starts from RIGHT --> BOTTOM --> LEFT --> TOP Neighbour Pixel
     * returns NULL if all edges where explored-
     *
     * @return an ImagePoint object or null if there is no edge left
     */
    public ImagePoint calcNextEdge()
    {
        ImagePoint nextEdgePoint;
        switch(nextEdge)
        {
            case RIGHT_EDGE:
                // try the get the right edge of the current pixel
                nextEdgePoint = getRightNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = BOTTOM_EDGE;
                    return nextEdgePoint;
                }
            case BOTTOM_EDGE:
                // try the get the bottom edge of the current pixel
                nextEdgePoint = getBottomNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = LEFT_EDGE;
                    return nextEdgePoint;
                }
            case LEFT_EDGE:
                // try to get the left edge of the current pixel
                nextEdgePoint = getLeftNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = TOP_EDGE;
                    return nextEdgePoint;
                }
            case TOP_EDGE:
                // try to get the top edge of the current pixel
                nextEdgePoint = getTopNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = NO_EDGE;
                    return nextEdgePoint;
                }
            default:
                nextEdge = NO_EDGE;
                return null; // all edges done
        }
    }

    /**
     * Explore the next Edge of the Pixel.
     * Returns null if all edges have been explored for this pixel
     *
     * @return the BoundaryPixel of the next Neighbour
     */
    public BoundaryPixel getNextBoundary()
    {
        ImagePoint nextEdge = calcNextEdge();
        if (nextEdge != null)
        {
            return new BoundaryPixel(nextEdge, imageWidth, imageHeight);
        }
        else
        {
            return null;
        }
    }

    protected ImagePoint getTopNeighbor()
    {
        if (getY() == 0)
        {
            return null;
        }
        else
        {
            return new ImagePoint(getIndex() - imageWidth, imageWidth);
        }
    }

    protected ImagePoint getLeftNeighbor()
    {
        if (getX() == 0)
        {
            return null;
        }
        else
        {
            return new ImagePoint(getIndex() -1, imageWidth);
        }
    }

    protected ImagePoint getBottomNeighbor()
    {
        if (getY() == imageHeight -1)
        {
            return null;
        }
        else
        {
            return new ImagePoint(getIndex() + imageWidth, imageWidth);
        }
    }

    protected ImagePoint getRightNeighbor()
    {
        if (getX() == imageWidth - 1)
        {
            return null;
        }
        else
        {
            return new ImagePoint(getIndex() + 1, imageWidth);
        }
    }
}
