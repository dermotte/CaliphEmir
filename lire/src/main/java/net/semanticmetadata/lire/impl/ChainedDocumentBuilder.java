package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: mlux
 * Date: 20.02.2007
 * Time: 15:11:59
 * To change this template use File | Settings | File Templates.
 */
public class ChainedDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(ChainedDocumentBuilder.class.getName());
    private LinkedList<DocumentBuilder> builders;
    private boolean docsCreated = false;

    public ChainedDocumentBuilder() {
        builders = new LinkedList<DocumentBuilder>();
    }

    public void addBuilder(DocumentBuilder builder) {
        if (docsCreated)
            throw new UnsupportedOperationException("Cannot modify chained builder after documents have been created!");
        builders.add(builder);
    }

    public Document createDocument(BufferedImage image, String identifier) {
        docsCreated = true;
        Document doc = new Document();
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
        // this is unfortunately rather slow, but however it works :)
        if (builders.size() >= 1) {
            for (DocumentBuilder builder : builders) {
                Document d = builder.createDocument(image, identifier);
                for (Iterator iterator = d.getFields().iterator(); iterator.hasNext();) {
                    Field f = (Field) iterator.next();
                    if (!f.name().equals(DocumentBuilder.FIELD_NAME_IDENTIFIER)) {
                        doc.add(f);
                    }
                }
            }
        }
        return doc;
    }
}
