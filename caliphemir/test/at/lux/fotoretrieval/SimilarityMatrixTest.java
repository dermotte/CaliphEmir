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
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.ScalableColor;
import at.lux.retrieval.calculations.SimilarityMatrix;
import at.lux.retrieval.clustering.HAC;
import at.lux.retrieval.fastmap.ArrayFastmapDistanceMatrix;
import at.lux.retrieval.fastmap.FastmapDistanceMatrix;
import at.lux.retrieval.fastmap.VisualDescriptorDistanceCalculator;
import junit.framework.TestCase;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Date: 13.01.2005
 * Time: 22:50:04
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimilarityMatrixTest extends TestCase {
    Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    public void testMatrix() throws IOException, JDOMException {
        String[] files;
        files = FileOperations.getAllDescriptions(new File("C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\example-data"), true);
//        files = FileOperations.getAllImages(new File("C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\testdaten"), true);
//        files = FileOperations.getAllImages(new File("C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\testdata"), true);
//        files = FileOperations.getAllImages(new File("C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph"), true);
        LinkedList<ColorLayout> colorLayoutList = new LinkedList<ColorLayout>();
        LinkedList<ScalableColor> scalableColorList = new LinkedList<ScalableColor>();
        LinkedList<EdgeHistogram> edgeHistogramList = new LinkedList<EdgeHistogram>();
        LinkedList<Date> dateList = new LinkedList<Date>();
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            SAXBuilder builder = new SAXBuilder();
            try {
                Element e = builder.build(file).getRootElement();
                List l = RetrievalToolkit.xpathQuery(e, "//VisualDescriptor", null);
                for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                    Element desc = (Element) iterator.next();
                    if (desc.getAttributeValue("type", xsi).equals("ColorLayoutType")) {
                        ColorLayout cl = new ColorLayout(desc);
                        colorLayoutList.add(cl);
                    } else if (desc.getAttributeValue("type", xsi).equals("ScalableColorType")) {
                        ScalableColor sc = new ScalableColor(desc);
                        scalableColorList.add(sc);
                    } else if (desc.getAttributeValue("type", xsi).equals("EdgeHistogramType")) {
                        try {
                            EdgeHistogram sc = new EdgeHistogram(desc);
                            edgeHistogramList.add(sc);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                l = RetrievalToolkit.xpathQuery(e, "//CreationCoordinates/Date/TimePoint", null);
                if (l.size()>0) {
                    Element date = (Element) l.get(0);
                    Calendar cal = Calendar.getInstance();
                    // 2004-06-30T10:05:48
                    String[] tmp = date.getTextTrim().split("T");
                    StringTokenizer st = new StringTokenizer(tmp[0], "-");
                    int year = Integer.parseInt(st.nextToken());
                    int month = Integer.parseInt(st.nextToken());
                    int day = Integer.parseInt(st.nextToken());
                    st = new StringTokenizer(tmp[1], ":");
                    int hour = Integer.parseInt(st.nextToken());
                    int minute = Integer.parseInt(st.nextToken());
                    int second = Integer.parseInt(st.nextToken());
                    cal.set(year, month, day, hour, minute, second);
                    dateList.add(cal.getTime());
                }
            } catch (JDOMException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        FastmapDistanceMatrix matrixFastmap;
//        matrixFastmap = new ArrayFastmapDistanceMatrix(colorLayoutList, new VisualDescriptorDistanceCalculator());
//        matrixFastmap = new ArrayFastmapDistanceMatrix(dateList, new DateDistanceCalculator());
//        matrixFastmap = new ArrayFastmapDistanceMatrix(edgeHistogramList, new VisualDescriptorDistanceCalculator());
        matrixFastmap = new ArrayFastmapDistanceMatrix(scalableColorList, new VisualDescriptorDistanceCalculator());
//        System.out.println(matrixFastmapFastmap.getSimilarity(0,1));
        assertTrue(matrixFastmap.getDistance(0, 0) == 0f);

        SimilarityMatrix matrix = matrixFastmap.getSimilarityMatrix();

//        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
//        df.setMaximumFractionDigits(2);
//        df.setMinimumIntegerDigits(2);
//        df.setMinimumFractionDigits(2);
//
//        for (int i = 0; i< matrixFastmapFastmap.getDimension(); i++) {
//            for (int j = 0; j<matrixFastmapFastmap.getDimension(); j++) {
//                System.out.print(df.format(matrixFastmapFastmap.getSimilarity(i,j)) + "\t");
//            }
//            System.out.println("");
//        }
//
        // checking if similarity matrix is well formed :)
        for (int i = 0; i<matrix.getDimension(); i++)
            for (int j = 0; j<matrix.getDimension(); j++) {
                if (i!=j) {
                    assert(matrix.getSimilarity(i,j)<=1f);
                    assert(matrix.getSimilarity(i,j)>=0f);
                } else {
                    assert(matrix.getSimilarity(i,j)==1f);
                }
            }
         // now go for the clustering :)
        System.out.println(matrix.toString());

        HAC hac = new HAC(matrix, 7);
        while(hac.step()>0);
        System.out.println("");
        System.out.println(hac.toString());

        hac = new HAC(matrix);
        while(hac.step()>0);
        System.out.println("");
//        System.out.println("Matrix dimension: " + matrix.getDimension());
        System.out.println(hac.toString());


        hac = new HAC(matrix, 5);
        while(hac.step()>0);
        System.out.println("");
        System.out.println(hac.toString());

        hac = new HAC(matrix, 4);
        while(hac.step()>0);
        System.out.println("");
        System.out.println(hac.toString());
    }

}
