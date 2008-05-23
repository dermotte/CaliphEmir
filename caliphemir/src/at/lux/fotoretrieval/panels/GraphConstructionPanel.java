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
package at.lux.fotoretrieval.panels;

import at.knowcenter.caliph.objectcatalog.graphics.Arrow;
import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.IconCache;
import at.lux.fotoretrieval.EmirConfiguration;
import at.lux.fotoretrieval.RetrievalFrame;
import at.lux.fotoretrieval.lucene.Node;
import at.lux.fotoretrieval.retrievalengines.LucenePathIndexRetrievalEngine;
import at.lux.fotoretrieval.retrievalengines.LuceneRetrievalEngine;
import at.lux.fotoretrieval.retrievalengines.RetrievalEngineFactory;
import at.lux.graphviz.LabeledEdge;
import at.lux.graphviz.LabeledNode;
import at.lux.graphviz.SpringEmbedder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

/**
 * Date: 04.01.2005
 * Time: 17:32
 *
 * @author Mathias Lux, mathias@juggle.at
 */

public class GraphConstructionPanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    private LinkedList<LabeledNode> semanticObjects = new LinkedList<LabeledNode>();
    private LinkedList<LabeledEdge> semanticRelations = new LinkedList<LabeledEdge>();
    private HashMap<String, LabeledNode> label2Node = new HashMap<String, LabeledNode>();
    private SpringEmbedder embedder = new SpringEmbedder(semanticObjects, semanticRelations);
    private HashMap<Point2D.Double, LabeledNode> point2node = new HashMap<Point2D.Double, LabeledNode>();
    private HashMap<Arrow, LabeledEdge> shape2edge = new HashMap<Arrow, LabeledEdge>();

    private static Color BACKGROUND_COLOR = new Color(152, 181, 255);
    public static final Color ARROW_COLOR = new Color(36, 74, 200);
    public static final Color NODE_COLOR = Color.green;

    private double OFFSET_X = EmirConfiguration.getInstance().getDouble("GraphConstructionPanel.EdgeOffset.x");
    private double OFFSET_Y = EmirConfiguration.getInstance().getDouble("GraphConstructionPanel.EdgeOffset.y");

    private String lastClickedLabel = null;
    private Point2D.Double lastClickedPoint = null;
    private Line2D.Double lastDraggedLine = null;
    private EmbedderThread embedderThread = null;

    private String[] relationArray;

    private LabeledNode currentNode;
    private JMenuItem helpMenuItem, removeNodeMenuItem;
    private HashMap<LabeledNode, Point.Double> nodeLocation;
    private boolean embedded = false;
    private LabeledNode currentMoveNode;
    private IconCache iconCache = IconCache.getInstance();


    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public GraphConstructionPanel() {
        init();
    }

    private void init() {
        addMouseListener(this);
        addMouseMotionListener(this);
        LinkedList<String> relations = new LinkedList<String>();
        relations.add("* any relation");
        relations.add("membershipFunction");
        for (String relation : LuceneRetrievalEngine.relationMapping.keySet()) {
            String inverse = LuceneRetrievalEngine.relationMapping.get(relation);
            relations.add(relation);
            relations.add(inverse);
        }
        Collections.sort(relations);
        relationArray = new String[1];
        relationArray = relations.toArray(relationArray);
        helpMenuItem = new JMenuItem("Help");
        helpMenuItem.addActionListener(this);
        helpMenuItem.setActionCommand("showHelp");
        helpMenuItem.setIcon(IconCache.getInstance().getHelpIcon());

        removeNodeMenuItem = new JMenuItem("Remove Node");
        removeNodeMenuItem.addActionListener(this);
        removeNodeMenuItem.setActionCommand("removeNode");
        removeNodeMenuItem.setIcon(new ImageIcon(AnnotationFrame.class.getResource("data/delete_obj.gif")));

        nodeLocation = new HashMap<LabeledNode, Point.Double>();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw background:
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.black);
        Font nFont = new Font("Verdana", Font.ITALIC, 9);
        g2.setFont(nFont);
        g2.setFont(nFont.deriveFont(Font.PLAIN));

        double maxWidth = getWidth() - 2d * OFFSET_X;
        double maxHeight = getHeight() - 2d * OFFSET_Y;

        double xMin = 1.0, xMax = 0.0, yMin = 1.0, yMax = 0.0;
        for (LabeledNode node : semanticObjects) {
            if (node.getX() < xMin) xMin = node.getX();
            if (node.getX() > xMax) xMax = node.getX();
            if (node.getY() < yMin) yMin = node.getY();
            if (node.getY() > yMax) yMax = node.getY();
        }

        if (!embedded) embedElements(xMin, xMax, maxWidth, yMin, yMax, maxHeight);

        shape2edge = new HashMap<Arrow, LabeledEdge>(semanticRelations.size());
        for (LabeledEdge edge : semanticRelations) {
            Point.Double src = nodeLocation.get(edge.getStartNode());
            Point.Double tgt = nodeLocation.get(edge.getEndNode());
            drawEdge(g2, edge, src, tgt);
            double moveX = tgt.x - src.x;
            double moveY = tgt.y - src.y;
            moveX *= .5;
            moveY *= .5;
            Point2D.Double lPoint = new Point2D.Double(src.x + moveX, src.y + moveY);
            drawLabel(g2, edge.getLabel(), lPoint, ARROW_COLOR);
        }
        for (LabeledNode labeledNode : nodeLocation.keySet()) {
            drawNode(g2, labeledNode, nodeLocation.get(labeledNode));
        }
        point2node = new HashMap<Point2D.Double, LabeledNode>(nodeLocation.keySet().size());
        for (LabeledNode node : nodeLocation.keySet()) {
            point2node.put(nodeLocation.get(node), node);
        }

        // draw dragged line:
        if (lastDraggedLine != null) {
            g2.setColor(Color.gray);
            g2.draw(lastDraggedLine);
        }
    }

    private void embedElements(double xMin, double xMax, double maxWidth, double yMin, double yMax, double maxHeight) {
        if (semanticObjects.size() == 1) {
            LabeledNode node = semanticObjects.getFirst();
            Point.Double point = new Point.Double(0, 0);
            point.x = (getWidth() >> 1);
            point.y = (getHeight() >> 1);
            nodeLocation.put(node, point);
        } else if (semanticObjects.size() == 2) {
            LabeledNode node = semanticObjects.get(0);
            Point.Double point = new Point.Double(0, 0);
            point.x = (OFFSET_X);
            point.y = (getHeight() / 2);
            nodeLocation.put(node, point);

            node = semanticObjects.get(1);
            point = new Point.Double(0, 0);
            point.x = (getWidth() - OFFSET_X);
            point.y = (getHeight() / 2);
            nodeLocation.put(node, point);
        } else if (semanticObjects.size() == 3) {
            LabeledNode node = semanticObjects.get(0);
            Point.Double point = new Point.Double(0, 0);
            point.x = (OFFSET_X);
            point.y = (OFFSET_Y);
            nodeLocation.put(node, point);

            node = semanticObjects.get(1);
            point = new Point.Double(0, 0);
            point.x = (getWidth() - OFFSET_X);
            point.y = (OFFSET_Y);
            nodeLocation.put(node, point);

            node = semanticObjects.get(2);
            point = new Point.Double(0, 0);
            point.x = (getWidth() / 2);
            point.y = (getHeight() - OFFSET_Y);
            nodeLocation.put(node, point);
        } else {
            for (LabeledNode node : semanticObjects) {
                double x = (((node.getX() - xMin) / (xMax - xMin)) * maxWidth + OFFSET_X);
                double y = (((node.getY() - yMin) / (yMax - yMin)) * maxHeight + OFFSET_Y);
                Point.Double point = new Point.Double(x, y);
                nodeLocation.put(node, point);
            }
        }

        if (embedderThread == null ||
                (embedderThread != null && !embedderThread.isEmbedderRunning())) {
            embedded = true;
        }
    }

    private void drawEdge(Graphics2D g2, LabeledEdge edge, Point2D.Double src, Point2D.Double tgt) {
        g2.setColor(ARROW_COLOR);
        double moveX = tgt.x - src.x;
        double moveY = tgt.y - src.y;
        double length = Math.sqrt(moveX * moveX + moveY * moveY);
        moveX *= 10 / length;
        moveY *= 10 / length;
        Point2D.Double target = new Point2D.Double(tgt.x - moveX, tgt.y - moveY);
        Line2D line = new Line2D.Double(src, target);
        Arrow arrow = new Arrow(line, 4d);
        shape2edge.put(arrow, edge);
        g2.fill(arrow);
        g2.setColor(Color.black);
    }

    private void drawNode(Graphics2D g2, LabeledNode node, Point.Double point) {
        int x = (int) (point.x - 10);
        int y = (int) (point.y - 10);
//        System.out.println(x + ", " + y);
        g2.setColor(NODE_COLOR);
        g2.fillOval(x, y, 20, 20);
        g2.setColor(ARROW_COLOR);
        g2.drawOval(x, y, 20, 20);
        Point2D.Double labelPoint;

        if ((getHeight() - point.y) > point.y)
            labelPoint = new Point2D.Double(point.x, point.y - 13);
        else
            labelPoint = new Point2D.Double(point.x, point.y + 13 + g2.getFontMetrics().getHeight());
        drawLabel(g2, node.getLabel(), labelPoint);
    }

    // draws the label inside an alpha blended box ...
    private static void drawLabel(Graphics2D g2, String label, Point.Double point) {
        drawLabel(g2, label, point, Color.white);
    }

    private static void drawLabel(Graphics2D g2, String label, Point.Double point, Color boxColor) {
        Composite comp = g2.getComposite();
        float alpha = 0.45f;
        int type = AlphaComposite.SRC_OVER;
        AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
        g2.setComposite(composite);

        g2.setColor(boxColor);
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(label, g2);
        int fillRectX = (int) point.x - (((int) bounds.getWidth()) >> 1) - 4;
        int fillRectY = (int) point.y - ((int) bounds.getHeight() + 1);
        int fillRectWidth = (int) bounds.getWidth() + 8;
        int fillRectHeight = (int) bounds.getHeight() + 2;
        RoundRectangle2D.Double labelBackGround = new RoundRectangle2D.Double(fillRectX, fillRectY, fillRectWidth, fillRectHeight, 12.0, 12.0);
        g2.fill(labelBackGround);
//        g2.fillRect(fillRectX, fillRectY, fillRectWidth, fillRectHeight);

        g2.setComposite(comp);
        g2.setColor(Color.black);
        int x = (int) point.x - ((int) bounds.getWidth() >> 1);
        g2.drawString(label, x, (int) point.y - 2);
    }

    public void addNode(String label) {
        if (!label2Node.keySet().contains(label)) {
            try {
                if (embedderThread != null) {
                    embedderThread.stopEmbedding();
                    embedderThread.join();
                }
                LabeledNode node = new LabeledNode(Math.random(), Math.random(), label);
                semanticObjects.add(node);
                label2Node.put(label, node);
                embedder = new SpringEmbedder(semanticObjects, semanticRelations);
                embedGraph();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeNode(String label) {
        LabeledNode node = label2Node.get(label);
        removeNode(node);
    }

    private void removeNode(LabeledNode node) {
        String label = node.getLabel();
        try {
            if (embedderThread != null) {
                embedderThread.stopEmbedding();
                embedderThread.join();
                embedderThread = null;
            }
            semanticObjects.remove(node);
            label2Node.remove(label);
            nodeLocation.remove(node);
            LinkedList<LabeledEdge> toRemove = new LinkedList<LabeledEdge>();
            for (LabeledEdge edge : semanticRelations) {
                if (edge.getEndNode().equals(node) || edge.getStartNode().equals(node)) {
                    toRemove.add(edge);
                }
            }
            for (LabeledEdge edge : toRemove) {
                removeEdge(edge);
            }
            embedGraph();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use to stop embedder, remove an edge and start embedder :)
     *
     * @param edge
     */
    public void removeRelation(LabeledEdge edge) {
        try {
            // stop the thread and join it until it dies :)
            // dirty but synchronized :)
            if (embedderThread != null) {
                embedderThread.stopEmbedding();
                embedderThread.join();
                embedderThread = null;
            }
            removeEdge(edge);
            embedder = new SpringEmbedder(semanticObjects, semanticRelations);
            embedGraph();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use when embedder is stoppeg, eg. from inside removeNodeMenuItem()
     *
     * @param edge
     */
    private void removeEdge(LabeledEdge edge) {
        semanticRelations.remove(edge);
//        embedder = new SpringEmbedder(semanticObjects, semanticRelations);
//        embedGraph();
    }

    public void addRelation(String label, LabeledNode src, LabeledNode tgt) {
        semanticRelations.add(new LabeledEdge(src, tgt, label));
        embedder = new SpringEmbedder(semanticObjects, semanticRelations);
        embedGraph();
    }

    public void addRelation(String label, String src, String tgt) {
        semanticRelations.add(new LabeledEdge(label2Node.get(src), label2Node.get(tgt), label));
        embedder = new SpringEmbedder(semanticObjects, semanticRelations);
        embedGraph();
    }

    public void embedGraph() {
        if (embedderThread != null) {
            embedderThread.stopEmbedding();
            try {
                embedderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        embedded = false;
        if (semanticObjects.size() > 3) {
            embedderThread = new EmbedderThread(embedder, this);
            embedderThread.start();
        } else {
            embedded = false;
            repaint();
        }
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON3 && e.isControlDown())) {
            double x = e.getPoint().x;
            double y = e.getPoint().y;
            // check for nodes ...
            for (Point2D.Double p : point2node.keySet()) {
                if (Math.abs(p.x - x) <= 10 && Math.abs(p.y - y) <= 10) {
                    removeNode(point2node.get(p).getLabel());
                }
            }
            // check for relations:
            LabeledEdge toRemove = null;
            for (Arrow arrow : shape2edge.keySet()) {
                if (arrow.contains(e.getPoint())) {
                    toRemove = shape2edge.get(arrow);
                }
            }
            if (toRemove != null) removeRelation(toRemove);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            double x = e.getPoint().x;
            double y = e.getPoint().y;
            // check for nodes ...
            JPopupMenu menu = new JPopupMenu();
            for (Point2D.Double p : point2node.keySet()) {
                if (Math.abs(p.x - x) <= 10 && Math.abs(p.y - y) <= 10) {
                    currentNode = point2node.get(p);
                    java.util.List<Node> nodes = getNodes(currentNode.getLabel());
                    for (Node node : nodes) {
                        JMenuItem menuItem = createMenuItemFromNode(node);
                        menu.add(menuItem);
                    }
                    if (nodes.size() > 0) menu.add(new JSeparator());
                    menu.add(removeNodeMenuItem);
                    menu.add(new JSeparator());
                }
            }
            menu.add(helpMenuItem);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private JMenuItem createMenuItemFromNode(Node node) {
        JMenuItem menuItem = new JMenuItem();
        StringBuilder labelBuilder = new StringBuilder(64).append(node.getLabel());
        if (node.getType() != null) {
            if (node.getType().startsWith("Agent")) {
                menuItem.setIcon(iconCache.getAgentIcon());
            } else if (node.getType().contains("Place")) {
                menuItem.setIcon(iconCache.getPlaceIcon());
            } else if (node.getType().contains("Event")) {
                menuItem.setIcon(iconCache.getEventIcon());
            } else if (node.getType().contains("Object")) {
                menuItem.setIcon(iconCache.getObjectIcon());
            } else if (node.getType().contains("Time")) {
                menuItem.setIcon(iconCache.getTimeIcon());
            } else {
                labelBuilder.append(" (").append(node.getType().replaceAll("Type", "")).append(')');
            }
        }
        menuItem.setText(labelBuilder.toString());
        StringBuilder builder = new StringBuilder(64);
        builder.append("label:\"").append(node.getLabel());
        builder.append("\" id:").append(node.getNodeID());
        builder.append(" type:").append(node.getType());
        menuItem.addActionListener(this);
        menuItem.setActionCommand("setLabel|" + builder.toString());
        return menuItem;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
        if (embedderThread != null) {
            embedderThread.stopEmbedding();
            try {
                embedderThread.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            embedderThread = null;
        }
        double x = e.getPoint().x;
        double y = e.getPoint().y;
        if ((e.getButton() == MouseEvent.BUTTON1)) {
            for (Point2D.Double p : point2node.keySet()) {
                if (Math.abs(p.x - x) <= 10 && Math.abs(p.y - y) <= 10) {
                    currentMoveNode = point2node.get(p);
                }
            }
        } else if ((e.getButton() == MouseEvent.BUTTON2) || (e.getButton() == MouseEvent.BUTTON3 && e.isAltDown())) {
            lastClickedLabel = null;
            lastClickedPoint = null;
            for (Point2D.Double p : point2node.keySet()) {
                if (Math.abs(p.x - x) <= 10 && Math.abs(p.y - y) <= 10) {
                    lastClickedLabel = point2node.get(p).getLabel();
                    lastClickedPoint = p;
                }
            }
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
        if (lastClickedLabel != null && lastClickedPoint != null) {
            double x = e.getPoint().x;
            double y = e.getPoint().y;
            String tgtLabel;
            for (Point2D.Double p : point2node.keySet()) {
                if (Math.abs(p.x - x) <= 10 && Math.abs(p.y - y) <= 10) {
                    tgtLabel = point2node.get(p).getLabel();
                    if (!tgtLabel.equals(lastClickedLabel)) {
                        // add new Relation:
                        Object label = JOptionPane.showInputDialog(this, "Please specify relation:", "Add semantic relation",
                                JOptionPane.PLAIN_MESSAGE, null, relationArray, relationArray[0]);

                        addRelation(label.toString(), lastClickedLabel, tgtLabel);
                    }
                }
            }
            lastClickedLabel = null;
            lastClickedPoint = null;
            lastDraggedLine = null;
            repaint();
        } else if (currentMoveNode != null) {
            currentMoveNode = null;
            repaint();
        }
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {

    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p/>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     */
    public void mouseDragged(MouseEvent e) {
        lastDraggedLine = null;
        if (lastClickedLabel != null && lastClickedPoint != null) {
            lastDraggedLine = new Line2D.Double(e.getPoint(), lastClickedPoint);
            repaint();
        } else if (currentMoveNode != null) {
            nodeLocation.put(currentMoveNode, new Point2D.Double(e.getPoint().getX(), e.getPoint().getY()));
            repaint();
        }
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e) {

    }

    /**
     * Creates the search string for searching in the graph list.
     *
     * @return the search String for searching in the Graph list
     */
    public String getSearchString() {
        StringBuilder sb = new StringBuilder(64);
        int count = 1;
        HashMap<String, Integer> label2position = new HashMap<String, Integer>(semanticObjects.size());
        for (LabeledNode node : semanticObjects) {
            String label = node.getLabel();
            // for emir: replace all numbers, which are in brackets :)
            label = label.replaceAll(" \\x28\\d\\x29", "");
            sb.append('[');
            sb.append(label);
            sb.append(']');
            sb.append(' ');
            label2position.put(node.getLabel(), count);
            count++;
        }
        for (LabeledEdge edge : semanticRelations) {
            //            sb.append('[');
            String label = edge.getLabel();
            if (label.indexOf('*') > -1) label = "\\w*";
            sb.append(label);
            sb.append(' ');
            LabeledNode src = (LabeledNode) edge.getStartNode();
            sb.append(label2position.get(src.getLabel()));
            sb.append(' ');
            LabeledNode tgt = (LabeledNode) edge.getEndNode();
            sb.append(label2position.get(tgt.getLabel()));
//            sb.append(']');
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * simple method to create a search string for searching within the 2-path index.
     *
     * @return string for searching within the 2-path index.
     */
    public String getPathSearchString() {
        StringBuilder sb = new StringBuilder(256);
        int count = 1;
        HashMap<String, Integer> label2position = new HashMap<String, Integer>(semanticObjects.size());
        for (LabeledNode node : semanticObjects) {
            String label = node.getLabel();
            if (label.startsWith(GraphSearchPanel.ANONYMOUS_NODE_NAME)) {
                label = "*";
            }
            // for emir: replace all numbers, which are in brackets :)
            label = label.replaceAll(" \\x28\\d\\x29", "");
            sb.append('[');
            sb.append(label);
            sb.append(']');
            sb.append(' ');
            label2position.put(node.getLabel(), count);
            count++;
        }
        for (LabeledEdge edge : semanticRelations) {
            String label = edge.getLabel();
            if (label.indexOf("*") > -1) label = "*";
            sb.append(label);
            sb.append(' ');
            LabeledNode src = (LabeledNode) edge.getStartNode();
            sb.append(label2position.get(src.getLabel()));
            sb.append(' ');
            LabeledNode tgt = (LabeledNode) edge.getEndNode();
            sb.append(label2position.get(tgt.getLabel()));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static java.util.List<Node> getNodes(String label) {
        return ((LucenePathIndexRetrievalEngine) RetrievalEngineFactory.getPathIndexRetrievalEngine()).getNodes(label, RetrievalFrame.BASE_DIRECTORY);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("showHelp")) {
            showHelp();
        } else if (cmd.equals("removeNode")) {
            removeNode(currentNode);
            currentNode = null;
        } else if (cmd.startsWith("setLabel|")) {
            String newLabel = cmd.substring("setLabel|".length());
            // remove old node in label table
            label2Node.remove(currentNode.getLabel());
            // add it with new label:
            label2Node.put(newLabel, currentNode);
            currentNode.setLabel(newLabel);
            repaint();
        }
    }

    /**
     * Shows basic help for using this panel.
     */
    private void showHelp() {
//        g2.drawString("Help:", HELP_STRING_OFFSET_X, getHeight() - 28);
//        g2.drawString("<Alt> + right mouse click on object to remove node or edge.", HELP_STRING_OFFSET_X, getHeight() - 18);
//        g2.drawString("Left mouse click on nodes and drag to other nodes to create edges.", HELP_STRING_OFFSET_X, getHeight() - 8);

        String helpString = "<Ctrl> + right mouse click on object to remove node or edge.\n" +
                "Middle mouse click or <Alt> + right mouse click on nodes and drag to other nodes to create edges.";
        JOptionPane.showMessageDialog(this, helpString, "Help", JOptionPane.INFORMATION_MESSAGE);
    }


}

class EmbedderThread extends Thread {
    private SpringEmbedder embedder;
    private boolean embedderRunning = true;
    private JPanel parent;

    public EmbedderThread(SpringEmbedder embedder, JPanel parent) {
        this.embedder = embedder;
        this.parent = parent;
    }

    public void run() {
        while (embedderRunning && embedder.step() > 0) {
            parent.repaint();
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Thread.currentThread().wait(40);
        }
        embedderRunning = false;
    }

    public void stopEmbedding() {
        embedderRunning = false;
    }

    public boolean isEmbedderRunning() {
        return embedderRunning;
    }
}
