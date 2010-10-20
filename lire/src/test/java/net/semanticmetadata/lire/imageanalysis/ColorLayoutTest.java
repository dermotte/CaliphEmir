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
        BufferedImage bi = ImageIO.read(new File("c:/temp/bug2.jpg"));
        ColorLayout cl = new ColorLayout();
        cl.extract(bi);
    }

}
