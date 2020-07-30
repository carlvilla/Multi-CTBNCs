package com.cig.mctbnc.learning.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.Node;

/**
 * Compute and store the sufficient statistics of a CTBN node. The sufficient
 * statistics are:
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
	private Node node;
	// Sufficient statistics
	private Map<State, Map<State, Integer>> Nxx;
	private Map<State, Integer> Nx;
	private Map<State, Double> T;
	static Logger logger = LogManager.getLogger(CTBNSufficientStatistics.class);

	public CTBNSufficientStatistics(Node node) {
		this.node = node;
		Nxx = new HashMap<State, Map<State, Integer>>();
		Nx = new HashMap<State, Integer>();
		T = new HashMap<State, Double>();
	}

	/**
	 * Compute the sufficient statistics of a CTBN node.
	 * 
	 * @param dataset
	 *            dataset from which sufficient statistics are extracted
	 */
	public void computeSufficientStatistics(Dataset dataset) {
		logger.trace("Computing sufficient statistics CTBN for node {}", node.getName());
		computeTransitions(dataset);
		computeTime(dataset);
	}

	/**
	 * Compute the number of times the variable transition from a certain state to
	 * another while its parents have a certain value.
	 * 
	 * @param dataset
	 *            dataset from which the sufficient statistic is extracted
	 */
	private void computeTransitions(Dataset dataset) {
		logger.trace("Computing sufficient statistics Nxx and Nx");
		String nameVariable = node.getName();
		List<State> statesVariable = dataset.getStatesVariable(nameVariable);
		
		// Variables that are only used if the node has parents
		List<Node> parents = node.getParents();
		List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
		List<State> statesParents = dataset.getStatesVariables(nameParents);
		
		
		for (State fromState : statesVariable) {			
			for (State toState : statesVariable) {
				// It is filter those transitions from a certain state to the same one
				if (!fromState.equals(toState)) {
					// Computation of sufficient statistics if node has parents
					if (node.hasParents()) {
						// Obtain number of times the variable transitions from "fromState" to "toState"
						// while parents take state "stateParents"
						for (State stateParents : statesParents) {
							// Modifications in "fromState" object would affect "toState". As
							// it is necessary to include the state of the parents to "fromState", it is
							// created a new State object
							State fromStateWithParents = new State(fromState.getEvents());
							fromStateWithParents.addEvents(stateParents.getEvents());
							// Compute Nxx									
							int Nijkm = dataset.getNumOccurrencesTransition(fromStateWithParents, toState);
							addOccurrencesNxx(fromStateWithParents, toState, Nijkm);														
							// Compute Nx							
							updateOccurrencesNx(fromStateWithParents, Nijkm);
						}
					}
					// Computation of sufficient statistics if node has no parents
					else {
						// Compute Nxx
						int Nijkm = dataset.getNumOccurrencesTransition(fromState, toState);
						addOccurrencesNxx(fromState, toState, Nijkm);
						// Compute Nx
						if (!fromState.equals(toState))
							updateOccurrencesNx(fromState, Nijkm);
					}
				}
			}
		}
	}

	/**
	 * Register the number of occurrences where the node transitions transitions
	 * from "fromState" (with the parents taking a certain value) to "toState".
	 * 
	 * @param fromState
	 *            current state
	 * @param toState
	 *            next state
	 * @param numOccurrences
	 *            number of occurrences
	 */
	private void addOccurrencesNxx(State fromState, State toState, int numOccurrences) {
		if (!Nxx.containsKey(fromState))
			Nxx.put(fromState, new HashMap<State, Integer>());
		Nxx.get(fromState).put(toState, numOccurrences);
	}

	/**
	 * Update the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to any other state.
	 * 
	 * @param fromState
	 *            current state
	 * @param numOccurrences
	 *            number of occurrences
	 */
	private void updateOccurrencesNx(State fromState, int numOccurrences) {
		if (Nx.containsKey(fromState))
			Nx.put(fromState, Nx.get(fromState) + numOccurrences);
		else
			Nx.put(fromState, numOccurrences);
	}

	/**
	 * Compute the time that the variable stay in a certain state while its parents
	 * take a certain value
	 * 
	 * @param dataset
	 *            dataset from which the sufficient statistic is extracted
	 */
	private void computeTime(Dataset dataset) {
		logger.trace("Computing sufficient statistic T");
		String nameVariable = node.getName();
		List<State> statesVariable = dataset.getStatesVariable(nameVariable);
		for (State state : statesVariable) {
			if (node.hasParents()) {
				List<Node> parents = node.getParents();
				List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
				List<State> statesParents = dataset.getStatesVariables(nameParents);
				// Obtain number of times the variable transitions from "fromState" to "toState"
				// while parents take state "stateParents"
				for (State stateParents : statesParents) {
					// Modifications in "state" object would remain. Therefore, it is created
					// a new State object to include the states of the parents
					State stateWithParents = new State(state.getEvents());
					stateWithParents.addEvents(stateParents.getEvents());
					double Tijk = dataset.getTimeState(stateWithParents);
					T.put(stateWithParents, Tijk);
				}
			} else {
				double Tijk = dataset.getTimeState(state);
				T.put(state, Tijk);
			}
		}
	}

	/**
	 * Return the number of times the variable transition from every state to
	 * specific states (including itself) while its parents have certain values.
	 * 
	 * @return number of occurrences of every transition
	 */
	public Map<State, Map<State, Integer>> getNxx() {
		if (Nxx.isEmpty())
			logger.warn("Sufficient statistic Nxx was not computed");
		return Nxx;
	}

	/**
	 * Return the number of times the variable leaves every state (i.e., the state
	 * is changed) while its parents have certain values.
	 * 
	 * @return number of times the variable leaves every state
	 */
	public Map<State, Integer> getNx() {
		if (Nx.isEmpty())
			logger.warn("Sufficient statistic Nx was not computed");
		return Nx;
	}

	/**
	 * Return the time that the variable stay in every state while its parents take
	 * different values.
	 * 
	 * @return time that the variable stay for every state
	 */
	public Map<State, Double> getT() {
		if (T.isEmpty())
			logger.warn("Sufficient statistic T was not computed");
		return T;
	}

}
