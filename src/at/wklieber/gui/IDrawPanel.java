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
import at.wklieber.tools.Console;
import at.wklieber.tools.IAccessFile;
import at.wklieber.tools.Java2dTools;
import at.wklieber.tools.MessageBox;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IDrawPanel
//extends JScrollPane
        extends JPanel
        implements MouseListener, MouseMotionListener, DropTargetListener {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(IDrawPanel.class.getName());
    private static Console console = Console.getReference();
    private static Settings cfg = Settings.getReference();
    private static Java2dTools java2dTools = Java2dTools.getReference();
    private static IAccessFile config = cfg.getConfigAccess();

    private static final int DEFAULT_SHAPE_SIZE = 150;

    public static final int DROP_ACCECPT_ALL = 0;
    public static final int DROP_ACCECPT_IMAGES = 1;       // use in the draw by sketch area
    public static final int DROP_ACCECPT_REPRESENTANTS = 2; // use in the mainframe


    private DropTarget dropTarget = null;
    private int acceptDropTargets = DROP_ACCECPT_ALL;

    // if true, the panel is used to draw Mulmiedia objects and operator
    // this means, when deleting objects, the previous operator (ILine) is removed too
    private boolean isMathMode = false;

    // contains a method that is called when a new compoent is dropped
    // to this drawPanel
    private IComponentReceivedInterface parentFrame = null;
    private BufferedImage backgroundImage = null;
    private Rectangle backgroundImageSize = null;

    private boolean acceptDnd = true; // if false, all dnd sources are not allowed


    public IDrawPanel() {
        init(null);
    }

    public IDrawPanel(IComponentReceivedInterface parentFrame1) {
        init(parentFrame1);
    }

    private void init(IComponentReceivedInterface parentFrame1) {
        parentFrame = parentFrame1;
        isMathMode = false;

        //--------- dnd trop stuff
        dropTarget = new DropTarget((Component) this, (int) DnDConstants.ACTION_COPY,
                        (DropTargetListener) this, true);
        dropTarget.setActive(true);
        this.setDropTarget(dropTarget);

        validate();
    }

    public boolean isAcceptDnd() {
        return acceptDnd;
    }

    public void setAcceptDnd(boolean acceptDnd) {
        this.acceptDnd = acceptDnd;
    }

    public int getAcceptDropTargets() {
        return acceptDropTargets;
    }

    public void setAcceptDropTargets(int acceptDropTargets) {
        this.acceptDropTargets = acceptDropTargets;
    }

    /**
     * returns the number of ILines in the drawpanel.
     * Note: all ILines must be at the and of the component-list (so they are painted first);
     */
    public int getILineComponentsCount() {
        int returnValue = 0;

        try {
            Component[] comps = this.getComponents();
            int counter = comps.length - 1;
            while ((counter > -1) && (ILine.class.isInstance(comps[counter]))) {
                returnValue++;
                counter--;
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /*public void setObjectPalette(ObjectPalette objectPalette) {
        this.objectPalette = objectPalette;
    }*/

    // not prepared to add lines
    public void addNewComponent(IComponent component1) {
        //this.add(component1, 0);
        this.add(component1);
        //this.add(component1, this.getComponents().length);
        component1.setDrawPanel((JPanel) this);
        component1.addMouseListener(component1);
        component1.addMouseMotionListener(component1);
    }

    /**
     * dnd drop target stuff. Supported types are Images and Java-Icomponents (internul stuff).
     * Supported is Windows with Internet Explorer and Mozilla
     *
     * @param dtde
     */
    public void drop(DropTargetDropEvent dtde) {
        try {
            if (!acceptDnd) {
                dtde.rejectDrop();
                return;
            }


            //cat.fine("dnd dropped, included Flavors:");
            Transferable t = dtde.getTransferable();
            DataFlavor[] flavorList = dtde.getCurrentDataFlavors();

            /*
            for (int i = 0; i < flavorList.length; i++) {
                cat.fine("--> Dnd Transfer Mime-Type: <" + flavorList[i].getMimeType() + ">, Info <" + flavorList[i].toString() + ">");
            }
            */
            /*
            cat.fine("TransferList");
            DataFlavor[] flavorListD = t.getTransferDataFlavors();
            for (int i = 0; i < flavorListD.length; i++) {
                cat.fine("--> Dnd Transfer Mime-Type: <" + flavorListD[i].getMimeType() + ">, Info <" + flavorListD[i].toString() + ">");
            }
            */
            IComponent component = null; //this object will receive the dropped data
            String urlString = ""; // set this to the image name to request metadat for search results
            URL imageUrl = null; // for downloading images in ie

            //------------- ACCEPT own IComponent Data ----------------------------
            if (t.isDataFlavorSupported(IComponentTransferable.localIComponentFlavor)) {
                cat.info("Got IComponent from Objectpalette");

                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                component = (IComponent) t.getTransferData(IComponentTransferable.localIComponentFlavor);
                dtde.getDropTargetContext().dropComplete(true);

                boolean accept = true;
                String errorMessage = "";
                if (acceptDropTargets == DROP_ACCECPT_REPRESENTANTS) {
                    // it is the panel of the mainframe - we do only accept images and IRepresentants

                    String name = component.getClass().getName();
                    //System.out.println(name);
                    if (!(IImageComponent.class.getName().equals(name))) {
                        accept = false;
                        errorMessage = "This kind of object cannot be dropped into his screen. \n " +
                                "Please use an image or a Representant";
                    }
                }

                // TODO: add here restrictions for the "draw by sketch" dialog

                if (!accept) {
                    MessageBox.displayMessage("Error: invalid object", errorMessage);
                    dtde.getDropTargetContext().dropComplete(true);
                    return; // ----------------> EXIT POINT <--------------------------------------
                }



                // prepare for default output in parent
                if (IImageComponent.class.isInstance(component)) {
                    IImageComponent c = (IImageComponent) component;
                    if (c.getImage() == null) cat.severe("Image is null");

                }

                if (IColorRectangle.class.isInstance(component)) {
                    IColorRectangle c = (IColorRectangle) component;
                    c.setDoDisplayPercentage(true);
                } else if (IShape.class.isInstance(component)) {
                    IShape c = (IShape) component;
                    //c.setMoveable(true);
                    //c.setResizeable(true);
                    ///c.setDoDnd(false);
                    c.setDrawDots(true);
                    c.setComponentBounds(0, 0, DEFAULT_SHAPE_SIZE, DEFAULT_SHAPE_SIZE);
                }

                //--------------------- get as much data as possible from standard flavors    ------
            } else {
                // tested for internet explorer (not working in all cases) and mozilla (working)

                boolean accept = false;
                BufferedImage bImage = null;

                String dropString = "";


                if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    cat.info("get IMAGE from imageFlavor directly");
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);

                    if (image != null) {
                        bImage = java2dTools.imageToBufferedImage(image);
                    }

                    cat.fine("DnD Target: Image received received");
                    accept = true;

                    // get the image url for further
                    if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        cat.info("get URL from stringFlavor directly");
                        String dummy = (String) t.getTransferData(DataFlavor.stringFlavor);
                        if (dummy != null && dummy.length() > 0) {
                            urlString = dummy;
                            cat.fine("Image URL: <" + urlString + ">");
                        }
                    } else { // try to get the url from something else: does not work
                        /*
                        for (int i = 0; i < flavorList.length; i++) {
                            DataFlavor x = flavorList[i];
                            if (x.isFlavorTextType()) {
                                String data = "";
                                Object q = t.getTransferData(x);

                                data = q.getClass().getName();
                                System.out.println("DATA: <" + data + ">");

                                if (String.class.isInstance(q)) {
                                    String res = (String) q;
                                    data = "String: <" + res + ">";
                                    System.out.println(data);
                                }

                                if (java.io.InputStreamReader.class.isInstance(q)) {
                                    InputStreamReader res = (InputStreamReader) q;
                                    data = MiscTools.inputStreamToString(res, "");
                                    data = "InputStreamReader: <" + data + ">";
                                    System.out.println(data);
                                }
                            }
                        } // end for
                        */
                    }
                } // end if is imageflavor


                // try to get the image url
                for (int i = 0; i < flavorList.length; i++) {
                    DataFlavor flavor = flavorList[i];
                    if (flavor.getRepresentationClass().equals(java.net.URL.class)) {
                        try {
                            if (dtde.isDataFlavorSupported(flavor)) {
                                if (!accept) {
                                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                                    accept = true;
                                }
                                URL myUrl = (URL) dtde.getTransferable().getTransferData(flavor);
                                cat.fine("DnD Target: URL received, null: " + (myUrl == null));
                                if (myUrl != null) {
                                    imageUrl = myUrl;
                                }
                                //outputArea.append("\tURL: " + myUrl.toString() + "\n");
                                break;
                            }
                        } catch (Exception e) {
                            cat.fine("unable to get The URL of the image. (Mozill causes this exception)");
                            //e.printStackTrace();
                        }
                    }
                } // end for
                //} // end if imageFlavor



                // ie just sends the string, so we have load it
                if ((bImage == null) && (imageUrl != null)) {
                    /*
                    URL url = null;
                    try {
                        url = new URL(imageUrl);
                    } catch (MalformedURLException e) {
                        //e.printStackTrace();
                        cat.severe(e.toString());
                    }
                      */
                    // Get the image
                    if (imageUrl != null) {
                        cat.info("load image from url: " + imageUrl.toExternalForm());
                        Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);
                        if (image != null) {
                            bImage = java2dTools.imageToBufferedImage(image);
                        }
                    }
                }

                component = new IImageComponent(this, bImage);

                if (!accept) {
                    cat.fine("Flavor rejected");
                    dtde.rejectDrop();
                }

            } // end if what flavor

            dtde.getDropTargetContext().dropComplete(true);

            //----- Now we have all the data

            if (component != null) {
                Point point = dtde.getLocation();
                component.setDrawPanel((JPanel) this);
                component.setComponentLocation(point);
                component.setDoDnd(false);
                component.setResizeable(true);
                component.setMoveable(true);

                if (parentFrame == null) { // draw it if no listener. Otherwise the listener gets the data and can do what it wants
                    addNewComponent(component);
                    repaint();
                    //addImage(image.getImage(), (int) point.getX(), (int) point.getY());
                } else {
                    parentFrame.getIcomponentFromDnd(component);
                }

                repaint();


                if (imageUrl != null) {
                    urlString = imageUrl.toExternalForm();
                }
                cat.fine("URL: <" + urlString + ">");
                if (urlString.length() > 0) {
                    String id = "";  // id to load mpeg7 data if needed
                    String fileId = "";

                    try {
                        //String urlPath = FileTools.getFilePath(urlString);
                        // get the filename without the ending ".jpg"

                        // Url is e.g: http://129.27.200.42:8082/cocoon/imb2/image.jsp?id=jr_test;IMB;1;T00:01:23:19F25
                        int start = urlString.indexOf('=');
                        String subString = urlString.substring(start + 1);
                        // remove eventuell ending stuff
                        start = subString.indexOf("\n");
                        if (start > 0) {
                            fileId = subString.substring(0, start);
                        } else
                            fileId = subString;
                        //fileId = urlString.substring(urlPath.length() - 1, urlString.length() - 4);

                        id = "";

                    } catch (Exception e) {
                        // make no furhter request on an failure
                        e.printStackTrace();
                        cat.severe(e.toString());
                    }

                    if (id.length() != 0) {
                        //MessageBox.displayMessage("", "");
                        //todo: add here the code to parse the mpeg7 file when retrieving a search result
                        //parseMpeg7Document(fileId);
                    }
                } // end if get further metadata
            } // endif got a valid image

            // for debugging
            //parseMpeg7Document("jr_test;IMB;1;");

            //{ Must catch IOException and UnsupportedFlavorException
        } catch (Exception e) {
            cat.severe(e.toString());
            e.printStackTrace();
        }
    }

    /*private void parseMpeg7Document(String fileId) {
        cat.fine("get MPEG7 file: <" + fileId + ">");

        //http://129.27.200.42:8082/cocoon/imb2/query-xml-xml
        //----- get the url from where to request the data
        String relQueryPoint = config.getProperty("imbConfig/broker/webAccess/mpeg7Url", "",
                "/cocoon/imb2/detail");

        String webServerBaseUrl = config.getProperty("imbConfig/broker/webAccess/baseUrl", "",
                "http://127.0.0.1:80");

        String webUrl = webServerBaseUrl + relQueryPoint + "?id=" + fileId;
        cat.fine("Complete url: <" + webUrl + ">");

        // open an httprequest to that url
        DownloadGuiThread thread = new DownloadGuiThread(webUrl);
        thread.start();   // show message box and lock gui
        //String result = thread.getResult();

        //thread.close();
        //thread = null;

        String result = MiscTools.downloadTextFileFromHttp(webUrl, "");

        thread.close(); // unlock gui
        thread = null;

        XmlTemplate temp = new XmlTemplate();
        temp.setDocument(result);
        Document doc = temp.getDocument(new Document(new Element("empty")));

        //XMLTreeView view = new XMLTreeView(new JFrame(), true, doc);
        //view.setSize(640, 480);
        //view.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
        //view.setVisible(true);

        if (parentFrame != null) {
            Controller controller = parentFrame.getController();
            List dataList = controller.loadMpeg7FromXml(doc);
            drawRepresentants(dataList);
        }

        //return "not implemented";
    }*/




    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    public Rectangle getBackgroundImageSize() {
        return backgroundImageSize;
    }

    public void setBackgroundImageSize(Rectangle backgroundImageSize) {
        this.backgroundImageSize = backgroundImageSize;
    }

    public void setBackgroundImage(BufferedImage backgroundImage1) {
        backgroundImage = java2dTools.getBrighterImage(backgroundImage1);
        // backgroundImageSize is set in drawComponent
        //backgroundImage = backgroundImage1;
        cat.fine("set new brighter image as background");
        //java2dTools.showImage(backgroundImage1);
        validate();
        repaint();
    }

    public void dragEnter(DropTargetDragEvent event) {
        //cat.fine( "dtlistener dragEnter");
    }

    public void dragOver(DropTargetDragEvent event) {
        //cat.fine( "dtlistener dragOver");
    }

    public void dropActionChanged(DropTargetDragEvent event) {
        //cat.fine( "dtlistener dropActionChanged");
    }

    public void dragExit(DropTargetEvent event) {
        //cat.fine( "dtlistener dragExit");
    }

    public String toString() {
        return "nothing to report";
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //cat.fine("repain panel for " + parentFrame);

        // draw the background image
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g;
            //cat.fine("Drawing Background");

            //g2.clearRect(0, 0, this.getWidth(), this.getHeight());
            Rectangle boundary;
            boundary = (Rectangle) java2dTools.fitToWindow(this.getBounds(),
                            new Rectangle(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight()));
            //cat.fine("Drawing " + boundary.toString());
            backgroundImageSize = new Rectangle((int) boundary.getX(), (int) boundary.getY(),
                            (int) boundary.getWidth(), (int) boundary.getHeight());
            g2.drawImage(backgroundImage, backgroundImageSize.x, backgroundImageSize.y,
                    backgroundImageSize.width, backgroundImageSize.height, Color.WHITE, null);
        } else {
            backgroundImageSize = null;
        }

        //java2dTools.showImage(backgroundImage);

    }

    /**
     * remove a component from the drawpanel and reset the search-opeartor component correctly
     *
     * @param a_component
     */
    public void removeIComponent(IComponent a_component) {
        if (!isMathMode) {
            remove(a_component);
            return; //------------------ EXIT POINT -----------------------
        }

        List<IComponentStruct> comps = getIComponents();

        int pos = getComponentPosition(comps, a_component);
        assert(pos >= 0);

        if (pos == 0) {
            remove(a_component);
            if (comps.size() > 1) {
                remove(comps.get(1).getComponent());
            }
        } else {
            IComponentStruct thisComponent = comps.get(pos);
            assert(!thisComponent.isOperator()); // operators are deleted automatically

            IComponentStruct perviousOperator = comps.get(pos - 1);
            assert(perviousOperator.isOperator());

            IComponentStruct lastComponent = comps.get(pos - 2);
            assert(!lastComponent.isOperator());

            IComponentStruct nextOperator = null;
            if (pos < (comps.size() - 1)) {
                nextOperator = comps.get(pos + 1);
                assert(nextOperator.isOperator());
            }

            // delete component and its previous operator
            remove(perviousOperator.getComponent());
            remove(thisComponent.getComponent());

            // set next operatro to the previous component if available
            if (nextOperator != null) {
                ILine op = (ILine) nextOperator.getComponent();
                IComponent p1 = op.getParentComponent01();
                IComponent p2 = op.getParentComponent02();
                IComponent newP = lastComponent.getComponent();

                if (newP == p1) {
                    p2 = newP;
                } else {
                    p1 = newP;
                }

                op.setParentComponent01(p1);
                op.setParentComponent02(p2);
            }
        }

        layoutComponents(null);

    }

    private int getComponentPosition(List<IComponentStruct> comps, IComponent a_component) {
        int pos = -1;
        for (int i = 0; i < comps.size(); i++) {
            IComponentStruct struct = comps.get(i);
            if (struct.equals(a_component)) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    /**
     * auto layout the componenst in the drawpanel
     */
    public void layoutComponents(Dimension aDefaultDimension) {
        double w = this.getWidth();
        double h = this.getHeight();

        if (w == 0) {
            w = aDefaultDimension.getWidth();
            h = aDefaultDimension.getHeight();
        }

        if (h > 100) {
            h = 100;
        }

        double ch = h * .9;
        double cw = (h / 4) * 3;

        double lineMin = 50;
        double posX = 10;
        double posY = (h - ch) / 2;

        List<IComponentStruct> comps = getIComponents();

        for (int i = 0; i < comps.size(); i++) {
            IComponentStruct struct = comps.get(i);
            if (!struct.isOperator()) {
                IComponent comp = struct.getComponent();
                if (i > 0) { // works because first compnent is never a opertor
                    assert(!struct.isOperator());
                    posX += cw + lineMin;
                }
                double x = posX;
                double y = posY;
                comp.setComponentBounds((int) x, (int) y, (int) cw, (int) ch);
                //System.out.println("Layout [x, ,y, w, h]: " + (int) x + ", " + (int) y + ", " + (int) cw + ", " + (int) ch);
            }
        }

        Rectangle rect = this.getBounds();
        rect.setSize((int) posX, (int) rect.getHeight());
        this.setBounds(rect);
    }

    private List<IComponentStruct> getIComponents() {
        List<IComponentStruct> returnValue = new ArrayList<IComponentStruct>();
        Component[] comps = this.getComponents();
        //comps = CollectionTools.revertArray(comps);

        for (int counter = 0; counter < comps.length; counter++) {
            boolean isOperator = (ILine.class.isInstance(comps[counter]));
            IComponent comp = (IComponent) comps[counter];
            returnValue.add(new IComponentStruct(comp, isOperator));
        }

        return returnValue;
    }

    /**
     * if true, the panel is used to draw Multimedia objects and operator
     * this means, when deleting objects, the previous operator (ILine) is removed too
     *
     * @return true if the panel is used to draw Multimedia objects and operator, false otherwise
     */
    public boolean isMathMode() {
        return isMathMode;
    }

    /**
     * if true, the panel is used to draw Mulmiedia objects and operator
     * this means, when deleting objects, the previous operator (ILine) is removed too
     */
    public void setMathMode(boolean mathMode) {
        isMathMode = mathMode;
    }

    /*public void paint(Graphics g) {
       super.paint(g);
    }*/

}

class IComponentStruct {
    private IComponent component;
    private boolean isOperator;

    public IComponentStruct(IComponent component, boolean isOperator) {
        setComponent(component);
        setOperator(isOperator);
    }

    public boolean equals(IComponent a_component) {
        boolean returnValue;
        returnValue = (a_component == component);
        return returnValue;
    }

    public IComponent getComponent() {
        return component;
    }

    public void setComponent(IComponent component) {
        this.component = component;
    }

    public boolean isOperator() {
        return isOperator;
    }

    public void setOperator(boolean operator) {
        isOperator = operator;
    }
}