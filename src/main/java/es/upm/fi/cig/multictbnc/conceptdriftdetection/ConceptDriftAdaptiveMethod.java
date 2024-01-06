package es.upm.fi.cig.multictbnc.conceptdriftdetection;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.gui.XYLineChart;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.List;

/**
 * Abstract class representing a concept drift adaptive method. This class provides a framework for implementing
 * adaptive methods to handle concept drifts in Multi-CTBNCs.
 *
 * @author Carlos Villa Blanco
 */
public abstract class ConceptDriftAdaptiveMethod {
    ConceptDriftScore conceptDriftScore;
    List<String> nameVariables;
    int numProcessedBatches;
    int numProcessedSequences;
    long timeLastUpdate;
    boolean showCharts;

    // ----- Graphs -----
    XYLineChart lineChartLocalLogLikelihood;
    XYLineChart lineChartPageHinkley;

    /**
     * Initializes the concept drift adaptive method with the specified parameters.
     *
     * @param conceptDriftScore  scoring mechanism to detect concept drift
     * @param namesVariables     list of variable names used in the model
     * @param detectionThreshold threshold for determining concept drifts
     * @param showCharts         flag indicating whether to display drift detection charts
     * @param title              title for the drift detection charts
     */
    public ConceptDriftAdaptiveMethod(ConceptDriftScore conceptDriftScore, List<String> namesVariables,
                                      double detectionThreshold, boolean showCharts, String title) {
        this.conceptDriftScore = conceptDriftScore;
        this.nameVariables = namesVariables;
        this.showCharts = showCharts;
        setUpLineChart(title, detectionThreshold);
    }

    /**
     * Sets up line charts for visualizing concept drift detection results if the charts are enabled.
     *
     * @param title              title for the drift detection charts
     * @param detectionThreshold threshold for determining concept drifts
     */
    protected abstract void setUpLineChart(String title, double detectionThreshold);

    /**
     * Abstract method for adapting the model based on the provided data batch. Implementations should define how the
     * model is updated in response to concept drifts.
     *
     * @param model           Multi-CTBNC model to adapt
     * @param batchDataStream new data batch
     * @return {@code true} if the model was adapted, {@code false} otherwise
     * @throws ErroneousValueException if an error occurs during model adaptation
     */
    public abstract boolean adaptModel(MultiCTBNC<CPTNode, CIMNode> model, Dataset batchDataStream)
            throws ErroneousValueException;

    /**
     * Returns a {@code String} describing the results of the last concept drift detection.
     *
     * @return String describing the concept drift detection results
     */
    public abstract String getResults();

    /**
     * Returns the time taken for the last update of the model.
     *
     * @return time taken for updating the model
     */
    public long getUpdatingTime() {
        return this.timeLastUpdate;
    }

    /**
     * Retrieves the list of nodes that were last identified as having undergone concept drift.
     *
     * @return list of nodes that experienced concept drift in the most recent adaptation
     */
    public abstract List<Node> getLastChangedNodes();
}