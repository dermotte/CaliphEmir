package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import at.lux.imageanalysis.VisualDescriptor;

/**
 * This class allows to create a DocumentBuilder based on a class implementing LireFeature.
 * Date: 28.05.2008
 * Time: 14:32:15
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GenericDocumentBuilder  extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final int MAX_IMAGE_DIMENSION = 1024;
    Class descriptorClass;
    String fieldName;

    /**
     * Creating a new Documentbuilder based on a class based on the interface {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     * @param descriptorClass has to implement {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     * @param fieldName the field name in the index.
     */
    public GenericDocumentBuilder(Class descriptorClass, String fieldName) {
        this.descriptorClass = descriptorClass;
        this.fieldName = fieldName;
    }

    public Document createDocument(BufferedImage image, String identifier) {
        String featureString = "";
        assert (image != null);
        BufferedImage bimg = image;
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to garuantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > MAX_IMAGE_DIMENSION) {
            bimg = ImageUtils.scaleImage(image, MAX_IMAGE_DIMENSION);
        }
        Document doc = null;
        logger.finer("Starting extraction from image [" + descriptorClass.getName() + "].");
        try {
            LireFeature vd = (LireFeature) descriptorClass.newInstance();
            vd.extract(bimg);
            featureString = vd.getStringRepresentation();
            logger.fine("Extraction finished [" + descriptorClass.getName() + "].");

            doc = new Document();
            doc.add(new Field(fieldName, featureString, Field.Store.YES, Field.Index.NO));
            if (identifier != null)
                doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));

        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic document builder: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic document builder: " + e.getMessage());
        }
        return doc;
    }
}
