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
import at.wklieber.tools.*;
import org.jdom.Element;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Werner Klieber
 * @version 1.0
 */
public class ObjectPalette extends JDialog

{
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(ObjectPalette.class.getName());
    private static Console console = Console.getReference();
    //private static Java2dTools java2dTools = Java2dTools.getReference();
    private static Mpeg7ConversionTools mpeg7Convert = Mpeg7ConversionTools.getReference();
    private static Settings cfg = Settings.getReference();

    public static final int PANEL_COLOR = 0;
    public static final int PANEL_SHAPE = 1;
    public static final int PANEL_IMAGE = 2;
    public static final int PANEL_IMAGE_LIST = 3;
    public static final int PANEL_REPRESENTANT = 4;

    // size of one component to draw.
    // is limited by drawComponents to draw a label
    private int elementWidth = 40;
    private int elementHeigh = 60;
    private int gap = 5;

    JPanel mainPanel = null; // This panel covers the complete drawing Frame
    private Container contentPane = null; // the contentPane of the Frame
    private JDialog mainFrame = null;
    private IDrawPanel drawPanel = null;
    private Container toolbarWrapper = null;
    private JMenuBar menuBar = null;

    private JPanel statusPanel = null;
    private JToolBar toolBar = null;
    //private JComboBox imageCombo = null;

    private MenuTools menuTool = null;
    private JLabel statusBar;

    private IComponent[] imageArray = new IComponent[0];
    private IComponent[] representantArray = new IComponent[0];
    private IComponent[] colorArray = new IComponent[0];
    private IComponent[] shapeArray = new IComponent[0];
    private IComponent[] imageListArray = new IComponent[0];

    private java.util.List<ImageListStruct> imageListCache = null; // List of IComponent[] Pictures that are read in from directories
    private String imageListBaseDir = null;


    private int currentDisplay = -1;
    private static final String SELECT_SAMPLE_IMAGES = "Select sample images";
    private MouseListener parentMouseListener;
    //private int currentListEntry = -1;

