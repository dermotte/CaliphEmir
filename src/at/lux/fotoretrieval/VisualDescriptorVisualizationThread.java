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

import at.lux.components.ProgressWindow;
import at.lux.fotoretrieval.panels.Visualization2DPanelWithFdp;
import at.lux.imageanalysis.ColorLayout;
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.ScalableColor;
import at.lux.imageanalysis.JDomVisualDescriptor;
import at.lux.retrieval.fastmap.ArrayFastmapDistanceMatrix;
import at.lux.retrieval.fastmap.FastMap;
import at.lux.retrieval.fastmap.FastmapDistanceMatrix;
import at.lux.retrieval.fastmap.VisualDescriptorDistanceCalculator;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * SimilarImageSearchThread
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class VisualDescriptorVisualizationThread extends Thread {
    String dir;
    RetrievalFrame parent;
    JProgressBar progress;
    DecimalFormat df;
    JDomVisualDescriptor.Type type;
    Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    private String[] files = null;
    LinkedList<String> fileList;

    public VisualDescriptorVisualizationThread(String directory,
                                               RetrievalFrame frame, JProgressBar progress, JDomVisualDescriptor.Type type) {
        this.dir = directory;
        this.parent = frame;
        this.progress = progress;
        df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(2);
        this.type = type;

    }

    public VisualDescriptorVisualizationThread(String[] files,
                                               RetrievalFrame frame, JProgressBar progress, JDomVisualDescriptor.Type type) {
        this.dir = null;
        this.files = files;
        this.parent = frame;
        this.progress = progress;
        df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(2);
        this.type = type;

    }

    public void run() {
        File directory = null;
        if (dir != null) directory = new File(dir);
        if ((directory != null && directory.exists()) || (files != null && files.length>1) ) {
            parent.setEnabled(false);
            ProgressWindow pw;
            pw = new ProgressWindow(parent, progress);
            try {
                pw.pack();
                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                pw.setLocation((d.width - pw.getWidth()) / 2, (d.height - pw.getHeight()) / 2);
                pw.setVisible(true);
                long stime, ftime;
                stime = System.currentTimeMillis();
                parent.setStatus("Loading & Mapping");

                FastmapDistanceMatrix matrixFastmap;
                if (directory != null) {
                    matrixFastmap = createMatrixFromDirectory(directory, progress);
                } else {
                    matrixFastmap = createMatrix(progress, files);
                }
                FastMap fm = new FastMap(matrixFastmap, 2);
                long ms = System.currentTimeMillis();
                fm.run();
                ms = System.currentTimeMillis() - ms;
                System.out.println("Time for " + matrixFastmap.getDimension() + " images: " + ms + " ms");
                stime = System.currentTimeMillis() - stime;
                ftime = System.currentTimeMillis();
                matrixFastmap.normalize();
                Visualization2DPanelWithFdp panel = new Visualization2DPanelWithFdp(fm.getPoints(), matrixFastmap, fileList, false);
                parent.setStatus("Formatting results ...");
                ftime = System.currentTimeMillis() - ftime;
                parent.addVisualization(panel);
                parent.setStatus("Searched for " + df.format(stime / 1000.0) + " s, formatting lasted " + df.format(ftime / 1000.0) + " s");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                pw.setVisible(false);
                parent.setEnabled(true);
            }
        } else {
            JOptionPane.showMessageDialog(parent, "Chosen repositors directory does not exist.");
        }
    }

    private FastmapDistanceMatrix createMatrixFromDirectory(File directory, JProgressBar progress) throws IOException {
        String[] files;
        files = FileOperations.getAllDescriptions(directory, true);
        return createMatrix(progress, files);
    }

    private FastmapDistanceMatrix createMatrix(JProgressBar progress, String[] files) {
        LinkedList<ColorLayout> colorLayoutList = new LinkedList<ColorLayout>();
        LinkedList<ScalableColor> scalableColorList = new LinkedList<ScalableColor>();
        LinkedList<EdgeHistogram> edgeHistogramList = new LinkedList<EdgeHistogram>();
        LinkedList<String> clFileList = new LinkedList<String>();
        LinkedList<String> scFileList = new LinkedList<String>();
        LinkedList<String> ehFileList = new LinkedList<String>();

        progress.setMinimum(0);
        progress.setMaximum(files.length);
        progress.setValue(0);

        FastmapDistanceMatrix matrixFastmap = null;
        for (int i = 0; i < files.length; i++) {
            progress.setValue(i);
            String file = files[i];
            SAXBuilder builder = new SAXBuilder();
            try {
                Element e = builder.build(new FileInputStream(file)).getRootElement();
                java.util.List l = RetrievalToolkit.xpathQuery(e, "//VisualDescriptor", null);
                for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                    Element desc = (Element) iterator.next();
                    if (desc.getAttributeValue("type", xsi).equals("ColorLayoutType")) {
                        ColorLayout cl = new ColorLayout(desc);
                        colorLayoutList.add(cl);
                        clFileList.add(file);
                    } else if (desc.getAttributeValue("type", xsi).equals("ScalableColorType")) {
                        ScalableColor sc = new ScalableColor(desc);
                        scalableColorList.add(sc);
                        scFileList.add(file);
                    } else if (desc.getAttributeValue("type", xsi).equals("EdgeHistogramType")) {
                        try {
                            EdgeHistogram sc = new EdgeHistogram(desc);
                            edgeHistogramList.add(sc);
                            ehFileList.add(file);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            } catch (JDOMException e1) {
                System.err.println("Error in " + file + ": " + e1.toString());
            } catch (IOException e1) {
                System.err.println("Error in " + file + ": " + e1.toString());
            }
        }
        if (type == JDomVisualDescriptor.Type.ColorLayout) {
            matrixFastmap = new ArrayFastmapDistanceMatrix(colorLayoutList, new VisualDescriptorDistanceCalculator());
            fileList = clFileList;
        } else if (type == JDomVisualDescriptor.Type.ScalableColor) {
            matrixFastmap = new ArrayFastmapDistanceMatrix(scalableColorList, new VisualDescriptorDistanceCalculator());
            fileList = scFileList;
        } else if (type == JDomVisualDescriptor.Type.EdgeHistogram) {
            matrixFastmap = new ArrayFastmapDistanceMatrix(edgeHistogramList, new VisualDescriptorDistanceCalculator());
            fileList = ehFileList;
        }
        return matrixFastmap;
    }

}