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

package at.wklieber.mpeg7;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.Namespace;

import at.wklieber.tools.Console;
import at.wklieber.tools.Java2dTools;


class IndexCoords {
    int i;
    double x;
    double y;

    public IndexCoords() {
        reset();
    }

    public void reset() {
        i = 0;
        x = 0;
        y = 0;
    }
}

class SortInd implements Comparator {
    public int compare(Object o1, Object o2) {
        //return (((const IndexCoords *)v1)->y <= ((const IndexCoords *)v2)->y) ? -1 : 1;

        IndexCoords v1 = (IndexCoords) o1;
        IndexCoords v2 = (IndexCoords) o2;
        return (v1.y <= v2.y ? -1 : 1);
    }

}

class
        Edge {
    int i;
    double x;
    double y;
    double dx;
    double dy;

    public Edge() {
        reset();
    }

    public void reset() {
        i = 0;
        x = 0;
        y = 0;
        dx = 0;
        dy = 0;
    }
}

class SortEdge implements Comparator {
    public int compare(Object o1, Object o2) {
        //return (((const Edge *)v1)->x <= ((const Edge *)v2)->x ? -1 : 1);

        Edge v1 = (Edge) o1;
        Edge v2 = (Edge) o2;
        return (v1.x <= v2.x ? -1 : 1);
    }

}


public class ContourShape {
    private static Console console = Console.getReference();
    private static Java2dTools java2dTools = Java2dTools.getReference();
    static final boolean debug = true;

    private static int BITS_TO_MASK(int a) {
        return ((2 << ((a) - 1)) - 1);
    }

    private static long BITS_TO_MASK(long a) {
        return ((2 << ((a) - 1)) - 1);
    }

    private static double CONTOURSHAPE_YP = 0.05;

    private static double CONTOURSHAPE_AP = 0.09;
    private static int CONTOURSHAPE_MAXCSS = 10;
    private static double CONTOURSHAPE_T = 0.000001;
    private static double CONTOURSHAPE_TXA0 = 3.8;
    private static double CONTOURSHAPE_TXA1 = 0.6;

    private static int CONTOURSHAPE_CSSPEAKBITS = 6;
    private static int CONTOURSHAPE_XBITS = 6;
    private static long CONTOURSHAPE_YBITS = 7;
    private static int CONTOURSHAPE_YnBITS = 3;
    private static int CONTOURSHAPE_CBITS = 6;
    private static int CONTOURSHAPE_EBITS = 6;

    private static double CONTOURSHAPE_ETHR = 0.6;
    private static double CONTOURSHAPE_CTHR = 1.0;
    private static double CONTOURSHAPE_ECOST = 0.4;
    private static double CONTOURSHAPE_CCOST = 0.3;

    private static double CONTOURSHAPE_NMATCHPEAKS = 2;
    private static double CONTOURSHAPE_TMATCHPEAKS = 0.9;

    private static double CONTOURSHAPE_XMAX = 1.0;
    private static double CONTOURSHAPE_YMAX = 1.7;
    private static double CONTOURSHAPE_CMIN = 12.0;
    private static double CONTOURSHAPE_CMAX = 110.0;
    private static double CONTOURSHAPE_EMIN = 1.0;
    private static double CONTOURSHAPE_EMAX = 10.0;

    private static int CONTOURSHAPE_CSSPEAKMASK = BITS_TO_MASK(CONTOURSHAPE_CSSPEAKBITS);
    private static int CONTOURSHAPE_XMASK = BITS_TO_MASK(CONTOURSHAPE_XBITS);
    private static long CONTOURSHAPE_YMASK = BITS_TO_MASK(CONTOURSHAPE_YBITS);
    private static int CONTOURSHAPE_YnMASK = BITS_TO_MASK(CONTOURSHAPE_YnBITS);
    private static int CONTOURSHAPE_CMASK = BITS_TO_MASK(CONTOURSHAPE_CBITS);
    private static long CONTOURSHAPE_EMASK = BITS_TO_MASK(CONTOURSHAPE_EBITS);


    private BufferedImage img;

    private BufferedImage regionShapeImage;

    char m_cPeaksCount; // all unsigned
    long[] m_piGlobalCurvatureVector = new long[2];
    long[] m_piPrototypeCurvatureVector = new long[2];
    long m_iHighestPeakY;
    long[] m_pPeak;


    /**
     * Create a ColorLayout Object from the given BufferedImage with the desired number of Coefficients
     *
     * @param image the input image
     */
    public ContourShape(BufferedImage image) {
        this.img = image;
        extract(img);
        init();
    }

    /**
     * Create a ColorLayout Object from the given BufferedImage with the desired number of Coefficients
     *
     * @param pointList List of Point()
     */
    public ContourShape(List pointList) {
        //this.img = image;
        init();
        extract(pointList);

    }


    /**
     * Create a ColorLayout Object from its descriptor
     *
     * @param descriptor the descriptor as JDOM Element
     */
    public ContourShape(Element descriptor) {
        this.img = null;

    }


    private void init() {
        regionShapeImage = null;
        //extract();
    }


