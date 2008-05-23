package at.lux.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <p/>
 * Date: 06.02.2006 <br>
 * Time: 14:18:25 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class ColorTest extends TestCase {
    private final String IMAGE_FILE_01 = "testdata/P1040588.JPG";

    public void testColorConversion() {
        Color color = new Color(100, 125, 150);
        int blue = color.getBlue();
        int blue2 = color.getRGB() & 255;

        int green = color.getGreen();
        int green2 = color.getRGB() >> 8 & 255;

        int red = color.getRed();
        int red2 = color.getRGB() >> 16 & 255;

        System.out.println(blue + " - " + blue2);
        System.out.println(green + " - " + green2);
        System.out.println(red + " - " + red2);
    }

    public void testPixelAccess() throws IOException {
        BufferedImage img = ImageIO.read(new FileInputStream(IMAGE_FILE_01));
        int pixel = img.getRGB(0,0);

        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = img.getRGB(0, 0, width, height, new int[width*height], 0, width);

//        assertTrue(pixel == pixels[0]);
        System.out.println("width = " + width);
        System.out.println("height = " + height);
        for (int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                boolean condition = img.getRGB(x, y) == pixels[y * width + x];
                if (!condition) System.out.println("x=" + x + " y=" + y);
                assertTrue(condition);
            }
        }

    }

}
