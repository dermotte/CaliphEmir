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

import at.wklieber.gui.IColorRectangle;
import at.wklieber.gui.IComponent;
import at.wklieber.gui.dominantcolor.DominantColorFinder;
import at.wklieber.gui.dominantcolor.DominantColorPlugin;
import at.wklieber.gui.dominantcolor.RGBColorPercentagePair;
import at.wklieber.gui.dominantcolor.RGBColorPercentagePairList;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;



import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;


public class Java2dTools {
    static Logger cat = Logger.getLogger(Java2dTools.class.getName());
    private static Java2dTools java2dTools = null;

    public static final String LOOK_CURRENT_OS = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    public static final String LOOK_WINDOWS = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    public static final String LOOK_MOTIF = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    public static final String LOOK_MAC = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
    public static final String LOOK_JAVA = "javax.swing.plaf.metal.MetalLookAndFeel";
    //public static final String LOOK_KUNSTSTOFF = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";

    public static final int COMPONENT_MIN_SIZE = 40;


    public static final int TYPE_SERIALIZE_RAW = 0; // serialize the complete image array data uncompressed
    public static final int TYPE_SERIALIZE_JPEG = 1; // serialize the complete image array data JPEG encoded


    public static Java2dTools getReference() {
        if (java2dTools == null) {
            java2dTools = new Java2dTools();
        }

        return java2dTools;
    }


