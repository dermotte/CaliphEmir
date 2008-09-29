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
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2008 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */
package net.semanticmetadata.lire.imageanalysis;

import at.lux.imageanalysis.VisualDescriptor;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.StringTokenizer;

/**
 * <p>VisualDescriptor for the AutoCorrelogram based on color as described in
 * Huang, J.; Kumar, S. R.; Mitra, M.; Zhu, W. & Zabih, R. (2007) "Image
 * Indexing Using Color Correlograms", IEEE Computer Society</p>
 * <p>see also DOI <a href="http://doi.ieeecomputersociety.org/10.1109/CVPR.1997.609412">10.1109/CVPR.1997.609412</a></p>
 * <p/>
 * Todo: Change the 2-dim array to a one dim array, as this is much faster in Java.
 */
public class AutoColorCorrelogram implements LireFeature {
    private float quantH;
    private float quantV;
    private float quantS;
//    private int[][][] quantTable;
    private int maxDistance = 4;
    private float[][] correlogram;
    private Mode mode = Mode.SuperFast;
    private int numBins = 256;
    private float quantH_f;
    private float quantS_f;
    private float quantV_f;

    /**
     * Defines the available analysis modes: Superfast uses the approach described in the paper, Quarterneighbourhood
     * investigates the pixels in down and to the right of the respective pixel and FullNeighbourhood investigates
     * the whole lot of pixels within maximumDistance of the respective pixel.
     */
    public enum Mode {
        FullNeighbourhood,
        QuarterNeighbourhood,
        SuperFast
    }

    /**
     * Creates a new AutoColorCorrelogram, where the distance k is limited to a maximum of
     * maxDistance (see publication mentioned above)
     *
     * @param maxDistance upper limit of k
     */
    public AutoColorCorrelogram(int maxDistance) {
        this.maxDistance = maxDistance;
        init();
    }

    /**
     * Creates a new AutoColorCorrelogram using a maximum L_inf pixel distance for analysis and given mode
     *
     * @param maxDistance maximum L_inf pixel distance for analysis
     * @param mode        the mode of calculation (determines the speed of extraction)
     */
    public AutoColorCorrelogram(int maxDistance, Mode mode) {
        this.maxDistance = maxDistance;
        this.mode = mode;
        init();
    }

    public AutoColorCorrelogram(Mode mode) {
        this.mode = mode;
        init();
    }

    public AutoColorCorrelogram() {
        init();
    }

    private void init() {
        if (numBins < 33) {
            quantH_f = 8f;
            quantS_f = 2f;
            quantV_f = 2f;
            numBins = 32;
        } else if (numBins < 65) {
            quantH_f = 8f;
            quantS_f = 4f;
            quantV_f = 2f;
            numBins = 64;
        } else if (numBins < 129) {
            quantH_f = 8f;
            quantS_f = 4f;
            quantV_f = 4f;
            numBins = 128;
        } else {
            quantH_f = 16f;
            quantS_f = 4f;
            quantV_f = 4f;
            numBins = 256;
        }
        quantH = 360f / quantH_f;
        quantS = 256f / quantS_f;
        quantV = 256f / quantV_f;

        // init quantization table:
//        int count = 0;
//        quantTable = new int[(int) quantH_f][(int) quantS_f][(int) quantV_f];
//        for (int[][] ints : quantTable) {
//            for (int[] anInt : ints) {
//                for (int k = 0; k < anInt.length; k++) {
//                    anInt[k] = count;
//                    assert (count < numBins);
//                    count++;
//                }
//            }
//        }
    }

    public void extract(BufferedImage bi) {
        Raster r = bi.getRaster();
        int[] histogram = new int[numBins];
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = 0;
        }
        int[][] quantPixels = new int[r.getWidth()][r.getHeight()];
        // quantize colors for each pixel (done in HSV color space):
        int[] pixel = new int[3];
        int[] hsv = new int[3];
        for (int x = 0; x < r.getWidth(); x++) {
            for (int y = 0; y < r.getHeight(); y++) {
                // converting to HSV:
                convertRgbToHsv(r.getPixel(x, y, pixel), hsv);
                // quantize the actual pixel:
                quantPixels[x][y] = quantize(hsv);
                // for normalization:
                histogram[quantPixels[x][y]]++;
            }
        }