    public Element getDescriptor() {
        Element returnVaue = null;

        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element vdesc = new Element("VisualDescriptor", mpeg7).setAttribute("type", "ContourShapeType", xsi);

        int num = m_cPeaksCount; //getNoOfPeaks();

        Element globalCurvatureElement = new Element("GlobalCurvature");
        vdesc.addContent(globalCurvatureElement);
        long[] lgcv; //= new long[2];
        lgcv = m_piGlobalCurvatureVector; //getGlobalCurvature();
        //vector<int> gcv(2);
        //gcv[0] = (int)lgcv[0];
        //gcv[1] = (int)lgcv[1];
        String gcv = lgcv[0] + " " + lgcv[1];
        globalCurvatureElement.setText(gcv);

        if (num > 0) {
            Element prototypeCurvatureElement = new Element("PrototypeCurvature");
            vdesc.addContent(prototypeCurvatureElement);
            long[] lpcv = new long[2];
            lpcv = m_piPrototypeCurvatureVector; //getPrototypeCurvature(lpcv[0], lpcv[1]);
            //vector<int> pcv(2);
            //pcv[0] = (int)lpcv[0];
            //pcv[1] = (int)lpcv[1];
            String pcv = lpcv[0] + " " + lpcv[1];
            prototypeCurvatureElement.setText(pcv);
        }

        Element highestPeakElement = new Element("HighestPeakY");
        vdesc.addContent(highestPeakElement);

        //getHighestPeakY()
        long peak = m_iHighestPeakY;
        highestPeakElement.setText("" + peak);

        for (int i = 1; i < num; i++) {
            //short xp, yp;

            Element peakElement = new Element("Peak");
            long[] point = getPeak(i);

            String xp = "" + point[0];
            String yp = "" + point[1];
            peakElement.setAttribute("peakX", xp);
            peakElement.setAttribute("peakY", yp);
        }


        returnVaue = vdesc;
        return vdesc;
    }

    private int getNoOfPeaks() {
        return m_cPeaksCount;
    }

    private void setNoOfPeaks(int cPeaks) {
        int cOldPeaks = m_cPeaksCount;

        // Only 5 bits used so mask out rest

        if (cPeaks > CONTOURSHAPE_CSSPEAKMASK)
            m_cPeaksCount = (char) CONTOURSHAPE_CSSPEAKMASK;
        else
            m_cPeaksCount = (char) cPeaks;

        // Manage peak memory
        if (m_cPeaksCount == cOldPeaks) {
            //if (m_pPeak != null) {
            //  m_pPeak = new short[0];
            //}

            m_pPeak = null;
            if (m_cPeaksCount > 0) {
                m_pPeak = new long[m_cPeaksCount * 2];
            }
        } else {
            //memset(m_pPeak, 0, m_cPeaksCount * sizeof(unsigned short) * 2);
            m_pPeak = new long[m_cPeaksCount * 2];
        }
    }


    private long[] getGlobalCurvature() {
        return m_piGlobalCurvatureVector;
    }


    private void setGlobalCurvature(long lC, long lE) {
        m_piGlobalCurvatureVector[0] = lC;
        m_piGlobalCurvatureVector[1] = lE;
    }


    private long getHighestPeakY() {
        return m_iHighestPeakY;
    }

    private void setHighestPeakY(long iHigh) {
        m_iHighestPeakY = iHigh;
    }

    void setPrototypeCurvature(long lC, long lE) {
        if (m_cPeaksCount > 0) {
            m_piPrototypeCurvatureVector[0] = lC;
            m_piPrototypeCurvatureVector[1] = lE;
        }
    }


    private long[] getPeak(int cIndex) {
        int cOffset = (cIndex * 2);
        long[] returnValue = new long[2];

        if (cIndex < m_cPeaksCount) {
            returnValue[0] = m_pPeak[cOffset];
            returnValue[1] = m_pPeak[cOffset + 1];
        } else {
            returnValue[0] = 0;
            returnValue[1] = 0;
        }

        return returnValue;
    }


    private void setPeak(int cIndex, long iX, long iY) {
        int cOffset = cIndex * 2;
        if (cIndex < m_cPeaksCount) {
            m_pPeak[cOffset] = iX;
            m_pPeak[cOffset + 1] = iY;
        }
    }

    /**
     * extract the shape from an image with white background and black shape
     *
     * @param image
     */
    public void extract(BufferedImage image) {
        throw new java.lang.UnsupportedOperationException("Method extract(BufferedImage) not yet implemented.");
        //long something = ExtractContour(10, new Point());
        // todo: call ExtractContor and next extract(List)
        //int nContour = ExtractContour(CONTOUR_SIZE, coords);
    }

    /**
     * extract the descrptor data
     *
     * @param pointList List of Point()
     */
    public void extract(List pointList) {
        //throw new java.lang.UnsupportedOperationException("Method extract(BufferedImage) not yet implemented.");

        /*
        Point2D.Double[] pointArray = new Point2D.Double[pointList.size()];
              pointArray =  (Point2D.Double[]) pointList.toArray(pointArray);
        extractCurvature(pointList.size(), pointArray);
        */

        extractPeaks(pointList);
    }

