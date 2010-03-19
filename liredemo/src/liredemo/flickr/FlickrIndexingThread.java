package liredemo.flickr;

import liredemo.LireDemoFrame;
import liredemo.indexing.MetadataBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * (c) 2002-2007 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FlickrIndexingThread extends Thread {
    LireDemoFrame parent;
    public static final String cacheDirectory = "./flickrphotos/";
    private int numberOfPhotosToIndex = 100;

    /**
     * Creates a new instance of FlickrIndexingThread
     *
     * @param parent
     */
    public FlickrIndexingThread(LireDemoFrame parent) {
        this.parent = parent;
    }

    public FlickrIndexingThread(LireDemoFrame parent, int maxNumberOfPhotos) {
        this.parent = parent;
        this.numberOfPhotosToIndex = maxNumberOfPhotos;
    }

    public void run() {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        df.setMaximumFractionDigits(0);
        df.setMinimumFractionDigits(0);
        try {
            File cacheDir = new File(cacheDirectory);
            if (!cacheDir.exists()) cacheDir.mkdir();
            parent.progressBarIndexing.setValue(0);
            parent.progressBarIndexing.setString("Getting photos from Flickr");
            List<FlickrPhoto> images = new LinkedList<FlickrPhoto>();
            HashSet<String> titles = new HashSet<String>(numberOfPhotosToIndex);
            try {
                while (images.size() < numberOfPhotosToIndex) {
                    List<FlickrPhoto> photos = FlickrPhotoGrabber.getRecentPhotos();
                    for (FlickrPhoto photo : photos) {
                        // check if it is already there:
                        if (!titles.contains(photo.url)) {
                            titles.add(photo.url);
                            if (images.size() < numberOfPhotosToIndex) images.add(photo);
                        } else {
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    parent.progressBarIndexing.setString("Getting photos from Flickr: " + images.size() + " found.");
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new SimpleAnalyzer());
            wrapper.addAnalyzer("tags", new WhitespaceAnalyzer());

//        iw = new IndexWriter(indexPath + "-new", wrapper, true, IndexWriter.MaxFieldLength.UNLIMITED);

            boolean create = !parent.checkBoxAddToExisintgIndex.isSelected();
            IndexWriter iw;
            if (create)
                iw = new IndexWriter(FSDirectory.open(new File(parent.textfieldIndexName.getText())), new SimpleAnalyzer(), create, IndexWriter.MaxFieldLength.UNLIMITED);
            else
                iw = new IndexWriter(FSDirectory.open(new File(parent.textfieldIndexName.getText())), new SimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
            int builderIdx = parent.selectboxDocumentBuilder.getSelectedIndex();
            DocumentBuilder builder = new MetadataBuilder();
            int count = 0;
            long time = System.currentTimeMillis();
            FlickrDownloadThread downloader = new FlickrDownloadThread(images, builder);
            new Thread(downloader).start();
            Document doc = null;
            while ((doc = downloader.getCurrentDoc()) != null) {
                try {
                    iw.addDocument(doc);
                } catch (Exception e) {
                    System.err.println("Could not add document");
                    // e.printStackTrace();
                }
                count++;
                float percentage = (float) count / (float) images.size();
                parent.progressBarIndexing.setValue((int) Math.floor(100f * percentage));
                float msleft = (float) (System.currentTimeMillis() - time) / percentage;
                float secLeft = msleft * (1 - percentage) / 1000f;
                String toPaint;
                if (secLeft > 60) toPaint = "~ " + Math.ceil(secLeft / 60) + " min. left";
                else if (secLeft > 30) toPaint = "< 1 min. left";
                else toPaint = "< 30 sec. left";
                parent.progressBarIndexing.setString(toPaint);
            }
            long timeTaken = (System.currentTimeMillis() - time);
            float sec = ((float) timeTaken) / 1000f;
            parent.progressBarIndexing.setValue(100);
            parent.progressBarIndexing.setString(Math.round(sec) + " sec. for " + count + " files");
            parent.buttonStartIndexing.setEnabled(true);
            iw.optimize();
            iw.close();

        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }

    private BufferedImage readFile(String path) throws IOException {
        BufferedImage image = ImageIO.read(new URL(path));
        return image;
    }

}