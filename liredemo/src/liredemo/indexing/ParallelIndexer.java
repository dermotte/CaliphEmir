package liredemo.indexing;

import net.semanticmetadata.lire.DocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.image.BufferedImage;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.imaging.jpeg.JpegProcessingException;

import javax.imageio.ImageIO;

import org.apache.lucene.document.Document;

/**
 * ...
 * Date: 10.06.2008
 * Time: 17:24:32
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ParallelIndexer implements Runnable {
    List<String> imageFiles;
    private int NUMBER_OF_SYNC_THREADS = 3;
    Hashtable<String, Boolean> indexThreads = new Hashtable<String, Boolean>(3);
    DocumentBuilder builder;
    LinkedList<Document> finished = new LinkedList<Document>();
    private boolean started = false;

    public ParallelIndexer(List<String> imageFiles, DocumentBuilder b) {
        this.imageFiles = new LinkedList<String>();
        assert(imageFiles!=null);
        this.imageFiles.addAll(imageFiles);
        builder = b;
    }

    public void run() {
        for (int i = 1; i < NUMBER_OF_SYNC_THREADS; i++) {
            PhotoIndexer runnable = new PhotoIndexer(this);
            Thread t = new Thread(runnable);
            t.start();
            indexThreads.put(t.getName(), false);
        }
        started = true;
    }

    public void addDoc(Document doc, String photofile) {
        synchronized (finished) {
            if (doc != null) finished.add(doc);
            Thread.yield();
        }
    }

    public synchronized Document getNext() {
        if (imageFiles.size() < 1) {
            boolean fb = true;
            for (String t : indexThreads.keySet()) {
                fb = fb && indexThreads.get(t);
            }
            if (started && fb) {
                return null;
            }
        }
        while (finished.size() < 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return finished.removeFirst();
    }

    private String getNextImage() {
        synchronized (imageFiles) {
            if (imageFiles.size() > 0) {
                return imageFiles.remove(0);

            } else return null;
        }

    }

    class PhotoIndexer implements Runnable {
        String photo;
        ParallelIndexer parent;
        private boolean hasFinished = false;

        PhotoIndexer(ParallelIndexer parent) {
            this.parent = parent;

        }

        public void run() {
            while ((photo = parent.getNextImage()) != null) {
                try {
                    Document doc = parent.builder.createDocument(readFile(photo), photo);
                    parent.addDoc(doc, photo);
                } catch (IOException e) {
                    e.printStackTrace();
                    parent.addDoc(null, photo);
                }
            }
            parent.indexThreads.put(Thread.currentThread().getName(),true);
        }

        private BufferedImage readFile(String path) throws IOException {
            BufferedImage image = null;
            if (path.toLowerCase().endsWith(".jpg")) {
                FileInputStream jpegFile = new FileInputStream(path);
                Metadata metadata = new Metadata();
                try {
                    new ExifReader(jpegFile).extract(metadata);
                    byte[] thumb = ((ExifDirectory) metadata.getDirectory(ExifDirectory.class)).getThumbnailData();
                    if (thumb != null) image = ImageIO.read(new ByteArrayInputStream(thumb));
    //            System.out.print("Read from thumbnail data ... ");
    //            System.out.println(image.getWidth() + " x " + image.getHeight());
                } catch (JpegProcessingException e) {
                    System.err.println("Could not extract thumbnail");
                    e.printStackTrace();
                } catch (MetadataException e) {
                    System.err.println("Could not extract thumbnail");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("Could not extract thumbnail");
                    e.printStackTrace();
                }
            }
            // Fallback & PNGs:
            if (image == null) image = ImageIO.read(new FileInputStream(path));
            return image;
        }

        public boolean hasFinished() {
            return hasFinished;
        }
    }


}
