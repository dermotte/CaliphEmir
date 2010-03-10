package net.semanticmetadata.lire.imageanalysis;

import at.lux.imageanalysis.EdgeHistogramImplementation;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
 * (c) 2002-2007 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
public class EdgeHistogramTest extends TestCase {
    public void testExtraction() throws IOException {
        BufferedImage image = ImageIO.read(new File("C:\\Java\\Projects\\Lire\\src\\test\\resources\\images\\Pginas de 060305_b_Pgina_1_Imagem_0004_Pgina_08_Imagem_0002.jpg"));
        EdgeHistogramImplementation eh = new EdgeHistogramImplementation(image);
        System.out.println("eh = " + eh.getStringRepresentation());
    }
}
