package es.upm.fi.cig.multictbnc.conceptdriftdetection;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;

import java.util.List;

/**
 * Interface representing scores that can be computed locally on each node of a MultiCTBNC and used to detect concept
 * drifts in a given data batch.
 *
 * @author Carlos Villa Blanco
 */
public interface ConceptDriftScore {

    /**
     * Compute the score over each node of the provided {@code MultiCTBNC}.
     *
     * @param model         MultiCTBNC model to be evaluated for concept drift
     * @param newBatch      new data batch that is used to compute the drift score
     * @param nameVariables names of the variables
     * @return array of doubles representing the drift scores for each node
     */
    double[] compute(MultiCTBNC<CPTNode, CIMNode> model, Dataset newBatch, List<String> nameVariables);
}
