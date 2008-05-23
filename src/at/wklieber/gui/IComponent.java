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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.gui;

import at.wklieber.Settings;
import at.wklieber.gui.data.IComponentData;
import at.wklieber.tools.Console;
import at.wklieber.tools.Java2dTools;
import at.wklieber.tools.MenuTools;



import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

public class IComponent
        extends JComponent
        implements MouseListener, MouseMotionListener,
        DragGestureListener, DragSourceListener {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IComponent.class.getName());
    protected static Console console = Console.getReference();
    protected static Java2dTools java2dTools = Java2dTools.getReference();

    protected static int MIN_SIZE = 15;
    protected static int DRAG_ACTION = DnDConstants.ACTION_COPY;

    protected static final int TYPE_VERT_UP = 0;
    protected static final int TYPE_VERT_MIDDLE = 1;
    protected static final int TYPE_VERT_DOWN = 2;
    protected static final int TYPE_HORIZ_LEFT = 10;
    protected static final int TYPE_HORIZ_MIDDLE = 11;
    protected static final int TYPE_HORIZ_RIGHT = 12;

    private static IComponent selectedComponent = null; // the reference of the active component, that has the focus

    // if true, the component is drawn
    protected boolean doShow = true;
    protected int componentMinSize = MIN_SIZE;
    // draw a rectangle on the components point
    // child classes have to use this variable on their on. It is ooptional
    protected boolean doDrawBorder = false;


    // the name of the component. this can be set as desired
    // and is not unique.
    // is for example used in objectpalette to show the text or to define the representant type (image, cameramovement)
    private String componentName = ""; // use set/getName

    public boolean isDoShow() {
        return doShow;
    }

    public void setDoShow(boolean doShow) {
        this.doShow = doShow;
    }

    public boolean isDoDrawBorder() {
        return doDrawBorder;
    }

    public void setDoDrawBorder(boolean doDrawBorder) {
        this.doDrawBorder = doDrawBorder;
    }

    // if true, drag & drop is supported (Just drag)
    protected boolean doDnd = false;
    // if true, the component can resize itsself
    // this means the subclass has to implement a resize funcionality, e.g. IColorRectangle
    protected boolean isResizeable = false;
    // if true, the mouse events for moving the component are enabled
    protected boolean isMoveable = true;
    // if true, the component can be deleted by "entf"
    protected boolean doRemove = true;
    // if true, the component is selected
    protected boolean isSelected = false;
    // if true, a visual effect is drawn, when the mouse pointer is over the component
    protected boolean doDrawEffectOnMouseOver = true;

    protected MenuTools menuTools = null;

    protected static AffineTransform t = new AffineTransform();

    //protected int doDrawBorder;

    private static Settings cfg = Settings.getReference();


    // this holds the Frame where the drawPanel is embedded
    // set this, if the component has to invoke the parentFrame
    // for example IImageComponent blocks the frame when opening a modal dialog
    protected JFrame parentFrame = null;

    private static Vector fonts = new Vector();

    static {
        try {
            //t.scale(0.7, 0.7);
            //t.wait(2000);
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font allfonts[] = ge.getAllFonts();
            for (int i = 0; i < allfonts.length; i++) {
                if (allfonts[i].canDisplayUpTo(allfonts[i].getName()) > 0) {
                    fonts.addElement(allfonts[i]);
                }
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }
    }


    public JPanel getDrawPanel() {
        return drawPanel;
    }

    public void setDrawPanel(JPanel drawPanel) {
        this.drawPanel = (IDrawPanel) drawPanel;
        this.setBounds(drawPanel.getBounds());
        //cat.fine("drawPanel set to " + getBounds().toString());
    }

    // the drawpanel where the Component can paint
    protected IDrawPanel drawPanel = null; //superclass of JPanel
    // a pointer to the parent of a component
    // used by updateParent() : e.g. by IDot to inform IRectangle to resize boundary
    protected IComponent parent = null;

    protected Color drawColor = null;
    protected Color backgroundColor = null;

    // the boundary of this object
    protected Rectangle boundary = null;

    // used when moving the component: this is the relative position of the mouse pointer
    // according to the ccomponent origiin.
    private int offsetX = 0;
    private int offsetY = 0;

    //private Border border = BorderFactory.createLineBorder(Color.BLACK, 3);

    // used by the mouse events to indicate when the mouse is over the component
    protected boolean isMouseOverComponent = false;
    // if true, a Dnd Action is currendly active (mouse pressed and doDnd=true);
    // this means that the currend object looses the focus (used in mouseover)
    private boolean isDragMode = false;

    private DragSource dragSource;
    //private DragGestureListener dgListener;
    //private DragSourceListener dsListener;


    public boolean isResizeable() {
        return isResizeable;
    }

    public void setResizeable(boolean resizeable) {
        isResizeable = resizeable;
    }

    public boolean isMoveable() {
        return isMoveable;
    }

    public void setMoveable(boolean moveable) {
        isMoveable = moveable;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public boolean isDoRemove() {
        return doRemove;
    }

    public void setDoRemove(boolean doRemove) {
        this.doRemove = doRemove;
    }

    public boolean isDoDrawEffectOnMouseOver() {
        return doDrawEffectOnMouseOver;
    }

    public void setDoDrawEffectOnMouseOver(boolean doDrawEffectOnMouseOver) {
        this.doDrawEffectOnMouseOver = doDrawEffectOnMouseOver;
    }


    protected IComponent() {
    }

    public IComponent(JPanel drawPanel1, IComponent parent1, int posX1, int posY1, int width1, int heigh1,
                      Color drawColor1, Color backgroundColor1, boolean resizeable1,
                      boolean isMoveable1, boolean selected1, boolean doDnd1) {

        init(drawPanel1, parent1, posX1, posY1, width1, heigh1,
                drawColor1, backgroundColor1, resizeable1, isMoveable1, selected1, doDnd1, "");
    }

    public IComponent getComponentParent() {
        return parent;
    }

    public void setComponentParent(IComponent parent) {
        this.parent = parent;
    }

    public int getComponentMinSize() {
        return componentMinSize;
    }

    public void setComponentMinSize(int componentMinSize) {
        this.componentMinSize = componentMinSize;
    }

    // initalize all important data
    // if child classes override this, they should call super.init() first and then implement their changes
    protected void init(JPanel drawPanel1, IComponent parent1, int posX1, int posY1, int width1, int heigh1,
                        Color drawColor1, Color backgroundColor1, boolean resizeable1,
                        boolean isMoveable1, boolean selected1, boolean doDnd1, String name1) {
        //drawPanel = drawPanel1;
        //this.setBounds(drawPanel.getBounds());
        UIManager.getLookAndFeelDefaults().put("ClassLoader", this.getClass().getClassLoader());
        this.setDrawPanel(drawPanel1);

        drawColor = drawColor1;
        parent = parent1;
        backgroundColor = backgroundColor1;
        isResizeable = resizeable1;
        isMoveable = isMoveable1;
        isSelected = selected1;
        doDnd = doDnd1;
        setName(name1);

        componentMinSize = MIN_SIZE;
        boundary = new Rectangle(posX1, posY1, width1, heigh1);
        if (isSelected()) {
            this.setComponentAsSelected();
        }

        String imageLocation = cfg.getIconsDir();
        //imageLocation = FileTools.resolvePath(imageLocation, false);
        menuTools = new MenuTools(imageLocation);
        setPopupMenuEntries();
        validate();


        if (doRemove) {
            //this.registerKeyboardAction(new ActionListener(), new KeyStroke(), 3);
        }

        //------ dnd stuff ---------
        //cat.fine("add dnd stuff");
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                //cat.fine("DND Source: Mouse pressed Adapter added");
                isSelected = true;
                repaint();
            }
        };
        drawPanel.addMouseListener(ml);

        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DRAG_ACTION, this);

        //this.setTransferHandler(new TransferHandler("text"));

        setDrawBorder(false);
        //drawPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    }

    public void setDrawBorder(boolean doDraw) {
        if (doDraw) {
            this.setBorder(null);
        } else {
            //TODO: do not work, border must be on boundary
            //this.setBorder(border);
        }
    }

    public Rectangle getComponentBounds() {
        return boundary.getBounds();
    }

    public void setComponentBounds(int x, int y, int width, int heigh) {
        if (boundary != null) {
            boundary.setBounds(x, y, width, heigh);
        }
    }

    public void setComponentBounds(Rectangle rect1) {
        if (boundary != null && rect1 != null) {
            boundary.setBounds(rect1);
        } else
            cat.severe("boundary or input rectangle is null! Component is now resized");
    }


    public void setComponentLocation(Point point1) {
        if (boundary != null) {
            boundary.setLocation(point1);
        }
    }

    public JFrame getParentFrame() {
        return parentFrame;
    }

    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    protected void addNewComponent(IComponent component1) {
        drawPanel.add(component1);

        //Container mainContainer = mainFrame.getContentPane();

        component1.addMouseListener(component1);
        component1.addMouseMotionListener(component1);
    }

    /**
     * to paint use the methods drawMouseOver, drawSelected, drawComponent
     * is overridden from JComponent
     */
    final public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        //System.out.println("paintComponent called");

        if (doShow) {
            g2.transform(t);

            drawComponent(g2);

            if (isSelected) {
                drawSelected(g2);
            }

            if (doDrawEffectOnMouseOver) {
                drawMouseOver(g2);
            }
        }
        validate();
    } // end method paint

    /**
     * Invoked by Swing to draw components.
     * Applications should not invoke <code>paint</code> directly,
     * but should instead use the <code>repaint</code> method to
     * schedule the component for redrawing.
     * <p/>
     * This method actually delegates the work of painting to three
     * protected methods: <code>paintComponent</code>,
     * <code>paintBorder</code>,
     * and <code>paintChildren</code>.  They're called in the order
     * listed to ensure that children appear on top of component itself.
     * Generally speaking, the component and its children should not
     * paint in the insets area allocated to the border. Subclasses can
     * just override this method, as always.  A subclass that just
     * wants to specialize the UI (look and feel) delegate's
     * <code>paint</code> method should just override
     * <code>paintComponent</code>.
     *
     * @param g the <code>Graphics</code> context in which to paint
     * @see #paintComponent
     * @see #paintBorder
     * @see #paintChildren
     * @see #getComponentGraphics
     * @see #repaint
     */
    public void paint(Graphics g) {
        super.paint(g);    //To change body of overridden methods use File | Settings | File Templates.
        //drawComponent((Graphics2D) g);
        //System.out.println("PAint called");
        //paintComponent(g);
    }

    // use this method is for drawing your derived components
    protected void drawComponent(Graphics2D g2) {
    }

    // use this method is for drawing your derived components when they are selected
    protected void drawSelected(Graphics2D g2) {
    }

    // this method is for visualizing when the component is under the mouse-pointer
    protected void drawMouseOver(Graphics2D g2) {
        if (isMouseOverComponent) {
            //setBorder(BorderFactory.createLineBorder(Color.black));
            Color borderColor;
            borderColor = java2dTools.getContrastColor(drawColor);
            //borderColor = Color.RED;
            g2.setPaint(borderColor);
            //cat.fine("Draw brand new visual effect in " + borderColor.toString());
            g2.draw3DRect((int) boundary.getX(), (int) boundary.getY(), (int) boundary.getWidth() - 1, (int) boundary.getHeight() - 1, false);
            g2.draw3DRect((int) boundary.getX() + 1, (int) boundary.getY() + 1, (int) boundary.getWidth() - 3, (int) boundary.getHeight() - 3, false);
            g2.draw3DRect((int) boundary.getX() + 2, (int) boundary.getY() + 2, (int) boundary.getWidth() - 5, (int) boundary.getHeight() - 5, false);
        }
    }

    /**
     * draw a text on the component
     * TODO: text is for now always drawn on the center
     */
    protected void drawText(Graphics2D g2, String text, int vertical_pos, int horizontal_pos,
                            boolean transparentBackground, boolean doDrawBox) {


        if (vertical_pos == TYPE_VERT_MIDDLE && horizontal_pos == TYPE_HORIZ_MIDDLE) {
            /*
            AttributedString as = new AttributedString(text);

            Shape shape = new Rectangle2D.Double(0,25,12,12);
             ShapeGraphicAttribute sga;
            sga = new ShapeGraphicAttribute(shape, GraphicAttribute.TOP_ALIGNMENT, false);
            as.addAttribute(TextAttribute.CHAR_REPLACEMENT, sga, 0, 1);
            */


            //float fontSize = 12;
            //Font f = ((Font) fonts.get(0)).deriveFont(Font.PLAIN, fontSize);
            //FontMetrics fm = getFontMetrics(f);
            //int strH = (int) (fm.getAscent()+fm.getDescent());

            // middle point of the component
            double middleX = boundary.getX() + boundary.getWidth() / 2;
            double middleY = boundary.getY() + boundary.getHeight() / 2;
            Color saveColor = this.drawColor;

            int borderDistance = 2;

            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(text, g2);

            double startX = middleX - ((double) rect.getWidth() / 2);
            double startY = middleY - ((double) rect.getHeight() / 2) + 2; // correct rounding mistakes
            rect = new Rectangle2D.Double(startX - borderDistance, startY - borderDistance,
                            rect.getWidth() + (2 * borderDistance), rect.getHeight() + (2 * borderDistance));

            if (!transparentBackground) {
                g2.setColor(Color.WHITE);
                g2.fill3DRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight(), true);
                g2.setColor(saveColor);
                g2.draw3DRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight(), true);
            }

            java2dTools.drawCenteredText(g2, text, (int) middleX, (int) middleY);

            g2.setColor(saveColor);
        } else {
            throw new java.lang.UnsupportedOperationException("text drawing for pos " + vertical_pos + ", " + horizontal_pos + " not implemented");
        }
    }


    public void validate() {
        super.validate();
        this.setBounds(drawPanel.getBounds());
    }

    /**
     * Repaints this component.
     * <p/>
     * If this component is a lightweight component, this method
     * causes a call to this component's <code>paint</code>
     * method as soon as possible.  Otherwise, this method causes
     * a call to this component's <code>update</code> method as soon
     * as possible.
     * <p/>
     * <b>Note</b>: For more information on the paint mechanisms utilitized
     * by AWT and Swing, including information on how to write the most
     * efficient painting code, see
     * <a href="http://java.sun.com/products/jfc/tsc/articles/painting/index.html">Painting in AWT and Swing</a>.
     *
     * @see #update(java.awt.Graphics)
     * @since JDK1.0
     */
    public void repaint() {
        super.repaint();    //To change body of overridden methods use File | Settings | File Templates.
        //System.out.println("ICOMPONENT: repaint: " + this.getBounds().toString());
    }


    //--------------- some tool methods ----------------------------

    /**
     * Add a component to the Object List for the drawing Panel.
     * This ensures the component is repainted automatically
     */
    protected void addOtherComponent(IComponent component1) {
        this.add(component1);
        component1.addMouseListener(component1);
        component1.addMouseMotionListener(component1);

        component1.revalidate();

    }

    // ensure that width and heigh are positive
    protected static Rectangle validateBounds(Rectangle rect1) {
        Rectangle returnValue = new Rectangle(rect1);

        int x = (int) returnValue.getX();
        int y = (int) returnValue.getY();
        int w = (int) returnValue.getWidth();
        int h = (int) returnValue.getHeight();

        if (w < 0) {
            x = x - w;
            w = -w;
        }

        if (h < 0) {
            y = y - h;
            h = -h;
        }

        returnValue.setBounds(x, y, w, h);

        return returnValue;
    }

    // override the original contains because the borders don't match
    public boolean contains(int posX, int posY) {
        if (boundary == null) {
            return false;
        }

        //cat.fine("Contains check for: " + posX + ", " + posY);
        int x = (int) boundary.getX();
        int y = (int) boundary.getY();
        int w = (int) boundary.getWidth();
        int h = (int) boundary.getHeight();

        boolean thisResult = ((posX >= x) && ((posX - x) <= w) && (posY >= y) && ((posY - y) <= h));
        //boolean superResult;
        //superResult = super.contains(posX, posY);

        //cat.fine("Contains this: " + x + ", " + y + ", " + w + ", " + h + ", " + thisResult);
        //cat.fine("Contains super: " + superResult);

        return thisResult;
    }


    protected void finalize() throws Throwable {
        super.finalize();
    }

    public String toString() {
        return "IComponent: " + this.getX() + ", " +
                       this.getY() + ", " + this.getWidth() + ", " +
                       this.getHeight() + ", Color" + drawColor.getRGB();
    }

    public boolean isDoDnd() {
        return doDnd;
    }

    public void setDoDnd(boolean doDnd) {
        this.doDnd = doDnd;
    }

    public Color getDrawColor() {
        return drawColor;
    }

    public void setDrawColor(Color drawColor) {
        this.drawColor = drawColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    // makes a deep clone of this IComponent
    // implementing classes should override this
    public Object clone() {
        IComponent returnValue = null;
        cat.fine("make a IComponent clone");
        returnValue = new IComponent(drawPanel, parent, (int) boundary.getX(), (int) boundary.getY(),
                        (int) boundary.getWidth(), (int) boundary.getHeight(),
                        drawColor, backgroundColor, isResizeable, isMoveable,
                        isSelected, doDnd);

        return returnValue;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    protected void setComponentAsSelected() {
        if (selectedComponent != null) {
            selectedComponent.setSelected(false);
        }

        selectedComponent = this;
        selectedComponent.setSelected(true);
    }

    /**
     * use for serialization
     */
    public IComponentData getComponentData() {
        IComponentData returnValue = new IComponentData(this);
        returnValue.setBackGround(backgroundColor);
        returnValue.setBoundary(boundary);


        return returnValue;
    }

    /**
     * use for deserialization
     */
    public void setComponentData(IComponentData data1) {
        //cat.fine("v: " + data1.getBoundary().toString());
        //cat.fine("n: " + this.boundary.toString());
        setComponentBounds(data1.getBoundary());
        this.backgroundColor = data1.getBackGround();
        setName(data1.getName());
    }


    //--------------------- Mouse Events ----------------------------------------
    public void mouseClicked(MouseEvent e) {
        //cat.fine("mouseClicked: " + e.getX() + ", " + e.getY());
        if (SwingUtilities.isRightMouseButton(e)) {
            menuTools.showPopupMenu(this, e.getX(), e.getY());
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            setComponentAsSelected();
        }
    }

    public void mousePressed(MouseEvent e) {
        //cat.fine("mousePressed: " + e.getX() + ", " + e.getY());
        isSelected = true;

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (!isMoveable) {
                return;
            }

            //move the rectangle if it was already painted
            //if not (new instance), this event doesn't occur
            if (contains(e.getX(), e.getY())) {
                offsetX = (int) (boundary.getX() - e.getX());
                offsetY = (int) (boundary.getY() - e.getY());
            }
        }

        repaint(this.getBounds());
    }

    public void mouseReleased(MouseEvent e) {
        //cat.fine("mouseReleased: " + e.getX() + ", " + e.getY());
    }

    public void mouseEntered(MouseEvent e) {
        //cat.fine("mouseEntered: " + e.getX() + ", " + e.getY() + ", DragMode: " + isDragMode);
        if ((!isMouseOverComponent) && (doDrawEffectOnMouseOver) && (!isDragMode)) {
            isMouseOverComponent = true;

            repaint();
        }
    }

    public void mouseExited(MouseEvent e) {
        //cat.fine("mouseExited: " + e.getX() + ", " + e.getY());
        if ((isMouseOverComponent) && (doDrawEffectOnMouseOver)) {
            isMouseOverComponent = false;
            repaint();
        }
    }


    public void mouseDragged(MouseEvent e) {
        //cat.fine("mouseDragged: " + e.getX() + ", " + e.getY());
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (!isMoveable) {
                //cat.fine("not movable");
                return;
            }
            //int oldX = boundary.x;
            boundary.setBounds((e.getX() + offsetX),
                    (e.getY() + offsetY),
                    (int) boundary.getWidth(), (int) boundary.getHeight());
            //cat.fine(" boundary moved: " + oldX + ", " + boundary.x);
            this.repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
        //cat.fine("mouseMoved: " + e.getX() + ", " + e.getY());
    }


    //--------------------- Mouse Events Dnd Source things -------------------------------

    public void dragGestureRecognized(DragGestureEvent event) {
        //cat.fine("DRAG: dragGestureRecognized: " + event.toString());

        if (doDnd) {
            IComponentTransferable trans = new IComponentTransferable(this);
            Transferable transferable = trans;

            //cat.fine("before start drag");

            dragSource.startDrag(event, DragSource.DefaultCopyDrop, transferable, this);
            //cat.fine("after start drag");

            isMouseOverComponent = false;
            isDragMode = true;

            repaint();
        }
    }

    public void dragEnter(DragSourceDragEvent event) {
        //cat.fine("DRAG: dragEnter: (not implemented)" + event.getX() + ", " + event.getY());
    }

    public void dragOver(DragSourceDragEvent event) {
        //cat.fine("DRAG: dragOver: (not implemented)" + event.getX() + ", " + event.getY());
        // cat.fine("Name: <"  + this.getComponentName());
        isDragMode = false;
    }

    public void dropActionChanged(DragSourceDragEvent event) {
        //cat.fine("DRAG: dropActionChanged: (not implemented)" + event.getX() + ", " + event.getY());
    }

    public void dragExit(DragSourceEvent event) {
        //cat.fine("DRAG: dragExit: (not implemented)" + event.getX() + ", " + event.getY());
    }

    public void dragDropEnd(DragSourceDropEvent event) {
        //cat.fine("DRAG: dragDropEnd: (not implemented)" + event.getX() + ", " + event.getY());
    }

    // this is invoked by a child to inform this class about changes
    // has to be ovveridden when using
    protected void updateParent(IComponent child1) {
        throw new java.lang.UnsupportedOperationException("class " + this.getClass().getName() + " not implemented");
    }

    public void setSize(Dimension d) {
        boundary.setSize(d);
    }

    // inserts the popup menu stuff
    protected void setPopupMenuEntries() {
        // actionlisterner for removing the element
        ActionListener removeComponentAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionRemoveComponent(e);
            }
        };

        ActionListener moveComponentToToptAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionMoveComponentToTop(e);
            }
        };

        ActionListener moveComponentToBackAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionMoveComponentToBack(e);
            }
        };

        menuTools.addPopupMenuEntry("&Remove", "Remove this element", "remove_component.gif", removeComponentAction);
        menuTools.addPopupMenuEntry("Group &Top", "Move this Component to the Top", "", moveComponentToToptAction);
        menuTools.addPopupMenuEntry("Group &Back", "Move this Component to the Background", "", moveComponentToBackAction);

        menuTools.addPopupMenuEntry("&Help", "Display help information", "help.gif", (ActionListener) null);
        //menuTools.addPopupMenuEntry("&Analysis", "Extract feature information out of this Element", "", null);

    }

    protected void removePopupMenuEntries() {
        menuTools.removeAllPopupEntries();
    }

    public void actionRemoveComponent(ActionEvent e) {
        try {
            drawPanel.removeIComponent(this);
            this.setEnabled(false);
            revalidate();
            drawPanel.repaint();

        } catch (Exception e1) {
            cat.severe(e1.toString());
            e1.printStackTrace();
        }
    }


    // first draw all lines, then the rest
    public void actionMoveComponentToTop(ActionEvent e) {
        try {
            int newPos = 0;
            /* if (!ILine.class.isInstance(this)) {
                newPos = drawPanel.getComponentCount() - drawPanel.getILineComponentsCount();
             }*/


            drawPanel.remove(this);
            drawPanel.add(this, newPos);

            revalidate();
            drawPanel.repaint();

        } catch (Exception e1) {
            cat.severe(e1.toString());
            e1.printStackTrace();
        }
    }

    public void actionMoveComponentToBack(ActionEvent e) {
        try {
            drawPanel.remove(this);
            /*if (ILine.class.isInstance(this)) {
               int newPos = drawPanel.getComponentCount() - drawPanel.getILineComponentsCount();
                drawPanel.add(this, newPos);
            } else {*/
            drawPanel.add(this);
            //}

            revalidate();
            drawPanel.repaint();

        } catch (Exception e1) {
            cat.severe(e1.toString());
            e1.printStackTrace();
        }
    }

}  // end class IComponent

