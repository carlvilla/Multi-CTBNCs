package com.cig.mctbnc.learning.structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

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

	/**
	 * Compute the penalized log-likelihood score for a discrete Bayesian network
	 * 
	 * @param bn Bayesian network
	 * @return penalize log-likelihood score
	 */
	static double logLikelihoodScore(BN<CPTNode> bn, String penalizationFunction) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = bn.getLearnedNodes();
		double llScore = 0.0;
		for (CPTNode node : nodes) {
			Map<State, Double> CPT = node.getCPT();
			Map<State, Integer> N = node.getSufficientStatistics();
			// Obtain an array with the values that the studied variable can take
			String[] possibleValuesStudiedVariable = node.getStates().stream()
					.map(stateAux -> stateAux.getValueNode(node.getName())).toArray(String[]::new);
			// All the possible states between the studied variable and its parents
			Set<State> states = N.keySet();
			for (State state : states) {
				for (String k : possibleValuesStudiedVariable) {
					State query = new State(state.getEvents());
					query.modifyEventValue(node.getName(), k);
					// Number of times the studied variable and its parents take a certain value
					int Nijk = N.get(query);
					if (Nijk != 0)
						llScore += Nijk * Math.log(CPT.get(query));
				}
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
	 * Compute the log-likelihood score at a given node of a discrete continuous
	 * time Bayesian network.
	 * 
	 * @param ctbn      continuous time Bayesian network
	 * @param nodeIndex index of the node
	 * @param penalize  boolean that determines if the log-likelihood is penalized
	 * @return penalized log-likelihood score
	 */
	static double logLikelihoodScore(CTBN<CIMNode> ctbn, int nodeIndex, String penalizationFunction) {
		// Obtain node to evaluate
		CIMNode node = ctbn.getNodes().get(nodeIndex);
		double llScore = 0.0;
		
		// Obtain parameters and sufficient statistics of the node
		// Contains the probabilities of transitioning from one state to another
		Map<State, Map<State, Double>> Oxx = node.getOxx();
		// CIMs. Given the state of a variable and its parents is obtained the
		// instantaneous probability
		Map<State, Double> Qx = node.getQx();
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Obtain all possible state of the variable and its parents
		for (State state : Qx.keySet()) {
			double qx = Qx.get(state);
			int nx = ss.getNx().get(state);
			double tx = ss.getTx().get(state);
			// Probability density function of the exponential distribution
			if (qx != 0)
				llScore += nx * Math.log(qx) - qx * tx;
			for (State toState : Oxx.get(state).keySet()) {
				// Probability of transitioning from "state" to "toState"
				double oxx = Oxx.get(state).get(toState);
				// Number of times the variable transitions from "state" to "toState"
				int nxx = ss.getNxy().get(state).get(toState);
				if (oxx != 0)
					llScore += nxx * Math.log(oxx);
			}
		}
		// If the specified penalization function is available, it is applied
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
			// Overfitting is avoid by penalizing the complexity of the network
			// Number of possible transitions
			int numStates = node.getStates().size();
			int numTransitions = (numStates - 1) * numStates;
			// Number of states of the parents
			int numStatesParents = node.getNumStateParents();
			// Complexity of the network
			int networkComplexity = numTransitions * numStatesParents;
			// Sample size
			int sampleSize = ctbn.getDataset().getNumObservation();
			// Non-negative penalization (For now it is performing a BIC penalization)
			double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);

			llScore -= (double) networkComplexity * penalization;
		}
		return llScore;
	}

}
