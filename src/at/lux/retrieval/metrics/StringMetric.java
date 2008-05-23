/*
 * StringMetric.java
 *
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package at.lux.retrieval.metrics;

/**
 * Created on 01. September 2005, 09:44
 * (c) 2005 by Know-Center
 * @author mlux
 */
public interface StringMetric {
    /**
     * Calculates the distance between two Strings. If 0 is returned the Strings are equal.
     * @param str1
     * @param str2
     * @return a positive float
     */
    float getDistance(String str1, String str2);

    /**
     * Calculates the similarity of two Strings. Returns 1 if they are equal. Returns 0 if one of the
     * Strings has a size of 0
     * @param str1
     * @param str2
     * @return a float out of [0,1]
     */
    float getSimilarity(String str1, String str2);
    
}
