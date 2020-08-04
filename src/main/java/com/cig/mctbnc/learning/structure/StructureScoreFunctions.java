package com.cig.mctbnc.learning.structure;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.CTBNSufficientStatistics;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBNC;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.DiscreteNode;

public class StructureScoreFunctions {

	static Logger logger = LogManager.getLogger(StructureScoreFunctions.class);

	/**
	 * Compute the penalized log-likelihood score of a probabilistic graphical model
	 * 
	 * @param pgm
	 * @return penalized log-likelihood score
	 */
	public static double penalizedLogLikelihoodScore(PGM pgm) {
		double llScore = 0;
		if (pgm instanceof BN) {
			logger.trace("Computing penalized log-likelihood of BN");
			llScore = penalizedLogLikelihoodScore((BN<CPTNode>) pgm);
		} else if (pgm instanceof CTBNC) {
			logger.trace("Computing penalized log-likelihood of CTBN");
			llScore = penalizedLogLikelihoodScore((CTBNC<CIMNode>) pgm);
		}
		logger.trace("Penalized log-likelihood is {}", llScore);
		return llScore;
	}

	/**
	 * Compute the penalized log-likelihood score for a discrete Bayesian network
	 * 
	 * @param bn
	 *            Bayesian network
	 * @return penalized log-likelihood score
	 */
	private static double penalizedLogLikelihoodScore(BN<CPTNode> bn) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = bn.getLearnedNodes();

		double llScore = 0.0;
		for (CPTNode node : nodes) {
			Map<State, Double> CPT = node.getCPT();
			Map<State, Integer> N = node.getSufficientStatistics();

			// Obtain an array with the values that the studied variable can take
			String[] possibleValuesStudiedVariable = node.getPossibleStates().stream()
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
			// Overfitting is avoid by penalizing the complexity of the network

			// Number of possible states of the parents of the currently studied variable
			int numPossibleStatesParents = node.getParents().stream().map(parent -> (DiscreteNode) parent)
					.map(parent -> parent.getStates()).map(listStates -> listStates.size()).reduce(1, (a, b) -> a * b);

			// Number of possible states of the currently studied variable
			int numPossibleStatesStudiedVariable = possibleValuesStudiedVariable.length;

			// Compute network complexity
			double networkComplexity = numPossibleStatesParents * (numPossibleStatesStudiedVariable - 1);

			// Compute non-negative penalization (For now it is performing a BIC
			// penalization)
			double penalization = Math.log(bn.getDataset().getNumDataPoints()) / 2;

			// Obtain number of states of the parents of the currently studied variable
			llScore -= networkComplexity * penalization;
		}

		return llScore;
	}

	/**
	 * Compute the penalized log-likelihood score for a discrete continuous time
	 * Bayesian network
	 * 
	 * @param ctbn
	 *            continuous time Bayesian network
	 * @return penalized log-likelihood score
	 */
	private static double penalizedLogLikelihoodScore(CTBNC<CIMNode> ctbn) {
		// Obtain nodes of the Bayesian networks with the learned parameters
		List<CIMNode> nodes = ctbn.getLearnedNodes();
		double llScore = 0.0;
		for (CIMNode node : nodes) {
			Map<State, Map<State, Double>> Oxx = node.getOxx();
			Map<State, Double> Qx = node.getQx();
			CTBNSufficientStatistics ss = node.getSufficientStatistics();
			for (State state : Qx.keySet()) {
				double qx = Qx.get(state);
				int nx = ss.getNx().get(state);
				double tx = ss.getTx().get(state);
				// Probability density function of the exponential distribution
				if (qx != 0)
					llScore += nx * Math.log(qx) - qx * tx;
				for (State toState : Oxx.get(state).keySet()) {
					double oxx = Oxx.get(state).get(toState);
					int nxx = ss.getNxy().get(state).get(toState);
					if (oxx != 0)
						llScore += nxx * Math.log(oxx);
				}
			}
		}

		// Overfitting is avoid by penalizing the complexity of the network

		// Compute network complexity (number of independent parameters)
		// Number of parameters for the instantaneous probabilities
		int numOxx = nodes.stream()
				.mapToInt(node -> node.getOxx().values().stream().mapToInt(map -> map.keySet().size()).sum()).sum();
		// Number of parameters for the
		int numQx = nodes.stream().mapToInt(node -> node.getQx().keySet().size()).sum();
		double networkComplexity = numOxx + numQx;

		// Compute non-negative penalization (For now it is performing a BIC
		// penalization)
		double penalization = Math.log(ctbn.getDataset().getNumObservation()) / 2;

		llScore -= networkComplexity * penalization;

		return llScore;
	}

}
