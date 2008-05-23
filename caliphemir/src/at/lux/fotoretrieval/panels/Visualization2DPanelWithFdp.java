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

import at.lux.fotoretrieval.EmirConfiguration;
import at.lux.retrieval.calculations.DistanceMatrix;
import at.lux.retrieval.fdp.FDP;
import at.lux.retrieval.fdp.FDPParameters;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Arrays;
import java.lang.reflect.Array;

/**
 * Date: 14.01.2005
 * Time: 00:00:38
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Visualization2DPanelWithFdp extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {
    protected EmirConfiguration emirConfiguration = EmirConfiguration.getInstance();
    protected boolean demoMode = emirConfiguration.getBoolean("emir.demomode");
    private float points[][], initialPoints[][];
    private DistanceMatrix matrixFastmap;
    private float maxX = 0f, maxY = 0f, minX = 0f, minY = 0f;
    private java.util.List<String> fileList;
    private static final int OFFSET = 20;
    private java.util.List<BufferedImage> imageList;
    private final float IMG_MAXIMUM_SIDE = emirConfiguration.getFloat("MdsVisPanel.ImageLoader.MaxImageSideLength");
    private double thumbSizeModifier = 0.5;
    private int imagesLoaded = 0;
    private AffineTransform transform;

    private double moveX = 0d, moveY = 0d;
    private FdpThread fdpThread;

    private double zoom = 1d;

    private boolean showPleaseWait = false;
    private boolean showPleaseWaitAllowed = emirConfiguration.getBoolean("MdsVisPanel.ImageLoader.ShowPleaseWait");

    private boolean antiAliased = true;
    private ImageLoaderThread imageLoaderThread;

    private enum MouseState {
        NONE, BUTTON1_PRESSED
    }

    ;
    private MouseState state = MouseState.NONE;

    private Point lastPoint = null;

    private FDP fdp = null;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public Visualization2DPanelWithFdp(float[][] points, DistanceMatrix matrixFastmap, java.util.List<String> files, boolean autoStartFDP) {
        this.points = points;
        initialPoints = new float[points.length][points[0].length];

        for (int i = 0; i < points.length; i++) {
            float[] point = points[i];
            for (int j = 0; j < point.length; j++) {
                initialPoints[i][j] = point[j];
            }
        }

        this.matrixFastmap = matrixFastmap;
        fileList = files;
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        init();
        imageLoaderThread = new ImageLoaderThread(this);
        new Thread(imageLoaderThread).start();
        FDPParameters fdpParameters = EmirConfiguration.getInstance().getFDPParameters();
        fdpParameters.setGravitation(3d / Math.sqrt(points.length));
        fdp = new FDP(matrixFastmap, fdpParameters, points);
        if (autoStartFDP) {
            fdpThread = new FdpThread(fdp, this);
            fdpThread.start();
            antiAliased = false;
            showPleaseWait = true;
        }
    }

    protected void reinit() {
        for (int i = 0; i < points.length; i++) {
            float[] point = points[i];
            for (int j = 0; j < point.length; j++) {
                point[j] = initialPoints[i][j];
            }
        }
        init();
        imageLoaderThread.stopThread();
        imagesLoaded = 0;
        imageLoaderThread = new ImageLoaderThread(this);
        new Thread(imageLoaderThread).start();
        FDPParameters fdpParameters = EmirConfiguration.getInstance().getFDPParameters();
        fdpParameters.setGravitation(3d / Math.sqrt(points.length));
        fdp = new FDP(matrixFastmap, fdpParameters, points);
        fdpThread = new FdpThread(fdp, this);
        fdpThread.start();
        antiAliased = false;
        showPleaseWait = true;
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
     *
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
        maxX = getMax(0);
        maxY = getMax(1);
        minX = getMin(0);
        minY = getMin(1);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
//        if (true)
         if (antiAliased)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        else
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // save old transformations ...
        AffineTransform old = g2.getTransform();
        // erase background
        g2.setColor(Color.black);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.green);
        // Apply move
        transform = AffineTransform.getTranslateInstance(moveX, moveY);
        AffineTransform scale = AffineTransform.getScaleInstance(zoom, zoom);
        transform.concatenate(scale);

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
                int bimgWidth = (int) Math.round((double) bi.getWidth() * thumbSizeModifier);
                int bimgHeight = (int) Math.round((double) bi.getHeight() * thumbSizeModifier);
                g2.drawImage(bi, projectionX - bimgWidth / 2, projectionY - bimgHeight / 2, bimgWidth, bimgHeight, null);
            } else {
                g2.fillOval(projectionX - 2, projectionY - 2, 4, 4);
            }
        }
        // restore old transformations:
        g2.setTransform(old);
        g2.setFont(g2.getFont().deriveFont(Font.ITALIC, 8.5f));
        g2.setColor(Color.gray.brighter());
        g2.drawString("Click <alt> + <right mouse button> to start / stop FDP, zoom with mouse wheel.", 5, this.getHeight() - 5);

        if (showPleaseWait && showPleaseWaitAllowed) {
            Font bigFont = g2.getFont().deriveFont(Font.BOLD, 48f);
            FontMetrics fontMetrics = g2.getFontMetrics(bigFont);
            g2.setFont(bigFont);
            g2.setColor(Color.white);
            String pleaseWait = "Please Wait!";
            int pwWidth = (int) fontMetrics.getStringBounds(pleaseWait, g2).getWidth();
            int pwHeight = (int) fontMetrics.getStringBounds(pleaseWait, g2).getHeight();
            g2.drawString(pleaseWait, (getWidth() - pwWidth) / 2, (getHeight() - pwHeight) / 2);
//            showPleaseWait = false;
        }
//        g2.setColor(Color.white);
//        g2.drawString("Click <alt> + <left mouse button> to start / stop FDP.", 9, this.getHeight()-11);
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
        if (lastPoint == null) lastPoint = e.getPoint();
        if (state == MouseState.BUTTON1_PRESSED) {
            moveX += e.getPoint().x - lastPoint.x;
            moveY += e.getPoint().y - lastPoint.y;
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
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            state = MouseState.BUTTON1_PRESSED;
        } else if (e.getButton() == MouseEvent.BUTTON3 && e.isAltDown()) {
            if (fdpThread != null && fdpThread.isRunning()) {
                fdpThread.setRunning(false);
            } else {
                fdpThread = new FdpThread(fdp, this);
                showPleaseWait = true;
                antiAliased = false;
                repaint();
                fdpThread.start();
            }
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
        state = MouseState.NONE;
        lastPoint = null;
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

    /**
     * Invoked when the mouse wheel is rotated.
     *
     * @see java.awt.event.MouseWheelEvent
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getModifiersEx() == MouseEvent.ALT_DOWN_MASK)
            thumbSizeModifier += (double) e.getWheelRotation() / 10.0;
        else
            increaseZoom(((double) e.getWheelRotation()) / 10d);
        repaint();
    }

    /**
     * Sets the new zoom and recalculates the center of the zoom.
     *
     * @param amount
     */
    private void increaseZoom(double amount) {
        double zoomDifference = amount;
        // added max zoomout ...
        zoom = Math.max(zoom + amount, 0.01d);
        Point2D.Double centerPoint = new Point2D.Double(((double) (getWidth() >> 1)), ((double) (getHeight() >> 1)));
        AffineTransform transform = AffineTransform.getTranslateInstance(moveX, moveY);
        AffineTransform scale = AffineTransform.getScaleInstance(zoom, zoom);
        transform.concatenate(scale);

        try {
            centerPoint = (Point2D.Double) transform.inverseTransform(centerPoint, null);
            moveX = (moveX - (zoomDifference * centerPoint.getX()));
            moveY = (moveY - (zoomDifference * centerPoint.getY()));
        } catch (NoninvertibleTransformException e) {
            System.err.println("Error: " + e.toString());
        }

    }

    /*
    private void setZoomFactor() {
        int centerX = this.getWidth() / 2;
        int centerY = this.getHeight() / 2;
        Point2D p2 = new Point2D.Double(centerX, centerY);
        AffineTransform scaleTransformation = AffineTransform.getScaleInstance(zoom, zoom);
        AffineTransform translateTransformation = AffineTransform.getTranslateInstance(moveX, moveY);
        translateTransformation.concatenate(scaleTransformation);
        Point2D dp = null;
        double zoomDiff = zoomFactor;
        zoomFactor = singleZoomFactor*singleZoomFactor;
        zoomDiff = zoomFactor - zoomDiff;
        try {
            dp = translateTransformation.inverseTransform(p2, null);
            translateX = translateX - (zoomDiff * dp.getX());
            translateY = translateY - (zoomDiff * dp.getY());
        } catch (NoninvertibleTransformException e) {
            log.error(e.toString());
            e.printStackTrace();
        }
//        translate.setLocation(translate.x - (int)  , translate.y - (int) (v*centerY));
//        System.out.println("Zoomfactor: " + zoomFactor);
        repaint();
    }
    */

    private float getMax(int index) {
        float f = points[0][index];
        for (int i = 1; i < points.length; i++) {
            if (f < points[i][index]) f = points[i][index];
        }
        return f;
    }

    private float getMin(int index) {
        float f = points[0][index];
        for (int i = 1; i < points.length; i++) {
            if (f > points[i][index]) f = points[i][index];
        }
        return f;
    }

    public boolean isShowPleaseWait() {
        return showPleaseWait;
    }

    public void setShowPleaseWait(boolean showPleaseWait) {
        this.showPleaseWait = showPleaseWait;
    }

    public boolean isAntiAliased() {
        return antiAliased;
    }

    public void setAntiAliased(boolean antiAliased) {
        this.antiAliased = antiAliased;
    }

}

