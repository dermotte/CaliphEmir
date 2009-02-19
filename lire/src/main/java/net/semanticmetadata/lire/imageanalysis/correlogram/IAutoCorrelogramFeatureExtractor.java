package net.semanticmetadata.lire.imageanalysis.correlogram;
/**
 * IAutoCorrelogramFeatureExtractor
 * @author Rodrigo Carvalho Rezende <rcrezende@gmail.com> http://www.rodrigorezende.net/
 */
public interface IAutoCorrelogramFeatureExtractor {
	/**
	* extract extracts an auto-correlogram from an Image
	* @param maxFeatureValue the maximum feature (color) value
	* @param distanceSet the distance windows of auto-correlogram
	* @param img the image
	* @return float[][] the auto-correlogram A[color][distance]
	*/
	float[][] extract(int maxFeatureValue, int[] distanceSet, int[][] img);
}
