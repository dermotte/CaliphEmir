package at.lux.imageanalysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.StringTokenizer;

/*
 * Based on the source on following license:
 * =========================================
 * Created on 05.11.2004
 * Color Structure Descriptor for MPEG7 VizIR
 *
 * Copyright (C) 2004 Adis Buturovic, Vienna University of Technology
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Contact address: adis@ims.tuwien.ac.at
 * For full description see MPEG7-CSD.pdf
 */


public class ColorStructureImplementation implements VisualDescriptor {


    public ColorStructureImplementation() {
        quantizationLevels = 64;
    }

    public ColorStructureImplementation(int quantizationLevels) {
        this.quantizationLevels = quantizationLevels;
    }

    public ColorStructureImplementation(BufferedImage image) {
        try {
            extractFeature(image);
        } catch (Exception e) {
            // todo: some error handling ...
            e.printStackTrace();
        }
    }

    /**
     * Quantity of color quantization bins
     */
    private int quantizationLevels = 64;

    /**
     * Subspaces needed for the quantization
     */
    private static int subspace = 0;

    /**
     * Color Structure Histogram
     */
    protected float[] ColorHistogram = null;

    /**
     * Quantization table for 256, 128, 64 and 32 quantisation bins.
     * <p/>
     * <br>form:
     * <code><br>
     * subspace0 , subspace1 , subspace2 , subspace3 , subspace4 <br>
     * Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum   - 256 Levels [offset pos  0 - pos  9]<br>
     * Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum   - 128 Levels [offset pos 10 - pos 19]<br>
     * Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum   -  64 Levels [offset pos 20 - pos 29]<br>
     * Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum  ,  Hue,Sum   -  32 Levels [offset pos 30 - pos 39]<br>
     * </code>
     */

    private static final int[] quantTable = {1, 32, 4, 8, 16, 4, 16, 4, 16, 4,             // Hue, Sum - subspace 0,1,2,3,4 for 256 levels
            1, 16, 4, 4, 8, 4, 8, 4, 8, 4,            // Hue, Sum - subspace 0,1,2,3,4 for 128 levels
            1, 8, 4, 4, 4, 4, 8, 2, 8, 1,            // Hue, Sum - subspace 0,1,2,3,4 for  64 levels
            1, 8, 4, 4, 4, 4, 4, 1, 4, 1};        // Hue, Sum - subspace 0,1,2,3,4 for  32 levels

    /**
     * The <code>extractFeature(MediaContent)</code> class loads the media content, convert it to HMMD color space and executes
     * the CSD extraction and quantization.
     *
     * @param image the image to be analyzed.
     * @throws Exception in some cases ...
     */

    public void extractFeature(BufferedImage image) throws Exception {

        // load image to BufferedImage

        double height = image.getHeight();
        double width = image.getWidth();

        int temp[][] = new int[(int) height - 1][(int) width - 1];

//        if (width > height) {
//            System.out.println("\nExit - vizir bug: file unsupported -> MediaFrame.getPixelAt");
//            System.exit(0);
//        }

        int ir[][] = temp;
        int ig[][] = temp;
        int ib[][] = temp;

        int iH[][] = temp;
        int iMax[][] = temp;
        int iMin[][] = temp;
        int iDiff[][] = temp;
        int iSum[][] = temp;

        // convert BufferedImage to double int array for every RGB color
        // ant then covert RGB values into HMMD

        WritableRaster raster = image.getRaster();
        int[] pixel = new int[3];
        for (int ch = 0; ch < height - 1; ch++) { //row
            for (int cw = 0; cw < width - 1; cw++) {//column
                raster.getPixel(cw, ch, pixel);
                ir[ch][cw] = pixel[0]; // RED
                ig[ch][cw] = pixel[1]; // GREEN
                ib[ch][cw] = pixel[2]; // BLUE

                int[] tempHMMD = RGB2HMMD(ir[ch][cw], ig[ch][cw], ib[ch][cw]);
                iH[ch][cw] = tempHMMD[0];                        // H
                iMax[ch][cw] = tempHMMD[1];                        // Max
                iMin[ch][cw] = tempHMMD[2];                         // Min
                iDiff[ch][cw] = tempHMMD[3];                         // Diff
                iSum[ch][cw] = tempHMMD[4];                         // Sum
            }
        }


        ColorHistogram = HMMDColorStuctureExtraction(iH, iMax, iMin, iDiff, iSum, (int) height, (int) width); // extract HMMD colors and make histogram

        // if ( quantizationLevels != 256 ) ColorHistogram = reQuantization(ColorHistogram); // requantize and normalize histogram to 0-255 range
        ColorHistogram = reQuantization(ColorHistogram);
    }

