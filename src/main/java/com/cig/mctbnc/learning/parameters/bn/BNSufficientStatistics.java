package com.cig.mctbnc.learning.parameters.bn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Observation;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.learning.parameters.SufficientStatistics;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.util.Util;

/**
 * Compute and store the sufficient statistics of a discrete BN node. The
 * sufficient statistics are:
 * 
 * (1) Nx: number of times a variable takes a certain state.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNSufficientStatistics implements SufficientStatistics {
	// Sufficient statistics
	private double[][] Nx;
	// Hyperparameters of the Dirichlet prior distribution (zero if MLE is used)
	private double NxHP;
	static Logger logger = LogManager.getLogger(BNSufficientStatistics.class);

	public BNSufficientStatistics(double NxHP) {
		this.NxHP = NxHP;
	}

	/**
	 * Compute the sufficient statistics of a node in a BN. This is the number of
	 * times the variable is in a certain state while its parents take a certain
	 * value.
	 * 
	 * @param dataset
	 */
	public void computeSufficientStatistics(DiscreteNode node, Dataset dataset) {
		logger.trace("Computing sufficient statistics BN for node {}", node.getName());
		String nameVariable = node.getName();
		// Initialize sufficient statistics
		initializeSufficientStatistics(node, dataset);
		// Iterate over all sequences to extract sufficient statistics
		for (Sequence sequence : dataset.getSequences()) {
			// As the class variables should have the same states during a sequence, only
			// the first observation is analyzed
			Observation observation = sequence.getObservations().get(0);
			String state = observation.getValueVariable(nameVariable);
			int idxState = node.setState(state);
			for (int j = 0; j < node.getNumParents(); j++) {
				DiscreteNode nodeParent = (DiscreteNode) node.getParents().get(j);
				String stateParent = observation.getValueVariable(nodeParent.getName());
				nodeParent.setState(stateParent);
			}
			int idxStateParents = node.getIdxStateParents();
			// Update the sufficient statistic for a given state of the node and its parents
			updateNx(idxStateParents, idxState, 1);
		}
	}

	/**
	 * Return the sufficient statistics of the node. This is the number of the
	 * variable is in a certain state while its parents take a certain value.
	 * 
	 * @return array with the number of appearances of each state of the node given
	 *         an instantiation of the parents
	 */
	public double[][] getNx() {
		return Nx;
	}

	public double getNxHyperparameter() {
		return NxHP;
	}

	private void updateNx(int idxStateParents, int idxState, int numOccurrences) {
		Nx[idxStateParents][idxState] += numOccurrences;
	}

	/**
	 * Initialize the sufficient statistics.
	 * 
	 * @param node
	 * @param dataset
	 */
	private void initializeSufficientStatistics(DiscreteNode node, Dataset dataset) {
		Nx = new double[node.getNumStatesParents()][node.getNumStates()];
		// Adds the imaginary counts (hyperparameters) to the sufficient statistics
		Util.fill2dArray(Nx, NxHP);

	}

}