    public void importDescriptor(Element descriptor) {
        throw new java.lang.UnsupportedOperationException("Method importDescriptor() not yet implemented.");

        /*
      if (!aDescription)
        return (unsigned long)-1;

      GenericDS l_DDLDescription;
      GenericDSInterfaceABC *l_DDLDescriptionInterface = NULL;

      string xsitype;
      if (aDescription->GetDSName() == "Descriptor")
      {
        aDescription->GetTextAttribute("xsi:type", xsitype);
        if (xsitype == "ContourShapeType")
        {
          l_DDLDescriptionInterface = aDescription;
        }
      }

      if (!l_DDLDescriptionInterface)
      {
        l_DDLDescription = aDescription->GetDescription("Descriptor");

        while (!l_DDLDescription.isNull())
        {
          l_DDLDescription.GetTextAttribute("xsi:type", xsitype);
          if (xsitype == "ContourShapeType")
            break;
          l_DDLDescription = l_DDLDescription.GetNextSibling("Descriptor");
        }

        if (l_DDLDescription.isNull())
          return (unsigned long)-1;

        l_DDLDescriptionInterface = l_DDLDescription.GetInterface();
      }

      GenericDS GlobalCurvature_element = l_DDLDescriptionInterface->GetDescription("GlobalCurvature");
      vector<int> gcv;
      GlobalCurvature_element.GetIntVector(gcv);
      SetGlobalCurvature((unsigned long)gcv[0], (unsigned long)gcv[1]);

      GenericDS PrototypeCurvature_element = l_DDLDescriptionInterface->GetDescription("PrototypeCurvature");
      if (!PrototypeCurvature_element.isNull())
      {
        vector<int> pcv;
        PrototypeCurvature_element.GetIntVector(pcv);
        SetPrototypeCurvature((unsigned long)pcv[0], (unsigned long)pcv[1]);
      }
      else
        SetPrototypeCurvature(0, 0);

      GenericDS HighestPeak_element = l_DDLDescriptionInterface->GetDescription("HighestPeakY");
      int peak0;
      HighestPeak_element.GetIntValue(peak0);
      SetHighestPeakY(peak0);

      // Now that the 'numberOfPeaks' attribute has been removed, we need this
      // special case to check for the case where there are zero peaks.

      if (peak0 > 0)
      {
        // The SetNoOfPeaks() call must be done BEFORE reading the peak data, in
        // order to allocate the memory, so we step through the Peak elements
        // here first...

        int numPeaks = 1;
        GenericDS Peak_element = l_DDLDescriptionInterface->GetDescription("Peak");
        while (!Peak_element.isNull())
        {
          Peak_element = Peak_element.GetNextSibling("Peak");
          numPeaks++;
        }
        SetNoOfPeaks(numPeaks);

        // And now step through properly to actually read and store the data...

        int i = 1;
        Peak_element = l_DDLDescriptionInterface->GetDescription("Peak");
        while (!Peak_element.isNull())
        {
          int xp, yp;
          Peak_element.GetIntAttribute("peakX", xp);
          Peak_element.GetIntAttribute("peakY", yp);
          SetPeak(i++, (unsigned short)xp, (unsigned short)yp);
          Peak_element = Peak_element.GetNextSibling("Peak");
        }

        // And finally, set the zeroth peak (same as the highest one)

        SetPeak(0, 0, peak0);
      }

      // If the highest peak has a height of zero, this implies NO peaks

      else
        SetNoOfPeaks(0);

      return 0;
        */
    }


    /**
     * Takes two ColorLayout DS and calculates similarity.
     *
     * @return -1.0 if c1 or c2 does not contain a valid ColorLayout DS
     */
    public static double getSimilarity(Element c1, Element c2) {
        double val = -1.0;
        int YCoeff1, YCoeff2, CCoeff1, CCoeff2, YCoeff, CCoeff;
        return val;
    }

    /**
     * Takes two ColorLayout DS and calculates similarity.
     * @return Vector of int[] (yCoeff at Vector.get(0), cbCoeff at Vector.get(1), crCoeff cbCoeff at Vector.get(2)) or null if not valid ColorLayoutDS
     */
    /*
    public static Vector getCoeffs(Element descriptor) {
        Vector vals = null;
        int[] y,cb,cr;
        int numY = 0;
        int numC = 0;
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        boolean isValid = false;

        if (descriptor.getChild("YDCCoeff", mpeg7) != null && descriptor.getChild("CbDCCoeff", mpeg7) != null && descriptor.getChild("CrDCCoeff", mpeg7) != null) {
            isValid = true;
            numC = 1;
            numY = 1;
        }
        if (isValid) {
            String str_y, str_cb, str_cr;
        }

        return vals;
    }
    */

    /**
     * Takes two ColorLayout Coeff sets and calculates similarity.
     *
     * @return -1.0 if data is not valid.
     */
    public static double getSimilarity(int[] YCoeff1, int[] CbCoeff1, int[] CrCoeff1, int[] YCoeff2, int[] CbCoeff2, int[] CrCoeff2) {
        int numYCoeff1, numYCoeff2, CCoeff1, CCoeff2, YCoeff, CCoeff;


        return -1;
    }


    private BufferedImage YCrCb2RGB(int[][] rgbSmallImage) {
        BufferedImage br = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        WritableRaster r = br.getRaster();
        double rImage, gImage, bImage;
        int pixel[] = new int[3];

        for (int i = 0; i < 64; i++) {
            rImage = ((rgbSmallImage[0][i] - 16.0) * 256.0) / 219.0;
            gImage = ((rgbSmallImage[1][i] - 128.0) * 256.0) / 224.0;
            bImage = ((rgbSmallImage[2][i] - 128.0) * 256.0) / 224.0;

            pixel[0] = Math.max(0, (int) ((1.0 * rImage) + (1.402 * bImage) + 0.5)); //R
            pixel[1] = Math.max(0, (int) ((1.0 * rImage) + (-0.34413 * gImage) + (-0.71414 * bImage) + 0.5));  //G
            pixel[2] = Math.max(0, (int) ((1.0 * rImage) + (1.772 * gImage) + 0.5)); //B

            r.setPixel(i % 8, i / 8, pixel);
        }

        return br;
    }


