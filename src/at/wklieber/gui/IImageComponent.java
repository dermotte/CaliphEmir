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
package at.wklieber.gui;

import at.wklieber.tools.FileTools;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

public class IImageComponent
        extends IRectangle {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IImageComponent.class.getName());
    //private static Console console = Console.getReference();


    protected BufferedImage image = null;


    // ------------- Constructors ----------------------------------------------
    // for use in displaying resizable images
    public IImageComponent(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                           Color drawColor1, Color backgroundColor1, boolean selected1, BufferedImage image1) {
        init(drawPanel1, posX1, posY1, width1, heigh1, drawColor1,
                backgroundColor1, true, true, selected1, false, image1, "");
    }

    // for use in ComponentPalette when the instanciaton class determines the size
    public IImageComponent(JPanel drawPanel1, BufferedImage image1) {
        this.init(drawPanel1, 0, 0, 0, 0, Color.BLACK, Color.WHITE,
                false, false, false, true, image1, "");
    }

    public IImageComponent(JPanel drawPanel1, String filename1) {
        String path = FileTools.getFilePath(filename1);
        String localName = "";
        if (path.length() < filename1.length()) {
            localName = filename1.substring(path.length());
        }

        this.init(drawPanel1, 0, 0, 0, 0, Color.BLACK, Color.WHITE,
                false, false, false, true, null, localName);
        setImage(filename1, false);
    }

    // all parameters for the clone method
    public IImageComponent(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                           Color drawColor1, Color backgroundColor1, boolean resizeable1,
                           boolean isMoveable1, boolean selected1, boolean doDnd1, BufferedImage image1) {
        this.init(drawPanel1, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, image1, "");


    }


    protected void init(JPanel drawPanel1, int posX1, int posY1, int width1, int heigh1,
                        Color drawColor1, Color backgroundColor1, boolean resizeable1,
                        boolean isMoveable1, boolean selected1, boolean doDnd1,
                        BufferedImage image1, String name1) {

        super.init(drawPanel1, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1,
                isMoveable1, selected1, doDnd1, name1);

        image = image1;
        if (image == null) {
            image = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        }
        if (width1 < 1 || heigh1 < 1) {
            fitBoundary();
        }


        validate();
        repaint();
    }

    // adapt the heigh and with of the comonent boundary to fit the image
    private void fitBoundary() {
        boundary.setSize(image.getHeight(), image.getWidth());
    }

    public BufferedImage getImage() {
        return image;
    }

    public ImageIcon getImageIcon() {
        return new ImageIcon(image);
    }

    public void setImage(BufferedImage image, boolean fitSizeToImage1) {
        this.image = image;
        if (fitSizeToImage1) {
            fitBoundary();
        }
    }

    public void setImage(String filename1, boolean fitSizeToImage1) {
        try {
            //this.image = javax.imageio.ImageIO.read(new File(new URI(FileTools.setUrlPrefix(filename1))));
            this.image = javax.imageio.ImageIO.read(new URL(FileTools.setUrlPrefix(filename1)));
            if (fitSizeToImage1) {
                fitBoundary();
            }
        } catch (Exception e) {
            console.error("Error loading image\"" + filename1 + " \": " + e.toString());
            //e.printStackTrace();
        }
    }

    public Object clone() {
        IImageComponent returnValue = null;

        returnValue = new IImageComponent(drawPanel, (int) boundary.getX(), (int) boundary.getY(),
                        (int) boundary.getWidth(), (int) boundary.getHeight(),
                        drawColor, backgroundColor, isResizeable,
                        isMoveable, isSelected, doDnd, image);
        cat.fine("make a IImageComponent Image: " + this.getComponentBounds().getWidth() +
                ", :" + this.getComponentBounds().getHeight());
        cat.fine("make a IImageComponent clone, Clone Image: " + returnValue.getComponentBounds().getWidth() +
                ", :" + returnValue.getComponentBounds().getHeight());

        return returnValue;
    }


    protected void drawComponent(Graphics2D g2) {
        //Graphics2D g2 = (Graphics2D) g;

        if (image == null) cat.severe("Image is null");
        if (boundary == null) cat.severe("boundary is null");

        Rectangle imageSize = new Rectangle((int) boundary.getX(), (int) boundary.getY(),
                        image.getWidth(this), image.getHeight(this));

        Rectangle fitSize;
        fitSize = (Rectangle) java2dTools.fitToWindow(boundary.getBounds(), imageSize);

        g2.drawImage(image, (int) fitSize.getX(), (int) fitSize.getY(), (int) fitSize.getWidth(), (int) fitSize.getHeight(), this);
        //super.paintComponent(g); // draw extra visual and so on
    } // end method paint

    /**
     * add some special menu entries
     * *7
     */
    protected void setPopupMenuEntries() {
        super.setPopupMenuEntries();

        menuTools.setParentClass(this);
        //menuTools.addPopupMenuEntry("&Extract Metadata", "Use this image to extract data", "dominant_color.gif", "actionColorAndShape");
        menuTools.addPopupMenuEntry("&Set as background", "Set this image as background", "background_image.gif", "actionBackgroundImage");
    }

    public void actionBackgroundImage(ActionEvent e) {
        BufferedImage newImage = java2dTools.getBrighterImage(this.getImage());
        drawPanel.setBackgroundImage(newImage);
        //image = newImage;
        //java2dTools.showImage(newImage);
        //repaint();
    }


    //--------------------- Mouse Events ----------------------------------------
    /* // ovveridden from icomponent
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        //cat.fine("mouseClicked: " + e.getX() + ", " + e.getY());
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() > 1)) { // left doubleclick
            setComponentAsSelected();

        } else {
            super.mouseClicked(e);
        }
    }*/


    public String toString() {
        return "nothing to report";
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
} // end class IColorRectangle

