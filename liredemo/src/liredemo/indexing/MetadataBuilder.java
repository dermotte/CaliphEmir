package liredemo.indexing;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SiftDocumentBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

/**
 * Created by: Mathias Lux, mathias@juggle.at
 * Date: 18.02.2010
 * Time: 15:20:59
 */
public class MetadataBuilder extends ChainedDocumentBuilder {
    public MetadataBuilder() {
        super();
        addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getExtensiveDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getDefaultAutoColorCorrelationDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
        addBuilder(new SiftDocumentBuilder());
    }

    @Override
    public Document createDocument(BufferedImage bufferedImage, String s) {
        Document d = super.createDocument(bufferedImage, s);
        // extract available metadata:
        Metadata metadata = new Metadata();
        try {
            new ExifReader(new FileInputStream(s)).extract(metadata);
            new IptcReader(new FileInputStream(s)).extract(metadata);
            // add metadata to document:
            Iterator i = metadata.getDirectoryIterator();
            while (i.hasNext()) {
                Directory dir = (Directory) i.next();
                String prefix = dir.getName();
                Iterator ti = dir.getTagIterator();
                while (ti.hasNext()) {
                    Tag tag = (Tag) ti.next();
                    // System.out.println(prefix+"-"+tag.getTagName()+" -> " + dir.getString(tag.getTagType()));
                    // add to document:
                    d.add(new Field(prefix + "-" + tag.getTagName(), dir.getString(tag.getTagType()), Field.Store.YES, Field.Index.ANALYZED));
                }
            }
        } catch (JpegProcessingException e) {
            System.err.println("Error reading EXIF & IPTC metadata from image file.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return d;
    }
}
