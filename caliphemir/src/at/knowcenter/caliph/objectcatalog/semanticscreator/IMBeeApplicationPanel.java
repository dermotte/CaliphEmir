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
package at.knowcenter.caliph.objectcatalog.semanticscreator;

import at.knowcenter.caliph.objectcatalog.OCToolkit;
import at.knowcenter.caliph.objectcatalog.gui.AgentTableModel;
import at.knowcenter.caliph.objectcatalog.gui.DNDJTable;
import at.knowcenter.caliph.objectcatalog.gui.EventTableModel;
import at.knowcenter.caliph.objectcatalog.gui.SemanticObjectTableModel;
import at.knowcenter.caliph.objectcatalog.mpeg7tools.Mpeg7FileFilter;
import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.IconCache;
import at.lux.fotoannotation.panels.ComponentFactory;
import at.lux.fotoannotation.dialogs.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>Teil des Projekts <b>IMB - Retrievaltools</b> vom Know-Center Graz in Kooperation mit dem Joanneum Research</p>
 * Applikation zum Zusammenstellen von semantischen Beschreibung auf Basis von MPEG-7 Deskriptoren
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class IMBeeApplicationPanel extends JPanel implements ActionListener, BeeDataExchange {
    public static String CONFIGURATION_FILE = "../data/imbee.conf.xml";
    public static String BASE_OBJECT_FILE = "base-objects.mp7.xml";
    private SAXBuilder xmlBuilder;
    // private JToolBar drawingtoolbar;
    private BeePanel beePanel;
    // private JPanel buttonPane;
    protected JTable agentTable, eventTable, venueTable;
    private JSplitPane rlSplitPane, tbSplitPane;
    // private JPopupMenu venueMenu;
    EventTableModel eventTableModel;
    AgentTableModel agentTableModel;
    SemanticObjectTableModel venueTableModel;
    JFrame parent;
    private String[] relationsArray;
    private JSplitPane agentEventSplit;
    private final Border emptyBorder = BorderFactory.createEmptyBorder(0, 3, 0, 3);


    public IMBeeApplicationPanel(JFrame parent) {
        super(new BorderLayout());
        this.parent = parent;
        xmlBuilder = new SAXBuilder();
        this.addComponentListener(new ComponentAdapter() {
            /**
             * Invoked when the component's size changes.
             */
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                reArrange();
            }
        });

        // --------------------------------------
        // reading relations from file ...
        // --------------------------------------
        relationsArray = new String[1];
        relationsArray[0] = "no relation found";
        try {
            Document relDoc = xmlBuilder.build(OCToolkit.getRelationsFile());
            java.util.List relList = relDoc.getRootElement().getChildren();
            Vector<String> tmpRelationsVector = new Vector<String>();
            for (Object aRelList : relList) {
                Element elem = (Element) aRelList;
                String tmpRelationName = elem.getChildText("name");
                String tmpInverseRelationName = elem.getChildText("inverse");
                if (tmpRelationName != null)
                    tmpRelationsVector.add(tmpRelationName);
                if (tmpInverseRelationName != null)
                    tmpRelationsVector.add(tmpInverseRelationName);
            }
            relationsArray = new String[tmpRelationsVector.size()];
            tmpRelationsVector.toArray(relationsArray);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Arrays.sort(relationsArray);

        // --------------------------------------
        // initialising tables ...
        // --------------------------------------
        agentTableModel = new AgentTableModel();
        eventTableModel = new EventTableModel();
        venueTableModel = new SemanticObjectTableModel();
        debug("[Startup IMBee] Reading base-objects");
        readBaseObjects();
        debug("[Startup IMBee] Finished reading base-objects");
        agentTableModel.detachAll();
        agentTableModel.sort();
        eventTableModel.detachAll();
        eventTableModel.sort();
        venueTableModel.detachAll();
        venueTableModel.sort();
        venueTable = new DNDJTable(venueTableModel);
        venueTable.setTableHeader(null);
        venueTable.setShowGrid(false);
        agentTable = new DNDJTable(agentTableModel);
        agentTable.setTableHeader(null);
        agentTable.setShowGrid(false);

        agentTable.addMouseListener(new MouseAdapter() {
            long lastMs = System.currentTimeMillis();
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (System.currentTimeMillis() - lastMs < 300) {
                        // edit agent ...
                        editAgent();
                    } else {
                        lastMs = System.currentTimeMillis();
                    }
                }
            }
        });

        eventTable = new DNDJTable(eventTableModel);
        eventTable.setTableHeader(null);
        eventTable.setShowGrid(false);

        // --------------------------------------
        // creating buttons for the tables ...
        // --------------------------------------
        JButton remAgentButton = new JButton();
        remAgentButton.addActionListener(this);
        remAgentButton.setActionCommand("removeAgent");
        remAgentButton.setToolTipText("Remove selected person objects.");
        remAgentButton.setIcon(IconCache.getInstance().getRemoveIcon());

        JButton newAgentButton = new JButton();
        newAgentButton.addActionListener(this);
        newAgentButton.setActionCommand("newAgent");
        newAgentButton.setToolTipText("Create a new person object.");
        newAgentButton.setIcon(IconCache.getInstance().getAddIcon());

        JPanel agentPane = new JPanel(new BorderLayout());
        agentPane.add(new JScrollPane(agentTable), BorderLayout.CENTER);
        JPanel abp = new JPanel();
        abp.add(remAgentButton);
        abp.add(newAgentButton);
        agentPane.add(abp, BorderLayout.SOUTH);

        JButton newEventButton = new JButton();
        newEventButton.addActionListener(this);
        newEventButton.setActionCommand("newEvent");
        newEventButton.setToolTipText("Create a new event object.");
        newEventButton.setIcon(IconCache.getInstance().getAddIcon());

        JButton remEventButton = new JButton();
        remEventButton.addActionListener(this);
        remEventButton.setIcon(IconCache.getInstance().getRemoveIcon());
        remEventButton.setActionCommand("removeEvent");
        remEventButton.setToolTipText("Remove selected event object.");

        JPanel ebp = new JPanel();
        ebp.add(remEventButton);
        ebp.add(newEventButton);

        // objects ...
        JButton newObjectButton = new JButton();
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand("newObject");
        newObjectButton.setToolTipText("Create a new semantic object / place / time.");
        newObjectButton.setIcon(IconCache.getInstance().getAddIcon());

        JButton remObjectButton = new JButton();
        remObjectButton.addActionListener(this);
        remObjectButton.setActionCommand("removeObject");
        remObjectButton.setToolTipText("Remove selected semantic object / place / time.");
        remObjectButton.setIcon(IconCache.getInstance().getRemoveIcon());

