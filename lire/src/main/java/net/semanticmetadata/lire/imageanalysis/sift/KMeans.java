/*
 * This file is part of the LIRE project: http://www.SemanticMetadata.net/lire.
 *
 * Lire is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Lire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lire; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * Note, that the SIFT-algorithm is protected by U.S. Patent 6,711,293: "Method and
 * apparatus for identifying scale invariant features in an image and use of same for
 * locating an object in an image" by the University of British Columbia. That is, for
 * commercial applications the permission of the author is required.
 *
 * (c) 2008-2010 by Mathias Lux, mathias@juggle.at
 */
package net.semanticmetadata.lire.imageanalysis.sift;

import net.semanticmetadata.lire.imageanalysis.Histogram;

import java.util.*;

/**
 * ...
 * Date: 23.09.2008
 * Time: 12:41:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class KMeans {
    private List<Image> images = new LinkedList<Image>();
    private int count = 0, numClusters = 256;
    private ArrayList<Histogram> features = null;
    private Cluster[] clusters = null;
    private HashMap<Histogram, Integer> featureIndex = null;

    public KMeans() {

    }

    public KMeans(int numClusters) {
        this.numClusters = numClusters;
    }

    public void addImage(String identifier, List<Histogram> features) {
        images.add(new Image(identifier, features));
        count += features.size();
    }

    public int getFeatureCount() {
        return count;
    }

    public void init() {
        // create a set of all features:
        features = new ArrayList<Histogram>(count);
        for (Iterator<Image> imageIterator = images.iterator(); imageIterator.hasNext();) {
            Image image = imageIterator.next();
            if (image.features.size()>0)
            for (Iterator<Histogram> iterator = image.features.iterator(); iterator.hasNext();) {
                Histogram histogram = iterator.next();
                for (int i = 0; i < histogram.descriptor.length; i++) {
                    if (Float.isNaN(histogram.descriptor[i])) {
                        System.err.println("Found a NaN in init");
                        System.out.println("image.identifier = " + image.identifier);
                        for (int j = 0; j < histogram.descriptor.length; j++) {
                            float v = histogram.descriptor[j];
                            System.out.print(v + ", ");
                        }
                        System.out.println("");
                    }

                }
                features.add(histogram);
            }
            else {
                System.err.println("Image with no MSER features: " + image.identifier);
            }
        }
        // find first clusters:
        clusters = new Cluster[numClusters];
        Set<Integer> medians = selectInitialMedians(numClusters);
        Iterator<Integer> mediansIterator = medians.iterator();
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new Cluster();
            float[] descriptor = features.get(mediansIterator.next()).descriptor;
            for (int j = 0; j < descriptor.length; j++) {
                if (Float.isNaN(descriptor[j]))
                    System.err.println("There is a NaN in the init medians!");

            }
            System.arraycopy(descriptor, 0, clusters[i].mean, 0, descriptor.length);
        }
//        reOrganizeFeatures();
//        recomputeMeans();
    }

    private Set<Integer> selectInitialMedians(int numClusters) {
        HashSet<Integer> medians = new HashSet<Integer>();
        while (medians.size() < numClusters && medians.size() < features.size()) {
            medians.add((int)(Math.random() * (double) numClusters));
        }
        return medians;
    }

    /**
     * Do one step and return the overall stress (squared error). You should do this until
     * the error is below a threshold or doesn't change a lot in between two subsequent steps.
     *
     * @return
     */
    public double clusteringStep() {
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].members.clear();
        }
        reOrganizeFeatures();
        recomputeMeans();
        return overallStress();
    }

    /**
     * Re-shuffle all features.
     */
    private void reOrganizeFeatures() {
        for (int k = 0; k < features.size(); k++) {
            Histogram f = features.get(k);
            Cluster best = clusters[0];
            double minDistance = clusters[0].getDistance(f);
            for (int i = 1; i < clusters.length; i++) {
                double v = clusters[i].getDistance(f);
                if (minDistance > v) {
                    best = clusters[i];
                    minDistance = v;
                }
            }
            best.members.add(k);
        }
    }

    /**
     * Computes the mean per cluster (averaged vector)
     */
    private void recomputeMeans() {
        int length = features.get(0).descriptor.length;
        for (int i = 0; i < clusters.length; i++) {
            float[] mean = clusters[i].mean;
            for (int j = 0; j < length; j++) {
                mean[j] = 0;
                for (Integer member : clusters[i].members) {
                    mean[j] += features.get(member).descriptor[j];
                }
                if (clusters[i].members.size()>1)
                    mean[j] = mean[j] / clusters[i].members.size();
            }
        }
    }

    /**
     * Squared error in classification.
     *
     * @return
     */
    private double overallStress() {
        double v = 0;
        int length = features.get(0).descriptor.length;

        for (int i = 0; i < clusters.length; i++) {
            for (Integer member : clusters[i].members) {
                float tmpStress = 0;
                for (int j = 0; j < length; j++) {
//                    if (Float.isNaN(features.get(member).descriptor[j])) System.err.println("Error: there is a NaN in cluster " + i + " at member " + member);
                    float f = Math.abs(clusters[i].mean[j] - features.get(member).descriptor[j]);
                    tmpStress += f ;
                }
                v += tmpStress;
            }
        }
        return v;
    }

    public Cluster[] getClusters() {
        return clusters;
    }

    public List<Image> getImages() {
        return images;
    }

    /**
     * Set the number of desired clusters.
     *
     * @return
     */
    public int getNumClusters() {
        return numClusters;
    }

    public void setNumClusters(int numClusters) {
        this.numClusters = numClusters;
    }

    private HashMap<Histogram, Integer> createIndex() {
        featureIndex = new HashMap<Histogram, Integer>(features.size());
        for (int i = 0; i < clusters.length; i++) {
            Cluster cluster = clusters[i];
            for (Iterator<Integer> fidit = cluster.members.iterator(); fidit.hasNext();) {
                int fid = fidit.next();
                featureIndex.put(features.get(fid), i);
            }
        }
        return featureIndex;
    }

    /**
     * Used to find the cluster of a feature actually used in the clustering process (so
     * it is known by the k-means class).
     *
     * @param f the feature to search for
     * @return the index of the Cluster
     */
    public int getClusterOfFeature(Histogram f) {
        if (featureIndex == null) createIndex();
        return featureIndex.get(f);
    }
}

class Image {
    public List<Histogram> features;
    public String identifier;
    public float[] localFeatureHistogram = null;
    private final int QUANT_MAX_HISTOGRAM = 256;

    Image(String identifier, List<Histogram> features) {
        this.features = features;
        this.identifier = identifier;
    }

    public float[] getLocalFeatureHistogram() {
        return localFeatureHistogram;
    }

    public void setLocalFeatureHistogram(float[] localFeatureHistogram) {
        this.localFeatureHistogram = localFeatureHistogram;
    }

    public void initHistogram(int bins) {
        localFeatureHistogram = new float[bins];
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            localFeatureHistogram[i] = 0;
        }
    }

    public void normalizeFeatureHistogram() {
        float max = 0;
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            max = Math.max(localFeatureHistogram[i], max);
        }
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            localFeatureHistogram[i] = (localFeatureHistogram[i] * QUANT_MAX_HISTOGRAM) / max;
        }
    }

    public void printHistogram() {
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            System.out.print(localFeatureHistogram[i] + " ");

        }
        System.out.println("");
    }
}
