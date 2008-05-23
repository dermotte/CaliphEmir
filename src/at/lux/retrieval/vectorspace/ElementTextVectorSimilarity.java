package at.lux.retrieval.vectorspace;

import at.lux.fotoretrieval.RetrievalToolkit;
import at.lux.fotoretrieval.lucene.Relation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
 * Date: 16.03.2006
 * Time: 21:32:35
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ElementTextVectorSimilarity {
    private Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    private Analyzer analyer = new SimpleAnalyzer();
    private HashMap<String, Integer> idf = new HashMap<String, Integer>(128);
    private int numDocs = 0;
    private int allDocsLength = 0;
    private boolean useRelations = true;

    public enum WeightType {
        TfIdf,
        BM25,
        Unweighted
    }

    public double getSimilarity(Document mpeg7Document1, Document mpeg7Document2) {
        return getSimilarity(mpeg7Document1, mpeg7Document2, WeightType.Unweighted);
    }

    public double getSimilarity(Document mpeg7Document1, Document mpeg7Document2, WeightType type) {
        List semanticBaseDoc1 = RetrievalToolkit.xpathQuery(mpeg7Document1.getRootElement(), "//Semantic/SemanticBase", null);
        List semanticBaseDoc2 = RetrievalToolkit.xpathQuery(mpeg7Document2.getRootElement(), "//Semantic/SemanticBase", null);

        List semanticRelationsDoc1 = RetrievalToolkit.xpathQuery(mpeg7Document1.getRootElement(), "//Semantic/Graph/Relation", null);
        List semanticRelationsDoc2 = RetrievalToolkit.xpathQuery(mpeg7Document2.getRootElement(), "//Semantic/Graph/Relation", null);

        HashMap<String, Integer> termVector1 = createTermVector(semanticBaseDoc1, semanticRelationsDoc1);
        HashMap<String, Integer> termVector2 = createTermVector(semanticBaseDoc2, semanticRelationsDoc2);
        HashSet<String> terms = new HashSet<String>(termVector1.size() + termVector2.size());
        if (type != WeightType.BM25) {
            terms.addAll(termVector1.keySet());
            terms.addAll(termVector2.keySet());
            double sum = 0f;
            double sum1 = 0;
            double sum2 = 0;
            for (String dim : terms) {
                double factor1 = 0f;
                double factor2 = 0f;
                if (termVector1.containsKey(dim)) {
                    double entry = termVector1.get(dim);
                    if (type == WeightType.TfIdf) {
                        if (!idf.containsKey(dim))
                            throw new UnsupportedOperationException("Document has to be added to corpus first!");
                        double idfValue = Math.log((double) numDocs / (double) idf.get(dim));
                        entry = entry * idfValue;
                    }
                    factor1 = entry;
                    sum1 += entry * entry;
                }
                if (termVector2.containsKey(dim)) {
                    double entry = termVector2.get(dim);
                    if (type == WeightType.TfIdf) {
                        if (!idf.containsKey(dim))
                            throw new UnsupportedOperationException("Document has to be added to corpus first!");
                        double idfValue = Math.log((double) numDocs / (double) idf.get(dim));
                        entry = entry * idfValue;
                    }
                    factor2 = entry;
                    sum2 += entry * entry;
                }
                sum += factor1 * factor2;
            }
            double upper = sum;
            double lower = (float) Math.sqrt((float) sum1 * (float) sum2);
            return upper / lower;
        } else {
            double avdl = (double) allDocsLength / (double) numDocs;
            double dl1 = termVector1.size();
            double dl2 = termVector2.size();
            double k1 = 1.5;
            double b = 0.5;
            double sum = 0.0;
/*
            for (String term : termVector1.keySet()) {
                if (termVector2.containsKey(term)) {
                    terms.add(term);
                }
            }
            for (String term : terms) {
                if (numDocs < idf.get(term)) {
                    System.out.println(term + " -> " + idf.get(term));
                }
                assert(numDocs >= idf.get(term));
                sum += getBm25Weight(k1, b, termVector1.get(term), idf.get(term), avdl, dl1) *
                        getBm25Weight(k1, b, termVector2.get(term), idf.get(term), avdl, dl2);
            }
            return sum;
*/
            terms.addAll(termVector1.keySet());
            terms.addAll(termVector2.keySet());
            double sum1 = 0;
            double sum2 = 0;
            for (String dim : terms) {
                double factor1 = 0f;
                double factor2 = 0f;
                if (termVector1.containsKey(dim)) {
                    double entry = termVector1.get(dim);
                    if (!idf.containsKey(dim))
                        throw new UnsupportedOperationException("Document has to be added to corpus first!");
//                    double idfValue = Math.log((double) numDocsInCorpus / (double) idf.get(dim));
                    entry = getBm25Weight(k1, b, termVector1.get(dim), idf.get(dim), avdl, dl1);
                    factor1 = entry;
                    sum1 += entry * entry;
                }
                if (termVector2.containsKey(dim)) {
                    double entry = termVector2.get(dim);
                    if (!idf.containsKey(dim))
                        throw new UnsupportedOperationException("Document has to be added to corpus first!");
//                    double idfValue = Math.log((double) numDocsInCorpus / (double) idf.get(dim));
                    entry = getBm25Weight(k1, b, termVector2.get(dim), idf.get(dim), avdl, dl2);
                    factor2 = entry;
                    sum2 += entry * entry;
                }
                sum += factor1 * factor2;
            }
            double upper = sum;
            double lower = (float) Math.sqrt((float) sum1 * (float) sum2);
            return upper / lower;

        }
    }

    private double getBm25Weight(double k1, double b, double termFreq, double docFreq, double avgDocLength, double docLength) {
        assert(numDocs >= docFreq);
        return ((k1 + 1.0) * termFreq) / (k1 * ((1 - b) + b * docLength / avgDocLength) + termFreq) * Math.log((numDocs - docFreq + 0.5) / (docFreq + 0.5));
    }


    public void addToCorpus(Document mpeg7Document) {
        numDocs++;
        List semanticBaseDoc = RetrievalToolkit.xpathQuery(mpeg7Document.getRootElement(), "//Semantic/SemanticBase", null);
        List semanticRelationsDoc = RetrievalToolkit.xpathQuery(mpeg7Document.getRootElement(), "//Semantic/Graph/Relation", null);
        HashMap<String, Integer> termVector = createTermVector(semanticBaseDoc, semanticRelationsDoc);
        // calculating the document term frequeny + avg. document length.
        for (String term : termVector.keySet()) {
            allDocsLength += termVector.get(term);
            if (!idf.containsKey(term)) {
                idf.put(term, 1);
            } else {
                idf.put(term, idf.get(term) + 1);
            }
        }
        // calculating the
    }

    private HashMap<String, Integer> createTermVector(List semanticBaseDoc, List semanticRelationsDoc) {
        HashMap<String, Integer> termVector = new HashMap<String, Integer>(32);
        // nodes ...
        for (Object node : semanticBaseDoc) {
            Element e = (Element) node;
            addToTermVector(e, termVector);
        }
        // relations ...
        if (useRelations) {
            for (Object node : semanticRelationsDoc) {
                Element e = (Element) node;
                String relationType = getRelationType(e.getAttributeValue("type"));
                if (!Relation.relationMapping.containsKey(relationType))
                    relationType = Relation.invertRelationType(relationType);
                if (termVector.get(relationType) == null) {
                    termVector.put(relationType, 0);
                }
                termVector.put(relationType, termVector.get(relationType) + 1);
            }
        }
        return termVector;
    }

    private void addToTermVector(Element node, HashMap<String, Integer> result) {
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

    private void addTerms(HashMap<String, Integer> terms, String text) {
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

    private static String getRelationType(String relationType) {
        int index = relationType.lastIndexOf(':');
        return relationType.substring(index + 1);
    }


}
