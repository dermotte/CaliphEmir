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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.StringTokenizer;

import net.semanticmetadata.lire.imageanalysis.correlogram.DynamicProgrammingAutoCorrelogramExtraction;
import net.semanticmetadata.lire.imageanalysis.correlogram.IAutoCorrelogramFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.correlogram.MLuxAutoCorrelogramExtraction;
import net.semanticmetadata.lire.imageanalysis.correlogram.NaiveAutoCorrelogramExtraction;
import at.lux.imageanalysis.VisualDescriptor;

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
    private float[][] correlogram;
    private int[] distanceSet;
    private int numBins;
    private float quantH_f;
    private float quantS_f;
    private float quantV_f;

    
    
    private static final ExtractionMethod DEFAULT_EXTRACTION_METHOD = ExtractionMethod.MluxAlgorithm;   
    private IAutoCorrelogramFeatureExtractor extractionAlgorithm;
    
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
     * Defines which algorithm to use to extract the features vector
     */
    public enum ExtractionMethod {
    	MluxAlgorithm,
    	NaiveHuangAlgorith,
    	DynamicProgrammingHuangAlgorithm
    }
    
    public AutoColorCorrelogram() {
    	this(64,new int[]{1,2,3,4},null);
    }

    /**
     * Creates a new AutoColorCorrelogram, where the distance k is limited to a maximum of
     * maxDistance (see publication mentioned above)
     *
     * @param maxDistance upper limit of k
     */
    public AutoColorCorrelogram(int maxDistance) {
    	this(maxDistance,Mode.SuperFast);
    }

    /**
     * Creates a new AutoColorCorrelogram using a maximum L_inf pixel distance for analysis and given mode
     * @param maxDistance maximum L_inf pixel distance for analysis
     * @param mode        the mode of calculation (determines the speed of extraction)
     */
    public AutoColorCorrelogram(int maxDistance, Mode mode) {
        this(64,null,new MLuxAutoCorrelogramExtraction(mode));
        int[] D = new int[maxDistance];
        for(int i=0;i<maxDistance;i++) D[i]=i+1;
        this.distanceSet = D;
    }
    /**
     * Creates a new AutoColorCorrelogram, where the distance k is in distance set
     * @param distanceSet distance set
     */
    public AutoColorCorrelogram(int[] distanceSet) {
        this(64,distanceSet,null);
    }
    /**
     * Creates a new AutoCorrelogram with specified algorithm of extraction and distance set
     * @param distanceSet distance set
     * @param extractionAlgorith the algorithm to extract 
     */
    public AutoColorCorrelogram(int[] distanceSet, IAutoCorrelogramFeatureExtractor extractionAlgorith) {
        this(64,distanceSet,extractionAlgorith);
    }

    /**
     * Creates a new AutoCorrelogram with specified algorithm of extraction
     * Uses distance set {1,2,3,4} which is chosen to be compatible with legacy code
     * @param extractionAlgorith the algorithm to extract 
     */
    public AutoColorCorrelogram(IAutoCorrelogramFeatureExtractor extractionAlgorith) {
        this(64,new int[]{1,2,3,4},extractionAlgorith);
    }

    /**
     * Creates a new AutoColorCorrelogram using a maximum L_inf pixel distance for analysis and given mode
     *
     * @param maxDistance maximum L_inf pixel distance for analysis
     * @param mode        the mode of calculation (determines the speed of extraction)
     */
    public AutoColorCorrelogram(int numBins, int[] distanceSet, IAutoCorrelogramFeatureExtractor extractionAlgorith) {
    	this.numBins = numBins;
        this.distanceSet = distanceSet;        

        if(extractionAlgorith == null){
        	switch(DEFAULT_EXTRACTION_METHOD) {	
        	case MluxAlgorithm:
        		this.extractionAlgorithm = new MLuxAutoCorrelogramExtraction();
        		break;
        	case NaiveHuangAlgorith:
        		this.extractionAlgorithm  = new NaiveAutoCorrelogramExtraction();
        		break;
        	case DynamicProgrammingHuangAlgorithm:
        		this.extractionAlgorithm  = DynamicProgrammingAutoCorrelogramExtraction.getInstance();
        		break;
        	}
        } else this.extractionAlgorithm = extractionAlgorith;
        
        if (numBins < 17) {
            quantH_f = 4f;
            quantS_f = 2f;
            quantV_f = 2f;
            this.numBins = 16;
        } else if (numBins < 33) {
            quantH_f = 8f;
            quantS_f = 2f;
            quantV_f = 2f;
            this.numBins = 32;
        } else if (numBins < 65) {
            quantH_f = 8f;
            quantS_f = 4f;
            quantV_f = 2f;
            this.numBins = 64;
        } else if (numBins < 129) {
            quantH_f = 8f;
            quantS_f = 4f;
            quantV_f = 4f;
            this.numBins = 128;
        } else {
            quantH_f = 16f;
            quantS_f = 4f;
            quantV_f = 4f;
            this.numBins = 256;
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

    private static int[][][] hsvImage(Raster r) {
        int[][][] pixels = new int[r.getWidth()][r.getHeight()][3];
        // quantize colors for each pixel (done in HSV color space):
        int[] pixel = new int[3];
        for (int x = 0; x < r.getWidth(); x++) {
            for (int y = 0; y < r.getHeight(); y++) {
                // converting to HSV:
                int[] hsv = new int[3];
                convertRgbToHsv(r.getPixel(x, y, pixel), hsv);
                // quantize the actual pixel:
                pixels[x][y] = hsv;
            }
        }
        return pixels;
    	
    }

    public void extract(BufferedImage bi) {
    	final Raster r = bi.getRaster();
        int[][][] hsvImage = hsvImage(r);        
        extract(hsvImage);    	
    }
    public void extract(int[][][] img) {
    	final int W = img.length;
    	final int H = img[0].length;
    	int[][] quantPixels = new int[W][H];
    	
        // quantize colors for each pixel (done in HSV color space):
        for (int x = 0; x < W; x++)
            for (int y = 0; y < H; y++) 
            	quantPixels[x][y] = quantize(img[x][y]);

        this.correlogram = this.extractionAlgorithm.extract(this.numBins, this.distanceSet, quantPixels);
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
    	int maxDistance = this.distanceSet.length;
        StringBuilder sb = new StringBuilder(numBins * maxDistance);
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
        correlogram = new float[numBins][Integer.parseInt(st.nextToken())];
        for (int i = 0; i < correlogram.length; i++) {
            for (int j = 0; j < correlogram[i].length; j++) {
                if (!st.hasMoreTokens())
                    throw new IndexOutOfBoundsException("Too few numbers in string representation!");
                correlogram[i][j] = Float.parseFloat(st.nextToken());
            }
        }
    }
}
