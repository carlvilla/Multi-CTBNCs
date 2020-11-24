package com.cig.mctbnc.learning.parameters.ctbn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Observation;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.SufficientStatistics;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Compute and store the sufficient statistics of a discrete CTBN node. The
 * sufficient statistics are:
 * 
 * (1) Mxy: number of times a variable transitions from a certain state to
 * another one while its parents take a certain value.
 * 
 * (2) Mx: number of times a variable leaves a certain state (for any other
 * state) while its parents take a certain value.
 * 
 * (3) T: time that a variable stays in a certain state while its parents take a
 * certain value.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNSufficientStatistics implements SufficientStatistics {
	// Sufficient statistics
	private Map<State, Map<State, Double>> Mxy;
	private Map<State, Double> Mx;
	private Map<State, Double> Tx;
	static Logger logger = LogManager.getLogger(CTBNSufficientStatistics.class);
	// Hyperparameters of the Dirichlet prior distribution (zero if MLE is used)
	private double MxyHP;
	private double MxHP; // defined with MxyHP and the number of states of the variable
	private double TxHP;

	/**
	 * Receives the hyperparameters of the Dirichlet prior distribution over the
	 * parameters that are necessary for Bayesian estimation.
	 * 
	 * @param MxyHP
	 * @param TxHP
	 */
	public CTBNSufficientStatistics(double MxyHP, double TxHP) {
		Mxy = new HashMap<State, Map<State, Double>>();
		Mx = new HashMap<State, Double>();
		Tx = new HashMap<State, Double>();
		this.MxyHP = MxyHP;
		this.TxHP = TxHP;
	}

	/**
	 * Compute the sufficient statistics of a CTBN node.
	 * 
	 * @param dataset dataset from which sufficient statistics are extracted
	 */
	public void computeSufficientStatistics(Node node, Dataset dataset) {
		// TODO The node has to be discrete. Improve code to be able to easily include
		// sufficient statistics for other types of nodes
		String nameVariable = node.getName();
		logger.trace("Computing sufficient statistics CTBN for node {}", nameVariable);
		// Variables that are only used if the node has parents
		List<Node> parents = node.getParents();
		List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
		// Initialize sufficient statistics
		initializeSufficientStatistics(node, dataset);
		// Iterate over all the sequences and their observations to calculate the
		// sufficient statistics
		for (Sequence sequence : dataset.getSequences())
			for (int i = 1; i < sequence.getNumObservations(); i++) {
				// State of the variables before the transition
				Observation fromObservation = sequence.getObservations().get(i - 1);
				State fromState = new State();
				String fromValue = fromObservation.getValueVariable(nameVariable);
				fromState.addEvent(nameVariable, fromValue);
				// State of the variables after the transition
				Observation toObservation = sequence.getObservations().get(i);
				State toState = new State();
				String toValue = toObservation.getValueVariable(nameVariable);
				toState.addEvent(nameVariable, toValue);
				// Check if the node is transitioning to a different state
				boolean nodeIsTransitioning = !fromValue.equals(toValue);
				// If the node has parents, their states are added to "fromState"
				for (String nameParent : nameParents)
					fromState.addEvent(nameParent, fromObservation.getValueVariable(nameParent));
				// If the node is transitioning to a different state, the sufficient statistics
				// Mxy and Mx have to be updated
				if (nodeIsTransitioning) {
					updateOccurrencesMxy(fromState, toState, 1);
					updateOccurrencesMx(fromState, 1);
				}
				// Increase the time the node and its parents are in a certain state
				double transitionTime = toObservation.getTimeValue() - fromObservation.getTimeValue();
				updateOccurrencesTx(fromState, transitionTime);
			}
	}

	/**
	 * Initialize the structures to store the sufficient statistics of the node.
	 * 
	 * @param dataset dataset used to compute the sufficient statistics
	 */
	private void initializeSufficientStatistics(Node node, Dataset dataset) {
		// Retrieve state the variable can take
		// TODO Instead of passing a node, the class should only accept DiscreteNode.
		// Improve architecture
		List<State> statesVariable = ((DiscreteNode) node).getStates();
		// Hyperparameter MxPrior (number of transitions originating from certain state)
		MxHP = MxyHP * (statesVariable.size() - 1);
		if (node.hasParents()) {
			List<Node> parents = node.getParents();
			List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
			List<State> statesParents = dataset.getPossibleStatesVariables(nameParents);
			for (State stateParents : statesParents)
				for (State fromState : statesVariable) {
					State fromStateWithParents = new State(fromState.getEvents());
					fromStateWithParents.addEvents(stateParents.getEvents());
					// Initialize Mx
					Mx.put(fromStateWithParents, MxHP);
					// Initialize Tx
					Tx.put(fromStateWithParents, TxHP);
					for (State toState : statesVariable)
						// if (!fromState.equals(toState))
						if (!fromState.getValues()[0].equals(toState.getValues()[0]))
							// Initialize Mxy
							updateOccurrencesMxy(fromStateWithParents, toState, MxyHP);
				}
		} else {
			for (State fromState : statesVariable) {
				// Initialize Mx
				Mx.put(fromState, MxHP);
				// Initialize Tx
				Tx.put(fromState, TxHP);
				for (State toState : statesVariable)
					// if (!fromState.equals(toState))
					if (!fromState.getValues()[0].equals(toState.getValues()[0]))
						// Initialize Mxy
						updateOccurrencesMxy(fromState, toState, MxyHP);
			}
		}
	}

	/**
	 * Update the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to "toState".
	 * 
	 * @param fromState      current state
	 * @param toState        next state
	 * @param numOccurrences number of occurrences
	 */
	private void updateOccurrencesMxy(State fromState, State toState, double numOccurrences) {
		Map<State, Double> mapFromState = Mxy.get(fromState);
		if (mapFromState == null) {
			// If the state 'fromState' was never seen before, it is created a map to
			// contain all the occurrences of its transitions to other states
			mapFromState = new HashMap<State, Double>();
			mapFromState.put(toState, numOccurrences);
			Mxy.put(fromState, mapFromState);
		} else {
			// Current value of Mxy for 'fromState' and 'toState'
			Double currentMxy = mapFromState.get(toState);
			if (currentMxy == null)
				currentMxy = 0.0;
			mapFromState.put(toState, currentMxy + numOccurrences);
		}
	}

	/**
	 * Update the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to any other state.
	 * 
	 * @param fromState      current state
	 * @param numOccurrences number of occurrences
	 */
	private void updateOccurrencesMx(State fromState, double numOccurrences) {
		// Current value of Mx for 'fromState'
		double currentMx = Mx.getOrDefault(fromState, 0.0);
		Mx.put(fromState, currentMx + numOccurrences);
	}

	/**
	 * Update the time the node spends in state "state" while its parents are in
	 * certain state (information also included in "state").
	 * 
	 * @param state current state of the node and its parents
	 * @param time  time
	 */
	private void updateOccurrencesTx(State state, double time) {
		// Current time computed for the state
		double currentTime = Tx.getOrDefault(state, 0.0);
		Tx.put(state, currentTime + time);
	}

	/**
	 * Return the sufficient statistic with the number of times the variable
	 * transition from a certain state to another while its parents have certain
	 * values
	 * 
	 * @return number of occurrences of every transition
	 */
	public Map<State, Map<State, Double>> getMxy() {
		// It is possible that Mxy is empty if the variable do not have any transition
		if (Mxy.isEmpty() && !Mx.isEmpty() && !Tx.isEmpty())
			logger.warn("Sufficient statistic Mxy was not computed");
		return Mxy;
	}

	/**
	 * Return the sufficient statistic with the number of times the variable leaves
	 * every state (i.e., the state changes) while its parents have certain values.
	 * 
	 * @return number of times the variable leaves every state
	 */
	public Map<State, Double> getMx() {
		if (Mx.isEmpty())
			logger.warn("Sufficient statistic Mx was not computed");
		return Mx;
	}

	/**
	 * Return the sufficient statistic with the time that the variable stay in every
	 * state while its parents take different values.
	 * 
	 * @return time that the variable stay for every state
	 */
	public Map<State, Double> getTx() {
		if (Tx.isEmpty())
			logger.warn("Sufficient statistic Tx was not computed");
		return Tx;
	}

	/**
	 * Return the value of the hyperparameter with the number of 'imaginary'
	 * transitions that occurred from a certain state to another before seen the
	 * data.
	 * 
	 * @return hyperparameter with the number of 'imaginary' transitions that
	 *         occurred from a certain state to another
	 */
	public double getMxyHyperparameter() {
		return MxyHP;
	}

	/**
	 * Return the value of the hyperparameter with the number of 'imaginary'
	 * transitions that occurred from a certain state before seen the data.
	 * 
	 * @return hyperparameter with the number of 'imaginary' transitions that
	 *         occurred from a certain state
	 */
	public double getMxHyperparameter() {
		return MxHP;
	}

	/**
	 * Return the value of the hyperparameter with the 'imaginary' time that was
	 * spent in a certain state before seen the data.
	 * 
	 * @return hyperparameter with the 'imaginary' time that was spent in a certain
	 *         state
	 */
	public double getTxHyperparameter() {
		return TxHP;
	}

}
