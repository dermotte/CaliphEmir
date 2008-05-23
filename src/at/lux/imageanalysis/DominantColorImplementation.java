/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
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
 */
package at.lux.imageanalysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/*
* DominantColorLogic.java
*
* Created on 25. June 2004, 07:15 and updated until 04. November 2004
* Author: Wolfgang Seiringer, 002521, w.seiringer@utanet.at for the vizir-project (visual information retrieval project)
* (http://vizir.ims.tuwien.ac.at) project of the IMS (Interactive Media Systems Group) www.ims.tuwien.ac.at, at the TU Wien
* www.tuwien.ac.at
*/

public class DominantColorImplementation {

    private int imageHeight;
    private int imageWidth;
    private int imsize;
    //--------------------------------------------------------//

    // Holding the actual image to process:
    private BufferedImage bufferedImage = null;

    private int SC_BIT = 5;
    //    private final char quantImageAlpha = 255;
    private static final int FLT_MAX = 1000000;
    private float DSTMIN = (float) 255.0;
    private float SPLFCT = (float) 0.10;
    private int DCNMAX = 8;
    private double EPSGLOB = 0.01; //Global stopping criteria, see extract method
    private double EPSSPLT = 0.02; //Criteria for splitting the colorbins
    private double VARTHR = 50.0;
    private int m_Coherency; //saves the spatical coherency

    //    private double dist, distold = FLT_MAX, distnew, eps = 1.0;
    private int j;

    int m_MaxSize = DCNMAX;
    int m_CurrSize = 1;

    private float[] m_Weights = new float[m_MaxSize]; //Saves the Percentages of the Dominant Colors
    private float[][] m_Centroids = new float[m_MaxSize][3]; //Saves the values of the Domiant Colors
    //      private int[] closest = new int[imsize]; //Is used for Clustering, see Method Cluster
    private int[] closest; //Is used for Clustering, see Method Cluster

    private int component0, component1, component2;
    private int bin_number0, bin_number1, bin_number2;

    // Variables for Saving the Values extracted from the XML-String
    public int DominantSize = 0; //Saves the "Size" extracted from the XML-String
    public int spatcoher = 0; //Saves the "SpatialCoherency" extracted from the XML-String
    public int[] Percentage = null; //Saves the "Percentage" extracted from the XML-String
    public int[][] ColorValueIndex = null; //Saves the "ColorValueIndex" extracted from the XML-String
    //-----------------

    //----- Variables used for the dominant color parameters
    private int VariancePresent = 0;
    private int SpatialCoherency = 0;
    private int ColorSpacePresentInt = 0;
    private String ColorSpacePresentStr = "0";
    private int ColorQuantizationPresent = 0;


    private int[][] icnts_f;
    private int[] iwgts_f;
    private float[][] m_Variances = new float[m_MaxSize][3];
    private int[] interleavedArray;

    //---------------------------------------------------//

    public DominantColorImplementation() {

    }


    public DominantColorValues extractDescriptor(BufferedImage image) {
        this.imageHeight = image.getHeight();
        this.imageWidth = image.getWidth();
        this.imsize = this.imageHeight * this.imageWidth;
        this.closest = new int[imsize];
        this.bufferedImage = image;

        this.extractFeature();

        DominantColorValues dc = new DominantColorValues();

        dc.size = m_CurrSize;
        dc.coherency = m_Coherency;
        dc.percentages = new int[dc.size];
        dc.colorValues = new int[dc.size][3];
        for (int i = 0; i < dc.size; i++) {
            dc.percentages[i] = iwgts_f[i];
            dc.colorValues[i][0] = icnts_f[i][0];
            dc.colorValues[i][1] = icnts_f[i][1];
            dc.colorValues[i][2] = icnts_f[i][2];
        }

        return dc;
    }

    /**
     * Use this extraction routine if the pixel data was taken from
     * a non rectangular region. Please note that the neighbourhood
     * of pixels cannot be taken into account (No reliable spatial
     * coherency can be created). The pixels array goes like:
     * {pixel1[0], pixel1[1], pixel1[2], pixel2[0], pixel2[1], ...}
     *
     * @param pixels gives the pixels one int after another
     * @return the extracted data.
     */
    public DominantColorValues extractDescriptor(int[] pixels) {
        this.imageHeight = pixels.length / 3;
        this.imageWidth = 1;
        this.imsize = pixels.length / 3;
        this.closest = new int[imsize];
        this.bufferedImage = null;
        this.interleavedArray = pixels;

        this.extractFeature();

        DominantColorValues dc = new DominantColorValues();

        dc.size = m_CurrSize;
        dc.coherency = m_Coherency;
        dc.percentages = new int[dc.size];
        dc.colorValues = new int[dc.size][3];
        for (int i = 0; i < dc.size; i++) {
            dc.percentages[i] = iwgts_f[i];
            dc.colorValues[i][0] = icnts_f[i][0];
            dc.colorValues[i][1] = icnts_f[i][1];
            dc.colorValues[i][2] = icnts_f[i][2];
        }

        return dc;
    }

