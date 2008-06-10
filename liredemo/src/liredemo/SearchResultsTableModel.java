/*
 * SearchResultsTableModel.java
 *
 * Created on 20. Februar 2007, 12:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package liredemo;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.utils.ImageUtils;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifDirectory;

/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
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
 * (c) 2002-2007 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SearchResultsTableModel extends DefaultTableModel {
    DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
    ImageSearchHits hits = null;
    private ArrayList<ImageIcon> icons;

    /**
     * Creates a new instance of SearchResultsTableModel
     */
    public SearchResultsTableModel() {
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int col) {
        if (col == 0) {
            return "File";
        } else if (col == 1) {
            return "Preview";
        } else if (col == 2) {
            return "Preview";
        }
        return "";
    }

    public Class getColumnClass(int column) {
        if (column == 0) {
            return String.class;
        } else {
            return ImageIcon.class;
        }
    }

    public int getRowCount() {
        if (hits == null) return 0;
        return hits.length();
    }

    public Object getValueAt(int row, int col) {
        if (col == 0) {
            String text = hits.doc(row).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
            if (hits.doc(row).getField("FlickrURL") != null) {
                text = hits.doc(row).getField("FlickrTitle").stringValue() + " - " + hits.doc(row).getField("FlickrURL").stringValue();
            }
            return df.format(hits.score(row)) + ": " + text;
//        } else if (col == 1) {
//            return hits.doc(row).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
        } else if (col == 1) {
            return icons.get(row);
        }
        return null;
    }

    /**
     * @param hits
     * @param progress
     */
    public void setHits(ImageSearchHits hits, JProgressBar progress) {
        this.hits = hits;
        icons = new ArrayList<ImageIcon>(hits.length());
        progress.setString("Searching finished. Loading images for result list.");
        for (int i = 0; i < hits.length(); i++) {
            ImageIcon icon = null;
            try {
                BufferedImage img = null;
                String fileIdentifier = hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
                if (!fileIdentifier.startsWith("http:")) {
                    Metadata metadata = new ExifReader(new FileInputStream(fileIdentifier)).extract();
                    if (metadata.containsDirectory(ExifDirectory.class)) {
                        ExifDirectory exifDirectory = (ExifDirectory) metadata.getDirectory(ExifDirectory.class);
                        if (exifDirectory.containsThumbnail()) {
                            img = ImageIO.read(new ByteArrayInputStream(exifDirectory.getThumbnailData()));
                        }
                    }
                    if (img == null) {
                        img = ImageIO.read(new FileInputStream(fileIdentifier));
                    }
                } else {
                    img = ImageIO.read(new URL(fileIdentifier));
                }
                icon = new ImageIcon(ImageUtils.scaleImage(img, 128));
                progress.setValue((i * 100) / hits.length());
            } catch (Exception ex) {
                Logger.getLogger("global").log(Level.SEVERE, null, ex);
            }
            icons.add(icon);
        }
        progress.setValue(100);
        fireTableDataChanged();
    }

    /**
     * @return
     */
    public ImageSearchHits getHits() {
        return hits;
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
