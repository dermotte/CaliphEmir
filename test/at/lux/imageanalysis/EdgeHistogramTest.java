package at.lux.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * <p/>
 * Date: 06.02.2006 <br>
 * Time: 15:18:36 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class EdgeHistogramTest extends TestCase {
    public void testEdgeHistogramExtraction() throws IOException {
        BufferedImage img = ImageIO.read(new FileInputStream("testdata/I-Know 04/P1040578.JPG"));
        long time;
        int countRuns = 25;

        EdgeHistogramImplementation e2 = null;
        time = System.currentTimeMillis();
        for (int i = 0; i < countRuns; i++) {
            e2 = new EdgeHistogramImplementation(img);
        }
        System.out.println("EdgeHistogram took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");

        ScalableColorImpl sc = null;
        time = System.currentTimeMillis();
        for (int i = 0; i < countRuns; i++) {
            sc = new ScalableColorImpl(img);
        }
        System.out.println("ScalableColor took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");

        ColorLayoutImpl cl = null;
        time = System.currentTimeMillis();
        for (int i = 0; i < countRuns; i++) {
            cl = new ColorLayoutImpl(img);
        }
        System.out.println("ColorLayout took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");

//        DominantColor dc = null;
//        time = System.currentTimeMillis();
//        for (int i = 0; i < countRuns; i++) {
//            dc = new DominantColor(img);
//        }
//        System.out.println("DominantColor took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");
    }

    public void testErrorInExtraction() throws IOException {
        BufferedImage image = ImageIO.read(new File("C:\\Java\\Projects\\Lire\\src\\test\\resources\\images\\Páginas de 060305_b_Página_1_Imagem_0004_Página_08_Imagem_0002.jpg"));
        EdgeHistogramImplementation eh = new EdgeHistogramImplementation(image);
        System.out.println("eh = " + eh.getStringRepresentation());
    }

}
