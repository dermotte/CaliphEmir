package at.lux.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of Caliph & Emir
 * Date: 02.02.2006
 * Time: 23:23:33
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ColorLayoutTest extends TestCase {
    public void testStringRepresentation() throws IOException {
        String name = "./testdata/P1040588.JPG";
        File file = new File(name);
        BufferedImage img = ImageIO.read(new FileInputStream(file));
        ColorLayoutImpl sc = new ColorLayoutImpl(img);
        String s = sc.getStringRepresentation();
        System.out.println(s);
        ColorLayoutImpl sc2 = new ColorLayoutImpl(s);
        float distance = sc.getDistance(sc2);
        System.out.println("sc2 = " + sc2.getStringRepresentation());
        System.out.println("distance = " + distance);
        assertTrue(distance == 0);

    }

}
