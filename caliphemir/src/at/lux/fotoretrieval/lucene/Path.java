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

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Date: 25.03.2005
 * Time: 23:42:58
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Path implements Comparable {
    LinkedList<String> nodes;
    LinkedList<String> relations;
    int length = 0;

    public Path(String start) {
        nodes = new LinkedList<String>();
        relations = new LinkedList<String>();
        nodes.add(start);
    }

    public Path(Relation triple) {
        nodes = new LinkedList<String>();
        relations = new LinkedList<String>();
        nodes.add(triple.getSource() + "");
        addRelation(triple.getType(), triple.getTarget() + "");
    }

    public boolean addRelation(String relation, String target) {
        boolean added = false;
        if (!nodes.contains(target)) {
            relations.add(relation);
            nodes.add(target);
            added = true;
        }
        if (added) length++;
        return added;
    }

    public int getLength() {
        return length;
    }

    public LinkedList<String> getNodes() {
        return nodes;
    }

    public LinkedList<String> getRelations() {
        return relations;
    }

    public String getEndPoint() {
        return nodes.getLast();
    }

    public Path clone() {
        Path returnPath = new Path(nodes.get(0));
        int count = 0;
        for (Iterator<String> iterator = nodes.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            if (count > 0) {
                returnPath.getNodes().add(s);
            }
            count++;
        }
        for (Iterator<String> iterator = relations.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            returnPath.getRelations().add(s);
        }
        return returnPath;
    }

    /**
     * Writes out the available paths ...
     *
     * @return
     */
    public String toString() {
        StringBuilder sw = new StringBuilder(64);
        sw.append('[');
        // if the first is smaller then do not change the order
        if (nodes.getFirst().compareTo(nodes.getLast()) < 0) {
            for (int i = 0; i < nodes.size(); i++) {
                sw.append(nodes.get(i));
                if (i < nodes.size() - 1) {
                    sw.append(' ');
                    sw.append(relations.get(i));
                    sw.append(' ');
                }
            }
        } else { // switch order of nodes ..
            for (int i = nodes.size() - 1; i >= 0; i--) {
                sw.append(nodes.get(i));
                if (i > 0) {
                    sw.append(' ');
                    sw.append(Relation.invertRelationType(relations.get(i - 1)));
                    sw.append(' ');
                }
            }
        }
        sw.append(']');
        return sw.toString();
    }

    /**
     * Writes out the available paths ...
     * @param invert if true it returns the inverted path.
     * @return
     */
    public String toString(boolean invert) {
        if (invert) {
            StringBuilder sw = new StringBuilder(64);
            sw.append('[');
            // if the first is smaller then do not change the order
            if (nodes.getFirst().compareTo(nodes.getLast()) > 0) {
                for (int i = 0; i < nodes.size(); i++) {
                    sw.append(nodes.get(i));
                    if (i < nodes.size() - 1) {
                        sw.append(' ');
                        sw.append(relations.get(i));
                        sw.append(' ');
                    }
                }
            } else { // switch order of nodes ..
                for (int i = nodes.size() - 1; i >= 0; i--) {
                    sw.append(nodes.get(i));
                    if (i > 0) {
                        sw.append(' ');
                        sw.append(Relation.invertRelationType(relations.get(i - 1)));
                        sw.append(' ');
                    }
                }
            }
            sw.append(']');
            return sw.toString();
        } else return toString();
    }

    public int compareTo(Object o) {
        if (o instanceof Path) return toString().compareTo(((Path) o).toString());
        else return 0;
    }

    public boolean isTheSamePath(Path p) {
        if (toString().equals(p.toString())) return true;
        else return false;
    }

    public boolean equals(Object o) {
        if (o instanceof Path) return toString().equals(((Path) o).toString());
        else return false;
    }

    public boolean containsNode(Node n) {
        return nodes.contains(n + "");
    }

    public boolean containsNode(String n) {
        return nodes.contains(n);
    }
}
