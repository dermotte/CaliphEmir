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
package at.lux.retrieval.clustering.suffixtree;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 03.06.2004
 *         Time: 16:17:52
 */

public class WordIndex {
    private HashMap<String, HashSet<StcDocument>> index;
    private HashSet<StcDocument> documents;

    public WordIndex() {
        index = new HashMap<String, HashSet<StcDocument>>();
        documents = new HashSet<StcDocument>();
    }

    public void addToIndex(String[] tokens, StcDocument stcDocument) {
        documents.add(stcDocument);
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (!index.containsKey(token)) {
                HashSet<StcDocument> documents = new HashSet<StcDocument>();
                index.put(token, documents);
            }
            index.get(token).add(stcDocument);
        }
    }

    public int getDocumentCount(String token) {
        if (!index.containsKey(token))
            return 0;
        else
            return index.get(token).size();
    }

    public int getNumberOfDocuments() {
        return documents.size();
    }

    /**
     * Returns a double in [0,1] giving the percentage in how many docs this terms occurs.
     *
     * @param token
     * @return
     */
    public double getDocumentFrequency(String token) {
        double numDocs = (double) getNumberOfDocuments();
        double numDocsWithTerm = (double) getDocumentCount(token);
        return numDocsWithTerm / numDocs;
    }
}
