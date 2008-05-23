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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.mpeg7;

import at.wklieber.tools.XmlTools;
import org.jdom.Document;
import org.jdom.Element;

import java.util.Date;
import java.util.List;

/**
 * User: wklieber
 * Date: 06.10.2004
 * Time: 22:22:56
 * This class wrapps setter and getter methods to access the mpeg7 instance metadata.
 * This are Dublin core metadata like title or author and some further mpeg7 metadata
 * like Generationtool, ...
 * This class is used to extend the Mpeg7Template class.
 * either use myMpeg7Instance.getInstanMetadata or give this instance in the constructor.
 * This class contains mpeg7-description data and mpeg7-Content description (media)
 * If the underlying is null, the values are stored in member variables. This can be
 * used to exchange mpeg7 data without invoking xml. Eg. by a non mpeg7 loader (eg. GoogleLoader)
 * to tranport its data to the mpeg7FormatNormalizer
 */
public class Mpeg7InstanceMetadata {
    Mpeg7Template mpeg7;

    // these member varialbes are use to get fast access to the data and to use
    // this class when no xml dom document is available (mpeg7=null)
    private String descriptionMediaId;
    private String descriptionVersion;
    private Date descriptionLastUpdate;
    //private String descriptionTitle;
    private String descriptionSummary;
    private String descriptionCreatorRole;
    private String descriptionCreatorAgentFamilyName;
    private String descriptionCreatorAgentGivenName;
    private Date descriptionCreationTime;
    private String contentCreationLocation;
    private String descriptionTool;
    private String contentType;
    private String contentTitle;
    private Date contentMediaCreationDate;
    private long contentFileSize;
    private String contentMediaUri;
    private String contentMediaSummary;
    private String contentGenre;

    private static final String OFFSET = "Mpeg7/";

    public Mpeg7InstanceMetadata(Mpeg7Template mpeg7) {
        setMpeg7(mpeg7);
    }


    /**
     * Title of the content. This comes from the creation-part of the Mpeg7 description
     *
     * @return
     */
    public String getContentTitle() {
        String xPath = OFFSET + "Description/MultimediaContent/*/CreationInformation/Creation/Title";
        //System.out.println(mpeg7.toString());
        contentTitle = readXmlValue(xPath, contentTitle);
        if (contentTitle == null) {
            contentTitle = "";
        }
        return contentTitle;
    }

    /**
     * Title of the content. This comes from the creation-part of the Mpeg7 description
     * ContentType is e.g. "Video", "Image", "Text", "Audio"
     */
    public void setContentTitle(String contentTitle, String contentType) {
        this.contentTitle = contentTitle;
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/CreationInformation/Creation/Title";
        writeXmlValue(xPath, this.contentTitle);
    }

    /**
     * get a keyframe from results from the mmdb. For each Timepoint in a temporal description
     * a keyframe exists in the database. Ths method retrieves the first timpepoint
     * Timepoints are also in "Description/Summarization"
     * ContentType is "Video".
     */
    public String getContentFirstKeyframe() {
        String xPath = OFFSET + "Description/MultimediaContent/Video/TemporalDecomposition/VideoSegment/MediaTime/MediaTimePoint";
        String returnValue = readXmlValue(xPath, "");

        return returnValue;
    }

    /**
     * content file size in byte
     *
     * @return
     */
    public long getContentFileSize() {
        String xPath = OFFSET + "Description/MultimediaContent/*/MediaInformation/MediaProfile/MediaFormat/FileSize";
        String sizeString = readXmlValue(xPath, "");
        if (sizeString.length() > 0) {
            contentFileSize = Long.parseLong(sizeString);
        } else {
            contentFileSize = 0;
        }

        return contentFileSize;
    }

    /**
     * content file size in byte
     *
     * @param contentFileSize
     */
    public void setContentFileSize(long contentFileSize, String contentType) {
        this.contentFileSize = contentFileSize;
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/MediaInformation/MediaProfile/MediaFormat/FileSize";
        writeXmlValue(xPath, Long.toString(this.contentFileSize));
    }

