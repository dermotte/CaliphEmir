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
package at.lux.fotoretrieval;

import org.jdom.Element;
import org.jdom.Namespace;

import javax.xml.transform.Transformer;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ResultListEntry
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ResultListEntry implements Comparable {
    private double relevance;
    private Element documentRoot;
    private String filePath = null;
    private String thumbPath = null;
    private String descriptionPath = null;
    private String HTMLSummary = null;
    private static DecimalFormat df = ((DecimalFormat) DecimalFormat.getInstance());
    private int quality = -1;

    private String semanticDescriptionString;
    private String creatorName;
    private String freeTextDescription;
    private String creationTime;
    private String imageFilePath;
    private String imageSize;

    public ResultListEntry(double relevance, Element documentRoot, String descriptionPath) {
        this.relevance = relevance;
        this.documentRoot = documentRoot;
        this.descriptionPath = descriptionPath;

        df.setMaximumFractionDigits(2);

        List results = RetrievalToolkit.xpathQuery(documentRoot, "//MediaProfile[@master='true']/MediaInstance/MediaLocator/MediaUri", null);
        if (results.size() > 0) {
            filePath = ((Element) results.get(0)).getTextTrim();
            if (results.size() > 1) {
                thumbPath = ((Element) results.get(1)).getTextTrim();
            }
        }
        // /Mpeg7/Description/MultimediaContent/Image/MediaInformation/MediaProfile/MediaFormat/VisualCoding/Frame
        results = RetrievalToolkit.xpathQuery(documentRoot, "//MediaProfile/MediaFormat/VisualCoding/Frame[number(@height) < 121 and number(@width) < 121]", null);
        if (results.size() > 0) {
            Element frame = ((Element) results.get(0));
            Element profile = (Element) frame.getParent().getParent().getParent();
            Namespace mpeg7 = profile.getNamespace();
            Element uri = profile.getChild("MediaInstance", mpeg7).getChild("MediaLocator", mpeg7).getChild("MediaUri", mpeg7);
            thumbPath = uri.getTextTrim();
        }
        // quality
        results = RetrievalToolkit.xpathQuery(documentRoot, "//QualityRating[@type='subjective']/RatingValue", null);
        if (results.size() > 0) {
            quality = Integer.parseInt(((Element) results.get(0)).getTextTrim());
        } else {
            quality = -1;
        }
        // creating the summary:
//        HTMLSummary = getSummary();
        extractSummary();
        this.documentRoot = null;
    }

    public ResultListEntry(double relevance, String thumbPath, String filePath, String creatorName, String freeTextDescription, String creationTime, String imageFilePath, String imageSize) {
        this.relevance = relevance;
        this.thumbPath = thumbPath;
        this.filePath = filePath;
        this.descriptionPath = null;
        this.creatorName = creatorName;
        this.freeTextDescription = freeTextDescription;
        this.creationTime = creationTime;
        this.imageFilePath = imageFilePath;
        this.imageSize = imageSize;
    }

    public double getRelevance() {
        return relevance;
    }

    public int getQuality() {
        return quality;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    /**
     * Gets path to MPEG-7 file
     *
     * @return Path to MPEG-7 file
     */
    public String getDescriptionPath() {
        return descriptionPath;
    }

    /**
     * Summary of result
     *
     * @return Summary of result based on HTML 3.2
     */
    public String getHTMLSummary() {
        return HTMLSummary;
    }

    public String getSemanticDescriptionString() {
        return semanticDescriptionString;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getFreeTextDescription() {
        return freeTextDescription;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public String getImageSize() {
        return imageSize;
    }

    private void extractSummary() {
        creatorName = "";
        List l = RetrievalToolkit.xpathQuery(documentRoot, "//CreationInformation/Creation/Creator/Agent/Name", documentRoot.getNamespace());
        if (l.size() > 0) {
            Element name = (Element) l.get(0);
            creatorName += name.getChildTextTrim("FamilyName", name.getNamespace());
            creatorName += ", ";
            creatorName += name.getChildTextTrim("GivenName", name.getNamespace());
        }
        creatorName += " with " + getValueOfPath("//CreationInformation/Creation/CreationTool/Tool/Name");
        creationTime = getValueOfPath("//CreationInformation/Creation/CreationCoordinates/Date/TimePoint");

        // so we can build up some description on our own.
        LinkedList<String> agents = new LinkedList<String>();
        LinkedList<String> events = new LinkedList<String>();
        LinkedList<String> times = new LinkedList<String>();
        LinkedList<String> places = new LinkedList<String>();

        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        l = RetrievalToolkit.xpathQuery(documentRoot, "//SemanticBase", documentRoot.getNamespace());
        for (Object aL : l) {
            Element elem = (Element) aL;
            String label;
            label = elem.getChild("Label", elem.getNamespace()).getChildText("Name", elem.getNamespace()).trim();
            if (elem.getAttribute("type", xsi).getValue().startsWith("AgentObjectType")) {
                agents.add(label);
            } else if (elem.getAttribute("type", xsi).getValue().startsWith("EventType")) {
                events.add(label);
            } else if (elem.getAttribute("type", xsi).getValue().startsWith("SemanticTimeType")) {
                times.add(label);
            } else if (elem.getAttribute("type", xsi).getValue().startsWith("SemanticPlaceType")) {
                places.add(label);
            }
        }
        semanticDescriptionString = appendAll(agents) + ((appendAll(agents).contains(",")) ? " are at " : " is at ") + appendAll(places) + " for " + appendAll(events) + " in " + appendAll(times);

        // fre text annotation:
        String valueOfPath = getValueOfPath("//TextAnnotation/FreeTextAnnotation");
        freeTextDescription = valueOfPath;

        valueOfPath = getValueOfPath("//MediaInformation/MediaProfile/MediaInstance/MediaLocator/MediaUri");
        imageFilePath = valueOfPath.substring(valueOfPath.lastIndexOf('/') + 1);
        l = RetrievalToolkit.xpathQuery(documentRoot, "//MediaInformation/MediaProfile/MediaFormat/VisualCoding/Frame", documentRoot.getNamespace());
        if (l.size() > 0) {
            Element frame = (Element) l.get(0);
            imageSize = frame.getAttribute("width").getValue() + " x " + frame.getAttribute("height").getValue() + " pixels ";
        }
    }

    private String getValueOfPath(String path) {
        String result = null;
        List l = RetrievalToolkit.xpathQuery(documentRoot, path, documentRoot.getNamespace());
        if (l.size() > 0) {
            Element name = (Element) l.get(0);
            result = name.getTextTrim();
        }
        return result;
    }

    private String appendAll(List<String> input) {
        StringBuilder sb = new StringBuilder(input.size() * 32);
        for (Iterator<String> iterator = input.iterator(); iterator.hasNext();) {
            sb.append(iterator.next());
            if (iterator.hasNext()) sb.append(", ");
        }
        return sb.toString();
    }

    public int compareTo(Object o) {
        ResultListEntry r = (ResultListEntry) o;
        return (int) Math.signum(r.getRelevance() - relevance);
    }
}
