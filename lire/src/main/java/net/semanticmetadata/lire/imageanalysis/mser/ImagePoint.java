package net.semanticmetadata.lire.imageanalysis.mser;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 28.06.2010
 * Time: 23:24:16
 */
public class ImagePoint
{
    int idx;
    int x;
    int y;

    public ImagePoint(int idx, int imageWidth)
    {
        this.idx = idx;
        this.x = ImageMask.getX(idx, imageWidth);
        this.y = ImageMask.getY(idx, imageWidth);
    }

    public ImagePoint(int idx, int x, int y)
    {
        this.idx = idx;
        this.x = x;
        this.y = y;
    }

    public int getIndex()
    {
        return idx;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    @Override
    public String toString()
    {
        return x + "," + y;
    }
}
