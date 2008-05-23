<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="http://www.w3.org/2001/XMLSchema" xmlns:mpeg7="urn:mpeg:mpeg7:schema:2001">
    <xsl:template match="/">
        <html>
            <head/>
            <body>
                <xsl:apply-templates select="//mpeg7:Description"/>
                <xsl:apply-templates select="//mpeg7:DescriptionMetadata"/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="//mpeg7:DescriptionMetadata">
        <font size="2">
            <i>
    	(Description created by:
                <xsl:value-of select="mpeg7:Creator/mpeg7:Agent/mpeg7:Name/mpeg7:GivenName"/>
                <xsl:value-of select="mpeg7:Creator/mpeg7:Agent/mpeg7:Name/mpeg7:FamilyName"/> with
                <xsl:value-of select="mpeg7:Instrument/mpeg7:Tool/mpeg7:Name"/>)
            </i>
        </font>
    </xsl:template>
    <xsl:template match="//mpeg7:Description">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="//mpeg7:Image">
        <table width="480">
            <tbody>

                <tr>
                    <td width="120">
                        <b>Relevance:</b>
                    </td>
                    <td>
                        _relevance_
                    </td>
                </tr>
                <tr>
                    <td>
                        <b>File:</b>
                    </td>
                    <td>
                        <a href="{mpeg7:MediaInformation/mpeg7:MediaProfile[@master='true']/mpeg7:MediaInstance/mpeg7:MediaLocator/mpeg7:MediaUri}">_filename_</a> (
                        <xsl:value-of select="mpeg7:MediaInformation/mpeg7:MediaProfile[@master='true']/mpeg7:MediaFormat/mpeg7:VisualCoding/mpeg7:Frame/@width"/>
				x
                        <xsl:value-of select="mpeg7:MediaInformation/mpeg7:MediaProfile[@master='true']/mpeg7:MediaFormat/mpeg7:VisualCoding/mpeg7:Frame/@height"/> pixel)
						from
                        <xsl:value-of select="mpeg7:CreationInformation/mpeg7:Creation/mpeg7:CreationCoordinates/mpeg7:Date/mpeg7:TimePoint"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <b>Creator:</b>
                    </td>
                    <td>
                        <xsl:value-of select="mpeg7:CreationInformation/mpeg7:Creation/mpeg7:Creator/mpeg7:Agent/mpeg7:Name/mpeg7:GivenName"/>
                        <xsl:value-of select="mpeg7:CreationInformation/mpeg7:Creation/mpeg7:Creator/mpeg7:Agent/mpeg7:Name/mpeg7:FamilyName"/>
						,
                        <xsl:value-of select="mpeg7:CreationInformation/mpeg7:Creation/mpeg7:CreationTool/mpeg7:Tool/mpeg7:Name"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <b>Quality rating:</b>
                    </td>
                    <td>
                        <xsl:value-of select="mpeg7:MediaInformation/mpeg7:MediaProfile/mpeg7:MediaQuality/mpeg7:QualityRating/mpeg7:RatingValue"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
						Description:
                        <xsl:value-of select="mpeg7:TextAnnotation/mpeg7:FreeTextAnnotation"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
						Semantic Objects: _semanticdescription_
                    </td>
                </tr>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template match="//mpeg7:SemanticBase">
        <xsl:value-of select="mpeg7:Label/mpeg7:Name"/>,
    </xsl:template>
</xsl:stylesheet>
