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
package at.lux.fotoretrieval.retrievalengines;

import junit.framework.TestCase;

/**
 * Date: 29.10.2004
 * Time: 17:32
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class LuceneRetrievalEngineTest extends TestCase {
    private LuceneRetrievalEngine engine;
    private String pathToIndex;

    protected void setUp() throws Exception {
        super.setUp();
        engine = new LuceneRetrievalEngine(40);
        pathToIndex = "C:\\Dokumente und Einstellungen\\Mathias\\Eigene Dateien\\JavaProjects\\Caliph\\testdata";
    }

    public void testSemanticIndexing() {
        engine.indexFilesSemantically(pathToIndex, null);
    }

}