    /**
     * Makes some initial Operations and calls the Main Extracting-Method
     * <p/>
     * First some Variables are set for the further work.
     * Then the input RBG-Values are converted into the necessary CIE-LUV values, the method rgb2luv is used for conversion.
     * After the need CIE-LUV values are availabe we can call the method EXTRACT, this method is responsible for clustering, splitting
     * calculating centroids,... and also puts the calculated Bins with color, perentage in an array for outputting.
     */
    public void extractFeature() {

        int[] RGB;
        float[] LUV;

        if (bufferedImage != null) {
            RGB = interleave(imsize, this.imageWidth, this.imageHeight);
        } else {
            RGB = interleavedArray;
        }

        LUV = new float[3 * imsize];

        rgb2luv(RGB, LUV, 3 * imsize); //Calculate the needed LUV-Values for the further operations

        char quantImageAlpha = 255;
        Extract(LUV, imsize, quantImageAlpha);//Calls the Main Extracting Method

    }//End Method extractFeature

//------------------------------- RGB to LUV konversation -----------------------------------

    /**
     * This method takes the Input-RGB Values and calculates the corresponding CIELUV Values.
     * <p/>
     * The LUV-Values are used during the DominantColors, after extracting the Dominant Colors,
     * the CIELUV Values are converted from CIELUV to RGB
     */
    private static void rgb2luv(int[] RGB, float[] LUV, int size) {

        int i;
        double x, y, X, Y, Z, den, u2, v2, X0, Z0, Y0, u20, v20, r, g, b;

        X0 = (0.607 + 0.174 + 0.201);
        Y0 = (0.299 + 0.587 + 0.114);
        Z0 = (0.066 + 1.117);

        u20 = 4d * X0 / (X0 + 15d * Y0 + 3d * Z0);
        v20 = 9d * Y0 / (X0 + 15d * Y0 + 3d * Z0);

        for (i = 0; i < size; i += 3) {
            if (RGB[i] <= 20)
                r = (double) (8.715e-4 * RGB[i]);
            else
                r = (double) Math.pow((RGB[i] + 25.245) / 280.245, 2.22);

            if (RGB[i + 1] <= 20)
                g = (double) (8.715e-4 * RGB[i + 1]);
            else
                g = (double) Math.pow((RGB[i + 1] + 25.245) / 280.245, 2.22);

            if (RGB[i + 2] <= 20)
                b = (double) (8.715e-4 * RGB[i + 2]);
            else
                b = (double) Math.pow((RGB[i + 2] + 25.245) / 280.245, 2.22);

            X = 0.412453 * r + 0.357580 * g + 0.180423 * b;
            Y = 0.212671 * r + 0.715160 * g + 0.072169 * b;
            Z = 0.019334 * r + 0.119193 * g + 0.950227 * b;

            if (X == 0.0 && Y == 0.0 && Z == 0.0) {
                x = 1.0 / 3.0;
                y = 1.0 / 3.0;
            } else {
                den = X + Y + Z;
                x = X / den;
                y = Y / den;
            }

            den = -2 * x + 12 * y + 3;
            u2 = 4 * x / den;
            v2 = 9 * y / den;

            if (Y > 0.008856)
                LUV[i] = (float) (116 * Math.pow(Y, 1.0 / 3.0) - 16);
            else
                LUV[i] = (float) (903.3 * Y);
            LUV[i + 1] = (float) (13 * LUV[i] * (u2 - u20));
            LUV[i + 2] = (float) (13 * LUV[i] * (v2 - v20));
        }
    }// End Method RGBtoLUV

// --------------------- LUV to RGB conversion------------------

