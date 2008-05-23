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

/*
 * Date: 26.07.2002
 * Time: 10:15:50
 */


package at.knowcenter.caliph.objectcatalog.gui;

import org.jdom.Attribute;
import org.jdom.Element;

import javax.swing.table.AbstractTableModel;

public class AttributeTableModel extends AbstractTableModel {
    Element node;

    public AttributeTableModel(Element e) {
        node = e;
    }

    public AttributeTableModel() {
        node = null;
    }

    public void setRoot(Element e) {
        node = e;
    }

    public int getRowCount() {
        if (node != null) {
            return node.getAttributes().size();
        } else
            return 2;
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (node != null) {
            if (columnIndex == 0) {
                return ((Attribute) node.getAttributes().get(rowIndex)).getName();
            } else {
                return ((Attribute) node.getAttributes().get(rowIndex)).getValue();
            }
        } else {
            return null;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return false;
        else
            return true;
    }

    public String getColumnName(int column) {
        if (column == 0)
            return "Name";
        else
            return "Value";
    }
}
