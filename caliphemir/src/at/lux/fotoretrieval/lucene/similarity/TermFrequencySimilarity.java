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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
package at.lux.fotoretrieval.lucene.similarity;

import org.apache.lucene.search.Similarity;

/**
 * This class implementa a "term frequency only" measure.
 * Date: 10.05.2005
 * Time: 23:57:34
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TermFrequencySimilarity extends Similarity{
    public float coord(int overlap, int maxOverlap) {
        return 1f;
    }

    public float idf(int docFreq, int numDocs) {
        return 1f;
    }

    public float lengthNorm(String fieldName, int numTokens) {
        return 1f;
    }

    public float queryNorm(float sumOfSquaredWeights) {
        return 1f;
    }

    public float sloppyFreq(int distance) {
        return 1f;
    }

    public float tf(float freq) {
        return freq;
    }
}
