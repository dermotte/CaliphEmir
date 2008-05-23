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
package at.lux.fotoannotation.panels;

import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.FileTableModel;
import at.lux.fotoannotation.utils.ImageFileFilter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FilePanel extends JPanel {
    private File directory;
    private JTable files;
    private FileTableModel ftm;
    private AnnotationFrame parent;

    public FilePanel(File directory, AnnotationFrame parent) {
        super(new BorderLayout());
        this.parent = parent;
        this.directory = directory;
        ftm = new FileTableModel(directory);
        files = new JTable(ftm);
        files.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            /**
             * Returns the default table cell renderer.
             *
             * @param table      the <code>JTable</code>
             * @param value      the value to assign to the cell at
             *                   <code>[row, column]</code>
             * @param isSelected true if cell is selected
             * @param row        the row of the cell to render
             * @param column     the column of the cell to render
             * @return the default table cell renderer
             */
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value.toString().startsWith("[")) {  //  Render roots bold
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else if (value.toString().toLowerCase().endsWith(".jpg")) {   // Render image files italic
                    label.setFont(label.getFont().deriveFont(Font.ITALIC));
                } else
                    label.setFont(label.getFont().deriveFont(Font.BOLD)); // Redner directories bold!
                return label;
            }
        });
        ListSelectionModel lsm = files.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm1 =
                        (ListSelectionModel) e.getSource();
                if (lsm1.isSelectionEmpty()) {
                    //no rows are selected
                } else {
                    int selectedRow = lsm1.getMinSelectionIndex();
                    debug("Selection changed: " + selectedRow);
                    selectNewFile(selectedRow);
                }
            }
        });
        this.add(new JScrollPane(files), BorderLayout.CENTER);
    }

    private void selectNewFile(int row) {
        String fname = files.getValueAt(row, 0).toString();
        File toOpen = null;
        if (fname.equals("..")) { // eines hinaufgehen ...
            try {
                directory = directory.getCanonicalFile().getParentFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ftm.setNewDirectory(directory);
                parent.setStatus("Changed to directory " + directory.toString());
                ftm.fireTableDataChanged();
            } catch (Exception e) {
                parent.setStatus("Error " + e.toString());
                debug("Error " + e.toString());
                // e.printStackTrace();
            }
        } else if (fname.indexOf('[') != 0) { // normales directory oder img-file
            File[] farray = directory.listFiles(new ImageFileFilter());
            for (int i = 0; i < farray.length; i++) {
                File file = farray[i];
                if (file.getName().equals(fname))
                    toOpen = file;
            }
        } else { // root (Laufwerk oder so)
            toOpen = new File(fname.substring(1, fname.length() - 1));
            parent.setStatus("Change to root " + toOpen.toString());
            // setting current directory:
            directory = toOpen;
            ftm.setNewDirectory(toOpen);
            ftm.fireTableDataChanged();
            toOpen = null;
        }
        if (toOpen != null) {
            if (toOpen.isDirectory()) {
                // setting current directory:
                directory = toOpen;
                ftm.setNewDirectory(directory);
                ftm.fireTableDataChanged();
                parent.setStatus("Changed to directory " + toOpen.toString());
            } else {
                try {
                    parent.setCurrentFile(toOpen);
                    debug("opening image file " + toOpen.toString());
                } catch (IOException e) {
                    debug("An Error occured: " + e.toString());
                    // e.printStackTrace();
                }
            }
        } else {
            debug("Selected file not opened!");
        }
    }

    private void debug(String message) {
        if (AnnotationFrame.DEBUG) System.out.println("[at.lux.fotoannotation.panels.FilePanel] " + message);
    }
}