    /**
     * Converts the LUV-Values in the corresponding RGB Values (Range 256)
     */
    private static void luv2rgb(int[] RGB, float[] LUV, int size) {
        int i, k;
        double x, y, X, Y, Z, den, u2, v2, X0, Z0, Y0, u20, v20;
        double[] vec = new double[3];

        X0 = (0.607 + 0.174 + 0.201);
        Y0 = (0.299 + 0.587 + 0.114);
        Z0 = (0.066 + 1.117);


        u20 = 4 * X0 / (X0 + 15 * Y0 + 3 * Z0);
        v20 = 9 * Y0 / (X0 + 15 * Y0 + 3 * Z0);

        for (i = 0; i < size; i += 3) {
            if (LUV[i] > 0) {
                if (LUV[i] < 8.0)
                    Y = ((double) LUV[i]) / 903.3;
                else
                    Y = Math.pow((((double) LUV[i]) + 16) / 116.0, 3.0);
                u2 = ((double) LUV[i + 1]) / 13.0 / ((double) LUV[i]) + u20;
                v2 = ((double) LUV[i + 2]) / 13.0 / ((double) LUV[i]) + v20;

                den = 6 + 3 * u2 - 8 * v2;
                x = 4.5 * u2 / den;
                y = 2.0 * v2 / den;

                X = x / y * Y;
                Z = (1 - x - y) / y * Y;
            } else {
                X = 0.0;
                Y = 0.0;
                Z = 0.0;
            }

            vec[0] = (3.240479 * X - 1.537150 * Y - 0.498536 * Z);
            vec[1] = (-0.969256 * X + 1.875992 * Y + 0.041556 * Z);
            vec[2] = (0.055648 * X - 0.204043 * Y + 1.057311 * Z);
            for (k = 0; k < 3; k++) {
                if (vec[k] <= 0.018)
                    vec[k] = 255 * 4.5 * vec[k];
                else
                    vec[k] = 255 * (1.099 * Math.pow(vec[k], 0.45) - 0.099);
                if (vec[k] > 255)
                    vec[k] = 255;
                else if (vec[k] < 0) vec[k] = 0;
                RGB[i + k] = Math.round((float) vec[k]);
            }
        }
    }

