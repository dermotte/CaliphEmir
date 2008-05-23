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

package at.lux.fotoannotation;

import at.knowcenter.caliph.objectcatalog.semanticscreator.IMBeeApplicationPanel;
import at.lux.components.ImageThumbPanel;
import at.lux.fotoannotation.panels.*;
import at.lux.imageanalysis.*;
import at.lux.imaging.BmpReader;
import at.lux.imaging.PpmReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.iptc.IptcReader;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * ImageLoader
 * Date: 14.09.2002
 * Time: 21:28:49
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ImageLoader extends Thread {
    public static DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();

    private JLabel status;
    private AnnotationFrame parent;
    private File img;
    private ImageThumbPanel imgPanel;
    private CreationPanel creationPanel;
    private TextDescriptionPanel textPanel;
    MetadataDescriptionPanel mdpanel;
    QualityPanel qPanel;
    IMBeeApplicationPanel bee;
    ColorLayoutPanel colorPanel;
    ShapePanel shapePanel;

    public ImageLoader(JLabel status, AnnotationFrame parent, File img, ImageThumbPanel imgPanel,
                       CreationPanel creationPanel, TextDescriptionPanel textPanel, MetadataDescriptionPanel mdpanel,
                       QualityPanel qPanel, IMBeeApplicationPanel bee, ColorLayoutPanel colorPanel, ShapePanel shapePanel) {
        this.status = status;
        this.parent = parent;
        this.img = img;
        this.imgPanel = imgPanel;
        this.creationPanel = creationPanel;
        this.textPanel = textPanel;
        this.mdpanel = mdpanel;
        this.qPanel = qPanel;
        this.bee = bee;
        this.colorPanel = colorPanel;
        this.shapePanel = shapePanel;
    }

    public void run() {
        parent.setEnabled(false);
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        imgPanel.setWait(true);
        status.setText("Please wait while loading image " + img.toString());
        try {
            // loading the image
            BufferedImage image;
            if (img.getName().toLowerCase().endsWith(".ppm")) {
                image = PpmReader.read(new FileInputStream(img));
            } else if (img.getName().toLowerCase().endsWith(".bmp")) {
                image = BmpReader.read(new FileInputStream(img));
            } else {
                image = ImageIO.read(new FileInputStream(img));
            }
            imgPanel.setImage(image);
            imgPanel.repaint();
            // clearing previous information:
            if (!parent.isAutopilot()) { // check if the autopilot was the source of the call!
                textPanel.clearTextFields();
            }
            // extracting information
            extractInformation(image);
            status.setText("Finished");
        } catch (IOException e) {
            status.setText("Error: " + e.toString());
        } finally {
            imgPanel.setWait(false);
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            parent.setEnabled(true);
            imgPanel.repaint();
        }
    }

    private void extractInformation(BufferedImage image) {
        // Standardwerte wiederherstellen:
        mdpanel.setToCurrentTime();
        creationPanel.resetTable();

        debug("Generating thumbnail ...");
        AnnotationToolkit.generateThumbnail(img);
        debug("Extracing information from the image itself");
        creationPanel.setBitsPerPixel(image.getColorModel().getPixelSize() + "");
        creationPanel.setImageSize(image.getWidth() + "", image.getHeight() + "");
        creationPanel.setFileSize(img.length() + "");
        if (img.getName().toLowerCase().endsWith(".jpg") || img.getName().toLowerCase().endsWith(".jpeg"))
            creationPanel.setFileFormat("JPEG");
        else if (img.getName().toLowerCase().endsWith(".ppm")) {
            creationPanel.setFileFormat("PPM");
        } else if (img.getName().toLowerCase().endsWith(".bmp")) {
            creationPanel.setFileFormat("BMP");
        } else if (img.getName().toLowerCase().endsWith(".png")) {
            creationPanel.setFileFormat("PNG");
        }
        String textPanelContent = null;
        try {
            // ----------------------------
            // Reading XMP (Adobe Photoshop
            // specific metadata format)
            // ----------------------------
            File xmpFile;
            xmpFile = new File(img.getCanonicalPath() + ".xmp");
            if (xmpFile.exists()) {
                SAXBuilder builder;
                builder = new SAXBuilder();
                try {
                    Element xmpRoot;
                    String suppCat = "", xmpDesc = "", xmpWords = "";
                    xmpRoot = builder.build(xmpFile).getRootElement();
                    // Create the whole bunch of needed namespaces:
                    XPath path = null;
                    try {
                        // Supplemental Categories:
                        path = new JDOMXPath("//photoshop:SupplementalCategories/rdf:Bag/rdf:li");
                        path.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
                        path.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                        path.addNamespace("photoshop", "http://ns.adobe.com/photoshop/1.0/");
                        List results = path.selectNodes(xmpRoot);
                        if (results.size() > 0) {
                            StringBuffer buff = new StringBuffer();
                            for (Iterator i = results.iterator(); i.hasNext();) {
                                Element elem = (Element) i.next();
                                if (elem.getTextTrim().length() > 0) {
                                    buff.append(elem.getTextTrim());
                                    if (i.hasNext())
                                        buff.append(", ");
                                }
                            }
                            debug(buff.toString() + " found as supplemental categories");
                            suppCat = buff.toString();
                        } else {
                            debug("no supplementalCategories found ...");
                        }
                        // Description:
                        path = new JDOMXPath("//dc:description/rdf:Alt/rdf:li");
                        path.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
                        path.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                        results = path.selectNodes(xmpRoot);
                        if (results.size() > 0) {
                            StringBuffer buff = new StringBuffer();
                            for (Iterator i = results.iterator(); i.hasNext();) {
                                Element elem = (Element) i.next();
                                if (elem.getTextTrim().length() > 0) {
                                    buff.append(elem.getTextTrim());
                                    if (i.hasNext())
                                        buff.append(", ");
                                }
                            }
                            debug(buff.toString() + " found as dc description");
                            xmpDesc = buff.toString();
                        } else {
                            debug("no dc description found ...");
                        }
                        if (suppCat.length() > 0) {
                            suppCat = suppCat + ", ";
                        }
                        if (xmpDesc.length() > 0) {
                            xmpDesc = xmpDesc + ", ";
                        }
                        if (xmpWords.length() > 0) {
                            xmpWords = xmpWords + ", ";
                        }
                        textPanel.setDescriptionText(suppCat + xmpDesc + xmpWords);
                        textPanelContent = suppCat + xmpDesc + xmpWords;
                        // Description:
                        path = new JDOMXPath("//dc:subject/rdf:Bag/rdf:li");
                        path.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
                        path.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                        results = path.selectNodes(xmpRoot);
                        if (results.size() > 0) {
                            StringBuffer buff = new StringBuffer();
                            for (Iterator i = results.iterator(); i.hasNext();) {
                                Element elem = (Element) i.next();
                                if (elem.getTextTrim().length() > 0) {
                                    buff.append(elem.getTextTrim());
                                    if (i.hasNext())
                                        buff.append(", ");
                                }
                            }
                            debug(buff.toString() + " found as dc keywords");
                            xmpDesc = buff.toString();
                        } else {
                            debug("no dc keywords found ...");
                        }
                    } catch (JaxenException e) {
                        debug("Error handling XPath: " + e.toString());
                    }
                } catch (JDOMException e) {
                    // XMP not processable
                    debug("XMP not processable: " + e.toString());
                }
            } else {
                debug("XMP file " + xmpFile.getName() + "not present ... ");
            }
            // ----------------------------
            // Reading EXIF
            // ----------------------------
            debug("Extracing EXIF");
            status.setText("Extracting EXIF ...");
            // reading out EXIF and IPTC:
            Metadata metadata = new Metadata();
            new ExifReader(img).extract(metadata);
            new IptcReader(img).extract(metadata);

            // getting exif tags:
            Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
            try {
                Date exifdate = null;
                if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME_ORIGINAL))
                    exifdate = exifDirectory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
                else if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME))
                    exifdate = exifDirectory.getDate(ExifDirectory.TAG_DATETIME);
                else if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME_DIGITIZED))
                    exifdate = exifDirectory.getDate(ExifDirectory.TAG_DATETIME_DIGITIZED);
                if (exifdate != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(exifdate);
                    String time = c.get(Calendar.YEAR)
                            + "-" + AnnotationToolkit.to2letters(c.get(Calendar.MONTH) + 1)
                            + "-" + AnnotationToolkit.to2letters(c.get(Calendar.DAY_OF_MONTH))
                            + "T" + AnnotationToolkit.to2letters(c.get(Calendar.HOUR_OF_DAY))
                            + ":" + AnnotationToolkit.to2letters(c.get(Calendar.MINUTE))
                            + ":" + AnnotationToolkit.to2letters(c.get(Calendar.SECOND));
                    creationPanel.setTime(time);
                    String dateString = AnnotationToolkit.toMonthString(c.get(Calendar.MONTH)) + ' ' + c.get(Calendar.YEAR);
                    textPanel.setWhen(dateString);
                    // Setting the time to some Properties in the parent frame to read them later on. 
                    parent.getCurrentFileProperties().setProperty("exif.time.short.string", dateString);
                    parent.getCurrentFileProperties().setProperty("exif.time.full.string", time);
//                    debug("Time set with Date-object: " + exifdate.toString());
                } else {
                    parent.getCurrentFileProperties().setProperty("exif.time.short.string", "n.a");
                    parent.getCurrentFileProperties().setProperty("exif.time.full.string", "n.a");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Iterator tagIterator = exifDirectory.getTagIterator();
            while (tagIterator.hasNext()) {
                try {
                    Tag currentTag = (Tag) tagIterator.next();
                    int tagType = exifDirectory.getInt(currentTag.getTagType());
                    status.setText("Extracting EXIF: " + currentTag.getTagName());
                    debug(exifDirectory.getTagName(tagType) + ": " + exifDirectory.getString(tagType));
                    creationPanel.addKeyValuePair(currentTag.getTagName(), currentTag.getDescription());
                } catch (MetadataException e) {
                    System.err.println("Exception parsing tag .. " + e.toString());
                }
            }
            // ----------------------------
            // Reading IPTC
            // ----------------------------
            Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
            tagIterator = iptcDirectory.getTagIterator();

            StringBuffer buff = new StringBuffer();
            if (iptcDirectory.containsTag(IptcDirectory.TAG_CAPTION)) {
                buff.append(iptcDirectory.getString(IptcDirectory.TAG_CAPTION) + "\n");
            }
            if (iptcDirectory.containsTag(IptcDirectory.TAG_SUPPLEMENTAL_CATEGORIES)) {
                buff.append(iptcDirectory.getString(IptcDirectory.TAG_SUPPLEMENTAL_CATEGORIES) + "\n");
            }
            if (iptcDirectory.containsTag(IptcDirectory.TAG_KEYWORDS)) {
                buff.append(iptcDirectory.getString(IptcDirectory.TAG_KEYWORDS) + "\n");
            }

            if (textPanelContent != null) {
                textPanel.setDescriptionText(textPanelContent + "\n" + buff.toString());
            } else {
                textPanel.setDescriptionText(buff.toString());
            }
            while (tagIterator.hasNext()) {
                try {
                    Tag currentTag = (Tag) tagIterator.next();
//                    int tagType = iptcDirectory.getInt(currentTag.getTagType());
                    status.setText("Extracting IPTC: " + currentTag.getTagName());
//                    System.out.println("Extracting IPTC: " + currentTag.getTagName());
                    debug(currentTag.getTagName() + ": " + currentTag.getTagName());
                    creationPanel.addKeyValuePair("(IPTC) " + currentTag.getTagName(), currentTag.getDescription());
                } catch (MetadataException e) {
                    System.err.println("Exception parsing tag .. " + e.toString());
                }
            }
            creationPanel.updateTable();
        } catch (IOException e) {
            debug("IOException reading EXIF: " + e.getMessage());
        } catch (JpegProcessingException e) {
            debug("Could not read metadata: " + e.getMessage());
        }
        /*
        catch (ExifProcessingException e) {
            // Keine EXIF Info?
            debug("ExifProcessingException: " + e.getMessage());
            // creating std tags ... (werden manuell befüllt)
            creationPanel.addKeyValuePair("Make", "");
            creationPanel.addKeyValuePair("Model", "");
            // Setting time from file ... weil ja keine Info da ..
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(img.lastModified()));
            String time = AnnotationToolkit.getMpeg7Time(c);
            creationPanel.setTime(time);
        } catch (MetadataException e) {
            e.printStackTrace();
        }
        */

        try {
            String filePath = img.getCanonicalPath();
            String mp7name = filePath.substring(0, filePath.lastIndexOf('.')) + ".mp7.xml";

            File mp7file = new File(mp7name);
            if (mp7file.exists()) {
                debug("Reading existing MPEG-7 information " + mp7name);
                status.setText("Reading existing MPEG-7 information");
                // ----------------------------
                // Reading from the MPEG-7 File
                // ----------------------------
                SAXBuilder builder = new SAXBuilder();
                Element root = builder.build(mp7file).getRootElement();

                // in case of the shape panel the whole document is
                // used to set the shapes ...
                shapePanel.setDescriptor(root);

                // textual description of the File ..
                String text = getSingleValue(root, "//Image/TextAnnotation/FreeTextAnnotation");
                if (text != null) textPanel.setDescriptionText(text);

                text = getSingleValue(root, "//Image/TextAnnotation/StructuredAnnotation/Who/Name");
                if (text != null) textPanel.setWho(text);
                text = getSingleValue(root, "//Image/TextAnnotation/StructuredAnnotation/When/Name");
                if (text != null) textPanel.setWhen(text);
                text = getSingleValue(root, "//Image/TextAnnotation/StructuredAnnotation/Where/Name");
                if (text != null) textPanel.setWhere(text);
                text = getSingleValue(root, "//Image/TextAnnotation/StructuredAnnotation/Why/Name");
                if (text != null) textPanel.setWhy(text);
                text = getSingleValue(root, "//Image/TextAnnotation/StructuredAnnotation/How/Name");
                if (text != null) textPanel.setHow(text);
                text = getSingleValue(root, "//Image/TextAnnotation/StructuredAnnotation/WhatAction/Name");
                if (text != null) textPanel.setWhatAction(text);
                text = getSingleValue(root, "//Image/TextAnnotation/StructuredAnnotation/WhatObject/Name");
                if (text != null) textPanel.setWhatObject(text);

                // description version
                text = getSingleValue(root, "//DescriptionMetadata/Version");
                if (text != null) {
                    mdpanel.setVersion(Double.toString(Double.parseDouble(text) + 1d));
                }
                // description tool
                text = getSingleValue(root, "//DescriptionMetadata/Instrument/Tool/Name");
                if (text != null) mdpanel.setInstrument(text);
                // description date
                text = getSingleValue(root, "//DescriptionMetadata/CreationTime");
                if (text != null) mdpanel.setTime(text);
                // description comment
                text = getSingleValue(root, "//DescriptionMetadata/Comment/FreeTextAnnotation");
                if (text != null) mdpanel.setTextDescription(text);
                // quality & definition ob der Descriptor gebraucht wird ...
                text = getSingleValue(root, "//Image/MediaInformation/MediaProfile/MediaQuality/QualityRating/RatingValue");
                if (text != null) {
                    qPanel.setQuality(Integer.parseInt(text));
                    qPanel.setIncludeQuality(true);
                } else {
                    qPanel.setIncludeQuality(false);
                }
                // quality agent
                text = getSingleValue(root, "//Image/MediaInformation/MediaProfile/MediaQuality/RatingSource/Name/GivenName");
                String text2 = getSingleValue(root, "//Image/MediaInformation/MediaProfile/MediaQuality/RatingSource/Name/FamilyName");
                if (text != null && text2 != null) {
                    debug("setting quality agent: " + text2 + ", " + text);
                    qPanel.setAgent(text2 + ", " + text);
                }
                // description metadata agent
                text = getSingleValue(root, "//DescriptionMetadata/Creator/Agent/Name/GivenName");
                text2 = getSingleValue(root, "//DescriptionMetadata/Creator/Agent/Name/FamilyName");
                if (text != null && text2 != null) {
                    debug("setting description metadata agent: " + text2 + ", " + text);
                    mdpanel.setAgent(text2 + ", " + text);
                }
                // creation agent
                text = getSingleValue(root, "//CreationInformation/Creation/Creator/Agent/Name/GivenName");
                text2 = getSingleValue(root, "//CreationInformation/Creation/Creator/Agent/Name/FamilyName");
                if (text != null && text2 != null) {
                    debug("setting creation agent: " + text2 + ", " + text);
                    creationPanel.setAgent(text2 + ", " + text);
                }

                // semantics ...
                java.util.List results = AnnotationToolkit.xpathQuery(root, "//Image/Semantic", null);
                if (results.size() > 0) {
                    debug("setting bee semantic description ...");
                    bee.setSemantics((Element) ((Element) results.get(0)).detach());
                }

                // Scalable Color
                boolean ccset = false, clset = false, ehset = false, dcset = false;
                results = AnnotationToolkit.xpathQuery(root, "//VisualDescriptor", null);
                if (results.size() > 0) {
                    for (Iterator it1 = results.iterator(); it1.hasNext();) {
                        Element elem = (Element) it1.next();
                        java.util.List atts = elem.getAttributes();
                        for (Iterator it2 = atts.iterator(); it2.hasNext();) {
                            Attribute att = (Attribute) it2.next();
                            if (att.getValue().equals("ScalableColorType")) {
                                debug("setting descriptor ScalableColor ...");
                                colorPanel.setScalableColor(new ScalableColor((Element) elem.detach()));
                                ccset = true;
                            } else if (att.getValue().equals("ColorLayoutType")) {
                                debug("setting descriptor ColorLayout ...");
                                colorPanel.setColorLayout(new ColorLayout((Element) elem.detach()));
                                clset = true;
                            } else if (att.getValue().equals("DominantColorType")) {
                                debug("setting descriptor DominantColor ...");
                                try {
                                    colorPanel.setDominantColor(new DominantColor((Element) elem.detach()));
                                    dcset = true;
                                } catch (VisualDescriptorException e) {
                                    e.printStackTrace();
                                }
                            } else if (att.getValue().equals("EdgeHistogramType")) {
                                debug("setting descriptor EdgeHistogram ...");
                                try {
                                    colorPanel.setEdgeHistogram(new EdgeHistogram((Element) elem.detach()));
                                    ehset = true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                if (!ccset) {
                    debug("Did not get the descriptor from XML: Extracing visual descriptor ScalableColor");
                    status.setText("Extracing visual descriptor ScalableColor ...");
                    colorPanel.setScalableColor(new ScalableColor(image));
                }
                if (!clset) {
                    debug("Did not get the descriptor from XML: Extracing visual descriptor ColorLayout");
                    status.setText("Extracing visual descriptor ColorLayout ...");
                    colorPanel.setColorLayout(new ColorLayout(image));
                }
                // Scaling down the image to speed up the extraction thing:
                if (!ehset || !dcset) {
                    if (Math.max(image.getWidth(), image.getHeight()) > 640) {
                        status.setText("Scaling down image for extracing Descriptors ...");
                        // switch images
                        image = scaleImage(image, 640);
                    }
                }
                if (!ehset) {
                    debug("Did not get the descriptor from XML: Extracing visual descriptor EdgeHistogram");
                    status.setText("Extracing visual descriptor EdgeHistogram ...");
                    colorPanel.setEdgeHistogram(new EdgeHistogram(image));
                }
                if (!dcset) {
                    debug("Did not get the descriptor from XML: Extracing visual descriptor DominantColor");
                    status.setText("Extracing visual descriptor DominantColor ...");
                    colorPanel.setDominantColor(new DominantColor(image));
                }

                debug("Finished reading MPEG-7 description.");
                AnnotationFrame.setDirty(false);
                parent.setTitle(AnnotationFrame.TITLE_BAR + ": " + img.getName());
            } else {
                debug("No MPEG-7 Description found, I've searched for " + mp7name);
                // here we check if the image is too big and scale it down:
                // todo: configure via properties.
                int maxSideLength = 640;
                if (Math.max(image.getWidth(), image.getHeight()) > maxSideLength) {
                    status.setText("Scaling down image for extracing Descriptors ...");
                    // switch images
                    image = scaleImage(image, maxSideLength);
                }
                debug("Extracing visual descriptor ColorLayout");
                status.setText("Extracing visual descriptor ColorLayout ...");
                colorPanel.setColorLayout(new ColorLayout(image));
                debug("Extracing visual descriptor ScalableColor");
                status.setText("Extracing visual descriptor ScalableColor ...");
                colorPanel.setScalableColor(new ScalableColor(image));
                status.setText("Extracing visual descriptor EdgeHistogram ...");
                colorPanel.setEdgeHistogram(new EdgeHistogram(image));
                status.setText("Extracing visual descriptor DominantColor ...");
                colorPanel.setDominantColor(new DominantColor(image));
                status.setText("Setting image in shape panel ...");
                shapePanel.setImage(image);
                AnnotationFrame.DIRTY = true;
            }
        } catch (IOException e) {
            debug("IOException while searching and reading existing MPEG-7 description " + e.toString());
        } catch (JDOMException e) {
            debug("Exception parsing existing MPEG-7 description" + e.toString());
        }

    }

    public static BufferedImage scaleImage(BufferedImage image, int maxSideLength) {
        double ow = image.getWidth();
        double oh = image.getHeight();
        double scale = 0.0;
        if (ow > oh) {
            scale = ((double) maxSideLength / ow);
        } else {
            scale = ((double) maxSideLength / oh);
        }
        // create smaller image
        BufferedImage img = new BufferedImage((int) (ow * scale), (int) (oh * scale), BufferedImage.TYPE_INT_RGB);
        // fast scale
        Graphics g = img.getGraphics();
        g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
    }

    private void debug(String message) {
        if (AnnotationFrame.DEBUG)
            System.out.println("[at.lux.fotoannotation.ImageLoader] " + message);
    }

//    private void collectGarbage() {
//        debug("Mem: "
//                + df.format(Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) + "MB of "
//                + df.format(Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) + "MB free");
//
//        debug("starting garbage collector ...");
//        System.gc();
//        debug("finished collecting garbage!");
//
//        debug("Mem: "
//                + df.format(Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) + "MB of "
//                + df.format(Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) + "MB free");
//        status.setText("Garbage collection finished: "
//                + df.format(Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) + "MB of "
//                + df.format(Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) + "MB free");
//
//    }

    private String getSingleValue(Element root, String xpath) {
        String text = null;
        java.util.List results = AnnotationToolkit.xpathQuery(root, xpath, null);
        if (results.size() > 0) {
            text = ((Element) results.get(0)).getTextTrim();
        }
        return text;
    }
}
