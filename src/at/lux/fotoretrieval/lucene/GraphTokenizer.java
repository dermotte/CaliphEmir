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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;

/**
 * Date: 25.03.2005
 * Time: 22:13:35
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GraphTokenizer extends TokenStream {
    private char last=' ';
    private Reader reader;
    private boolean tokenstart, tokenend;

    public GraphTokenizer(Reader in) {
//        super(in);
        reader = in;
    }

    protected boolean isTokenChar(char c) {
        boolean returnValue = false;
        if (c == ' ' && last == ']') {
            returnValue = true;
        }
        last = c;
        return returnValue;
    }

    public Token next() throws IOException {
        StringBuilder currenttoken = new StringBuilder(64);
        // currenttoken.append('[');
        char[] character = new char[1];
        int i = reader.read(character);
        // reset our states :)
        tokenstart = false;
        tokenend = false;
        do {
            // end of stream reached ...
            if (i == 0) return null;

            if (character[0] == '[') { // token starts here ...
                tokenstart = true;
            } else if (character[0] == ']') { // token ends here ...
                tokenend = true;
            } else if (tokenstart && !tokenend) { // between end and start ...
                currenttoken.append(character[0]);
            }
            // we found our token and return it ...
            if (tokenstart && tokenend) {
                // currenttoken.append(']');
                // prepend a token because lucene does not allow leading wildcards. 
                currenttoken.insert(0, '_');
                String tokenString = currenttoken.toString().toLowerCase().replace(' ', '_').trim();
                Token t = new Token(tokenString, 0, tokenString.length()-1);
                return t;
            }
            i = reader.read(character);
        } while (i>0 && !tokenend);
        return null;
    }
}
