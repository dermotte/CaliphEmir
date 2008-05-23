package at.lux.imaging;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BmpReaderTest extends TestCase {
    File testFile;

    protected void setUp() throws Exception {
        testFile = new File("test.bmp");
        assertTrue(testFile.exists());
    }

    public void testReader() throws IOException {
        BufferedImage image = BmpReader.read(new FileInputStream(testFile));
        ImageIO.write(image, "png", new File("test-out.png"));
    }


}
