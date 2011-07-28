package net.semanticmetadata.lire.imageanalysis;

import at.lux.imageanalysis.VisualDescriptor;
import com.stromberglabs.jopensurf.SURFInterestPoint;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;

/**
 * Mathias Lux, mathias@juggle.at
 * Date: 29.09.2010
 * Time: 15:44:14
 * To change this template use File | Settings | File Templates.
 */
public class SurfFeature extends Histogram implements LireFeature {
    SURFInterestPoint sip;

    public SurfFeature(SURFInterestPoint surfInterestPoint) {
        this.sip = surfInterestPoint;
        descriptor = sip.getDescriptor();
    }

    public SurfFeature() {
        sip = null;
    }

    public void extract(BufferedImage bimg) {
        // does nothing ....
    }

    public float getDistance(VisualDescriptor visualDescriptor) {
        if (!(visualDescriptor instanceof SurfFeature)) return -1;
        return MetricsUtils.distL2(descriptor, ((SurfFeature) visualDescriptor).descriptor);
    }

    public String getStringRepresentation() {
        throw new UnsupportedOperationException("No implemented!");
    }

    public void setStringRepresentation(String s) {
        throw new UnsupportedOperationException("No implemented!");
    }

    /**
     * Provides a much faster way of serialization.
     * @return a byte array that can be read with the corresponding method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(descriptor);
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     * @param in byte array from corresponding method
     */
    public void setByteArrayRepresentation(byte[] in) {
        descriptor = SerializationUtils.toFloatArray(in);
    }

    public double[] getDoubleHistogram() {
        return ConversionUtils.toDouble(descriptor);
    }

}
