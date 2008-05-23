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
import java.util.Arrays;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 31.07.2002
 *         Time: 10:38:23
 */
public class RelationTableModel extends AbstractTableModel {
    private String[] relations;

    public RelationTableModel(String[] relations) {
        this.relations = relations;
        Arrays.sort(relations);
    }

    public String[] getRelations() {
        return relations;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public int getColumnCount() {
        return 1;
    }

    public int getRowCount() {
        return relations.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object returnVal = null;
        if (columnIndex < 1) {
            returnVal = relations[rowIndex];
        }
        return returnVal;
    }

    public String getColumnName(int column) {
        return null;
    }
}
