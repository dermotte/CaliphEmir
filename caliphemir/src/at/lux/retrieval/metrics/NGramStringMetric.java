package at.lux.retrieval.metrics;

import at.lux.retrieval.metrics.impl.NGramSimilarity;

/**
 * This class provides a metric based on n-grams. The input strings are converted
 * to vectors based on their n-sized substrings with pre- and appended whitespaces.
 * The similarity is calculated by computing the cosine coefficient between them<p/>
 * Example:<br>
 * "house" is converted to (" HO", "HOU", "OUS", "USE", "SE ") <br>
 * "mouse" is converted to (" MO", "MOU", "OUS", "USE", "SE ") <br>
 * So the vecors are:<br>
 * (1,1,1,1,1,0,0) <br>
 * (0,0,1,1,1,1,1) <br>
 * with the dimensions (" HO", "HOU", "OUS", "USE", "SE ", " MO", "MOU")<p>
 * Date: 01.09.2005 <br>
 * Time: 10:19:02 <br>
  * @author Mathias Lux, mlux@know-center.at
 */
public class NGramStringMetric implements StringMetric{
    public float getSimilarity(String str1, String str2) {
        // if no ngram can be created return a similarity of 0.
        String s1 = " " + str1.trim() + " ";
        String s2 = " " + str2.trim() + " ";
        if (Math.min(s1.length(), s2.length()) < 3) return 0f;
        NGramSimilarity ngrams = new NGramSimilarity();
        return ngrams.getSimilarity(s1, s2);
    }

    public float getDistance(String str1, String str2) {
        return 1f-getSimilarity(str1, str2);
    }
}
