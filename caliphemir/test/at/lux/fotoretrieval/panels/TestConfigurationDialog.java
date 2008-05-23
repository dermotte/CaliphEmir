package at.lux.fotoretrieval.panels;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

import at.lux.fotoretrieval.EmirConfiguration;

/**
 * <p/>
 * Date: 11.01.2006 <br>
 * Time: 14:22:00 <br>
 * Know-Center Graz, Inffeldgasse 21a, 8010 Graz, AUSTRIA <br>
 *
 * @author Mathias Lux, mlux@know-center.at
 */
public class TestConfigurationDialog {
    public static void main(String[] args) {
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel(new PlasticLookAndFeel());
        } catch (Exception e) {
            System.err.println("Could not set Look & Feel: " + e.toString());
        }

        JDialog d = new JDialog((Frame) null, "Test Configuration");
        d.setSize(640, 480);
//        d.getContentPane().add(new ConfigurationSelectionPanel(), BorderLayout.WEST);
        EmirConfiguration.getInstance(new Properties());
        ConfigurationDialogPanel config = new ConfigurationDialogPanel(d);
//        config.configurationPanel.add(new MdsConfiguration(), BorderLayout.CENTER);
        d.getContentPane().add(config, BorderLayout.CENTER);
        d.setVisible(true);
    }
}
