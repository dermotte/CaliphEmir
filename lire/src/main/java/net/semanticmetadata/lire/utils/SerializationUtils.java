package net.semanticmetadata.lire.utils;

/**
 * Utility class for serialization issues.
 * Created by: Mathias Lux, mathias@juggle.at
 * Date: 19.03.2010
 * Time: 14:58:26
 */
public class SerializationUtils {
    /**
     * Converts a byte array with 4 elements to an int. Used to put ints into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow).
     * Taken from http://www.daniweb.com/code/snippet216874.html
     *
     * @param data the input byte array
     * @return the resulting int
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(int)
     */
    public static int toInt(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        return (int) ( // NOTE: type cast not necessary for int
                (0xff & data[0]) << 24 |
                        (0xff & data[1]) << 16 |
                        (0xff & data[2]) << 8 |
                        (0xff & data[3]) << 0
        );
    }

    /**
     * Converts an int to a byte array with 4 elements. Used to put ints into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow).
     * Taken from http://www.daniweb.com/code/snippet216874.html
     *
     * @param data the int to convert
     * @return the resulting byte array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toInt(byte[])
     */
    public static byte[] toBytes(int data) {
        return new byte[]{
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff),
        };
    }

    /**
     * Converts a float to a byte array with 4 elements. Used to put floats into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toFloat(byte[])} to decode.
     *
     * @param data the float to convert
     * @return the resulting byte array
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toFloat(byte[])
     */
    public static byte[] toBytes(float data) {
        return toBytes(Float.floatToRawIntBits(data));
    }

    /**
     * Converts a byte array with 4 elements to a float. Used to put floats into a byte[] payload in a convenient
     * and fast way by shifting without using streams (which is kind of slow). Use
     * {@link net.semanticmetadata.lire.utils.SerializationUtils#toBytes(float)} to encode.
     *
     * @param data the input byte array
     * @return the resulting float
     * @see net.semanticmetadata.lire.utils.SerializationUtils#toBytes(float)
     */
    public static float toFloat(byte[] data) {
        return Float.intBitsToFloat(toInt(data));
    }
}
