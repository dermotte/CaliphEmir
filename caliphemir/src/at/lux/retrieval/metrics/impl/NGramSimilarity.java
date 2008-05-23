package at.lux.retrieval.metrics.impl;

/**
 * Title:        NGramSimilarity
 * Description:  provides a function for calculating the "distance" between 2 words.
 * @author Mathias Lux, mlux@know-center.at
 */

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

public class NGramSimilarity {
    HashMap<String, Integer> nGramList1;
    HashMap<String, Integer> nGramList2;
    int i, matchingtrigrams, n;
    Enumeration enum1;
    String tmp;

    /**
     * new Matcher with Trigrams
     */
    public NGramSimilarity() {
        nGramList1 = new HashMap<String, Integer>(12);
        nGramList2 = new HashMap<String, Integer>(12);
        i = 0;
        n = 3;
    }

    /**
     * new Matcher with n-grams
     */
    public NGramSimilarity(int n) {
        nGramList1 = new HashMap<String, Integer>(12);
        nGramList2 = new HashMap<String, Integer>(12);
        i = 0;
        this.n = n;
    }

    /**
     * returns "similarity" betwenn str1 and str2
     */
    public float getSimilarity(String str1, String str2) {
        nGramList1 = calculateNgrams(str1);
        nGramList2 = calculateNgrams(str2);
        HashSet<String> dimensions = new HashSet<String>(nGramList1.size() + nGramList2.size());
        dimensions.addAll(nGramList1.keySet());
        dimensions.addAll(nGramList2.keySet());
        float sum = 0f;
        int sum1 = 0;
        int sum2 = 0;
        for (String dim : dimensions) {
            float factor1 = 0f;
            float factor2 = 0f;
            if (nGramList1.containsKey(dim)) {
                Integer entry = nGramList1.get(dim);
                factor1 = entry;
                sum1 += entry * entry;
            }
            if (nGramList2.containsKey(dim)) {
                Integer entry = nGramList2.get(dim);
                factor2 = entry;
                sum2 += entry * entry;
            }
            sum += factor1 * factor2;
        }
        float upper = sum;
        float lower = (float) Math.sqrt((float) sum1 * (float) sum2);
        return upper / lower;
    }

    private HashMap<String, Integer> calculateNgrams(String str) {
        HashMap<String, Integer> map = new HashMap<String, Integer>(10);
        for (i = 0; i < str.length() - (n - 1); i++) {
            String gram = str.substring(i, i + n).toUpperCase();
            if (map.containsKey(gram)) {
                map.put(gram, map.get(gram) + 1);
            } else
                map.put(gram, 1);
        }
        return map;
    }
}