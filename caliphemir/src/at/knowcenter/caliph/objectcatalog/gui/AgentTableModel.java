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
 * Date: 31.07.2002
 * Time: 11:05:59
 */
package at.knowcenter.caliph.objectcatalog.gui;

import org.jdom.Element;

import javax.swing.table.AbstractTableModel;
import java.util.Iterator;
import java.util.Vector;

/**
 * Holds a list of JDOM nodes which should represent MPEG-7 SemanticAgentObjects
 */
public class AgentTableModel extends AbstractTableModel {
    private Vector agents;
//    Logger logger = Logger.getLogger(AgentTableModel.class);

    public Vector getAgents() {
        return agents;
    }

    /**
     * New TableModel with empty Vector
     */
    public AgentTableModel() {
        agents = new Vector();
    }

    public AgentTableModel(Element node) {
        agents = new Vector();
        agents.add(node);
    }

    public AgentTableModel(Vector agents) {
        this.agents = agents;
    }

    public String getColumnName(int column) {
        return null;
    }

    public int getColumnCount() {
        return 1;
    }

    public int getRowCount() {
        return agents.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            String s = new String();
            Element e = (Element) agents.get(rowIndex);
            try {
                s = e.getChild("Label", e.getNamespace()).getChildText("Name", e.getNamespace());
                if (!(s != null)) {
                    s = e.getChild("Label", e.getNamespace()).getChildText("Definition", e.getNamespace());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return s;
        } else
            return null;
    }

    public void addAgent(Element node) {
        boolean exists = false;
        for (Iterator i = agents.iterator(); i.hasNext();) {
            Element elem = (Element) i.next();
            String str1, str2;
            str1 = elem.getChild("Label", elem.getNamespace()).getChild("Name", elem.getNamespace()).getTextTrim();
            str2 = node.getChild("Label", node.getNamespace()).getChild("Name", node.getNamespace()).getTextTrim();
            if (str1.equals(str2))
                exists = true;
        }
        if (!exists) agents.add(node);
    }

    public void addAllAgents(Vector all) {
        agents.addAll(all);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void detachAll() {
        for (Iterator i = agents.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            e.detach();
        }
    }

    public Element getNodeAt(int row) {
        return (Element) agents.get(row);
    }

    public void sort() {
        Vector tmpAgents = new Vector();
        boolean hasElements = !agents.isEmpty();
        while (hasElements) {
            Element min = null;
            hasElements = false;
            for (Iterator i = agents.iterator(); i.hasNext();) {
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
                tmpAgents.add(min);
                agents.remove(min);
            }
        }
        agents = tmpAgents;
    }
}
