package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * User: Mathias Lux, mathias@juggle.at
 * Date: 20.10.2010
 * Time: 16:21:45
 */
public class ColorLayoutTest extends TestCase{
    public void testExtraction() throws IOException {
        BufferedImage bi = ImageIO.read(new File(".\\src\\test\\resources\\images\\img01.JPG"));
        ColorLayout cl = new ColorLayout();
        cl.extract(bi);

        System.out.println(cl.getStringRepresentation());
        ColorLayout cl2 = new ColorLayout();
        cl2.setByteArrayRepresentation(cl.getByteArrayRepresentation());
        System.out.println(cl2.getStringRepresentation());
        System.out.println("Distance: " + cl2.getDistance(cl));
    }

}
