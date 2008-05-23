package at.lux.fotoretrieval.retrievalengines;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JProgressBar;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import at.lux.components.StatusBar;
import at.lux.fotoretrieval.ResultListEntry;
import at.lux.fotoretrieval.RetrievalToolkit;
import at.lux.imageanalysis.ColorLayout;
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.JDomVisualDescriptor;
import at.lux.imageanalysis.ScalableColor;
import at.lux.imageanalysis.VisualDescriptorException;
import at.lux.imageanalysis.db.SQLGenerator;
import at.lux.imageanalysis.db.SQLGeneratorFactory;
import at.lux.fotoannotation.ImageLoader;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcReader;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
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

/**
 * This file is part of Caliph & Emir
 * Date: 27.10.2005
 * Time: 22:11:36
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DatabaseRetrievalEngine extends AbstractRetrievalEngine {
    private static Logger log = Logger.getLogger(DatabaseRetrievalEngine.class.getName());

    private static int MAX_COUNT = 50;
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static SAXBuilder builder = new SAXBuilder();
    private static final boolean OPTION_READ_FROM_XML = false;

    public List<ResultListEntry> getImagesBySemantics(String xPath, Vector objects, String whereToSearch, boolean recursive, JProgressBar progress) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public List<ResultListEntry> getSimilarImages_fromSet(Set<Element> VisualDescriptorSet, String whereToSearch, boolean recursive, JProgressBar progress) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public List<ResultListEntry> getSimilarImages(Element visualDescriptor, String whereToSearch, boolean recursive, JProgressBar progress) {
        List<ResultListEntry> results = new LinkedList<ResultListEntry>();
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        String descType = visualDescriptor.getAttributeValue("type", xsi);
        at.lux.imageanalysis.JDomVisualDescriptor desc = null;
        if (descType.equals("ColorLayoutType")) {
            desc = new ColorLayout(visualDescriptor);
        } else if (descType.equals("EdgeHistogramType")) {
            try {
                desc = new EdgeHistogram(visualDescriptor);
            } catch (VisualDescriptorException e) {
                e.printStackTrace();
            }
        } else if (descType.equals("ScalableColorType")) {
            desc = new ScalableColor(visualDescriptor);
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        if (desc != null) {
            // retrieve from the database:
            Connection conn = null;
            try {
                Class.forName(driver).newInstance();
                conn = DriverManager.getConnection("jdbc:derby:imageDB");
                Statement statement = conn.createStatement();
                SQLGenerator sqlGenerator = SQLGeneratorFactory.getSQLGenerator(SQLGeneratorFactory.Database.Derby);
                String searchSelectStatement = sqlGenerator.getSearchSelectStatement(desc);
                if (progress != null) {
                    progress.setString("Searching in DB ...");
                }
                ResultSet rs = statement.executeQuery(searchSelectStatement);
                if (progress != null) {
                    progress.setString("Reading results from DB ...");
                }
                int count = 0;
                SAXBuilder builder = new SAXBuilder();
                if (progress != null) {
                    progress.setMinimum(0);
                    progress.setMaximum(MAX_COUNT);
                    progress.setValue(0);
                }

                while (rs.next() && count < MAX_COUNT) {
                    String file = rs.getString(1);
                    int index = file.lastIndexOf('.');
                    String descriptionFilePath = file.substring(0, index) + ".mp7.xml";
                    File descriptionFile = new File(descriptionFilePath);
                    if (OPTION_READ_FROM_XML && descriptionFile.exists()) {
                        try {
                            Element e = builder.build(descriptionFile).getRootElement();
                            ResultListEntry entry = new ResultListEntry(rs.getDouble(2), e, descriptionFilePath);
                            results.add(entry);
                        } catch (Exception e1) {
                            log.warning("Could not add entry to results: " + e1.toString());
                        }
                    } else {
                        String thumbnailPath = file.substring(0, file.lastIndexOf(File.separatorChar) + 1) + "tn_" + file.substring(file.lastIndexOf(File.separatorChar) + 1);
                        File thumbnNail = new File(thumbnailPath);
                        String creatorName="", description="", creationTime="", imageSize = "";
                        if (file.toLowerCase().endsWith(".jpg")) {
                            Metadata metadata = new Metadata();
                            File imgFile = new File(file);
                            new ExifReader(imgFile).extract(metadata);
                            new IptcReader(imgFile).extract(metadata);
                            Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
                            if (metadata.containsDirectory(IptcDirectory.class)) {
                                Directory iptc = metadata.getDirectory(IptcDirectory.class);
                                if (iptc.containsTag(IptcDirectory.TAG_DATE_CREATED))
                                    creationTime = iptc.getString(IptcDirectory.TAG_DATE_CREATED);
                                if (iptc.containsTag(IptcDirectory.TAG_CAPTION))
                                    description = iptc.getString(IptcDirectory.TAG_CAPTION);
                                if (iptc.containsTag(IptcDirectory.TAG_COPYRIGHT_NOTICE))
                                    creatorName = iptc.getString(IptcDirectory.TAG_COPYRIGHT_NOTICE);

                            }

                            if (metadata.containsDirectory(ExifDirectory.class)) {
                                if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME_ORIGINAL))
                                    creationTime = exifDirectory.getString(ExifDirectory.TAG_DATETIME_ORIGINAL);
                                else if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME))
                                    creationTime = exifDirectory.getString(ExifDirectory.TAG_DATETIME);
                                else if (exifDirectory.containsTag(ExifDirectory.TAG_DATETIME_DIGITIZED))
                                    creationTime = exifDirectory.getString(ExifDirectory.TAG_DATETIME_DIGITIZED);

                                if (exifDirectory.containsTag(ExifDirectory.TAG_MAKE))
                                    creatorName += exifDirectory.getString(ExifDirectory.TAG_MAKE) + " ";
                                if (exifDirectory.containsTag(ExifDirectory.TAG_MODEL))
                                    creatorName += exifDirectory.getString(ExifDirectory.TAG_MODEL);
                                if (exifDirectory.containsTag(ExifDirectory.TAG_EXIF_IMAGE_WIDTH) && exifDirectory.containsTag(ExifDirectory.TAG_EXIF_IMAGE_HEIGHT))
                                    imageSize = exifDirectory.getString(ExifDirectory.TAG_EXIF_IMAGE_WIDTH) + "x" + exifDirectory.getString(ExifDirectory.TAG_EXIF_IMAGE_HEIGHT);

                            }
                        }
                        if (!thumbnNail.exists()) {
                            thumbnailPath = file;
                        }
                        ResultListEntry entry = new ResultListEntry(rs.getDouble(2), thumbnailPath, file, creatorName, description, creationTime, file, imageSize);
                        results.add(entry);
                    }
                    if (progress != null) {
                        progress.setValue(++count);
                    }

                }
            } catch (Exception e) {
                log.warning("Error: " + e.toString());
            } finally {
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        return results;
    }

    public List<ResultListEntry> getImagesByXPathSearch(String xPath, String whereToSearch, boolean recursive, JProgressBar progress) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void indexAllImages(String pathToIndex, StatusBar statusBar) {
        File f = new File(pathToIndex);
        if (!f.exists()) return;

        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        df.setMaximumFractionDigits(0);
        df.setMinimumIntegerDigits(2);

        Connection conn = null;

        try {
            statusBar.setStatus("Getting database connection.");
            SQLGenerator gen = SQLGeneratorFactory.getSQLGenerator(SQLGeneratorFactory.Database.Derby);
            Class.forName(gen.getDriverClassName()).newInstance();
            conn = DriverManager.getConnection(gen.getConnectionURL());
            Statement statement = conn.createStatement();
            statusBar.setStatus("Creating database and tables.");
            // create tables:
            // todo: provide a clean entry to the current state of the database. Drop database or something else!
            String tableCreate = gen.getCreateTableStatement(JDomVisualDescriptor.Type.ColorLayout);
            try {
                statement.executeUpdate(tableCreate);
            } catch (SQLException e) {
                log.warning("Could not create table for ColorLayout:" + e.toString());
            }

            tableCreate = gen.getCreateTableStatement(JDomVisualDescriptor.Type.EdgeHistogram);
            try {
                statement.executeUpdate(tableCreate);
            } catch (SQLException e) {
                log.warning("Could not create table for EdgeHistogram:" + e.toString());
            }

            tableCreate = gen.getCreateTableStatement(JDomVisualDescriptor.Type.ScalableColor);
            try {
                statement.executeUpdate(tableCreate);
            } catch (SQLException e) {
                log.warning("Could not create table for ScalableColor:" + e.toString());
            }

            String[] images = RetrievalToolkit.getAllImages(f, true);
            double len = images.length;
            long ms = System.currentTimeMillis();
            for (int i = 0; i < images.length; i++) {
                String image = images[i];
                try {
                    String descriptionFile = image.substring(0, image.lastIndexOf('.')) + ".mp7.xml";
                    File description = new File(descriptionFile);
                    if (description.exists()) {
                        log.finest("Indexing from description file: " + descriptionFile);
                        indexFileFromDescription(descriptionFile, image, gen, statement);
                    } else {
                        log.finest("Indexing from image file: " + image);
                        indexFileFromImage(gen, image, statement);
                    }
                    double percent = ((double) i) / len * 100d;
                    statusBar.setStatus("Finished " + df.format(percent) + "%");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("ms = " + (System.currentTimeMillis() - ms));
        } catch (Exception e) {
            log.warning(e.toString());
        }

    }

    private void indexFileFromImage(SQLGenerator gen, String imageFilePath, Statement statement) throws IOException, SQLException {
        // TODO: The generation of image descriptors from file has to be made from smaller version of the image (speed and memory issues)
        BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
        img = ImageLoader.scaleImage(img, 512);
        ScalableColor sc = new ScalableColor(img);
        String sql = gen.getInsertStatement(imageFilePath, sc);
        statement.executeUpdate(sql);
        EdgeHistogram eh = new EdgeHistogram(img);
        sql = gen.getInsertStatement(imageFilePath, eh);
        statement.executeUpdate(sql);
        ColorLayout cl = new ColorLayout(img);
        sql = gen.getInsertStatement(imageFilePath, cl);
        statement.executeUpdate(sql);
    }

    private void indexFileFromDescription(String descriptionFilePath, String imageFilePath, SQLGenerator gen, Statement statement) throws JDOMException, IOException, SQLException, VisualDescriptorException {
        Document document = builder.build(new FileInputStream(descriptionFilePath));
        List list = RetrievalToolkit.xpathQuery(document.getRootElement(), "//VisualDescriptor", null);
        for (int j = 0; j < list.size(); j++) {
            Element elem = (Element) list.get(j);
            List atts = elem.getAttributes();
            for (Iterator it2 = atts.iterator(); it2.hasNext();) {
                Attribute att = (Attribute) it2.next();
                if (att.getValue().equals("ScalableColorType")) {
                    ScalableColor sc = new ScalableColor(elem);
                    if (sc.getNumberOfCoefficients() == 256) {
                    } else {
                        // load from image file ...
                        log.finest("Fallback from description file to image file for ScalableColor");
                        BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                        sc = new ScalableColor(img);
                    }
                    String sql = gen.getInsertStatement(imageFilePath, sc);
                    statement.executeUpdate(sql);
                } else if (att.getValue().equals("ColorLayoutType")) {
                    ColorLayout cl = new ColorLayout(elem);
                    String sql = gen.getInsertStatement(imageFilePath, cl);
                    statement.executeUpdate(sql);
                } else if (att.getValue().equals("DominantColorType")) {
                    // nothing to do yet ...
                } else if (att.getValue().equals("EdgeHistogramType")) {
                    EdgeHistogram eh = new EdgeHistogram(elem);
                    String sql = gen.getInsertStatement(imageFilePath, eh);
                    statement.executeUpdate(sql);
                }
            }
        }
    }
}
