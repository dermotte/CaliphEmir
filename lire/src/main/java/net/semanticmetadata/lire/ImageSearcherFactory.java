package net.semanticmetadata.lire;

import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram;
import net.semanticmetadata.lire.imageanalysis.Tamura;
import net.semanticmetadata.lire.impl.*;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * <h2>Searching in an Index</h2>
 * Use the ImageSearcherFactory for creating an ImageSearcher, which will retrieve the images
 * for you from the index.
 * <p/>
 * <pre>
 * IndexReader reader = IndexReader.open(indexPath);
 * ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
 * FileInputStream imageStream = new FileInputStream("image.jpg");
 * BufferedImage bimg = ImageIO.read(imageStream);
 * // searching for an image:
 * ImageSearchHits hits = null;
 * hits = searcher.search(bimg, reader);
 * for (int i = 0; i < 5; i++) {
 * System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
 * }
 * <p/>
 * // searching for a document:
 * Document document = hits.doc(0);
 * hits = searcher.search(document, reader);
 * for (int i = 0; i < 5; i++) {
 * System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
 * }
 * </pre>
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 03.02.2006
 * <br>Time: 00:30:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ImageSearcherFactory {
    /**
     * Default number of maximum hits.
     */
    public static int NUM_MAX_HITS = 100;

    /**
     * Creates a new simple image searcher with the desired number of maximum hits.
     *
     * @param maximumHits
     * @return the searcher instance
     */
    public static ImageSearcher createSimpleSearcher(int maximumHits) {
        return new SimpleImageSearcher(maximumHits);
    }

    /**
     * Returns a new default ImageSearcher with a predefined number of maximum
     * hits defined in the {@link ImageSearcherFactory#NUM_MAX_HITS}
     *
     * @return the searcher instance
     */
    public static ImageSearcher createDefaultSearcher() {
        return new SimpleImageSearcher(NUM_MAX_HITS);
    }

    /**
     * Returns a new ImageSearcher with the given number of maximum hits
     * which only takes the overall color into account. texture and color
     * distribution are ignored.
     *
     * @param maximumHits defining how many hits are returned in max (e.g. 100 would be ok)
     * @return the ImageSearcher
     * @see ImageSearcher
     */
    public static ImageSearcher createColorOnlySearcher(int maximumHits) {
        return createWeightedSearcher(maximumHits, 1f, 1f, 0f);
    }

    /**
     * Returns a new ImageSearcher with the given number of maximum hits and
     * the specified weights on the different matching aspects. All weights
     * should be in [0,1] whereas a weight of 0 implies that the feature is
     * not taken into account for searching. Note that the effect is relative and
     * can only be fully applied if the {@link DocumentBuilderFactory#getExtensiveDocumentBuilder() extensive DocumentBuilder}
     * is used.
     *
     * @param maximumHits             defining how many hits are returned in max
     * @param colorHistogramWeight    a weight in [0,1] defining the importance of overall color in the images
     * @param colorDistributionWeight a weight in [0,1] defining the importance of color distribution (which color where) in the images
     * @param textureWeight           defining the importance of texture (which edges where) in the images
     * @return the searcher instance or NULL if the weights are not appropriate, eg. all 0 or not in [0,1]
     * @see DocumentBuilderFactory
     */
    public static ImageSearcher createWeightedSearcher(int maximumHits,
                                                       float colorHistogramWeight,
                                                       float colorDistributionWeight,
                                                       float textureWeight) {
        if (isAppropriateWeight(colorHistogramWeight)
                && isAppropriateWeight(colorDistributionWeight)
                && isAppropriateWeight(textureWeight)
                && (colorHistogramWeight + colorDistributionWeight + textureWeight > 0f))
            return new SimpleImageSearcher(maximumHits, colorHistogramWeight, colorDistributionWeight, textureWeight);
        else
            return null;
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits number of hits returned.
     * @return
     */
    public static ImageSearcher createDefaultCorrelogramImageSearcher(int maximumHits) {
        return new CorrelogramImageSearcher(maximumHits, AutoColorCorrelogram.Mode.FullNeighbourhood);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits number of hits returned.
     * @return
     */
    public static ImageSearcher createFastCorrelogramImageSearcher(int maximumHits) {
        return new CorrelogramImageSearcher(maximumHits, AutoColorCorrelogram.Mode.SuperFast);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.CEDD}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createCEDDImageSearcher(int maximumHits) {
        return new GenericImageSearcher(maximumHits, CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
    }


    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createColorHistogramImageSearcher(int maximumHits) {
        return new GenericImageSearcher(maximumHits, SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createTamuraImageSearcher(int maximumHits) {
        return new GenericImageSearcher(maximumHits, Tamura.class, DocumentBuilder.FIELD_NAME_TAMURA);
    }


    /**
     * Checks if the weight is in [0,1]
     *
     * @param f the weight to check
     * @return true if the weight is in [0,1], false otherwise
     */
    private static boolean isAppropriateWeight(float f) {
        boolean result = false;
        if (f <= 1f && f >= 0) result = true;
        return result;

    }
}