    public static void setLookAndFeel(String lookAndFeel, JFrame frame1) {
        try {
            // the java look - on all plattforms
            //"javax.swing.plaf.metal.MetalLookAndFeel"
            //UIManager.getCrossPlatformLookAndFeelClassName();

            UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();

            for (int i = 0; i < info.length; i++) {
                cat.fine("LOOK: " + info[i].getName() + ", \"" + info[i].getClassName() + "\"");
            }

            LookAndFeel[] infoAux = UIManager.getAuxiliaryLookAndFeels();

            if (infoAux == null) {
                cat.fine("LOOK AUX: no additional look and feels installed");
            } else {
                for (int i = 0; i < infoAux.length; i++) {
                    cat.fine("LOOK AUX: " + infoAux[i].getName() + ", \"" + infoAux[i].getDescription());
                }
            }

            // the look and feel of th currend os
            if (lookAndFeel.equals(LOOK_WINDOWS)) {
                UIManager.setLookAndFeel(LOOK_WINDOWS);
            } else if (lookAndFeel.equals(LOOK_MOTIF)) {
                UIManager.setLookAndFeel(LOOK_MOTIF);
            } else if (lookAndFeel.equals(LOOK_MAC)) {
                UIManager.setLookAndFeel(LOOK_MAC);
            } else if (lookAndFeel.equals(LOOK_JAVA)) {
                UIManager.setLookAndFeel(LOOK_JAVA);
            } /*else if (lookAndFeel.equals(LOOK_KUNSTSTOFF)) {
                UIManager.setLookAndFeel(new com.incors.plaf.kunststoff.KunststoffLookAndFeel());
            }*/ else
                UIManager.getSystemLookAndFeelClassName();

            if (frame1 != null) {
                Rectangle rect = frame1.getBounds();
                SwingUtilities.updateComponentTreeUI(frame1);
                frame1.pack();
                frame1.setBounds(rect);
                frame1.validate();
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }
    }


    private Java2dTools() {
    } // end constructor


    public void drawCenteredText(Graphics g1, String text1, int middleX1, int middleY1) {
        int textWidth = (g1.getFontMetrics()).stringWidth(text1);
        //int d = (g1.getFontMetrics()).getDescent();
        int textHeight = (g1.getFontMetrics()).getHeight();


        int x = (middleX1 - (textWidth / 2));
        int y = (middleY1 + (textHeight / 2));

        g1.drawString(text1, x, y);
    }

    /**
     * use the paint method to generate an svg stream
     */
    public void java2Svg(JPanel panel1, String filename1) {
        cat.severe("not implemented");
        /*try {
           System.out.println("writing svg. Pleas wait");
           // Get a DOMImplementation
           DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
           // Create an instance of org.w3c.dom.Document
           Document document = domImpl.createDocument(null, "svg", null);
           // Create an instance of the SVG Generator
           SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
           // Ask the test to render into the SVG Graphics2D implementation
           //TestSVGGen test = new TestSVGGen();
           panel1.paint(svgGenerator);
           // Finally, stream out SVG to the standard output using UTF-8
           // character to byte encoding

           FileOutputStream fout = new FileOutputStream(filename1);
           Writer out = new OutputStreamWriter(fout, "UTF-8");

           //Writer out = new OutputStreamWriter(System.out, "UTF-8");
           boolean useCSS = true; // we want to use CSS style attribute
           svgGenerator.stream(out, useCSS);
           System.out.println("File written: \"" + filename1 + "\"");
        } catch (Exception e) {
           e.printStackTrace();
        }*/
    }

    public void viewSvg(String filename1) {
        cat.severe("not implemented");
        /*try {
           String[] args = new String[1];
           args[0] = filename1;

           org.apache.batik.apps.svgbrowser.Main browser;
           browser = new org.apache.batik.apps.svgbrowser.Main(args);


        } catch (Exception e) {
           e.printStackTrace();
        }*/
    }

    /**
     * Retrun a Color that has enough contrast with the drawColor so it can be used for
     * a readable text. Default color is black. If the drawcolor is dark, white is returned
     */
    public Color getContrastColor(Color drawColor1) {
        Color returnValue = Color.BLACK;
        if (drawColor1 == null) {
            return returnValue;
        }

        //System.out.println("C: " + drawColor1.toString());
        try {
            Color black = Color.BLACK;
            //drawColor1.
            if (drawColor1.getRGB() == black.getRGB()) {
                returnValue = Color.GRAY;

            }

            if ((drawColor1.getRed() < 10) && (drawColor1.getRed() < 10) && (drawColor1.getRed() < 10)) {
                returnValue = Color.GRAY;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * fit a frame (e. g. a image) to a window by leaving the aspect ratio unchanged
     *
     * @param window1    the size of the window that defines the outer border
     * @param component1 the size of a component that has to be fit in
     * @return the new size of the component1. The component lies in the middle of the outer window
     */
    /*
    public Rectangle fitToWindow(Rectangle window1, Rectangle component1) {
        Rectangle returnValue = new Rectangle((int) window1.getX(), (int) window1.getY(), (int) component1.getWidth(), (int) component1.getHeight());

        // ratio from width to heigth
        float ratioComponent = (float) (component1.width / (float) component1.height);
        float ratioWindows = (float) (window1.width / (float) window1.height);

        //cat.fine("-----------------------------------------------");
        //cat.fine("Input component: " + returnValue.toString() + "Ratio: " + ratioComponent);
        //cat.fine("Input win : " + window1.toString() + "Ratio: " + ratioWindows);
        try {
            // ratio from width to heigth

            boolean hasShrinked = false;
            // shrink component width if window is smaller
            if (returnValue.width > window1.width) {
                int newWidth = (int) (window1.width);
                int newHeigh = (int) (newWidth / ratioComponent);

                returnValue.setBounds((int) returnValue.getX(), (int) returnValue.getY(), newWidth, newHeigh);
                hasShrinked = true;
                //cat.fine("CORRECT width: ratio: " + (newWidth / (float) newHeigh) + ", new w/h: " + newWidth + ", " + newHeigh);
            }

            // shrink frame heigh if window is too small
            // the wide of the image fits already to the windows bounds
            if (returnValue.height > window1.height) {
                int newHeigh = (int) window1.height;
                int newWidth = (int) (newHeigh * ratioComponent);


                returnValue.setBounds((int) returnValue.getX(), (int) returnValue.getY(), (int) newWidth, (int) newHeigh);
                hasShrinked = true;
                //cat.fine("CORRECT Heigh: ratio: " + (newWidth / (float) newHeigh) + ", new w/h: " + newWidth + ", " + newHeigh);
            }

            if (!hasShrinked) {
                // strech frame width if window is too big
                float rateWithBigger = (window1.width / (float) (returnValue.width));
                //float rateWithBigger = 1000;
                float rateHeighBigger = (window1.height / (float) (returnValue.height));
                //float rateHeighBigger = 1000;

                float rateBigger = 1;
                float ratioFrameW = 1;
                float ratioFrameH = 1;
                if (rateWithBigger < rateHeighBigger) {
                    rateBigger = rateWithBigger;
                    //ratioFrameH = 1/ratioComponent;
                } else {
                    rateBigger = rateHeighBigger;
                    //ratioFrameW = ratioComponent;
                }

                int newWidth = (int) (returnValue.width * rateBigger * ratioFrameW);
                int newHeigh = (int) (returnValue.height * rateBigger * ratioFrameH);
                returnValue.setBounds((int) returnValue.getX(), (int) returnValue.getY(), (int) newWidth, (int) newHeigh);
                //cat.fine("CORRECT Streched: rW: " + ratioFrameW + ", rH: " + ratioFrameH + ", bigger rate: " + rateBigger);
                //cat.fine("-->New Width: " + newWidth + ", new Heigh: " + newHeigh);
            }  // end if strech

            // finally center the frame
            //int middleX = (int) window1.getWidth() / 2;
            //int middleY = (int) window1.getHeight() / 2;

            int offsetX = (int) ((window1.getWidth() - returnValue.getWidth()) / 2);
            int offsetY = (int) ((window1.getHeight() - returnValue.getHeight()) / 2);

            int with = returnValue.width;
            int height = returnValue.height;
            returnValue.setBounds((int) returnValue.getX() + offsetX, (int) returnValue.getY() + offsetY, with, height);
            //cat.fine("New Center: " + returnValue.toString());
            //returnValue.setBounds(0, 0, with, height);

        } catch (Exception e) {
            cat.severe(e.toString());
        }


        return returnValue;
    }
    */

    public Rectangle2D fitToWindow(Rectangle2D window1, Rectangle2D component1) {
        Rectangle2D returnValue = (Rectangle2D) window1.clone();
        returnValue.setRect(window1.getX(), window1.getY(), component1.getWidth(), component1.getHeight());

        // ratio from width to heigth
        double ratioComponent = (double) (component1.getWidth() / (double) component1.getHeight());
        double ratioWindows = (double) (window1.getWidth() / (double) window1.getHeight());

        //cat.fine("-----------------------------------------------");
        //cat.fine("Input component: " + returnValue.toString() + "Ratio: " + ratioComponent);
        //cat.fine("Input win : " + window1.toString() + "Ratio: " + ratioWindows);
        try {
            // ratio from width to heigth

            boolean hasShrinked = false;
            // shrink component width if window is smaller
            if (returnValue.getWidth() > window1.getWidth()) {
                double newWidth = (window1.getWidth());
                double newHeigh = (newWidth / ratioComponent);

                returnValue.setRect(returnValue.getX(), returnValue.getY(), newWidth, newHeigh);
                hasShrinked = true;
                //cat.fine("CORRECT width: ratio: " + (newWidth / (float) newHeigh) + ", new w/h: " + newWidth + ", " + newHeigh);
            }

            // shrink frame heigh if window is too small
            // the wide of the image fits already to the windows bounds
            if (returnValue.getHeight() > window1.getHeight()) {
                double newHeigh = window1.getHeight();
                double newWidth = (newHeigh * ratioComponent);


                returnValue.setRect(returnValue.getX(), returnValue.getY(), newWidth, newHeigh);
                hasShrinked = true;
                //cat.fine("CORRECT Heigh: ratio: " + (newWidth / (float) newHeigh) + ", new w/h: " + newWidth + ", " + newHeigh);
            }

            if (!hasShrinked) {
                // strech frame width if window is too big
                double rateWithBigger = (window1.getWidth() / (double) (returnValue.getWidth()));
                //float rateWithBigger = 1000;
                double rateHeighBigger = (window1.getHeight() / (double) (returnValue.getHeight()));
                //float rateHeighBigger = 1000;

                double rateBigger = 1;
                double ratioFrameW = 1;
                double ratioFrameH = 1;
                if (rateWithBigger < rateHeighBigger) {
                    rateBigger = rateWithBigger;
                    //ratioFrameH = 1/ratioComponent;
                } else {
                    rateBigger = rateHeighBigger;
                    //ratioFrameW = ratioComponent;
                }

                double newWidth = (returnValue.getWidth() * rateBigger * ratioFrameW);
                double newHeigh = (returnValue.getHeight() * rateBigger * ratioFrameH);
                returnValue.setRect(returnValue.getX(), returnValue.getY(), newWidth, newHeigh);
                //cat.fine("CORRECT Streched: rW: " + ratioFrameW + ", rH: " + ratioFrameH + ", bigger rate: " + rateBigger);
                //cat.fine("-->New Width: " + newWidth + ", new Heigh: " + newHeigh);
            }  // end if strech

            // finally center the frame
            //int middleX = (int) window1.getWidth() / 2;
            //int middleY = (int) window1.getHeight() / 2;

            double offsetX = ((window1.getWidth() - returnValue.getWidth()) / 2);
            double offsetY = ((window1.getHeight() - returnValue.getHeight()) / 2);

            double with = returnValue.getWidth();
            double height = returnValue.getHeight();
            returnValue.setRect(returnValue.getX() + offsetX, returnValue.getY() + offsetY, with, height);
            //cat.fine("New Center: " + returnValue.toString());
            //returnValue.setBounds(0, 0, with, height);

        } catch (Exception e) {
            cat.severe(e.toString());
        }


        return returnValue;
    }


    /**
     * norimalize to 0..1
     *
     * @param pointList
     * @return Point2D.Double[]
     */
    public java.util.List normalizeCoordinate(java.util.List pointList) {
        if (pointList == null) {
            return new Vector(0, 1);
        }

        java.util.List returnValue = new Vector(pointList.size(), 5);

        Rectangle2D.Double border;
        border = (Rectangle2D.Double) calculateBorder(pointList, 5, new Rectangle2D.Double(0, 0, 0, 0));
        Rectangle2D.Double normalBorder =
                (Rectangle2D.Double) fitToWindow(new Rectangle2D.Double(0, 0, 1, 1), new Rectangle2D.Double(border.getX(), border.getY(), border.getWidth(), border.getHeight()));
        if (pointList == null) {
            return returnValue;
        }

        for (int i = 0; i < pointList.size(); i++) {
            Object o = (Object) pointList.get(i);
            Point2D.Double newPoint = null;
            if (Point.class.isInstance(o)) {
                Point oldPoint = (Point) o;
                newPoint = new Point2D.Double(oldPoint.getX(), oldPoint.getY());
            } else {
                newPoint = (Point2D.Double) o;
            }

            // add the points
            Point2D.Double normalPoint = (Point2D.Double) fitPointToOtherBorder(newPoint, border, normalBorder);
            returnValue.add(normalPoint);
        } // end for

        return returnValue;
    }


    // This method returns true if the specified image has transparent pixels
    public boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();

        if (cm == null) {
            return false;
        } else {
            return cm.hasAlpha();
        }
    }

    // This method returns a buffered image with the contents of an image
    public BufferedImage imageToBufferedImage(Image image) {
        if (image == null) {
            return null;
        }

        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        //System.out.println("image 1");
        //showImage(image);
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e665 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);

            //   System.out.println("image 2");
            //showImage(bimage);
        } catch (HeadlessException e) {
            cat.severe(e.toString());
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, Color.WHITE, null);
        g.dispose();

        //System.out.println("image 3");
        //showImage(bimage);

        return bimage;
    }


    public Image bufferedImageToImage(BufferedImage bImage1) {
        Image returnValue = null;
        if (bImage1 == null) {
            return returnValue;
        }


        try {
            returnValue = Toolkit.getDefaultToolkit().createImage(bImage1.getSource());

            /*ImageFilter filter = new ImageFilter();
                JLabel i = new JLabel();
                Image img;
                img = i.createImage(new FilteredImageSource(img1.getSource(), filter));
            */
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    // old version, uses Filters. This means performance problems, because
    // the filter is applied each time the image has to be drawn.
/*public BufferedImage getBrighterImage(BufferedImage image1) {
       BufferedImage returnValue = image1;

        if (image1 == null) {
            return returnValue;
        }

       ImageFilter filter;
       filter = new Transparent(30);
       Image image;

        ImageProducer orgImageProducer = image1.getSource();
        ImageProducer imageProducer = new FilteredImageSource(orgImageProducer, filter);

        image = Toolkit.getDefaultToolkit().createImage(imageProducer);
       returnValue = ImageToBufferedImage(image);

       return returnValue;
    }*/

    public BufferedImage getBrighterImage(BufferedImage image1) {
        BufferedImage returnValue = image1;

        if (image1 == null) {
            return returnValue;
        }

        BufferedImage l_img = new BufferedImage(image1.getWidth(), image1.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D l_gfx = l_img.createGraphics();
        l_gfx.setColor(new Color(1f, 1f, 1f));
        l_gfx.fillRect(0, 0, l_img.getWidth(), l_img.getHeight());

        l_gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        l_gfx.drawImage(image1, 0, 0, null);

        returnValue = l_img;

        return returnValue;
    }


    public void showImage(BufferedImage img1) {
        try {
            ImageFilter filter = new ImageFilter();
            JLabel i = new JLabel();
            Image img;
            //img = i.createImage(new FilteredImageSource(image1.getSource(), filter));
            img = i.createImage(new FilteredImageSource(img1.getSource(), filter));
            showImage(img);
        } catch (Exception e) {
            cat.severe(e.toString());
        }
    }

    public void showImage(Image img) {

        try {
            JDialog test = new JDialog(new JDialog(), true);
            int width = img.getWidth(null);
            int height = img.getHeight(null);
            if (width < 50) {
                width = 50;
            }
            if (height < 50) {
                height = 50;
            }
            test.setSize(width, height);
            test.getContentPane().setLayout(new BorderLayout());
            JButton x = new JButton("Image");
            ImageIcon icon = new ImageIcon(img);
            x.setIcon(icon);
            test.getContentPane().add(x, BorderLayout.CENTER);
            test.setVisible(true);
            //test.show();
        } catch (Exception e) {
            cat.severe(e.toString());
        }
    }


    /**
     * @param filename        filename of the input-File
     * @param width           sesired with of the returned image
     * @param heigh           sesired hight of the returned image
     * @param keepAspectRatio if true, the source image is fit inot the given boundary keeping the aspect ration
     * @param defaultImage    image to use if the source image can not be read or is an invalid image
     * @return
     */
    public Image scaleImage(String filename, int width, int heigh, boolean keepAspectRatio, Image defaultImage) {
        Image returnValue = defaultImage;
        try {
            Image image = null;
            if (filename.equals(""))
                image = defaultImage;
            else {
                if (FileTools.existsFile(filename)) {
                    image = ImageIO.read(new File(filename));
                } else {
                    cat.info("Image at \"" + filename + "\" does not exist");
                    image = defaultImage;
                }

            }

            if (image == null) {
                return returnValue;
            }

            int h = image.getHeight(null);
            int w = image.getWidth(null);

            int MIN_IMAGE_SIZE = 5;
            if (h < MIN_IMAGE_SIZE || w < MIN_IMAGE_SIZE || filename.equals("")) {
                image = defaultImage;
            }

            int scaleX = 0;
            int scaleY = 0;
            int scaleWidth = 0;
            int scaleHeigh = 0;
            if (keepAspectRatio) {

                Rectangle2D window = new Rectangle(0, 0, width, heigh);
                Rectangle2D imageSize = new Rectangle(0, 0, w, h);
                Rectangle2D newSize = fitToWindow(window, imageSize);

                scaleX = (int) newSize.getX();
                scaleY = (int) newSize.getY();
                scaleWidth = (int) newSize.getWidth();
                scaleHeigh = (int) newSize.getHeight();
            } else {
                scaleX = 0;
                scaleY = 0;
                scaleWidth = width;
                scaleHeigh = heigh;
            }

            //showImage(image);
            BufferedImage img = new BufferedImage(width, heigh, BufferedImage.TYPE_INT_RGB);
            Graphics g2 = img.getGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, heigh);
            g2.drawImage(image, scaleX, scaleY, scaleWidth, scaleHeigh, null);

            ImageFilter filter = new ImageFilter();
            JLabel i = new JLabel();
            returnValue = i.createImage(new FilteredImageSource(img.getSource(), filter));
            //showImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * extract dominant colors using a separate dialog
     */
    public IColorRectangle[] getDominantColor(BufferedImage image1, JPanel drawPanel1) {
        IColorRectangle[] returnValue = new IColorRectangle[0];

        if (image1 == null) {
            cat.severe("Image is null. No colors are extracted");
            return returnValue;
        }
        try {

            Frame dummyFrame = new Frame();
            DominantColorPlugin dominantColor = new DominantColorPlugin(dummyFrame, image1);
            RGBColorPercentagePairList list = dominantColor.getResult();
            if (list == null) {
                return returnValue; //---------------- EXIT POINT ------------------------
            }
            returnValue = new IColorRectangle[list.size()];

            int posX = 0;
            int posY = 0;

            int drawSize = drawPanel1.getWidth() * drawPanel1.getHeight();
            float ratio = ((float) drawPanel1.getWidth() / drawPanel1.getHeight()); // r = w / h

            int i = 0;
            for (Iterator it = list.iterator(); it.hasNext(); i++) {
                RGBColorPercentagePair data = (RGBColorPercentagePair) it.next();

                /* TODO: use this, if you want the size depending on the draw area size
                int width = (int) Math.sqrt((drawSize * data.getPercentage()) / ratio);
                int heigh = (int) (width * ratio);

                if (width < COMPONENT_MIN_SIZE) {
                    width = COMPONENT_MIN_SIZE;
                }
                if (heigh < COMPONENT_MIN_SIZE) {
                    heigh = COMPONENT_MIN_SIZE;
                }*/

                int width = COMPONENT_MIN_SIZE;
                int heigh = (int) (width * 1.33);

                System.out.println("Color:[" + data.getColor().getRed() + ", " +
                        data.getColor().getGreen() + ", " +
                        data.getColor().getBlue() + "], " +
                        "Percent: " + data.getPercentage() + ", x: " + posX + ", y: " + posY +
                        ", Width: " + width + ", Heigh: " + heigh + ", Panel: " + drawPanel1.getWidth() + ", " +
                        drawPanel1.getHeight());

                //ColorComponent component = new ColorComponent(drawPanel, 10, posY, width, heigh, data.getColor(), Color.WHITE);
                //addColorComponent(component);

                IColorRectangle component = new IColorRectangle(drawPanel1, posX, posY, width, heigh,
                        data.getColor(), Color.WHITE, false);
                returnValue[i] = component;
                //addNewComponent(component);


                // position the components from left to right and up to down
                /*posY += (heigh + 10);

                if (posY > (drawPanel1.getY() - 15)) {
                   posX += drawPanel1.getWidth() / 3;
                   posY = 0;
                }*/

                posX += drawPanel1.getWidth() / 3;

                if (posX > (drawPanel1.getWidth() - 15)) {
                    posY += drawPanel1.getHeight() / 3;
                    posX = 0;
                }
            }

            // swap the compoent order so the biggest component do not cover the smaller ones
            IColorRectangle[] swapList = new IColorRectangle[returnValue.length];
            for (int j = 0; j < returnValue.length; j++) {
                swapList[j] = returnValue[returnValue.length - j - 1];
            }
            returnValue = swapList;
            //repaint();
        } catch (Exception e1) {
            cat.severe(e1.toString());
            e1.printStackTrace();
        }

        return returnValue;
    }

    /**
     * returns the dominant color of the given image in the given shape (Point() list).
     * "null" is returned, if no color can be extracted
     */
    public Color getDominantColor(BufferedImage image1, java.util.List pointList1) {
        Color returnValue = null;

        Rectangle shapeBorder;
        shapeBorder = (Rectangle) calculateBorder(pointList1, 0, new Rectangle(0, 0, 0, 0));
        Point leftUpPoint = new Point((int) shapeBorder.getX(), (int) shapeBorder.getY());
        Point rightDownPoint = new Point((int) (shapeBorder.getX() + shapeBorder.getWidth()),
                (int) (shapeBorder.getY() + shapeBorder.getHeight()));

        try {
            RGBColorPercentagePairList colorList = new RGBColorPercentagePairList();

            DominantColorFinder colorFinder = new DominantColorFinder(image1.getRaster(), colorList,
                    leftUpPoint, rightDownPoint);
            colorFinder.run();

            if (colorList != null && colorList.size() > 0) {
                RGBColorPercentagePair data = (RGBColorPercentagePair) colorList.get(0);
                returnValue = (Color) data.getColor();
            }
        } catch (Exception e) {
            cat.severe(e.toString());
            e.printStackTrace();
        }

        return returnValue;
    }


    private int strahlenSatz(int x1, int x2, int y2) {
        return (int) (((double) x1 * y2) / x2);
    }

    /**
     * returns the point where the line from the given point to the
     * middle of the rectangle tangents the rectangle border
     */
    public Point getRectangleLineCut(Rectangle rect1, Point p1) {
        Point returnValue = new Point(0, 0);

        if (rect1 == null || p1 == null) {
            return returnValue;
        }

        try {
            // rectangle middle point
            int middleX = (int) (rect1.getX() + (rect1.getWidth() / 2));
            int middleY = (int) (rect1.getY() + (rect1.getHeight() / 2));

            returnValue = new Point(middleX, middleY);

            //Console.getReference().echo("Middle: " + middleX + ", " + middleY + ", P: " + p1.toString());
            // Strahlensatz
            // first try left or right corner
            int x1 = (int) ((rect1.getWidth() / 2));
            int x2 = (int) (p1.getX() - middleX);
            int y2 = (int) (p1.getY() - middleY);
            int y1 = strahlenSatz(x1, x2, y2);

            Point borderPoint = null;
            if (p1.getX() > middleX) { //first try right border
                borderPoint = new Point(middleX + x1, middleY + y1);
            } else { //otherwise use the left corner
                borderPoint = new Point(middleX - x1, middleY - y1);
            }

            Rectangle smallerRect = new Rectangle((int) rect1.getX() + 2, (int) rect1.getY() + 2,
                    (int) rect1.getWidth() - 4, (int) rect1.getHeight() - 4);
            if (smallerRect.contains(borderPoint)) { // if inside return the middle-point
                //Console.getReference().echo("is inside, " + smallerRect.toString() + ", "  + borderPoint.toString());
                return returnValue;
            }
            //Console.getReference().echo("Rect y: " + (rect1.getHeight() / 2) + ", Y: " + y1 + ", " + Math.abs(y1));

            if ((rect1.getHeight() / 2) >= Math.abs(y1)) { // check that the right border was calculated
                returnValue = borderPoint;
                return returnValue;
            }

            // no the line must tangent the upper or lower border
            y1 = (int) ((rect1.getHeight() / 2));
            x2 = (int) (p1.getX() - middleX);
            y2 = (int) (p1.getY() - middleY);
            x1 = strahlenSatz(x2, y2, y1);

            borderPoint = null;
            if (p1.getY() > middleY) { //first try the lower border
                borderPoint = new Point(middleX + x1, middleY + y1);
            } else { //otherwise use the upper corner
                borderPoint = new Point(middleX - x1, middleY - y1);
            }

            /*if (smallerRect.contains(borderPoint)) { // if inside the rectangle return the middle-point
               //Console.getReference().echo("is inside, " + smallerRect.toString() + ", "  + borderPoint.toString());
               return returnValue;
            }*/
            //Console.getReference().echo("Rect: " + (rect1.toString()) + ", P: " + borderPoint.toString());

            if ((rect1.getWidth() / 2) >= Math.abs(x1)) { // check that the right border was calculated
                returnValue = borderPoint;
                return returnValue;
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    public Shape pointListToShape(java.util.List pointList) {
        Shape returnValue = new Rectangle(0, 0, 0, 0);

        try {
            Polygon polygon = new Polygon();
            for (Iterator pointIterator = pointList.iterator(); pointIterator.hasNext();) {
                Point point = (Point) pointIterator.next();
                polygon.addPoint((int) point.getX(), (int) point.getY());
            }
            returnValue = polygon;
        } catch (Exception e) {
            e.printStackTrace();
            cat.severe(e.toString());
        }

        return returnValue;
    }


    /**
     * calculate the surronding border of a list of Point()
     * dotSize in pixel: border is calculated to be outside the dots
     * if not border can be calculated, rectangel with zero dimension is returned
     */
    public Rectangle2D calculateBorder(java.util.List pointList1, int dotSize1, Rectangle2D defaultRectangle) {
        Rectangle2D returnValue = defaultRectangle;

        if (pointList1 == null || pointList1.size() < 1) {
            return returnValue;
        }

        Point2D typePoint = (Point2D) pointList1.get(0);

        try {
            Point2D left = (Point2D) typePoint.clone();
            left.setLocation(0, 0);
            Point2D right = (Point2D) typePoint.clone();
            right.setLocation(0, 0);
            Point2D up = (Point2D) typePoint.clone();
            up.setLocation(0, 0);
            Point2D down = (Point2D) typePoint.clone();
            down.setLocation(0, 0);

            boolean isFirst = true;
            for (Iterator it = pointList1.iterator(); it.hasNext();) {
                Point2D p = (Point2D) it.next();

                //cat.fine("DOT: " + p.toString());
                if (isFirst) {
                    left = p;
                    right = p;
                    up = p;
                    down = p;

                    isFirst = false;
                }
                if (left.getX() > p.getX()) left = p;
                if (right.getX() < p.getX()) right = p;
                if (up.getY() > p.getY()) up = p;
                if (down.getY() < p.getY()) down = p;
            } // end while

            int size = dotSize1;
            returnValue.setRect(left.getX() - size, up.getY() - size,
                    right.getX() - left.getX() + 2 * size, down.getY() - up.getY() + 2 * size);
            //cat.fine("New Surrounding border: " + returnValue.toString());
        } catch (Exception e) {
            e.printStackTrace();
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * fit the point to a new rectangle size. Used to transform coordinates
     */
    public Point2D fitPointToOtherBorder(Point2D point1, Rectangle2D orginalBorder1, Rectangle2D newBorder1) {
        Point2D returnValue = point1;

        if (point1 == null || orginalBorder1 == null || newBorder1 == null) {
            return returnValue;
        }

        try {
            double ratioX = newBorder1.getWidth() / orginalBorder1.getWidth();
            double ratioY = newBorder1.getHeight() / orginalBorder1.getHeight();

            double relX = point1.getX() - orginalBorder1.getX();
            double relY = point1.getY() - orginalBorder1.getY();

            returnValue = (Point2D) point1.clone();
            returnValue.setLocation((newBorder1.getX() + (relX * ratioX)),
                    (newBorder1.getY() + (relY * ratioY)));

        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * list of Point()
     */
    public java.util.List fitPointToOtherBorder(java.util.List pointList1, Rectangle orginalBorder1, Rectangle newBorder1) {
        if (pointList1 == null) {
            return new Vector();
        }
        java.util.List returnValue = new Vector(pointList1.size());

        try {
            for (Iterator listIterator = pointList1.iterator(); listIterator.hasNext();) {
                Point point = (Point) listIterator.next();
                Point newPoint = (Point) fitPointToOtherBorder(point, orginalBorder1, newBorder1);
                returnValue.add(newPoint);
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }


    public byte[] intToByteArray(int value) {
        byte[] byte_array = new byte[4];

        byte_array[0] = (byte) ((value >>> 24) & 0xFF);
        byte_array[1] = (byte) ((value >>> 16) & 0xFF);
        byte_array[2] = (byte) ((value >>> 8) & 0xFF);
        byte_array[3] = (byte) (value & 0xFF);
        return byte_array;
    }

    public int byteArrayToInt(byte[] byte_array) {
        int value = ((((int) byte_array[0] & 0xFF) << 24) |
                (((int) byte_array[1] & 0xFF) << 16) |
                (((int) byte_array[2] & 0xFF) << 8) |
                ((int) byte_array[3] & 0xFF));
        return value;
    }

    /**
     * Grabs the pixels of the given Image object and returns the array of those
     * pixels. Note that the Image needs to be preloaded in order for this
     * method to work correctly.
     * Parameters:
     * Image img: The image whose pixels to grab.
     */
    public byte[] serializeImage(Image image1, int TYPE_CODING) {
        byte[] returnValue = new byte[0];
        if (image1 == null) {
            return returnValue;
        }

        try {
            switch (TYPE_CODING) {
                case TYPE_SERIALIZE_RAW:
                    {

                        int width = image1.getWidth(null);
                        int height = image1.getHeight(null);

                        // one int has four bytes
                        int[] pixels = new int[((width * height) + 2)];

                        PixelGrabber grabber = new PixelGrabber(image1, 0, 0, width, height, pixels, 0, width);
                        if (!grabber.grabPixels()) {
                            return returnValue;
                        }
                        pixels[pixels.length - 2] = width;
                        pixels[pixels.length - 1] = height;

                        // convert to a byte array
                        byte[] byteArray = new byte[pixels.length * 4];
                        for (int counter = 0; counter < pixels.length; counter++) {
                            int element = pixels[counter];

                            byte[] bytes = java2dTools.intToByteArray(element);
                            byteArray[counter * 4] = bytes[0];
                            byteArray[(counter * 4) + 1] = bytes[1];
                            byteArray[(counter * 4) + 2] = bytes[2];
                            byteArray[(counter * 4) + 3] = bytes[3];
                        }

                        returnValue = byteArray;
                        break;
                    }
                case TYPE_SERIALIZE_JPEG:
                    {
                        TYPE_SERIALIZE_JPEG:
                        returnValue = serializeImage(imageToBufferedImage(image1), TYPE_CODING);
                        break;
                    }
            } // end switch
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    public byte[] serializeImage(BufferedImage bImage1, int TYPE_CODING) {
        byte[] returnValue = new byte[0];
        try {
            switch (TYPE_CODING) {
                case TYPE_SERIALIZE_RAW:
                    {
                        returnValue = serializeImage(bufferedImageToImage(bImage1), TYPE_CODING);
                        break;
                    }
                case TYPE_SERIALIZE_JPEG:
                    {
                        ByteArrayOutputStream fos = new ByteArrayOutputStream();
                        JPEGImageEncoder encoder =
                                JPEGCodec.createJPEGEncoder(fos);
                        encoder.encode(bImage1);
                        fos.flush();
                        returnValue = fos.toByteArray();
                        fos.close();
                        break;
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            cat.severe(e.toString());
        }

        return returnValue;
    }


    // Creates an Image object from the given array of pixels.
    // Note that the returned Image is created using an ImageProducer
    // (MemoryImageSource), and such Images are painted much slower than
    // offscreen Images created via Component.createImage(int,int) and you can't
    // draw on these Images as their getGraphics() method will return null. If
    // you need an Image that will be drawn fast and/or you need to paint
    // additional things on the Image, create an offscreen Image using
    // Component.createImage(int,int) and paint the Image returned by this method
    // on that Image.
    // Parameters:
    //   int [] pixels: The pixels array.
    public Image deserializeImage(byte[] bytePixels, Image default1, int TYPE_CODING) {
        Image returnValue = default1;

        try {
            switch (TYPE_CODING) {
                case TYPE_SERIALIZE_RAW:
                    {

                        // convert the byte array to an int array
                        int dataLength = bytePixels.length / 4;
                        int[] pixels = new int[dataLength];
                        byte[] bytes = new byte[4];
                        int counter = 0;
                        while (counter < dataLength) {
                            int byteCounter = counter * 4;

                            // String subString = str1.substring(counter, counter + 32);
                            //data[counter] = Integer.parseInt(subString);


                            bytes[0] = bytePixels[byteCounter];
                            bytes[1] = bytePixels[byteCounter + 1];
                            bytes[2] = bytePixels[byteCounter + 2];
                            bytes[3] = bytePixels[byteCounter + 3];

                            pixels[counter] = java2dTools.byteArrayToInt(bytes);
                            counter++;
                        }


                        if (pixels.length == 0) {
                            return returnValue;
                        }


                        int width = pixels[pixels.length - 2];
                        int height = pixels[pixels.length - 1];

                        //int[] imageData = new int[pixels.length - 2];
                        //System.arraycopy(imageData, 0, pixels, 2, pixels.length - 2);
                        MemoryImageSource imageSource = new MemoryImageSource(width, height, pixels, 0, width);

                        returnValue = Toolkit.getDefaultToolkit().createImage(imageSource);
                        break;
                    }
                case TYPE_SERIALIZE_JPEG:
                    returnValue = deserializeImage(bytePixels, imageToBufferedImage(default1), TYPE_CODING);
                    break;
            }


        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    public BufferedImage deserializeImage(byte[] pixels, BufferedImage default1, int TYPE_CODING) {
        BufferedImage returnValue = default1;
        try {
            switch (TYPE_CODING) {
                case TYPE_SERIALIZE_RAW:
                    {
                        returnValue = imageToBufferedImage(deserializeImage(pixels, bufferedImageToImage(default1), TYPE_CODING));
                        break;
                    }
                case TYPE_SERIALIZE_JPEG:
                    {
                        InputStream fin = new ByteArrayInputStream(pixels);
                        returnValue = javax.imageio.ImageIO.read(fin);
                        fin.close();
                        break;
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            cat.severe(e.toString());
        }

        return returnValue;
    }

    public BufferedImage javaObjectsToImage(IComponent[] componentArray) {
        BufferedImage returnValue = null;


        try {
            BufferedImage bffImg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics gfx = bffImg.createGraphics();
            gfx.setColor(Color.WHITE);
            gfx.fillRect(0, 0, (bffImg.getWidth() - 1), (bffImg.getHeight() - 1));
            //gfx.drawRect(10, 10, 50, 20);  // draw a rectangle

            for (int counter = 0; counter < componentArray.length; counter++) {
                IComponent iComponent = componentArray[counter];
                iComponent.paint(gfx);
            }

            //showImage(bffImg);
            returnValue = bffImg;
        } catch (Exception e) {
            cat.severe(e.toString());
            e.printStackTrace();
        }

        return returnValue;
    }


} // end class Java2dTools