    /**
     * The <code>HMMDColorStuctureExtraction(int[][], int[][], int[][], int[][], int[][], int, int)</code> class builds the Color Structure Histogram.
     *
     * @param iH     - Hue values of the frame
     * @param iMax   - Max values of the frame
     * @param iMin   - Min values of the frame
     * @param iDiff  - Diff values of the frame
     * @param iSum   - values of the frame
     * @param height - height of the frame
     * @param width  - width of the frame
     * @return float[] - Color Structure Histogram
     * @throws Exception in some cases ...
     */

    private float[] HMMDColorStuctureExtraction(int iH[][], int iMax[][], int iMin[][], int iDiff[][], int iSum[][], int height, int width) throws Exception {
        long hw = height * width;
        long p = Math.round(0.5 * Math.log(hw)) - 8;

        if (p < 0) p = 0;
        double K = Math.pow(2, p);

        double E = 8 * K;
        int m = 0;

        width--;
        height--; // um ueberlauf in der schleife zu vermeiden - 24.07.2005

        if (quantizationLevels == 0) {
            setQuantizationLevels(64);
            System.out.println("WARNING: quantization size will be set to default: 64");
        } // default value is 64

        float h[] = new float[quantizationLevels];        // CSD temp
        int t[] = new int[quantizationLevels];        // CSD temp - int sollte stimmen

        for (int i = 0; i < quantizationLevels; i++) { /* t[i] = 0; */
            h[i] = 0.0f;
        }

        for (int y = 0; y < ((height - E) + 1); y += K) {
            for (int x = 0; x < ((width - E) + 1); x += K) {
                // re initialize the local histogram t[m]
                for (m = 0; m < quantizationLevels; m++) t[m] = 0;

                //collect local histogram over pixels in structuring element
                for (int yy = y; yy < y + E; yy += K)
                    for (int xx = x; xx < x + E; xx += K) {
                        // get quantized pixel color and update local histogramm

                        // The 256-cell color space is quantized non-uniformly as follows.

                        // First, the HMMD Color space is divided into 5 subspaces:
                        // subspaces 0,1,2,3, and 4.
                        // This subspace division is defined along the Diff(colorfulness) axis of the HMMD Color space.
                        // The subspaces are defined by cut-points which determine the following diff axis intervals:
                        // [(0,6),(6,20),(20,60),(60,110),(110,255)].

                        // Second, each color subspace is uniformly quantized along Hue and Sum axes, where the
                        // number of Quantization levels along each axis is defined in the Table for each operating point.
                        //
                        // Example:
                        //				64 levels					32 levels
                        //
                        //	Subspace	Hue		Sum				Hue			Sum
                        //		0		1		8				1			8
                        //		1		4		4				4			4
                        //		2		4		4				4			4
                        //		3		8		2				4			1
                        //		4		8		1				4			1

                        int offset = 0;    // offset position in the quantization table

                        int q = 0;
                        try {

                            // define the subspace along the Diff axis

                            if (iDiff[xx][yy] < 7) subspace = 0;
                            else if ((iDiff[xx][yy] > 6) && (iDiff[xx][yy] < 21)) subspace = 1;
                            else if ((iDiff[xx][yy] > 19) && (iDiff[xx][yy] < 61)) subspace = 2;
                            else if ((iDiff[xx][yy] > 59) && (iDiff[xx][yy] < 111)) subspace = 3;
                            else if ((iDiff[xx][yy] > 109) && (iDiff[xx][yy] < 256)) subspace = 4;

                            // HMMD Color Space quantization
                            // see MPEG7-CSD.pdf

                            if (quantizationLevels == 256) {
                                offset = 0;
                                //m = (int)(((float)iH[xx][yy] / quantizationLevels) * quantTable[offset+subspace] + ((float)iSum[xx][yy] / quantizationLevels) * quantTable[offset+subspace+1]);
                                m = (int) ((iH[xx][yy] / quantizationLevels) * quantTable[offset + subspace] + (iSum[xx][yy] / quantizationLevels) * quantTable[offset + subspace + 1]);
                            } else if (quantizationLevels == 128) {
                                offset = 10;
                                //m = (int)(((float)iH[xx][yy] / quantizationLevels) * quantTable[offset+subspace] + ((float)iSum[xx][yy] / quantizationLevels) * quantTable[offset+subspace+1]);
                                m = (int) ((iH[xx][yy] / quantizationLevels) * quantTable[offset + subspace] + (iSum[xx][yy] / quantizationLevels) * quantTable[offset + subspace + 1]);
                            } else if (quantizationLevels == 64) {
                                offset = 20;
                                //m = (int)(((float)iH[xx][yy] / quantizationLevels) * quantTable[offset+subspace] + ((float)iSum[xx][yy] / quantizationLevels) * quantTable[offset+subspace+1]);
                                m = (int) ((iH[xx][yy] / quantizationLevels) * quantTable[offset + subspace] + (iSum[xx][yy] / quantizationLevels) * quantTable[offset + subspace + 1]);

                            } else if (quantizationLevels == 32) {
                                offset = 30;
                                //m = (int)(((float)iH[xx][yy] / quantizationLevels) * quantTable[offset+subspace] + ((float)iSum[xx][yy] / quantizationLevels) * quantTable[offset+subspace+1]);
                                m = (int) ((iH[xx][yy] / quantizationLevels) * quantTable[offset + subspace] + (iSum[xx][yy] / quantizationLevels) * quantTable[offset + subspace + 1]);

                                // System.out.println("m: " + m);

                            }


                            t[m]++;

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("PROB? - quant. schleife: x = " + xx + " y = " + yy);
                            System.out.println("quantizationLevels = " + quantizationLevels);
                            System.out.println("subspace = " + subspace);
                        }
                    }

                // increment the color structure histogramm for each color present in the structuring element
                for (m = 0; m < quantizationLevels; m++) {
                    if (t[m] > 0) h[m]++;
                }
            }
        }

        int S = (width - (int) E + (int) K) / (int) K * ((height - (int) E + (int) K) / (int) K);
        for (m = 0; m < quantizationLevels; m++) {
            h[m] = h[m] / S;
        }
        return h;
    }

