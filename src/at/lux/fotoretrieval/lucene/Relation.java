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

import java.util.HashMap;
import java.util.Iterator;

/**
 * Date: 29.10.2004
 * Time: 23:57:34
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Relation implements Comparable {
    private int source, target;
    private String type;
    public static HashMap<String, String> relationMapping;
    public static HashMap<String, String> relationMappingInverse;

    static {
        relationMapping = new HashMap<String, String>(27);
        relationMapping.put("key", "keyFor");
        relationMapping.put("annotates", "annotatedBy");
        relationMapping.put("shows", "appearsIn");
        relationMapping.put("references", "referencedBy");
        relationMapping.put("quality", "qualityOf");
        relationMapping.put("symbolizes", "symbolizedBy");
        relationMapping.put("location", "locationOf");
        relationMapping.put("source", "sourceOf");
        relationMapping.put("destination", "destinationOf");
        relationMapping.put("path", "pathOf");
        relationMapping.put("time", "timeOf");
        relationMapping.put("depicts", "depictedBy");
        relationMapping.put("represents", "representedBy");
        relationMapping.put("context", "contextFor");
        relationMapping.put("interprets", "interpretedBy");
        relationMapping.put("agent", "agentOf");
        relationMapping.put("patient", "patientOf");
        relationMapping.put("experiencer", "experiencerOf");
        relationMapping.put("stimulus", "stimulusOf");
        relationMapping.put("causer", "causerOf");
        relationMapping.put("goal", "goalOf");
        relationMapping.put("beneficiary", "beneficiaryOf");
        relationMapping.put("theme", "themeOf");
        relationMapping.put("result", "resultOf");
        relationMapping.put("instrument", "instrumentOf");
        relationMapping.put("accompanier", "accompanierOf");
        relationMapping.put("summarizes", "summarizedBt");
        relationMapping.put("specializes", "generalizes");
        relationMapping.put("exemplifies", "exemplifiedBy");
        relationMapping.put("part", "partOf");
        relationMapping.put("property", "propertyOf");
        relationMapping.put("user", "userOf");
        relationMapping.put("component", "componentOf");
        relationMapping.put("substance", "substanceOf");
        relationMapping.put("entails", "entailedBy");
        relationMapping.put("manner", "mannerOf");
        relationMapping.put("state", "stateOf");
        relationMapping.put("influences", "dependsOn");

        relationMappingInverse = new HashMap<String, String>(relationMapping.size());
        for (Iterator<String> iterator = relationMapping.keySet().iterator(); iterator.hasNext();) {
            String s = iterator.next();
            relationMappingInverse.put(relationMapping.get(s), s);
        }

    }


    /**
     * Creates a new Relation with given source, target and name
     *
     * @param source defines the node ID of the source
     * @param target defines the node ID of the target
     * @param type
     */
    public Relation(int source, int target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return (type + " " + source + " " + target);
    }

    public boolean isSourceOrTarget(int nodeID) {
        boolean result = false;
        if (source == nodeID || target == nodeID) result = true;
        return result;
    }

    public int compareTo(Object o) {
        String s = ((Relation) o).toString();
        return (toString().compareTo(s));
    }

    public Relation clone() {
        return new Relation(source, target, new String(type));
    }

    public boolean equals(Object o) {
        return o.toString().equals(toString());
    }

    /**
     * eliminates an inverse relation as there is an inverse
     * defined for each possible relation.
     */
    public void eliminateInverse() {
        if (relationMapping.containsKey(type)) {
            type = relationMapping.get(type);
            int tmp = source;
            source = target;
            target = tmp;
        }
    }

    /**
     * Inverts the current relation ..
     */
    public void invert() {
        if (relationMapping.containsKey(type)) {
            type = relationMapping.get(type);
            int tmp = source;
            source = target;
            target = tmp;
        } else {
            type = relationMappingInverse.get(type);
            int tmp = source;
            source = target;
            target = tmp;
        }
    }

    /**
     * Allows the fast inversion of the given relationType
     * @param relationType
     * @return the inverse MPEG-7 relationType or NULL if none found.
     */
    public static String invertRelationType(String relationType) {
        String result = null;
        if (relationMapping.containsKey(relationType)) {
            result = relationMapping.get(relationType);
        } else {
            result = relationMappingInverse.get(relationType);
        }
        return result;
    }
}
