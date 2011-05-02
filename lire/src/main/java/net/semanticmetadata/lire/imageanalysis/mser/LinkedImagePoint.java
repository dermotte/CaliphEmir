package net.semanticmetadata.lire.imageanalysis.mser;

/**
 * ImagePoint (linked List)
 *
 * User: Shotty
 * Date: 28.06.2010
 * Time: 23:25:19
 */
public class LinkedImagePoint
{
    LinkedImagePoint prev;
    LinkedImagePoint next;
    ImagePoint point;

    public LinkedImagePoint(ImagePoint point)
    {
        this.point = point;
    }

    public LinkedImagePoint getPrev()
    {
        return prev;
    }

    public LinkedImagePoint getNext()
    {
        return next;
    }

    public ImagePoint getPoint()
    {
        return point;
    }

    public void setPrev(LinkedImagePoint prev)
    {
        this.prev = prev;
    }

    public void setNext(LinkedImagePoint next)
    {
        this.next = next;
    }
}