    /**
     * e.g. Documentary
     *
     * @return
     */
    public String getContentGenre() {
        String xPath = OFFSET + "Description/MultimediaContent/*/CreationInformation/Classification/Genre/Name";
        //System.out.println(mpeg7.toString());
        contentGenre = readXmlValue(xPath, contentGenre);
        if (contentGenre == null) {
            contentGenre = "";
        }
        return contentTitle;
    }

    /**
     * e.g. Documentary
     *
     * @param contentGenre
     */
    public void setContentGenre(String contentGenre, String contentType) {
        this.contentGenre = contentGenre;
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/CreationInformation/Classification/Genre/Name";
        writeXmlValue(xPath, this.contentGenre);
    }

    /**
     * creation date of the content file
     *
     * @return
     */
    public Date getContentMediaCreationDate() {
        String xPath = OFFSET + "Description/MultimediaContent/*/CreationInformation/Creation/CreationCoordinates/CreationDate/TimePoint";
        //System.out.println(mpeg7.toString());
        String dateString = readXmlValue(xPath, "");
        if (dateString.length() > 0) {
            contentMediaCreationDate = Mpeg7ConversionTools.getReference().timePointToDate(dateString);
        } else {
            contentMediaCreationDate = null;
        }

        return contentMediaCreationDate;
    }

    /**
     * creation date of the content file
     *
     * @param contentMediaCreationDate
     */
    public void setContentMediaCreationDate(Date contentMediaCreationDate, String contentType) {
        this.contentMediaCreationDate = contentMediaCreationDate;
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/CreationInformation/Creation/CreationCoordinates/CreationDate/TimePoint";

        writeXmlValue(xPath, Mpeg7ConversionTools.getReference().dateTotimePoint(this.contentMediaCreationDate));
    }

    /**
     * e.g. video, image, ...
     * This is stored in mpeg7 as mediaFormat and seems to be the same as contentType.
     * To avoid confusion, here contentType and mediaFormat is the same.
     * Here we use just the name contentType.
     * The data can read either from the mediaFormat tag, which is not always present or from the contenType tag, that
     * is the parent of the mediaInformation. Since this is always available, we used this. (But maybe this is
     * not according to the MPEG7 intentions???)
     *
     * @return contentType can be used withn the set-Functions. if the xml file contains
     *         no contentType, the contentType of the last setContentType() call is returned or
     *         "Text" if this value is null or empty.
     */
    public String getContentType() {
        String returnValue = "Text";
        //String xPath = OFFSET + "Description/MultimediaContent/*/MediaInformation/MediaProfile/MediaFormat/Content";
        String xPath = OFFSET + "Description/MultimediaContent/*";
        //System.out.println(mpeg7.toString());
        if (contentType != null && contentType.length() > 0) {
            returnValue = contentType;
        }
        String result = readXmlValue(xPath, returnValue, true);
        assert(result != null);
        /*if (contentType == null) {
            contentType = "";
        }*/

        //System.err.println("RETURN: \"" + result + "\", default: \"" + returnValue + "\", contentType: \"" + contentType + "\"");
        contentType = result;
        returnValue = result;

        return returnValue;
    }

    /**
     * e.g. video, image, ...
     *
     * @param contentMediaformat can be used a contentType
     */
    public void setContentMediaformat(String contentMediaformat, String contentType) {
        this.contentType = contentMediaformat;
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/MediaInformation/MediaProfile/MediaFormat/Content";
        writeXmlValue(xPath, this.contentType);
    }

    /**
     * Summary about the content of the media file
     *
     * @return
     */
    public String getContentMediaSummary() {
        String xPath = OFFSET + "Description/MultimediaContent/*/TextAnnotation/FreeTextAnnotation";
        //System.out.println(mpeg7.toString());
        contentMediaSummary = readXmlValue(xPath, contentMediaSummary);
        if (contentMediaSummary == null) {
            contentMediaSummary = "";
        }
        return contentMediaSummary;
    }

    /**
     * Summary about the content of the media file
     *
     * @param contentMediaSummary
     */
    public void setContentMediaSummary(String contentMediaSummary, String contentType) {
        this.contentMediaSummary = contentMediaSummary;
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/TextAnnotation/FreeTextAnnotation";
        writeXmlValue(xPath, this.contentMediaSummary);


    }