    private long extractContour(int n, Point ishp) {
        throw new java.lang.UnsupportedOperationException("Method ExtractContour() not implemented");
        /*
      int dr[] = {  0, -1, -1, -1,  0,  1,  1,  1 };
      int dc[] = {  1,  1,  0, -1, -1, -1,  0,  1 };

      int size = 0;
      Point2 *xy = 0;
      MomImage *mask_chan;

      if (m_ImageMedia->a_chan) {
        mask_chan=m_ImageMedia->a_chan;
      }
      else {
        mask_chan=m_ImageMedia->y_chan;
      }
      for (unsigned int r = 0; (r < mask_chan->y) && (size == 0); r++)
      {
    #ifdef WHITE_ON_BLACK
        UChar lastPel = 0;
    #else // !WHITE_ON_BLACK
        UChar lastPel = 255;
    #endif // !WHITE_ON_BLACK
        for (unsigned int c = 0; (c < mask_chan->x) && (size == 0); c++)
        {
          UChar thisPel = *xydata(mask_chan, c, r);

          if (thisPel != lastPel)
          {
            UChar        dir = 0, dir0;
            unsigned int cr = r;
            unsigned int cc = c;
            UChar *p[8];

            do
            {
              p[0] = xydata(mask_chan, cc + 1, cr    );
              p[1] = xydata(mask_chan, cc + 1, cr - 1);
              p[2] = xydata(mask_chan, cc    , cr - 1);
              p[3] = xydata(mask_chan, cc - 1, cr - 1);
              p[4] = xydata(mask_chan, cc - 1, cr    );
              p[5] = xydata(mask_chan, cc - 1, cr + 1);
              p[6] = xydata(mask_chan, cc    , cr + 1);
              p[7] = xydata(mask_chan, cc + 1, cr + 1);

              int i;
              for (i = 0; i < 8; i++)
              {
    #ifdef WHITE_ON_BLACK
                if (p[(dir+3-i)&7] && (*p[(dir+3-i)&7] != 0))
                {
                  if (!p[(dir+4-i)&7])
                  {
                    dir = (dir+3-i)&7;
                    break;
                  }
                  else if (*p[(dir+4-i)&7] == 0)
                  {
                    dir = (dir+3-i)&7;
                    break;
                  }
                }
    #else // !WHITE_ON_BLACK
                if (p[(dir+3-i)&7] && (*p[(dir+3-i)&7] == 0))
                {
                  if (!p[(dir+4-i)&7])
                  {
                    dir = (dir+3-i)&7;
                    break;
                  }
                  else if (*p[(dir+4-i)&7] != 0)
                  {
                    dir = (dir+3-i)&7;
                    break;
                  }
                }
    #endif // !WHITE_ON_BLACK
              }

              if (i == 8)
                break;
              else if (size == 0)
                dir0 = dir;
              else if ((cr == r) && (cc == c) && (dir == dir0))
                break;

              if ((size % 32) == 0)
              {
                Point2 *nxy = new Point2[size+32];
                memset(nxy, 0, (size+32)*sizeof(Point2));
                if (size > 0)
                {
                  memcpy(nxy, xy, size*sizeof(Point2));
                  delete[] xy;
                }
                xy = nxy;
              }

              xy[size].x = cc;
              xy[size].y = cr;
              size++;

              cr += dr[dir]; cc += dc[dir];

            } while (1);
          }
        }
      }

      if (size == 0)
        return 0;

      double per = 0.0;
      double *dst = new double[size];
      for (int i1 = 0; i1 < size; i1++)
      {
        int i2 = (i1 == 0) ? (size-1) : (i1-1);
        double dx = xy[i1].x - xy[i2].x;
        double dy = xy[i1].y - xy[i2].y;
        dst[i1] = sqrt(dx*dx + dy*dy);
        per += dst[i1];
      }

      double del = per / (double)n;

      int cur = 0;
      double oldd = dst[cur];
      ishp[0] = xy[0];
      for (int j = 1; j < n; j++)
      {
        if (oldd > del)
        {
          double f = del / oldd;
          oldd -= del;
          int i1 = (cur < size-1) ? (cur + 1) : 0;
          double xs = f*(xy[i1].x - ishp[j-1].x);
          double ys = f*(xy[i1].y - ishp[j-1].y);
          ishp[j].x = ishp[j-1].x + xs;
          ishp[j].y = ishp[j-1].y + ys;
        }
        else
        {
          double newd = oldd + dst[++cur];
          while (newd < del)
            newd += dst[++cur];
          oldd = newd - del;
          double f = (dst[cur] - oldd) / dst[cur];
          int i1 = (cur < size-1) ? (cur + 1) : 0;
          double xs = f*(xy[i1].x - xy[cur].x);
          double ys = f*(xy[i1].y - xy[cur].y);
          ishp[j].x = xy[cur].x + xs;
          ishp[j].y = xy[cur].y + ys;
        }
      }

      delete[] dst;
      delete[] xy;

      return n;
        */
    }

    private void initPointArray(Point2D.Double[] pointArray) {
        if (pointArray != null) {
            for (int i = 0; i < pointArray.length; i++) {
                Point2D.Double point = pointArray[i];
                if (point == null) {
                    point = new Point2D.Double();
                    pointArray[i] = point;
                }
                point.x = 0;
                point.y = 0;
            }
        }
    }

    private void initIndexCoords(IndexCoords[] indexArray) {
        if (indexArray != null) {
            for (int i = 0; i < indexArray.length; i++) {
                IndexCoords index = indexArray[i];
                if (index == null) {
                    index = new IndexCoords();
                    indexArray[i] = index;
                }
                index.reset();
            }
        }
    }

    private void initEdgeArray(Edge[] edgeArray) {
        if (edgeArray != null) {
            for (int i = 0; i < edgeArray.length; i++) {
                Edge index = edgeArray[i];
                if (index == null) {
                    index = new Edge();
                    edgeArray[i] = index;
                }
                index.reset();
            }
        }
    }


