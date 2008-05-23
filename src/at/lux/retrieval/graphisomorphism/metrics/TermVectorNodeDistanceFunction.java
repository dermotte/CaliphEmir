package at.lux.retrieval.graphisomorphism.metrics;

import at.lux.retrieval.graphisomorphism.NodeDistanceFunction;
import at.lux.fotoretrieval.RetrievalToolkit;
import org.jdom.Element;
import org.jdom.Namespace;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

import java.util.*;
import java.io.StringReader;
import java.io.IOException;
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
 * Time: 15:56:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TermVectorNodeDistanceFunction implements NodeDistanceFunction {
    Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    Analyzer analyer = new SimpleAnalyzer();
    Type type = Type.BagOfWords;

    public enum Type {BagOfWords, CosinusCoefficient, DiceCoefficient, JaccardCoefficient};

    public TermVectorNodeDistanceFunction(Type type) {
        this.type = type;
    }

    public float getDistance(Element node1, Element node2) {
        HashMap<String, Integer> termVector1 = createTermVector(node1);
        HashMap<String, Integer> termVector2 = createTermVector(node2);
        int maxSize = Math.max(termVector1.keySet().size(), termVector2.keySet().size());
        if (type == Type.BagOfWords) {
            // bag of words approach:
            HashSet<String> terms = new HashSet<String>(maxSize);
            terms.addAll(termVector1.keySet());
            terms.addAll(termVector2.keySet());
            int intersectionCardinality = 0;
            for (String term : terms) {
                if (termVector1.containsKey(term) && termVector2.containsKey(term)) {
                    intersectionCardinality++;
                }
            }
            return 1f - ((float) intersectionCardinality / (float) maxSize);
        } else if (type == Type.CosinusCoefficient){
            HashSet<String> terms = new HashSet<String>(maxSize);
            terms.addAll(termVector1.keySet());
            terms.addAll(termVector2.keySet());
            float sum = 0f;
            int sum1 = 0;
            int sum2 = 0;
            for (String dim : terms) {
                float factor1 = 0f;
                float factor2 = 0f;
                if (termVector1.containsKey(dim)) {
                    Integer entry = termVector1.get(dim);
                    factor1 = entry;
                    sum1 += entry * entry;
                }
                if (termVector2.containsKey(dim)) {
                    Integer entry = termVector2.get(dim);
                    factor2 = entry;
                    sum2 += entry * entry;
                }
                sum += factor1 * factor2;
            }
            float upper = sum;
            float lower = (float) Math.sqrt((float) sum1 * (float) sum2);
            return 1f - upper / lower;
        } else if (type == Type.DiceCoefficient){
            throw new UnsupportedOperationException("Not implemented yet!");
        } else if (type == Type.JaccardCoefficient){
            throw new UnsupportedOperationException("Not implemented yet!");
        } else {
            throw new UnsupportedOperationException("Type " + type.name() + " unknown!");
        }
    }

    /**
     * Returns the maximum distance for this function.
     * Used for normalization and algorithmic issues
     *
     * @return the maximum distance.
     */
    public float getMaxDistance() {
        return 1f;
    }

    private HashMap<String, Integer> createTermVector(Element node) {
        HashMap<String, Integer> result = new HashMap<String, Integer>(32);
        // add labels:
        addTerms(result, getTextFromXPath(node, "Label/Name"));
        // add free text:
        addTerms(result, getTextFromXPath(node, "Definition/FreeTextAnnotation"));
        // if agent:
        if (node.getAttributeValue("type", xsi).equals("AgentObjectType")) {
            addTerms(result, getTextFromXPath(node, "Agent/Name/GivenName"));
            addTerms(result, getTextFromXPath(node, "Agent/Name/FamilyName"));
            addTerms(result, getTextFromXPath(node, "Agent/Affiliation/Organization/Name"));
            addTerms(result, getTextFromXPath(node, "Agent/Address/PostalAddress/AddressLine"));
            addTerms(result, getTextFromXPath(node, "Agent/ElectronicAddress/Email"));
            addTerms(result, getTextFromXPath(node, "Agent/ElectronicAddress/Url"));
        } else if (node.getAttributeValue("type", xsi).equals("EventType")) {
            addTerms(result, getTextFromXPath(node, "SemanticPlace/Label/Name"));
            addTerms(result, getTextFromXPath(node, "SemanticPlace/Place/PostalAddress/AddressLine"));
            addTerms(result, getTextFromXPath(node, "SemanticTime/Label/Name"));
        } else if (node.getAttributeValue("type", xsi).equals("SemanticTimeType")) {
            // nothing more to do ...
        } else if (node.getAttributeValue("type", xsi).equals("SemanticPlaceType")) {
            addTerms(result, getTextFromXPath(node, "Place/PostalAddress/AddressLine"));
        }
        return result;
    }

    private String getTextFromXPath(Element node, String xPath) {
        StringBuilder sb = new StringBuilder(128);
        List labels = RetrievalToolkit.xpathQuery(node, xPath, null);
        for (Iterator iterator = labels.iterator(); iterator.hasNext();) {
            Element e = (Element) iterator.next();
            sb.append(e.getTextTrim());
            if (iterator.hasNext()) sb.append(' ');
        }
        return sb.toString();
    }

    private void addTerms(HashMap<String, Integer> terms, String text)  {
        TokenStream tokenStream = analyer.tokenStream("tmp", new StringReader(text));
        Token token;
        try {
            while ((token = tokenStream.next()) != null) {
                String s = token.termText();
                if (terms.get(s) == null) {
                    terms.put(s, 0);
                }
                terms.put(s, terms.get(s) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