    /**
     * The <code>reQuantization(float[] colorHistogramTemp)</code> class is responsible for re-quantization of
     * the CSD Histogram and normalizing to 8-bit code values
     *
     * @return float[] - normalized Color Structure Histogram
     */

    private float[] reQuantization(float[] colorHistogramTemp) {

        float[] uniformCSD = new float[colorHistogramTemp.length];

        for (int i = 0; i < colorHistogramTemp.length; i++) {
            // System.out.print(colorHistogramTemp[i] + " ");
            // System.out.println(" --- ");

            if (colorHistogramTemp[i] == 0) uniformCSD[i] = 0; //
            else if (colorHistogramTemp[i] < 0.000000001f)
                uniformCSD[i] = (int) Math.round((((float) colorHistogramTemp[i] - 0.32f) / (1f - 0.32f)) * 140 + (115 - 35 - 35 - 20 - 25 - 1));     // (int)Math.round((1f / 0.000000001f) * (float)colorHistogramTemp[i]);
            else if (colorHistogramTemp[i] < 0.037f)
                uniformCSD[i] = (int) Math.round((((float) colorHistogramTemp[i] - 0.32f) / (1f - 0.32f)) * 140 + (115 - 35 - 35 - 20 - 25));
            else if (colorHistogramTemp[i] < 0.08f)
                uniformCSD[i] = (int) Math.round((((float) colorHistogramTemp[i] - 0.32f) / (1f - 0.32f)) * 140 + (115 - 35 - 35 - 20));
            else if (colorHistogramTemp[i] < 0.195f)
                uniformCSD[i] = (int) Math.round((((float) colorHistogramTemp[i] - 0.32f) / (1f - 0.32f)) * 140 + (115 - 35 - 35));
            else if (colorHistogramTemp[i] < 0.32f)
                uniformCSD[i] = (int) Math.round((((float) colorHistogramTemp[i] - 0.32f) / (1f - 0.32f)) * 140 + (115 - 35));
            else if (colorHistogramTemp[i] > 0.32f)
                uniformCSD[i] = (int) Math.round((((float) colorHistogramTemp[i] - 0.32f) / (1f - 0.32f)) * 140 + 115);
            else uniformCSD[i] = (int) Math.round((255f / 1f) * (float) colorHistogramTemp[i]);

        }

        return uniformCSD;
    }

