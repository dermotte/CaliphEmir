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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at) and the Know-Center Graz
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */
package at.knowcenter.caliph.objectcatalog.gui;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 25.07.2002
 *         Time: 10:38:51
 */
public class FileTableModel extends AbstractTableModel {
    Vector files;

    public FileTableModel(Vector files) {
        this.files = files;
    }

    public int getRowCount() {
        if (files != null)
            return files.size();
        else
            return 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        return "Files to import";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        // if (rowIndex <= files.size())
        return files.elementAt(rowIndex);
        // else return null;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setFiles(Vector files) {
        this.files = files;
    }
}
