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
package at.lux.fotoannotation.panels;

import at.lux.fotoannotation.AnnotationFrame;
import at.wklieber.Settings;
import at.wklieber.gui.*;
import at.wklieber.gui.data.DataInterface;
import at.wklieber.gui.data.DescriptionData;
import at.wklieber.gui.data.IComponentData;
import at.wklieber.mpeg7.*;
import at.wklieber.tools.FileTools;
import at.wklieber.tools.Mpeg7DateFormat;
import org.jdom.Document;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Date: 16.03.2005
 * Time: 22:09:05
 *
 * @author Werner Klieber, werner@klieber.info
 */
public class ShapePanel extends JPanel implements AnnotationPanel {
    static Logger log = Logger.getLogger(ShapePanel.class.getName());

    private AnnotationFrame parent;

    private ObjectPalette palette;
    private IDrawPanel drawPanel;
    private JPanel buttonPanel;
    private Container toolbar;
    private DrawSketchFrame frame;

    /**
     * Creates a new <code>ShapePanel</code> with a double buffer
     * and a flow layout.
     */
    public ShapePanel(AnnotationFrame parent) {
        init(parent);
    }

    private void init(AnnotationFrame parent) {
        this.parent = parent;

        JPanel palettePanel = new JPanel();
        palettePanel.setLayout(new BorderLayout());
        palettePanel.setSize(300, 300);
        palettePanel.setMinimumSize(new Dimension(300, 300));

        palettePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Color palette"));

        ///JLabel test = new JLabel("palleten panel");
        //palettePanel.add(test, BorderLayout.NORTH);

        //System.out.println("PAl: " + palettePanel.getSize());
        palette = new ObjectPalette(palettePanel);
        palettePanel.validate();
        palettePanel.repaint();

        // initialize all member-menu Panels
        //drawPanel = new IDrawPanel(this);
        drawPanel = new IDrawPanel();
        drawPanel.setMathMode(true);
        buttonPanel = new JPanel();

        //------------------------ define the Drawpanel ---------------------------
        toolbar = new Container();
        LayoutManager mgr = new FlowLayout(FlowLayout.LEFT);
        toolbar.setLayout(mgr);
        //JToolBar tool = new JToolBar("test");
        //toolbar.add(tool);
        //tool.add(new JLabel("test"));
        //tool.add(new JButton("ui"));


        //get the image
        AnnotationFrame parentFrame = parent;
        List iComponentList = new ArrayList();
        frame = new DrawSketchFrame(parentFrame, toolbar, iComponentList);


        //link all panels together
        this.setLayout(new BorderLayout());
        Container pane = new Container();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        //JButton spacer = new JButton("this is a placeholder");
        //spacer.setMinimumSize(new Dimension(100, 2));
        JPanel spacer = new JPanel(null, true);
        spacer.setMinimumSize(new Dimension(100,3));
        spacer.setMaximumSize(new Dimension(100,3));
        spacer.setPreferredSize(new Dimension(100,3));

        pane.add(palettePanel);
        pane.add(spacer);
        //pane.add(toolbar);


        this.add(toolbar, BorderLayout.NORTH);
        this.add(frame, BorderLayout.CENTER);
        this.add(pane,BorderLayout.WEST);
        //this.add(palettePanel, BorderLayout.WEST);

        this.revalidate();
        this.validate();
        //spacer.setVisible(false);
    }


