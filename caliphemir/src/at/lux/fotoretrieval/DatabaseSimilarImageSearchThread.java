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
package at.lux.fotoretrieval;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JProgressBar;

import org.jdom.Element;

import at.lux.components.ProgressWindow;
import at.lux.fotoretrieval.panels.ResultsPanel;
import at.lux.fotoretrieval.retrievalengines.DatabaseRetrievalEngine;
import at.lux.fotoretrieval.retrievalengines.RetrievalEngine;

/**
 * SimilarImageSearchThread
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DatabaseSimilarImageSearchThread extends Thread {
    Set <Element> visualDescriptor = Collections.synchronizedSet(new HashSet());;
    String dir;
    boolean recursive;
    RetrievalFrame parent;
    JProgressBar progress;
    DecimalFormat df;

    public DatabaseSimilarImageSearchThread(Element visualDescriptor, String directory, boolean descend,
                                            RetrievalFrame frame, JProgressBar progress) {
        this.visualDescriptor.add(visualDescriptor);
        this.dir = directory;
        this.recursive = descend;
        this.parent = frame;
        this.progress = progress;
        df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(2);

    }

	public DatabaseSimilarImageSearchThread(Set<Element> visualDescriptor, String directory, boolean descend,
            RetrievalFrame frame, JProgressBar progress) {
		this.visualDescriptor = visualDescriptor;
		this.dir = directory;
		this.recursive = descend;
		this.parent = frame;
		this.progress = progress;
		df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);

}
    public void run() {
        parent.setEnabled(false);
        ProgressWindow pw = new ProgressWindow(parent, progress);
        pw.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        pw.setLocation((d.width - pw.getWidth()) / 2, (d.height - pw.getHeight()) / 2);
        pw.setVisible(true);
        long stime, ftime;
        stime = System.currentTimeMillis();
        parent.setStatus("searching for similar images ...");
//        RetrievalEngine engine = new FileSystemRetrieval();
        RetrievalEngine engine = new DatabaseRetrievalEngine();
        List<ResultListEntry> results = engine.getSimilarImages(visualDescriptor.iterator().next(), dir, recursive, progress);
        stime = System.currentTimeMillis() - stime;
        ftime = System.currentTimeMillis();
        parent.setStatus("Formatting results ...");
        ResultsPanel rp = new ResultsPanel(results, progress);
        ftime = System.currentTimeMillis() - ftime;
        parent.addResult(rp);
        parent.setStatus("Searched for " + df.format(stime / 1000.0) + " s, formatting lasted " + df.format(ftime / 1000.0) + " s");
        pw.setVisible(false);
        parent.setEnabled(true);
    }

}