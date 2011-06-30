package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.BasicFeatures;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;

public class BasicDocumentBuilder extends AbstractDocumentBuilder {

	
	public Document createDocument(BufferedImage image, String identifier) {
		BasicFeatures classifier = new BasicFeatures();
		classifier.extract(image);
		Document doc = new Document();

        doc.add(new Field(DocumentBuilder.FIELD_NAME_BASIC_FEATURES, classifier.getStringRepresentation(), Field.Store.YES,Field.Index.NO));
        
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		return doc;
	}

}