    /**
     * main routine to extract the simularity vector
     *
     * @param pointList List of Point()
     * @return
     */
    private long extractPeaks(List pointList) {
        int n = pointList.size();

        Point2D.Double[] ishp = new Point2D.Double[n];
        ishp = (Point2D.Double[]) pointList.toArray(ishp);

        Point2D.Double[] fshp = new Point2D.Double[n]; // Point() list
        initPointArray(fshp);

        //List peaks = new Vector(CONTOURSHAPE_MAXCSS, 10);    // new Point2[CONTOURSHAPE_MAXCSS];
        Point2D.Double[] peaks = new Point2D.Double[CONTOURSHAPE_MAXCSS];
        initPointArray(peaks);
        int nPeaks = 0;

        Point2D.Double[] dxdy = new Point2D.Double[n];
        Point2D.Double[] d2xd2y = new Point2D.Double[n];
        Point2D.Double[] ang = new Point2D.Double[n];
        Point2D.Double[] fxfy = new Point2D.Double[n];

        initPointArray(dxdy);
        initPointArray(d2xd2y);
        initPointArray(ang);
        initPointArray(fxfy);

        double[] nMinima = new double[n];
        double[] nMaxima = new double[n];
        int nNmin = 0;
        int nNmax = 0;

        double[] oMinima = new double[n];
        double[] oMaxima = new double[n];
        int oNmin = 0;
        int oNmax = 0;

        for (int n1 = 0; n1 < n; n1++) {
            int n2 = (n1 > 0) ? (n1 - 1) : (n - 1);
            dxdy[n1].x = ishp[n1].x - ishp[n2].x;
            dxdy[n1].y = ishp[n1].y - ishp[n2].y;
        }

        int rec = 0, maxrec = (int) (0.262144 * n * n);
        do {
            if (nNmin > 0) {
                //memcpy(oMinima, nMinima, nNmin*sizeof(double));
                System.arraycopy(nMaxima, 0, oMaxima, 0, nNmin);
            }
            oNmin = nNmin;

            if (nNmax > 0) {
                //memcpy(oMaxima, nMaxima, nNmax*sizeof(double));
                System.arraycopy(nMaxima, 0, oMaxima, 0, nNmax);
            }
            oNmax = nNmax;

            ang[0].x = 0.0;
            d2xd2y[0].x = dxdy[0].x - dxdy[n - 1].x;
            d2xd2y[0].y = dxdy[0].y - dxdy[n - 1].y;
            double len = Math.sqrt(dxdy[0].x * dxdy[0].x + dxdy[0].y * dxdy[0].y);
            for (int i1 = 1; i1 < n; i1++) {
                ang[i1].x = len;
                d2xd2y[i1].x = dxdy[i1].x - dxdy[i1 - 1].x;
                d2xd2y[i1].y = dxdy[i1].y - dxdy[i1 - 1].y;
                len += Math.sqrt(dxdy[i1].x * dxdy[i1].x + dxdy[i1].y * dxdy[i1].y);
            }

            double ilen = 1.0 / len;
            for (int i2 = 0; i2 < n; i2++) {
                ang[i2].x *= ilen;
                ang[i2].y = dxdy[i2].x * d2xd2y[i2].y - dxdy[i2].y * d2xd2y[i2].x;
            }

            nNmin = nNmax = 0;
            double y0, x0;
            double x1 = ang[0].x;
            double y1 = ang[0].y;
            for (int j1 = 0; j1 < n; j1++) {
                int j1w = j1 + 1;
                while (j1w >= n) j1w -= n;
                x0 = x1;
                y0 = y1;
                x1 = ang[j1w].x;
                y1 = ang[j1w].y;
                if ((y0 < -CONTOURSHAPE_T) && (y1 >= -CONTOURSHAPE_T)) {
                    for (int j2 = j1 + 1; j2 < j1 + n; j2++) {
                        int j2w = j2;
                        while (j2w >= n) j2w -= n;
                        double y2 = ang[j2w].y;

                        if (y2 < -CONTOURSHAPE_T)
                            break;

                        if (y2 >= CONTOURSHAPE_T) {
                            double x2 = ang[j2w].x;
                            double dx = x2 - x0;
                            while (dx < 0.0) dx += 1.0;
                            double x = -y0 * dx / (y2 - y0) + x0;
                            while (x > 1.0) x -= 1.0;
                            nMinima[nNmin] = x;
                            nNmin++;
                            break;
                        }
                    }
                }
                if ((y0 >= CONTOURSHAPE_T) && (y1 < CONTOURSHAPE_T)) {
                    for (int j2 = j1 + 1; j2 < j1 + n; j2++) {
                        int j2w = j2;
                        while (j2w >= n) j2w -= n;
                        double y2 = ang[j2w].y;

                        if (y2 >= CONTOURSHAPE_T)
                            break;

                        if (y2 < -CONTOURSHAPE_T) {
                            double x2 = ang[j2w].x;
                            double dx = x2 - x0;
                            while (dx < 0.0) dx += 1.0;
                            double x = -y0 * dx / (y2 - y0) + x0;
                            while (x > 1.0) x -= 1.0;
                            nMaxima[nNmax] = x;
                            nNmax++;
                            break;
                        }
                    }
                }
            }

            for (int f1 = 0; f1 < n; f1++) {
                int f0 = (f1 > 0) ? (f1 - 1) : (n - 1);
                int f2 = (f1 < n - 1) ? (f1 + 1) : 0;
                fxfy[f1].x = 0.25 * (dxdy[f0].x + 2.0 * dxdy[f1].x + dxdy[f2].x);
                fxfy[f1].y = 0.25 * (dxdy[f0].y + 2.0 * dxdy[f1].y + dxdy[f2].y);
            }
            //memcpy(dxdy, fxfy, n*sizeof(Point2));
            System.arraycopy(fxfy, 0, dxdy, 0, n);

            if ((nNmin < oNmin) && (nNmax < oNmax) &&
                    (oNmin <= (CONTOURSHAPE_MAXCSS)) &&
                    (oNmax <= (CONTOURSHAPE_MAXCSS))) {
                for (int m1 = 0; m1 < nNmin; m1++) {
                    int idx = 0;
                    double diff = 9999.9;
                    for (int k1 = 0; k1 < oNmin; k1++) {
                        double d = Math.abs(nMinima[m1] - oMinima[k1]);
                        if (d > 0.5) d = 1.0 - d;
                        if (d < diff) {
                            idx = k1;
                            diff = d;
                        }
                    }
                    oNmin--;
                    if (idx < oNmin)
                    //memmove(&oMinima[idx], &oMinima[idx+1], (oNmin-idx)*sizeof(double));
                        System.arraycopy(oMinima, (idx + 1), oMinima, idx, (oNmin - idx));
                }

                for (int m2 = 0; m2 < nNmax; m2++) {
                    int idx = 0;
                    double diff = 9999.9;
                    for (int k1 = 0; k1 < oNmax; k1++) {
                        double d = Math.abs(nMaxima[m2] - oMaxima[k1]);
                        if (d > 0.5) d = 1.0 - d;
                        if (d < diff) {
                            idx = k1;
                            diff = d;
                        }
                    }
                    oNmax--;
                    if (idx < oNmax) {
                        //memmove(&oMaxima[idx], &oMaxima[idx+1], (oNmax-idx)*sizeof(double));
                        System.arraycopy(oMaxima, (idx + 1), oMaxima, idx, (oNmax - idx));
                    }
                }

                while (oNmin == 0) {
                    int idx = 0;
                    double diff = 9999.9;
                    for (int m3 = 0; m3 < oNmax; m3++) {
                        double d = Math.abs(oMaxima[m3] - oMinima[0]);
                        if (d > 0.5) d = 1.0 - d;
                        if (d < diff) {
                            idx = m3;
                            diff = d;
                        }
                    }

                    double x = 0.5 * (oMinima[0] + oMaxima[idx]);
                    if (Math.abs(oMinima[0] - oMaxima[idx]) > 0.5) {
                        if (x > 0.5)
                            x -= 0.5;
                        else
                            x += 0.5;
                    }

                    int xidx = 0;
                    diff = Math.abs(ang[0].x - x);
                    if (diff > 0.5) diff = 1.0 - diff;
                    for (int l1 = 1; l1 < n; l1++) {
                        double d = Math.abs(ang[l1].x - x);
                        if (d > 0.5) d = 1.0 - d;
                        if (d < diff) {
                            diff = d;
                            xidx = l1;
                        }
                    }

                    //memmove(&peaks[1], &peaks[0], (CONTOURSHAPE_MAXCSS-1)*sizeof(peaks[0]));
                    System.arraycopy(peaks, 0, peaks, 1, (CONTOURSHAPE_MAXCSS - 1));

                    peaks[0].x = (double) xidx;
                    peaks[0].y = rec;
                    if (nPeaks < CONTOURSHAPE_MAXCSS)
                        nPeaks++;

                    if (--oNmin == 0) {
                        //memmove(&oMinima[0], &oMinima[1], oNmin*sizeof(oMinima[0]));
                        System.arraycopy(oMinima, 1, oMinima, 0, (oNmin));
                    }

                    if (--oNmax > idx) {
                        //memmove(&oMaxima[idx], &oMaxima[idx+1], (oNmax-idx)*sizeof(oMaxima[0]));
                        System.arraycopy(oMaxima, (idx + 1), oMaxima, idx, (oNmax - idx));
                    }
                }
            }

            rec++;

        } while ((rec < maxrec) && (nNmin > 0) && (nNmax > 0));

        oMaxima = null;
        oMinima = null;
        nMaxima = null;
        nMinima = null;
        fxfy = null;
        ang = null;

        double xc = 0.0;
        double yc = 0.0;
        double len = 0;
        for (int s1 = 0; s1 < n; s1++) {
            len += Math.sqrt(dxdy[s1].x * dxdy[s1].x + dxdy[s1].y * dxdy[s1].y);
            xc += dxdy[s1].x;
            yc += dxdy[s1].y;
            fshp[s1].x = xc;
            fshp[s1].y = yc;
        }

        double nsmap = 1.0 / ((double) n * (double) n);
        for (int p1 = 0; p1 < nPeaks; p1++) {
            double pl = 0.0;
            for (int p2 = 0; p2 < peaks[p1].x; p2++)
                pl += Math.sqrt(dxdy[p2].x * dxdy[p2].x + dxdy[p2].y * dxdy[p2].y);
            peaks[p1].x = pl / len;
            peaks[p1].y = CONTOURSHAPE_TXA0 * Math.pow(peaks[p1].y * nsmap, CONTOURSHAPE_TXA1);
        }

        d2xd2y = null;
        dxdy = null;

        double offset = peaks[0].x;
        for (int p2 = 0; p2 < nPeaks; p2++) {
            peaks[p2].x -= offset;
            if (peaks[p2].x < 0.0)
                peaks[p2].x += 1.0;
        }

        if (peaks[0].y < CONTOURSHAPE_AP)
            nPeaks = 0;

        for (int p3 = 0; p3 < nPeaks; p3++) {
            if (peaks[p3].y < CONTOURSHAPE_YP * peaks[0].y)
                nPeaks = p3;
        }

        setNoOfPeaks(nPeaks);

        if (nPeaks == 0) {
            setHighestPeakY(0);
        }

        double py = 0;
        for (int i = 0; i < nPeaks; i++) {
            long qx = 0; //unsigned
            long qy = 0;
            qx = (int) ((int) ((CONTOURSHAPE_XMASK * peaks[i].x / CONTOURSHAPE_XMAX) + 0.5) & CONTOURSHAPE_XMASK);
            if (i == 0) {
                //unsigned
                long qyl = (long) ((CONTOURSHAPE_YMASK * peaks[i].y / CONTOURSHAPE_YMAX) + 0.5);
                if (qyl > CONTOURSHAPE_YMASK)
                    qy = CONTOURSHAPE_YMASK;
                else
                    qy = (int) (qyl & CONTOURSHAPE_YMASK);
                py = (qy * CONTOURSHAPE_YMAX / (double) CONTOURSHAPE_YMASK);
            } else {
                //unsigned
                long qyl = (long) ((CONTOURSHAPE_YnMASK * peaks[i].y / py) + 0.5);
                if (qyl > CONTOURSHAPE_YnMASK)
                    qy = CONTOURSHAPE_YnMASK;
                else
                    qy = (int) (qyl & CONTOURSHAPE_YnMASK);
                py = (qy * py / (double) CONTOURSHAPE_YnMASK);
            }
            setPeak(i, qx, qy);
            if (i == 0) {
                setHighestPeakY(qy);
            }
        }

        //unsigned
        long[] qce = new long[2];//qe, qc;
        qce = extractCurvature(n, ishp);//, qc, qe);
        setGlobalCurvature(qce[0], qce[1]);

        if (nPeaks > 0) {
            qce = extractCurvature(n, fshp); //, qce[0], qce[1]);
            setPrototypeCurvature(qce[0], qce[1]);
        }

        peaks = null;
        fshp = null;

        return nPeaks;
    }

