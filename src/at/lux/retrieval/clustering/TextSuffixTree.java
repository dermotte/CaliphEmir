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
package at.lux.retrieval.clustering;

import java.util.*;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 03.06.2004
 *         Time: 13:42:41
 */

public class TextSuffixTree extends AbstractSuffixTree{

    public TextSuffixTree(HashSet<String> stopwordlist) {
        super();
        stopwords = stopwordlist;
    }

    /**
     * Override this one if you want to change the way of handling tokens
     * (or words in this implementation)
     * @param sentence
     * @return
     */
    protected String[] getTokens(String sentence) {
        // TODO: eventually think about integrating some symbols like @ and / for supporting URIs as tokens
        String[] tokens = sentence.toLowerCase().split("[^a-zA-Z0-9ƒ‹÷‰ˆ¸ﬂ]");
        return tokens;
    }

    /**
     * Override this method if you want to use another method to create the sentences.
     * @param phrase
     * @return an array of sentences.
     */
    protected String[] getSentences(String phrase) {
        String[] sentences = phrase.toLowerCase().split("[.;!?:\\-\\\"'()\\[\\]{}]\\s");
        return sentences;
    }

    protected String[] filterTokens(String[] tokens) {
        LinkedList<String> tokenList = new LinkedList<String>();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            boolean add = true;
            if (token.length() < 3) add = false;
            if (token.equals("...")) add = false;
            if (add) tokenList.add(token);
        }
        String[] result = new String[tokenList.size()];
        tokenList.toArray(result);
        return result;
    }

}



