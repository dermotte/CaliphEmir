package net.semanticmetadata.lire.imageanalysis;

import at.lux.imageanalysis.VisualDescriptor;

import java.awt.image.BufferedImage;

/**
 * This interface is a bit more restrictive than VisualDescriptor. It is needed for GenericDocumentBuilder. 
 * Date: 28.05.2008
 * Time: 14:44:16
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface LireFeature extends VisualDescriptor {
    public void extract(BufferedImage bimg);
}
