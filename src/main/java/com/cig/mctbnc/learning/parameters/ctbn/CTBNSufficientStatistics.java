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
 * (1) Nxx: number of times a variable transitions from a certain state to
 * another one while its parents take a certain value.
 * 
 * (2) Nx: number of times a variable leaves a certain state (for any other
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
	private Map<State, Map<State, Double>> Nxy;
	private Map<State, Double> Nx;
	private Map<State, Double> Tx;
	static Logger logger = LogManager.getLogger(CTBNSufficientStatistics.class);
	// Imaginary counts (zero if MLE is used)
	private double NxyPrior;
	private double TxPrior;

	/**
	 * Receives the hyperparameters of the Dirichlet prior distribution over the
	 * parameters that are necessary for Bayesian estimation.
	 */
	public CTBNSufficientStatistics(double NxyPrior, double TxPrior) {
		Nxy = new HashMap<State, Map<State, Double>>();
		Nx = new HashMap<State, Double>();
		Tx = new HashMap<State, Double>();
		this.NxyPrior = NxyPrior;
		this.TxPrior = TxPrior;
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
		for (Sequence sequence : dataset.getSequences()) {
			for (int i = 1; i < sequence.getNumObservations(); i++) {
				// State of the variables before the transition
				Observation fromObservation = sequence.getObservations().get(i - 1);
				State fromState = new State();
				fromState.addEvent(nameVariable, fromObservation.getValueVariable(nameVariable));
				// State of the variables after the transition
				Observation toObservation = sequence.getObservations().get(i);
				State toState = new State();
				toState.addEvent(nameVariable, toObservation.getValueVariable(nameVariable));
				// Check if the node is transitioning to a different state
				boolean nodeIsTransitioning = !fromState.equals(toState);
				// If the node has parents, their states are added to "fromState"
				for (String nameParent : nameParents)
					fromState.addEvent(nameParent, fromObservation.getValueVariable(nameParent));
				// If the node is transitioning to a different state, the sufficient statistics
				// Nxy and Nx have to be updated
				if (nodeIsTransitioning) {
					updateOccurrencesNxy(fromState, toState, 1);
					updateOccurrencesNx(fromState, 1);
				}
				// Increase the time the node and its parents are in a certain state
				double transitionTime = toObservation.getTimeValue() - fromObservation.getTimeValue();
				updateOccurrencesTx(fromState, transitionTime);
			}
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
		// Hyperparameter NxPrior (number of transitions originating from certain state)
		double NxPrior = NxyPrior * (statesVariable.size() - 1);
		if (node.hasParents()) {
			List<Node> parents = node.getParents();
			List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
			List<State> statesParents = dataset.getPossibleStatesVariables(nameParents);
			for (State stateParents : statesParents) {
				for (State fromState : statesVariable) {
					State fromStateWithParents = new State(fromState.getEvents());
					fromStateWithParents.addEvents(stateParents.getEvents());
					// Initialize Nx
					Nx.put(fromStateWithParents, NxPrior);
					// Initialize Tx
					Tx.put(fromStateWithParents, TxPrior);
					for (State toState : statesVariable)
						if (!fromState.equals(toState))
							// Initialize Nxy
							updateOccurrencesNxy(fromStateWithParents, toState, NxyPrior);
				}
			}
		} else {
			for (State fromState : statesVariable) {
				// Initialize Nx
				Nx.put(fromState, NxPrior);
				// Initialize Tx
				Tx.put(fromState, TxPrior);
				for (State toState : statesVariable)
					if (!fromState.equals(toState))
						// Initialize Nxy
						updateOccurrencesNxy(fromState, toState, NxyPrior);
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
	private void updateOccurrencesNxy(State fromState, State toState, double numOccurrences) {
		// If the state 'fromState' was never seen before, it is created a map to
		// contain all the occurrences of it transitioning to other states
		Map<State, Double> mapFromState = Nxy.get(fromState);
		if (mapFromState == null) {
			mapFromState = new HashMap<State, Double>();
			Nxy.put(fromState, mapFromState);
		}
		// Current value of Nxy for 'fromState' and 'toState'
		Double currentNxy = mapFromState.get(toState);
		if (currentNxy == null)
			currentNxy = 0.0;
		mapFromState.put(toState, currentNxy + numOccurrences);
	}

	/**
	 * Update the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to any other state.
	 * 
	 * @param fromState      current state
	 * @param numOccurrences number of occurrences
	 */
	private void updateOccurrencesNx(State fromState, double numOccurrences) {
		// Current value of Nx for 'fromState'
		Double currentNx = Nx.get(fromState);
		if (currentNx == null)
			currentNx = 0.0;
		Nx.put(fromState, currentNx + numOccurrences);
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
		Double currentTime = Tx.get(state);
		if (currentTime == null)
			currentTime = 0.0;
		Tx.put(state, currentTime + time);
	}

	/**
	 * Return the sufficient statistic with the number of times the variable
	 * transition from a certain state to another while its parents have certain
	 * values
	 * 
	 * @return number of occurrences of every transition
	 */
	public Map<State, Map<State, Double>> getNxy() {
		// It is possible that Nxy is empty if the variable do not have any transition
		// TODO remove variables with zero variance during preprocessing
		if (Nxy.isEmpty() && !Nx.isEmpty() && !Tx.isEmpty())
			logger.warn("Sufficient statistic Nxy was not computed");
		return Nxy;
	}

	/**
	 * Return the sufficient statistic with the number of times the variable leaves
	 * every state (i.e., the state changes) while its parents have certain values.
	 * 
	 * @return number of times the variable leaves every state
	 */
	public Map<State, Double> getNx() {
		if (Nx.isEmpty())
			logger.warn("Sufficient statistic Nx was not computed");
		return Nx;
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

}
