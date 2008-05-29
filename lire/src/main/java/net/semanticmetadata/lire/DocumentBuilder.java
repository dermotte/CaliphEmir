package net.semanticmetadata.lire;

import org.apache.lucene.document.Document;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
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
 * <h2>Creating an Index</h2>
 * <p/>
 * Use DocumentBuilderFactory to create a DocumentBuilder, which
 * will create Lucene Documents from images. Add this documents to
 * an index like this:
 * <p/>
 * <pre>
 * System.out.println(">> Indexing " + images.size() + " files.");
 * DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
 * IndexWriter iw = new IndexWriter(indexPath, new SimpleAnalyzer(), true);
 * int count = 0;
 * long time = System.currentTimeMillis();
 * for (String identifier : images) {
 * Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
 * iw.addDocument(doc);
 * count ++;
 * if (count % 25 == 0) System.out.println(count + " files indexed.");
 * }
 * long timeTaken = (System.currentTimeMillis() - time);
 * float sec = ((float) timeTaken) / 1000f;
 * <p/>
 * System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
 * iw.optimize();
 * iw.close();
 * </pre>
 * <p/>
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 31.01.2006
 * <br>Time: 23:02:00
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface DocumentBuilder {
    public static final int MAX_IMAGE_SIDE_LENGTH = 800;

    public static final String FIELD_NAME_SCALABLECOLOR = "descriptorScalableColor";
    public static final String FIELD_NAME_COLORLAYOUT = "descriptorColorLayout";
    public static final String FIELD_NAME_EDGEHISTOGRAM = "descriptorEdgeHistogram";
    public static final String FIELD_NAME_AUTOCOLORCORRELOGRAM = "featureAutoColorCorrelogram";
    public static final String FIELD_NAME_COLORHISTOGRAM = "featureColorHistogram";
    public static final String FIELD_NAME_CEDD = "featureCEDD";
    public static final String FIELD_NAME_TAMURA = "featureTAMURA";
    public static final String FIELD_NAME_IDENTIFIER = "descriptorImageIdentifier";

    /**
     * Creates a new Lucene document from a BufferedImage. The identifier can be used like an id
     * (e.g. the file name or the url of the image)
     *
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or an URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     */
    public Document createDocument(BufferedImage image, String identifier);

    /**
     * Creates a new Lucene document from an InputStream. The identifier can be used like an id
     * (e.g. the file name or the url of the image)
     *
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or an URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     * @throws IOException in case the image cannot be retrieved from the InputStream
     */
    public Document createDocument(InputStream image, String identifier) throws IOException;
}
