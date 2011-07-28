package net.semanticmetadata.lire.utils;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.sift.Cluster;

import java.io.IOException;

/**
 * Date: 28.09.2010
 * Time: 12:45:27
 * Mathias Lux, mathias@juggle.at
 */
public class SerializationUtilsTest extends TestCase {

    /**
     * Test some basic serialization routines ...
     */
    public void testSerialization() {
        {
            // --- floats
            float[] test = new float[100];
            for (int i = 0; i < test.length; i++) {
                test[i] = (float) (Math.random() * 1000);
            }
            byte[] bytes = SerializationUtils.toByteArray(test);
            float[] floats = SerializationUtils.toFloatArray(bytes);

            for (int i = 0; i < floats.length; i++) {
                assertEquals(floats[i], test[i]);
            }
        }
        {
            // --- doubles

            double[] test = new double[100];
            for (int i = 0; i < test.length; i++) {
                test[i] = (double) (Math.random() * 1000);
            }
            byte[] bytes = SerializationUtils.toByteArray(test);
            double[] floats = SerializationUtils.toDoubleArray(bytes);

            for (int i = 0; i < floats.length; i++) {
                // need to cast to floats due to the loss in precision in conversion.
                assertEquals((float) floats[i], (float) test[i]);
            }
        }
    }

    /**
     * Test serialization of Clusters ...
     */
    public void testClusterSerialization() throws IOException {
        Cluster[] tc = new Cluster[12];
        for (int i = 0; i < tc.length; i++) {
            float[] test = new float[128];
            for (int j = 0; j < test.length; j++) {
                test[j] = (float) (Math.random() * 1000);
            }
            tc[i] = new Cluster(test);
        }

        Cluster.writeClusters(tc, "test-tmp.dat");

        Cluster[] clusters = Cluster.readClusters("test-tmp.dat");

        for (int i = 0; i < clusters.length; i++) {
            System.out.println(clusters[i].toString().equals(tc[i].toString()));
        }
    }
}
