package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.imageanalysis.LireFeature;

/**
 * User: Mathias, mathias@juggle.at
 * Date: 28.07.11
 * Time: 12:03
 */
public class GenericFastDocumentBuilder extends GenericDocumentBuilder {
    public GenericFastDocumentBuilder(Class<? extends LireFeature> descriptorClass, String fieldName) {
        super(descriptorClass, fieldName, Mode.Fast);
    }
}
