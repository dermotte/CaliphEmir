package at.lux.retrieval.suffixtreemodel;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * The TermFrequencyWalker is used to calculate the similarity
 * weighted by term frequency using the SuffixTree Model.
 * This file is part of Caliph & Emir
 * Date: 19.02.2006
 * Time: 10:36:02
 *
 * @author Mathias Lux, mathias@juggle.at
 * @see at.lux.retrieval.suffixtreemodel.SuffixTree
 */
public class TermFrequencyWalker {
    int countEdges;
    double sum;
    int countCorpusDocuments;

    public TermFrequencyWalker() {
        init();
    }

    private void init() {
        sum = 0;
        countEdges = 0;
        countCorpusDocuments = 1;
    }

    public int getCountEdges() {
        return countEdges;
    }

    public double getSum() {
        return sum;
    }

    public void addToSum(double summand) {
        sum += summand;
    }

    public void incrementCountEdges() {
        countEdges++;
    }

    public int getCountCorpusDocuments() {
        return countCorpusDocuments;
    }

    public void setCountCorpusDocuments(int countCorpusDocuments) {
        this.countCorpusDocuments = countCorpusDocuments;
    }
}
