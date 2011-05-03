package net.semanticmetadata.lire.imageanalysis;

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mathias Lux, mathias@juggle.at
 * Date: 04.10.2010
 * Time: 13:21:55
 */
public class SurfMserTest extends TestCase {

    public void testExtract() throws IOException {
        Surf sm = new Surf(ImageIO.read(new FileInputStream("wang-1000/0.jpg")));
//        Surf sm = new SurfMser(ImageIO.read(new FileInputStream("wang-1000/0.jpg")));
        List<SURFInterestPoint> pts = sm.getFreeOrientedInterestPoints();
        for (Iterator<SURFInterestPoint> surfInterestPointIterator = pts.iterator(); surfInterestPointIterator.hasNext();) {
            SURFInterestPoint pt = surfInterestPointIterator.next();
            System.out.println(pt.getX() + ", " + pt.getY());
        }
    }
}
