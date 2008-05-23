package at.lux.retrieval.graphisomorphism.metrics;

import at.lux.retrieval.graphisomorphism.EdgeDistanceFunction;
import at.lux.fotoretrieval.lucene.Relation;
import org.jdom.Element;
import org.jdom.Namespace;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * Simple edge distance function
 * This file is part of Caliph & Emir
 * Date: 16.02.2006
 * Time: 22:34:31
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleEdgeDistanceFunction implements EdgeDistanceFunction {
    public enum EdgeInversionType {
        Allow, Forbid, WeightInverted }

    private EdgeInversionType type;

    public SimpleEdgeDistanceFunction(EdgeInversionType type) {
        this.type = type;
    }

    public float getDistance(Element edge1, Element edge2) {
        Namespace mpeg7 = edge1.getNamespace();
        String relationType1 = edge1.getAttributeValue("type");
        String relationType2 = edge2.getAttributeValue("type");

        relationType1 = getRelationType(relationType1);
        relationType2 = getRelationType(relationType2);

        float result = 1f;

        if (type == EdgeInversionType.Allow) {
            if (!Relation.relationMapping.containsKey(relationType1))
                relationType1 = Relation.invertRelationType(relationType1);
            if (!Relation.relationMapping.containsKey(relationType2))
                relationType2 = Relation.invertRelationType(relationType2);
            if (relationType1.equals(relationType2)) result = 0f;
        } else if (type == EdgeInversionType.Forbid) {
            if (relationType1.equals(relationType2)) result = 0f;
        } else if (type == EdgeInversionType.WeightInverted) {
            if (relationType1.equals(relationType2)) result = 0f;
            else if (!Relation.relationMapping.containsKey(relationType2) || !Relation.relationMapping.containsKey(relationType1)) {
                if (!Relation.relationMapping.containsKey(relationType1))
                    relationType1 = Relation.invertRelationType(relationType1);
                if (!Relation.relationMapping.containsKey(relationType2))
                    relationType2 = Relation.invertRelationType(relationType2);
                if (relationType1.equals(relationType2)) result = 0.5f;
            }
        }
        return result;
    }

    public float getMaxDistance() {
        return 1f;
    }

    private static String getRelationType(String relationType) {
        int index = relationType.lastIndexOf(':');
        return relationType.substring(index + 1);
    }
}