    public String getContentMediaUri() {
        String xPath = OFFSET + "Description/MultimediaContent/*/MediaInformation/MediaProfile/MediaInstance/MediaLocator/MediaUri";
        //System.out.println(mpeg7.toString());
        contentMediaUri = readXmlValue(xPath, contentMediaUri);

        if (contentMediaUri == null) {
            contentMediaUri = "";
        }

        return contentMediaUri;
    }


    public void setContentMediaUri(String contentMediaUri, String contentType) {

        this.contentMediaUri = contentMediaUri;
        // change this path also in XmlMpeg7Result
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/MediaInformation/MediaProfile/MediaInstance/MediaLocator/MediaUri";
        writeXmlValue(xPath, this.contentMediaUri);
    }


    /**
     * where the description has been created
     *
     * @return
     */
    public String getContentCreationLocation() {
        String xPath = OFFSET + "Description/MultimediaContent/*/MediaInformation/CreationInformation/Creation/CreationCoordinates/CreationLocation/Name";
        //System.out.println(mpeg7.toString());
        contentCreationLocation = readXmlValue(xPath, contentCreationLocation);
        if (contentCreationLocation == null) {
            contentCreationLocation = "";
        }

        return contentCreationLocation;
    }

    /**
     * where the description has been created
     *
     * @param contentCreationLocation
     */
    public void setContentCreationLocation(String contentCreationLocation, String contentType) {
        this.contentCreationLocation = contentCreationLocation;
        String xPath = OFFSET + "Description/MultimediaContent/" + contentType + "/MediaInformation/CreationInformation/Creation/CreationCoordinates/CreationLocation/Name";
        writeXmlValue(xPath, this.contentCreationLocation);
    }

    /**
     * set a prived identifier to identify this record. This should be used instead of "Recorc/RecordId" of
     * Serachresults
     *
     * @param idUri
     */
    public void setDescriptionUri(String idUri) {
        // uses the Private identivier of MPEG-7. Tag should be addedd after the DescripitonMetata/Commment tag accoring
        // to the mpeg7-schema. Order is: Comment, PublicIdendifier(0..n), PrivateIdentifier(0..n)
        this.descriptionMediaId = idUri;
        String xPath = OFFSET + "DescriptionMetadata/PrivateIdentifier";
        writeXmlValue(xPath, this.descriptionMediaId);
    }

    /**
     * get the prived identifier to identify this record. This should be used instead of "Recorc/RecordId" of
     * Serachresults
     */
    public String getDescriptionUri() {
        // uses the Private identivier of MPEG-7. Tag should be addedd after the DescripitonMetata/Commment tag accoring
        // to the mpeg7-schema. Order is: Comment, PublicIdendifier(0..n), PrivateIdentifier(0..n)

        String xPath = OFFSET + "DescriptionMetadata/PrivateIdentifier";
        //System.out.println(mpeg7.toString());
        descriptionMediaId = readXmlValue(xPath, descriptionMediaId);
        if (descriptionMediaId == null) {
            descriptionMediaId = "";
        }

        return descriptionMediaId;
    }

    public Date getDescriptionCreationTime() {
        String xPath = OFFSET + "DescriptionMetadata/CreationTime";
        //System.out.println(mpeg7.toString());
        String dateStr = readXmlValue(xPath, "");
        if (dateStr.length() > 0) {
            descriptionCreationTime = Mpeg7ConversionTools.getReference().timePointToDate(dateStr);
        } else {
            descriptionCreationTime = null;
        }

        return descriptionCreationTime;
    }

    public void setDescriptionCreationTime(Date descriptionCreationTime) {
        this.descriptionCreationTime = descriptionCreationTime;
        String xPath = OFFSET + "DescriptionMetadata/CreationTime";
        writeXmlValue(xPath, Mpeg7ConversionTools.getReference().dateTotimePoint(this.descriptionCreationTime));
    }

    /**
     * who has created this description
     *
     * @return
     */
    public String getDescriptionCreatorAgentFamilyName() {
        String xPath = OFFSET + "DescriptionMetadata/Creator/Agent/Name/FamilyName";
        //System.out.println(mpeg7.toString());
        descriptionCreatorAgentFamilyName = readXmlValue(xPath, descriptionCreatorAgentFamilyName);
        if (descriptionCreatorAgentFamilyName == null) {
            descriptionCreatorAgentFamilyName = "";
        }

        return descriptionCreatorAgentFamilyName;
    }