    /**
     * This method controlls the extracting.
     * <p/>
     * The Method is responsible for the correct number of iterations and calls the submethod which calculate the
     * required values.
     * It loops trough the mthods cluster, calculate centroids and calculates the distortion.
     * imdata      -> color value of input image ( LUV color space, linear array)
     * imsize      -> total image size ( width * height)
     * m_MaxSize   -> Maximun DominantColor Number (initial value DCNMAX = 8)
     * m_CurrSize  -> Current(final) DominantColor Number (int)
     * m_Weights   -> Percentage of DominantColor (float)
     * m_Centroids -> ColorValue of DominantColor (LUV color space, float)
     * m_Variances -> ColorVariance of DominantColor (float)
     * iwgts       -> Percentage of DominantColor (int)
     * icnts       -> ColorValue of DominantColor (RGB color space, int)
     * m_Coherency -> Spatial Coherency of DominantColor
     */
    private void Extract(float[] imdata, int imsize, char quantImageAlpha) {

        double dist, distold = FLT_MAX, distnew, eps = 1.0, tmp;
        float aglfct = DSTMIN;
        float splfct = SPLFCT;
        int i, k;

        m_MaxSize = DCNMAX;
        m_CurrSize = 1;

        for (i = 0; i < m_MaxSize; i++) {

            m_Weights[i] = (float) 0.0;

            m_Centroids[i][0] = (float) 0.0;
            m_Centroids[i][1] = (float) 0.0;
            m_Centroids[i][2] = (float) 0.0;

            m_Variances[i][0] = (float) 0.0;
            m_Variances[i][1] = (float) 0.0;
            m_Variances[i][2] = (float) 0.0;
        }


        i = 0;

        distnew = Cluster(closest, imdata, imsize, quantImageAlpha);

        while (eps > EPSGLOB) {

            //find centroids
            Centroids(closest, imdata, imsize, quantImageAlpha);

            //classify bins
            distnew = Cluster(closest, imdata, imsize, quantImageAlpha);

            // calculate total distortion
            if (distold > 0.0)
                eps = (distold - distnew) / distold;
            else
                eps = 0.0;
            distold = distnew;

            //decide on splitting
            if (i == 0 || ((eps < EPSSPLT) && (m_CurrSize < m_MaxSize))) {

                Split(closest, imdata, imsize, splfct, quantImageAlpha);
                distnew = Cluster(closest, imdata, imsize, quantImageAlpha);
                eps = 1.0;
            }

            // check for identical codevectors
            for (j = 0; j < m_CurrSize; j++) {
                for (k = 0; k < j; k++) {
                    dist = 0.0;
                    tmp = m_Centroids[j][0] - m_Centroids[k][0];
                    dist += tmp * tmp;
                    tmp = m_Centroids[j][1] - m_Centroids[k][1];
                    dist += tmp * tmp;
                    tmp = m_Centroids[j][2] - m_Centroids[k][2];
                    dist += tmp * tmp;
                    if (dist == 0.0)
                        System.out.println("WARNING: two identical codevectors " + j + ", " + k);
                }
            }

            i++;

        }
//  System.out.println("Extract: iterations finished " + i);

        // merging using agglomerative clustering
        Agglom(aglfct);

        // calculate variances and normalise
        distnew = Cluster(closest, imdata, imsize, quantImageAlpha);
        Centroids(closest, imdata, imsize, quantImageAlpha);
        distnew = Cluster(closest, imdata, imsize, quantImageAlpha);

        if (this.VariancePresent != 0) {
            Vars(closest, imdata, imsize, quantImageAlpha);
        }

        for (j = 0; j < m_CurrSize; j++)
            m_Weights[j] /= imsize;

        // quantise and set descriptor members
        int[] iwgts = new int[m_CurrSize];

        int[][] icnts = new int[3 * m_CurrSize][3 * m_CurrSize];
        for (i = 0; i < m_CurrSize; i++)
            icnts[i] = new int[3];

        int[][] ivars = new int[3 * m_CurrSize][3 * m_CurrSize];
        for (i = 0; i < m_CurrSize; i++)
            ivars[i] = new int[3];

        for (i = 0; i < m_CurrSize; i++) {
            iwgts[i] = (int) (31.9999 * m_Weights[i]);
            luv2rgb(icnts[i], m_Centroids[i], 3);
        }

        for (i = 0; i < m_CurrSize; i++) {
            for (int j = 0; j < 3; j++)
                ivars[i][j] = (m_Variances[i][j] > VARTHR) ? 1 : 0;
        }

        // calculate spatial coherency
        if (this.SpatialCoherency != 0) {
            m_Coherency = GetSpatialCoherency(imdata, 3, m_CurrSize, m_Centroids, quantImageAlpha);
        } else
            m_Coherency = 0;

        String colspcpres = this.ColorSpacePresentStr;
//        int colspcpresInt = this.ColorSpacePresentInt;
        int colquantpres = this.ColorQuantizationPresent;

        int max_h;
//        float conversionmatrix;

        component0 = 0;
        component1 = 1;
        component2 = 2; // default RGB

        bin_number0 = 32;
        bin_number1 = 32;
        bin_number2 = 32; // default 5 bit

        max_h = 256;

        if (colspcpres != "0") {

            int[] in = new int[25];

//            int[] res = in;

            for (i = 0; i < m_CurrSize; i++) {
                in[12] = icnts[i][1];
                in[12] = icnts[i][2];
                in[12] = icnts[i][0];
            }
        }

        if (colquantpres != 0) {
            bin_number0 = this.GetBinNumberByComponent(component0);
            bin_number1 = this.GetBinNumberByComponent(component1);
            bin_number2 = this.GetBinNumberByComponent(component2);
        }

        for (i = 0; i < m_CurrSize; i++) {
            icnts[i][0] = icnts[i][0] * bin_number0 / max_h;
            icnts[i][1] = icnts[i][1] * bin_number1 >> 8;
            icnts[i][2] = icnts[i][2] * bin_number2 >> 8;
        }

        //Save values for output
//        String ColorSpacePresent = colspcpres;

//        int ColorQuantizationPresen = colquantpres;

//      *****************************************************************
// This output is used for testing the outputvalues, can be removed
//      System.out.println("size:                      " + m_CurrSize);
//
//      System.out.println("SpatialCoherency:          " + m_Coherency);

        for (int we = 0; we < m_CurrSize; we++) {
//      System.out.println("Percentage: " + iwgts[we]);
//      System.out.println("Values: " + icnts[we][0] + " " + icnts[we][1] + " " + icnts[we][2]);
//      System.out.println("------------------------------------------------");
        }
//      ********************************************************************

        icnts_f = icnts;
        iwgts_f = iwgts;

    } //end method extract

//---------------------------------------------------------------------------

