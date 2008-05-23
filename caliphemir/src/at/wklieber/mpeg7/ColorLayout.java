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

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class ColorLayout {
    static final boolean debug = true;
    private int[][] shape;
    private int _ySize, _xSize;
    private BufferedImage img;

    private static int[] availableCoeffNumbers = {1, 3, 6, 10, 15, 21, 28, 64};

    private int[] _YCoeff, _CbCoeff, _CrCoeff;

    private int numberOfCCoeff = 64, numberOfYCoeff = 64;

    private static int[] zigzag = {
        0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5,
        12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28,
        35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51,
        58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63
    };

    private static double[][] cosin = {
        {
            3.535534e-01, 3.535534e-01, 3.535534e-01, 3.535534e-01,
            3.535534e-01, 3.535534e-01, 3.535534e-01, 3.535534e-01
        },
        {
            4.903926e-01, 4.157348e-01, 2.777851e-01, 9.754516e-02,
            -9.754516e-02, -2.777851e-01, -4.157348e-01, -4.903926e-01
        },
        {
            4.619398e-01, 1.913417e-01, -1.913417e-01, -4.619398e-01,
            -4.619398e-01, -1.913417e-01, 1.913417e-01, 4.619398e-01
        },
        {
            4.157348e-01, -9.754516e-02, -4.903926e-01, -2.777851e-01,
            2.777851e-01, 4.903926e-01, 9.754516e-02, -4.157348e-01
        },
        {
            3.535534e-01, -3.535534e-01, -3.535534e-01, 3.535534e-01,
            3.535534e-01, -3.535534e-01, -3.535534e-01, 3.535534e-01
        },
        {
            2.777851e-01, -4.903926e-01, 9.754516e-02, 4.157348e-01,
            -4.157348e-01, -9.754516e-02, 4.903926e-01, -2.777851e-01
        },
        {
            1.913417e-01, -4.619398e-01, 4.619398e-01, -1.913417e-01,
            -1.913417e-01, 4.619398e-01, -4.619398e-01, 1.913417e-01
        },
        {
            9.754516e-02, -2.777851e-01, 4.157348e-01, -4.903926e-01,
            4.903926e-01, -4.157348e-01, 2.777851e-01, -9.754516e-02
        }
    };
    private static int[][] weightMatrix = new int[3][64];
    private BufferedImage colorLayoutImage;

    /**
     * Create a ColorLayout Object from the given BufferedImage. 6 Y and 3 C Coefficients are used,
     * if you want to use another number you have to set it with the Setters.
     *
     * @param image the input image
     */
    public ColorLayout(BufferedImage image) {
        this.img = image;
        _ySize = image.getHeight();
        _xSize = image.getWidth();
        init();
    }

    /**
     * Create a ColorLayout Object from the given BufferedImage with the desired number of Coefficients
     *
     * @param image          the input image
     * @param numberOfYCoeff desired number of Y Coefficients
     * @param numberOfCCoeff desired number of Cr and Cb Coefficients
     */
    public ColorLayout(int numberOfYCoeff, int numberOfCCoeff, BufferedImage image) {
        this.numberOfCCoeff = getRightCoeffNumber(numberOfCCoeff);
        this.numberOfYCoeff = getRightCoeffNumber(numberOfYCoeff);
        this.img = image;
        _ySize = image.getHeight();
        _xSize = image.getWidth();
        init();
    }

    /**
     * Create a ColorLayout Object from its descriptor
     *
     * @param descriptor the descriptor as JDOM Element
     */
    public ColorLayout(Element descriptor) {
        this.img = null;
        _YCoeff = new int[64];
        _CbCoeff = new int[64];
        _CrCoeff = new int[64];
        colorLayoutImage = null;

        Vector v = getCoeffs(descriptor);
        if (v != null) {
            int[] y = (int[]) v.get(0);
            int[] cb = (int[]) v.get(1);
            int[] cr = (int[]) v.get(2);
            for (int i = 0; i < 64; i++) {
                if (i < y.length) {
                    _YCoeff[i] = y[i];
                } else {
                    _YCoeff[i] = 16;
                }
                if (i < cb.length) {
                    _CbCoeff[i] = cb[i];
                    _CrCoeff[i] = cr[i];
                } else {
                    _CbCoeff[i] = 16;
                    _CrCoeff[i] = 16;
                }

            }
        } else {
            debug("Descriptor not valid!!!");
        }
    }


    private void init() {
        shape = new int[3][64];
        _YCoeff = new int[64];
        _CbCoeff = new int[64];
        _CrCoeff = new int[64];
        colorLayoutImage = null;
        extract();
    }

    private void createShape() {
        int y_axis, x_axis;
        int i, k, x, y, j;
        long[][] sum = new long[3][64];
        int[] cnt = new int[64];
        double yy = 0.0;
        int R, G, B;

        //init of the blocks
        for (i = 0; i < 64; i++) {
            cnt[i] = 0;
            sum[0][i] = 0;
            sum[1][i] = 0;
            sum[2][i] = 0;
            shape[0][i] = 0;
            shape[1][i] = 0;
            shape[2][i] = 0;
        }

        WritableRaster raster = img.getRaster();
        int[] pixel = {0, 0, 0};
        for (y = 0; y < _ySize; y++) {
            for (x = 0; x < _xSize; x++) {
                raster.getPixel(x, y, pixel);
                R = pixel[0];
                G = pixel[1];
                B = pixel[2];

                y_axis = (int) (y / (_ySize / 8.0));
                x_axis = (int) (x / (_xSize / 8.0));

                k = y_axis * 8 + x_axis;

                //RGB to YCbCr, partition and average-calculation
                yy = (0.299 * R + 0.587 * G + 0.114 * B) / 256.0;
                sum[0][k] += (int) (219.0 * yy + 16.5); // Y
                sum[1][k] += (int) (224.0 * 0.564 * (B / 256.0 * 1.0 - yy) + 128.5); // Cb
                sum[2][k] += (int) (224.0 * 0.713 * (R / 256.0 * 1.0 - yy) + 128.5); // Cr
                cnt[k]++;
            }
        }

        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                for (k = 0; k < 3; k++) {
                    if (cnt[i * 8 + j] != 0)
                        shape[k][i * 8 + j] = (int) (sum[k][i * 8 + j] / cnt[i * 8 + j]);
                    else
                        shape[k][i * 8 + j] = 0;
                }
            }
        }
    }

    public void Fdct(int[] shapes) {
        int i, j, k;
        double s;
        double[] dct = new double[64];

        //calculation of the cos-values of the second sum
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += cosin[j][k] * shapes[8 * i + k];
                dct[8 * i + j] = s;
            }
        }

        for (j = 0; j < 8; j++) {
            for (i = 0; i < 8; i++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += cosin[i][k] * dct[8 * k + j];
                shapes[8 * i + j] = (int) Math.floor(s + 0.499999);
            }
        }
    }

    private int quant_ydc(int i) {
        int j;
        if (i > 192)
            j = 112 + (i - 192) / 4;
        else if (i > 160)
            j = 96 + (i - 160) / 2;
        else if (i > 96)
            j = 32 + (i - 96);
        else if (i > 64)
            j = 16 + (i - 64) / 2;
        else
            j = i / 4;

        return j;
    }

    private int quant_cdc(int i) {
        int j;
        if (i > 191)
            j = 63;
        else if (i > 160)
            j = 56 + (i - 160) / 4;
        else if (i > 144)
            j = 48 + (i - 144) / 2;
        else if (i > 112)
            j = 16 + (i - 112);
        else if (i > 96)
            j = 8 + (i - 96) / 2;
        else if (i > 64)
            j = (i - 64) / 4;
        else
            j = 0;

        return j;
    }


    private int quant_ac(int i) {
        int j;

        if (i > 255)
            i = 255;
        //if(i > 239)
        //i = 239;
        if (i < -256)
            i = -256;
        if ((Math.abs(i)) > 127)
            j = 64 + (Math.abs(i)) / 4;
        else if ((Math.abs(i)) > 63)
            j = 32 + (Math.abs(i)) / 2;
        else
            j = Math.abs(i);
        j = (i < 0) ? -j : j;

        j += 128;
        //j+=132;

        return j;
    }

    public int extract() {

        createShape();

        Fdct(shape[0]);
        Fdct(shape[1]);
        Fdct(shape[2]);

        _YCoeff[0] = quant_ydc(shape[0][0] >> 3) >> 1;
        _CbCoeff[0] = quant_cdc(shape[1][0] >> 3);
        _CrCoeff[0] = quant_cdc(shape[2][0] >> 3);

        //quantization and zig-zagging
        for (int i = 1; i < 64; i++) {
            _YCoeff[i] = quant_ac((shape[0][(zigzag[i])]) >> 1) >> 3;
            _CbCoeff[i] = quant_ac(shape[1][(zigzag[i])]) >> 3;
            _CrCoeff[i] = quant_ac(shape[2][(zigzag[i])]) >> 3;
        }

        setYCoeff(_YCoeff);
        setCbCoeff(_CbCoeff);
        setCrCoeff(_CrCoeff);
        return 0;
    }

    public Element getDescriptor() {
        Namespace mpeg7, xsi;
        mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element vdesc = new Element("VisualDescriptor", mpeg7).setAttribute("type", "ColorLayoutType", xsi);
        // Ersten Werte:
        Element ydc, cbdc, crdc;
        ydc = new Element("YDCCoeff", mpeg7).addContent(_YCoeff[0] + "");
        cbdc = new Element("CbDCCoeff", mpeg7).addContent(_CbCoeff[0] + "");
        crdc = new Element("CrDCCoeff", mpeg7).addContent(_CrCoeff[0] + "");
        vdesc.addContent(ydc);
        vdesc.addContent(cbdc);
        vdesc.addContent(crdc);
        if (numberOfYCoeff > 1) {
            Element yac = new Element("YACCoeff" + (numberOfYCoeff - 1), mpeg7);
            StringBuffer b = new StringBuffer();
            for (int i = 1; i < numberOfYCoeff; i++) {
                b.append(_YCoeff[i] + " ");
            }
            yac.setText(b.toString().trim());
            vdesc.addContent(yac);
        }
        if (numberOfCCoeff > 1) {
            Element cbac = new Element("CbACCoeff" + (numberOfCCoeff - 1), mpeg7);
            Element crac = new Element("CrACCoeff" + (numberOfCCoeff - 1), mpeg7);
            StringBuffer bcb, bcr;
            bcb = new StringBuffer();
            bcr = new StringBuffer();
            for (int i = 1; i < numberOfCCoeff; i++) {
                bcb.append(_CbCoeff[i] + " ");
                bcr.append(_CrCoeff[i] + " ");
            }
            cbac.setText(bcb.toString().trim());
            crac.setText(bcr.toString().trim());

            vdesc.addContent(cbac);
            vdesc.addContent(crac);
        }

        return vdesc;
    }

    private void setYCoeff(int[] YCoeff) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < numberOfYCoeff; i++) {
            b.append(YCoeff[i] + " ");
        }
