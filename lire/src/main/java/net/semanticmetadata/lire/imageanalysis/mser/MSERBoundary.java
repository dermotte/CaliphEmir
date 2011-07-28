/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.imageanalysis.mser;

import ij.ImagePlus;
import ij.gui.NewImage;

import java.awt.*;

/**
 * This class is used to draw the outline of a collection of MSERs on a {@code BufferedImage}.
 *
 * @author Christine Keim
 */
public class MSERBoundary {

    private ImagePlus image = null;
    //	private Color color;
    private Color[] color = new Color[]{
            Color.RED, Color.PINK, Color.ORANGE, Color.GREEN, Color.MAGENTA, Color.CYAN,
            Color.BLUE, Color.YELLOW};
    private int colIndex = 0;
    private static final int WHITE = 255;
    private static final int TEMP_COLOR = 50;

    /**
     * The constructor initializes the object with a {@code BufferedImage}.
     *
     * @param image Image on which the outline is draw.
     */
    public MSERBoundary(ImagePlus image) {
        this.image = image;
    }


    protected static int[] findBorders(ImagePoint[] pixels) {
        int ymin = Integer.MAX_VALUE;
        int ymax = 0;
        int xmin = Integer.MAX_VALUE;
        int xmax = 0;

        for (int i = 0; i < pixels.length; i++) {
            ImagePoint p = pixels[i];
            if (p.getX() > xmax) {
                xmax = p.getX();
            }
            if (p.getX() < xmin) {
                xmin = p.getX();
            }
            if (p.getY() > ymax) {
                ymax = p.getY();
            }
            if (p.getY() < ymin) {
                ymin = p.getY();
            }
            /*
               if( i == 0 ){
                   image.setRGB(p.getX(), p.getY(), 0xFFFF0000); System.out.println("(" + p.getX() + ", " + p.getY() + ")");
                   image.setRGB(p.getX() + 1, p.getY() + 1, 0xFFFF0000);
                   image.setRGB(p.getX() - 1, p.getY() - 1, 0xFFFF0000);
                   image.setRGB(p.getX() + 1, p.getY(), 0xFFFF0000);
                   image.setRGB(p.getX() - 1, p.getY(), 0xFFFF0000);
                   image.setRGB(p.getX(), p.getY() + 1, 0xFFFF0000);
                   image.setRGB(p.getX(), p.getY() - 1, 0xFFFF0000);
                   image.setRGB(p.getX() - 1, p.getY() + 1, 0xFFFF0000);
                   image.setRGB(p.getX() + 1, p.getY() - 1, 0xFFFF0000);
               }
               */
        }

        return new int[]{xmin, xmax, ymin, ymax};

    }

    /**
     * Helper method.
     *
     * @param pixels Set of pixels of which the border line should be drawn.
     */
    private void apply(ImagePoint[] pixels) {

        ImagePoint p0 = null;

        int[] borders = findBorders(pixels);
        int xmin = borders[0];
        int xmax = borders[1];
        int ymin = borders[2];
        int ymax = borders[3];

        ImagePoint[][] reg = new ImagePoint[xmax - xmin + 1][ymax - ymin + 1];
        for (int i = 0; i < pixels.length; i++) {
            reg[pixels[i].getX() - xmin][pixels[i].getY() - ymin] = pixels[i];
        }

        int x = -1, y = 0;
        int dir = 0;
        while (reg[++x][y] == null) {
            ;
        }
        p0 = reg[x][y];

        while (true) {

            int t = (dir + 3) % 4;
            for (int i = 0; i < 4; i++) {
                int tx = x, ty = y;

                switch (t) {
                    case 0: {
                        tx = x + 1;
                        break;
                    }
                    case 1: {
                        ty = y - 1;
                        break;
                    }
                    case 2: {
                        tx = x - 1;
                        break;
                    }
                    case 3: {
                        ty = y + 1;
                        break;
                    }
                }
                if ((tx >= 0) && (ty >= 0) && (tx <= (xmax - xmin)) && (ty <= (ymax - ymin)) && (reg[tx][ty] != null)) {
                    x = tx;
                    y = ty;
                    dir = t;
                    int xCoor = reg[x][y].getX();
                    int yCoor = reg[x][y].getY();
                    image.getProcessor().drawPixel(xCoor, yCoor);
                    break;
                }
                t = (t + 1) % 4;
            }
            if (reg[x][y] == p0) {
                break;
            }

        }
    }

    public void apply(MSERGrowthHistory[] msers) {

        for (int i = 0; i < msers.length; i++) {
            image.setColor(color[colIndex]);
            apply(msers[i].getPoints());
            colIndex = ++colIndex % color.length;
        }

    }

    public int compare(ImagePoint p1, ImagePoint p2) {
        return 0;
    }

    public ImagePlus[] getAreaImages(MSERGrowthHistory[] msers) {
        ImagePlus[] images = new ImagePlus[msers.length];
        for (int i = 0; i < msers.length; i++) {
            images[i] = getAreaImage(msers[i].getPoints());
        }

        return images;
    }

    protected static ImagePlus getAreaImage(ImagePoint[] pixels) {
        int[] borders = findBorders(pixels);

        int xmin = borders[0];
        int xmax = borders[1];
        int ymin = borders[2];
        int ymax = borders[3];


        ImagePlus image = NewImage.createByteImage("AREA", xmax - xmin + 1, ymax - ymin + 1, 1, NewImage.FILL_WHITE);
        image.setColor(Color.BLACK);
        int xCoor;
        int yCoor;
        for (int i = 0; i < pixels.length; i++) {
            xCoor = pixels[i].getX() - xmin;
            yCoor = pixels[i].getY() - ymin;
            image.getProcessor().drawPixel(xCoor, yCoor);
        }

        return image;
    }

}
