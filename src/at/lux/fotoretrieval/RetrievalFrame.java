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
package at.lux.fotoretrieval;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.apache.lucene.index.IndexReader;
import org.jdom.Element;

import at.lux.components.StatusBar;
import at.lux.fotoannotation.AnnotationToolkit;
import at.lux.fotoannotation.AnnotationFrame;
import at.lux.fotoannotation.IconCache;
import at.lux.fotoannotation.dialogs.FullSizeImagePanel;
import at.lux.fotoretrieval.dialogs.AboutDialog;
import at.lux.fotoretrieval.dialogs.HelpDialog;
import at.lux.fotoretrieval.dialogs.IndexLocationDialog;
import at.lux.fotoretrieval.dialogs.IndexingWizardDialog;
import at.lux.fotoretrieval.lucene.IndexerThread;
import at.lux.fotoretrieval.panels.*;
import at.lux.fotoretrieval.retrievalengines.LucenePathIndexRetrievalEngine;
import at.lux.fotoretrieval.retrievalengines.LuceneRetrievalEngine;
import at.lux.imageanalysis.JDomVisualDescriptor;
import at.lux.imageanalysis.VisualDescriptor;
import at.lux.splash.SplashScreen;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

/**
 * Main class for retrieval ...
 */
public class RetrievalFrame extends JFrame implements ActionListener, RetrievalOperations, StatusBar {
    public static final boolean DEBUG = false;
    public static final String CONFIGURATION_FILE = "emir.properties";
    public static String BASE_DIRECTORY = ".";
    private JPanel statusPanel;
    private JProgressBar garbageBar;
    private JLabel status;
    private JTabbedPane tabs;
    private ContentBasedImageRetrievalPanel cbPanel;
    private GarbageTracker gTracker;
    private TextInputSearchPanel searchPanel;
    private XPathSearchPanel xpathInputPanel;
    private LuceneSearchPanel luceneInputPanel;
    private Properties props;
    private SemanticSearchPanel semanticSearchPanel;
    private GraphSearchPanel graphSearchPanel;

    private final String windowSizeX = "window.size.x";
    private final String windowSizeY = "window.size.y";
    private final String windowLocationX = "window.location.x";
    private final String windowLocationY = "window.location.y";
    private final String dataRepositoryBaseDirectory = "data.base.directory";

    private JDialog configDialog = null;

    public RetrievalFrame() {
        super();
        try {
            this.setIconImage(ImageIO.read(RetrievalFrame.class.getResource("data/find.gif")));
        } catch (Exception e) {
            debug("Couldn't set Icon: " + e.toString() + " " + e.getMessage());
        }
        init();
    }

    private void init() {
        setTitle(RetrievalToolkit.PROGRAM_NAME + ' ' + AnnotationToolkit.PROGRAM_VERSION);

        // ----------------------------
        // Setting Emir props
        // ----------------------------
        props = new Properties();
        File conf = new File("./" + CONFIGURATION_FILE);
        debug("./" + CONFIGURATION_FILE);
        if (conf.exists()) {
            try {
                props.load(new FileInputStream(conf));
            } catch (IOException e) {
                System.err.println("Error loading config file: " + e.toString());
            }
            BASE_DIRECTORY = props.getProperty(dataRepositoryBaseDirectory, ".");
        } else {
            props.setProperty(dataRepositoryBaseDirectory, BASE_DIRECTORY);
        }
        // creating a base configuration from saved properties
        EmirConfiguration.getInstance(props);
        // ----------------------------
        // Setting Frame props
        // ----------------------------
        int xwidth = Integer.parseInt(props.getProperty(windowSizeX, "640"));
        int ywidth = Integer.parseInt(props.getProperty(windowSizeY, "480"));
        setSize(xwidth, ywidth);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int xloc = Integer.parseInt(props.getProperty(windowLocationX, "" + ((d.width - this.getWidth()) / 2)));
        int yloc = Integer.parseInt(props.getProperty(windowLocationY, "" + ((d.height - this.getHeight()) / 2)));
        setLocation(xloc, yloc);
        JMenuBar retrievalMenuBar = RetrievalToolkit.createRetrievalMenuBar(this);
        retrievalMenuBar.add(createResultsPanelMenu(), retrievalMenuBar.getComponentCount() - 1);
        this.setJMenuBar(retrievalMenuBar);

        // ----------------------------
        // Setting Frame Listeners ...
        // ----------------------------
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        // ----------------------------
        // Creating Components
        // ----------------------------
        cbPanel = new ContentBasedImageRetrievalPanel(this);
        searchPanel = new TextInputSearchPanel(this);
        xpathInputPanel = new XPathSearchPanel(this);
        luceneInputPanel = new LuceneSearchPanel(this);
        semanticSearchPanel = new SemanticSearchPanel(this);
        graphSearchPanel = new GraphSearchPanel(this);

        statusPanel = new JPanel(new BorderLayout());
        status = new JLabel(RetrievalToolkit.PROGRAM_NAME + " " + AnnotationToolkit.PROGRAM_VERSION);
        status.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        garbageBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 16);
        garbageBar.setStringPainted(true);
        garbageBar.setBorder(BorderFactory.createEmptyBorder());
        garbageBar.setToolTipText("This bar shows how much memory is allocated by the VM and how much of it isalready in use.");
//        garbageBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        gTracker = new GarbageTracker(garbageBar);
        statusPanel.add(status, BorderLayout.CENTER);
        statusPanel.add(garbageBar, BorderLayout.EAST);

