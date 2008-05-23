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
package at.lux.fotoretrieval.lucene;

import at.lux.fotoretrieval.RetrievalFrame;
import at.lux.fotoretrieval.EmirConfiguration;
import at.lux.fotoretrieval.retrievalengines.LucenePathIndexRetrievalEngine;
import at.lux.fotoretrieval.retrievalengines.LuceneRetrievalEngine;
import at.lux.fotoretrieval.retrievalengines.DatabaseRetrievalEngine;

/**
 * Date: 14.10.2004
 * Time: 10:06:49
 * @author Mathias Lux, mathias@juggle.at
 */
public class IndexerThread implements Runnable {
    RetrievalFrame parent;
    private String indexBasePath;

    public IndexerThread(RetrievalFrame parent, String indexBasePath) {
        this.parent = parent;
        this.indexBasePath = indexBasePath;
    }

    public void run() {
        parent.setEnabled(false);
        parent.setStatus("Please wait while indexing");
        // use derby only if it is defined in the configuration.
        if (EmirConfiguration.getInstance().getBoolean("Retrieval.Cbir.useDerby")) {
            DatabaseRetrievalEngine dbEngine = new DatabaseRetrievalEngine();
            dbEngine.indexAllImages(indexBasePath, parent);
        }
        LuceneRetrievalEngine engine = new LuceneRetrievalEngine(EmirConfiguration.getInstance().getInt("Retrieval.Result.Maximum"));
        engine.indexFiles(indexBasePath, parent);
//        engine.indexFilesSemantically(indexBasePath, parent);
        LucenePathIndexRetrievalEngine pathIndexEngine = new LucenePathIndexRetrievalEngine(30);
        pathIndexEngine.indexFilesSemantically(indexBasePath, parent);
        parent.setStatus("Indexing finished");
        parent.setEnabled(true);
    }
}