    private long[] extractCurvature(int n, Point2D.Double[] shp) {
        long[] returnValue = new long[2]; // long qc, long qe

        long qc;
        long qe;

        double ecc = 0.0, cir = 0.0;

        //IndexCoords *ind = new IndexCoords[n];
        IndexCoords[] ind = new IndexCoords[n];
        initIndexCoords(ind);

        double x1 = shp[0].x, x2 = shp[0].x;
        for (int k0 = 0; k0 < n; k0++) {
            if (shp[k0].x < x1) x1 = shp[k0].x;
            if (shp[k0].x > x2) x2 = shp[k0].x;
            ind[k0].i = k0;
            ind[k0].x = shp[k0].x;
            ind[k0].y = shp[k0].y;
        }



        //qsort(ind, n, sizeof(ind[0]), compare_ind);
        java.util.Arrays.sort(ind, new SortInd());

        double y1 = ind[0].y, y2 = ind[n - 1].y;

        int iw = (int) (x2 - x1 + 1);
        int ih = (int) (y2 - y1 + 1);
        int[] xy = new int[iw * ih];
        //memset(xy, 0, iw*ih*sizeof(UChar));

        int nedge = 0;
        Edge[] edgelist = new Edge[n];
        initEdgeArray(edgelist);

        int ybot = (int) Math.ceil(y1 - 0.5);
        int ytop = (int) Math.floor(y2 - 0.5);
        if (ybot - (int) y1 < 0) ybot = (int) y1;
        if (ytop - (int) y1 >= ih) ytop = ih - 1 + (int) y1;

        int k1 = 0;
        for (int y = ybot; y <= ytop; y++) {
            for (; (k1 < n) && (ind[k1].y < (y + 0.5)); k1++) {
                int i1 = ind[k1].i;
                int i0 = (i1 < n - 1) ? i1 + 1 : 0;
                int i2 = (i1 > 0) ? i1 - 1 : n - 1;

                if (shp[i0].y <= y - 0.5) {
                    int e;
                    for (e = 0; (e < nedge) && (edgelist[e].i != i1); e++) ;
                    if (e < nedge) {
                        //memmove(&edgelist[e], &edgelist[e+1], (nedge-e)*sizeof(edgelist[0]));
                        System.arraycopy(edgelist, (e + 1), edgelist, e, (nedge - e));
                        nedge--;
                    }
                } else if (shp[i0].y >= y + 0.5) {
                    edgelist[nedge].i = i1;
                    edgelist[nedge].dx = (shp[i1].x - shp[i0].x) / (shp[i1].y - shp[i0].y);
                    edgelist[nedge].x = edgelist[nedge].dx * (y - shp[i0].y) + shp[i0].x;
                    nedge++;
                }

                if (shp[i2].y <= y - 0.5) {
                    int e;
                    for (e = 0; (e < nedge) && (edgelist[e].i != i2); e++) ;
                    if (e < nedge) {
                        //memmove(&edgelist[e], &edgelist[e+1], (nedge-e)*sizeof(edgelist[0]));
                        System.arraycopy(edgelist, (e + 1), edgelist, e, (nedge - e));
                        nedge--;
                    }
                } else if (shp[i2].y >= y + 0.5) {
                    edgelist[nedge].i = i2;
                    edgelist[nedge].dx = (shp[i1].x - shp[i2].x) / (shp[i1].y - shp[i2].y);
                    edgelist[nedge].x = edgelist[nedge].dx * (y - shp[i2].y) + shp[i2].x;
                    nedge++;
                }
            }

            //qsort(edgelist, nedge, sizeof(edgelist[0]), compare_edges);
            java.util.Arrays.sort(edgelist, new SortEdge());

            for (int s = 0; s < nedge - 1; s += 2) {
                int xl = (int) Math.ceil(edgelist[s].x) - (int) x1;
                if (xl < 0) xl = 0;
                int xr = (int) Math.floor(edgelist[s + 1].x) - (int) x1;
                if (xr >= iw) xr = iw - 1;
                int yl = (int) (y - (int) y1);
                for (int f = xl; f <= xr; f++)
                    xy[f + yl * iw] = 255;

                edgelist[s].x += edgelist[s].dx;
                edgelist[s + 1].x += edgelist[s + 1].dx;
            }
        }

        double perim = Math.sqrt((shp[0].x - shp[n - 1].x) * (shp[0].x - shp[n - 1].x) +
                (shp[0].y - shp[n - 1].y) * (shp[0].y - shp[n - 1].y));
        for (int p = 1; p < n; p++) {
            perim += Math.sqrt((shp[p].x - shp[p - 1].x) * (shp[p].x - shp[p - 1].x) +
                    (shp[p].y - shp[p - 1].y) * (shp[p].y - shp[p - 1].y));
        }

        double vol = 0;
        double meanx = 0.0;
        double meany = 0.0;

        for (int vy = 0; vy < ih; vy++) {
            for (int vx = 0; vx < iw; vx++) {
                if (xy[vx + vy * iw] == 0) {
                    meanx += vx;
                    meany += vy;
                    vol++;
                }
            }
        }
        meanx /= vol;
        meany /= vol;

        double i11 = 0.0, i20 = 0.0, i02 = 0.0;
//  double rad = 0.0, co = 0.0;
        for (int ey = 0; ey < ih; ey++) {
            for (int ex = 0; ex < iw; ex++) {
                if (xy[ex + ey * iw] == 0) {
                    i11 += (ex - meanx) * (ey - meany);
                    i20 += (ex - meanx) * (ex - meanx);
                    i02 += (ey - meany) * (ey - meany);
                }
            }
        }

        double temp1 = (i20 + i02);
        double temp2 = Math.sqrt(i20 * i20 + i02 * i02 - 2.0 * i02 * i20 + 4.0 * i11 * i11);

        cir = perim * perim / vol;
        ecc = Math.sqrt((temp1 + temp2) / (temp1 - temp2));

        ind = null;
        edgelist = null;
        xy = null;

        if (ecc >= CONTOURSHAPE_EMAX)
            qe = CONTOURSHAPE_EMASK;
        else if (ecc < CONTOURSHAPE_EMIN)
            qe = 0;
        else
            qe = (long) ((long) (((CONTOURSHAPE_EMASK + 1) * (ecc - CONTOURSHAPE_EMIN)
                    / (CONTOURSHAPE_EMAX - CONTOURSHAPE_EMIN))) & CONTOURSHAPE_EMASK);

        if (cir >= CONTOURSHAPE_CMAX)
            qc = CONTOURSHAPE_CMASK;
        else if (cir < CONTOURSHAPE_CMIN)
            qc = 0;
        else
            qc = (long) (((long) ((CONTOURSHAPE_CMASK + 1) * (cir - CONTOURSHAPE_CMIN)
                    / (CONTOURSHAPE_CMAX - CONTOURSHAPE_CMIN))) & CONTOURSHAPE_CMASK);

        returnValue[0] = qc;
        returnValue[1] = qe;

        return returnValue;
    }