//        JButton importObjectsButton = new JButton("(i)");
//        importObjectsButton.addActionListener(this);
//        importObjectsButton.setActionCommand("importObjects");

        JPanel obp = new JPanel();
        obp.add(remObjectButton);
        obp.add(newObjectButton);
//        obp.add(importObjectsButton);

        JPanel eventPane = new JPanel(new BorderLayout());
        eventPane.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        eventPane.add(ebp, BorderLayout.SOUTH);

        JPanel venuePane = new JPanel(new BorderLayout());
        venuePane.add(new JScrollPane(venueTable), BorderLayout.CENTER);
        venuePane.add(obp, BorderLayout.SOUTH);

        agentEventSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        agentEventSplit.setContinuousLayout(true);
        agentEventSplit.setDividerSize(3);
        agentEventSplit.setDividerLocation(0.5);

        JPanel titledAgentPanel = ComponentFactory.createTitledPanel("Persons", agentPane);
        JPanel titledEventPanel = ComponentFactory.createTitledPanel("Events", eventPane);
        titledAgentPanel.setBorder(emptyBorder);
        titledEventPanel.setBorder(emptyBorder);
        agentEventSplit.add(titledAgentPanel, JSplitPane.TOP);
        agentEventSplit.add(titledEventPanel, JSplitPane.BOTTOM);

        // ---------------------------------------
        // Panels & SplitPanels ...
        // ---------------------------------------
        beePanel = new BeePanel(this);

        tbSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tbSplitPane.setDividerSize(3);
        tbSplitPane.setDividerLocation(0.75d);
        JPanel titledVenuePanel = ComponentFactory.createTitledPanel("Places, Times and Objects", venuePane);

        titledVenuePanel.setBorder(emptyBorder);
        tbSplitPane.add(agentEventSplit, JSplitPane.TOP);
        tbSplitPane.add(titledVenuePanel, JSplitPane.BOTTOM);
        rlSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);


        rlSplitPane.setDividerSize(3);
        
        rlSplitPane.setContinuousLayout(true);
        rlSplitPane.add(beePanel, JSplitPane.LEFT);
        rlSplitPane.add(tbSplitPane, JSplitPane.RIGHT);
        this.add(rlSplitPane, BorderLayout.CENTER);
        reArrange();
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("createObjectFromAgent")) {
            if (agentTable.getSelectedRow() > -1) {
                // String s = (String) agentTable.getValueAt(agentTable.getSelectedRow(), 0);
                Element elem = agentTableModel.getNodeAt(agentTable.getSelectedRow());
                beePanel.addObject(new Point(20 + (int) (Math.random() * 100), 20 + (int) (Math.random() * 100)), elem);
            }
        } else if (e.getActionCommand().equals("createObjectFromEvent")) {
            if (eventTable.getSelectedRow() > -1) {
                Element elem = eventTableModel.getNodeAt(eventTable.getSelectedRow());
                beePanel.addObject(new Point(20 + (int) (Math.random() * 100), 20 + (int) (Math.random() * 100)), elem);
            }
        } else if (e.getActionCommand().equals("export")) {
            exportXMLData(beePanel.createDocument());
        } else if (e.getActionCommand().equals("removeAgent")) {
            int[] indices = agentTable.getSelectedRows();
            Arrays.sort(indices);
            for (int i = 0; i < indices.length; i++) {
                int index = indices[indices.length - i - 1];   // von hinten loeschen, sonst stimmts nimmer
                agentTableModel.getAgents().remove(index);
            }
            agentTableModel.fireTableDataChanged();
        } else if (e.getActionCommand().equals("removeEvent")) {
            int[] indices = eventTable.getSelectedRows();
            Arrays.sort(indices);
            for (int i = 0; i < indices.length; i++) {
                int index = indices[indices.length - i - 1];   // von hinten loeschen, sonst stimmts nimmer
                eventTableModel.getEvents().remove(index);
            }
            eventTableModel.fireTableDataChanged();
        } else if (e.getActionCommand().equals("removeObject")) {
            int[] indices = venueTable.getSelectedRows();
            Arrays.sort(indices);
            for (int i = 0; i < indices.length; i++) {
                int index = indices[indices.length - i - 1];   // von hinten loeschen, sonst stimmts nimmer
                venueTableModel.getObjects().remove(index);
            }
            venueTableModel.fireTableDataChanged();
        } else if (e.getActionCommand().equals("newAgent")) {
            NewAgentDialog dialog = new NewAgentDialog(parent);
            dialog.pack();
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();

            dialog.setLocation((ss.width - dialog.getWidth()) >> 1, (ss.height - dialog.getHeight()) >> 1);
            dialog.setVisible(true);

            if (dialog.createXML() != null) {
                agentTableModel.addAgent(dialog.createXML());
                agentTableModel.sort();
                agentTableModel.fireTableDataChanged();
            }
        } else if (e.getActionCommand().equals("newEvent")) {
            NewEventDialog dialog = new NewEventDialog(parent);
            dialog.pack();
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation((ss.width - dialog.getWidth()) >> 1, (ss.height - dialog.getHeight()) >> 1);
            dialog.setVisible(true);

            if (dialog.createXML() != null) {
                eventTableModel.addEvent(dialog.createXML());
                eventTableModel.sort();
                eventTableModel.fireTableDataChanged();
            }
        } else if (e.getActionCommand().equals("importAgents")) {
            importAgents();
        } else if (e.getActionCommand().equals("importEvents")) {
            importEvents();
        } else if (e.getActionCommand().equals("importObjects")) {
            importObjects();
        } else if (e.getActionCommand().equals("newObject")) {
            String[] options = {"SemanticTime", "SemanticPlace", "Object"};
            JComboBox cbox = new JComboBox(options);
            int selection = JOptionPane.showConfirmDialog(this, cbox, "Select Type", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (selection == JOptionPane.OK_OPTION) {
                NewDescriptorDialogInterface descDialog = null;
                if (cbox.getSelectedIndex() == 0) {
                    descDialog = new NewTimeDialog(parent);
                } else if (cbox.getSelectedIndex() == 1) {
                    descDialog = new NewPlaceDialog(parent);
                } else if (cbox.getSelectedIndex() == 2) {
                    descDialog = new NewObjectDialog(parent);
                }
                JDialog dialog = ((JDialog) descDialog);
                dialog.pack();
                Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
                dialog.setLocation((ss.width - dialog.getWidth()) >> 1, (ss.height - dialog.getHeight()) >> 1);
                dialog.setVisible(true);

                if (descDialog.createXML() != null) {
                    venueTableModel.addObject(descDialog.createXML());
                    venueTableModel.sort();
                    venueTableModel.fireTableDataChanged();
                }
            }
        }

    }


    /**
     * Exportiert übergebenes <code>org.jdom.Document</code> in eine Datei nachdem die Datei über einen
     * <code>JFileChooser</code> ausgewählt wurde. Geschrieben wird im Encoding UTF-8.
     *
     * @param data Dokument, das in eine Datei geschrieben wird.
     */
    private void exportXMLData(Document data) {
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            String strData = outputter.outputString(data);
            JFileChooser jfc = new JFileChooser(".");
            int returnVal = jfc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                FileOutputStream fout = new FileOutputStream(jfc.getSelectedFile());
                OutputStreamWriter stream_out = new OutputStreamWriter(fout, "UTF-8");
                stream_out.write(strData);
                stream_out.flush();
                stream_out.close();
                debug("wrote mpeg-7 document in File: " + jfc.getSelectedFile());
            }
        } catch (IOException e) {
            debug("Error writing mpeg-7 document to file");
            e.printStackTrace();
        }
    }


    public Vector getAgents() {
        return agentTableModel.getAgents();
    }

    public Vector getEvents() {
        return eventTableModel.getEvents();
    }

    public Vector getVenues() {
        return venueTableModel.getObjects();
    }

    public Vector getPossibleObjects() {
        Vector ret = new Vector();
        ret.addAll(agentTableModel.getAgents());
        ret.addAll(eventTableModel.getEvents());
        ret.addAll(venueTableModel.getObjects());
        return ret;
    }

    public Document getSemanticsDocument() {
        return beePanel.createDocument();
    }

    public void importAgents() {
        File f = getFile(".", new Mpeg7FileFilter());
        if (f != null) {
            agentTableModel.addAllAgents(retrieveNodes(f, "AgentObjectType"));
            agentTableModel.addAllAgents(retrieveNodes(f, "fsw:FSWPlayerType"));
            agentTableModel.addAllAgents(retrieveNodes(f, "fsw:FSWCoachType"));
            agentTableModel.addAllAgents(retrieveNodes(f, "fsw:FSWRefereeType"));
            agentTableModel.detachAll();
            agentTableModel.sort();
            agentTableModel.fireTableDataChanged();
        }
    }

    public void importEvents() {
        File f = getFile(".", new Mpeg7FileFilter());
        if (f != null) {
            eventTableModel.addAllEvents(retrieveNodes(f, "EventType"));
            eventTableModel.detachAll();
            eventTableModel.sort();
            eventTableModel.fireTableDataChanged();
        }
    }

    public void importObjects() {
        File f = getFile(".", new Mpeg7FileFilter());
        if (f != null) {
            venueTableModel.addAllObjects(retrieveNodes(f, "SemanticPlaceType"));
            venueTableModel.addAllObjects(retrieveNodes(f, "ObjectType"));
            venueTableModel.addAllObjects(retrieveNodes(f, "SemanticTimeType"));
            venueTableModel.detachAll();
            venueTableModel.sort();
            venueTableModel.fireTableDataChanged();
        }
    }

    private Vector<Element> retrieveNodes(File f, String type) {
        Vector<Element> v = new Vector<Element>();
        SAXBuilder builder = new SAXBuilder();
        try {
            Document agents = builder.build(f);
            Namespace mpeg7 = agents.getRootElement().getNamespace();
            Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            java.util.List nodeList = agents.getRootElement().getChild("Description", mpeg7).getChild("Semantics", mpeg7).getChildren("SemanticBase", mpeg7);
            for (Iterator i = nodeList.iterator(); i.hasNext();) {
                Element e = (Element) i.next();
                if (e.getAttributeValue("type", xsi).equals(type))
                    v.add(e);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;
    }

    private File getFile(String directory, javax.swing.filechooser.FileFilter filter) {
        File myFile = null;
        JFileChooser jfc = new JFileChooser(directory);
        jfc.setFileFilter(filter);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            myFile = jfc.getSelectedFile();
        }
        return myFile;
    }

    public void addAgents(Vector v) {
        agentTableModel.addAllAgents(v);
        agentTableModel.sort();
        agentTableModel.fireTableDataChanged();
    }

    public void addEvents(Vector v) {
        eventTableModel.addAllEvents(v);
        eventTableModel.sort();
        eventTableModel.fireTableDataChanged();
    }

    private JPopupMenu generateVenueMenu() {
        JPopupMenu menu = new JPopupMenu("Venues");
        Namespace mpeg7 = null;
        Vector venues = venueTableModel.getObjects();
        for (Iterator i = venues.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            String label = e.getChild("Label", e.getNamespace()).getChildTextTrim("Name", e.getNamespace());
            JMenuItem item = new JMenuItem(label);
            item.setActionCommand("addVenue-" + label);
            item.addActionListener(beePanel);
            menu.add(item);
        }
        return menu;
    }

    private void readBaseObjects() {
        try {
            Document d = xmlBuilder.build(BASE_OBJECT_FILE);

            if (d != null) {
                Element e = d.getRootElement();
                Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                Namespace mpeg7 = e.getNamespace();
                Namespace fsw = OCToolkit.getFSWNamespace();

                java.util.List latt = e.getAttributes();
                for (Iterator it = latt.iterator(); it.hasNext();) {
                    Attribute attribute = (Attribute) it.next();
                    if (attribute.getNamespacePrefix().equals("xsi"))
                        xsi = attribute.getNamespace();
                }
                if (!(e != null)) {
                    debug("Error loading agents from file: root element is NULL");
                }
                e = e.getChild("Description", mpeg7);
                e = e.getChild("Semantics", mpeg7);

                java.util.List l = e.getChildren();
                for (Iterator it = l.iterator(); it.hasNext();) {
                    Element tmpElement = (Element) it.next();
                    if (tmpElement.getName().equals("SemanticBase") && tmpElement.getAttributeValue("type", xsi) != null)
                    {
                        if (tmpElement.getAttributeValue("type", xsi).equals("AgentObjectType") || tmpElement.getAttributeValue("type", xsi).equals("fsw:FSWRefereeType"))
                        {
                            agentTableModel.addAgent(tmpElement);
                        } else
                        if (tmpElement.getAttributeValue("type", xsi).equals("SemanticPlaceType") || tmpElement.getAttributeValue("type", xsi).equals("ObjectType") || tmpElement.getAttributeValue("type", xsi).equals("SemanticTimeType"))
                        {
                            venueTableModel.addObject(tmpElement);
                        } else if (tmpElement.getAttributeValue("type", xsi).equals("EventType")) {
                            eventTableModel.addEvent(tmpElement);
                        }
                    } else {
//                        logger.debug(tmpElement + " has no matching attribute!");
                    }
                }
            } else {
                debug("Error loading agents from file: File not found");
            }
        } catch (Exception e1) {
            debug("Error loading agents from file: " + e1);
            e1.printStackTrace();
        }

    }

    public void saveCatalog() {
        try {
            Document d = xmlBuilder.build(BASE_OBJECT_FILE);

            Element semantics = d.getRootElement();
            Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            Namespace mpeg7 = semantics.getNamespace();

            semantics = semantics.getChild("Description", mpeg7).getChild("Semantics", mpeg7);
            semantics.removeContent();
            semantics.addContent(new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent("Semanitscher Katalog")));

            for (Iterator iterator = agentTableModel.getAgents().iterator(); iterator.hasNext();) {
                Element elem = (Element) iterator.next();
                if (elem.getParent() != null) elem = (Element) elem.clone();
                semantics.addContent(elem.detach());
            }
            for (Iterator iterator = eventTableModel.getEvents().iterator(); iterator.hasNext();) {
                Element elem = (Element) iterator.next();
                if (elem.getParent() != null) elem = (Element) elem.clone();
                semantics.addContent(elem.detach());
            }
            for (Iterator iterator = venueTableModel.getObjects().iterator(); iterator.hasNext();) {
                Element elem = (Element) iterator.next();
                if (elem.getParent() != null) elem = (Element) elem.clone();
                semantics.addContent(elem.detach());
            }

            FileOutputStream fos = new FileOutputStream(BASE_OBJECT_FILE);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
            new XMLOutputter(Format.getPrettyFormat()).output(d, osw);
            osw.close();
            fos.close();
        } catch (JDOMException e) {
            debug("JDOMException saving catalogue: " + e.getMessage());
        } catch (IOException e) {
            debug("IOException saving catalogue: " + e.getMessage());
        }
    }


    private void debug(String message) {
        if (AnnotationFrame.DEBUG)
            System.out.println("[IMBeeApplikationPanel] " + message);
    }

    public String[] getRelations() {
        return relationsArray;
    }

    public void setSemantics(Element node) {
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Namespace mpeg7 = node.getNamespace();
        java.util.List l = node.getChildren();
        for (Iterator it = l.iterator(); it.hasNext();) {
            Element tmpElement = (Element) it.next();
            if (tmpElement.getName().equals("SemanticBase") && tmpElement.getAttributeValue("type", xsi) != null) {
                if (tmpElement.getAttributeValue("type", xsi).equals("AgentObjectType") || tmpElement.getAttributeValue("type", xsi).equals("fsw:FSWRefereeType"))
                {
                    agentTableModel.addAgent(tmpElement);
                } else if (tmpElement.getAttributeValue("type", xsi).equals("SemanticPlaceType") ||
                        tmpElement.getAttributeValue("type", xsi).equals("ObjectType") ||
                        tmpElement.getAttributeValue("type", xsi).equals("SemanticTimeType")) {
                    venueTableModel.addObject(tmpElement);
                } else if (tmpElement.getAttributeValue("type", xsi).equals("EventType")) {
                    eventTableModel.addEvent(tmpElement);
                }
            }
        }
        beePanel.importNode(node);
        agentTableModel.detachAll();
        eventTableModel.detachAll();
        venueTableModel.detachAll();
        agentTableModel.sort();
        eventTableModel.sort();
        venueTableModel.sort();
        agentTableModel.fireTableDataChanged();
        eventTableModel.fireTableDataChanged();
        venueTableModel.fireTableDataChanged();
    }

    public void addVenues(Vector v) {
        venueTableModel.addAllObjects(v);
        venueTableModel.sort();
        venueTableModel.fireTableDataChanged();
    }

    public void reArrange() {
        rlSplitPane.setDividerLocation(0.75);
        tbSplitPane.setDividerLocation(0.7);
        agentEventSplit.setDividerLocation(0.6);
    }

    public String[] getSemanticAgentsNames() {
        return beePanel.getSemanticAgentsNames();
    }

    public String[] getSemanticEventsNames() {
        return beePanel.getSemanticEventsNames();
    }

    public String[] getSemanticPlacesNames() {
        return beePanel.getSemanticPlacesNames();
    }

    public String[] getSemanticTimesNames() {
        return beePanel.getSemanticTimesNames();

    }

    public JFrame getParentJFrame() {
        return parent;
    }

    private void editAgent() {
        // TODO: add code here to edit the agent curently selected.
    }

}
