package net.semanticmetadata.lire.imageanalysis.correlogram;


/**
* NaiveAutoCorrelogramExtraction is an implementation of the naice approach to extract auto-correlogram 
* feature vector from images (Full Neighborhood is used). It is based on Huang et al paper,
* "Image Indexing Using Color Correlograms", CVPR1997. J Huang, S. R Kumar, M. Mitra, W. Zhu, R. Zabih
*
* This method is very similar to MLux FullNeighbourhood algorithm, but
* doesn't acummulate the feature frequence over different distances and
* uses the standard normalization.
* 
* @author Rodrigo Carvalho Rezende <rcrezende@gmail.com> http://www.rodrigorezende.net/
**/
public class NaiveAutoCorrelogramExtraction implements IAutoCorrelogramFeatureExtractor{
	/**
	* extract extracts an auto-correlogram from an Image
	* @param maxFeatureValue the maximum feature (color) value
	* @param distanceSet the distance windows of auto-correlogram
	* @param img the image
	* @return float[][] the auto-correlogram A[color][distance]
	*/
	public float[][] extract(int maxFeatureValue, int[] distanceSet, int[][] img) {
		long totalComplexity = 0;
		final int maxDistance = distanceSet[distanceSet.length-1];
		
		int[] histogram = new int[maxFeatureValue];
		final float[][] correlogram = new float[maxFeatureValue][maxDistance];
		
		final int W = img.length;
		final int H = img[0].length;
		
		//builds the histogram for normalization
		for (int x = 0; x < W; x++)
			for (int y = 0; y < H; y++) {
				histogram[img[x][y]]++;
				totalComplexity++;				
			}
        
		//for each distance window $d$
		int N_DIST = distanceSet.length;
    	for (int di = 0; di < N_DIST; ++di) {
    		int d = distanceSet[di];
    		//for each pixel $p$
			for (int x = 0; x < W; ++x) {
				for (int y = 0; y < H; ++y) {
    				int c = img[x][y];
    				//counts each pixel in neighborhood (distance $d$) which has the same color of pixel $p$

    				//horizontal
    				for (int dx = -d; dx <= d; dx++) {
    					int X = x + dx, Y = y - d;
    					if (0<=X&&X<W&&0<=Y&&Y<H && img[X][Y] == c) {
    						correlogram[c][di]++;
							totalComplexity++;
						}
    					Y = y + d;
    					if (0<=X&&X<W&&0<=Y&&Y<H && img[X][Y] == c) {
    						correlogram[c][di]++;
							totalComplexity++;
						}
    				}
    				//vertical
    				for (int dy = -d + 1; dy <= d - 1; dy++) {
    					int X = x - d, Y = y + dy;
    					if (0<=X&&X<W&&0<=Y&&Y<H && img[X][Y] == c){
    						correlogram[c][di]++;
							totalComplexity++;
						}
    					X = x + d;
    					if (0<=X&&X<W&&0<=Y&&Y<H && img[X][Y] == c) {
    						correlogram[c][di]++;
							totalComplexity++;
						}
    				}
    			}
    		}
			//normalize the feature vector
    		for (int c = 0; c < maxFeatureValue; ++c)
    			if (histogram[c] > 0)
    				correlogram[c][di] = (float) correlogram[c][di] / (((float)histogram[c]) * 8.0f * d);
    	}
//		System.out.println("Complexity: "+((float)totalComplexity/(H*W))+"*O(|I|)");
		return correlogram;
    }

//	public static void main(String[] args) {
//		int[][] I = new int[200][200];
//		float[][] A = null;
//		long t0,tf;
//		int C = 16;
//		int[] D = {1,3,5,7};
//
//		for(int i=0;i<I.length; i++)
//			for(int j=0;j<I[i].length; j++)
//				I[i][j] = ((i+1)*(j*j+1))%C;
//
//		tf = System.currentTimeMillis();
//		NaiveAutoCorrelogramExtraction naivACorrExt = new NaiveAutoCorrelogramExtraction();
//		for(int i=0;i<10;i++) {
//			t0 = tf;
//			A = naivACorrExt.extract(C, D, I);
//			tf = System.currentTimeMillis();
//			System.out.println("Exctraction "+(i+1)+" time: "+(tf-t0)+"ms");
//		}
//		print(A);
//
//
//	}

//	static void print(float[][] M) {
//		System.out.println();
//		for(int i=0;i<M.length;i++) {
//			for(int j=0;j<M[i].length;j++) {
//				System.out.print(M[i][j]+" ");
//			}
//			System.out.println();
//		}
//	}
//	static void print(int[][] M) {
//		System.out.println();
//		for(int i=0;i<M.length;i++) {
//			for(int j=0;j<M[i].length;j++) {
//				System.out.print(M[i][j]+" ");
//			}
//			System.out.println();
//		}
//	}
}


