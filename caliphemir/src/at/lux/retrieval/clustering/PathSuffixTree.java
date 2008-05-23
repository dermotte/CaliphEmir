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

import at.lux.fotoretrieval.lucene.Relation;

import java.util.ArrayList;

/**
 * <p/>
 * Date: 18.10.2005 <br>
 * Time: 09:32:39 <br>
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class PathSuffixTree extends AbstractSuffixTree{
    /**
     * Defines the way relations are handled in this tree. Either they are left out or they
     * are only used as types with no direction indicator, or they are used as they appear in
     * the path.
     */
    public enum Type {NoRelations, UndirectedRelation, FullRelations}

    private Type type = Type.NoRelations;


    /**
     * Actually we dont need a stopword list for this one. The Default type
     * {@link at.lux.retrieval.clustering.PathSuffixTree.Type} NoRelations
     * is used.
     * @param stopwordlist should be set to null as it is not used.
     */
    public PathSuffixTree() {
        super();
        MIN_SENTENCE_SIZE = 0;
    }

    /**
     * Create a new PathSuffixTree of given type.
     * @param type
     */
    public PathSuffixTree(Type type) {
        super();
        this.type = type;
    }


    /**
     * The Tokens of the Path are created here. There are two possible ways to
     * create the tokens: Either we take the relations names into accoutn or we
     * do not. a hybrid solution is just to store the type of the relation and
     * not its direction.
     * Use {@link at.lux.retrieval.clustering.PathSuffixTree.Type}
     * in the constructor to select behaviour.
     * @param sentence gives the sentence to tokenize.
     * @return the tokens.
     */
    protected String[] getTokens(String sentence) {
        String[] result = sentence.split("\\s");
        if (type == Type.NoRelations) {
            // strip all relations from the array:
            ArrayList<String> r = new ArrayList<String>(result.length / 2 + 1);
            for (int i = 0; i < result.length; i++) {
                String node = result[i];
                // if the node is a number in square brackets
                if (node.matches("\\d+")) {
                    r.add(node);
                }
            }
            result = r.toArray(new String[r.size()]);
        } else if (type == Type.FullRelations) {
            ArrayList<String> r = new ArrayList<String>(result.length);
            for (int i = 0; i < result.length; i++) {
                String node = result[i];
                // if the node is a number in square brackets
                if (node!=null) {
                    r.add(node);
                }
            }
            result = r.toArray(new String[r.size()]);
        } else if (type == Type.UndirectedRelation) {
            // strip all relations from the array:
            ArrayList<String> r = new ArrayList<String>(result.length);
            for (int i = 0; i < result.length; i++) {
                String node = result[i];
                // if the node is a number in square brackets
                if (node != null && node.matches("\\d+")) {
                    r.add(node);
                } else {
                    // invert the relation if it is no key of the mapping table.
                    if (!Relation.relationMapping.containsKey(node))
                        node = Relation.invertRelationType(node);
                    r.add(node);
                }
            }
            result = r.toArray(new String[r.size()]);
        }
        return result;
    }

    /**
     * The paths are provided within a String, where each
     * line represents one path.This single path is interpreted as sentence.
     * @param phrase
     * @return one single path as sentence.
     */
    protected String[] getSentences(String phrase) {
        String[] result = phrase.split("\\n");
        return result;
    }

    protected String[] filterTokens(String[] tokens) {
        return tokens;
    }

}
