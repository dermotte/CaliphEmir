package net.semanticmetadata.lire.imageanalysis.utils;

/**
 * ...
 * Date: 28.05.2008
 * Time: 11:26:28
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Quantization {
    /**
     * Simple linear quantization for 3 component color spaces with int values (e.g. rgb).
     * @param pixel the actual pixel values in a int[3] array
     * @param numBins the number of bins (quantization levels)
     * @param maxPixelValue upper border for pixel limits. pixel values in pixel[] are from [0-maxPixelValue]
     * @return the quantized values < numbins
     */
    public static int quantUniformly(int[] pixel, int numBins, double maxPixelValue) {
        assert(pixel.length == 3);
        double quant = (maxPixelValue * maxPixelValue * maxPixelValue) / (double) numBins;
        return (int) ((pixel[0] + pixel[1] * maxPixelValue + pixel[2] * maxPixelValue * maxPixelValue) / quant);
    }
}