    /**
     * Each color is asign a cluster
     */
    private double Cluster(int[] closest, float[] imdata, int imsize, char quantImageAlpha) {

        int i, j, jmin;
        double d1, d2, d3, dist, distmin, disttot = 0.0;
        float[] im1 = imdata;
//        float[] im2 = imdata, im3 = imdata;
        int imsize_msk;
//        char pAlpha;

        // now we cluster
        imsize_msk = 0;

        for (i = 0; i < imsize; i++) {
            jmin = 0;
            distmin = FLT_MAX;

            for (j = 0; j < m_CurrSize; j++) {
                d1 = im1[3 * i] - m_Centroids[j][0];
                d2 = im1[3 * i + 1] - m_Centroids[j][1];
                d3 = im1[3 * i + 2] - m_Centroids[j][2];

                dist = d1 * d1 + d2 * d2 + d3 * d3;
                if (dist < distmin) {
                    jmin = j;
                    distmin = dist;
                }
            }

            closest[i] = jmin;
            if (closest[i] != 0) disttot += distmin;
            imsize_msk++;

        }
        return disttot / imsize_msk;
    } // end method cluster
//----------------------------------Ende Cluster-----------------------------------------

    /**
     * Calculates the centroid of the colorcluster
     */
//----------------------------------Begin Centroid-----------------------------------------
    private void Centroids(int[] closest, float[] imdata, int imsize, char quantImageAlpha) {
        int i, j;
        double weight;
        float[] im1 = imdata;
//        float[] im2 = imdata, im3 = imdata;
//        char pAlpha;

        //set the centroids values to 0
        for (j = 0; j < m_CurrSize; j++) {
            m_Weights[j] = (float) 0.0;
            m_Centroids[j][0] = (float) 0.0;
            m_Centroids[j][1] = (float) 0.0;
            m_Centroids[j][2] = (float) 0.0;
        }

        for (i = 0; i < imsize; i++) {
            int ii = closest[i];
            m_Weights[ii]++;

            m_Centroids[ii][0] += im1[3 * i];
            m_Centroids[ii][1] += im1[3 * i + 1];
            m_Centroids[ii][2] += im1[3 * i + 2];
        }
        for (j = 0; j < m_CurrSize; j++) {
            weight = m_Weights[j];

            if (weight != 0.0) {
                m_Centroids[j][0] /= weight;
                m_Centroids[j][1] /= weight;
                m_Centroids[j][2] /= weight;
            } else {
                System.out.println("WARNING: zero weight for colour " + j);
            }
        }

    } //end method centroids
//------------------------- END CENTROIDS --------------------------------------------------

//---------------------------------------------------------------------------

    /**
     * Calculates the Total Distortion, the distorion is further on needed for deciding if we should split the colorbins or not.
     */
/*
    private double Dist(int[] closest, float[] imdata, int imsize, char quantImageAlpha) {
        int i, j;
        double d1, d2, d3, dist = 0.0;

        float[] im1 = imdata;
        int imsize_msk;

//        char pAlpha;

        imsize_msk = 0;

        for (i = 0; i < imsize; i++) {

            j = closest[i];
            d1 = im1[3 * i] - m_Centroids[j][0];
            d2 = im1[3 * i + 1] - m_Centroids[j][1];
            d3 = im1[3 * i + 2] - m_Centroids[j][2];

            dist += (d1 * d1) + (d2 * d2) + (d3 * d3);
            imsize_msk++;
        }

        return dist / imsize_msk;
    } //end method dist
*/

//---------------------------------------------------------------------------
    private void Vars(int[] closest, float[] imdata, int imsize, char quantImageAlpha) {
        int i, j;
        double tmp;

//        char pAlpha;

        //set the m_Variances Values to 0
        for (i = 0; i < m_CurrSize; i++) {
            m_Variances[i][0] = (float) 0.0;
            m_Variances[i][1] = (float) 0.0;
            m_Variances[i][2] = (float) 0.0;
        }

        for (i = 0; i < imsize; i++) {
            j = closest[i];
            tmp = imdata[3 * i] - m_Centroids[j][0];
            m_Variances[j][0] += tmp * tmp;
            tmp = imdata[3 * i + 1] - m_Centroids[j][1];
            m_Variances[j][1] += tmp * tmp;
            tmp = imdata[3 * i + 2] - m_Centroids[j][2];
            m_Variances[j][2] += tmp * tmp;
        }

        //Normalise values
        for (j = 0; j < m_CurrSize; j++) {
            m_Variances[j][0] /= m_Weights[j];
            m_Variances[j][1] /= m_Weights[j];
            m_Variances[j][2] /= m_Weights[j];
        }

    } // end method vars
//---------------------------------------------------------------------------

