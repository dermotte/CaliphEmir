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
import net.semanticmetadata.lire.imageanalysis.utils.Quantization;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;

/**
 * This class provides a simple color histogram for content based image retrieval.
 * Number of bins is configurable, histogram is normalized to 8 bit per bin (0-255). <br>
 * Defaults are given in the final fields. Available options are given by the enums.
 * <p/>
 * <br/>Date: 14.05.2008
 * <br/>Time: 09:47:10
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleColorHistogram implements LireFeature {
    public static final int DEFAULT_NUMBER_OF_BINS = 64;
    public static final HistogramType DEFAULT_HISTOGRAM_TYPE = HistogramType.RGB;
    public static final DistanceFunction DEFAULT_DISTANCE_FUNCTION = DistanceFunction.JSD;

    /**
     * Lists possible types for the histogram class
     */
    enum HistogramType {
        RGB, HSV, Luminance
    }

    /**
     * Lists distance functions possible for this histogram class
     */
    enum DistanceFunction {
        L1, L2, JSD
    }

    /**
     * Temporary pixel field ... re used for speed and memory issues ...
     */
    private int[] pixel = new int[3];
    private int[] histogram;
    private HistogramType histogramType;
    private DistanceFunction distFunc;

    /**
     * Default constructor
     */
    public SimpleColorHistogram() {
        histogramType = DEFAULT_HISTOGRAM_TYPE;
        histogram = new int[DEFAULT_NUMBER_OF_BINS];
        distFunc = DEFAULT_DISTANCE_FUNCTION;
    }

    /**
     * Extracts the color histogram from the given image.
     *
     * @param image
     */
    public void extract(BufferedImage image) {
        if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
            throw new UnsupportedOperationException("Color space not supported. Only RGB.");
        WritableRaster raster = image.getRaster();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                if (histogramType == HistogramType.HSV) {
                    rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
                } else if (histogramType == HistogramType.Luminance) {
                    rgb2yuv(pixel[0], pixel[1], pixel[2], pixel);
                }
                histogram[quant(pixel)]++;
            }
        }
        normalize(histogram);
    }

    private void normalize(int[] histogram) {
        // find max:
        int max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (histogram[i] * 255) / max;
        }
    }

    private int quant(int[] pixel) {
        int quantRgb = (256 * 256 * 256) / histogram.length;
        if (histogramType == HistogramType.HSV) {
            // Todo: tune this one ...
            int qH = (pixel[0] * 8) / 360;    // more granularity in color
            int qS = (pixel[2] * 4) / 100;
            return qH * (qS + 1);
        } else if (histogramType == HistogramType.Luminance) {
            return (pixel[0] * histogram.length) / (256);
        } else {
            return Quantization.quantUniformly(pixel, histogram.length, 256);
        }
    }

    public float getDistance(VisualDescriptor vd) {
        // Check if instance of the right class ...
        if (!(vd instanceof SimpleColorHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        SimpleColorHistogram ch = (SimpleColorHistogram) vd;

        // check if parameters are fitting ...
        if ((ch.histogram.length != histogram.length) || (ch.histogramType != histogramType))
            throw new UnsupportedOperationException("Histogram lengths or color spaces do not match");

        // do the comparison ...
        double sum = 0;
        if (distFunc == DistanceFunction.JSD)
            return (float) jsd(histogram, ch.histogram);
        else if (distFunc == DistanceFunction.L1)
            return (float) distL1(histogram, ch.histogram);
        else
            return (float) distL2(histogram, ch.histogram);
    }

    /**
     * Manhattan distance
     *
     * @param h1
     * @param h2
     * @return
     */
    private static double distL1(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += Math.abs(h1[i] - h2[i]);
        }
        return sum / h1.length;
    }

    /**
     * Euclidean distance
     *
     * @param h1
     * @param h2
     * @return
     */
    private static double distL2(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] - h2[i]) * (h1[i] - h2[i]);
        }
        return Math.sqrt(sum);
    }

    /**
     * Jeffrey Divergence or Jensen-Shannon divergence (JSD) from
     * Deselaers, T.; Keysers, D. & Ney, H. Features for image retrieval: an experimental comparison Inf. Retr., Kluwer Academic Publishers, 2008, 11, 77-107
     *
     * @param h1
     * @param h2
     * @return
     */
    private static double jsd(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            // don't know if this one is right, but however ...
            if (h1[i] > 0 && h2[i] > 0) {
                sum += h1[i] * Math.log(2d * h1[i] / (h1[i] + h2[i])) +
                        h2[i] * Math.log(2d * h2[i] / (h1[i] + h2[i]));
            }
        }
        return sum;
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(histogram.length * 4);
        sb.append(histogramType.name());
        sb.append(' ');
        sb.append(histogram.length);
        sb.append(' ');
        for (int i = 0; i < histogram.length; i++) {
            sb.append(histogram[i]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        histogramType = HistogramType.valueOf(st.nextToken());
        histogram = new int[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < histogram.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation!");
            histogram[i] = Integer.parseInt(st.nextToken());
        }
    }

    /* **************************************************************
   * Color Conversion routines ...
   ************************************************************** */

    /**
     * Adapted from ImageJ documentation:
     * http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector//HTMLHelp/farbraumJava.htm
     *
     * @param r
     * @param g
     * @param b
     * @param yuv
     */
    public static void rgb2yuv(int r, int g, int b, int[] yuv) {
        int y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        int u = (int) ((b - y) * 0.492f);
        int v = (int) ((r - y) * 0.877f);

        yuv[0] = y;
        yuv[1] = u;
        yuv[2] = v;
    }

    /**
     * Adapted from ImageJ documentation:
     * http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector//HTMLHelp/farbraumJava.htm
     *
     * @param r
     * @param g
     * @param b
     * @param hsv
     */
    public static void rgb2hsv(int r, int g, int b, int hsv[]) {

        int min;    //Min. value of RGB
        int max;    //Max. value of RGB
        int delMax; //Delta RGB value

        min = Math.min(r, g);
        min = Math.min(min, b);

        max = Math.max(r, g);
        max = Math.max(max, b);

        delMax = max - min;

//        System.out.println("hsv = " + hsv[0] + ", " + hsv[1] + ", "  + hsv[2]);

        float H = 0f, S = 0f;
        float V = max / 255f;

        if (delMax == 0) {
            H = 0f;
            S = 0f;
        } else {
            S = delMax / 255f;
            if (r == max) {
                if (g >= b) {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60;
                } else {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60 + 360;
                }
            } else if (g == max) {
                H = (2 + (b / 255f - r / 255f) / (float) delMax / 255f) * 60;
            } else if (b == max) {
                H = (4 + (r / 255f - g / 255f) / (float) delMax / 255f) * 60;
            }
        }
//        System.out.println("H = " + H);
        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }
}
