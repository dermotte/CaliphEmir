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

package at.lux.fotoannotation;

import at.knowcenter.caliph.objectcatalog.semanticscreator.IMBeeApplicationPanel;
import at.lux.components.ImageThumbPanel;
import at.lux.components.StatusBar;
import at.lux.fotoannotation.dialogs.*;
import at.lux.fotoannotation.mpeg7.Mpeg7ImageDescription;
import at.lux.fotoannotation.mpeg7.Mpeg7ThumbnailMediaProfile;
import at.lux.fotoannotation.panels.*;
import at.lux.fotoannotation.utils.TextChangesListener;
import at.lux.splash.SplashScreen;
import at.wklieber.LoggerInit;
import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Properties;

/**
 * This Class is the main JFrame where the application is started.
 *
 * @author Mathias Lux, mathias@juggle.at
 * @version 0.9 alpha
 */

public class AnnotationFrame extends JFrame implements ActionListener, StatusBar {
    public static final boolean DEBUG = false;
    public static boolean DIRTY = false;
    public static String TITLE_BAR = AnnotationToolkit.PROGRAM_NAME + " " + AnnotationToolkit.PROGRAM_VERSION;
    private AnnotationFrameProperties properties;
    private ImageThumbPanel imagePanel;
    private MetadataDescriptionPanel mdPanel;
    private QualityPanel qualityPanel;
    private TextDescriptionPanel textPanel;
    private ShapePanel shapePanel;
    // private AgentComboBoxModel agentsModel;
    private File currentFile = null;
    private JSplitPane lrSplit, tbSplit;
    private JTabbedPane tabs;
    private CreationPanel creationPanel;
    private DecimalFormat df;
    private JLabel status;
    private IMBeeApplicationPanel beePanel;
    private ColorLayoutPanel colorPanel;
    private JPanel gridPanel1;
    private JProgressBar garbageState;
    private GarbageTracker gtracker;
    private FileTreeModel fileTreeModel = new FileTreeModel();
    private JTree fileTree = new JTree(fileTreeModel);
    private JComboBox fileComboBox;
    private Properties currentFileProperties = new Properties();

    private boolean autopilot = false;
    private boolean systemExitOnWindowClosing = true;

    public AnnotationFrame() {
        super(TITLE_BAR);
        init();
    }

    public AnnotationFrame(boolean systemExitOnWindowClosing) {
        super(TITLE_BAR);
        this.systemExitOnWindowClosing = systemExitOnWindowClosing;
        init();
    }

