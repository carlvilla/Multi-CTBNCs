package es.upm.fi.cig.mctbnc.learning.parameters.bn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.data.representation.Observation;
import es.upm.fi.cig.mctbnc.data.representation.Sequence;
import es.upm.fi.cig.mctbnc.learning.parameters.SufficientStatistics;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;
import es.upm.fi.cig.mctbnc.util.Util;

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
	private double nxHP;
	static Logger logger = LogManager.getLogger(BNSufficientStatistics.class);

	/**
	 * Constructs a {@code BNSufficientStatistics} by receiving the hyperparameter
	 * of the Dirichlet prior distribution.
	 * 
	 * @param nxHP number of times the variables are in a certain state while its
	 *             parents take a certain instantiation (hyperparameter)
	 */
	public BNSufficientStatistics(double nxHP) {
		this.nxHP = nxHP;
	}

	/**
	 * Computes the sufficient statistics of a node in a BN. This is the number of
	 * times the variable is in a certain state while its parents take a certain
	 * instantiation.
	 * 
	 * @param dataset dataset from which the sufficient statistics are extracted
	 */
	@Override
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
	 * Returns the sufficient statistics of the node. This is the number of times
	 * the variable is in a certain state while its parents take a certain instantiation.
	 * 
	 * @return array with the number of appearances of each state of the node given
	 *         an instantiation of the parents
	 */
	public double[][] getNx() {
		return this.Nx;
	}

	/**
	 * Returns the hyperparameter value of the Dirichlet prior distribution.
	 * 
	 * @return hyperparameter value
	 */
	public double getNxHyperparameter() {
		return this.nxHP;
	}

	private void updateNx(int idxStateParents, int idxState, int numOccurrences) {
		this.Nx[idxStateParents][idxState] += numOccurrences;
	}

	/**
	 * Initializes the sufficient statistics.
	 * 
	 * @param node
	 * @param dataset
	 */
	private void initializeSufficientStatistics(DiscreteNode node, Dataset dataset) {
		this.Nx = new double[node.getNumStatesParents()][node.getNumStates()];
		// Adds the imaginary counts (hyperparameters) to the sufficient statistics
		Util.fill2dArray(this.Nx, this.nxHP);
	}

}
