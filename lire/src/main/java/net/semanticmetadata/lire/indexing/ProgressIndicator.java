package net.semanticmetadata.lire.indexing;

public class ProgressIndicator {

    int numDocsAll = 0;
    int numDocsProcessed = 0;

    MetricSpacesInvertedListIndexing.State currentState = MetricSpacesInvertedListIndexing.State.Idle;

    public int getNumDocsAll() {
        return numDocsAll;
    }

    public void setNumDocsAll(int numDocsAll) {
        this.numDocsAll = numDocsAll;
    }

    public int getNumDocsProcessed() {
        return numDocsProcessed;
    }

    public void setNumDocsProcessed(int numDocsProcessed) {
        this.numDocsProcessed = numDocsProcessed;
    }

    public MetricSpacesInvertedListIndexing.State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(MetricSpacesInvertedListIndexing.State currentState) {
        this.currentState = currentState;
    }
}