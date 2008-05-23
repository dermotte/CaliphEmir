/*
 * This file is part of Caliph & Emir.
 *
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
 * 
 * changes by Margit Lang November 2005
 */
package at.lux.fotoretrieval.retrievalengines;

import at.lux.fotoretrieval.FileOperations;
import at.lux.fotoretrieval.ResultListEntry;
import at.lux.fotoretrieval.RetrievalFrame;
import at.lux.fotoretrieval.RetrievalToolkit;
import at.lux.imageanalysis.ColorLayout;
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.ScalableColor;
import at.lux.imageanalysis.EdgeHistogramImplementation;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * FileSystemRetrieval
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FileSystemRetrieval extends AbstractRetrievalEngine {
    private int maxResults = 50;
    private Vector matches1;

    public FileSystemRetrieval(int maxResults) {
        matches1 = new Vector();
        this.maxResults = maxResults;
    }
/**
 * @author margit
 * @param VisualDescriptorSet
 * @param whereToSearch
 * @param recursive
 * @param progress
 * 
 */	
	public List<ResultListEntry> getSimilarImages_fromSet(Set<Element> VisualDescriptorSet, String whereToSearch,
            boolean recursive, JProgressBar progress) 
    {
		Element VisualDescriptor;
		List<ResultListEntry> results = new LinkedList<ResultListEntry>();
		Namespace mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		int counter = -1;
		try 
		{
			String[] descriptions = FileOperations.getAllDescriptions(new File(whereToSearch), recursive);
			double[][] diffs = new double[VisualDescriptorSet.size()][descriptions.length];
			double[] difference = new double [diffs[0].length];
			for (int j = 0; j<diffs.length; j++)
			{
				for (int i = 0; i < diffs[j].length; i++) 
				{
					diffs[j][i] = -1.0;
					difference[i]=-1.0;
				}
			}
			double[] maxdiff = new double[maxResults];
			for (int i = 0; i < maxdiff.length; i++) 
			{
				maxdiff[i] = -1.0;
			}
			SAXBuilder builder = new SAXBuilder();
			for (Iterator iterator = VisualDescriptorSet.iterator();iterator.hasNext();)
			{	counter++;
				VisualDescriptor = (Element) iterator.next();
				String descType = VisualDescriptor.getAttributeValue("type", xsi);
				if (progress != null)
				{
					progress.setString(VisualDescriptorSet.size()+" Margit: Finding MPEG-7 files ...");
				}
				
				if (progress != null)
				{
					progress.setMinimum(0);
					progress.setMaximum(descriptions.length);
					progress.setValue(0);
					progress.setString("Comparing images " + descType);
				}
				
				ScalableColor scc = null;
				for (int i = 0; i < descriptions.length; i++) 
				{
					Element e = null;
					try 
					{						
						e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
						java.util.List l = RetrievalToolkit.xpathQuery(e, "//VisualDescriptor", null);
						if (l.size() > 0) 
						{
								// 	debug("Comparing elements ...");
							if (descType.equals("ColorLayoutType")) 
							{
								//     	debug("Comparing ColorLayout ...");
								Element toCompare = null;
								for (Iterator iter = l.iterator(); iter.hasNext();) 
								{
									Element elem = (Element) iter.next();
									if (elem.getAttributeValue("type", xsi).equals("ColorLayoutType")) 
									{
										//diffs[counter][i] = ColorLayout.getSimilarity(VisualDescriptor, (Element) l.get(0));
										difference[i]= difference[i] + ColorLayout.getSimilarity(VisualDescriptor, (Element) l.get(0));
									}
								}
							} 
							else if (descType.equals("EdgeHistogramType")) 
							{
								EdgeHistogram a = null;
								try 
								{
									a = new EdgeHistogram(VisualDescriptor);
								} 
								catch (Exception e1) 
								{
									System.err.println("EdgeHistogram a is not an valid EdgeHistogram: " + e1.getMessage());
								}
								for (Iterator iter = l.iterator(); iter.hasNext();) 
								{
									Element elem = (Element) iter.next();
									if (elem.getAttributeValue("type", xsi).equals("EdgeHistogramType")) 
									{
										EdgeHistogram b = null;
										try 
										{
											b = new EdgeHistogram(elem);
										} 
										catch (Exception e1) 
										{
											System.err.println("EdgeHistogram b is not an valid EdgeHistogram: " + e1.getMessage());
										}
										difference[i]= difference[i]+ (double) EdgeHistogramImplementation.calculateDistance(a.getHistogram(), b.getHistogram());
										//diffs[counter][i] = (double) EdgeHistogram.getSimilarity(a.getHistogram(), b.getHistogram());
									}
								}
							} 
							else if (descType.equals("ScalableColorType")) 
							{
							//     	debug("Comparing ScalableColor ...");
								String numC, numB;
								numC = VisualDescriptor.getAttributeValue("numOfCoeff");
								numB = VisualDescriptor.getAttributeValue("numOfBitplanesDiscarded");
								for (Iterator iter = l.iterator(); iter.hasNext();) 
								{
									Element elem = (Element) iter.next();
									if (elem.getAttributeValue("type", xsi).equals("ScalableColorType")) 
									{ // Ists der richtige D?
										if (numC.equals(elem.getAttributeValue("numOfCoeff"))
											&& numB.equals(elem.getAttributeValue("numOfBitplanesDiscarded"))) 
										{ // Stimmen die Zahlen?
											if (!(scc != null)) 
											{
												scc = new ScalableColor(VisualDescriptor);
											}
											difference[i]= difference[i]+scc.getSimilarity(new ScalableColor(elem));
											//diffs[counter][i] = scc.getSimilarity(new ScalableColor(elem));
											//debug(diffs[counter][i] + " - " + descriptions[i]);
										} 
										else 
										{
											debug("Not matched (numC,numB): " + "(" + numC + "," + numB + ") to (" +
												elem.getAttributeValue("numOfCoeff") + "," +
												elem.getAttributeValue("numOfBitplanesDiscarded") + ")");
										}
									}
								}
								//difference[i]= difference[i]+diffs[counter][i];
							}
							
	                    }
							
						
						if (progress != null)
						{
							progress.setValue(i);
						}						
					} 
					catch (JDOMException e1) 
					{
						debug("Error building document " + descriptions[i]);
					} 
					catch (Exception e2) 
					{
					e2.printStackTrace();
					}
				}
			}
			
				for (int i=0; i< difference.length;i++){
					Element e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
					difference[i]= difference[i]/VisualDescriptorSet.size();
				
					if (difference[i] > -1) {
                        if (i == maxResults) {
                            Arrays.sort(maxdiff);
                        }
                        if (i < maxResults) {
                            maxdiff[i] = difference[i];
                            results.add(new ResultListEntry(difference[i], e, descriptions[i]));
                        } else {
                            if (difference[i] <= maxdiff[maxResults - 1] || maxdiff[maxResults - 1] < 0) {
                                results.add(new ResultListEntry(difference[i], e, descriptions[i]));
                                maxdiff[maxResults - 1] = difference[i];
                                for (int jj = maxResults - 1; jj > 0; jj--) {
                                    // bubble up :)
                                    if (maxdiff[jj - 1] > maxdiff[jj]) {
                                        double tmp = maxdiff[jj - 1];
                                        maxdiff[jj - 1] = maxdiff[jj];
                                        maxdiff[jj] = tmp;
                                    }
                                }
                            }
                        }
                    }
					//results.add(new ResultListEntry(difference[i], e, descriptions[i]));
				}		
			// setting up results:
			
			if (progress != null) 
			{
				progress.setMinimum(0);
				progress.setMaximum(descriptions.length);
				progress.setValue(0);
				progress.setString("Sorting images");
			}
			// Sorting Vector ...
			Collections.sort(results, new Comparator() 
			{
				public int compare(Object o1, Object o2) 
				{
					int retVal = 0;
					double rel = ((ResultListEntry) o1).getRelevance() - ((ResultListEntry) o2).getRelevance();
					if (rel < 0) retVal = -1;
					if (rel > 0) retVal = 1;
					return retVal;
				}
			});
		} 
		catch (Exception e) 
		{
			debug("Couldn't find descriptions ... " + e.getMessage());
		}
		return results;
	}

	
	

    public List<ResultListEntry> getImagesByXPathSearch(String xPath, String whereToSearch,
                                         boolean recursive, JProgressBar progress) {

        Vector results = new Vector();
        try {
            if (progress != null)
                progress.setString("Finding MPEG-7 files ...");
            String[] descriptions = FileOperations.getAllDescriptions(new File(whereToSearch), recursive);
            double[] diffs = new double[descriptions.length];
            for (int i = 0; i < diffs.length; i++) {
                diffs[i] = -1.0;
            }
            SAXBuilder builder = new SAXBuilder();
            if (progress != null) {
                progress.setMinimum(0);
                progress.setMaximum(descriptions.length);
                progress.setValue(0);
                progress.setString("Searching for XPath ...");
            }
            double[] maxdiff = new double[maxResults];
            for (int i = 0; i < maxdiff.length; i++) {
                maxdiff[i] = -1.0;
            }
            for (int i = 0; i < descriptions.length; i++) {
                Element e = null;
                try {
                    e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
                    java.util.List l = RetrievalToolkit.xpathQuery(e, xPath, null);
                    if (l.size() > 0) {
                        diffs[i] = 1.0 / (double) l.size();
                        if (i == maxResults) {
                            Arrays.sort(maxdiff);
                        }
                        if (i < maxResults) {
                            maxdiff[i] = diffs[i];
                            results.add(new ResultListEntry(diffs[i], e, descriptions[i]));
                        } else {
                            if (diffs[i] <= maxdiff[maxResults - 1] || maxdiff[maxResults - 1] < 0) {
                                results.add(new ResultListEntry(diffs[i], e, descriptions[i]));
                                maxdiff[maxResults - 1] = diffs[i];
                                for (int jj = maxResults - 1; jj > 0; jj--) {
                                    // bubble up :)
                                    if (maxdiff[jj - 1] > maxdiff[jj]) {
                                        double tmp = maxdiff[jj - 1];
                                        maxdiff[jj - 1] = maxdiff[jj];
                                        maxdiff[jj] = tmp;
                                    }
                                }
                            } else {
                                debug("Result omitted!");
                            }
                        }
                    }
                    l = null;
                    if (progress != null)
                        progress.setValue(i);
                } catch (JDOMException e1) {
                    debug("Error building document " + descriptions[i]);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            // setting up results:

            if (progress != null) {
                progress.setMinimum(0);
                progress.setMaximum(descriptions.length);
                progress.setValue(0);
                progress.setString("Sorting images");
            }
            // Sorting Vector ...
            Collections.sort(results, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int retVal = 0;
                    double rel = ((ResultListEntry) o1).getRelevance() - ((ResultListEntry) o2).getRelevance();
                    if (rel < 0) retVal = -1;
                    if (rel > 0) retVal = 1;
                    return retVal;
                }
            });

        } catch (Exception e) {
            debug("Couldn't find descriptions ... " + e.getMessage());
            e.printStackTrace(System.out);
        }
        return results;
    }

    public List<ResultListEntry> getSimilarImages(Element VisualDescriptor, String whereToSearch,
                                   boolean recursive, JProgressBar progress) {
        List<ResultListEntry> results = new LinkedList<ResultListEntry>();
        Namespace mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        String descType = VisualDescriptor.getAttributeValue("type", xsi);
        try {
            if (progress != null)
                progress.setString("Finding MPEG-7 files ...");
            String[] descriptions = FileOperations.getAllDescriptions(new File(whereToSearch), recursive);
            double[] diffs = new double[descriptions.length];
            for (int i = 0; i < diffs.length; i++) {
                diffs[i] = -1.0;
            }
            SAXBuilder builder = new SAXBuilder();
            if (progress != null) {
                progress.setMinimum(0);
                progress.setMaximum(descriptions.length);
                progress.setValue(0);
                progress.setString("Comparing images");
            }
            double[] maxdiff = new double[maxResults];
            for (int i = 0; i < maxdiff.length; i++) {
                maxdiff[i] = -1.0;
            }
            ScalableColor scc = null;
            for (int i = 0; i < descriptions.length; i++) {
                Element e = null;
                try {
                    e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
                    java.util.List l = RetrievalToolkit.xpathQuery(e, "//VisualDescriptor", null);
                    if (l.size() > 0) {
//                        debug("Comparing elements ...");
                        if (descType.equals("ColorLayoutType")) {
//                            debug("Comparing ColorLayout ...");
                            Element toCompare = null;
                            for (Iterator iter = l.iterator(); iter.hasNext();) {
                                Element elem = (Element) iter.next();
                                if (elem.getAttributeValue("type", xsi).equals("ColorLayoutType")) {

                                    diffs[i] = ColorLayout.getSimilarity(VisualDescriptor, (Element) l.get(0));
                                }
                            }
                        } else if (descType.equals("EdgeHistogramType")) {
                            EdgeHistogram a = null;
                            try {
                                a = new EdgeHistogram(VisualDescriptor);
                            } catch (Exception e1) {
                                System.err.println("EdgeHistogram a is not an valid EdgeHistogram: " + e1.getMessage());
                            }
                            for (Iterator iter = l.iterator(); iter.hasNext();) {
                                Element elem = (Element) iter.next();
                                if (elem.getAttributeValue("type", xsi).equals("EdgeHistogramType")) {
                                    EdgeHistogram b = null;
                                    try {
                                        b = new EdgeHistogram(elem);
                                    } catch (Exception e1) {
                                        System.err.println("EdgeHistogram b is not an valid EdgeHistogram: " + e1.getMessage());
                                    }
                                    diffs[i] = (double) EdgeHistogramImplementation.calculateDistance(a.getHistogram(), b.getHistogram());
                                }
                            }
                        } else if (descType.equals("ScalableColorType")) {
//                            debug("Comparing ScalableColor ...");
                            String numC, numB;
                            numC = VisualDescriptor.getAttributeValue("numOfCoeff");
                            numB = VisualDescriptor.getAttributeValue("numOfBitplanesDiscarded");
                            for (Iterator iter = l.iterator(); iter.hasNext();) {
                                Element elem = (Element) iter.next();
                                if (elem.getAttributeValue("type", xsi).equals("ScalableColorType")) { // Ists der richtige D?
                                    if (numC.equals(elem.getAttributeValue("numOfCoeff"))
                                            && numB.equals(elem.getAttributeValue("numOfBitplanesDiscarded"))) { // Stimmen die Zahlen?
                                        if (!(scc != null)) {
                                            scc = new ScalableColor(VisualDescriptor);
                                        }
                                        diffs[i] = scc.getSimilarity(new ScalableColor(elem));
                                        debug(diffs[i] + " - " + descriptions[i]);
                                    } else {
                                        debug("Not matched (numC,numB): " + "(" + numC + "," + numB + ") to (" +
                                                elem.getAttributeValue("numOfCoeff") + "," +
                                                elem.getAttributeValue("numOfBitplanesDiscarded") + ")");
                                    }
                                }
                            }
                        }
                        if (diffs[i] > -1) {
                            if (i == maxResults) {
                                Arrays.sort(maxdiff);
                            }
                            if (i < maxResults) {
                                maxdiff[i] = diffs[i];
                                results.add(new ResultListEntry(diffs[i], e, descriptions[i]));
                            } else {
                                if (diffs[i] <= maxdiff[maxResults - 1] || maxdiff[maxResults - 1] < 0) {
                                    results.add(new ResultListEntry(diffs[i], e, descriptions[i]));
                                    maxdiff[maxResults - 1] = diffs[i];
                                    for (int jj = maxResults - 1; jj > 0; jj--) {
                                        // bubble up :)
                                        if (maxdiff[jj - 1] > maxdiff[jj]) {
                                            double tmp = maxdiff[jj - 1];
                                            maxdiff[jj - 1] = maxdiff[jj];
                                            maxdiff[jj] = tmp;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (progress != null)
                        progress.setValue(i);
                } catch (JDOMException e1) {
                    debug("Error building document " + descriptions[i]);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            // setting up results:

            if (progress != null) {
                progress.setMinimum(0);
                progress.setMaximum(descriptions.length);
                progress.setValue(0);
                progress.setString("Sorting images");
            }
            // Sorting Vector ...
            Collections.sort(results, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int retVal = 0;
                    double rel = ((ResultListEntry) o1).getRelevance() - ((ResultListEntry) o2).getRelevance();
                    if (rel < 0) retVal = -1;
                    if (rel > 0) retVal = 1;
                    return retVal;
                }
            });

        } catch (Exception e) {
            debug("Couldn't find descriptions ... " + e.getMessage());
        }
        return results;
    }

    public List<ResultListEntry> getImagesBySemantics(String xPath, Vector objects, String whereToSearch,
                                                      boolean recursive, JProgressBar progress) {

        LinkedList<ResultListEntry> results = new LinkedList<ResultListEntry>();
        boolean isResult = false;
        try {
            if (progress != null)
                progress.setString("Finding MPEG-7 files ...");
            String[] descriptions = FileOperations.getAllDescriptions(new File(whereToSearch), recursive);
            double[] diffs = new double[descriptions.length];
            for (int i = 0; i < diffs.length; i++) {
                diffs[i] = -1.0;
            }
            SAXBuilder builder = new SAXBuilder();
            if (progress != null) {
                progress.setMinimum(0);
                progress.setMaximum(descriptions.length);
                progress.setValue(0);
                progress.setString("Searching for XPath ...");
            }
            double[] maxdiff = new double[maxResults];
            for (int i = 0; i < maxdiff.length; i++) {
                maxdiff[i] = -1.0;
            }
            for (int i = 0; i < descriptions.length; i++) {
                Element e = null;
                try {
                    e = builder.build(new FileInputStream(descriptions[i])).getRootElement();
                    java.util.List l = RetrievalToolkit.xpathQuery(e, xPath, null);
                    isResult = false;
                    if (l.size() > 0) {
                        isResult = true;
                        Element semantics = (Element) l.get(0);
                        Namespace mpeg7 = semantics.getNamespace();

                        java.util.List rels = semantics.getChild("Graph", mpeg7).getChildren("Relation", mpeg7);
                        java.util.List obje = semantics.getChildren("SemanticBase", mpeg7);

                        String rel1 = (String) objects.get(3);
                        String rel2 = (String) objects.get(4);

                        boolean rel1OK = false;
                        boolean rel2OK = false;

                        debug("found Element matching XPath Statement ....");
                        // checking relation 1
                        if (rel1.length() > 0) {
                            if (rel1.equals("*")) {
                                for (Iterator it1 = rels.iterator(); it1.hasNext();) {
                                    Element relation = (Element) it1.next();
                                    // test if objects are referenced:
                                    boolean inverse = false;
                                    if (((String) objects.get(5)).length() > 0) inverse = true;
                                    if (isValidRelation(relation, (String) objects.get(0), (String) objects.get(1), obje, inverse, 1)) {
                                        rel1OK = true;
                                    }
                                }
                            } else {
                                for (Iterator it1 = rels.iterator(); it1.hasNext();) {
                                    Element relation = (Element) it1.next();
                                    if (relation.getAttributeValue("type").endsWith(":" + rel1)) {
                                        // test if objects are referenced:
                                        boolean inverse = false;
                                        if (((String) objects.get(5)).length() > 0) inverse = true;
                                        if (isValidRelation(relation, (String) objects.get(0), (String) objects.get(1), obje, inverse, 1)) {
                                            rel1OK = true;
                                        }
                                    }
                                }
                            }
                        } else {
                            rel1OK = true;
                        }

                        // Checking relation 2
                        if (rel2.length() > 0) {
                            if (rel2.equals("*")) {
                                for (Iterator it1 = rels.iterator(); it1.hasNext();) {
                                    Element relation = (Element) it1.next();
                                    // test if objects are referenced:
                                    boolean inverse = false;
                                    if (((String) objects.get(6)).length() > 0) inverse = true;
                                    if (isValidRelation(relation, (String) objects.get(1), (String) objects.get(2), obje, inverse, 2)) {
                                        rel2OK = true;
                                    }
                                }
                            } else {
                                for (Iterator it1 = rels.iterator(); it1.hasNext();) {
                                    Element relation = (Element) it1.next();
                                    if (relation.getAttributeValue("type").endsWith(":" + rel2)) {
                                        // test if objects are referenced:
                                        boolean inverse = false;
                                        if (((String) objects.get(6)).length() > 0) inverse = true;
                                        if (isValidRelation(relation, (String) objects.get(1), (String) objects.get(2), obje, inverse, 2)) {
                                            rel2OK = true;
                                        }
                                    }
                                }
                            }
                        } else {
                            rel2OK = true;
                        }

                        if (rel2.length() > 0) {
                        }

                        isResult = rel1OK && rel2OK;
                    }
                    if (isResult) {
                        diffs[i] = 1.0 / (double) l.size();
                        if (i == maxResults) {
                            Arrays.sort(maxdiff);
                        }
                        if (i < maxResults) {
                            maxdiff[i] = diffs[i];
                            results.add(new ResultListEntry(diffs[i], e, descriptions[i]));
                        } else {
                            if (diffs[i] <= maxdiff[maxResults - 1] || maxdiff[maxResults - 1] < 0) {
                                results.add(new ResultListEntry(diffs[i], e, descriptions[i]));
                                maxdiff[maxResults - 1] = diffs[i];
                                for (int jj = maxResults - 1; jj > 0; jj--) {
                                    // bubble up :)
                                    if (maxdiff[jj - 1] > maxdiff[jj]) {
                                        double tmp = maxdiff[jj - 1];
                                        maxdiff[jj - 1] = maxdiff[jj];
                                        maxdiff[jj] = tmp;
                                    }
                                }
                            } else {
                                debug("Result omitted!");
                            }
                        }
                    }
                    l = null;
                    if (progress != null)
                        progress.setValue(i);
                } catch (JDOMException e1) {
                    debug("Error building document " + descriptions[i]);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            // setting up results:

            if (progress != null) {
                progress.setMinimum(0);
                progress.setMaximum(descriptions.length);
                progress.setValue(0);
                progress.setString("Sorting images");
            }
            // Sorting Vector ...
            Collections.sort(results, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int retVal = 0;
                    double rel = ((ResultListEntry) o1).getRelevance() - ((ResultListEntry) o2).getRelevance();
                    if (rel < 0) retVal = -1;
                    if (rel > 0) retVal = 1;
                    return retVal;
                }
            });

        } catch (Exception e) {
            debug("Couldn't find descriptions ... " + e.getMessage());
            e.printStackTrace(System.out);
        }
        return results;
    }

    private void debug(String message) {
        if (RetrievalFrame.DEBUG)
            System.out.println("[at.lux.fotoretrieval.retrievalengines.FileSystemRetrieval] " + message);
    }

    private boolean isValidRelation(Element relation, String obj1, String obj2, java.util.List objects, boolean inverse, int numRelation) {
        boolean isValid = false;
        if (inverse) debug("Searching for inverse relation");
        Namespace mpeg7 = relation.getNamespace();
        String src = relation.getAttributeValue("source");
        String tgt = relation.getAttributeValue("target");
        Element target = null;
        Element source = null;
        for (Iterator iterator = objects.iterator(); iterator.hasNext();) {
            Element obj = (Element) iterator.next();
            String id = obj.getAttributeValue("id");
            if (id != null) {
                if (tgt.substring(1).equals(id)) {
                    target = obj;
//                    debug("Target: " + obj.getChild("Label", mpeg7).getChildTextTrim("Name", mpeg7));
                }
                if (src.substring(1).equals(id)) {
                    source = obj;
//                    debug("Source: " + obj.getChild("Label", mpeg7).getChildTextTrim("Name", mpeg7));
                }
            }

        }

        debug("Checking relation: " + relation.getAttributeValue("type") + " src: " + source.getChild("Label", mpeg7).getChildTextTrim("Name", mpeg7) + ", tgt: " + target.getChild("Label", mpeg7).getChildTextTrim("Name", mpeg7));
        if (!inverse) {
            debug("for source contains '" + obj1 + "' and target contains '" + obj2 + "'");
        } else {
            debug("for source contains '" + obj2 + "' and target contains '" + obj1 + "'");
        }

        if (target != null && source != null) {
            int hits1 = 1;
            if (!obj1.equals("*")) {
                if (!inverse) {
                    hits1 = RetrievalToolkit.xpathQuery(source, "*[contains(descendant-or-self::node(),'" + obj1 + "')]", null).size();
                } else {
                    hits1 += RetrievalToolkit.xpathQuery(target, "*[contains(descendant-or-self::node(),'" + obj1 + "')]", null).size();
                }
            }
            int hits2 = 1;
            if (!obj2.equals("*")) {
                if (!inverse) {
                    hits2 = RetrievalToolkit.xpathQuery(target, "*[contains(descendant-or-self::node(),'" + obj2 + "')]", null).size();
                } else {
                    hits2 += RetrievalToolkit.xpathQuery(source, "*[contains(descendant-or-self::node(),'" + obj2 + "')]", null).size();
                }
            }
            if (hits1 > 0 && hits2 > 0) {
                isValid = true;
                if (numRelation == 1) {
                    if (inverse) {
                        matches1.add(source);
                    } else {
                        matches1.add(target);
                    }
                } else {

                    if (inverse) {
                        isValid = matches1.contains(target);
                    } else {
                        isValid = matches1.contains(source);
                    }
//                    isValid = (matches1.contains(target) || matches1.contains(source));

                }
                debug("Match detected!");
            }
        }
        return isValid;
    }
}
