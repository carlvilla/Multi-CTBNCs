package es.upm.fi.cig.multictbnc.conceptdriftdetection;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.UserInterfaceUtil;

import java.util.List;
import java.util.stream.DoubleStream;

/**
 * This class implements a concept drift adaptive method that operates globally on a MultiCTBNC model. The method
 * utilizes a Page-Hinkley test to detect significant changes in the log-likelihood score of the model, indicating
 * possible concept drifts. Upon detection, the model is adapted.
 */
public class ConceptDriftGloballyAdaptiveMethod extends ConceptDriftAdaptiveMethod {
    PageHinkleyTest pageHinkleyTest;

    /**
     * Initializes the globally adaptive concept drift method with specified parameters.
     *
     * @param namesVariables         list of variable names used in the model
     * @param conceptDriftScore      scoring mechanism to detect concept drift
     * @param magnitudeThreshold     threshold for magnitude change in the Page-Hinkley test
     * @param detectionThreshold     threshold for drift detection in the Page-Hinkley test
     * @param resetAfterConceptDrift flag to reset the Page-Hinkley test after detecting a drift
     * @param windowSize             size of the window for the Page-Hinkley test
     * @param showCharts             flag indicating whether to display drift detection charts
     * @param title                  title for the drift detection charts
     */
    public ConceptDriftGloballyAdaptiveMethod(List<String> namesVariables, ConceptDriftScore conceptDriftScore,
                                              double magnitudeThreshold, double detectionThreshold,
                                              boolean resetAfterConceptDrift, int windowSize, boolean showCharts,
                                              String title) {
        super(conceptDriftScore, namesVariables, detectionThreshold, showCharts, title);
        this.pageHinkleyTest = new PageHinkleyTest(magnitudeThreshold, detectionThreshold, resetAfterConceptDrift,
                windowSize);
    }

    /**
     * Sets up line charts for visualizing concept drift detection results if the charts are enabled.
     *
     * @param title              title for the line charts
     * @param detectionThreshold threshold value for detecting concept drifts
     */
    protected void setUpLineChart(String title, double detectionThreshold) {
        if (this.lineChartLocalLogLikelihood == null && this.showCharts) {
            this.lineChartLocalLogLikelihood = UserInterfaceUtil.createXYLineChart(
                    "Global Average Log-likelihood Evolution - " + title, "Batch Number",
                    "Global Average Log-likelihood", null, "Model");
            this.lineChartPageHinkley = UserInterfaceUtil.createXYLineChart("Page Hinkley Value Evolution - " + title,
                    "Batch Number", "Page Hinkley Value", null, "Model");
            this.lineChartPageHinkley.addHorizontalValueMarker(detectionThreshold);
        }
    }

    /**
     * Adapts the provided MultiCTBNC model based on the new data batch. It detects concept drifts and updates the
     * model.
     *
     * @param model    MultiCTBNC model to be adapted.
     * @param newBatch new data batch for concept drift detection and model adaptation
     * @return {@code true} if a concept drift is detected and the model is adapted, {@code false} otherwise
     * @throws ErroneousValueException if an error occurs during the adaptation process
     */
    public boolean adaptModel(MultiCTBNC<CPTNode, CIMNode> model, Dataset newBatch) throws ErroneousValueException {
        double globalAvgLogLikelihood = computeGlobalAvgLogLikelihood(model, newBatch);
        boolean isChangeDetected = detectChanges(globalAvgLogLikelihood);
        if (isChangeDetected)
            this.timeLastUpdate = model.learn(newBatch);
        else
            this.timeLastUpdate = 0;
        return isChangeDetected;
    }

    @Override
    public String getResults() {
        return "";
    }

    @Override
    public List<Node> getLastChangedNodes() {
        return null;
    }

    private double computeGlobalAvgLogLikelihood(MultiCTBNC<CPTNode, CIMNode> model, Dataset newBatch) {
        double[] avgLocalLogLikelihoods = this.conceptDriftScore.compute(model, newBatch, model.getNameVariables());
        double globalAvgLogLikelihood = DoubleStream.of(avgLocalLogLikelihoods).sum();
        if (this.showCharts)
            this.lineChartLocalLogLikelihood.update(this.numProcessedBatches, globalAvgLogLikelihood);
        // Update the number of processed batches
        this.numProcessedBatches++;
        return globalAvgLogLikelihood;
    }

    private boolean detectChanges(double globalAvgLogLikelihood) {
        boolean isChangeDetected = pageHinkleyTest.detectChange(globalAvgLogLikelihood);
        if (this.showCharts) {
            this.lineChartPageHinkley.update(this.numProcessedBatches, pageHinkleyTest.getPageHinkleyValue());
            if (isChangeDetected)
                this.lineChartLocalLogLikelihood.addVerticalValueMarker(this.numProcessedBatches);
        }
        return isChangeDetected;
    }

}
