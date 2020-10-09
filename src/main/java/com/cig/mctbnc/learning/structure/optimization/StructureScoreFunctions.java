package com.cig.mctbnc.learning.structure.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.util.Util;

/**
 * Contains evaluation function to measures the fitness of the structures of
 * different PGM.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class StructureScoreFunctions {

	static Logger logger = LogManager.getLogger(StructureScoreFunctions.class);

	static Map<String, DoubleUnaryOperator> penalizationFunctionMap = new HashMap<>() {
		private static final long serialVersionUID = 1L;
		{
			put("BIC", N -> Math.log(N) / 2);
			put("AIC", N -> 1.0);
		}
	};

	// ---------- BAYESIAN NETWORKS ----------

	/**
	 * Compute the (penalized) log-likelihood score for a discrete Bayesian network.
	 * This is done by computing the marginal log-likelihood of the graph. It is
	 * assumed a uniform prior structure.
	 * 
	 * @param bn                   Bayesian network
	 * @param penalizationFunction penalization function
	 * @return penalized log-likelihood score
	 */
	public static double logLikelihoodScore(BN<CPTNode> bn, String penalizationFunction) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = bn.getLearnedNodes();
		double llScore = 0.0;
		for (CPTNode node : nodes) {
			// Obtain an array with the values that the studied variable can take
			String[] possibleValuesStudiedVariable = node.getStates().stream()
					.map(stateAux -> stateAux.getValueVariable(node.getName())).toArray(String[]::new);
			// All the possible states between the studied variable and its parents
			Set<State> states = node.getSufficientStatistics().keySet();
			for (State state : states)
				for (String k : possibleValuesStudiedVariable) {
					State query = new State(state.getEvents());
					query.modifyEventValue(node.getName(), k);
					llScore += classProbability(node, state);
				}
			// If the specified penalization function is available, it is applied
			if (penalizationFunctionMap.containsKey(penalizationFunction)) {
				// Overfitting is avoid by penalizing the complexity of the network
				// Number of possible states of the parents of the currently studied variable
				int numPossibleStatesParents = node.getParents().stream().map(parent -> (DiscreteNode) parent)
						.map(parent -> parent.getStates()).map(listStates -> listStates.size())
						.reduce(1, (a, b) -> a * b);

				// Number of possible states of the currently studied variable
				int numPossibleStatesStudiedVariable = possibleValuesStudiedVariable.length;
				// Compute network complexity
				double networkComplexity = numPossibleStatesParents * (numPossibleStatesStudiedVariable - 1);
				// Compute non-negative penalization (For now it is performing a BIC
				// penalization)
				int sampleSize = bn.getDataset().getNumDataPoints();
				double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
				// Obtain number of states of the parents of the currently studied variable
				llScore -= networkComplexity * penalization;
			}
		}
		return llScore;
	}

	/**
	 * Estimate the probability of a class variable taking a certain value.
	 * 
	 * @param node  class variable node
	 * @param state state of the class variable node and its parents
	 * @return
	 */
	private static double classProbability(CPTNode node, State state) {
		// Number of times the studied variable and its parents take a certain value
		int Nijk = node.getSufficientStatistics().get(state);
		double classProbability = 0.0;
		if (Nijk != 0)
			classProbability = Nijk * Math.log(node.getCPT().get(state));
		return classProbability;
	}

	// ---------- CONTINUOUS TIME BAYESIAN NETWORKS ----------

	/**
	 * Compute the (penalized) log-likelihood score at a given node of a discrete
	 * continuous time Bayesian network.
	 * 
	 * @param ctbn                 continuous time Bayesian network
	 * @param nodeIndex            index of the node
	 * @param penalizationFunction penalization function
	 * @return penalized log-likelihood score
	 */
	public static double logLikelihoodScore(CTBN<CIMNode> ctbn, int nodeIndex, String penalizationFunction) {
		// Obtain node to evaluate
		CIMNode node = ctbn.getNodes().get(nodeIndex);

//		System.out.println("-----------");
//		System.out.println("Nodo: " + node.getName());
//		System.out.println(Arrays.toString(node.getParents().stream().map(nodop -> nodop.getName()).toArray()));

		double llScore = 0.0;
		llScore += logLikelihoodScore(node);
		// Apply the specified penalization function (if available)
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
			// Overfitting is avoid by penalizing the complexity of the network
			// Number of possible transitions
			int numStates = node.getStates().size();
			int numTransitions = (numStates - 1) * numStates;
			// Number of states of the parents
			int numStatesParents = node.getNumStateParents();
			// Complexity of the network
			int networkComplexity = numTransitions * numStatesParents;
			// Sample size (number of sequences)
			int sampleSize = ctbn.getDataset().getNumDataPoints();
			// Non-negative penalization
			double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
			llScore -= (double) networkComplexity * penalization;
		}

