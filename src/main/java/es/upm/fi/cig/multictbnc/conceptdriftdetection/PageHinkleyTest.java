package es.upm.fi.cig.multictbnc.conceptdriftdetection;

import es.upm.fi.cig.multictbnc.data.representation.SlidingWindow;
import es.upm.fi.cig.multictbnc.util.Util;

/**
 * Implements the Page Hinkley Test for concept drift detection.
 *
 * @author Carlos Villa Blanco
 */
public class PageHinkleyTest {
    double magnitudeThreshold;
    double detectionThreshold;
    boolean resetAfterConceptDrift;
    double sumObservedValues;
    double maxCumulatedDifference;
    int numObservedValues;
    double pageHinkleyValue;
    SlidingWindow<Double> slidingWindow;

    /**
     * Initializes the Page Hinkley Test with the specified parameters.
     *
     * @param magnitudeThreshold     magnitude threshold
     * @param detectionThreshold     detection threshold
     * @param resetAfterConceptDrift whether to reset statistics after detecting a concept drift
     * @param windowSize             size of the sliding window for calculations
     */
    public PageHinkleyTest(double magnitudeThreshold, double detectionThreshold, boolean resetAfterConceptDrift,
                           Integer windowSize) {
        this.magnitudeThreshold = magnitudeThreshold;
        this.detectionThreshold = detectionThreshold;
        this.resetAfterConceptDrift = resetAfterConceptDrift;
        this.maxCumulatedDifference = 0;
        this.slidingWindow = new SlidingWindow<>(windowSize);
    }

    /**
     * Detects concept drifts based on a new observation.
     *
     * @param newValue the new observation value
     * @return {@code true} if concept drift is detected, {@code false} otherwise
     */
    public boolean detectChange(double newValue) {
        this.numObservedValues++;
        this.sumObservedValues += newValue;
        double meanObservedValues = this.sumObservedValues / this.numObservedValues;
        double cumulatedDifference = newValue - meanObservedValues - this.magnitudeThreshold;
        this.maxCumulatedDifference = Util.getMaxValue(this.maxCumulatedDifference, cumulatedDifference);
        this.pageHinkleyValue = this.maxCumulatedDifference - cumulatedDifference;
        boolean IsChangeDetected = this.pageHinkleyValue > this.detectionThreshold;
        this.slidingWindow.add(newValue);
        if (IsChangeDetected && this.resetAfterConceptDrift) {
            this.numObservedValues = this.slidingWindow.size();
            this.sumObservedValues = this.slidingWindow.stream().mapToDouble(Double::doubleValue).sum();
            this.maxCumulatedDifference = 0;
        }
        return IsChangeDetected;
    }

    /**
     * Returns the last Page Hinkley value calculated.
     *
     * @return Page Hinkley value
     */
    public double getPageHinkleyValue() {
        return this.pageHinkleyValue;
    }

}
