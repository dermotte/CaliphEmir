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
package at.wklieber.gui.dominantcolor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DominantColorFinder extends Thread {
    private static boolean debug = true;

    private WritableRaster wr;
    private JProgressBar pb;
    private RGBColorPercentagePairList list;
    private DominantColorPlugin dialog;
    private JLabel status;
    private Point a, b;
    private int numberOfPixels;

    public DominantColorFinder(WritableRaster raster, JProgressBar progressBar, RGBColorPercentagePairList list, DominantColorPlugin dialog, JLabel status, Point a, Point b) {
        wr = raster;
        pb = progressBar;
        this.list = list;
        this.dialog = dialog;
        this.status = status;
        this.a = a;
        this.b = b;
        numberOfPixels = wr.getWidth() * wr.getHeight();
    }

    public DominantColorFinder(WritableRaster raster, RGBColorPercentagePairList list, Point a, Point b) {
        wr = raster;
        pb = null;
        this.list = list;
        this.dialog = null;
        this.status = null;
        this.a = a;
        this.b = b;
        numberOfPixels = wr.getWidth() * wr.getHeight();
    }


    public void run() {
        if (dialog != null) {
            dialog.setEnabled(false);
        }

        int[] pixels = new int[3];

        Hashtable ht = new Hashtable();
        Hashtable ct = new Hashtable();
        Vector v = new Vector();

        int temp = 0;       // temporary value
        int max = 1;       // peak
        int colorCount = 0;
        String px;

        double cols = (double) wr.getWidth();

        if (status != null) {
            status.setText("Examining pixels ...");
        }
        int sw, ew, sh, eh;
        if (a != null && b != null) {
            sw = a.x;
            ew = b.x;
            sh = a.y;
            eh = b.y;
            numberOfPixels = (sw - ew) * (sh - eh);
            cols = (double) (ew - sw);
        } else {
            sw = 0;
            ew = wr.getWidth();
            sh = 0;
            eh = wr.getHeight();
        }
        if (debug) System.out.println("counting colors from pixel (" + sw + "," + sh + ") to (" + ew + "," + eh + ")");
        for (int w = sw; w < ew; w++) {
            for (int h = sh; h < eh; h++) {
                wr.getPixel(w, h, pixels);
                px = pixels[0] + "," + pixels[1] + "," + pixels[2];
                if (!ht.containsKey(px)) {
                    ht.put(px, "1");
                } else {
                    temp = Integer.parseInt((String) ht.get(px)) + 1;
                    if (temp > max) max = temp;
                    ht.put(px, temp + "");
                }
            }
            if (pb != null) {
                int pbVal = (int) Math.round(100.0 * ((double) (w - sw)) / cols);
                if (pbVal < 0) pbVal = 0;
                if (pbVal > 100) pbVal = 100;
                pb.setValue(pbVal);
                pb.setString(pbVal + "%");
            }
        }
        if (pb != null) {
            pb.setValue(100);
            pb.setString("100%");
        }
        if (status != null) status.setText(ht.size() + " colors counted, peak is " + max);

        int[] counts = new int[ht.size()];
        int i = 0;

        Enumeration e = ht.elements();
        while (e.hasMoreElements()) {
            temp = Integer.parseInt((String) e.nextElement());
            counts[i] = temp;
            i++;
        }

        if (status != null) status.setText("Sorting elements ...");
        Arrays.sort(counts);
        int[] hitlist = new int[8];
        for (int ii = 0; ii < hitlist.length; ii++)
            hitlist[ii] = 0;

        if (counts.length > 0) hitlist[0] = counts[counts.length - 1];
        if (counts.length > 1) hitlist[1] = counts[counts.length - 2];
        if (counts.length > 2) hitlist[2] = counts[counts.length - 3];
        if (counts.length > 3) hitlist[3] = counts[counts.length - 4];
        if (counts.length > 4) hitlist[4] = counts[counts.length - 5];
        if (counts.length > 5) hitlist[5] = counts[counts.length - 6];
        if (counts.length > 6) hitlist[6] = counts[counts.length - 7];
        if (counts.length > 7) hitlist[7] = counts[counts.length - 8];

        e = ht.keys();

        if (status != null) status.setText("Generating result ...");
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            temp = Integer.parseInt((String) ht.get(key));
            if (!(temp < hitlist[7])) {
                double percent = 0.0;
                int[] myColor = new int[3];
                percent = ((double) temp) / ((double) (numberOfPixels));
                StringTokenizer st = new StringTokenizer(key, ",");
                myColor[0] = Integer.parseInt(st.nextToken());
                myColor[1] = Integer.parseInt(st.nextToken());
                myColor[2] = Integer.parseInt(st.nextToken());
                RGBColorPercentagePair cp = new RGBColorPercentagePair(myColor, percent);
                list.add(cp);
                // System.out.println("[" + key + "]:" + temp);
            }
        }
        list.setUpperLeftCorner(new Point(0, 0));
        list.setUpperLeftCorner(new Point(wr.getWidth(), wr.getHeight()));
        if (a != null && b != null) {
            list.setUpperLeftCorner(new Point(a));
            list.setUpperLeftCorner(new Point(b));
        }
        if (pb != null) pb.setString("Finished!");
        if (dialog != null) {
            dialog.setEnabled(true);
        }
        if (list.size() > 0) {
            RGBColorPercentagePair first = (RGBColorPercentagePair) list.get(0);
            if (status != null) status.setText("dominant: " + first.toString());
        }
        if (dialog != null) {
            dialog.hideIfToldToDo();
        }
    }


    public static void main(String[] args) {
        try {
            WritableRaster r = ImageIO.read(new File("testimage.jpg")).getRaster();
            RGBColorPercentagePairList list = new RGBColorPercentagePairList();
            DominantColorFinder f = new DominantColorFinder(r, list, new Point(0, 0), new Point(10, 10));
            f.run();
//            System.out.println(list.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