    private static void debug(String message) {
        if (debug) System.out.println("[ColorLayout] " + message);
    }

    /*
    EXAMPLE:
    ========
    <VisualDescriptor xsi:type="ColorLayoutType">
        <YDCCoeff>12</YDCCoeff>
        <CbDCCoeff>2</CbDCCoeff>
        <CrDCCoeff>2</CrDCCoeff>
        <YACCoeff5>1 1 1 1 1</YACCoeff5>
        <CbACCoeff2>2 2</CbACCoeff2>
        <CrACCoeff2>2 2</CrACCoeff2>
    </VisualDescriptor>

    */


    public static void main(String[] args) {
        try {
            List pointListStar3 = new Vector();
            pointListStar3.add(new Point2D.Double(290, 439));
            pointListStar3.add(new Point2D.Double(312, 308));
            pointListStar3.add(new Point2D.Double(425, 238));
            pointListStar3.add(new Point2D.Double(300, 285));
            pointListStar3.add(new Point2D.Double(183, 222));
            pointListStar3.add(new Point2D.Double(286, 306));

            //console.line();
            //console.echo(CollectionTools.printCollectionContent(pointListStar3));
            pointListStar3 = java2dTools.normalizeCoordinate(pointListStar3);
            //console.line();
            //console.echo(CollectionTools.printCollectionContent(pointListStar3));
            ContourShape cl = new ContourShape(pointListStar3);
            Element desc1 = cl.getDescriptor();
            //console.line();
            //todo: check this, jdom changed

            //new XMLOutputter("  ", true).output(desc1, System.out);
            //new XMLOutputter().output(desc1, System.out);

            //console.echo("");

            List pointListBox = new Vector();
            pointListStar3.add(new Point2D.Double(30, 30));
            pointListStar3.add(new Point2D.Double(200, 30));
            pointListStar3.add(new Point2D.Double(200, 100));
            pointListStar3.add(new Point2D.Double(30, 100));
            ContourShape c2 = new ContourShape(pointListStar3);
            Element desc2 = cl.getDescriptor();
            //console.line();
            //todo: check this, jdom changed
            //new XMLOutputter("  ", true).output(desc1, System.out);
            //new XMLOutputter().output(desc1, System.out);
            //console.echo("");
            //console.line();

            //Element desc2 = new ContourShape(64, 64, ImageIO.read(new FileInputStream("test2.jpg"))).getDescriptor();
            //Element desc3 = new ContourShape(64, 64, ImageIO.read(new FileInputStream("test3.jpg"))).getDescriptor();

            //System.out.println("Similarity test1 test2: " + ContourShape.getSimilarity(desc1, desc2));
            //System.out.println("Similarity test1 test3: " + ContourShape.getSimilarity(desc1, desc3));
            //System.out.println("Similarity test2 test3: " + ContourShape.getSimilarity(desc2, desc3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}