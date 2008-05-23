package net.semanticmetadata.lire.impl;

import org.apache.lucene.document.Document;

/**
 * ...
 * Date: 23.05.2008
 * Time: 16:30:20
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DissimilarityResult extends SimpleResult {
    public DissimilarityResult(float dissimilarity, Document document) {
        super(dissimilarity, document);
    }

    public int compareTo(Object o) {
        if (!(o instanceof SimpleResult)) {
            return 0;
        } else {
            int compareValue = - (int) Math.signum(distance - ((SimpleResult) o).distance);
            // Bugfix after hint from Kai Jauslin
            if (compareValue == 0 && !(document.equals(((SimpleResult) o).document)))
                compareValue = document.hashCode() - ((SimpleResult) o).document.hashCode();
            return compareValue;
        }
    }
}