//		System.out.println("Total: " + llScore);

		return llScore;
	}

	/**
	 * Compute the marginal log-likelihood for a certain CIM node.
	 * 
	 * @param node
	 * @return marginal log likelihood
	 */
	private static double logLikelihoodScore(CIMNode node) {
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Obtain parameters and sufficient statistics of the node
		// Contains the probabilities of transitioning from one state to another
		Map<State, Map<State, Double>> Oxx = node.getOxx();
		// CIMs. Given the state of a variable and its parents is obtained the
		// instantaneous probability
		Map<State, Double> Qx = node.getQx();
		// Store marginal log likelihood
		double mll = 0.0;
		for (State state : Qx.keySet()) {
			double qx = Qx.get(state);
			double nx = ss.getNx().get(state);
			double tx = ss.getTx().get(state);
			// Probability density function of the exponential distribution. If it is 0,
			// there are no transitions from this state
			if (qx != 0) {
				mll += nx * Math.log(qx) - qx * tx;
				for (State toState : Oxx.get(state).keySet()) {
					// Probability of transitioning from "state" to "toState"
					double oxx = Oxx.get(state).get(toState);
					// Number of times the variable transitions from "state" to "toState"
					double nxx = ss.getNxy().get(state).get(toState);
					if (oxx != 0)
						mll += nxx * Math.log(oxx);
				}
			}
		}
		return mll;
	}

	/**
	 * Compute the (penalized) conditional log-likelihood score at a given node of a
	 * discrete continuous time Bayesian network. This is based on the idea of
	 * optimizing the conditional log-likelihood proposed by Friedman et al., 1997
	 * and used for the first time to learn the structure of a CTBNC by Codecasa and
	 * Stella, 2015.
	 * 
	 * @param ctbn                 continuous time Bayesian network
	 * @param nodeIndex            index of the node
	 * @param penalizationFunction penalization function
	 * @return (penalized) conditional log-likelihood score
	 */
	public static double conditionalLogLikelihoodScore(CTBN<CIMNode> ctbn, int nodeIndex, String penalizationFunction) {
		// Obtain node to evaluate
		CIMNode node = ctbn.getNodes().get(nodeIndex);
		// Store the conditional log-likelihood score
		double cllScore = 0.0;

//		System.out.println("-----------");
//		System.out.println("Nodo: " + node.getName());
//		System.out.println(Arrays.toString(node.getParents().stream().map(nodop -> nodop.getName()).toArray()));

		// The contribution of a node is 0 if it has no class variables as parents
		if (!hasClassVariablesAsParent(node))
			return 0;// Double.NEGATIVE_INFINITY;

		// Obtain possible states of the class variables that are parents of the node
		List<String> nameCVs = nameClassVariablesParents(node);
		List<State> statesCVs = ctbn.getDataset().getPossibleStatesVariables(nameCVs);
		// Parameters of class variables are in the BN, their nodes are retrieved
		BN<CPTNode> bnClassSubgraph = (BN<CPTNode>) ctbn.getBnClassSubgraph();
		List<CPTNode> nodesCVs = bnClassSubgraph.getNodesByNames(nameCVs);
		// Compute unnormalized posteriors given each possible state of the class
		// variables
		double[] uPs = new double[statesCVs.size()];
		for (int i = 0; i < statesCVs.size(); i++) {
			// Class probability term
			uPs[i] = logPriorProbabilityClass(nodesCVs, statesCVs.get(i), ctbn.getDataset());
			// Posterior probability of the sequences given the class configuration
			uPs[i] += logPosteriorProbabilitySequence(node, statesCVs.get(i));
			// Add the class probability and posterior probability
			cllScore += uPs[i];
		}

//		System.out.println("Without denominator: "+cllScore);

		// Prior probability of the sequences (Denominator term)
		// The log-sum-exp trick is used to avoid underflows
		int idxLargestUP = Util.getIndexLargestValue(uPs);
		double largestUP = uPs[idxLargestUP];
		// Sum the exponential value of each unnormalized posterior minus the largest
		// unnormalized posterior
		double sum = Arrays.stream(uPs).map(uP -> Math.exp(uP - largestUP)).sum();
		// Obtain the prior probability (normalizing constant)
		double nc = largestUP + Math.log(sum);
		cllScore -= nc;
		// Apply the specified penalization function (if available)
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
			// Overfitting is avoid by penalizing the complexity of the network
			// Number of possible transitions
			int numStates = node.getStates().size();
			int numTransitions = (numStates - 1) * numStates;
			// Number of states of the parents
			int numStatesParents = node.getNumStateParents();
			// Complexity of the network
			int networkComplexity = numTransitions * numStatesParents;
			// Sample size (number of sequences)
			int sampleSize = ctbn.getDataset().getNumDataPoints();
			// Non-negative penalization
			double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
			cllScore -= (double) networkComplexity * penalization;
		}

