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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jdom.Document;
import org.jdom.Element;

import at.lux.fotoannotation.AnnotationFrame;
import at.lux.imageanalysis.ColorLayout;
import at.wklieber.Settings;
import at.wklieber.gui.data.IComponentData;
import at.wklieber.tools.ColorBlockExtractor;
import at.wklieber.tools.Console;
import at.wklieber.tools.FileTools;
import at.wklieber.tools.Java2dTools;
import at.wklieber.tools.MenuTools;

public class DrawSketchFrame
        extends JPanel
        implements IComponentReceivedInterface {
    private static Logger log = Logger.getLogger(DrawSketchFrame.class.getName());
    private static Console console = Console.getReference();
    protected static Java2dTools java2dTools = Java2dTools.getReference();
    private static Settings cfg = Settings.getReference();

    public static final String DEFAULT_WINDOW_TITLE = "Specify Colors";


    // ----------------------------- static stuff end ----------------------------------

    JPanel mainPanel = null; // This panel covers the complete drawing Frame
    JPanel mainFrame = null; // the main panel to draw (This panel)
    private JFrame parentFrame = null; // an instance is passed within the constructor
    Container toolbarWrapper = null;
    IDrawPanel drawPanel = null;
    JMenuBar menuBar = null;

    JPanel statusPanel = null;
    JPanel buttonPanel = null;
    JToolBar toolBar = null;
    MenuTools menuTool = null;

    private JLabel statusBar;

    private java.util.List<IComponentData> iComponentList = null; // List of IComponent
    private BufferedImage image = null;  // the (background) image to fill with meta data
    private BufferedImage sketchImage = null; // the image just containing the drawed components (shapes, ...)
    private static int COLOR_LAYOUT_Y = 64;
    private static int COLOR_LAYOUT_C = 64;
    private AnnotationFrame annotationsFrame;


    public DrawSketchFrame(JFrame parentFrame1, java.util.List iComponentList1) {
        super();
        init(parentFrame1, null, null, iComponentList1);
    }

    public DrawSketchFrame(AnnotationFrame parentFrame1, Container toolbar, java.util.List iComponentList1) {
        super();
        annotationsFrame = parentFrame1;
        BufferedImage image1 = parentFrame1.getImage();
        init(null, image1, toolbar, iComponentList1);
    }


    private void init(JFrame parentFrame1, BufferedImage image1, Container toolbar, java.util.List iComponentList1) {
        parentFrame = parentFrame1;
        image = image1;

        mainFrame = this;

        clear();

        Container mainContainer = mainFrame;

        if (mainPanel != null) {
            mainPanel.removeAll();
        }

        // initialize all member-menu Panels
        mainPanel = new JPanel(); // This panel covers the complete Frame
        drawPanel = new IDrawPanel(this);
        menuBar = new JMenuBar();

        statusPanel = new JPanel();
        buttonPanel = new JPanel();
        if (toolbar == null) {
            toolbarWrapper = new Container();
            LayoutManager mgr = new FlowLayout(FlowLayout.LEADING);
            toolbarWrapper.setLayout(mgr);
        } else {
            toolbarWrapper = toolbar;
        }


        //------------------------ add Elements ---------------------------
        String iconLocation = cfg.getIconsDir();
        menuTool = new MenuTools(this, menuBar, toolbarWrapper, iconLocation);
        setMenuEntries();

        ActionListener drawShapeAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionDrawShape(e);
            }
        };

        menuTool.addPopupMenuEntry("Draw &Shape", "Draw a shape", "", drawShapeAction);

        //------------------------ define the Drawpanel ---------------------------
        drawPanel.setIgnoreRepaint(false);
        drawPanel.setPreferredSize(new Dimension(600, 500));
        drawPanel.setToolTipText("");
        drawPanel.setBackground(Color.white);
        drawPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        drawPanel.setLayout(null);

        //--------------- link all panels to the frame
        mainPanel.setLayout(new BorderLayout());

        // Set this instance as the application's menu bar
        // add a wrapper for menubar and toolbar

        if (annotationsFrame != null) {
            mainContainer.add(toolbarWrapper, BorderLayout.NORTH);
            mainPanel.add(drawPanel, BorderLayout.CENTER);
            mainContainer.add(mainPanel, BorderLayout.CENTER);
        }


        mainPanel.validate();
        mainPanel.repaint();


        this.setVisible(true);
        clear();

        if (parentFrame != null) {
            parentFrame.setEnabled(false);
            log.fine("hide parent frame");
            //parentFrame.hide();
        } else
            log.fine("parent frame is null");


        //drawIComponents();
        setIComponents(iComponentList1);
        //test();
    }

    private void drawIComponents() {
        // draw components, if available
        for (Iterator<IComponentData> it = iComponentList.iterator(); it.hasNext();) {
            IComponentData obj = it.next();
            /*if (!(obj instanceof IComponent)) {
                log.severe("got draw-list with invalid object-type: \"" + obj.getClass().getName() + "\". Need elements of type ICompontent");
                continue;
            }*/
            IComponent icomp;
            icomp = obj.getIComponent(this.drawPanel);


            //icomp = (IComponent) obj;
            IComponent clone;
            clone = (IComponent) icomp.clone();
            clone.setDrawPanel(this.drawPanel);

            clone.setMoveable(true);
            clone.setResizeable(true);
            clone.setDoDnd(false);

            addNewComponent(clone);
        }
    }

    public void setIComponents(java.util.List<IComponentData> iComponentList1) {
        iComponentList = iComponentList1;
        if (iComponentList == null) iComponentList = new ArrayList();
        drawIComponents();
    }


    /**
     * empty all data
     */
    public void clear() {
        //iComponentDataList = new Vector();
        if (annotationsFrame != null) {
            image = annotationsFrame.getImage();
        }

//        System.out.println("Setting beackgroundimage: " + image);

        if (drawPanel != null) {
            drawPanel.removeAll();
            drawPanel.setBackgroundImage(image);
        }

        repaint();
    }

    private Component buildBottomPanel() {
        statusBar = new JLabel(" Idle...");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        return statusBar;
    }

    // not tested
    public void setImage(BufferedImage image) {
        this.image = image;
        clear();
    }

    // inserts the menu and toolbar stuff
    private void setMenuEntries() {
        menuTool.readMenuFromConfigFile("shapeBar", MenuTools.TYPE_MENUBAR);
    }


    public String toString() {
        return "DrawSketchFrame";
    }

    // --------- actions for the Buttons ----------------------



    public void actionDominantColor(ActionEvent e) {
        try {
            if (image == null) { // load one image from file
                String file = FileTools.showOpenDialog(cfg.getDataDir(), "jpg", "JPEG Image", true, null);
                //log.fine("Analyse file: " + file);

                if (file == null) {
                    return;
                }

                image = javax.imageio.ImageIO.read(new File(file));
                drawPanel.setBackgroundImage(image);
            }

            ColorBlockExtractor colorBlocks = new ColorBlockExtractor(image, false);
            BufferedImage colorReducedImage = colorBlocks.getQuantizedImage();
            IColorRectangle[] components = java2dTools.getDominantColor(colorReducedImage, drawPanel);

            for (int i = 0; i < components.length; i++) {
                addNewComponent(components[i]);
            }

            repaint();
        } catch (Exception e1) {
            log.severe(e1.toString());
            e1.printStackTrace();
        }
    }

    public void actionColorLayout(ActionEvent e) {
        try {
            if (image == null) { // load one image from file
                String file = FileTools.showOpenDialog(cfg.getDataDir(), "jpg", "JPEG Image", true, null);
                //log.fine("Analyse file: " + file);

                if (file == null) {
                    return;
                }

                image = javax.imageio.ImageIO.read(new File(file));
                drawPanel.setBackgroundImage(image);
            }

            ColorLayout colorLayout = new ColorLayout(image);
            colorLayout.setNumberOfYCoeff(COLOR_LAYOUT_Y);
            colorLayout.setNumberOfCCoeff(COLOR_LAYOUT_C);


            Element colorLayoutElement = colorLayout.getDescriptor();

            Document doc = new Document(colorLayoutElement);


            repaint();
        } catch (Exception e1) {
            log.severe(e1.toString());
            e1.printStackTrace();
        }
    }


    public void actionClear(ActionEvent e) {
        clear();
    }

    public void actionOpenMpeg7(ActionEvent e) {
        log.info("method " + "actionOpenMpeg7" + " not implemented");
    }

    public void actionSaveMpeg7(ActionEvent e) {
        log.info("method " + "actionSaveMpeg7" + " not implemented");
    }

    public void actionCloseDialog(ActionEvent e) {
        log.info("method " + "actionCloseDialog" + " not implemented");
    }

    public void actionColorPalette(ActionEvent e) {
        log.info("method " + "actionColorPalette" + " not implemented");
    }

    public void actionViewMpeg7(ActionEvent e) {
        /*Mpeg7 mpeg7Document = generateMpeg7FromData(dataList1);

        XMLTreeView view = new XMLTreeView(new JFrame(), true, mpeg7Document.getDocument((Document) null));
        view.setSize(640, 480);
        //view.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
        view.setVisible(true);*/
    }

    /**
     * make a list with all data extracted from the visible components
     * like shape color and shape
     */
    private java.util.List makeIComponentList() { // update the List of the parent
        java.util.List returnValue = new Vector(1, 3);

        try {
            Component[] comp = drawPanel.getComponents();
            for (int i = 0; i < comp.length; i++) {
                if (IComponent.class.isInstance(comp[i])) {
                    IComponent icomp = (IComponent) comp[i];
                    //log.fine("ADD JCOMPONENT: " + icomp.toString());
                    returnValue.add(icomp);
                }
            }
        } catch (Exception e1) {
            log.severe(e1.toString());
            e1.printStackTrace();
        }
        return returnValue;
    }


    private void updateData() { // update the List of the parent
        try {
            /*
            Component[] comp = drawPanel.getComponents();
            iComponentList.clear();
            for (int i = 0; i < comp.length; i++) {
                if (IComponent.class.isInstance(comp[i])) {
                    IComponent icomp = (IComponent) comp[i];
                    //log.fine("ADD JCOMPONENT: " + icomp.toString());
                    iComponentList.add(icomp);
                }
            } */
            // note: handler of iComponent may not change !!!!
            iComponentList.clear();
            iComponentList.addAll(makeIComponentList());
        } catch (Exception e1) {
            log.severe(e1.toString());
            e1.printStackTrace();
        }
    }


    public java.util.List getiComponentList() {
        return iComponentList;
    }

    public java.util.List<IComponentData> getDataList() {
        java.util.List<IComponentData> returnValue;
        returnValue = makeDataList();
        return returnValue;
    }


    public BufferedImage getImage() {
        return image;
    }


    public void actionDrawShape(ActionEvent e) {
        try {
            IShape shape1 = new IShape(drawPanel, Color.BLACK, Color.GRAY,
                            image, drawPanel.getBackgroundImageSize());
            addNewComponent(shape1);
        } catch (Exception e1) {
            log.severe(e1.toString());
            e1.printStackTrace();
        }
    }


    private void addNewComponent(IComponent component1) {
        drawPanel.add(component1);

        component1.addMouseListener(component1);
        component1.addMouseMotionListener(component1);
    }


    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * now we have an new document
     */
    public void getIcomponentFromDnd(IComponent component1) {

        drawPanel.addNewComponent(component1);
    }


    /**
     * make a list with all data extracted from the visible components
     * like shape color, cameraMotions and so on
     */
    private java.util.List<IComponentData> makeDataList() {
        java.util.List<IComponentData> returnValue = new ArrayList<IComponentData>();
        try {
            Component[] comp = drawPanel.getComponents();
            java.util.List<IComponentData> iComponentDataList = new ArrayList<IComponentData>();
            for (Component component1 : comp) {
                if (IComponent.class.isInstance(component1)) {
                    IComponent icomp = (IComponent) component1;
                    //log.fine("ADD JCOMPONENT: " + icomp.toString());
                    iComponentDataList.add(icomp.getComponentData());
                }
            }
            returnValue.addAll(iComponentDataList);

        } catch (Exception e1) {
            log.severe(e1.toString());
            e1.printStackTrace();
        }

        return returnValue;
    }

    // return backgroundImage or the sketch
    public BufferedImage getSketchImage() {
        BufferedImage returnValue = null;

        if (image == null) {
            return returnValue;
        }

        try {
            BufferedImage similarityImage;
            //updateData();
            java.util.List componentList = makeIComponentList();
            similarityImage = image;
            if (!(componentList == null) && !(componentList.size() == 0)) {
                IComponent[] compArray = new IComponent[componentList.size()];
                int counter;
                counter = 0;
                for (Iterator it = componentList.iterator(); it.hasNext();) {
                    IComponent icomp = (IComponent) it.next();
                    IComponent clone = (IComponent) icomp.clone();
                    if (IShape.class.isInstance(clone)) {
                        IShape shape = (IShape) clone;
                        shape.setDoDrawEffectOnMouseOver(false);
                        shape.setDoDrawBorder(false);
                    }
                    clone.setDrawPanel(this.drawPanel);

                    clone.setMoveable(true);
                    clone.setResizeable(true);
                    clone.setDoDnd(false);

                    compArray[counter] = clone;
                    counter++;
                } // end for

                similarityImage = java2dTools.javaObjectsToImage(compArray);
                returnValue = similarityImage;
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        return returnValue;

    }

    public void actionViewMetadataImage(ActionEvent e) {
        BufferedImage similarityImage;

        similarityImage = getSketchImage();
        /*
        java.util.List componentList = makeIComponentList();
        similarityImage = image;
        if (!(componentList == null) && !(componentList.size() == 0)) {
            IComponent[] compArray = new IComponent[componentList.size()];
            int counter;
            counter = 0;
            for (Iterator it = componentList.iterator(); it.hasNext();) {
                IComponent icomp = (IComponent) it.next();
                IComponent clone = (IComponent) icomp.clone();
                clone.setDrawPanel(this.drawPanel);

                clone.setMoveable(true);
                clone.setResizeable(true);
                clone.setDoDnd(false);

                compArray[counter] = clone;
                counter++;
            } // end for

            similarityImage = java2dTools.javaObjectsToImage(compArray);
            */
        java2dTools.showImage(similarityImage);
        //} // end if
    }


}