class ImageLoaderThread implements Runnable {
    private Visualization2DPanelWithFdp panel;
    private EmirConfiguration emirConfiguration = EmirConfiguration.getInstance();
    int imageLoaderStartWait = emirConfiguration.getInt("MdsVisPanel.ImageLoader.StartWait");
    int imageLoaderStepWait = emirConfiguration.getInt("MdsVisPanel.ImageLoader.StepWait");
    private boolean isRunning = true;

    public ImageLoaderThread(Visualization2DPanelWithFdp panel) {
        this.panel = panel;
    }

    public void run() {
        try {
            Thread.sleep(imageLoaderStartWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (panel.initNextImage() && isRunning) {
            try {
                Thread.sleep(imageLoaderStepWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isRunning = false;
    }

    public void stopThread() {
        isRunning = false;
    }

}

class FdpThread extends Thread {
    FDP fdp;
    JPanel panel;
    private EmirConfiguration emirConfiguration = EmirConfiguration.getInstance();
    private float STOP_CONDITION = emirConfiguration.getFloat("MdsVisPanel.FDP.StopCondition");
    private int fdpStepWait = emirConfiguration.getInt("MdsVisPanel.FDP.StepWait");
    private int fdpStartWait = emirConfiguration.getInt("MdsVisPanel.FDP.StartWait");

    private boolean running = false;
    private Visualization2DPanelWithFdp vPanel = null;

    public FdpThread(FDP fdp, JPanel panel) {
        this.fdp = fdp;
        this.panel = panel;
        init();
    }

    public FdpThread(FDP fdp, int fdpStepWait, JPanel panel, float stopAtMovement) {
        this.fdp = fdp;
        this.fdpStepWait = fdpStepWait;
        this.panel = panel;
        this.STOP_CONDITION = stopAtMovement;
        init();
    }

    public FdpThread(FDP fdp, int fdpStepWait, JPanel panel) {
        this.fdp = fdp;
        this.fdpStepWait = fdpStepWait;
        this.panel = panel;
        init();
    }

    private void init() {
        if (panel instanceof Visualization2DPanelWithFdp)
            vPanel = (Visualization2DPanelWithFdp) panel;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        running = true;
        float currentMovement = -100;
        try {
//            fdpStartWait = 1000;
            Thread.sleep(fdpStartWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 1000 && running; i++) {
            try {
                Thread.sleep(fdpStepWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fdp.step();
            if (vPanel != null) {
                vPanel.setShowPleaseWait(false);
            }
            if (currentMovement > 0 && (Math.abs(currentMovement - fdp.getCurrentMovement()) < STOP_CONDITION)) {
                System.out.println("Needed " + i + " steps for stable layout.");
                break;
            }
            currentMovement = fdp.getCurrentMovement();
//            System.out.println("Current movement: " + Math.abs(currentMovement-fdp.getCurrentMovement()));
            panel.repaint();
        }
//        System.out.println("No stable layout reached within time.");
        if (vPanel != null) {
            vPanel.setAntiAliased(true);
        }
        panel.repaint();
        running = false;
        if (vPanel.demoMode) {
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            vPanel.reinit();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }


}
