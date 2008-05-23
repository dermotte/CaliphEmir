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
package at.lux.fotoannotation;

import at.lux.components.ProgressWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * AutoPilotThread
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class AutoPilotThread extends Thread {
    AnnotationFrame parent;
    File currentFile;

    public AutoPilotThread(AnnotationFrame parent, File currentFile) {
        this.parent = parent;
        this.currentFile = currentFile;
    }

    public void run() {
        // setting state autopilot to true:
        parent.setAutopilot(true);
        JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL);
        progress.setString("Autopilot");
        progress.setStringPainted(true);
        ProgressWindow pw = new ProgressWindow(parent, progress, "Please stand by while indexing images ...");
        pw.pack();
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        pw.setLocation((ss.width - pw.getWidth()) / 2, (ss.height - pw.getHeight()) / 2);
        File _dir = currentFile.getParentFile();
        File[] _myFiles = _dir.listFiles();
        int count = 0;
        for (int i = 0; i < _myFiles.length; i++) {
            File file = _myFiles[i];
            if (!file.isDirectory() &&
                    (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg"))
                    && (!file.getName().startsWith("tn_"))) {
                count++;
            }
        }
        progress.setMaximum(count);
        progress.setValue(0);
        progress.setString("0 %");
        pw.setVisible(true);

        int state = 0;
        for (int i = 0; i < _myFiles.length; i++) {
            File file = _myFiles[i];
            if (!file.isDirectory() &&
                    (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg"))
                    && (!file.getName().startsWith("tn_"))) {
                try {
                    state++;
                    parent.loadCurrentFile(file);
                    progress.setString(((state * 100) / count) + " %");
                    progress.setValue(state);
                    parent.saveFile();
                } catch (IOException e) {
                    debug(file.getName() + " didn't work with autopilot :(");
                }
            }
        }
        progress.setString("100 %");
        pw.setVisible(false);
        // setting state autopilot to false:
        parent.setAutopilot(false);
    }

    private void debug(String message) {
        if (AnnotationFrame.DEBUG) System.out.println("[at.lux.fotoannotation.AutoPilotThread] " + message);
    }

}
