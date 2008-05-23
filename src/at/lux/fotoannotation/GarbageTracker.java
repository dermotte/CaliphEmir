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

import javax.swing.*;
import java.text.DecimalFormat;

public class GarbageTracker extends Thread {
    private JProgressBar garbageState;
    private boolean stopped = false;
    public static DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();

    public GarbageTracker(JProgressBar garbageState) {
        this.garbageState = garbageState;
        init();
    }

    private void init() {
        df.setMaximumFractionDigits(1);
    }

    public void run() {
        debug(" started running ...");
        String memory;
        float full, free, used;
        try {
            while (!stopped) {
                full = Runtime.getRuntime().totalMemory() / (1024f * 1024f);
                free = Runtime.getRuntime().freeMemory() / (1024f * 1024f);
                used = full - free;
                memory = df.format(used) + "M of " + df.format(full) + "M";
                garbageState.setValue(Math.round(used));
                garbageState.setMaximum(Math.round(full));
                garbageState.setString(memory);
                sleep(3000);
            }
        } catch (Exception e) {
            debug("GarbageTracker stopped 'cause of " + e.toString() + ": " + e.getMessage());
        }
    }

    public void stopMonitor() {
        this.stopped = true;
    }

    private void debug(String message) {
        if (AnnotationFrame.DEBUG)
            System.out.println("[at.lux.fotoannotation.GarbageTracker] " + message);
    }

}