    //static initializer for setting look & feel
    static {
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    public ObjectPalette() {
        super();
        init(null);
    }

    // does not work right: the elements are not drawn correctly
    public ObjectPalette(JPanel mainPanel) {
        super();
        init(mainPanel);
    }

    // use this to display only a own set of components
    // NOTE: not fully implemented yet. Will not work
    /* public ObjectPalette(IComponent[] componentArray1) {
         super();
         cat.fine("not fully implemented yet. Will not work");
         init(componentArray1);
     }*/

    private void init(JPanel a_mainFrame) {
        boolean isStandalone;
        parentMouseListener = null;

//        System.out.println("Main: " + a_mainFrame.getSize());

        if (a_mainFrame != null) {
            mainFrame = null;
            contentPane = a_mainFrame;
            isStandalone = false;
        } else {
            mainFrame = this;
            mainFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            mainFrame.setSize(new Dimension(400, 400));
            mainFrame.setTitle("Object Palette");
            contentPane = mainFrame.getContentPane();
            contentPane.setLayout(new BorderLayout());
            isStandalone = true;
        }
        //mainFrame.setVisible(true);



        if (mainPanel != null) {
            mainPanel.removeAll();
        }

        // initialize all member-menu Panels
        mainPanel = new JPanel(); // This panel covers the complete Frame
        mainPanel.setSize(contentPane.getSize());
        drawPanel = new IDrawPanel();
        drawPanel.setSize(contentPane.getSize());
        drawPanel.setAcceptDnd(false);
        menuBar = new JMenuBar();
        statusPanel = new JPanel();
        //buttonPanel = new JPanel();
        toolbarWrapper = new Container();
        LayoutManager mgr = new FlowLayout(FlowLayout.LEADING);
        toolbarWrapper.setLayout(mgr);

        //------------------------ add Elements ---------------------------
        String iconLocation = cfg.getIconsDir();
        menuTool = new MenuTools(this, menuBar, toolbarWrapper, iconLocation);
        setMenuEntries();

        //------------------------ define the Drawpanel ---------------------------
        //drawPanel.setMinimumSize(new Dimension(200, 200));
        drawPanel.setIgnoreRepaint(false);
        if (isStandalone) {
            drawPanel.setPreferredSize(new Dimension(400, 400));
        }
        drawPanel.setToolTipText("");
        //drawPanel.setBackground(Color.white);
        //drawPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        drawPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        drawPanel.setLayout(null);
        drawPanel.add(new Scrollbar(Scrollbar.VERTICAL));

        //--------------- link all panels to the frame
        mainPanel.setLayout(new BorderLayout());

        if (isStandalone) {
            mainFrame.setJMenuBar(menuBar);
            contentPane.add(toolbarWrapper, BorderLayout.NORTH);

            Component bottomPanel = buildBottomPanel();
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        }


        contentPane.add(mainPanel, BorderLayout.CENTER);
        mainPanel.add(drawPanel, BorderLayout.CENTER);


        if (isStandalone) {
            mainFrame.validate();
            mainFrame.repaint();
            //setIComponents(componentArray1);
            mainFrame.setVisible(true); // show before drawing objects, because otherwise they are not valid

            // show the representants as default
            //actionChooseRepresentant(null);
        } else {
//            System.out.println("Main: " + mainPanel.getSize());
//            System.out.println("content: " + contentPane.getSize());
//            System.out.println("drawPanel: " + drawPanel.getSize());
            actionChooseColor(null);  // choose the color-objects as default
            contentPane.validate();
            contentPane.repaint();
            contentPane.setVisible(true);
//            System.out.println("Main: " + mainPanel.getSize());
//            System.out.println("content: " + contentPane.getSize());
//            System.out.println("drawPanel: " + drawPanel.getSize());

        }
    }  // end method init

    private Component buildBottomPanel() {
        statusBar = new JLabel(" Idle...");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        return statusBar;
    }

    public JPanel getDrawPanel() {
        return (JPanel) drawPanel;
    }


    // inserts the menu and toolbar stuff
    private void setMenuEntries() {
        //menuTool.readMenuFromConfigFile("objectDialog", MenuTools.TYPE_MENUBAR);

        //---- add a combobox with all directories in the image directory to the menu

        /*if (imageListCache == null) {
            imageListCache = new Vector<ImageListStruct>();
        }

        java.util.Vector<String> lastDirEntry = new Vector<String>();
        lastDirEntry.add(SELECT_SAMPLE_IMAGES);
        imageListBaseDir = cfg.getImageDir();

        String[] dirs = new String[0]; // Array with all sub directory names*/
        /*if (cfg.isStartedAsJarFile()) {
            String archive = cfg.getBaseFile();
            String directory = cfg.getImageDir();
            String dir = directory.substring(archive.length());

            archive = archive.substring(0, archive.length() - 2);
            try {
                dirs = UnzipTools.getFileList(archive, dir, false);
            } catch (Exception e) {
                cat.fine(e);
            }
        } else {*/
        //dirs = FileTools.subDirList(new FileParserSettings(imageListBaseDir, "*.*"));
        //}

        // sort out directories containing no images
        /*java.util.List<String> dirList = new Vector<String>(dirs.length, 10);
        for (int i = 0; i < dirs.length; i++) {
            String[] dummy = getImagesNamesFromDirectory(imageListBaseDir + dirs[i] + "/");
            if (dummy.length > 0) {
                dirList.add(dirs[i]);
            }
        }
        dirs = (String[]) dirList.toArray(new String[dirList.size()]);


        for (int i = 0; i < dirs.length; i++) {
            lastDirEntry.add(dirs[i]);
            //cat.fine("Como add: " + dirs[i]);
            ImageListStruct data = new ImageListStruct(new IComponent[0], dirs[i]);
            imageListCache.add(data);
        }

*/
        /*imageCombo = new JComboBox(lastDirEntry);

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selected = (String) cb.getSelectedItem();
                int index = (int) cb.getSelectedIndex();
                actionImageArraySelected(selected, index);
            }
        };
        imageCombo.addActionListener(listener);
        menuTool.addToolBarEntry(imageCombo);
*/

        //menuBar.add(imageCombo);
    }

    /**
     * an entry of the compobox has been clicked
     */
    private void actionImageArraySelected(String selected, int index) {
        //cat.fine("Selected: " + index);
        //drawPanel.removeAll();
        if (index < 0) {
            //autodetect image:
            for (int i = 0; i < imageListCache.size(); i++) {
                ImageListStruct struct = imageListCache.get(i);
                if (struct.getTitle().equalsIgnoreCase(selected)) {
                    index = i;
                    break;
                }
            }

            //return;
        }

        if (index == 0 && selected.equalsIgnoreCase(SELECT_SAMPLE_IMAGES)) {
            return;
        }

        currentDisplay = PANEL_IMAGE_LIST;
        //currentListEntry = index;


        IComponent[] compArray = null;
        if (index < imageListCache.size()) {
            try {
                ImageListStruct data = imageListCache.get(index);
                compArray = data.getImages();
            } catch (Exception e) {
                cat.fine(e.toString());
            }
        }

        if (compArray == null || compArray.length == 0) {
            compArray = getImagesFromDirectory(imageListBaseDir + selected + "/");
            ImageListStruct data = new ImageListStruct(new IComponent[0], selected);
            imageListCache.add(index, data);
        }

        imageListArray = compArray;
        //validateTree();
        //repaint();
        //drawComponents();
        mainFrame.validate();
        mainFrame.repaint();

    }

    public void actionSelectImageArray(String entryName) {
        actionImageArraySelected(entryName, -1);
    }

    // import images from a user given directory to the combobox
    public void actionImportImages(ActionEvent e) {
        //cat.fine("Selected: " + index);
        //drawPanel.removeAll();
        //String selected = "5";

        String dir = FileTools.showOpenDialog("", "jpg", "Select the image import Directory", true, "");
        dir = FileTools.getFilePath(dir);
        cat.fine("DIR: " + dir);
        if (dir.length() == 0) {
            return;
        }

        imageFromDirToPalette(dir);
    }

    // import images from a given directory to the combobox
    private void imageFromDirToPalette(String dir) {
        if (getImagesNamesFromDirectory(dir, false).length < 1) {
            MessageBox.displayMessage("Information", "No images found in directory \"" + dir + "\"");
            return;
        }

        String[] filePathNameList = FileTools.parsePathName(dir);

        String entryName = "Unknown";
        if (filePathNameList.length > 0) {
            entryName = filePathNameList[filePathNameList.length - 1];
        }

        currentDisplay = PANEL_IMAGE_LIST;
        //int index = imageCombo.getItemCount(); // index is zero based.
        //imageCombo.addItem(entryName);
        //imageCombo.setSelectedIndex(index);

        /*ImageListStruct data = new ImageListStruct(new IComponent[0], entryName);
        imageListCache.add(data); // just for updating the reading process
        IComponent[] images = getImagesFromDirectory(dir, false);
        data = new ImageListStruct(images, entryName);
        //currentListEntry = index; // not really used
        imageListCache.add(index, data);  // add new IComponent[]
        imageListArray = images;   // current components to draw
*/

        //validateTree();
        //repaint();
        //drawComponents();
        mainFrame.validate();
        mainFrame.repaint();

    }


    public void actionChooseColor(ActionEvent e) {
        if (currentDisplay == PANEL_COLOR)
            return;

        if (colorArray.length == 0) {
            colorArray = new IRectangle[12];
            colorArray[0] = new IColorRectangle(drawPanel, Color.BLACK, Color.WHITE, "Black");
            colorArray[1] = new IColorRectangle(drawPanel, Color.DARK_GRAY, Color.WHITE, "Dark gray");
            colorArray[2] = new IColorRectangle(drawPanel, Color.LIGHT_GRAY, Color.WHITE, "Light gray");
            colorArray[3] = new IColorRectangle(drawPanel, Color.BLUE, Color.WHITE, "Blue");
            colorArray[4] = new IColorRectangle(drawPanel, Color.MAGENTA, Color.WHITE, "Magenta");
            colorArray[5] = new IColorRectangle(drawPanel, Color.GREEN, Color.WHITE, "Green");
            colorArray[6] = new IColorRectangle(drawPanel, Color.CYAN, Color.WHITE, "Cyan");
            colorArray[7] = new IColorRectangle(drawPanel, Color.ORANGE, Color.WHITE, "Orange");
            colorArray[8] = new IColorRectangle(drawPanel, Color.PINK, Color.WHITE, "Pink");
            colorArray[9] = new IColorRectangle(drawPanel, Color.RED, Color.WHITE, "Red");
            colorArray[10] = new IColorRectangle(drawPanel, Color.YELLOW, Color.WHITE, "Yellow");
            colorArray[11] = new IColorRectangle(drawPanel, Color.WHITE, Color.WHITE, "White");
        }

        //drawPanel.removeAll();
        currentDisplay = PANEL_COLOR;
        drawComponents();
        validateTree();
        repaint();
        //System.out.println("comp: " + colorArray[0].getSize());
    }

    public void actionChooseShape(ActionEvent e) {
        if (currentDisplay == PANEL_SHAPE)
            return;

        if (shapeArray.length == 0) {
            //shapeArray = new IShape[0];
            //colorArray[0] = new IColorRectangle(drawPanel, Color.BLACK, Color.WHITE);
            IAccessFile config = cfg.getConfigAccess();
            java.util.List shapeElements = config.getProperties("imbConfig/userInterface/shapes/*");
            shapeArray = new IShape[shapeElements.size()];
            int counter = 0;
            for (Iterator it = shapeElements.iterator(); it.hasNext(); counter++) {
                Element elem = (Element) it.next();
                String name = elem.getName();
                cat.fine("Shape Name: " + name);

                String[] shapePoints = config.getProperties("imbConfig/userInterface/shapes/" + elem.getName(), "point", new String[0]);

                IComponentData data = new IComponentData(new IShape());
                data.setDotList(mpeg7Convert.stringArray2PointList(shapePoints));
                data.setBoundary(new Rectangle(0, 0, elementWidth, elementHeigh));
                data.setName(elem.getName());
                IShape iShape = new IShape(drawPanel, data);
                iShape.setDrawDots(false);
                iShape.setResizeable(false);
                iShape.setMoveable(false);
                iShape.setDoDnd(true);
                shapeArray[counter] = iShape;
            }
        } // end if

        //drawPanel.removeAll();
        currentDisplay = PANEL_SHAPE;
        drawComponents();
        validateTree();
        repaint();
    }

    private void updateStatusBar(String text) {
        statusBar.setText(text);
        statusBar.repaint();
    }


    protected IComponent[] getImagesFromDirectory(String directory1) {
        return getImagesFromDirectory(directory1, true);
    }

    protected IComponent[] getImagesFromDirectory(final String directory1, boolean doGetBaseAutomatic1) {
        //cat.fine("Dir:" + directory1);

        String[] fileArray = getImagesNamesFromDirectory(directory1, doGetBaseAutomatic1);

        final String[] files = fileArray;
        final IComponent[] iArray = new IRectangle[files.length];

        final ObjectPalette thisClass = this;
        //imageArray = iArray;
        Runnable doWorkRunnable = new Runnable() {
            public void run() {
                for (int i = 0; i < files.length; i++) {
                    //cat.fine("file: " + files[i]);
                    updateStatusBar("read image " + (i + 1) + "/" + files.length + " (" + files[i] + ")");

                    String fileName = FileTools.resolvePath(directory1 + files[i], false);
                    iArray[i] = new IImageComponent(drawPanel, fileName);

                    // this class as listener
                    if (parentMouseListener != null) {
                        iArray[i].addMouseListener(parentMouseListener);
                    }

                    drawPanel.validate();
                    mainFrame.repaint();
                }
                updateStatusBar("Idle...");
            }
        };
        //SwingUtilities.invokeLater(doWorkRunnable);

        Thread thread = new Thread(doWorkRunnable);
        thread.start();


        drawPanel.validate();
        mainFrame.repaint();

        return iArray;
    }


    /**
     * @param directory1
     * @param doGetBaseAutomatic1
     * @return a STring array with all image names in the given directory
     */
    private String[] getImagesNamesFromDirectory(String directory1, boolean doGetBaseAutomatic1) {
        cat.fine("Dir:" + directory1);
        String[] returnValue = new String[0];

        try {
            String[] fileArray = new String[0];
            /* if (doGetBaseAutomatic1 && cfg.isStartedAsJarFile()) {
                 String archive = cfg.getBaseFile();
                 archive = archive.substring(0, archive.length() - 2);
                 String dir = directory1.substring(archive.length() + 2);
                 //cat.fine("Load from Jar: " + archive + ", dir: " + dir);

                 fileArray = UnzipTools.getFileList(archive, dir, true);
                 //cat.fine("GO:" + fileArray.length);
                 for (int i = 0; i < fileArray.length; i++) {
                     cat.fine(fileArray[i]);
                 }
             } else {*/
            //fileArray = FileTools.fileList(directory1, "*.jpg");
            if (FileTools.exists(directory1)) {
                fileArray = FileTools.fileList(new FileParserSettings(directory1, "*.jpg"));
            }
            //cat.fine("GO:" + fileArray.length);
            //}

            returnValue = fileArray;
        } catch (Exception e) {
            e.printStackTrace();
            cat.severe(e.toString());
        }

        return returnValue;
    }

    private String[] getImagesNamesFromDirectory(String directory1) {
        return getImagesNamesFromDirectory(directory1, true);
    }


    /*public void actionChooseImage(ActionEvent e) {
        if (currentDisplay == PANEL_IMAGE)
            return;

        currentDisplay = PANEL_IMAGE;
        if (imageArray.length == 0) {
            String location = cfg.getImageDir();

            imageArray = getImagesFromDirectory(location);
            String[] files = FileTools.fileList(location, "*.jpg");
            imageArray = new IRectangle[files.length];
            for(int i = 0; i < files.length; i++) {
               cat.fine("file: " + files[i]);

            }


            imageArray = new IRectangle[2];


            String fileName = FileTools.resolvePath(location + "batman.jpg", false);
            imageArray[0] = new IImageComponent(drawPanel, fileName);

            fileName = FileTools.resolvePath(location + "lara croft.jpg", false);
            imageArray[1] = new IImageComponent(drawPanel, fileName);
        }

        //drawPanel.removeAll();

        drawComponents();
        validateTree();
        repaint();
    }*/

    /* public void actionChooseRepresentant(ActionEvent e) {
         if (currentDisplay == PANEL_REPRESENTANT)
             return;

         currentDisplay = PANEL_REPRESENTANT;
         if (representantArray.length == 0) {
             String location = cfg.getResentantImageDir();
             String fileName;

             representantArray = new IRepresentant[5];
             IRepresentant representant;

             fileName = FileTools.resolvePath(location + "camera_movement.jpg", false);
             representant = new IRepresentant(drawPanel, IRepresentant.TYPE_CAMERA_MOTION);
             representant.setName("Camera Movement");
             representant.setEditable(false);
             representantArray[0] = representant;

             fileName = FileTools.resolvePath(location + "sketch.jpg", false);
             representant = new IRepresentant(drawPanel, IRepresentant.TYPE_SKETCH);
             representant.setName("Sketch");
             representant.setEditable(false);
             representantArray[1] = representant;


             fileName = FileTools.resolvePath(location + "semantik.jpg", false);
             representant = new IRepresentant(drawPanel, IRepresentant.TYPE_SEMANTIC_DESCRIPTION);
             representant.setName("Semantic");
             representant.setEditable(false);
             representantArray[2] = representant;

             fileName = FileTools.resolvePath(location + "text.jpg", false);
             representant = new IRepresentant(drawPanel, IRepresentant.TYPE_TEXT_DESCRIPTION);
             representant.setName("Text");
             representant.setEditable(false);
             representantArray[3] = representant;


             fileName = FileTools.resolvePath(location + "audio.jpg", false);
             representant = new IRepresentant(drawPanel, IRepresentant.TYPE_SOUND_FILE);
             representant.setName("Audio");
             representant.setEditable(false);
             representantArray[4] = representant;
         }

         drawComponents();
         validateTree();
         repaint();
     }*/


    protected void addOtherComponent(IComponent component1) {
        drawPanel.add(component1);
        component1.addMouseListener(component1);
        component1.addMouseMotionListener(component1);

        component1.revalidate();

    }

    public void clearComponents() {
        drawPanel.removeAll();
    }

    /*public void setIComponents(IComponent[] componentArray1) {
       imageArray = componentArray1;
       drawComponents();
    }*/

    // called to reorganize the drawing Elements
    private void drawComponents() {
        clearComponents();

        // get the component array that has to be drawn
        IComponent[] componentArray1 = new IComponent[0];
        switch (currentDisplay) {
            case PANEL_COLOR:
                componentArray1 = colorArray;
                break;
            case PANEL_SHAPE:
                componentArray1 = shapeArray;
                break;
            case PANEL_IMAGE:
                componentArray1 = imageArray;
                break;
            case PANEL_IMAGE_LIST:
                componentArray1 = imageListArray;
                break;
            case PANEL_REPRESENTANT:
                componentArray1 = representantArray;
                break;

        }

        // estimate all needed sizes
        int textHeigh = 20;
        int componentWidth = elementWidth;
        int componentHeigh = elementHeigh - textHeigh;


        int maxColums = (drawPanel.getWidth() / (elementWidth + gap));
        maxColums = 2;

        int row = 0;
        int column = 0;
        try {
            for (int i = 0; i < componentArray1.length; i++) {
                //cat.fine("Add new image nr. " + i + ", name " + imageArray[i].toString());
                //ImageComponent imageComponent = new ImageComponent((JPanel) mainContainer, imageList[i]);
                //System.out.println("Added: " + imageComponent.toString());

                IComponent component = componentArray1[i];
                if (component == null) {
                    //cat.fine("null:" + i);
                    continue;
                }

                int x = gap + (column * (elementWidth + gap));
                int y = gap + (row * (elementHeigh + gap));
                int w = componentWidth;
                int h = componentHeigh;

                component.setDrawBorder(true);
                component.setComponentBounds(x, y, w, h);
                component.setSize(w, h);
                addOtherComponent(component);

                // draw the label
                String labelText = component.getName();
                //labelText = "Hello";
                JLabel label = new JLabel(labelText);
                label.setBounds(x, (y + componentHeigh),
                        componentWidth, textHeigh);
                label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                drawPanel.add(label);
                //cat.fine("comp: " + component.getComponentBounds().toString() + "; Label: " + label.toString());

                if (column < (maxColums - 1)) {
                    column++;
                } else {
                    column = 0;
                    row++;
                }

            } // end for
            validate();


        } catch (Exception e) {
            cat.severe(e.toString());
            e.printStackTrace();
        }
    }


    public void paint(Graphics g) {
        //Graphics2D g2 = (Graphics2D) g;

        drawComponents(); // we have to paint ourself
        super.paint(g);

        validate();
    } // end method paint


    /**
         * set a mouselistenr for mouseEvents on the IComponents
         *
         * @param mouseListner
         */
    public void setListenerForIComponentMouseMovement(MouseListener mouseListner) {
        parentMouseListener = mouseListner;
    }

} // end class

// data needed to store images and titles for one entry in the image list combo

class ImageListStruct {
    private IComponent[] images;
    private String title;

    public ImageListStruct(IComponent[] images, String title) {
        this.images = images;
        this.title = title;
    }

    public IComponent[] getImages() {
        return images;
    }

    public void setImages(IComponent[] images) {
        this.images = images;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}