    /**
     * Is called if the user selects another image to annotate.
     *
     * @param shapeDescriptor can be null if no descriptor exists.
     */
    public void setDescriptor(Element shapeDescriptor) {
        BufferedImage img = parent.getImage();
        frame.setImage(img);

        if (shapeDescriptor!=null) {
            // in this specific case the whole document is returned to the panel:
            // a template is created and the shapes are read.
            Mpeg7 mpeg7 = new Mpeg7();
            mpeg7.createTemplateDocument();
            Document doc1 = new Document(((Element) ((Element) shapeDescriptor.clone()).detach()));
            mpeg7.setDocument(doc1);
            // all necessary shape descriptors are extracted from a
            // fully featured MPEG-7 file ...
            List xmlData = extractMpeg7Data(mpeg7);
            List<DataInterface> guiData = new ArrayList<DataInterface>();
            for (int i = 0; i < xmlData.size(); i++) {
                Object ds = xmlData.get(i);
                if (DescriptionMetadataDs.class.isInstance(ds)) {
                    DescriptionMetadataDs mds = (DescriptionMetadataDs) ds;

                    DescriptionData data = new DescriptionData();
                    data.setDescription(mds.getCreationDescription());
                    data.setCreatorFirstName(mds.getCreatorGivenName());
                    data.setCreatorLastName(mds.getCreatorFamilyName());
                    data.setCreationPlace(mds.getCreationPlace());
                    data.setCreationTime(Mpeg7DateFormat.format(mds.getCreationTime(), null));
                    data.setCreationTool(mds.getCreationTool());

                    log.fine("-> DATA: " + data.toString());
                    log.fine("-> DS: " + mds.toString());

                    guiData.add(data);
                } else /*if (CameraMotionDs.class.isInstance(ds)) {
                    CameraMotionDs mds = (CameraMotionDs) ds;
                    CameraMotionData data = new CameraMotionData();

                    data.setMotionName(mds.getMotionName());
                    data.setAmountOfmotion(mds.getMotionAmount());
                    data.setDuration(mds.getDuration());
                    data.setMotionsegmentType("MIXED");
                    data.setTimePoint(Mpeg7DateFormat.format(mds.getTimePoint(), new Date(0)));

                    returnValue.add(data);
                    cat.fine("-> DATA: " + data.toString());
                    cat.fine("-> DS: " + mds.toString());
                } else*/ if (StillRegionDs.class.isInstance(ds)) {
                    StillRegionDs mds = (StillRegionDs) ds;

                    // if data contains box and dominant color, then extract color and position
                    Rectangle box = mds.getBox();
                    if ((box != null) && (box.getWidth() != 0) && (box.getHeight() != 0)) {
                        IComponentData data = new IComponentData(new IColorRectangle());
                        data.setBoundary(box);
                        Color[] cArray = mds.getColors();
                        if (cArray != null && cArray.length > 0) {
                            data.setForeGround(cArray[0]);
                        }

                        guiData.add(data);
                        log.fine("-> DATA: " + data.toString());
                        log.fine("-> DS: " + mds.toString());
                    } else { // now check if it has a shape. Then extract the shape data
                        IComponentData data = new IComponentData(new IShape());
                        List pointList = mds.getShape();

                        data.setDotList(pointList);
                        Color[] cArray = mds.getColors();
                        if (cArray != null && cArray.length > 0) {
                            data.setFillColor(cArray[0]);
                        }

                        guiData.add(data);
                        log.fine("-> DATA: " + data.toString());
                        log.fine("-> DS: " + mds.toString());


                    }
                } /*else if (SemanticDs.class.isInstance(ds)) {
                    SemanticDs mds = (SemanticDs) ds;
                    SemanticMpeg7Data data = new SemanticMpeg7Data();

                    data.setSemanticDs(mds.getDocument(new Document(new Element(Mpeg7Template.getRootTag()))));

                    returnValue.add(data);
                    cat.fine("-> DATA: " + data.toString());
                    cat.fine("-> DS: " + mds.toString());
                }*/ else {
                    log.severe("Unknown MPEG7 Descriptor: \"" + ds.getClass().getName() + "\"");
                }
            } // end for

            List<IComponentData> componentList = new ArrayList<IComponentData>();
            for (int i = 0; i < guiData.size(); i++) {
                DataInterface dataInterface = guiData.get(i);
                if (dataInterface instanceof IComponentData) {
                    componentList.add((IComponentData) dataInterface);
                }
            }

            frame.setIComponents(componentList);
        }


        frame.revalidate();
        frame.repaint();
        repaint();
    }

