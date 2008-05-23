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

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 22.10.2002
 *         Time: 15:46:09
 */
public class DNDJTable extends JTable implements DragSourceListener, DragGestureListener {
    DragSource dragSource = new DragSource();

    public DNDJTable(TableModel dm) {
        super(dm);
        init();
    }

    public void init() {
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragGestureRecognized(DragGestureEvent event) {
        Object selected = getModel().getValueAt(getSelectedRow(), 0);
        if (selected != null) {
            StringSelection text = new StringSelection(selected.toString());
            dragSource.startDrag(event, DragSource.DefaultMoveDrop, text, this);
        } else {
            System.out.println("nothing was selected");
        }
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
    }
}
