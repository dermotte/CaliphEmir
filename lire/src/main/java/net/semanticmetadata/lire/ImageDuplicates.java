package net.semanticmetadata.lire;

import java.util.List;

/**
 * This interface specifies the format for returning found duplicates.<br>
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 04.08.2006
 * <br>Time: 10:20:20
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface ImageDuplicates {
    /**
     * Returns the size of the result list.
     *
     * @return the size of the result list.
     */
    public int length();

    /**
     * Returns the list of duplicates at given position
     *
     * @param position defines the position.
     * @return the list of document identifiers at given position.
     */
    public List<String> getDuplicate(int position);
}
