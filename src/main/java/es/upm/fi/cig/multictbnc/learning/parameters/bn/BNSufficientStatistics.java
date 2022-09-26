package es.upm.fi.cig.multictbnc.learning.parameters.bn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.learning.parameters.SufficientStatistics;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Compute and store the sufficient statistics of a discrete BN node. The sufficient statistics are:
 * <p>
 * (1) Nx: number of times a variable takes a certain state.
 *
 * @author Carlos Villa Blanco
 */
public class BNSufficientStatistics implements SufficientStatistics {
	static Logger logger = LogManager.getLogger(BNSufficientStatistics.class);
	// Sufficient statistics
	private double[][] Nx;
	// Hyperparameters of the Dirichlet prior distribution (zero if MLE is used)
	private double nHP;
	private double nxHP;

	/**
	 * Constructs a {@code BNSufficientStatistics} by receiving the hyperparameter of the Dirichlet prior distribution.
	 *
	 * @param nHP number of times the variables are in a certain state while its parents take a certain instantiation
	 *            (hyperparameter)
	 */
	public BNSufficientStatistics(double nHP) {
		this.nHP = nHP;
	}

	/**
	 * Returns the sufficient statistics of the node. This is the number of times the variable is in a certain state
	 * while its parents take a certain instantiation.
	 *
	 * @return array with the number of appearances of each state of the node given an instantiation of the parents
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

	/**
	 * Computes the sufficient statistics of a node in a BN. This is the number of times the variable is in a certain
	 * state while its parents take a certain instantiation.
	 *
	 * @param dataset dataset from which the sufficient statistics are extracted
	 */
	@Override
	public void computeSufficientStatistics(DiscreteNode node, Dataset dataset) {
		logger.trace("Computing sufficient statistics BN for node {}", node.getName());
		String nameVariable = node.getName();
		// Initialise sufficient statistics
		initialiseSufficientStatistics(node);
		// Iterate over all sequences to extract sufficient statistics
		for (Sequence sequence : dataset.getSequences()) {
			String state = sequence.getClassVariables().get(nameVariable);
			int idxState = node.setState(state);
			for (int j = 0; j < node.getNumParents(); j++) {
				DiscreteNode nodeParent = (DiscreteNode) node.getParents().get(j);
				String stateParent = sequence.getClassVariables().get(nodeParent.getName());
				nodeParent.setState(stateParent);
			}
			int idxStateParents = node.getIdxStateParents();
			// Update the sufficient statistic for a given state of the node and its parents
			updateNx(idxStateParents, idxState, 1);
		}
	}

	/**
	 * Initialises the sufficient statistics. In the case of Bayesian estimation, the imaginary counts are defined
	 * based
	 * on the number of states of the node and its parents, so the total number of imaginary samples is not influenced
	 * by the cardinality of those nodes.
	 *
	 * @param node node
	 */
	private void initialiseSufficientStatistics(DiscreteNode node) {
		this.Nx = new double[node.getNumStatesParents()][node.getNumStates()];
		// Adds the imaginary counts (hyperparameters) to the sufficient statistics
		this.nxHP = this.nHP / (node.getNumStatesParents() * node.getNumStates());
		Util.fill2dArray(this.Nx, this.nxHP);
	}

	private void updateNx(int idxStateParents, int idxState, int numOccurrences) {
		try {
			this.Nx[idxStateParents][idxState] += numOccurrences;
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			logger.warn(
					"The sufficient statistic Nx could not be updated as a never-before-seen state has been received");
		}
	}

}