    /**
     * The <code>RGB2HMMD (int ir, int ig, int ib)</code> class is responsible for converting RGB values to HMMD values
     *
     * @param ir - RED component
     * @param ig - GREEN component
     * @param ib - BLUE component
     * @return int[] - HMMD value of the pixle
     * @throws Exception
     * @author adis@ims.tuwien.ac.at
     */

    private static int[] RGB2HMMD(int ir, int ig, int ib) throws Exception {
        int HMMD[] = new int[5];

        float max = (float) Math.max(Math.max(ir, ig), Math.max(ig, ib));
        float min = (float) Math.min(Math.min(ir, ig), Math.min(ig, ib));

        float diff = (max - min);
        float sum = (float) ((max + min) / 2.);

        float hue = 0;
        if (diff == 0) hue = 0;
        else if (ir == max && (ig - ib) > 0) hue = 60 * (ig - ib) / (max - min);
        else if (ir == max && (ig - ib) <= 0) hue = 60 * (ig - ib) / (max - min) + 360;
        else if (ig == max) hue = (float) (60 * (2. + (ib - ir) / (max - min)));
        else if (ib == max) hue = (float) (60 * (4. + (ir - ig) / (max - min)));

        diff /= 2;

        HMMD[0] = (int) (hue);
        HMMD[1] = (int) (max);
        HMMD[2] = (int) (min);
        HMMD[3] = (int) (diff);
        HMMD[4] = (int) (sum);

        return (HMMD);
    }

    /**
     * With <code>setQuantizationLevels(int)</code> class you can set the quantisation size
     *
     * @param number
     * @throws Exception
     */

    public void setQuantizationLevels(int number) throws Exception {
        if ((number != 32) && (number != 64) && (number != 128) && (number != 256))
            throw new Exception("have to be chosen from: 32, 64, 128, 256");
        quantizationLevels = number;
    }

    /**
     * Added by Mathias Lux, just an L1 distance function ...
     *
     * @param descriptor
     * @return
     */
    public float getDistance(VisualDescriptor descriptor) {
        float result = -1f;
        if (descriptor instanceof ColorStructureImplementation) {
            ColorStructureImplementation c2 = (ColorStructureImplementation) descriptor;
            if (c2.quantizationLevels == quantizationLevels) {
                // quant levels match, so we can calc distance ...
                result = 0f;
                for (int i = 0; i < ColorHistogram.length; i++) {
                    result += Math.abs(ColorHistogram[i] - c2.ColorHistogram[i]);
                }
            }
        }
        return result;
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("colorstructure;");
        for (float value : ColorHistogram) {
            sb.append((int) value);
            sb.append(' ');
        }
        return sb.toString();
    }

    public void setStringRepresentation(String descriptor) {
        String[] parts = descriptor.split(";");
        if (!parts[0].equals("colorstructure")) {
            throw new UnsupportedOperationException("This is no valid representation of a ColorStructur descriptor!");
        }
        LinkedList<Float> vals = new LinkedList<Float>();
        StringTokenizer st = new StringTokenizer(parts[1], " ");
        while (st.hasMoreElements()) {
            vals.add(Float.parseFloat(st.nextToken()));
        }
        if ((vals.size() != 32) && (vals.size() != 64) && (vals.size() != 128) && (vals.size() != 256))
            throw new UnsupportedOperationException("This is no valid representation of a ColorStructur descriptor! " +
                    "Quant level has to be 32, 64, 128 or 256.");
        ColorHistogram = new float[vals.size()];
        quantizationLevels = vals.size();
    }
}
