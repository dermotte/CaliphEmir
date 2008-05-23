package net.semanticmetadata.lire;

import junit.framework.TestCase;
import org.apache.lucene.document.Document;

import java.io.FileInputStream;
import java.io.IOException;
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
 * This file is part of Caliph & Emir
 * Date: 31.01.2006
 * Time: 23:54:18
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DocumentBuilderFactoryTest extends TestCase {
    public void testGetDefaultDocumentBuilder() {
        DocumentBuilder builder = DocumentBuilderFactory.getDefaultDocumentBuilder();
        testBuilder(builder);
    }

    public void testGetExtensiveDocumentBuilder() {
        DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
        testBuilder(builder);
    }

    public void testGetFastDocumentBuilder() {
        DocumentBuilder builder = DocumentBuilderFactory.getFastDocumentBuilder();
        testBuilder(builder);
    }

    private void testBuilder(DocumentBuilder builder) {
        assertNotNull(builder);
        try {
            String identifier = "img01.JPG";
            Document doc = builder.createDocument(new FileInputStream("./src/test/resources/images/img01.JPG"), identifier);
            assertNotNull(doc);
            assertEquals(identifier, doc.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        } catch (IOException e) {
            fail(e.toString());
        }

    }

}
