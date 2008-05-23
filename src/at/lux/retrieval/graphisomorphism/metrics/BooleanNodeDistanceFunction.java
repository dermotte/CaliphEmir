package at.lux.retrieval.graphisomorphism.metrics;

import at.lux.retrieval.graphisomorphism.NodeDistanceFunction;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
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
 * Date: 16.02.2006
 * Time: 22:35:05
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class BooleanNodeDistanceFunction implements NodeDistanceFunction {
    /**
     * Boolean distance function: 1 in case of not equal, 0 in case of
     * string output equality
     * @param node1 first semantic object.
     * @param node2 second semantic object.
     * @return 1 in case of not equal, 0 in case of string output equality.
     */
    public float getDistance(Element node1, Element node2) {
        XMLOutputter out = new XMLOutputter(Format.getRawFormat());
        String nodeContent1 = out.outputString(node1).trim().replaceAll("id=\"id_[0-9]*\"", "");
        String nodeContent2 = out.outputString(node2).trim().replaceAll("id=\"id_[0-9]*\"", "");
        float result = 1f;
        if (nodeContent1.equals(nodeContent2)) result = 0f;
        return result;
    }

    public float getMaxDistance() {
        return 1f;
    }
}
