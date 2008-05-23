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
package at.lux.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.text.DecimalFormat;

import javax.swing.JPanel;

/**
 * Allows the preview of an image
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class ImageThumbPanel extends JPanel {
    private static int EMPTY_BORDER = 10;
    private Image img = null;
    private BufferedImage orig;
    private int imgWidth, imgHeight;
    private DecimalFormat df;
    private double factor;
    private boolean rescale;
    private BufferedImage toPaint;
    private int x;
    private int y;
    private boolean wait = false;
    private int bitsPerPixel;
    private static final String NO_IMAGE_SELECTED_MSG = "Preview: No image selected.";
    Color bgColor = super.getBackground();

    private static float kernelElement = 1f / 16f;
    public static final float[] BLUR4x4 =
            {
                    kernelElement, kernelElement, kernelElement, kernelElement,
                    kernelElement, kernelElement, kernelElement, kernelElement,
                    kernelElement, kernelElement, kernelElement, kernelElement,
                    kernelElement, kernelElement, kernelElement, kernelElement
            };
    private ViewImageDialog viewImageDialog = null;


    public ImageThumbPanel() {
        super();
        this.setToolTipText("Click left mouse button to show picture in full size.");
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    showImage();
                } else if (event.getButton() == MouseEvent.BUTTON3) {
                    rescale = true;
                    repaint();
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                rescale = true;
                repaint();
            }
        });
        df = (DecimalFormat) DecimalFormat.getInstance();
        df.setMaximumFractionDigits(1);
        rescale = true;
        bitsPerPixel = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getBitDepth();
        this.setMinimumSize(new Dimension(120, 90));
    }


    public void setImage(BufferedImage img) {
        orig = img;
        int bits = img.getColorModel().getPixelSize();
        this.img = img;
        if (bits != bitsPerPixel) {
//            debug("setting colormode from " + bits + " to " + bitsPerPixel);
            BufferedImage tmp = this.getGraphicsConfiguration().createCompatibleImage(img.getWidth(), img.getHeight());
            Graphics gTemp = tmp.getGraphics();
//            debug("drawing new image");
            gTemp.drawImage(img, 0, 0, null);
            img = tmp;
//            debug("finished new image");
        }
        imgWidth = this.img.getWidth(this);
        imgHeight = this.img.getHeight(this);
        rescale = true;
        repaint();
    }


    public void paintComponent(Graphics graphics) {
        // super.paint(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setBackground(bgColor);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setFont(g2.getFont().deriveFont(10f));
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());

        if (img != null & !wait) {
            if (rescale) {
                factor = Math.max((double) imgWidth / ((double) (this.getWidth() - EMPTY_BORDER)),
                        (double) imgHeight / ((double) (this.getHeight() - EMPTY_BORDER)));
                x = (int) (imgWidth / factor);
                y = (int) (imgHeight / factor);

                toPaint = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2img = (Graphics2D) toPaint.getGraphics();
                g2img.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2img.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2img.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2img.drawImage(img, 0, 0, x, y, null);
                rescale = false;
            }
            g2.drawImage(toPaint, (getWidth() - toPaint.getWidth()) / 2, (getHeight() - toPaint.getHeight()) / 2, toPaint.getWidth(), toPaint.getHeight(), null);
        } else {
            int stringWidth = g2.getFontMetrics().stringWidth(NO_IMAGE_SELECTED_MSG);
            g2.drawString(NO_IMAGE_SELECTED_MSG, (getWidth() - stringWidth) / 2, (getHeight() / 2 + g2.getFont().getSize() / 2));
        }
    }

    private void showImage() {
        if (img != null) {
            if (viewImageDialog == null || !viewImageDialog.isVisible()) {
                viewImageDialog = new ViewImageDialog(orig);
                viewImageDialog.setVisible(true);
            }
        }
    }

    public void redrawImage() {
        rescale = true;
        repaint();
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }

    /**
     * returns the full size image, which is currently previewed.
     *
     * @return the currently previewed image.
     */
    public BufferedImage getImage() {
        return orig;
    }

    private BufferedImage blurImage(BufferedImage src, BufferedImage dest) {
        Kernel kernel = new Kernel(4, 4, BLUR4x4);
        ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return cop.filter(src, dest);
    }

}
