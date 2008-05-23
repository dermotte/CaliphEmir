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
package at.lux.fotoannotation.utils;

import at.lux.fotoannotation.AnnotationFrame;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TextChangesListener extends KeyAdapter implements ActionListener, ChangeListener {
    private static TextChangesListener listener_ = null;
    private static AnnotationFrame aframe_ = null;

    private TextChangesListener() {
        super();
    }

    public static TextChangesListener getInstance() {
        if (!(listener_ != null)) {
            listener_ = new TextChangesListener();
        }
        return listener_;
    }

    public static void createInstance(AnnotationFrame frame) {
        aframe_ = frame;
        listener_ = new TextChangesListener();
    }

    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
        if (e.getKeyCode() == KeyEvent.VK_S && e.getModifiers() == KeyEvent.CTRL_DOWN_MASK) {
            aframe_.saveFile();
        } else if (e.getKeyCode() != KeyEvent.VK_CONTROL) {
            AnnotationFrame.setDirty(true);
            if (aframe_.getTitle().indexOf("*") == -1) {
                aframe_.setTitle("* " + aframe_.getTitle());
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        AnnotationFrame.setDirty(true);
        if (aframe_.getTitle().indexOf("*") == -1) {
            aframe_.setTitle("* " + aframe_.getTitle());
        }
    }

    public void stateChanged(ChangeEvent e) {
        AnnotationFrame.setDirty(true);
        if (aframe_.getTitle().indexOf("*") == -1) {
            aframe_.setTitle("* " + aframe_.getTitle());
        }
    }

    public void fireDataChanged() {
        AnnotationFrame.setDirty(true);
        if (aframe_.getTitle().indexOf("*") == -1) {
            aframe_.setTitle("* " + aframe_.getTitle());
        }
    }
}
