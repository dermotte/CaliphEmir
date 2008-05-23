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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Date: 14.01.2005
 * Time: 00:00:38
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Visualization2DPanel extends JPanel implements MouseMotionListener, MouseListener {
    private float points[][];
    private float maxX = 0f, maxY = 0f, minX = 0f, minY = 0f;
    private java.util.List<String> fileList;
    private static final int OFFSET = 20;
    private java.util.List<BufferedImage> imageList;
    private static final float IMG_MAXIMUM_SIDE = 20f;
    private int imagesLoaded = 0;
    private AffineTransform transform;

    private int moveX = 0, moveY = 0;
    private enum MouseState {NONE, BUTTON1_PRESSED};
    private MouseState state = MouseState.NONE;

    private Point lastPoint = null;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public Visualization2DPanel(float[][] points, java.util.List<String> files) {
        this.points = points;
        fileList = files;
        addMouseMotionListener(this);
        addMouseListener(this);
        init();
        LoaderThread lt = new LoaderThread(this);
        new Thread(lt).start();
    }

    private void init() {
        imageList = new LinkedList<BufferedImage>();
        for (int i = 0; i < points.length; i++) {
            float[] point = points[i];
            if (point[0] > maxX) maxX = point[0];
            if (point[1] > maxY) maxY = point[1];
            if (point[0] < minX) minX = point[0];
            if (point[1] < minY) minX = point[1];
        }
    }

    /**
     * Allows the iterative loading of image files ...
     * @return true if another image file can be loaded, false otherwise.
     */
    public boolean initNextImage() {
        if (imagesLoaded < points.length) {
            String pathname = fileList.get(imagesLoaded);
            pathname = pathname.replace(".mp7.xml", ".jpg");
            File f = new File(pathname);
            BufferedImage bi = null;
            try {
//                System.out.println("Reading file " + imagesLoaded + ": " + pathname);
                bi = ImageIO.read(f);
                if (bi != null) bi = resize(bi);
            } catch (IOException e) {
                System.err.println("Error reading image " + pathname);
            }
            imageList.add(bi);
        }
        imagesLoaded++;
        repaint();
        if (imagesLoaded > points.length)
            return false;
        else
            return true;
    }

    private BufferedImage resize(BufferedImage img) {
        int height = img.getHeight();
        int width = img.getWidth();
        float scaleFactor = ((float) width) / IMG_MAXIMUM_SIDE;
        if (height > width) {
            scaleFactor = ((float) height) / IMG_MAXIMUM_SIDE;
        }
        int widthNew = (int) (((float) width) / scaleFactor);
        int heightNew = (int) (((float) height) / scaleFactor);
        BufferedImage bi = new BufferedImage(widthNew, heightNew, BufferedImage.TYPE_INT_RGB);
        bi.getGraphics().drawImage(img, 0, 0, bi.getWidth(), bi.getHeight(), null);
        return bi;
    }

    /**
     * Calls the UI delegate's paint method, if the UI delegate
     * is non-<code>null</code>.  We pass the delegate a copy of the
     * <code>Graphics</code> object to protect the rest of the
     * paint code from irrevocable changes
     * (for example, <code>Graphics.translate</code>).
     * <p/>
     * If you override this in a subclass you should not make permanent
     * changes to the passed in <code>Graphics</code>. For example, you
     * should not alter the clip <code>Rectangle</code> or modify the
     * transform. If you need to do these operations you may find it
     * easier to create a new <code>Graphics</code> from the passed in
     * <code>Graphics</code> and manipulate it. Further, if you do not
     * invoker super's implementation you must honor the opaque property,
     * that is
     * if this component is opaque, you must completely fill in the background
     * in a non-opaque color. If you do not honor the opaque property you
     * will likely see visual artifacts.
     * <p/>
     * The passed in <code>Graphics</code> object might
     * have a transform other than the identify transform
     * installed on it.  In this case, you might get
     * unexpected results if you cumulatively apply
     * another transform.
     *
     * @param g the <code>Graphics</code> object to protect
     * @see #paint
     * @see javax.swing.plaf.ComponentUI
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // save old transformations ...
        AffineTransform old = g2.getTransform();
        // erase background
        g2.setColor(Color.black);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.green);
        // Apply move
        transform = AffineTransform.getTranslateInstance(moveX,  moveY);
        g2.setTransform(transform);
        float width = this.getWidth() - 2 * OFFSET;
        float height = this.getHeight() - 2 * OFFSET;
        for (int i = 0; i < points.length; i++) {
            float[] point = points[i];
            float x = (point[0] - minX) / (maxX - minX);
            float y = (point[1] - minY) / (maxY - minY);
            int projectionX = (int) (x * width) + OFFSET;
            int projectionY = (int) (y * height) + OFFSET;
            BufferedImage bi = null;
            if (i < imageList.size()) bi = imageList.get(i);
            if (bi != null) {
                g2.drawImage(bi, projectionX - bi.getWidth() / 2, projectionY - bi.getHeight() / 2, null);
            } else {
                g2.fillOval(projectionX - 2, projectionY - 2, 4, 4);
            }
        }
        // restore old transformations:
        g2.setTransform(old);
    }


    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p/>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     */
    public void mouseDragged(MouseEvent e) {
//        System.out.println("Dragged: " + e.getPoint());
        if (lastPoint==null) lastPoint = e.getPoint();
        if (state == MouseState.BUTTON1_PRESSED) {
            moveX += e.getPoint().x-lastPoint.x;
            moveY += e.getPoint().y-lastPoint.y;
            repaint();
        }
        lastPoint = e.getPoint();
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e) {
//        System.out.println("Moved: " + e.getPoint());
//        if (lastPoint==null) lastPoint = e.getPoint();
//        if (state == MouseState.BUTTON1_PRESSED) {
//            moveX += e.getPoint().x-lastPoint.x;
//            moveY += e.getPoint().y-lastPoint.y;
//        }
//        lastPoint = e.getPoint();
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getModifiers() == MouseEvent.ALT_DOWN_MASK) {
        }

    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON1) state = MouseState.BUTTON1_PRESSED;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
        state = MouseState.NONE;
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {

    }
}

class LoaderThread implements Runnable {
    private Visualization2DPanel panel;

    public LoaderThread(Visualization2DPanel panel) {
        this.panel = panel;
    }

    public void run() {
        while (panel.initNextImage()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
