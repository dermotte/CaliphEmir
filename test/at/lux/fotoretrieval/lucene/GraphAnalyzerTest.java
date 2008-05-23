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

import junit.framework.TestCase;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.StringReader;

/**
 * Date: 25.03.2005
 * Time: 22:33:47
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GraphAnalyzerTest extends TestCase {
    public void testGraphAnalyzer() throws IOException {
        GraphAnalyzer ga = new GraphAnalyzer();
        StringReader sr = new StringReader("[0] [1] [2] [10] [27] [locationOf 1 0] [locationOf 1 10] [locationOf 10 27] [timeOf 2 0]");
        TokenStream ts = ga.tokenStream(null, sr);
        Token t = ts.next();
        do {
            System.out.println(t.termText());
            t = ts.next();
        } while (t != null);
    }
}