    /**
     * Splits the ColorBins, this means that one colorbin is split in to two new colorbins and the old colorbin is  discarded
     */
    private void Split(int[] closest, float[] imdata, int imsize, double factor, char quantImageAlpha) {

        int i, j, jmax = 0;
        double d1, d2, d3, diff1, diff2, diff3;

        double[] d1s = new double[8];
        double[] d2s = new double[8];
        double[] d3s = new double[8];
        double[] dists = new double[8];
        double distmax = 0.0;

//        char pAlpha;

        //set distortion values to 0
        for (j = 0; j < m_CurrSize; j++) {
            d1s[j] = 0.0;
            d2s[j] = 0.0;
            d3s[j] = 0.0;
            dists[j] = 0.0;
        }

        for (i = 0; i < imsize; i++) {
            j = closest[i];
            d1 = imdata[3 * i] - m_Centroids[j][0];
            d2 = imdata[3 * i + 1] - m_Centroids[j][1];
            d3 = imdata[3 * i + 2] - m_Centroids[j][2];
            d1s[j] += d1 * d1;
            d2s[j] += d2 * d2;
            d3s[j] += d3 * d3;
        }

        for (j = 0; j < m_CurrSize; j++) {
            dists[j] = d1s[j] + d2s[j] + d3s[j];
            d1s[j] /= m_Weights[j];
            d2s[j] /= m_Weights[j];
            d3s[j] /= m_Weights[j];
        }

        for (j = 0; j < m_CurrSize; j++)
            if (dists[j] > distmax) {
                jmax = j;
                distmax = dists[j];
            }

        diff1 = factor * Math.sqrt(d1s[jmax]);
        diff2 = factor * Math.sqrt(d2s[jmax]);
        diff3 = factor * Math.sqrt(d3s[jmax]);

        m_Centroids[m_CurrSize][0] = (float) (m_Centroids[jmax][0] + diff1);
        m_Centroids[m_CurrSize][1] = (float) (m_Centroids[jmax][1] + diff2);
        m_Centroids[m_CurrSize][2] = (float) (m_Centroids[jmax][2] + diff3);

        m_Centroids[jmax][0] = (float) (m_Centroids[jmax][0] - diff1);
        m_Centroids[jmax][1] = (float) (m_Centroids[jmax][1] - diff2);
        m_Centroids[jmax][2] = (float) (m_Centroids[jmax][2] - diff3);

        m_CurrSize++;

    } // end method Split

//----------------- END SPLIT -------------------------------------

//------------------Begin Agglom---------------------------------------------------------

    /**
     * Merging the bins using the "Agglomerative Clusterint Method"
     *
     * @param distthr ... some parameter :-)
     */
    private void Agglom(double distthr) {

        double d1, d2, d3, distmin = 0.0;
        double[][] dists = new double[8][8];
        double w1min, w2min;
        int ja, jb = 0, jamin, jbmin;

        do {

            for (ja = 0; ja < m_CurrSize; ja++)
                for (jb = 0; jb < ja; jb++) {
                    d1 = m_Centroids[ja][0] - m_Centroids[jb][0];
                    d2 = m_Centroids[ja][1] - m_Centroids[jb][1];
                    d3 = m_Centroids[ja][2] - m_Centroids[jb][2];
//                    dists[ja][jb] = d1 * d1 + d2 * d2 + d3 * d3;

                    /* START: added by janine to circumvent divisions by 0 */
                    //Distanz > 0, nur wenn weight nicht 0 ist!
                    if (m_Weights[ja] > 0.0 && m_Weights[jb] > 0.0)
                        dists[ja][jb] = (d1 * d1 + d2 * d2 + d3 * d3);
                    else
                        dists[ja][jb] = 0.0;
                    /* END: added by janine to circumvent divisions by 0 */

                }

            distmin = FLT_MAX;
            jamin = 0;
            jbmin = 0;
            for (ja = 0; ja < m_CurrSize; ja++)
                for (jb = 0; jb < ja; jb++)
                    if (dists[ja][jb] < distmin) {
                        distmin = dists[ja][jb];
                        jamin = ja;
                        jbmin = jb;
                    }

            if (distmin > distthr)
                break;


            w1min = m_Weights[jamin];
            w2min = m_Weights[jbmin];

            // 'if' eingefuegt von Janine (division durch 0)
            if (w1min + w2min > 0) {
                m_Centroids[jbmin][0] = (float) ((w1min * m_Centroids[jamin][0] + w2min * m_Centroids[jbmin][0]) / (w1min + w2min));
                m_Centroids[jbmin][1] = (float) ((w1min * m_Centroids[jamin][1] + w2min * m_Centroids[jbmin][1]) / (w1min + w2min));
                m_Centroids[jbmin][2] = (float) ((w1min * m_Centroids[jamin][2] + w2min * m_Centroids[jbmin][2]) / (w1min + w2min));
            }
            m_Weights[jbmin] += w1min;
            m_CurrSize--;


            for (jb = jamin; jb < m_CurrSize; jb++) {
                m_Weights[jb] = m_Weights[jb + 1];
                m_Centroids[jb][0] = m_Centroids[jb + 1][0];
                m_Centroids[jb][1] = m_Centroids[jb + 1][1];
                m_Centroids[jb][2] = m_Centroids[jb + 1][2];
                m_Variances[jb][0] = m_Variances[jb + 1][0];
                m_Variances[jb][1] = m_Variances[jb + 1][1];
                m_Variances[jb][2] = m_Variances[jb + 1][2];
            }

        } while (m_CurrSize > 1 && distmin < distthr);

    } // end method aglom

//----------------------------END Agglom-----------------------------------------------

//------------------------- Get Spatial Coherency ---------------------------------------------------

