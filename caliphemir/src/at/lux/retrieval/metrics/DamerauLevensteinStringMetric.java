package at.lux.retrieval.metrics;

import at.lux.retrieval.metrics.impl.DLMetricDynamic;

/**
 * @author Mathias Lux, mlux@know-center.at
 */
public class DamerauLevensteinStringMetric implements StringMetric {
    public float getDistance(String str1, String str2) {
        if (Math.min(str1.length(), str2.length()) < 1) return 0f;
        DLMetricDynamic dl = new DLMetricDynamic();
        return (float) dl.calculateDistance(str1, str2);
    }

    public float getSimilarity(String str1, String str2) {
        return 1f/(1f+getDistance(str1, str2));
    }
}
