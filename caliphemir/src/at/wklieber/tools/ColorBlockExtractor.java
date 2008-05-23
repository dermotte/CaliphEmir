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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.tools;




import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.WritableRaster;
import java.util.logging.Logger;


public class ColorBlockExtractor {
    static Logger cat = Logger.getLogger(ColorBlockExtractor.class.getName());

    private static int BLOCK_MIN_SIZE = 20;

    private BufferedImage image = null;
    private JDialog imageDialog = null;

    private Color[] rasterColors = null;

    boolean showDialog = false;

    public ColorBlockExtractor(BufferedImage image1, boolean showDialog1) {
        init(image1, showDialog1);
    } // end constructor


    public void init(BufferedImage image1, boolean showDialog1) {
        if (image1 != null) {
            image = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(800, 600, BufferedImage.TYPE_3BYTE_BGR);
        }

        showDialog = showDialog1;

        Graphics2D l_gfx = image.createGraphics();
        //l_gfx.setColor(new Color(1f, 1f, 1f));
        //l_gfx.fillRect(0, 0, image.getWidth(), image.getHeight());

        //l_gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f));
        l_gfx.drawImage(image1, 0, 0, null);

        if (showDialog) {
            imageDialog = new JDialog(new JDialog(), "Image preview", false);
            Container contentPane = imageDialog.getContentPane();
            //contentPane.setLayout(new BorderLayout());
            //contentPane.setSize(image.getWidth(), image.getHeight());
            imageDialog.setSize(image.getWidth(), image.getHeight());

            drawImage();
            //System.out.println("Size: " + x.getSize().toString());
            imageDialog.setVisible(true);
        }

        // -- set the sample colors
        rasterColors = new Color[12];
        rasterColors[0] = Color.BLACK;
        rasterColors[1] = Color.DARK_GRAY;
        rasterColors[2] = Color.LIGHT_GRAY;
        rasterColors[3] = Color.BLUE;
        rasterColors[4] = Color.MAGENTA;
        rasterColors[5] = Color.GREEN;
        rasterColors[6] = Color.CYAN;
        rasterColors[7] = Color.ORANGE;
        rasterColors[8] = Color.PINK;
        rasterColors[9] = Color.RED;
        rasterColors[10] = Color.YELLOW;
        rasterColors[11] = Color.WHITE;
    }

    private void drawImage() {
        imageDialog.getContentPane().removeAll();
        //Java2dTools.getReference().showImage(image);
        JLabel i = new JLabel();
        ImageFilter filter = new ImageFilter();
        Image img;
        img = i.createImage(new FilteredImageSource(image.getSource(), filter));

        JButton x = new JButton("Image");
        ImageIcon icon = new ImageIcon(img);
        x.setIcon(icon);
        x.setSize(image.getWidth(), image.getHeight());
        imageDialog.getContentPane().add(x);
        imageDialog.repaint();
    }

    private void filterClosing() {

        int[][] matrix = {{0, 1, 0},
                          {1, 1, 1},
                          {0, 1, 0}};

        WritableRaster raster = image.getRaster();

        int[] pixels = new int[3];

        System.out.println("start");
        for (int w = 0; w < image.getWidth(); w++) {
            for (int h = 0; h < image.getHeight(); h++) {
                raster.getPixel(w, h, pixels);
                //System.out.println(pixels[0] + "," + pixels[1] + "," + pixels[2]);

                Color matchColor = null;
                int matchDiff = 0;

                //-- apply filter
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; i <= 1; i++) {
                        Color color = rasterColors[i];
                        int diff = Math.abs((color.getRed() - pixels[0])) +
                                Math.abs((color.getGreen() - pixels[1])) +
                                Math.abs((color.getBlue() - pixels[2]));
                        if (i == 0) {
                            matchColor = color;
                            matchDiff = diff;
                        } else if (diff < matchDiff) {
                            matchColor = color;
                            matchDiff = diff;
                        }
                    }
                } // end for i (Color raster)
                pixels[0] = matchColor.getRed();
                pixels[1] = matchColor.getGreen();
                pixels[2] = matchColor.getBlue();
                //System.out.println("[" + w + ", " + h +"]: " + pixels[0] + "," + pixels[1] + "," + pixels[2]);
                raster.setPixel(w, h, pixels);
            } // end for heigh
        } // end for weidth
        System.out.println("done");

    }

    private void quantize() {
        Graphics2D g = (Graphics2D) image.getGraphics();
        //Raster raster = image.getData();
        WritableRaster raster = image.getRaster();
        //g.drawLine();

        int[] pixels = new int[3];

        //System.out.println("start");
        for (int w = 0; w < image.getWidth(); w++) {
            for (int h = 0; h < image.getHeight(); h++) {
                raster.getPixel(w, h, pixels);
                //System.out.println(pixels[0] + "," + pixels[1] + "," + pixels[2]);

                Color matchColor = null;
                int matchDiff = 0;
                for (int i = 0; i < rasterColors.length; i++) {
                    Color color = rasterColors[i];
                    int diff = Math.abs((color.getRed() - pixels[0])) +
                            Math.abs((color.getGreen() - pixels[1])) +
                            Math.abs((color.getBlue() - pixels[2]));
                    if (i == 0) {
                        matchColor = color;
                        matchDiff = diff;
                    } else if (diff < matchDiff) {
                        matchColor = color;
                        matchDiff = diff;
                    }
                } // end for i (Color raster)
                pixels[0] = matchColor.getRed();
                pixels[1] = matchColor.getGreen();
                pixels[2] = matchColor.getBlue();
                //System.out.println("[" + w + ", " + h +"]: " + pixels[0] + "," + pixels[1] + "," + pixels[2]);
                raster.setPixel(w, h, pixels);
            } // end for heigh
        } // end for weidth
        //System.out.println("done");
    }

    public BufferedImage getQuantizedImage() {
        quantize();
        return image;
    }

    public void getDominantColorBlocks() {
        try {
            //Thread.sleep(2000);
            quantize();
            if (showDialog) {
                drawImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


} // end class Java2dTools