    /**
     * Extracts the spatial coherency
     */
    private int GetSpatialCoherency(float[] ColorData, int dim, int N, float[][] col_float, char quantImageAlpha) {

//        char pAlpha;
        double CM = 0.0;
        int NeighborRange = 1;

        float SimColorAllow = (float) Math.sqrt(DSTMIN);
        boolean[] IVisit = new boolean[imsize];

        for (int x = 0; x < (imsize); x++) {
            IVisit[x] = false;
        }

        int All_Pixels = 0;

        {

            for (int x = 0; x < (imsize); x++)
                if (IVisit[x] == false) All_Pixels++;

        }

        {
            for (int i = 0; i < N; i++) {

                int Corres_Pixels = 0;
                double Coherency = GetCoherencyWithColorAllow(ColorData, dim, IVisit, col_float[i][0], col_float[i][1], col_float[i][2],
                        SimColorAllow, NeighborRange, Corres_Pixels++);

                CM += (Coherency * (double) Corres_Pixels) / 2.7;

            }
        }
        return QuantizeSC(CM);
    }//end method get spatial coherency
//---------------------------------- ENDe GET spazial conherency-----------------------------------------

//--------------------------------- GET Coherency with Color Allow ---------------------------------

    private double GetCoherencyWithColorAllow(float[] ColorData, int dim, boolean[] IVisit,

                                              float l, float u, float v,
                                              float Allow, int NeighborRange, int OUTPUT_Corres_Pixel_Count) {
        int count, i, j;
        int Neighbor_Count = 0;

        int Pixel_Count = 0;
        double Coherency = 0.0;


        int width = imageWidth;


        int height = imageHeight;
        int ISize = imsize * dim;

        for (count = 0; count < ISize; count += dim) {
            i = (count / dim) % width; //width
            j = (count / dim) / width; //height

            float l1, u1, v1;
            l1 = ColorData[count];
            u1 = ColorData[count + 1];
            v1 = ColorData[count + 2];


            double distance;

            distance = Math.sqrt(Math.pow(l - l1, 2) + Math.pow(u - u1, 2) + Math.pow(v - v1, 2));

            if ((distance < Allow) && (IVisit[count / dim] == false))//no overlap checking
            {
                IVisit[count / dim] = true;
                Pixel_Count++;
                int nSameNeighbor = 0;
                for (int y = j - NeighborRange; y <= j + NeighborRange; y++) {
                    for (int x = i - NeighborRange; x <= i + NeighborRange; x++) {
                        if (!((i == x) && (j == y))) {
                            int Index = (y * width + x) * dim;
                            if ((Index >= 0) && (Index < ISize)) {
                                float l2 = ColorData[Index];
                                float u2 = ColorData[Index + 1];
                                float v2 = ColorData[Index + 2];
                                distance = Math.sqrt(Math.pow((l - l2), 2) + Math.pow((u - u2), 2) + Math.pow((v - v2), 2));
                                if (distance < Allow) {
                                    nSameNeighbor++;
                                }
                            }
                        }
                    }
                }
                Neighbor_Count += nSameNeighbor;
            }
        }

        OUTPUT_Corres_Pixel_Count = Pixel_Count;

        int neighbor_check_window_size = (NeighborRange << 1) + 1;
        neighbor_check_window_size *= neighbor_check_window_size;

        if (Pixel_Count == 0)
            Coherency = 0.0;
        else
            Coherency = (double) Neighbor_Count / (double) Pixel_Count / (double) (neighbor_check_window_size - 1);

        return Coherency;
    }// method get Coherency with Color Allow

//-----------------------------END get coherency with color allow ----------------------------------------------


