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
package at.lux.imageanalysis;

import junit.framework.TestCase;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
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
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */

/**
 * Date: 03.10.2005
 * Time: 20:37:57
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DominantColorTest extends TestCase {
    private String IMAGE_FILE_01;

    public void testExtraction() throws IOException {
        BufferedImage img = ImageIO.read(new FileInputStream(IMAGE_FILE_01));
        DominantColor dc = new DominantColor(img);
        Element descriptor = dc.getDescriptor();

        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(descriptor, System.out);
    }

    public void testMatching() throws IOException {
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

        String name1 = IMAGE_FILE_01;
        BufferedImage img1 = ImageIO.read(new FileInputStream(name1));
        DominantColor dc1 = new DominantColor(img1);
        System.out.println("dc1 generated ...");
//        out.output(dc1.getDescriptor(), System.out);

        String name2 = "testdata/I-Know 02/iknow_001.JPG";
        BufferedImage img2 = ImageIO.read(new FileInputStream(name2));
        DominantColor dc2 = new DominantColor(img2);
        System.out.println("dc2 generated ...");
//        out.output(dc2.getDescriptor(), System.out);

        String name3 = "testdata/I-Know 02/iknow_002.JPG";
        BufferedImage img3 = ImageIO.read(new FileInputStream(name3));
        DominantColor dc3 = new DominantColor(img3);
        System.out.println("dc3 generated ...");
//        out.output(dc3.getDescriptor(), System.out);

        System.out.println("Matching " + name1 + " to " + name1 + ": " + dc1.getDistance(dc1));
        System.out.println("Matching " + name1 + " to " + name2 + ": " + dc1.getDistance(dc2));
        System.out.println("Matching " + name2 + " to " + name1 + ": " + dc2.getDistance(dc1));
        System.out.println("Matching " + name1 + " to " + name3 + ": " + dc1.getDistance(dc3));
        System.out.println("Matching " + name3 + " to " + name1 + ": " + dc3.getDistance(dc1));
        System.out.println("Matching " + name2 + " to " + name3 + ": " + dc2.getDistance(dc3));
        System.out.println("Matching " + name3 + " to " + name2 + ": " + dc3.getDistance(dc2));
    }

    public void testPixelArrayBasedExtraction() throws IOException {
        String name1 = IMAGE_FILE_01;
        BufferedImage img1 = ImageIO.read(new FileInputStream(name1));
        DominantColor dc1 = new DominantColor(img1);
        System.out.println("dc1 generated ...");

        DominantColor dc2 = new DominantColor(interleave(img1));
        System.out.println("dc2 generated ...");

        System.out.println("Matching descriptors extracted from different routines: " + dc1.getDistance(dc2));
    }

    public void testPerformance() throws IOException {
        String name1 = IMAGE_FILE_01;
        BufferedImage img1 = ImageIO.read(new FileInputStream(name1));

        DominantColorImplementation dc1 = null;
        int numRounds = 5;
        long time = System.currentTimeMillis();
        for (int i = 0; i< numRounds ; i++) {
            dc1 = new DominantColorImplementation();
            dc1.extractDescriptor(img1);
        }
        time = System.currentTimeMillis() - time;
        float timeTaken = (float) time / (float) numRounds;
        System.out.println("Orig: timeTaken per extraction in average on " + numRounds + " rounds = " + timeTaken);
        System.gc();
        FastDominantColorImpl dc2 = null;
        time = System.currentTimeMillis();
        for (int i = 0; i< numRounds ; i++) {
            dc2 = new FastDominantColorImpl();
            dc2.extractDescriptor(img1);
        }
        time = System.currentTimeMillis() - time;
        timeTaken = (float) time / (float) numRounds;
        System.out.println("Fast: timeTaken per extraction in average on " + numRounds + " rounds = " + timeTaken);
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

    protected void setUp() throws Exception {
        super.setUp();
        IMAGE_FILE_01 = "testdata/P1040588.JPG";
    }
}
