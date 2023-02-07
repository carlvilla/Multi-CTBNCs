package es.upm.fi.cig.multictbnc.learning.parameters;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.DiscreteStateNode;

/**
 * Interface for sufficient statistics of discrete nodes.
 *
 * @author Carlos Villa Blanco
 */
public interface SufficientStatistics {

    /**
     * Computes the sufficient statistics of a discrete node.
     *
     * @param node    node whose sufficient statistics are computed
     * @param dataset dataset from which is extracted the sufficient statistics
     */
    void computeSufficientStatistics(DiscreteStateNode node, Dataset dataset);

}