    /**
     * Takes the input RGB-Matrix and converts it into an array, withe 3*imagesize
     */
    private int[] interleave(int size, int width, int height) {
        int[] pixelarray = new int[3 * size];
        int j = 0;
        WritableRaster raster = bufferedImage.getRaster();
        int[] pixel = new int[3];
        for (int i = 0; i < width; i++) { //row
            for (int ii = 0; ii < height; ii++) {//column
                raster.getPixel(i, ii, pixel);
                pixelarray[3 * j] = pixel[0];
                pixelarray[3 * j + 1] = pixel[1];
                pixelarray[3 * j + 2] = pixel[2];
                j++;
            }
        }
        return pixelarray;
    }

    private int QuantizeSC(double sc) {
        if (sc < 0.70)
            return 1;
        else
            return (int) ((sc - 0.70) / (1.0 - 0.70) * (Math.pow(2.0, (double) SC_BIT) - 3.0) + .5) + 2;
    }

    private int GetBinNumberByComponent(int component) {
        int i;
        int NumComponents = 3;
        int[] m_Component = new int[3];
        int[] m_BinNumber = new int[3];

        int ColorSpace = this.ColorSpacePresentInt;
        String ColorSpaceStr = this.ColorSpacePresentStr;

        if (ColorSpaceStr == "Monochrome") NumComponents = 1;         // monochrome (0101)
        if (ColorSpace == 5) NumComponents = 1;                                 // monochrome (0101)

        for (i = 0; i < NumComponents; i++)
            if (m_Component[i] == component) return m_BinNumber[i];

        return -1;
    }

//---------------------------------------------------------------------------
// now some methods which are used to get and set die parameters for the extraction ->  ColorQuantizationPresent,
// VariancePresent, SpatialCoherency, ColorSpacePresent

//gets the parameter for Varinace

    public int getVariance() {
        return (this.VariancePresent);
    }

//sets the parameter for variance

    public void setVariance(int Variance) {
        this.VariancePresent = Variance;
    }
//gets the parameter for spatialcoherency

    public int getSpatialCoherency() {
        return (this.SpatialCoherency);
    }
//sets the parameter for spatial coherency

    public void setSpatialCoherency(int Coherency) {
        this.SpatialCoherency = Coherency;
    }
//gets the parameter for ColorQuantizationPresent

    public int getColorQuantizationPresent() {
        return (this.ColorQuantizationPresent);
    }
//sets the parameter for spatial ColorQuantizationPresent

    public void setColorQuantizationPresent(int Quantization) {
        this.ColorQuantizationPresent = Quantization;
    }
//gets the parameter for ColorSpacePresent as Integer

    public int getColorSpacePresentInt() {
        return (this.ColorSpacePresentInt);
    }
//sets the parameter for ColorSpacePresent as Integer

    public void setColorSpacePresentInt(String Colorspace) {
        this.ColorSpacePresentStr = Colorspace;
    }
//gets the parameter for ColorSpacePresent as String

    public String getColorSpacePresentStr() {
        return (this.ColorSpacePresentStr);
    }
//sets the parameter for ColorSpacePresent as String

    public void setColorSpacePresentStr(String Colorspace) {
        this.ColorSpacePresentStr = Colorspace;
    }

    public static class DominantColorValues {
        private int size;
        private int percentages[];
        private int colorValues[][];
        private int coherency;

        public int getSize() {
            return this.size;
        }

        public int getPercentage(int idx) {
            return this.percentages[idx];
        }

        public int getSpatialCoherency() {
            return this.coherency;
        }

        public int[] getColorValue(int idx) {
            return colorValues[idx];
        }

        public void setCoherency(int coherency) {
            this.coherency = coherency;
        }

        public void setColorValues(int[][] colorValues) {
            this.colorValues = colorValues;
        }

        public void setPercentages(int[] percentages) {
            this.percentages = percentages;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