//		System.out.println(cllScore);

		return cllScore;
	}

	/**
	 * Estimate prior probability of a class configuration (stateCVs). This method
	 * will receive the class configuration of those class variables that are
	 * parents of a certain feature (observed class variables). However, it is
	 * necessary to also extract the parents of those class variables (unobserved
	 * class variables) to marginalize them out.
	 * 
	 * @param nodesCVs nodes of the studied class variables
	 * @param stateCVs state (class configuration) of the studied class variables
	 * @param dataset
	 * @return
	 */
	private static double logPriorProbabilityClass(List<CPTNode> nodesCVs, State stateCVs, Dataset dataset) {
		double lp = 0.0;
		// Iterate over class variables nodes
		for (CPTNode node : nodesCVs) {
			// Extract value of the node from the studied class configuration
			String valueNode = stateCVs.getValueVariable(node.getName());
			// State containing the state of class variables in "stateCVs" and their parents
			State query = new State();
			// Add class of the node to the state
			query.addEvent(node.getName(), valueNode);
			// Obtain the parents of the class variable
			List<Node> parents = node.getParents();
			// Class variables that are parents of "nodeCV" but not of the feature
			List<Node> parentsUnobserved = new ArrayList<Node>();
			for (Node parent : parents) {
				String valueParent = stateCVs.getValueVariable(parent.getName());
				if (valueParent != null)
					// The parent is also parent of the feature
					query.addEvent(parent.getName(), valueParent);
				else
					// Class variable has a parent that is not parent of the studied feature
					parentsUnobserved.add(parent);
			}
			// To obtain probability of class configuration is necessary to marginalize out
			// their parents, which are not observed
			if (parentsUnobserved.size() > 0) {
				// Class variable has parents that are not included in "stateClassVariables"
				// (they are not observed). Therefore, to obtain the probability of the class
				// configuration is necessary to marginalize them out
				List<String> nameParentsUnobserved = parentsUnobserved.stream().map(parent -> parent.getName())
						.collect(Collectors.toList());
				// Extract all class configurations from unobserved class variables
				List<State> states = dataset.getPossibleStatesVariables(nameParentsUnobserved);
				for (final State state : states) {
					// Add to class configuration of unobserved class variables the state of
					// observed class variables
					state.addEvents(query.getEvents());
					double nx = node.getSufficientStatistics().get(state);
					double ox = node.getCPT().get(state);
					if (ox > 0)
						lp += nx * Math.log(ox);
				}
			} else {
				// All parents of the class variables are observed
				double nx = node.getSufficientStatistics().get(query);
				double ox = node.getCPT().get(query);
				if (ox > 0)
					lp += nx * Math.log(ox);
			}
		}
		return lp;

	}

	/**
	 * Estimate posterior probability of the sequences, considering only a certain
	 * feature node, given a class configuration of the class variables that are
	 * parent of that node.
	 * 
	 * @param node     studied feature node
	 * @param stateCVs state (class configuration) of the class variables that are
	 *                 parents of "node"
	 * @return
	 */
	private static double logPosteriorProbabilitySequence(CIMNode node, State stateCVs) {
		// Sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Names of the class variables that are parents of the feature node
		List<String> namesCVs = stateCVs.getNameVariables();
		// States the feature node and its parents (features and class variables) take
		Set<State> statesNodeAndParents = node.getQx().keySet();
		double lpp = 0;
		// To compute the probability of the transitions of the node for all the
		// sequences given a class configuration, it is necessary to marginalize out
		// those features that are parents of the node (unobserved variables)
		for (State fromState : statesNodeAndParents) {
			// Check that "fromState" contains the studied class configuration
			boolean containsCC = namesCVs.stream()
					.allMatch(nameCV -> fromState.getValueVariable(nameCV).equals(stateCVs.getValueVariable(nameCV)));
			if (containsCC) {
				double qx = node.getQx().get(fromState);
				double nx = ss.getNx().get(fromState);
				double tx = ss.getTx().get(fromState);
				if (qx > 0)
					lpp += nx * Math.log(qx) - qx * tx;
				// Maps with probabilities (ox_) and number of occurrences (nx_) of transitions
				// from "fromState" to any other possible state of the feature node
				Map<State, Double> ox_ = node.getOxx().get(fromState);
				Map<State, Double> nx_ = node.getSufficientStatistics().getNxy().get(fromState);
				// Iterate over all states of the feature node (except its state in "fromState")
				for (State toState : ox_.keySet()) {
					double oxx = ox_.get(toState);
					double nxx = nx_.get(toState);
					if (oxx > 0)
						lpp += nxx * Math.log(oxx);
				}
			}
		}
		return lpp;

	}

	/**
	 * Check if a node has a class variable as parent.
	 * 
	 * @param node
	 * @return
	 */
	private static boolean hasClassVariablesAsParent(Node node) {
		return node.getParents().stream().anyMatch(parent -> parent.isClassVariable());
	}

	/**
	 * Return the name of the class variables that are parent of the given node.
	 * 
	 * @param node
	 * @return
	 */
	private static List<String> nameClassVariablesParents(Node node) {
		Stream<Node> CVParents = node.getParents().stream().filter(parent -> parent.isClassVariable());
		return CVParents.map(CVParent -> CVParent.getName()).collect(Collectors.toCollection(ArrayList::new));
	}

}
