package es.upm.fi.cig.mctbnc.learning.parameters.ctbn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.data.representation.Observation;
import es.upm.fi.cig.mctbnc.data.representation.Sequence;
import es.upm.fi.cig.mctbnc.learning.parameters.SufficientStatistics;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;
import es.upm.fi.cig.mctbnc.util.Util;

/**
 * Computes and stores the sufficient statistics of a discrete CTBN node. The
 * sufficient statistics are:
 * 
 * (1) Mxy: number of times a variable transitions from a certain state to
 * another one while its parents take a certain instantiation.
 * 
 * (2) Mx: number of times a variable leaves a certain state (for any other
 * state) while its parents take a certain instantiation.
 * 
 * (3) Tx: time that a variable stays in a certain state while its parents take
 * a certain instantiation.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNSufficientStatistics implements SufficientStatistics {
	// Sufficient statistics
	private double[][][] Mxy;
	private double[][] Mx;
	private double[][] Tx;
	// Hyperparameters of the Dirichlet prior distribution (zero if MLE is used)
	private double mxyHP;
	private double mxHP; // defined with MxyHP and the number of states of the variable
	private double txHP;
	static Logger logger = LogManager.getLogger(CTBNSufficientStatistics.class);

	/**
	 * Receives the hyperparameters of the Dirichlet prior distribution over the
	 * parameters that are necessary for Bayesian estimation.
	 * 
	 * @param mxyHP number of times a variable transitions from a certain state to
	 *              another one while its parents take a certain instantiation
	 *              (hyperparameter)
	 * @param txHP  time that a variable stays in a certain state while its parents
	 *              take a certain instantiation (hyperparameter)
	 */
	public CTBNSufficientStatistics(double mxyHP, double txHP) {
		this.mxyHP = mxyHP;
		this.txHP = txHP;
	}

	/**
	 * Computes the sufficient statistics of a CTBN node.
	 * 
	 * @param dataset dataset from which sufficient statistics are extracted
	 */
	@Override
	public void computeSufficientStatistics(DiscreteNode node, Dataset dataset) {
		String nameVariable = node.getName();
		logger.trace("Computing sufficient statistics CTBN for node {}", nameVariable);
		// Initialize sufficient statistics
		initializeSufficientStatistics(node, dataset);
		// Iterate over all sequences and observations to extract sufficient statistics
		for (Sequence sequence : dataset.getSequences()) {
			for (int i = 1; i < sequence.getNumObservations(); i++) {
				// State of the variable before the transition
				Observation fromObservation = sequence.getObservations().get(i - 1);
				String fromState = fromObservation.getValueVariable(nameVariable);
				int idxFromState = node.setState(fromState);
				// State of the parents before the transition
				for (int j = 0; j < node.getNumParents(); j++) {
					DiscreteNode nodeParent = (DiscreteNode) node.getParents().get(j);
					String stateParent = fromObservation.getValueVariable(nodeParent.getName());
					nodeParent.setState(stateParent);
				}
				int idxStateParents = node.getIdxStateParents();
				// State of the variable after the transition
				Observation toObservation = sequence.getObservations().get(i);
				String toState = toObservation.getValueVariable(nameVariable);
				int idxtoState = node.setState(toState);
				// If the node is transitioning to a different state, Mxy and Mx are updated
				if (idxFromState != idxtoState) {
					updateMxy(idxStateParents, idxFromState, idxtoState, 1);
					updateMx(idxStateParents, idxFromState, 1);
				}
				// Increase the time the node and its parents are in a certain state
				double transitionTime = toObservation.getTimeValue() - fromObservation.getTimeValue();
				updateTx(idxStateParents, idxFromState, transitionTime);
			}
		}

	}

	/**
	 * Returns the sufficient statistic with the number of times the variable leaves
	 * every state (i.e., the state changes) while its parents have certain values.
	 * 
	 * @return number of times the variable leaves every state
	 */
	public double[][] getMx() {
		if (this.Mx == null)
			logger.warn("Sufficient statistic mx was not computed");
		return this.Mx;
	}

	/**
	 * Returns the sufficient statistic with the number of times the variable
	 * transition from a certain state to another while its parents have certain
	 * values
	 * 
	 * @return number of occurrences of every transition
	 */
	public double[][][] getMxy() {
		if (this.Mxy == null)
			logger.warn("Sufficient statistic mxy was not computed");
		return this.Mxy;
	}

	/**
	 * Returns the sufficient statistic with the time that the variable stay in
	 * every state while its parents take different values.
	 * 
	 * @return time that the variable stay for every state
	 */
	public double[][] getTx() {
		if (this.Tx == null)
			logger.warn("Sufficient statistic tx was not computed");
		return this.Tx;
	}

	/**
	 * Returns the value of the hyperparameter with the number of 'imaginary'
	 * transitions that occurred from a certain state to another before seen the
	 * data.
	 * 
	 * @return hyperparameter with the number of 'imaginary' transitions that
	 *         occurred from a certain state to another
	 */
	public double getMxyHyperparameter() {
		return this.mxyHP;
	}

	/**
	 * Returns the value of the hyperparameter with the number of 'imaginary'
	 * transitions that occurred from a certain state before seen the data.
	 * 
	 * @return hyperparameter with the number of 'imaginary' transitions that
	 *         occurred from a certain state
	 */
	public double getMxHyperparameter() {
		return this.mxHP;
	}

	/**
	 * Returns the value of the hyperparameter with the 'imaginary' time that was
	 * spent in a certain state before seen the data.
	 * 
	 * @return hyperparameter with the 'imaginary' time that was spent in a certain
	 *         state
	 */
	public double getTxHyperparameter() {
		return this.txHP;
	}

	/**
	 * Initializes the structures to store the sufficient statistics of the node.
	 * 
	 * @param dataset dataset used to compute the sufficient statistics
	 */
	private void initializeSufficientStatistics(DiscreteNode node, Dataset dataset) {
		// Hyperparameter MxPrior (number of transitions originating from certain state)
		this.mxHP = this.mxyHP * (node.getNumStates() - 1);
		this.Mxy = new double[node.getNumStatesParents()][node.getNumStates()][node.getNumStates()];
		this.Mx = new double[node.getNumStatesParents()][node.getNumStates()];
		this.Tx = new double[node.getNumStatesParents()][node.getNumStates()];
		// Adds the imaginary counts (hyperparameters) to the sufficient statistics
		Util.fill3dArray(this.Mxy, this.mxyHP);
		Util.fill2dArray(this.Mx, this.mxyHP * (node.getNumStates() - 1));
		Util.fill2dArray(this.Tx, this.txHP);
	}

	/**
	 * Updates the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to "toState".
	 * 
	 * @param fromState      current state
	 * @param toState        next state
	 * @param numOccurrences number of occurrences
	 */
	private void updateMxy(int stateParents, int fromState, int toState, double numOccurrences) {
		this.Mxy[stateParents][fromState][toState] += numOccurrences;
	}

	/**
	 * Updates the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to any other state.
	 * 
	 * @param fromState      current state
	 * @param numOccurrences number of occurrences
	 */
	private void updateMx(int stateParents, int fromState, double numOccurrences) {
		// Mx.merge(fromState, numOccurrences, Double::sum);
		this.Mx[stateParents][fromState] += numOccurrences;

	}

	/**
	 * Updates the time the node spends in state "state" while its parents are in
	 * certain state (information also included in "state").
	 * 
	 * @param state current state of the node and its parents
	 * @param time  time
	 */
	private void updateTx(int stateParents, int fromState, double time) {
		this.Tx[stateParents][fromState] += time;
	}

}
