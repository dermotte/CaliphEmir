package at.lux.retrieval.graphisomorphism;

import org.jdom.Element;
import org.jdom.Document;

import java.util.ArrayList;
import java.util.Collection;
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
 * This file is part of Caliph & Emir
 * Date: 18.02.2006
 * Time: 18:14:53
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public abstract class AbstractSubgraphIsomorphism {
    protected ArrayList<Relation> createEdgesCache(Collection relations) {
        ArrayList<Relation> result = new ArrayList<Relation>(relations.size());
        for (Object obj : relations) {
            result.add(new Relation((Element) obj));
        }
        return result;

    }

    public abstract float getDistance(Document mpeg7Document1, Document mpeg7Document2);

    class Relation {
        String sourceId, targetId;
        String type;
        Element element;

        public Relation(Element element) {
            this.element = element;
            type = getRelationType(element.getAttributeValue("type"));
            sourceId = element.getAttributeValue("source").substring(1);
            targetId = element.getAttributeValue("target").substring(1);
        }

        public boolean connectsId(String id) {
            return sourceId.equals(id) || targetId.equals(id);
        }

        public boolean connectsNodes(String id1, String id2) {
            return connectsId(id1) && connectsId(id2);
        }

        private String getRelationType(String relationType) {
            int index = relationType.lastIndexOf(':');
            return relationType.substring(index + 1);
        }

        public Element getElement() {
            return element;
        }

    }
}
