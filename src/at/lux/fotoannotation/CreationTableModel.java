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

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

public class CreationTableModel extends AbstractTableModel {
    Vector keys, values;
    String[] defaultKeys = {"Time", "File format", "File size",
                            "Image x-width", "Image y-width", "Bits per pixel"};

    public CreationTableModel() {
        keys = new Vector();
        values = new Vector();
        for (int i = 0; i < defaultKeys.length; i++) {
            keys.add(defaultKeys[i]);
            values.add("");
        }
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return keys.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return keys.get(rowIndex).toString();
        else
            return values.get(rowIndex).toString();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 1);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1)
            values.setElementAt(aValue, rowIndex);
    }

    public String getColumnName(int column) {
        /*if (column == 0)
            return "tag";
        else
            return "value";*/
        return "";
    }

    public void addKeyValuePair(String key, String value) {
        keys.add(key);
        values.add(value);
    }

    public Vector getKeys() {
        return keys;
    }

    public Vector getValues() {
        return values;
    }

    public void resetValues() {
        keys.removeAllElements();
        values.removeAllElements();
        for (int i = 0; i < defaultKeys.length; i++) {
            keys.add(defaultKeys[i]);
            values.add("");
        }
        fireTableDataChanged();
    }
}
