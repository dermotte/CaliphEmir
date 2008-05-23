package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.ImageDuplicates;

import java.util.List;

/**
 * This class implements the format for returning found duplicates.<br>
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 04.08.2006
 * <br>Time: 10:20:20
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleImageDuplicates implements ImageDuplicates {
    private List<List<String>> duplicates;


    public SimpleImageDuplicates(List<List<String>> duplicates) {
        this.duplicates = duplicates;
    }

    public int length() {
        return duplicates.size();
    }

    public List<String> getDuplicate(int position) {
        return duplicates.get(position);
    }
}
