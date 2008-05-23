package at.lux.fotoannotation;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import at.lux.components.TitlePanelFactory;

/**
 * <p/>
 * Date: 03.11.2005 <br>
 * Time: 09:32:35 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class ComponentsTest extends TestCase {
    public void testTitlePanelFactory() {

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add(TitlePanelFactory.getTitlePanel("Test: "));
        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}
