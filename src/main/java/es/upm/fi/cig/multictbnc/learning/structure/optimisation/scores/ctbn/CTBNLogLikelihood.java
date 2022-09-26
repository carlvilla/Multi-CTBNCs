package es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.AbstractLikelihood;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.function.DoubleUnaryOperator;

/**
 * Implements the log-likelihood score for CTBNs with nodes that have CIMs.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNLogLikelihood extends AbstractLikelihood implements CTBNScoreFunction {

	/**
	 * Receives the name of the penalisation function for the structure complexity.
	 *
	 * @param penalisationFunction name of the penalisation function
	 */
	public CTBNLogLikelihood(String penalisationFunction) {
		super(penalisationFunction);
	}

	@Override
	public double compute(CTBN<? extends Node> ctbn) {
		double ll = 0;
		for (int idxNode = 0; idxNode < ctbn.getNumNodes(); idxNode++)
			ll += compute(ctbn, idxNode);
		return ll;
	}

	/**
	 * Computes the (penalised) log-likelihood score at a given node of a discrete continuous-time Bayesian network.
	 *
	 * @param ctbn    continuous-time Bayesian network
	 * @param idxNode index of the node
	 * @return penalised log-likelihood score
	 */
	@Override
	public double compute(CTBN<? extends Node> ctbn, int idxNode) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(idxNode);
		double ll = 0.0;
		ll += node.estimateLogLikelihood();
		// Apply the specified penalisation function (if available)
		DoubleUnaryOperator penalisationFunction = this.getPenalisationFunction();
		if (penalisationFunction != null) {
			// Overfitting is avoided by penalising the complexity of the network
			// Number of possible transitions
			int numStates = node.getNumStates();
			int numTransitions = (numStates - 1) * numStates;
			// Number of states of the parents
			int numStatesParents = node.getNumStatesParents();
			// Complexity of the network
			int networkComplexity = numTransitions * numStatesParents;
			// Sample size
			int numObservations = ctbn.getDataset().getNumObservation();
			int numTotalTransitions = numObservations - ctbn.getDataset().getNumDataPoints();
			double penalisation = penalisationFunction.applyAsDouble(numTotalTransitions);
			ll -= networkComplexity * penalisation;
		}
		return ll;
	}

	@Override
	public boolean isDecomposable() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "Log-likelihood";
	}

}