    public void setDescriptionCreatorAgentFamilyName(String descriptionCreatorAgentFamilyName) {
        this.descriptionCreatorAgentFamilyName = descriptionCreatorAgentFamilyName;
        String xPath = OFFSET + "DescriptionMetadata/Creator/Agent/Name/FamilyName";
        writeXmlValue(xPath, this.descriptionCreatorAgentFamilyName);
    }

    public String getDescriptionCreatorAgentGivenName() {
        String xPath = OFFSET + "DescriptionMetadata/Creator/Agent/Name/GivenName";
        //System.out.println(mpeg7.toString());
        descriptionCreatorAgentGivenName = readXmlValue(xPath, descriptionCreatorAgentGivenName);
        if (descriptionCreatorAgentGivenName == null) {
            descriptionCreatorAgentGivenName = "";
        }
        return descriptionCreatorAgentGivenName;
    }

    public void setDescriptionCreatorAgentGivenName(String descriptionCreatorAgentGivenName) {
        this.descriptionCreatorAgentGivenName = descriptionCreatorAgentGivenName;
        String xPath = OFFSET + "DescriptionMetadata/Creator/Agent/Name/GivenName";
        writeXmlValue(xPath, this.descriptionCreatorAgentGivenName);
    }

    /**
     * e.g. creator
     *
     * @return
     */
    public String getDescriptionCreatorRole() {
        String xPath = OFFSET + "DescriptionMetadata/Creator/Role/Name";
        //System.out.println(mpeg7.toString());
        descriptionCreatorRole = readXmlValue(xPath, descriptionCreatorRole);
        if (descriptionCreatorRole == null) {
            descriptionCreatorRole = "";
        }
        return descriptionCreatorRole;
    }

    public void setDescriptionCreatorRole(String descriptionCreatorRole) {
        this.descriptionCreatorRole = descriptionCreatorRole;
        String xPath = OFFSET + "DescriptionMetadata/Creator/Role/Name";
        writeXmlValue(xPath, this.descriptionCreatorRole);
    }

    public Date getDescriptionLastUpdate() {
        String xPath = OFFSET + "DescriptionMetadata/LastUpdate";
        //System.out.println(mpeg7.toString());
        String dateStr = readXmlValue(xPath, "");
        if (dateStr.length() > 0) {
            descriptionLastUpdate = Mpeg7ConversionTools.getReference().timePointToDate(dateStr);
        } else {
            descriptionLastUpdate = null;
        }
        return descriptionLastUpdate;
    }

    public void setDescriptionLastUpdate(Date descriptionLastUpdate) {
        this.descriptionLastUpdate = descriptionLastUpdate;
        String xPath = OFFSET + "DescriptionMetadata/LastUpdate";
        writeXmlValue(xPath, Mpeg7ConversionTools.getReference().dateTotimePoint(this.descriptionLastUpdate));
    }

    /**
     * Summary describing the description purpose and context
     *
     * @return
     */
    public String getDescriptionSummary() {
        String xPath = OFFSET + "DescriptionMetadata/Comment/FreeTextAnnotation";
        //System.out.println(mpeg7.toString());
        descriptionSummary = readXmlValue(xPath, descriptionSummary);
        if (descriptionSummary == null) {
            descriptionSummary = "";
        }
        return descriptionSummary;
    }

    /**
     * Summary describing the description purpose and context
     *
     * @param descriptionSummary
     */
    public void setDescriptionSummary(String descriptionSummary) {
        this.descriptionSummary = descriptionSummary;
        String xPath = OFFSET + "DescriptionMetadata/Comment/FreeTextAnnotation";
        writeXmlValue(xPath, this.descriptionSummary);
    }

    /**
     * meaningful title descripting this desctipion and the content
     * Don't know where to get this title. There is just an FreeTextannotation and this fit better as "Summary"
     * @return
     */
    /*public String getDescriptionTitle() {
        String xPath = OFFSET + "DescriptionMetadata/Comment/FreeTextAnnotation";
        //System.out.println(mpeg7.toString());
        descriptionTitle = readXmlValue(xPath, descriptionTitle);
        if (descriptionTitle == null) {
            descriptionTitle = "";
        }
        return descriptionTitle;
    }*/

