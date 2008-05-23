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
package at.lux.fotoretrieval.dialogs;

import at.lux.fotoannotation.AnnotationToolkit;
import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.dialogs.FullSizeImagePanel;
import at.lux.fotoretrieval.RetrievalToolkit;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Properties;

public class AboutDialog extends JDialog {

    public AboutDialog(Frame owner) {
        super(owner, true);
        BufferedImage img = null;
        try {
//            img = ImageIO.read(AnnotationFrame.class.getResourceAsStream("data/AboutEmir.png"));
            img = ImageIO.read(getClass().getResource("/resources/images/AboutEmir.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FullSizeImagePanel panel = new FullSizeImagePanel(img);
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }
        });
        Properties buildProps = new Properties();
        try {
            buildProps.load(at.lux.fotoannotation.dialogs.AboutDialog.class.getResource("build.properties").openStream());
        } catch (IOException e) {
            debug("Build properties could not be loaded!");
        }
        String build = "";
        if (buildProps.getProperty("build-number") != null) {
            build = " (build #" + buildProps.getProperty("build-number") + ")";
        }

        this.setTitle("About " + RetrievalToolkit.PROGRAM_NAME + " " + AnnotationToolkit.PROGRAM_VERSION + build);
        this.getContentPane().add(panel, BorderLayout.CENTER);
    }

    private void debug(String message) {
        System.out.println("[at.lux.fotoretrieval.dialogs.AboutDialog] " + message);
    }
}