        tabs = new JTabbedPane(JTabbedPane.NORTH);
        tabs.add("Index", luceneInputPanel);
        tabs.add("Graph", graphSearchPanel);
        tabs.add("Image", cbPanel);
//        tabs.add("Keywords", searchPanel);
//        tabs.add("XPath", xpathInputPanel);
//        tabs.add("Semantics", semanticSearchPanel);

        this.getContentPane().add(tabs, BorderLayout.CENTER);
        this.getContentPane().add(statusPanel, BorderLayout.SOUTH);

        gTracker.start();
    }

    private JMenu createResultsPanelMenu() {
        JMenu resultsMenu = new JMenu("Results");
        resultsMenu.setMnemonic(KeyEvent.VK_R);

        JMenuItem saveResults = new JMenuItem("Export result list ...", IconCache.getInstance().getSaveAsIcon());
        saveResults.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveResults.addActionListener(this);
        saveResults.setActionCommand("exportResultList");

        JMenuItem clusterResults = new JMenuItem("Cluster results", IconCache.getInstance().getClusterIcon());
        clusterResults.setAccelerator(KeyStroke.getKeyStroke("F2"));
        clusterResults.addActionListener(this);
        clusterResults.setActionCommand("clusterResults");

        JMenuItem mdsResults = new JMenuItem("Visualize results", IconCache.getInstance().getMdsIcon());
        mdsResults.setAccelerator(KeyStroke.getKeyStroke("F3"));
        mdsResults.addActionListener(this);
        mdsResults.setActionCommand("mdsResults");

        resultsMenu.add(saveResults);
        resultsMenu.addSeparator();
        resultsMenu.add(clusterResults);
        resultsMenu.add(mdsResults);

        return resultsMenu;
    }

    public static void main(String[] args) {
        JWindow w = null;
        try {
            w = new JWindow();
            BufferedImage img = ImageIO.read(AnnotationFrame.class.getResourceAsStream("data/SplashEmir.png"));
            FullSizeImagePanel panel = new FullSizeImagePanel(img);
            w.getContentPane().add(panel, BorderLayout.CENTER);
            w.pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            w.setLocation((screenSize.width - w.getWidth()) / 2, (screenSize.height - w.getHeight()) / 2);
            w.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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


        try {
            RetrievalFrame rf = new RetrievalFrame();
            SplashScreen splash = new SplashScreen(rf);
            if (showSplash) {
                // now show the splash screen if this has not been done before.
                splash.setVisible(true);
            }
            w.setVisible(false);
            rf.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, "Error: " + e.toString(), "Error starting Emir!", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Overides method defined in Interface ActionListener
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("exit")) {
            exitApplication();
        } else if (e.getActionCommand().equals("index")) {
            createIndex();
        } else if (e.getActionCommand().equals("closeTab")) {
            if (tabs.getSelectedIndex() > 2) {
                tabs.remove(tabs.getSelectedIndex());
            }
        } else if (e.getActionCommand().equals("about")) {
            AboutDialog adialog = new AboutDialog(this);
            adialog.pack();
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            adialog.setLocation((ss.width - adialog.getWidth()) / 2, (ss.height - adialog.getHeight()) / 2);
            adialog.setVisible(true);
        } else if (e.getActionCommand().equals("k-tab")) {
            tabs.add("Keywords", searchPanel);
            tabs.setSelectedComponent(searchPanel);
        } else if (e.getActionCommand().equals("s-tab")) {
            tabs.add("Semantics", semanticSearchPanel);
            tabs.setSelectedComponent(semanticSearchPanel);
        } else if (e.getActionCommand().equals("x-tab")) {
            tabs.add("XPath", xpathInputPanel);
            tabs.setSelectedComponent(xpathInputPanel);
        } else if (e.getActionCommand().equals("wizardIndex")) {
            IndexingWizardDialog wizardDialog = new IndexingWizardDialog(this);
        } else if (e.getActionCommand().equals("visitHomepage")) {
            openUrlInBrowser(props.getProperty("help.homepage"));
        } else if (e.getActionCommand().equals("showHelpOnline")) {
            openUrlInBrowser(props.getProperty("help.online"));
        } else if (e.getActionCommand().equals("showConfig")) {
            if (configDialog == null) {
                configDialog = new JDialog((Frame) null, "Emir :: Configuration");
                configDialog.setSize(640, 480);
                Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
                configDialog.setLocation((ss.width - configDialog.getWidth())/2, (ss.height-configDialog.getHeight())/2);
                ConfigurationDialogPanel config = new ConfigurationDialogPanel(configDialog);
                configDialog.getContentPane().add(config, BorderLayout.CENTER);
            }
            configDialog.setVisible(true);
        } else if (e.getActionCommand().equals("createIndex")) {
            createIndex();
        } else if (e.getActionCommand().equals("indexPath")) {
            IndexLocationDialog dg = new IndexLocationDialog(this);
            dg.pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            dg.setLocation(((int) d.getWidth() - dg.getWidth()) >> 1, ((int) d.getHeight() - dg.getHeight()) >> 1);
            dg.setVisible(true);
        } else if (e.getActionCommand().equals("viCl")) {
            // visualize current repository using ColorLayout:
            JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
            pbar.setStringPainted(true);
            VisualDescriptorVisualizationThread vt = new VisualDescriptorVisualizationThread(BASE_DIRECTORY, this, pbar, JDomVisualDescriptor.Type.ColorLayout);
            vt.start();
        } else if (e.getActionCommand().equals("viSc")) {
            // visualize current repository using ScalableColor:
            JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
            pbar.setStringPainted(true);
            VisualDescriptorVisualizationThread vt = new VisualDescriptorVisualizationThread(BASE_DIRECTORY, this, pbar, JDomVisualDescriptor.Type.ScalableColor);
            vt.start();
        } else if (e.getActionCommand().equals("viEh")) {
            // visualize current repository using EdgeHistogram:
            JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
            pbar.setStringPainted(true);
            VisualDescriptorVisualizationThread vt = new VisualDescriptorVisualizationThread(BASE_DIRECTORY, this, pbar, JDomVisualDescriptor.Type.EdgeHistogram);
            vt.start();
        } else if (e.getActionCommand().equals("viSg")) {
            // visualize current repository using semantic graphs:
            if (true) {
                JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
                pbar.setStringPainted(true);
                GraphDistanceVisualizationThread vt = new GraphDistanceVisualizationThread(BASE_DIRECTORY, this, pbar);
                vt.start();
            } else {
                JOptionPane.showMessageDialog(this, "Visualization not available.\nNot fully implemented with new index structure!");
            }
        } else if (e.getActionCommand().equals("showIndexPath")) {
            JOptionPane.showMessageDialog(this, "Current data repository:\n " + BASE_DIRECTORY);
            setStatus("Current data repository: " + BASE_DIRECTORY);
        } else if (e.getActionCommand().equals("exportResultList")) {
            try {
                Component component = tabs.getSelectedComponent();
                if (component instanceof ResultsPanel) {
                    File tempFile = File.createTempFile("emir_results_", ".html");
                    String html = ((ResultsPanel) component).getResultHtml();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
                    bw.write(html);
                    bw.close();
                    openUrlInBrowser(tempFile.getCanonicalPath());
                }
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(this, "Error saving description: " + e.toString());
            }
        } else if (e.getActionCommand().equals("clusterResults")) {
            // TODO: implement this ...
            JOptionPane.showMessageDialog(this, "Not implemented yet!");
        } else if (e.getActionCommand().equals("mdsResults")) {
//            JOptionPane.showMessageDialog(this, "Not implemented yet!");
            Component component = tabs.getSelectedComponent();
            if (component instanceof ResultsPanel) {
                String[] files = ((ResultsPanel) component).getResultFiles();
                JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
                pbar.setStringPainted(true);
                int metric = Integer.parseInt(EmirConfiguration.getInstance().getProperty("Metric.FDP"));
                if (metric == 0 || metric == 1 || metric == 3) {
                    VisualDescriptor.Type metricType = JDomVisualDescriptor.Type.ScalableColor;
                    if (metric == 1) metricType = VisualDescriptor.Type.ColorLayout;
                    if (metric == 2) metricType = VisualDescriptor.Type.DominantColor;
                    if (metric == 3) metricType = VisualDescriptor.Type.EdgeHistogram;
                    VisualDescriptorVisualizationThread vt = new VisualDescriptorVisualizationThread(files, this, pbar, metricType);
                    vt.start();
                } else if (metric == 4) {
                    JOptionPane.showMessageDialog(this, "Not implemented yet! Only ScalableColor, ColorLayout and EdgeHistogram are implemented for visualizing results.");
                    // GraphDistanceVisualizationThread vt = new GraphDistanceVisualizationThread(".", this, pbar);
                } else if (metric == 6) {
                    GraphDistanceVisualizationThread vt = new GraphDistanceVisualizationThread(files, this, pbar);
                    vt.start();
                } else {
                    JOptionPane.showMessageDialog(this, "Not implemented yet! Only ScalableColor, ColorLayout and EdgeHistogram are implemented for visualizing results.");
                }
            }
        } else if (e.getActionCommand().equals("help")) {
            HelpDialog adialog = new HelpDialog(this);
            // adialog.pack();
            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
            adialog.setLocation((ss.width - adialog.getWidth()) / 2, (ss.height - adialog.getHeight()) / 2);
            adialog.setVisible(true);

        } else {
            JOptionPane.showMessageDialog(this, "Not implemented yet!");
        }
    }

    private void openUrlInBrowser(String url) {
        String osName = System.getProperty("os.name");
        // take linux settings
        String browserCmd = props.getProperty("browser.linux");
        // or windows in case of windows :)
        if (osName.toLowerCase().indexOf("windows") > -1) {
            browserCmd = props.getProperty("browser.windows");
        }
        browserCmd = browserCmd.replace("{url}", url);
        try {
            Runtime.getRuntime().exec(browserCmd);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not start browser. Please surf to "
                    + url + " to visit the online help.");
        }
    }

    public void createIndex() {
        int answer = JOptionPane.showConfirmDialog(this, "Do you really want to create an index?");
        if (answer == JOptionPane.OK_OPTION) {
            Thread indexer = new Thread(new IndexerThread(this, BASE_DIRECTORY));
            indexer.start();
        }
    }

    /**
     * is called before program is exited.
     */
    private void exitApplication() {
        debug("Exiting application");
        props.setProperty(dataRepositoryBaseDirectory, BASE_DIRECTORY);
        props.setProperty(windowSizeX, this.getWidth() + "");
        props.setProperty(windowSizeY, this.getHeight() + "");
        props.setProperty(windowLocationX, this.getLocation().x + "");
        props.setProperty("yloc", this.getLocation().y + "");

        props = EmirConfiguration.getInstance().saveProperties(props);

        try {
            props.store(new FileOutputStream("./" + CONFIGURATION_FILE, false), "Emir configuration file (automatically generated)");
            debug("Saved configuration to " + "./" + CONFIGURATION_FILE);
        } catch (IOException e) {
            debug("Couldn't store " + "./" + CONFIGURATION_FILE);
        }

        // shutting down the database:
        try {
            DriverManager.getConnection("jdbc:derby:imageDB;shutdown=true");
        } catch (SQLException e) {
            System.out.println("DB shutdown successful!");
        }

        System.exit(0);
    }

    private void debug(String message) {
        if (RetrievalFrame.DEBUG) {
            System.out.println("[at.lux.fotoretrieval.RetrievalFrame] " + message);
        }
    }

    public void searchForImage(Element ColorLayoutDescriptor, String directory, boolean descendIntoSubdirs) {
        debug("Starting search for similar image");
        JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
        pbar.setStringPainted(true);

        Thread t = null;
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) getJMenuBar().getMenu(3).getItem(2);
        if (!item.getState()) {
            t = new SimilarImageSearchThread(ColorLayoutDescriptor, directory, descendIntoSubdirs, this, pbar);
        } else {
            t = new DatabaseSimilarImageSearchThread(ColorLayoutDescriptor, directory, descendIntoSubdirs, this, pbar);
        }
        t.start();
    }

    public void searchForImage(Set<Element> ColorLayoutDescriptor, String directory, boolean descendIntoSubdirs) {
        debug("Starting search for similar image");
        JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
        pbar.setStringPainted(true);

        Thread t = null;
        boolean useDerby = EmirConfiguration.getInstance().getBoolean("Retrieval.Cbir.useDerby");
        if (!useDerby) {
            t = new SimilarImageSearchThread(ColorLayoutDescriptor, directory, descendIntoSubdirs, this, pbar);
        } else {
            t = new DatabaseSimilarImageSearchThread(ColorLayoutDescriptor, directory, descendIntoSubdirs, this, pbar);
        }
        t.start();
    }

    public void searchForImage(String xPath, String directory, boolean descendIntoSubdirs) {
        debug("Starting search for keywords ... ");
        JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
        pbar.setStringPainted(true);

        Thread t = new SearchThroughXPathThread(xPath, directory, descendIntoSubdirs, this, pbar);
        t.start();
    }

    public void searchForImageInIndex(String xPath, String directory, boolean descendIntoSubdirs) {
        if (IndexReader.indexExists(LuceneRetrievalEngine.parseFulltextIndexDirectory(BASE_DIRECTORY)) &&
                IndexReader.indexExists(LuceneRetrievalEngine.parseSemanticIndexDirectory(BASE_DIRECTORY))) {

            debug("Starting search for keywords ... ");
            JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
            pbar.setStringPainted(true);

            Thread t = new SearchThroughLuceneThread(xPath, directory, descendIntoSubdirs, this, pbar);
            t.start();
        } else {
            setStatus("Using index from " + BASE_DIRECTORY);
            JOptionPane.showMessageDialog(this, "Given directory (" + BASE_DIRECTORY + ") contains no index, please \ncreate one first (see menu).");
        }

    }

    public void searchForImage(String xPath, Vector objects, String directory, boolean descendIntoSubdirs) {
        debug("Starting search for semantic description ... ");
        JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
        pbar.setStringPainted(true);

        Thread t = new SearchSemanticDescriptionThread(xPath, objects, directory, descendIntoSubdirs, this, pbar);
        t.start();
    }

    public void setStatus(String message) {
        status.setText(message);
    }

    public void addResult(ResultsPanel rp) {
        tabs.add("Results", rp);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    public void addVisualization(JPanel p) {
        tabs.add("Vis", p);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    public QualityConstraintPanel getQualityConstraints() {
        return searchPanel.getQualityConstraints();
    }

    public void searchForSemanticsInIndex(String xPath, String directory, boolean descendIntoSubdirs) {
        if (IndexReader.indexExists(LuceneRetrievalEngine.parseFulltextIndexDirectory(BASE_DIRECTORY)) &&
                IndexReader.indexExists(LuceneRetrievalEngine.parseSemanticIndexDirectory(BASE_DIRECTORY))) {

            debug("Starting search for semantic annotations ... ");
            JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
            pbar.setStringPainted(true);

            Thread t = new SearchSemanticInIndexThread(xPath, directory, descendIntoSubdirs, this, pbar);
            t.start();
        } else {
            setStatus("Using index from " + BASE_DIRECTORY);
            JOptionPane.showMessageDialog(this, "Given directory (" + BASE_DIRECTORY + ") contains no index, please \ncreate one first (see menu).");
        }

    }

    public void searchForSemanticsInPathIndex(String xPath, String directory, boolean descendIntoSubdirs) {
        if (IndexReader.indexExists(LucenePathIndexRetrievalEngine.parsePathIndexDirectory(BASE_DIRECTORY)) &&
                IndexReader.indexExists(LuceneRetrievalEngine.parseSemanticIndexDirectory(BASE_DIRECTORY))) {

            debug("Starting search for semantic annotations ... ");
            JProgressBar pbar = new JProgressBar(JProgressBar.HORIZONTAL);
            pbar.setStringPainted(true);

            Thread t = new SearchSemanticInPathIndexThread(xPath, directory, descendIntoSubdirs, this, pbar);
            t.start();
        } else {
            setStatus("Using index from " + BASE_DIRECTORY);
            JOptionPane.showMessageDialog(this, "Given directory (" + BASE_DIRECTORY + ") contains no index, please \ncreate one first (see menu).");
        }

    }
}


