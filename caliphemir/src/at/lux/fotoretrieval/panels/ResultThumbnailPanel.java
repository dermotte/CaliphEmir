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
package at.lux.fotoretrieval.panels;

import at.lux.components.ViewImageDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.net.URI;

/**
 * ResultThumbnailPanel
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ResultThumbnailPanel extends JPanel {
    private BufferedImage image;
    private final int FIXED_THUMB_EDGE_LENGTH = 120;
    private Dimension size = new Dimension(FIXED_THUMB_EDGE_LENGTH, FIXED_THUMB_EDGE_LENGTH);
    private String fileUri;
    private final Color bgColor = super.getBackground();

/*
    public static final float[] BLUR3x3 =
            {
            0.1f, 0.1f, 0.1f,
            0.1f, 0.2f, 0.1f,
            0.1f, 0.1f, 0.1f
            };
*/

    public static final float[] BLUR3x3 =
            {
                    1f / 9f, 1f / 9f, 1f / 9f,
                    1f / 9f, 1f / 9f, 1f / 9f,
                    1f / 9f, 1f / 9f, 1f / 9f
            };

    private static float kernelElement = 1f / 16f;
    public static final float[] BLUR4x4 =
            {
                    kernelElement, kernelElement, kernelElement, kernelElement,
                    kernelElement, kernelElement, kernelElement, kernelElement,
                    kernelElement, kernelElement, kernelElement, kernelElement,
                    kernelElement, kernelElement, kernelElement, kernelElement
            };

    private static final int MAX_THUMB_EDGE_LENGTH = 100;

    public ResultThumbnailPanel(BufferedImage image, String filePath) {
        this.fileUri = filePath;
        this.setPreferredSize(size);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        if (image.getHeight(null) == image.getWidth(null) && (image.getWidth(null) == FIXED_THUMB_EDGE_LENGTH)) {
            this.image = new BufferedImage(FIXED_THUMB_EDGE_LENGTH, FIXED_THUMB_EDGE_LENGTH, BufferedImage.TYPE_INT_RGB);
            Graphics g2 = this.image.getGraphics();
            g2.drawImage(image, 0, 0, null);
        } else {
            createThumbnail(image);
        }
        // apply a view dialog on click :)
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                BufferedImage imgToShow = null;
                try {
                    if (fileUri.startsWith("file:") || fileUri.startsWith("http:")) {
//                        URI tmpFileUri = URI.create(fileUri);
//                        imgToShow = ImageIO.read(new File(tmpFileUri));
                        imgToShow = ImageIO.read(new File(fileUri.substring("file:/".length())));
                    } else {
                        imgToShow = ImageIO.read(new File(fileUri));
                    }
                } catch (Exception ex) {
                    System.err.println("Error reading " + fileUri);
                    ex.printStackTrace();
                }
                if (imgToShow != null) {
                    ViewImageDialog vid = new ViewImageDialog(imgToShow);
                    vid.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Image for preview not found.");
                }
            }
        });
    }

    public ResultThumbnailPanel(String fileUri) {
        this.image = null;
        this.fileUri = fileUri;
//        size = new Dimension(120, 120);
        this.setPreferredSize(size);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                loadImage();
            }
        });
    }

    private void loadImage() {
        try {
            if (!(image != null)) {
                BufferedImage img = ImageIO.read(new URI(fileUri).toURL());
                createThumbnail(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        repaint();
    }

    private void createThumbnail(BufferedImage img) {
        int x, y, max;
        double factor;
        x = img.getWidth(null);
        y = img.getHeight(null);
        if (x > y) {
            max = x;
            factor = ((double) MAX_THUMB_EDGE_LENGTH) / ((double) x);
        } else {
            max = y;
            factor = ((double) MAX_THUMB_EDGE_LENGTH) / ((double) y);
        }
        x = (int) Math.floor((double) x * factor);
        y = (int) Math.floor((double) y * factor);
        this.image = new BufferedImage(FIXED_THUMB_EDGE_LENGTH, FIXED_THUMB_EDGE_LENGTH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) this.image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setBackground(bgColor);
        g2.clearRect(0, 0, FIXED_THUMB_EDGE_LENGTH, FIXED_THUMB_EDGE_LENGTH);
        g2.setColor(Color.black);
        g2.fillRect((MAX_THUMB_EDGE_LENGTH - x) / 2 + 7, (MAX_THUMB_EDGE_LENGTH - y) / 2 + 7, x, y);
        this.image = blurImage(this.image, new BufferedImage(FIXED_THUMB_EDGE_LENGTH, FIXED_THUMB_EDGE_LENGTH, BufferedImage.TYPE_INT_RGB));
        g2 = (Graphics2D) this.image.getGraphics();
        g2.drawImage(img, (MAX_THUMB_EDGE_LENGTH - x) / 2 + 5, (MAX_THUMB_EDGE_LENGTH - y) / 2 + 5, x, y, null);
        g2.setColor(Color.white);
        g2.drawRect((MAX_THUMB_EDGE_LENGTH - x) / 2 + 5, (MAX_THUMB_EDGE_LENGTH - y) / 2 + 5, x, y);
    }

    public void paint(Graphics g) {
//        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.black);
//        g2.clearRect(0, 0, 110, 110);
        if (image != null) {

            g2.drawImage(image, 5, 5, 110, 110, null);
        } else {
            g2.drawRect(5, 5, 110, 110);
            g2.drawLine(5, 5, 115, 115);
            g2.drawLine(5, 115, 115, 5);
        }
    }

    private BufferedImage blurImage(BufferedImage src, BufferedImage dest) {
        Kernel kernel = new Kernel(4, 4, BLUR4x4);
        ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return cop.filter(src, dest);
    }
}
