package net.semanticmetadata.lire.imageanalysis.mser;

import at.lux.imageanalysis.VisualDescriptor;
import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;

/**
 * Feature describing an MSER
 * <p/>
 * Date: 27.03.2011
 * Time: 10:00:08
 *
 * @author Christine Keim, christine.keim@inode.at
 */
public class MSERFeature extends Histogram implements LireFeature {
    MSERGrowthHistory mser;

    public MSERFeature(MSERGrowthHistory maxStableExtremalRegion, float[] invariants) {
        this.mser = maxStableExtremalRegion;
        descriptor = invariants;
    }

    public MSERFeature() {
        mser = null;
    }

    public void extract(BufferedImage bimg) {
        throw new RuntimeException("Extraction not available here");
        // does nothing ....
    }

    public float getDistance(VisualDescriptor visualDescriptor) {
        if (!(visualDescriptor instanceof MSERFeature)) return -1;
        return MetricsUtils.distL2(descriptor, ((MSERFeature) visualDescriptor).descriptor);
    }

    public String getStringRepresentation() {
        throw new UnsupportedOperationException("not implemented due to performance issues");
    }

    public void setStringRepresentation(String s) {
        throw new UnsupportedOperationException("not implemented due to performance issues");
    }

    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toBytes(descriptor);
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        descriptor = SerializationUtils.toFloatArray(in);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < descriptor.length; i++) {
            float v = descriptor[i];
            sb.append(v);
            sb.append(' ');

        }
        return sb.toString();
    }
}