    /**
     * meaningful title descripting this desctipion and the content
     *
     * @param descriptionTitle
     */
    /*public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
        String xPath = OFFSET + "DescriptionMetadata/Comment/FreeTextAnnotation";
        writeXmlValue(xPath, this.descriptionTitle);
        //System.out.println(mpeg7.toString());
    }*/


    /**
     * name of the tool (Softwareaplication) used for createing the desctiption
     *
     * @return
     */
    public String getDescriptionTool() {
        String xPath = OFFSET + "DescriptionMetadata/Instrument/Tool/Name";
        //System.out.println(mpeg7.toString());
        descriptionTool = readXmlValue(xPath, descriptionTool);
        if (descriptionTool == null) {
            descriptionTool = "";
        }
        return descriptionTool;
    }

    /**
     * name of the tool (Softwareaplication) used for createing the desctiption
     *
     * @param descriptionTool
     */
    public void setDescriptionTool(String descriptionTool) {
        this.descriptionTool = descriptionTool;
        String xPath = OFFSET + "DescriptionMetadata/Instrument/Tool/Name";
        writeXmlValue(xPath, this.descriptionTool);
    }

    public String getDescriptionVersion() {
        String xPath = OFFSET + "DescriptionMetadata/Version";
        //System.out.println(mpeg7.toString());
        descriptionVersion = readXmlValue(xPath, descriptionVersion);
        if (descriptionVersion == null) {
            descriptionVersion = "";
        }
        return descriptionVersion;
    }

    public void setDescriptionVersion(String descriptionVersion) {
        this.descriptionVersion = descriptionVersion;
        String xPath = OFFSET + "DescriptionMetadata/Version";
        writeXmlValue(xPath, this.descriptionVersion);
    }


    private String readXmlValue(String xPath, String defaultValue, boolean doGetElementName) {
        String value = defaultValue;
        if (mpeg7 != null) {
            //value = mpeg7.getDomValue(xPath, defaultValue);
            //List<Element> valueList = XmlTools.xpathQuery(mpeg7.getDocument((Document) null), xPath, Mpeg7Template.getMpeg7Namespace());
            //System.out.println(mpeg7.toString());
            /*Document d = mpeg7.getDocument((Document) null);
            Namespace ns = d.getRootElement().getNamespace();*/
            //XmlTools.removeAllNamespacesRecursive(d.getRootElement());
            //xPath = "Mpeg7";
            List<Element> valueList = XmlTools.xpathQuery(mpeg7.getDocument((Document) null), xPath);
            //List<Element> valueList = XmlTools.xpathQuery(mpeg7.getDocument((Document) null), xPath, Mpeg7Template.getMpeg7Namespace());
            if (!valueList.isEmpty()) {
                Element valueElelement = valueList.get(0);
                if (valueElelement != null) {
                    if (doGetElementName) {
                        value = valueElelement.getName();
                    } else {
                        value = valueElelement.getText();
                    }
                }
            }
        }

        return value;
    }

    private String readXmlValue(String xPath, String defaultValue) {
        return readXmlValue(xPath, defaultValue, false);
    }


    private void writeXmlValue(String xPath, String value) {
        if (mpeg7 != null) {
            //mpeg7.setDomValue(xPath, Mpeg7Template.getMpeg7Namespace(), value, "");
            mpeg7.setDomValue(xPath, value);
        }
    }

    public Mpeg7Template getMpeg7() {
        return mpeg7;
    }

    public void setMpeg7(Mpeg7Template mpeg7) {
        this.mpeg7 = mpeg7;
        //if (this.mpeg7.getDocument((Document) null).)
    }

    public double getDescriptionVersion(double defaultValue) {
        double returnValue = defaultValue;

        try {
            Double.parseDouble(this.getDescriptionVersion());
        } catch (Exception e) {
            //e.printStackTrace();
            //silent catch: use defaultvalue on error
        }

        return returnValue;
    }
}
