package liredemo.indexing;

import net.semanticmetadata.lire.DocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
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
    HashSet<String> todo;
    private int NUMBER_OF_SYNC_THREADS = 3;
    DocumentBuilder builder;
    LinkedList<Document> finished = new LinkedList<Document>();

    public ParallelIndexer(List<String> imageFiles, DocumentBuilder b) {
        this.imageFiles = imageFiles;
        todo = new HashSet<String>();
        todo.addAll(imageFiles);
        builder = b;
    }

    public void run() {
        ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_SYNC_THREADS);
        for (String photo : todo) {
            // System.out.println("photo.title = " + photo.title);
            pool.execute(new PhotoIndexer(photo, this));
        }
        pool.shutdown();
        while (!pool.isTerminated()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        while (todo.size()>0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public synchronized void addDoc(Document doc, String photofile) {
        if (doc!=null) finished.add(doc);
        todo.remove(photofile);
    }

    public Document getNext() {
        if (todo.size()<1) return null;
        else {
            while (finished.size()<1) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return finished.removeFirst();
        }
    }

    class PhotoIndexer implements Runnable {
        String photo;
        ParallelIndexer parent;

        PhotoIndexer(String photo, ParallelIndexer parent) {
            this.photo = photo;
            this.parent = parent;

        }

        public void run() {
            try {
                Document doc = parent.builder.createDocument(readFile(photo), photo);
                parent.addDoc(doc, photo);
            } catch (IOException e) {
                e.printStackTrace();
                parent.addDoc(null, photo);
            }

        }

        private BufferedImage readFile(String path) throws IOException {
            BufferedImage image = null;
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
            // Fallback:
            if (image == null) image = ImageIO.read(new FileInputStream(path));
            return image;
        }
    }

}