//        System.out.println("y:  " + b.toString());
    }


    private void setCbCoeff(int[] CbCoeff) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < numberOfCCoeff; i++) {
            b.append(CbCoeff[i] + " ");
        }
//        System.out.println("cb: " + b.toString());
    }

    private void setCrCoeff(int[] CrCoeff) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < numberOfCCoeff; i++) {
            b.append(CrCoeff[i] + " ");
        }
//        System.out.println("cr: " + b.toString());
    }

    /**
     * Nicht alle Werte sind laut MPEG-7 erlaubt ....
     */
    private int getRightCoeffNumber(int num) {
        int val = 0;
        if (num <= 1)
            val = 1;
        else if (num <= 3)
            val = 3;
        else if (num <= 6)
            val = 6;
        else if (num <= 10)
            val = 10;
        else if (num <= 15)
            val = 15;
        else if (num <= 21)
            val = 21;
        else if (num <= 28)
            val = 28;
        else if (num > 28) val = 64;
        return val;
    }

    public static void main(String[] args) {
        ColorLayout cl = null;
        try {
            cl = new ColorLayout(64, 64, ImageIO.read(new FileInputStream("test1.jpg")));
            Element desc1 = cl.getDescriptor();
            //new XMLOutputter("  ", true).output(desc1, System.out);
            new XMLOutputter().output(desc1, System.out);
            Element desc2 = new ColorLayout(64, 64, ImageIO.read(new FileInputStream("test2.jpg"))).getDescriptor();
            Element desc3 = new ColorLayout(64, 64, ImageIO.read(new FileInputStream("test3.jpg"))).getDescriptor();

//            System.out.println("Similarity test1 test2: " + ColorLayout.getSimilarity(desc1, desc2));
//            System.out.println("Similarity test1 test3: " + ColorLayout.getSimilarity(desc1, desc3));
//            System.out.println("Similarity test2 test3: " + ColorLayout.getSimilarity(desc2, desc3));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes two ColorLayout DS and calculates similarity.
     *
     * @return -1.0 if c1 or c2 does not contain a valid ColorLayout DS
     */
    public static double getSimilarity(Element c1, Element c2) {
        double val = -1.0;
        int YCoeff1, YCoeff2, CCoeff1, CCoeff2, YCoeff, CCoeff;
        Vector v1 = getCoeffs(c1);
        Vector v2 = getCoeffs(c2);
        int[] y1, cb1, cr1, y2, cb2, cr2;
        if (v1 != null && v2 != null) { // valid??
            y1 = (int[]) v1.get(0);
            cb1 = (int[]) v1.get(1);
            cr1 = (int[]) v1.get(2);
            y2 = (int[]) v2.get(0);
            cb2 = (int[]) v2.get(1);
            cr2 = (int[]) v2.get(2);
            val = getSimilarity(y1, cb1, cr1, y2, cb2, cr2);
        }
        return val;
    }

    /**
     * Takes two ColorLayout DS and calculates similarity.
     *
     * @return Vector of int[] (yCoeff at Vector.get(0), cbCoeff at Vector.get(1), crCoeff cbCoeff at Vector.get(2)) or null if not valid ColorLayoutDS
     */
    public static Vector getCoeffs(Element descriptor) {
        Vector vals = null;
        int[] y, cb, cr;
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
            str_y = descriptor.getChildTextTrim("YDCCoeff", mpeg7) + " ";
            str_cb = descriptor.getChildTextTrim("CbDCCoeff", mpeg7) + " ";
            str_cr = descriptor.getChildTextTrim("CrDCCoeff", mpeg7) + " ";
            java.util.List l = descriptor.getChildren();
            for (Iterator i = l.iterator(); i.hasNext();) {
                Element e = (Element) i.next();
                if (e.getName().startsWith("YACCoeff")) {
                    numY = Integer.parseInt(e.getName().substring(8)) + 1;
                    str_y = str_y + e.getTextTrim();
                } else if (e.getName().startsWith("CbACCoeff")) {
                    numC = Integer.parseInt(e.getName().substring(9)) + 1;
                    str_cb = str_cb + e.getTextTrim();
                } else if (e.getName().startsWith("CrACCoeff")) {
                    numC = Integer.parseInt(e.getName().substring(9)) + 1;
                    str_cr = str_cr + e.getTextTrim();
                }
            }

            // getting y-coeff:
            y = new int[numY];
            cb = new int[numC];
            cr = new int[numC];

            debug("NumYCoeffs: " + numY + ", NumCCoeffs: " + numC);

            StringTokenizer st = new StringTokenizer(str_y.trim(), " ");
            int countSteps = 0;
            while (st.hasMoreElements()) {
                y[countSteps] = Integer.parseInt(st.nextToken());
                countSteps++;
            }
            st = new StringTokenizer(str_cb.trim(), " ");
            countSteps = 0;
            while (st.hasMoreElements()) {
                cb[countSteps] = Integer.parseInt(st.nextToken());
                countSteps++;
            }
            st = new StringTokenizer(str_cr.trim(), " ");
            countSteps = 0;
            while (st.hasMoreElements()) {
                cr[countSteps] = Integer.parseInt(st.nextToken());
                countSteps++;
            }
            vals = new Vector();
            vals.add(y);
            vals.add(cb);
            vals.add(cr);
        }
        return vals;
    }

    /**
     * Takes two ColorLayout Coeff sets and calculates similarity.
     *
     * @return -1.0 if data is not valid.
     */
    public static double getSimilarity(int[] YCoeff1, int[] CbCoeff1, int[] CrCoeff1, int[] YCoeff2, int[] CbCoeff2, int[] CrCoeff2) {
        int numYCoeff1, numYCoeff2, CCoeff1, CCoeff2, YCoeff, CCoeff;

        //Numbers of the Coefficients of two descriptor values.
        numYCoeff1 = YCoeff1.length;
        numYCoeff2 = YCoeff2.length;
        CCoeff1 = CbCoeff1.length;
        CCoeff2 = CbCoeff2.length;

        //take the minimal Coeff-number
        YCoeff = Math.min(numYCoeff1, numYCoeff2);
        CCoeff = Math.min(CCoeff1, CCoeff2);

        setWeightingValues();

        int j;
        int[] sum = new int[3];
        int diff;
        sum[0] = 0;

        for (j = 0; j < YCoeff; j++) {
            diff = (YCoeff1[j] - YCoeff2[j]);
            sum[0] += (weightMatrix[0][j] * diff * diff);
        }

        sum[1] = 0;
        for (j = 0; j < CCoeff; j++) {
            diff = (CbCoeff1[j] - CbCoeff2[j]);
            sum[1] += (weightMatrix[1][j] * diff * diff);
        }

        sum[2] = 0;
        for (j = 0; j < CCoeff; j++) {
            diff = (CrCoeff1[j] - CrCoeff2[j]);
            sum[2] += (weightMatrix[2][j] * diff * diff);
        }

        //returns the distance between the two desciptor values
        double val = Math.sqrt(sum[0] * 1.0) + Math.sqrt(sum[1] * 1.0) + Math.sqrt(sum[2] * 1.0);

        return val;
    }

    private static void setWeightingValues() {
        weightMatrix[0][0] = 2;
        weightMatrix[0][1] = weightMatrix[0][2] = 2;
        weightMatrix[1][0] = 2;
        weightMatrix[1][1] = weightMatrix[1][2] = 1;
        weightMatrix[2][0] = 4;
        weightMatrix[2][1] = weightMatrix[2][2] = 2;

        for (int i = 0; i < 3; i++) {
            for (int j = 3; j < 64; j++)
                weightMatrix[i][j] = 1;
        }
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

    public BufferedImage getColorLayoutImage() {
        if (colorLayoutImage != null)
            return colorLayoutImage;
        else {
            int[][] smallReImage = new int[3][64];

            // inverse quantization and zig-zagging
            smallReImage[0][0] = IquantYdc((_YCoeff[0]));
            smallReImage[1][0] = IquantCdc((_CbCoeff[0]));
            smallReImage[2][0] = IquantCdc((_CrCoeff[0]));

            for (int i = 1; i < 64; i++) {
                smallReImage[0][(zigzag[i])] = IquantYac((_YCoeff[i]));
                smallReImage[1][(zigzag[i])] = IquantCac((_CbCoeff[i]));
                smallReImage[2][(zigzag[i])] = IquantCac((_CrCoeff[i]));
            }

            // inverse Discrete Cosine Transform
            Idct(smallReImage[0]);
            Idct(smallReImage[1]);
            Idct(smallReImage[2]);

            // YCrCb to RGB
            colorLayoutImage = YCrCb2RGB(smallReImage);
            return colorLayoutImage;
        }
    }

    private void Idct(int[] iShapes) {
        int u, v, k;
        double s;
        double[] dct = new double[64];

        //calculation of the cos-values of the second sum
        for (u = 0; u < 8; u++) {
            for (v = 0; v < 8; v++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += cosin[k][v] * iShapes[8 * u + k];
                dct[8 * u + v] = s;
            }
        }

        for (v = 0; v < 8; v++) {
            for (u = 0; u < 8; u++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += cosin[k][u] * dct[8 * k + v];
                iShapes[8 * u + v] = (int) Math.floor(s + 0.499999);
            }
        }
    }


    private int IquantYdc(int i) {
        int j;
        i = i << 1;
        if (i > 112)
            j = 194 + (i - 112) * 4;
        else if (i > 96)
            j = 162 + (i - 96) * 2;
        else if (i > 32)
            j = 96 + (i - 32);
        else if (i > 16)
            j = 66 + (i - 16) * 2;

        else
            j = i * 4;

        return j * 8;
    }

    private int IquantCdc(int i) {
        int j;
        if (i > 63)
            j = 192;
        else if (i > 56)
            j = 162 + (i - 56) * 4;
        else if (i > 48)
            j = 145 + (i - 48) * 2;
        else if (i > 16)
            j = 112 + (i - 16);
        else if (i > 8)
            j = 97 + (i - 8) * 2;
        else if (i > 0)
            j = 66 + i * 4;
        else
            j = 64;
        return j * 8;
    }

    private int IquantYac(int i) {
        int j;
        i = i << 3;
        i -= 128;
        if (i > 128)
            i = 128;
        if (i < -128)
            i = -128;
        if ((Math.abs(i)) > 96)
            j = (Math.abs(i)) * 4 - 256;
        else if ((Math.abs(i)) > 64)
            j = (Math.abs(i)) * 2 - 64;
        else
            j = Math.abs(i);
        j = (i < 0) ? -j : j;

        return j * 2;
    }

    private int IquantCac(int i) {
        int j;
        i = i << 3;
        i -= 128;
        if (i > 128)
            i = 128;
        if (i < -128)
            i = -128;
        if ((Math.abs(i)) > 96)
            j = (Math.abs(i) * 4 - 256);
        else if ((Math.abs(i)) > 64)
            j = (Math.abs(i) * 2 - 64);
        else
            j = Math.abs(i);
        j = (i < 0) ? -j : j;

        return j;
    }

    public int getNumberOfCCoeff() {
        return numberOfCCoeff;
    }

    public void setNumberOfCCoeff(int numberOfCCoeff) {
        this.numberOfCCoeff = numberOfCCoeff;
    }

    public int getNumberOfYCoeff() {
        return numberOfYCoeff;
    }

    public void setNumberOfYCoeff(int numberOfYCoeff) {
        this.numberOfYCoeff = numberOfYCoeff;
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
}
