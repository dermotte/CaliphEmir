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
 * (c) 2008 by Mathias Lux, mathias@juggle.at
 */
package net.semanticmetadata.lire.imageanalysis.sift;

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
    private int count = 0, numClusters = 32;
    private ArrayList<Feature> features = null;
    private Cluster[] clusters = null;
    private HashMap<Feature, Integer> featureIndex = null;

    public KMeans() {
    }

    public void addImage(String identifier, List<Feature> features) {
        images.add(new Image(identifier, features));
        count += features.size();
    }

    public void init() {
        // create a set of all features:
        features = new ArrayList<Feature>(count);
        for (Iterator<Image> imageIterator = images.iterator(); imageIterator.hasNext();) {
            Image image = imageIterator.next();
            for (Iterator<Feature> iterator = image.features.iterator(); iterator.hasNext();) {
                features.add(iterator.next());
            }
        }
        // find first clusters:
        clusters = new Cluster[numClusters];
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new Cluster();
            System.arraycopy(features.get(i).descriptor, 0, clusters[i].mean, 0, clusters[i].mean.length);
        }
        reOrganizeFeatures();
        recomputeMeans();
    }

    /**
     * Do one step and return the overall stress (squared error). You should do this until
     * the error is below a treshold or doesn't change a lot in between two subsequent steps.
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
            Feature f = features.get(k);
            Cluster best = clusters[0];
            double minDistance = clusters[0].getDistance(f);
            for (int i = 1; i < clusters.length; i++) {
                double v = clusters[i].getDistance(f);
                if (minDistance >= v) {
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
        for (int i = 0; i < clusters.length; i++) {
            float[] mean = clusters[i].mean;
            for (int j = 0; j < mean.length; j++) {
                mean[j] = 0;
                for (Iterator<Integer> fit = clusters[i].members.iterator(); fit.hasNext();) {
                    mean[j] += features.get(fit.next()).descriptor[j];
                }
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
        for (int i = 0; i < clusters.length; i++) {
            for (Integer member : clusters[i].members) {
                float tmpStress = 0;
                for (int j = 0; j < clusters[i].mean.length; j++) {
                    float f = clusters[i].mean[j] - features.get(member).descriptor[j];
                    tmpStress += f * f;
                }
                v += Math.sqrt(tmpStress);
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

    private HashMap<Feature, Integer> createIndex() {
        featureIndex = new HashMap<Feature, Integer>(features.size());
        for (int i = 0; i < clusters.length; i++) {
            Cluster cluster = clusters[i];
            for (Iterator<Integer> fidit = cluster.members.iterator(); fidit.hasNext();) {
                int fid = fidit.next();
                featureIndex.put(features.get(fid), i);
            }
        }
        return featureIndex;
    }

    public int getClusterOfFeature(Feature f) {
        if (featureIndex == null) createIndex();
        return featureIndex.get(f);
    }
}

class Cluster implements Comparable {
    float[] mean;
    HashSet<Integer> members = new HashSet<Integer>();

    Cluster() {
        this.mean = new float[4 * 4 * 8];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
//        sb.append(median).append(": \t");
//        for (int i = 0; i < mean.length; i++) {
//            sb.append(mean[i]);
//            sb.append(", ");
//        }
//        sb.append(" | ");
        for (Integer integer : members) {
            sb.append(integer);
            sb.append(", ");
        }
        return sb.toString();
    }

    public int compareTo(Object o) {
        return ((Cluster) o).members.size() - members.size();
    }

    public double getDistance(Feature f) {
        double d = 0;
        for (int i = 0; i < mean.length; i++) {
            double a = mean[i] - f.descriptor[i];
            d += a * a;
        }
        return Math.sqrt(d);
    }


}

class Image {
    public List<Feature> features;
    public String identifier;
    public int[] localFeatureHistogram = null;
    private final int QUANT_MAX_HISTOGRAM = 64;

    Image(String identifier, List<Feature> features) {
        this.features = features;
        this.identifier = identifier;
    }

    public int[] getLocalFeatureHistogram() {
        return localFeatureHistogram;
    }

    public void setLocalFeatureHistogram(int[] localFeatureHistogram) {
        this.localFeatureHistogram = localFeatureHistogram;
    }

    public void initHistogram(int bins) {
        localFeatureHistogram = new int[bins];
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            localFeatureHistogram[i] = 0;
        }
    }

    public void normalizeFeatureHistogram() {
        int max = 0;
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
