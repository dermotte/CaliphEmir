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

//import org.apache.log4j.Logger;

import org.jdom.Element;

import javax.swing.table.AbstractTableModel;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 01.08.2002
 *         Time: 12:35:35
 */
public class EventTableModel extends AbstractTableModel {
    private Vector events;
//    Logger logger = Logger.getLogger(EventTableModel.class);

    /**
     * New TableModel with empty Vector
     */
    public EventTableModel() {
        events = new Vector();
    }

    public EventTableModel(Element node) {
        events = new Vector();
        events.add(node);
    }

    public EventTableModel(Vector agents) {
        this.events = agents;
    }

    public Vector getEvents() {
        return events;
    }

    public String getColumnName(int column) {
        return null;
    }

    public int getColumnCount() {
        return 1;
    }

    public int getRowCount() {
        return events.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            String s = new String();
            Element e = (Element) events.get(rowIndex);
            try {
                s = e.getChild("Label", e.getNamespace()).getChildText("Name", e.getNamespace());
                if (!(s != null)) {
                    s = e.getChild("Label", e.getNamespace()).getChildText("Definition", e.getNamespace());
                }
                // logger.debug("Requested Element @ (" + rowIndex + "," + columnIndex + "): " + s);
            } catch (Exception e1) {
//                logger.error(e + ": " + e1);
                e1.printStackTrace();
            }
            return s;
        } else
            return null;
    }

    public Element getNodeAt(int row) {
        return (Element) (events.get(row));
    }

    public void addEvent(Element node) {
        boolean exists = false;
        for (Iterator i = events.iterator(); i.hasNext();) {
            Element elem = (Element) i.next();
            String str1, str2;
            str1 = elem.getChild("Label", elem.getNamespace()).getChild("Name", elem.getNamespace()).getTextTrim();
            str2 = node.getChild("Label", node.getNamespace()).getChild("Name", node.getNamespace()).getTextTrim();
            if (str1.equals(str2))
                exists = true;
        }
        if (!exists) events.add(node);

    }

    public void addAllEvents(Vector all) {
        events.addAll(all);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void detachAll() {
        for (Iterator i = events.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            e.detach();
        }
    }

    public void sort() {
        Vector tmpEvents = new Vector();
        boolean hasElements = !events.isEmpty();
        while (hasElements) {
            Element min = null;
            hasElements = false;
            for (Iterator i = events.iterator(); i.hasNext();) {
                hasElements = true;
                Element e = (Element) i.next();
                if (!(min != null))
                    min = e;
                else {
                    String s1 = e.getChild("Label", e.getNamespace()).getChildText("Name", e.getNamespace());
                    String minStr = min.getChild("Label", min.getNamespace()).getChildText("Name", min.getNamespace());
                    if (s1.compareTo(minStr) < 0)
                        min = e;
                }
            }
            if (min != null) {
                tmpEvents.add(min);
                events.remove(min);
            }
        }
        events = tmpEvents;
    }

}