    /**
     * Creates the MPEG-7 descriptor from the annotation
     * and returns it as JDOM Element
     *
     * @return the descriptor as JDOM Element or NULL on error
     */
    public Element createXML() {
        Element returnValue = null;

        List<IComponentData> components = frame.getDataList();
        Mpeg7 mpeg7 = generateMpeg7FromData(components);
        returnValue = mpeg7.getDocument((Element) null);
        // TODO: This save to file thing should vanish here ...
        String mpegString = mpeg7.getDocument("");
        FileTools.saveToFile("c:/test.xml", mpegString);
        /*System.out.println("==================================================");
        System.out.println(mpegString);
        System.out.println("==================================================");*/

        return returnValue;
    }

    /**
     * generate a mpeg7 document filled with data from the input-data list  (DataInterface)
     */
    private Mpeg7 generateMpeg7FromData(List<IComponentData> dataList1) {
        Mpeg7 returnValue = new Mpeg7();
        returnValue.createTemplateDocument();

        if (dataList1 == null) {
            return returnValue;
        }

        /*cat.fine("List contains: " + dataList1.size() + ", MPEG null: "
                + (returnValue.getDocument((Document) null) == null));*/

        Iterator it = dataList1.iterator();
        SpatialDecomposition spatialDecompWrapper = null;
        while (it.hasNext()) {
            //cat.fine("XX: " + it.next().getClass().getName());
            DataInterface dataObject = (DataInterface) it.next();
            if (dataObject == null) {
                continue;
            }

            if (dataObject.getId() == DataInterface.DESCRIPTION_DATA) {
                DescriptionData dsData = (DescriptionData) dataObject;
                //cat.fine("load a DescriptionDS, " + dsData.toString());
                DescriptionMetadataDs ds = new DescriptionMetadataDs();
                ds.createTemplateDocument();

                //cat.fine("MPEG null: " + (returnValue.getDocument((Document) null) == null));

                ds.setData(dsData.getDescription(),
                        dsData.getCreatorFirstName(), dsData.getCreatorLastName(),
                        dsData.getCreationTime(), dsData.getCreationPlace(), dsData.getCreationTool());
                returnValue.addDescriptor(ds);
                //--------------------------------------------------------------------------
                /*} else if (dataObject.getId() == DataInterface.CAMERA_MOTION_DATA) {
                    CameraMotionData dsData = (CameraMotionData) dataObject;
                    //cat.fine("process a Camera Motion DS, " + dsData.toString());
                    CameraMotionDs ds = new CameraMotionDs();
                    ds.createTemplateDocument();

                    //cat.fine("MPEG null: " + (returnValue.getDocument((Document) null) == null));

                    ds.setData(Mpeg7DateFormat.date2Timepoint(dsData.getTimePoint()), dsData.getDuration(),
                            dsData.getMotionName(), dsData.getAmountOfmotion());
                    returnValue.addDescriptor(ds);*/
                //--------------------------------------------------------------------------
            } else if (dataObject.getId() == DataInterface.ICOMPONENT_DATA) {
                IComponentData dsData = (IComponentData) dataObject;
                if (dsData.getComponentName().equalsIgnoreCase(IColorRectangle.class.getName())) {
                    //IColorRectangle colorRectangle = dsData.

                    //log.fine("process a ColorRectangle, " + dsData.toString());

                    // save the position
                    SpatialLocatorDs spatialLocator = new SpatialLocatorDs(SpatialLocatorDs.TYPE_BOX);
                    spatialLocator.createTemplateDocument();
                    spatialLocator.setData(dsData.getBoundary());
                    //log.fine(spatialLocator.toString());


                    // save the color
                    DominantColorDs dominantColor = new DominantColorDs();
                    dominantColor.createTemplateDocument();
                    dominantColor.setData(dsData.getForeGround());

                    // The stillregion holts region and color
                    StillRegionDs regionDs = new StillRegionDs();
                    regionDs.createTemplateDocument();

                    regionDs.setData(spatialLocator, dominantColor);

                    // the spatial wrapper that gets all the data regionData
                    if (spatialDecompWrapper == null) {
                        spatialDecompWrapper = new SpatialDecomposition();
                        spatialDecompWrapper.createTemplateDocument();
                    }

                    spatialDecompWrapper.addData(regionDs);
                } else if (dsData.getComponentName().equalsIgnoreCase(IShape.class.getName())) {

                    //log.fine("process a Shape, " + dsData.toString());

                    // save the Shape position
                    SpatialLocatorDs spatialLocator = new SpatialLocatorDs(SpatialLocatorDs.TYPE_SHAPE);
                    spatialLocator.createTemplateDocument();
                    spatialLocator.setData(dsData.getDotList());
                    //log.fine(spatialLocator.toString());

                    // save the color
                    DominantColorDs dominantColor = new DominantColorDs();
                    dominantColor.createTemplateDocument();
                    dominantColor.setData(dsData.getFillColor());


                    // The stillregion holts region and color
                    StillRegionDs regionDs = new StillRegionDs();
                    regionDs.createTemplateDocument();

                    regionDs.setData(spatialLocator, dominantColor);

                    // the spatial wrapper that gets all the data regionData
                    if (spatialDecompWrapper == null) {
                        spatialDecompWrapper = new SpatialDecomposition();
                        spatialDecompWrapper.createTemplateDocument();
                    }

                    spatialDecompWrapper.addData(regionDs);
                } else
                    log.severe("Not implemented: can not create MPEG 7 for <" + dsData.getComponentName() + ">");
            } else
                log.severe("Not implemented: can not save Data with id <" + dataObject.getId() + ">");
        } // end while

        // add the wrapper with all StillRegionDs desrcipions
        if (spatialDecompWrapper != null) {
            returnValue.addDescriptor(spatialDecompWrapper);
        }

        log.fine("MPEG7: " + returnValue.toString());
        //System.out.println("XXXMPEG7: " + returnValue.toString());

        returnValue.writeToFile("C:/mpeg7/" + Settings.DEFAULT_MPEG7_NAME);
        log.fine("MPEG 7 Creation done");
        return returnValue;
    } // end method