        // Find the auto-correlogram.
        correlogram = new float[256][maxDistance];
        for (int i1 = 0; i1 < correlogram.length; i1++) {
            for (int j = 0; j < correlogram[i1].length; j++) {
                correlogram[i1][j] = 0;
            }
        }
        int[] tmpCorrelogram = new int[maxDistance];
        for (int x = 0; x < r.getWidth(); x++) {
            for (int y = 0; y < r.getHeight(); y++) {
                int color = quantPixels[x][y];
                getNumPixelsInNeighbourhood(x, y, quantPixels, tmpCorrelogram);
                for (int i = 0; i < maxDistance; i++) {
                    // bug fixed based on comments of Rodrigo Carvalho Rezende, rcrezende <at> gmail.com
                    correlogram[color][i] += tmpCorrelogram[i];
                }
            }
        }
        // normalize the correlogram:
        // Note that this is not the common normalization routine described in the paper, but an adapted one.
        float[] max = new float[maxDistance];
        for (int i = 0; i < max.length; i++) {
            max[i] = 0;

        }
        for (int c = 0; c < numBins; c++) {
            for (int i = 0; i < maxDistance; i++) {
                max[i] = Math.max(correlogram[c][i], max[i]);
            }
        }
        for (int c = 0; c < numBins; c++) {
            for (int i = 0; i < maxDistance; i++) {
                correlogram[c][i] = correlogram[c][i] / max[i];
            }
        }
    }

    private void getNumPixelsInNeighbourhood(int x, int y, int[][] quantPixels, int[] correlogramm) {
        // set to zero for each color at distance 1:
        for (int i = 0; i < correlogramm.length; i++) {
            correlogramm[i] = 0;
        }
        for (int d = 1; d <= maxDistance; d++) {
            // bug fixed based on comments of Rodrigo Carvalho Rezende, rcrezende <at> gmail.com
            if (d > 1) correlogramm[d - 1] += correlogramm[d - 2]; // -> Wrong! Just the reference
//            if (d > 1) System.arraycopy(correlogramm[d - 2], 0, correlogramm[d - 1], 0, correlogramm[d - 1].length);
            int color = quantPixels[x][y];
            if (mode == Mode.QuarterNeighbourhood) {
                // TODO: does not work properly (possible) -> check if funnny
                for (int td = 0; td < d; td++) {
                    if (isInPicture(x + d, y + td, quantPixels.length, quantPixels[0].length))
                        if (quantPixels[x + d][y + td] == color) correlogramm[d - 1]++;
                    if (isInPicture(x + td, y + d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + td][y + d]) correlogramm[d - 1]++;
                    if (isInPicture(x + d, y + d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + d][y + d]) correlogramm[d - 1]++;
                }
            } else if (mode == Mode.FullNeighbourhood) {
                //if (isInPicture(x + d, y + d, quantPixels.length, quantPixels[0].length))
                //    correlogramm[quantPixels[x + d][y + d]][d - 1]++;
                for (int i = -d; i <= d; i++) {
                    if (isInPicture(x + i, y + d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + i][y + d]) correlogramm[d - 1]++;
                    if (isInPicture(x + i, y - d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + i][y - d]) correlogramm[d - 1]++;
                }
                for (int i = -d + 1; i <= d - 1; i++) {
                    if (isInPicture(x + d, y + i, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + d][y + i]) correlogramm[d - 1]++;
                    if (isInPicture(x - d, y + i, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x - d][y + i]) correlogramm[d - 1]++;
                }
            } else {
                if (isInPicture(x + d, y, quantPixels.length, quantPixels[0].length)) {
                    assert (quantPixels[x + d][y] < numBins);
                    assert (d - 1 < maxDistance);
                    if (color == quantPixels[x + d][y]) correlogramm[d - 1]++;
                }
                if (isInPicture(x, y + d, quantPixels.length, quantPixels[0].length)) {
                    assert (quantPixels[x][y + d] < numBins);
                    if (color == quantPixels[x][y + d]) correlogramm[d - 1]++;
                }
            }
        }
    }

    private static boolean isInPicture(int x, int y, int maxX, int maxY) {
        // possibly made faster??
        return !(x < 0 || y < 0) && !(y >= maxY || x >= maxX);
    }

    /**
     * Quantizes a pixel according to numBins number of bins and a respective algorithm.
     *
     * @param pixel the pixel to quantize.
     * @return the quantized value ...
     */
    private int quantize(int[] pixel) {
        return (int) ((int) (pixel[0] / quantH) * (quantV_f) * (quantS_f)
                + (int) (pixel[1] / quantS) * (quantV_f)
                + (int) (pixel[2] / quantV) );
    }

    /**
     * @param rgb RGB Values
     * @param hsv HSV values to set.
     */
    private static void convertRgbToHsv(int[] rgb, int[] hsv) {
        if (hsv.length < 3) {
            throw new IndexOutOfBoundsException("HSV array too small, a minim of three elements is required.");
        }
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];
        int max, min;
        float hue = 0f;

        max = Math.max(R, G);     //calculation of max(R,G,B)
        max = Math.max(max, B);

        min = Math.min(R, G);     //calculation of min(R,G,B)
        min = Math.min(min, B);

        if (max == 0)
            hsv[1] = 0;
        else {
            // Saturation in [0,255]
            hsv[1] = (int) (((max - min) / (float) max) * 255f);
        }

        if (max == min) {
            hue = 0;     // (max - min) = 0
        } else {
            float maxMinusMin = (float) (max - min);
            if (R == max)
                hue = ((G - B) / maxMinusMin);

            else if (G == max)
                hue = (2 + (B - R) / maxMinusMin);

            else if (B == max)
                hue = (4 + (R - G) / maxMinusMin);

            hue *= 60f;

            if (hue < 0f)
                hue += 360f;
        }
        // hue in [0,359]
        hsv[0] = (int) (hue);
        // value in [0,255]
        hsv[2] = max;
    }

    public float getDistance(VisualDescriptor vd) {
        if (!(vd instanceof AutoColorCorrelogram)) return -1;
        float result;
        float[][] vdCorrelogram = ((AutoColorCorrelogram) vd).correlogram;
        result = l1(vdCorrelogram);
        return result;
    }

    private float l2(float[][] vdCorrelogram) {
        float result = 0;
        for (int i = 0; i < correlogram.length; i++) {
            float[] ints = correlogram[i];
            for (int j = 0; j < ints.length; j++) {
                float v = correlogram[i][j] - vdCorrelogram[i][j];
                result += v * v;
            }
        }
        return (float) Math.sqrt(result);
    }

    private float cosineCoeff(float[][] vdCorrelogram) {
        float dot = 0, c1 = 0, c2 = 0;
        for (int i = 0; i < correlogram.length; i++) {
            float[] ints = correlogram[i];
            for (int j = 0; j < ints.length; j++) {
                dot += correlogram[i][j] * vdCorrelogram[i][j];
                c1 += correlogram[i][j] * correlogram[i][j];
                c2 += vdCorrelogram[i][j] * vdCorrelogram[i][j];
            }
        }
        return 1 - (float) (dot / (Math.sqrt(c1) * Math.sqrt(c2)));
    }

    private float jsd(float[][] vdCorrelogram) {
        double sum = 0d;
        for (int i = 0; i < correlogram.length; i++) {
            float[] ints = correlogram[i];
            for (int j = 0; j < ints.length; j++) {
                sum += correlogram[i][j] > 0 ? correlogram[i][j] * Math.log(2d * correlogram[i][j] / (correlogram[i][j] + vdCorrelogram[i][j])) : 0 +
                        vdCorrelogram[i][j] > 0 ? vdCorrelogram[i][j] * Math.log(2d * vdCorrelogram[i][j] / (correlogram[i][j] + vdCorrelogram[i][j])) : 0;
            }
        }
        return (float) sum;
    }

    private float l1(float[][] vdCorrelogram) {
        float result = 0;
        for (int i = 0; i < correlogram.length; i++) {
            float[] ints = correlogram[i];
            for (int j = 0; j < ints.length; j++) {
                float v = Math.abs(correlogram[i][j] - vdCorrelogram[i][j]);
                result += v;
            }
        }
        return result;
    }

    private float tanimoto(float[][] vdCorrelogram) {
        // Tanimoto coefficient
        double Result = 0;
        double Temp1 = 0;
        double Temp2 = 0;

        double TempCount1 = 0, TempCount2 = 0, TempCount3 = 0;

        for (int i = 0; i < correlogram.length; i++) {
            float[] ints = correlogram[i];
            for (int j = 0; j < ints.length; j++) {
                Temp1 += correlogram[i][j];
                Temp2 += vdCorrelogram[i][j];
            }
        }

        if (Temp1 == 0 || Temp2 == 0) Result = 100;
        if (Temp1 == 0 && Temp2 == 0) Result = 0;

        if (Temp1 > 0 && Temp2 > 0) {
            for (int i = 0; i < correlogram.length; i++) {
                float[] ints = correlogram[i];
                for (int j = 0; j < ints.length; j++) {
                    TempCount1 += (correlogram[i][j] / Temp1) * (vdCorrelogram[i][j] / Temp2);
                    TempCount2 += (vdCorrelogram[i][j] / Temp2) * (vdCorrelogram[i][j] / Temp2);
                    TempCount3 += (correlogram[i][j] / Temp1) * (correlogram[i][j] / Temp1);

                }
            }
            Result = (100 - 100 * (TempCount1 / (TempCount2 + TempCount3
                    - TempCount1))); //Tanimoto
        }
        return (float) Result;
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(256 * 5);
        sb.append(maxDistance);
        sb.append(' ');
        for (int i = 0; i < correlogram.length; i++) {
            for (int j = 0; j < correlogram[i].length; j++) {
                sb.append(correlogram[i][j]);
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String string) {
        StringTokenizer st = new StringTokenizer(string);
        correlogram = new float[256][Integer.parseInt(st.nextToken())];
        for (int i = 0; i < correlogram.length; i++) {
            for (int j = 0; j < correlogram[i].length; j++) {
                if (!st.hasMoreTokens())
                    throw new IndexOutOfBoundsException("Too few numbers in string representation!");
                correlogram[i][j] = Float.parseFloat(st.nextToken());
            }
        }
    }
}
