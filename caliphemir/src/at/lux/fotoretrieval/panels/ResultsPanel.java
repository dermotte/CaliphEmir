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
package at.lux.fotoretrieval.panels;

import at.lux.fotoretrieval.ResultListEntry;
import at.lux.fotoretrieval.RetrievalToolkit;
import at.lux.fotoannotation.AnnotationFrame;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * ResultsPanel
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ResultsPanel extends JPanel {
    private static int max = 20;
    private List<ResultListEntry> results;
    private JProgressBar progress;
    private boolean show_thumbs = true;
    private QualityConstraintPanel qualityConstraints = null;
    private static DecimalFormat df;

    static {
        df = ((DecimalFormat) DecimalFormat.getInstance());
        df.setMaximumFractionDigits(2);
    }
//    private StreamSource streamSource = null;

    public ResultsPanel(List<ResultListEntry> results, JProgressBar progress) {
        this.results = results;
        this.progress = progress;
        init();
    }

    public ResultsPanel(List<ResultListEntry> results, JProgressBar progress, QualityConstraintPanel qualityConstraints) {
        this.results = results;
        this.progress = progress;
        this.qualityConstraints = qualityConstraints;
        init();
    }

    public ResultsPanel(Vector results) {
        this.results = results;
        this.progress = null;
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        JPanel thumbs = new JPanel(new GridLayout(0, 1));
        JPanel descriptions = new JPanel(new GridLayout(0, 1));
        JPanel resultsPanel = new JPanel(new BorderLayout());
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(2);
//        df.setMaximumIntegerDigits(3);

        if (progress != null) {
            progress.setMinimum(0);
            progress.setMaximum(Math.min(max, results.size()));
            progress.setValue(0);
            progress.setString("Formatting results");
        }

        int count = 0;
        File file;
        String strDesc;
        ResultThumbnailPanel thumb = null;
        for (Iterator i = results.iterator(); i.hasNext();) {
            ResultListEntry entry = (ResultListEntry) i.next();
            if (entry.getFilePath() != null) try {
//                long time = System.currentTimeMillis();

//                file = new File();
                thumb = null;
                int qualityRating = 1;
                if (show_thumbs) {
                    if ((entry.getThumbPath() == null)) {
                        thumb = new ResultThumbnailPanel(entry.getFilePath());
                    } else if (entry.getThumbPath().startsWith("file:")) {
                        boolean isValid = true;
                        URL thumbURL = new URI(entry.getThumbPath()).toURL();
                        try {
                            thumbURL.openStream();
                        } catch (IOException ioex) {
                            // so the url stored in the MPEG-7 file is false
                            isValid = false;
//                                System.err.println("Did not find thumbnail: " + thumbURL.toString());
                        }
                        if (isValid == false) {
                            // now we try and see if the file is in the directory of the description:
                            String path = entry.getDescriptionPath().substring(0, entry.getDescriptionPath().lastIndexOf('\\'));
                            String thumbnailFile = entry.getThumbPath().substring(entry.getThumbPath().lastIndexOf('/') + 1);
                            String x = path + "\\" + thumbnailFile;
//                                System.out.println(x);
                            File thumbTwemp = new File(x);
                            if (thumbTwemp.exists()) {
                                // yes it's here ...
                                thumbURL = thumbTwemp.toURL();
                                isValid = true;
                            }
                        }
                        try {
                            if (isValid) {
                                thumb = new ResultThumbnailPanel(ImageIO.read(thumbURL), thumbURL.toString().replaceAll("tn_", ""));
                            } else {
                                File f = new File(entry.getDescriptionPath());
//                                    debug(f.getParent() + thumbURL.getFile().substring(thumbURL.getFile().lastIndexOf('/')));
                                String thumbPath = f.getParent() + thumbURL.getFile().substring(thumbURL.getFile().lastIndexOf('/'));
                                File inputFile = new File(thumbPath);
                                thumb = new ResultThumbnailPanel(ImageIO.read(inputFile), thumbPath.replaceAll("tn_", ""));
                            }
                        } catch (IOException e) {
                            thumb = new ResultThumbnailPanel(entry.getFilePath());
                        }
                    } else {
                        thumb = new ResultThumbnailPanel(ImageIO.read(new FileInputStream(entry.getThumbPath())), entry.getFilePath());
                    }
                    thumb.setToolTipText("Right mouse button opens entry in Caliph / Editor.");
                }
                /*
                // old method ...
                String doc = "<html><i>Relevance: </i>" + df.format(entry.getRelevance()) + "<br>";
                doc += "<i>Image: </i>" + entry.getImageFilePath() + ", "  + entry.getImageSize() +  "<br>";
                doc += "<i>Creator: </i>" + entry.getCreatorName() + "<br>";
                doc += "<i>Creation time: </i>" + entry.getCreationTime() + "<br>";
                doc += "<i>Description: </i>" + entry.getFreeTextDescription() + "<br>";
                doc += "<i>Semantics: </i>" + entry.getSemanticDescriptionString() + "<br>";
                doc += "</html>";
                */
                JPanel descPanel = new JPanel(new BorderLayout());
                GridLayout layout = new GridLayout(0, 1);
                layout.setHgap(1);
                layout.setVgap(1);
                JPanel namesPanel = new JPanel(layout);
                JPanel valuesPanel = new JPanel(layout);

//                    Font plainFont = this.getFont().deriveFont(Font.PLAIN);

                namesPanel.add(createBoldLabel("Relevance: "));
                namesPanel.add(createBoldLabel("Image: "));
                namesPanel.add(createBoldLabel("Creator: "));
                namesPanel.add(createBoldLabel("Time: "));
                namesPanel.add(createBoldLabel("Description: "));
//                    namesPanel.add(new JLabel("Semantics: "));

                valuesPanel.add(new JLabel(df.format(entry.getRelevance())));
                valuesPanel.add(new JLabel(entry.getImageFilePath() + ", " + entry.getImageSize()));
                valuesPanel.add(new JLabel(entry.getCreatorName()));
                valuesPanel.add(new JLabel(entry.getCreationTime()));
                valuesPanel.add(new JLabel(entry.getFreeTextDescription()));
//                    valuesPanel.add(new JLabel(entry.getSemanticDescriptionString()));

//                    Component[] labels = valuesPanel.getComponents();
//                    for (int j = 0; j < labels.length; j++) {
//                        Component label = labels[j];
//                        if (label instanceof JLabel) {
//                            label.setFont(plainFont);
//                        }
//                    }

                descPanel.add(namesPanel, BorderLayout.WEST);
                descPanel.add(valuesPanel, BorderLayout.CENTER);

                descPanel.addMouseListener(new MyMouseAdapter(entry));

//                    String doc = entry.getHTMLSummary();
                // String fpath = entry.getFilePath();
//                    JLabel desc;
//                    debug(doc);
//                    if (doc != null) {
//                        desc = new JLabel(doc);
//                    } else {
//                        desc = new JLabel("No description available!");
//                    }
                JPanel rowPanel = new JPanel(new BorderLayout());
                if (thumb != null) rowPanel.add(thumb, BorderLayout.WEST);
                rowPanel.add(descPanel, BorderLayout.CENTER);
                rowPanel.add(new JSeparator(), BorderLayout.SOUTH);
                JPanel tmpPanel = new JPanel(new BorderLayout());
                boolean add = false;
//                    if (entry!=null) debug("entry is not NULL");
//                    if (qualityConstraints!=null) debug("qualityConstraints is not NULL");
//                    debug("Quality: " + entry.getQuality() + ", Mode: " + qualityConstraints.getMode());
                if (qualityConstraints != null) {
                    if ((entry.getQuality() < 0 && qualityConstraints.getMode() != 2)) {
                        // Die Beschreibung enthält keine Qualitätsbewertung, daher wird das File immer angezeigt,
                        // ausser man sucht nach einer bestimmten Qualität (genau)
                        add = true;
                    } else {
                        if (qualityConstraints.getMode() == 0) {
                            // eine untere Grenze wurde angegeben ...
                            if (entry.getQuality() >= qualityConstraints.getQuality()) {
                                add = true;
                            }
                        } else if (qualityConstraints.getMode() == 1) {
                            // eine obere Grenze wurd angegeben
                            if (entry.getQuality() <= qualityConstraints.getQuality()) {
                                add = true;
                            }
                        } else if (qualityConstraints.getMode() == 2) {
                            // eine bestimmte qualitätsstufe wird gesucht ...
                            if (entry.getQuality() == qualityConstraints.getQuality()) {
                                add = true;
                            }
                        }

                    }
                } else {
                    add = true;
                }
                if (add) {
                    tmpPanel.add(rowPanel, BorderLayout.NORTH);
                    descriptions.add(tmpPanel);
                }
                if (progress != null) {
                    progress.setValue(count++);
                }
//                System.out.println("One row: " + (System.currentTimeMillis() - time) + " ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (count > max) break;
        }
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        resultsPanel.add(thumbs, BorderLayout.WEST);
        resultsPanel.add(descriptions, BorderLayout.CENTER);
        this.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);
    }

    public Image getImage(String FileName) {
        // Load image
        Image loadedImage = Toolkit.getDefaultToolkit().getImage(FileName);
        // Wait until loaded
        try {
            MediaTracker mediaTracker = new MediaTracker(this);
            mediaTracker.addImage(loadedImage, 0);
            mediaTracker.waitForID(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//         Return loaded image
        return loadedImage;
    }

    private Document transform(Document in) throws JDOMException {

        Document d = null;
        try {
            Transformer transformer = TransformerFactory.newInstance().
                    newTransformer(new StreamSource(RetrievalToolkit.class.getResource("data/html-short.xsl").openStream()));
            JDOMResult out = new JDOMResult();
            transformer.transform(new JDOMSource(in), out);
            d = out.getDocument();
        } catch (TransformerException e) {
            debug("Transformation failed, TransformerException: " + e.getMessageAndLocation());
        } catch (TransformerFactoryConfigurationError error) {
            debug("Transformation failed, " + error.toString());
        } catch (IOException e) {
            debug("Transformation failed, IOException: " + e.getMessage());
        }
        return d;
    }

    private void debug(String message) {
        System.out.println("[at.lux.fotoretrieval.panels.ResultsPanel] " + message);
    }

    private JLabel createBoldLabel(String text) {
        JLabel result = new JLabel();
        result.setFont(result.getFont().deriveFont(Font.BOLD));
        result.setText(text);
        return result;
    }

    /**
     * Returns an array containing all result file descriptions
     *
     * @return array containing all result file descriptions sorted by relevance.
     */
    public String[] getResultFiles() {
        ArrayList<String> result = new ArrayList<String>(results.size());
        for (Iterator<ResultListEntry> iterator = results.iterator(); iterator.hasNext();) {
            ResultListEntry entry = iterator.next();
            result.add(entry.getDescriptionPath());
        }
        return (String[]) result.toArray(new String[0]);
    }

    public String getResultHtml() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />\n" +
                "<title>Emir Results</title>\n" +
                "<style type=\"text/css\">\n" +
                "<!--\n" +
                ".result {\n" +
                "\tborder-bottom-width: 1px;\n" +
                "\tborder-top-style: solid;\n" +
                "\tborder-right-style: solid;\n" +
                "\tborder-bottom-style: solid;\n" +
                "\tborder-left-style: solid;\n" +
                "\tborder-top-color: #999999;\n" +
                "\tborder-right-color: #999999;\n" +
                "\tborder-bottom-color: #999999;\n" +
                "\tborder-left-color: #999999;\n" +
                "\tborder-top-width: 0px;\n" +
                "\tborder-right-width: 0px;\n" +
                "\tborder-left-width: 0px;\n" +
                "\theight: 140px;\n" +
                "\tposition: relative;\n" +
                "\tleft: 140px;\n" +
                "}\n" +
                "body {\n" +
                "\tbackground-color: #FFFFFF;\n" +
                "\twidth: 640px;\n" +
                "\tfont-family: Georgia, Times, serif;\n" +
                "\tfont-size: 10pt;\n" +
                "}\n" +
                ".imgbox {\n" +
                "\tposition: relative;\n" +
                "\tleft: -140px;\n" +
                "\ttop: 0px;\n" +
                "\theight: 120px;\n" +
                "\twidth: 120px;\n" +
                "\tborder: 1px solid #CCCCCC;\n" +
                "\ttext-align: center;\n" +
                "\tvertical-align: middle;\n" +
                "\tmargin: 5px;\n" +
                "\tpadding: 5px;\n" +
                "}\n" +
                ".description {\n" +
                "\tposition: relative;\n" +
                "\ttop: -140px;\n" +
                "\tmargin-left: 5px;\n" +
                "}\n" +
                "-->\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n");
        for (ResultListEntry entry : results) {

            String descriptionPath = entry.getDescriptionPath();
            int endIndex = Math.max(descriptionPath.lastIndexOf("\\"), descriptionPath.lastIndexOf("/"));
            String directory = descriptionPath.substring(0, endIndex);

            String thumbnail = entry.getThumbPath().replaceAll("file:", "");
            thumbnail = thumbnail.substring(thumbnail.lastIndexOf("/"));
            thumbnail = directory + thumbnail;
            String imageFile = directory + "/" + entry.getImageFilePath();

            sb.append("<div class=\"result\">\n" +
                    "  <div class=\"imgbox\" calss=\"imgbox\">" +
                    "<a href=\"file://" + imageFile + "\">" +
                    "<img src=\"file://" + thumbnail + "\" alt=\"img1.jpg\" border=\"0\" align=\"middle\" /></a></div>\n" +
                    "  <div class=\"description\"> <strong>Score:</strong> "+entry.getRelevance()+" <br />\n" +
                    "    <strong>File:</strong> " + entry.getImageFilePath() + " [<a href=\"file://"+descriptionPath +"\">open MPEG-7 file</a>] <br />\n" +
                    "    <strong>Description:</strong> "+entry.getFreeTextDescription()+" </div>\n" +
                    "</div>\n");
        }
        sb.append("</body>\n" +
                "</html>");
        return sb.toString();
    }

    private static class MyMouseAdapter extends MouseAdapter {
        private final ResultListEntry entry;
        private static AnnotationFrame caliph;

        public MyMouseAdapter(ResultListEntry entry) {
            this.entry = entry;
        }

        public void mouseClicked(MouseEvent e) {
            System.out.println("e = " + e.getButton());
            if (e.getButton() == MouseEvent.BUTTON3) {
                if (caliph == null) {
                    caliph = new AnnotationFrame(false);
                }
                String descriptionPath = entry.getDescriptionPath();
                String filePath = entry.getFilePath();
                int descriptionPathEndIndex = Math.max(descriptionPath.lastIndexOf('\\'), descriptionPath.lastIndexOf('/'));
                String file = descriptionPath.substring(0, descriptionPathEndIndex) + filePath.substring(filePath.lastIndexOf('/'));
                File description = new File(file);
                if (!description.exists())
                    JOptionPane.showMessageDialog(null, "Could not find file " + entry.getFilePath(), "Error loading description", JOptionPane.ERROR_MESSAGE);
                try {
                    caliph.setCurrentFile(description);
                    caliph.setVisible(true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
