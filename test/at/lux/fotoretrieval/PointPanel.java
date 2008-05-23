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
package at.lux.fotoretrieval;

import at.lux.imageanalysis.ColorLayout;
import at.lux.retrieval.calculations.DistanceMatrix;
import at.lux.retrieval.fdp.FDP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Date: 14.01.2005
 * Time: 00:00:38
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class PointPanel extends JPanel implements MouseListener {
    private float points[][];
    private float maxX, maxY, minX, minY;
    DistanceMatrix matrixFastmap;
    private FDP fdp;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public PointPanel(float[][] points, DistanceMatrix matrixFastmap) {
        this.points = points;
        this.matrixFastmap = matrixFastmap;
        fdp = new FDP(matrixFastmap, points);
        addMouseListener(this);
        initMinMax();
    }

    private void initMinMax() {
        maxX = getMax(0);
        maxY = getMax(1);
        minX = getMin(0);
        minY = getMin(1);

/*
        for (int i = 1; i < points.length; i++) {
            float[] point = points[i];
            if (point[0] > maxX) {
                maxX = point[0];
            } else if (point[0] < minX) {
                minX = point[0];
            }
            if (point[1] > maxY) {
                maxY = point[1];
            } else if (point[1] < minY) {
                minX = point[1];
            }
        }
*/
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
        initMinMax();
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        float width = this.getWidth();
        float height = this.getHeight();
        for (int i = 0; i < points.length; i++) {
            float[] point = points[i];
            float x = (point[0] - minX) / (maxX - minX);
            float y = (point[1] - minY) / (maxY - minY);
            Object cl = matrixFastmap.getUserObject(i);
            if (cl instanceof ColorLayout) {
                g2.drawImage(((ColorLayout) cl).getColorLayoutImage(), (int) (x * width), (int) (y * height), null);
            } else {
                g2.drawOval((int) (x * width), (int) (y * height), 3, 3);
            }
        }
    }


    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Thread t = new Thread(new FdpThread(fdp, this));
            t.start();
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

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

}

class FdpThread implements Runnable {
    FDP fdp;
    PointPanel panel;

    public FdpThread(FDP fdp, PointPanel panel) {
        this.fdp = fdp;
        this.panel = panel;
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
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            fdp.step();
            System.out.println(fdp.getCurrentMovement());
            panel.repaint();
        }
    }
}
