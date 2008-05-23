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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
package at.lux.fotoannotation.dialogs;

import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.AnnotationToolkit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

public class AboutDialog extends JDialog {
    private JLabel freeText;
    private JLabel label, status, desc;

    public AboutDialog(Frame owner) {
        super(owner, true);
        Properties buildProps = new Properties();
        try {
            buildProps.load(AboutDialog.class.getResource("build.properties").openStream());
        } catch (IOException e) {
            debug("Build properties could not be loaded!");
        }
        String build = "";
        if (buildProps.getProperty("build-number") != null) {
            build = " (build #" + buildProps.getProperty("build-number") + ")";
        }
        this.setTitle("About " + AnnotationToolkit.PROGRAM_NAME + " " + AnnotationToolkit.PROGRAM_VERSION + build);

        BufferedImage img =  null;
        try {
//            img = ImageIO.read(AnnotationFrame.class.getResourceAsStream("data/AboutCaliph.png"));
            img = ImageIO.read(getClass().getResource("/resources/images/AboutCaliph.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FullSizeImagePanel panel = new FullSizeImagePanel(img);
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }
        });

/*
        freeText = new JLabel("<html>© 2002-2005 by Mathias Lux, mathias@juggle.at</html>", JLabel.CENTER);
        freeText.setFont(freeText.getFont().deriveFont(Font.ITALIC, 12f));
        status = new JLabel("<html>Know-Center<br>Inffeldgasse 21a<br>8010 Graz<br>Austria</html>", JLabel.CENTER);
        status.setFont(status.getFont().deriveFont(10f));
        status.setIcon(new ImageIcon(AnnotationFrame.class.getResource("data/caliph-icon.png")));
        desc = new JLabel("<html>" +
                AnnotationToolkit.PROGRAM_NAME + " is a java based Common And LIght-weight PHoto annotation system. All metadata is saved as MPEG-7.<br><br>" +
                "Thanks to (alphabetically order): <ul><li>Jutta Becker (jutta.becker@acm.org)</li><li>Michael Granitzer (mgrani@know-center.at)</li><li>Wolfgang Kienreich (wkien@know-center.at)</li><li>Werner Klieber (wklieber@know-center.at)</li><li>Harald Kosch (harald.kosch@itec.uni-klu.ac.at)</li><li>Harald Krottmaier (hkrott@iicm.edu)</li><li>Helmut Neuschmied (helmut.neuschmied@joanneum.at)</li><li>Vedran Sabol (vsabol@know-center.at)</li></ul>" +
                "<div align=\"center\">The Know-Center is a Competence Center funded within the Austrian Competence Center program <b>K plus</b> under the auspices of the Austrian Ministry of Transport, Innovation and Technology (http://www.kplus.at)</div>" +
                "<br><div align=\"center\">Parts of this software are taken from a project called <b>IMB</b>, developed by the <b>Know-Center</b> in cooperation with <b>Joanneum Research</b> (http://www.joanneum.at)</div>" +
                "<br><div align=\"center\">Started in the context of a lecture hold by Harald Krottmaier, <b>IICM - TU Graz</b> (http://www.iicm.edu)</div>" +
                "</html>", JLabel.CENTER);
        desc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        desc.setPreferredSize(new Dimension(360, 320));
        desc.setFont(desc.getFont().deriveFont(9f));
        label = new JLabel("About " + AnnotationToolkit.PROGRAM_NAME + " " + AnnotationToolkit.PROGRAM_VERSION + "", JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        JPanel bpane = new JPanel(new BorderLayout());
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(ok);
        bpane.add(desc, BorderLayout.SOUTH);
        bpane.add(status, BorderLayout.CENTER);
        bpane.add(freeText, BorderLayout.NORTH);

        JPanel AboutPane = new JPanel(new BorderLayout());
        AboutPane.setBorder(BorderFactory.createEtchedBorder());
        AboutPane.add(label, BorderLayout.NORTH);
        AboutPane.add(bpane, BorderLayout.CENTER);
        this.getContentPane().add(AboutPane, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
*/
        this.getContentPane().add(panel, BorderLayout.CENTER);
    }

    private void debug(String message) {
        System.out.println("[at.lux.fotoannotation.dialogs.AboutDialog] " + message);
    }
}