    private void init() {
        try {
            LoggerInit.configureLog4j("log4j.properties", "INFO");
            this.setIconImage(ImageIO.read(AnnotationFrame.class.getResource("data/caliph-icon.png")));
//            this.setIconImage(ImageIO.read(AnnotationFrame.class.getResource("data/icon.gif")));
        } catch (Exception e) {
            debug("Couldn't set Icon: IOException " + e.getMessage());
        }
        df = (DecimalFormat) DecimalFormat.getInstance();
        df.setMaximumFractionDigits(2);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);
                exitApplication();
            }
        });
        // -------------------------------------------------------------------------------
        // Ueberpruefung ob das agent file existiert, sonst muss es angelegt werden ...
        // Eine sicherheitskopie befindet sich im package ...
        // -------------------------------------------------------------------------------
        debug("Checking if agentfile is here ...");
        File agentsFile = new File(AnnotationToolkit.AGENTS_FILE);
        if (!agentsFile.exists()) {
            try {
                debug("Generating sample agents file");
                Document d = new SAXBuilder().build(AnnotationFrame.class.getResource("data/agents.mp7.xml"));
                FileOutputStream fos = new FileOutputStream(agentsFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
                new XMLOutputter(Format.getPrettyFormat()).output(d, osw);
                osw.close();
                fos.close();
                debug("Finished generating sample agents file");
            } catch (Exception e) {
                debug("Error generating sample agents file " + e.toString() + ", " + e.getMessage());
            }
        } else {
            debug("agentfile found :)");
        }
        debug("reading configurationfile ... ");
        // -------------------------------------------------------------------------------
        // Ueberpruefung ob das base-object file existiert, sonst muss es angelegt werden.
        // Eine sicherheitskopie befindet sich im package ...
        // -------------------------------------------------------------------------------
        debug("Checking if agentfile is here ...");
        File baseobjectsFile = new File("base-objects.mp7.xml");
        if (!baseobjectsFile.exists()) {
            try {
                debug("Generating sample base-objects file");
                Document d = new SAXBuilder().build(AnnotationFrame.class.getResource("data/base-objects.mp7.xml"));
                FileOutputStream fos = new FileOutputStream(baseobjectsFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
                new XMLOutputter(Format.getPrettyFormat()).output(d, osw);
                osw.close();
                fos.close();
                debug("Finished generating sample base-objects file");
            } catch (Exception e) {
                debug("Error generating sample base-objects file " + e.toString() + ", " + e.getMessage());
            }
        } else {
            debug("base-objects file found :)");
        }
        debug("reading configurationfile ... ");
        // -------------------------------------------------------------------------------
        // Ueberpruefung ob das property file existiert, sonst muss es angelegt werden ...
        // Eine sicherheitskopie befindet sich im package ...
        // -------------------------------------------------------------------------------
        File pFile = new File("properties.xml");
        if (!pFile.exists()) {
            try {
                debug("Generating sample property file");
                Document d = new SAXBuilder().build(AnnotationFrame.class.getResource("data/properties.xml"));
                FileOutputStream fos = new FileOutputStream(pFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
                new XMLOutputter(Format.getPrettyFormat()).output(d, osw);
                osw.close();
                fos.close();
                debug("Finished generating sample property file");
            } catch (Exception e) {
                debug("Error generating sample property file " + e.toString() + ", " + e.getMessage());
            }
        }
        properties = new AnnotationFrameProperties(new File("properties.xml"), this);
        debug("finished reading configurationfile");
        JMenuBar menuBar = properties.getMenuBar();
        // For JGoodies Look & Feel:
        menuBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        menuBar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);
        this.setJMenuBar(menuBar);
        debug("finished creating menu");

        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getNewLeadSelectionPath().getLastPathComponent();
                if (node.getUserObject() instanceof FileNodeObject) {
                    File f = ((FileNodeObject) node.getUserObject()).getContent();
                    if (!f.isDirectory()) {
                        try {
                            setCurrentFile(f);
                        } catch (IOException e) {
                            System.err.println("Error opening file: " + f.toString());
                            System.err.println(e.toString());
                            //                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        setToCurrentLocation(fileTree);

        // -------------------------------------
        // Initialising main Objects ...
        // -------------------------------------
        // TextChangeListener intialization for detecting changes of the document.
        TextChangesListener.createInstance(this);
        // -------------------------------------
        // Adding Components
        // -------------------------------------

        beePanel = new IMBeeApplicationPanel(this);
        colorPanel = new ColorLayoutPanel();

        lrSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        lrSplit.setBorder(BorderFactory.createEmptyBorder());
        lrSplit.setDividerSize(3);
        tbSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tbSplit.setBorder(BorderFactory.createEmptyBorder());
        tbSplit.setDividerSize(3);

        mdPanel = new MetadataDescriptionPanel(new AgentComboBoxModel(this));
        imagePanel = new ImageThumbPanel();
//        fp = new FilePanel(new File("."), this);
        qualityPanel = new QualityPanel(new AgentComboBoxModel(this));
        textPanel = new TextDescriptionPanel(this);
        creationPanel = new CreationPanel(new AgentComboBoxModel(this));
        shapePanel = new ShapePanel(this);

        JPanel qualTextPanel = new JPanel(new BorderLayout());
        qualTextPanel.add(qualityPanel, BorderLayout.SOUTH);
        qualTextPanel.add(textPanel, BorderLayout.CENTER);
        gridPanel1 = new JPanel(new GridLayout(0, 2));
        JPanel _leftPanel1 = new JPanel(new BorderLayout());
//        gridPanel2.add(textPanel);
        _leftPanel1.add(qualTextPanel, BorderLayout.CENTER);
        _leftPanel1.add(mdPanel, BorderLayout.SOUTH);
        gridPanel1.add(_leftPanel1);
        gridPanel1.add(creationPanel);
        JPanel gridPanel3 = new JPanel(new GridLayout(0, 1));
        gridPanel3.add(colorPanel);
//        gridPanel3.add(qualTextPanel);

        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBorder(BorderFactory.createEmptyBorder());
        tabs.add("Image Information", gridPanel1);
        tabs.add("Semantics", beePanel);
        tabs.add("Shape", shapePanel);
        tabs.add("Visuals", gridPanel3);

        // file tree ....
        // ---------------
        JScrollPane fileTreeScrollPane = new JScrollPane(fileTree);
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(fileTreeScrollPane, BorderLayout.CENTER);
        // TODO: remove error here ...
        fileComboBox = new JComboBox(File.listRoots());
        fileComboBox.setActionCommand("rootChanged");
        fileComboBox.addActionListener(this);
        filePanel.add(fileComboBox, BorderLayout.NORTH);
        tbSplit.add(filePanel, JSplitPane.TOP);
        tbSplit.add(imagePanel, JSplitPane.BOTTOM);

        lrSplit.add(tbSplit, JSplitPane.LEFT);
        lrSplit.add(tabs, JSplitPane.RIGHT);

        lrSplit.setDividerLocation(properties.getLrSplit());
        tbSplit.setDividerLocation(properties.getTbSplit());

        status = new JLabel(TITLE_BAR);
        status.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        garbageState = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        garbageState.setStringPainted(true);
        garbageState.setPreferredSize(new Dimension(100, 18));
        garbageState.setToolTipText("This bar shows the memory allocated by the VM and how much of it is already in use.");
        garbageState.setBorder(BorderFactory.createEmptyBorder());
        gtracker = new GarbageTracker(garbageState);

        JPanel sgcPanel = new JPanel(new BorderLayout());
        sgcPanel.add(garbageState, BorderLayout.EAST);
        sgcPanel.add(status, BorderLayout.CENTER);

        this.getContentPane().add(lrSplit, BorderLayout.CENTER);
        this.getContentPane().add(sgcPanel, BorderLayout.SOUTH);

        this.setSize(properties.getFrameWidth(), properties.getFrameHeigth());
        this.setLocation(properties.getFrameLocationX(), properties.getFrameLocationY());

        beePanel.reArrange();
        //beePanel.revalidate();
        gtracker.start();
        // call init from global configuration while splash is on screen :)
        IconCache.getInstance();
    }

    private void setToCurrentLocation(JTree fileTree) {
        // Todo select the node with the current location and show and select it.
//        TreePath p = new TreePath(pathObjects);
//        fileTree.setSelectionPath(p);
//        fileTree.treeDidChange();
    }

    /**
     * Main method used to embedGraph Caliph
     *
     * @param args
     */
    public static void main(String[] args) {
        JWindow w = new JWindow();
        try {
            w = new JWindow();
            BufferedImage img = ImageIO.read(AnnotationFrame.class.getResourceAsStream("data/SplashCaliph.png"));
            FullSizeImagePanel panel = new FullSizeImagePanel(img);
            w.getContentPane().add(panel, BorderLayout.CENTER);
            w.pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            w.setLocation((screenSize.width - w.getWidth()) / 2, (screenSize.height - w.getHeight()) / 2);
            w.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        UIManager.put("Application.useSystemFontSettings", Boolean.TRUE);
//        PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
        try {
            UIManager.setLookAndFeel(new PlasticLookAndFeel());
        } catch (Exception e) {
            System.err.println("Could not set Look & Feel: " + e.toString());
        }
        boolean showSplash = true;

        // get config file from splashscreen class
        File splashProps = new File(SplashScreen.LICENSE_ACCEPTED_FILENAME);
        // look if exists
        if (splashProps.exists()) {
            // load props
            try {
                Properties licenseAcceptedProps = new Properties();
                licenseAcceptedProps.load(new FileInputStream(splashProps));
                // if property is set we do not have to show the splashscreen:
                String tmp = licenseAcceptedProps.getProperty("license.accepted");
                if (tmp != null && tmp.equals("true")) {
                    showSplash = false;
                }
            } catch (IOException e) {
                System.err.println("Warn: Could not read license properties file.");
            }
        }
        // if there is some problem starting Caliph the program exits
        // and gives some error message:
        try {
            AnnotationFrame frame = new AnnotationFrame();
            if (showSplash) {
                // now show the splash screen if this has not been done before.
                SplashScreen splash = new SplashScreen(frame);
                splash.setVisible(true);
            }
            if (w != null) w.setVisible(false);
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, "Error: " + e.toString(), "Error starting Caliph!", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * implemented from Interface ActionListener
     */
    public void actionPerformed(ActionEvent event) {
//        gtracker.run();
        String actionCommand = event.getActionCommand();
        if (actionCommand.equals("exitApplication")) {
            exitApplication();
        } else if (actionCommand.equals("openFile")) {
            debug("open file operation initiated");
            openFile();
            debug("open file operation ended");
        } else if (actionCommand.equals("showAbout")) {
            AboutDialog adialog = new AboutDialog(this);
            adialog.pack();
            adialog.setResizable(false);
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            adialog.setLocation((ss.width - adialog.getWidth()) / 2, (ss.height - adialog.getHeight()) / 2);
            adialog.setVisible(true);
        } else if (actionCommand.equals("visitHomepage")) {
            openUrlInBrowser(properties.getUrlHomepage());
        } else if (actionCommand.equals("showHelpOnline")) {
            String helpURL = properties.getUrlHelp();
            openUrlInBrowser(helpURL);
        } else if (actionCommand.equals("showHelp")) {
            HelpDialog adialog = new HelpDialog(this);
            // adialog.pack();
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            adialog.setLocation((ss.width - adialog.getWidth()) / 2, (ss.height - adialog.getHeight()) / 2);
            adialog.setVisible(true);
        } else if (actionCommand.equals("saveFileAs")) {
            JOptionPane.showMessageDialog(this, "not implemented yet");
        } else if (actionCommand.equals("gc")) {
            debug("Mem: "
                    + df.format(Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) + "MB of "
                    + df.format(Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) + "MB free");

            debug("starting garbage collector ...");
            System.gc();
            debug("finished collecting garbage!");

            debug("Mem: "
                    + df.format(Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) + "MB of "
                    + df.format(Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) + "MB free");
            status.setText("Garbage collection finished: "
                    + df.format(Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) + "MB of "
                    + df.format(Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) + "MB free");
//            gtracker.embedGraph();

        } else if (actionCommand.equals("viewXML")) {
            if (getCurrentFile() == null) {
                JOptionPane.showMessageDialog(this, "No image has been selected.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            TextPreviewDialog preview = new TextPreviewDialog(this, createDocument());
            preview.setSize(properties.getTextViewWidth(), properties.getTextViewHeight());
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            preview.setLocation((ss.width - preview.getWidth()) / 2, (ss.height - preview.getHeight()) / 2);
            preview.setVisible(true);
            properties.setTextViewHeight(preview.getHeight());
            properties.setTextViewWidth(preview.getWidth());
        } else if (actionCommand.equals("saveFile")) {
            if (currentFile != null) {
                saveFile();
            } else {
                JOptionPane.showMessageDialog(this, "No image selected!");
            }
        } else if (actionCommand.equals("viewExternal")) {
            if (currentFile != null && properties.getExternalViewer() != null) {
                try {
                    debug("Run: " + properties.getExternalViewer() + " \"" + currentFile.getCanonicalPath() + "\"");
                    Runtime.getRuntime().exec(properties.getExternalViewer() + " \"" + currentFile.getCanonicalPath() + "\"");
                } catch (IOException e) {
                    debug("Error with external viewer - " + e.toString() + ": " + e.getMessage());
                }
            }
        } else if (actionCommand.equals("setExternal")) {
            ExternalViewerSelectDialog d = new ExternalViewerSelectDialog(this, properties.getExternalViewer());
            d.pack();
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            d.setLocation((ss.width - d.getWidth()) / 2, (ss.height - d.getHeight()) / 2);
            d.setVisible(true);

            properties.setExternalViewer(d.getExternalViewer());
        } else if (actionCommand.equals("autoPilot")) {
            if (currentFile != null) {
                startAutoPilot();
            }
        } else if (actionCommand.equals("rootChanged")) {
            File f = (File) fileComboBox.getSelectedItem();
            fileTreeModel.setRoot(f);
        } else if (actionCommand.equals("delAgent")) {
            DeleteAgentDialog d = new DeleteAgentDialog(this);
            d.pack();
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            d.setLocation((ss.width - d.getWidth()) / 2, (ss.height - d.getHeight()) / 2);
            d.setVisible(true);

        } else {
            JOptionPane.showMessageDialog(this, "Not Implemented!");
        }
    }

    private void openUrlInBrowser(String url) {
        String osName = System.getProperty("os.name");
        // take linux settings
        String browserCmd = properties.getBrowserLinux();
        // or windows in case of windows :)
        if (osName.toLowerCase().indexOf("windows") > -1) {
            browserCmd = properties.getBrowserWindows();
        }
        browserCmd = browserCmd.replace("{url}", url);
        try {
            Runtime.getRuntime().exec(browserCmd);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not start browser. Please surf to "
                    + url + " to visit the online help.");
        }
    }

    /**
     * is called when applikation is exited ...
     * saving properties and closing app.
     * todo: save file? dialog
     */
    private void exitApplication() {
        properties.setFrameHeigth(this.getHeight());
        properties.setFrameWidth(this.getWidth());
        properties.setFrameLocationX(this.getLocation().x);
        properties.setFrameLocationY(this.getLocation().y);
        properties.setTbSplit(tbSplit.getDividerLocation());
        properties.setLrSplit(lrSplit.getDividerLocation());
        debug("saving configuration ...");
        properties.saveConfiguration();
        debug("finished saving configuration");
        debug("saving semantic objects ...");
        beePanel.saveCatalog();
        debug("semantic objects saved");
        if (DIRTY) {
            if (askIfSave()) {
                if (systemExitOnWindowClosing) {
                    System.exit(0);
                } else {
                    setVisible(false);
                }
            }
        } else {
            if (systemExitOnWindowClosing) {
                System.exit(0);
            } else {
                setVisible(false);
            }
        }
    }

    private boolean askIfSave() {
        int returnVal = JOptionPane.showConfirmDialog(this, "Would you like to save your changes? Otherwise your changes will be lost.", "Save description?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (returnVal == JOptionPane.YES_OPTION) {
            debug("Save file ...");
            saveFile();
            return true;
        } else if (returnVal == JOptionPane.NO_OPTION) {
            debug("Quit without saving ...");
            return true;
        } else {
            debug("no quitting, no saving :)");
            return false;
        }
    }

    private void startAutoPilot() {
        AutoPilotThread t = new AutoPilotThread(this, currentFile);
        t.start();
    }

    /**
     * is called when a file is explicitely openend, meanwhile not used :)
     */
    private void openFile() {
        JFileChooser jfc = new JFileChooser(properties.getLastDir());
        // jfc.setCurrentDirectory(new File(properties.getLastDir()));
        if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(this)) {
            debug("opening file: " + jfc.getSelectedFile().toString());
            properties.setLastDir(jfc.getCurrentDirectory().toString());
            debug("saving path: " + jfc.getCurrentDirectory().toString());
            try {
                setCurrentFile(jfc.getSelectedFile());
            } catch (IOException e) {
                debug("Error reading File: " + e.toString());
            }
        }
        // System.gc();
    }

    /**
     * Set the "dirty" trag if information has changed :) needed for
     * bugging the user with questions like: Do you want to save the
     * file? or Unsaved changes will be lost :)
     *
     * @param isDirty
     */
    public static void setDirty(boolean isDirty) {
        DIRTY = isDirty;
    }

    /**
     * set a new Image to edit ... thread-based ...
     */
    public void setCurrentFile(File f) throws IOException {
        boolean loadIt = true;
        if (DIRTY) {
            loadIt = askIfSave();
        }
        if (loadIt) {
            ImageLoader t = new ImageLoader(status, this, f, imagePanel, creationPanel, textPanel, mdPanel, qualityPanel,
                    beePanel, colorPanel, shapePanel);
            shapePanel.setDescriptor(null);
            t.start();
            currentFile = f;
        }
    }

    /**
     * loadCurrentFile is the not thread based method to load a file, it's used
     * by the autopilot
     *
     * @param f defines the file to open
     * @throws IOException
     */
    public void loadCurrentFile(File f) throws IOException {
        ImageLoader t = new ImageLoader(status, this, f, imagePanel, creationPanel, textPanel, mdPanel, qualityPanel,
                beePanel, colorPanel, shapePanel);
        t.run();

        currentFile = f;
    }

    public void saveFile() {
        if (currentFile != null) {
            try {
                XMLOutputter op = new XMLOutputter(Format.getPrettyFormat());
                String imgName = currentFile.getCanonicalPath();
                String fname = imgName.substring(0, imgName.lastIndexOf("."));
                fname = fname + ".mp7.xml";
                File saveFile = new File(fname);
                // check if file is arleady in existence and ask if we should overwrite:
//                boolean write = false;
//                if (saveFile.exists()) {
//                    if (JOptionPane.showConfirmDialog(this,
//                            "Overwrite existing file?", "Overwrite?",
//                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//                        write = true;
//                    }
//                } else {
//                    write = true;
//                }
                if (true) {
                    FileOutputStream fos = new FileOutputStream(saveFile);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
                    op.output(createDocument(), osw);
                    osw.close();
                    fos.close();
                    debug("File written to " + saveFile.toString());
                    status.setText("File written to " + saveFile.toString());
                    setDirty(false);
                    if (getTitle().indexOf('*') > -1)
                        setTitle(getTitle().substring(2));
                }
            } catch (IOException e) {
                debug("Exception while saving " + e.toString());
            }
        }

    }


    /**
     * This is the method where all the information is collected and put together. The result is valid
     * MPEG-7 Description
     */
    public Document createDocument() {
        Element mediaFormat, creation, tmp, semantics;
        tmp = creationPanel.createXML();
        mediaFormat = (Element) tmp.getChild("MediaFormat", tmp.getNamespace()).detach();
        creation = (Element) tmp.getChild("CreationInformation", tmp.getNamespace()).detach();
        tmp = beePanel.getSemanticsDocument().getRootElement();
        semantics = ((Element) tmp.getChild("Description", tmp.getNamespace()).getChild("Semantics",
                tmp.getNamespace()).clone());
        semantics.setName("Semantic");
        File thumbFile = AnnotationToolkit.generateThumbnail(currentFile);
        Element thumbNailProfile = null;
        Mpeg7ThumbnailMediaProfile thumbnail = null;
        thumbnail = new Mpeg7ThumbnailMediaProfile(thumbFile, 120, 120);
        thumbNailProfile = thumbnail.createDocument();

        Mpeg7ImageDescription m7id = new Mpeg7ImageDescription(creation, mdPanel.createXML(), mediaFormat,
                AnnotationToolkit.getMpeg7MediaInstance(currentFile),
                qualityPanel.createXML(), thumbNailProfile, semantics, textPanel.createXML(), colorPanel.createXML());
        Document document = m7id.createDocument();

        // TODO: add shape here ...
        Element shape = shapePanel.createXML();
        java.util.List results = AnnotationToolkit.xpathQuery(shape, "//Image/SpatialDecomposition", null);
        if (results.size() > 0) {
            shape = (Element) ((Element) results.get(0)).detach();
            java.util.List result = AnnotationToolkit.xpathQuery(document.getRootElement(), "//Image", null);
            if (result.size() > 0) ((Element) result.get(0)).addContent(shape);
        }

        return document;
    }

    private void debug(String message) {
        if (DEBUG) System.out.println("[at.lux.fotoannotation.AnnotationFrame] " + message);
    }

    public void setStatus(String message) {
        status.setText(message);
    }

    public String getSemanticAgentsNames() {
        String[] names = beePanel.getSemanticAgentsNames();
        return createStringFromArray(names);
    }

    private String createStringFromArray(String[] names) {
        StringWriter sw = new StringWriter(names.length * 32);
        for (int i = 0; i < names.length; i++) {
            sw.append(names[i]);
            if (i < names.length - 1) sw.append(", ");
        }
        return sw.toString();
    }

    public String getSemanticEventsNames() {
        String[] names = beePanel.getSemanticEventsNames();
        return createStringFromArray(names);
    }

    public String getSemanticTimesNames() {
        String[] names = beePanel.getSemanticTimesNames();
        return createStringFromArray(names);
    }

    public String getSemanticPlacesNames() {
        String[] names = beePanel.getSemanticPlacesNames();
        return createStringFromArray(names);
    }

    /**
     * Getter for the file which is currently annotated.
     *
     * @return the File which is currently annotated.
     */
    public File getCurrentFile() {
        return currentFile;
    }

    public BufferedImage getImage() {
        return imagePanel.getImage();
    }

    public boolean isAutopilot() {
        return autopilot;
    }

    public void setAutopilot(boolean autopilot) {
        this.autopilot = autopilot;
    }

    public Properties getCurrentFileProperties() {
        return currentFileProperties;
    }
}
