package net.semanticmetadata.lire.utils;

import com.sun.org.apache.bcel.internal.generic.NEW;

import javax.xml.transform.Result;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Mathias Lux, mathias@juggle.at
 * Date: 28.07.11
 * Time: 10:48
 */
public class ConversionUtils {
    /**
     * Converts a float array to a double array the "bad way".
     * However, it's sufficient for tests right now.
     * @param in
     * @return
     */
    public static double[] toDouble(float[] in) {
        double[] result = new double[in.length];
        for (int i = 0; i < in.length; i++) {
              result[i] = (double) in[i];

        }
        return result;
    }

    public static double[] toDouble(int[] in) {
        double[] result = new double[in.length];
        for (int i = 0; i < in.length; i++) {
              result[i] = (double) in[i];

        }
        return result;
    }
}
