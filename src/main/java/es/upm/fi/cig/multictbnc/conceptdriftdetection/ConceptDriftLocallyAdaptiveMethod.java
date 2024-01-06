package es.upm.fi.cig.multictbnc.conceptdriftdetection;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.UserInterfaceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements a concept drift adaptive method that operates locally on each node of a MultiCTBNC model.
 * The method utilizes a Page-Hinkley test to detect significant changes in the local log-likelihood scores of
 * individual nodes, indicating possible concept drifts. Upon detection, the model is adapted accordingly.
 */
public class ConceptDriftLocallyAdaptiveMethod extends ConceptDriftAdaptiveMethod {
    private static final Logger logger = LogManager.getLogger(ConceptDriftLocallyAdaptiveMethod.class);
    List<PageHinkleyTest> pageHinkleyTests;
    List<Node> lastChangedNodes;


    /**
     * Initializes the locally adaptive concept drift method with specified parameters.
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
    public ConceptDriftLocallyAdaptiveMethod(List<String> namesVariables, ConceptDriftScore conceptDriftScore,
                                             double magnitudeThreshold, double detectionThreshold,
                                             boolean resetAfterConceptDrift, int windowSize, boolean showCharts,
                                             String title) {
        super(conceptDriftScore, namesVariables, detectionThreshold, showCharts, title);
        this.pageHinkleyTests = new ArrayList<>();
        for (int idxVariable = 0; idxVariable < nameVariables.size(); idxVariable++) {
            PageHinkleyTest pageHinkleyTest = new PageHinkleyTest(magnitudeThreshold, detectionThreshold,
                    resetAfterConceptDrift, windowSize);
            pageHinkleyTests.add(pageHinkleyTest);
        }
    }

    /**
     * Sets up line charts for visualizing the evolution of the average local log-likelihood and Page Hinkley values
     * if the charts are enabled.
     *
     * @param title              title for the line charts
     * @param detectionThreshold threshold value for detecting concept drifts
     */
    protected void setUpLineChart(String title, double detectionThreshold) {
        if (this.lineChartLocalLogLikelihood == null && this.showCharts) {
            this.lineChartLocalLogLikelihood = UserInterfaceUtil.createXYLineChart(
                    "Average Local Log-likelihood Evolution - " + title, "Batch Number",
                    "Average Local " + "Log-likelihood", null, this.nameVariables.toArray(new String[0]));
            this.lineChartPageHinkley = UserInterfaceUtil.createXYLineChart("Page Hinkley Value Evolution - " + title,
                    "Batch Number", "Page Hinkley Value", null, this.nameVariables.toArray(new String[0]));
            this.lineChartPageHinkley.addHorizontalValueMarker(detectionThreshold);
        }
    }

    /**
     * Adapts the provided MultiCTBNC model based on the new data batch. It detects concept drifts and updates the
     * model accordingly.
     *
     * @param model    MultiCTBNC model to be adapted.
     * @param newBatch new data batch for concept drift detection and model adaptation
     * @return {@code true} if a concept drift is detected and the model is adapted, {@code false} otherwise
     * @throws ErroneousValueException if an error occurs during the adaptation process
     */
    public boolean adaptModel(MultiCTBNC<CPTNode, CIMNode> model, Dataset newBatch) throws ErroneousValueException {
        double[] avgLocalLogLikelihoods = computeAvgLocalLogLikelihoods(model, newBatch);
        this.lastChangedNodes = detectChanges(model, avgLocalLogLikelihoods);
        boolean isChangeDetected = this.lastChangedNodes.size() > 0;
        if (isChangeDetected) {
            System.out.println("Before updating:");
            System.out.println(model);
            this.timeLastUpdate = model.update(this.lastChangedNodes, newBatch);
            System.out.println("After updating:");
            System.out.println(model);
        } else
            this.timeLastUpdate = 0;
        // Update the number of processed batches
        this.numProcessedBatches++;
        // Update the number of processed sequences
        this.numProcessedSequences += newBatch.getNumDataPoints();
        // Return if the model was adapted and time to adapt it
        return isChangeDetected;
    }

    @Override
    public String getResults() {
        // Return a string with the nodes that changed. Or a map with the time...
        StringBuilder results = new StringBuilder();
        List<String> namesChangedNodes =
                this.lastChangedNodes.stream().map(Node::getName).collect(Collectors.toList());
        results.append("Concept drift detected in variables: " + namesChangedNodes);
        return results.toString();
    }

    @Override
    public long getUpdatingTime() {
        return this.timeLastUpdate;
    }

    @Override
    public List<Node> getLastChangedNodes() {
        return lastChangedNodes;
    }

    private double[] computeAvgLocalLogLikelihoods(MultiCTBNC<CPTNode, CIMNode> model, Dataset newBatch) {
        double[] avgLocalLogLikelihoods = this.conceptDriftScore.compute(model, newBatch, this.nameVariables);
        if (this.showCharts)
            this.lineChartLocalLogLikelihood.update(this.numProcessedBatches, avgLocalLogLikelihoods);
        return avgLocalLogLikelihoods;
    }

    private List<Node> detectChanges(MultiCTBNC<CPTNode, CIMNode> model, double[] avgLocalLogLikelihoods) {
        List<Node> changedNodes = new ArrayList<Node>();
        int numVariables = this.nameVariables.size();
        double[] pageHinkleyValues = new double[numVariables];
        for (int idxVariable = 0; idxVariable < numVariables; idxVariable++) {
            PageHinkleyTest pageHinkleyTestVariable = pageHinkleyTests.get(idxVariable);
            boolean isChangeDetected = pageHinkleyTestVariable.detectChange(avgLocalLogLikelihoods[idxVariable]);
            pageHinkleyValues[idxVariable] = pageHinkleyTestVariable.getPageHinkleyValue();
            logger.info("Page-Hinkley value of node {} is {}", model.getNodeByIndex(idxVariable).getName(),
                    pageHinkleyValues[idxVariable]);
            if (isChangeDetected) {
                Node node = model.getNodeByName(this.nameVariables.get(idxVariable));
                changedNodes.add(node);
                if (this.showCharts)
                    this.lineChartLocalLogLikelihood.addVerticalValueMarker(this.numProcessedBatches);
            }
        }
        if (this.showCharts)
            this.lineChartPageHinkley.update(this.numProcessedBatches, pageHinkleyValues);
        return changedNodes;
    }

}
