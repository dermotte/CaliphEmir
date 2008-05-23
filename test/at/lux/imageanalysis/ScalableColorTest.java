package at.lux.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
/*
 * This file is part of Caliph & Emir.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */

/**
 * This file is part of Caliph & Emir
 * Date: 01.11.2005
 * Time: 21:48:16
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ScalableColorTest extends TestCase {
    public void testSCStringRepresentation() throws IOException {
        String name = "C:\\Java\\Projects\\Caliph\\biglie.jpg";
        File file = new File(name);
        BufferedImage img = ImageIO.read(new FileInputStream(file));
        ScalableColor sc = new ScalableColor(img, 256, 0);
        String s = sc.getStringRepresentation();
        System.out.println(s);
        ScalableColor sc2 = new ScalableColor(s);
        float distance = sc.getDistance(sc2);
        System.out.println("sc2 = " + sc2.getStringRepresentation());
        System.out.println("distance = " + distance);
        assertTrue(distance == 0);
    }

    public void testEHStringRepresentation() throws IOException {
        String name = "C:/Java/JavaProjects/CaliphEmir/testdata/P1040588.JPG";
        File file = new File(name);
        BufferedImage img = ImageIO.read(new FileInputStream(file));
        EdgeHistogram eh = new EdgeHistogram(img);
        String s = eh.getStringRepresentation();
        System.out.println(s);
        EdgeHistogram eh2 = new EdgeHistogram(s);
        float distance = eh.getDistance(eh2);
        System.out.println("eh2 = " + eh2.getStringRepresentation());
        System.out.println("distance = " + distance);
        assertTrue(distance == 0);
    }

    public void testExtraction() throws FileNotFoundException, IOException {
        String name = "C:/Java/JavaProjects/CaliphEmir/testdata/I-Know 02/iknow_001.JPG";
        File file = new File(name);
        BufferedImage img = ImageIO.read(new FileInputStream(file));
        for (int j = 0; j < 10; j++) {
            new ScalableColor(img);
        }
    }

    public void testPixelArrayBasedExtraction() throws IOException {
        String name1 = "testdata/I-Know 02/iknow_001.JPG";
        BufferedImage img1 = ImageIO.read(new FileInputStream(name1));
        ScalableColor dc1 = new ScalableColor(img1);
        System.out.println("sc1 generated ...");

        ScalableColor dc2 = new ScalableColor(interleave(img1));
        System.out.println("sc2 generated ...");

        System.out.println("Matching descriptors extracted from different routines: " + dc1.getDistance(dc2));
    }


    private int[] interleave(BufferedImage bufferedImage) {
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        int[] pixelarray = new int[3 * height * width];
        int j = 0;
        WritableRaster raster = bufferedImage.getRaster();
        int[] pixel = new int[3];
        for (int i = 0; i < width; i++) { //row
            for (int ii = 0; ii < height; ii++) {//column
                raster.getPixel(i, ii, pixel);
                pixelarray[3 * j] = pixel[0];
                pixelarray[3 * j + 1] = pixel[1];
                pixelarray[3 * j + 2] = pixel[2];
                j++;
            }
        }
        return pixelarray;
    }

    public void testArtificialImage() {
        doWithColor(Color.red);
        doWithColor(Color.blue);
        doWithColor(Color.yellow);
    }

    private void doWithColor(Color color) {
        BufferedImage img = createImage(color);
        // Hand it over to ScalableColor to create a descriptor:
        ScalableColorImpl scd = new ScalableColorImpl(img);
        // create the string representation
        String sc = scd.getStringRepresentation();
        System.out.println("sc = " + sc);
        System.out.println("color = " + color);
    }

    private static BufferedImage createImage(Color color) {
        int imgWidth = 64;
        BufferedImage img = new BufferedImage(imgWidth, imgWidth, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, imgWidth, imgWidth);
        return img;
    }

}