    /**
     * extract data from a mpeg7 document and filles data list with all extracted Descriptors.
     * This dat can be used in the userinterface to display the content
     */
    private List extractMpeg7Data(Mpeg7 mpeg7Document1) {
        List returnValue = new ArrayList<IComponentData>();

        if (mpeg7Document1 == null) {
            return returnValue;
        }

        // pass the file to all Mpeg 7 descriptor classes and let them extract their data
        // extract description Metadata
        List<IComponentData> descriptionList = new DescriptionMetadataDs().extractFromMpeg7(mpeg7Document1.getDocument((Document) null));
        log.fine("Extraction Info: " + descriptionList.size() + " DescriptionMetadata extracted");
        returnValue.addAll(descriptionList);

        //extract CameraMotion
        descriptionList = new CameraMotionDs().extractFromMpeg7(mpeg7Document1.getDocument((Document) null));
        log.fine("Extraction Info: " + descriptionList.size() + " Camera Motion data extracted");
        returnValue.addAll(descriptionList);

        /*System.out.println("---------------------------------------------------");
        System.out.println("MPEG: " + mpeg7Document1.toString());
        System.out.println("---------------------------------------------------");*/
        //extract Dominant color and positon
        descriptionList = new StillRegionDs().extractFromMpeg7(mpeg7Document1.getDocument((Document) null));
        log.fine("Extraction Info: " + descriptionList.size() + " Color+Position data extracted");
        returnValue.addAll(descriptionList);

        //extract Semantic descriptor
        /*descriptionList = new SemanticDs().extractFromMpeg7(mpeg7Document1.getDocument((Document) null));
        log.fine("Extraction Info: " + descriptionList.size() + " Semantic data extracted");
        returnValue.addAll(descriptionList);*/


        log.fine("Data extraction from MPEG 7 file done: " + returnValue.toString());
        return returnValue;
    } // end method


    public void setImage(BufferedImage image) {
        BufferedImage img = image;
        frame.setImage(img);
    }
}
