package es.upm.fi.cig.mctbnc.learning.parameters.bn;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.mctbnc.nodes.CPTNode;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Defines methods for parameter learning algorithms of discrete Bayesian
 * networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class BNParameterLearningAlgorithm implements ParameterLearningAlgorithm {
	static Logger logger = LogManager.getLogger(BNParameterLearningAlgorithm.class);

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		setSufficientStatistics(nodes, dataset);
		setCPTs(nodes);
	}

	@Override
	public void learn(Node node, Dataset dataset) {
		setSufficientStatistics((CPTNode) node, dataset);
		setCPTs((CPTNode) node);
	}

	/**
	 * Obtains for each node the number of times it takes a certain state while its
	 * parents take a certain instantiation.
	 * 
	 * @param nodes   list of nodes
	 * @param dataset dataset from which the sufficient statistics are extracted
	 */
	public void setSufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		for (int i = 0; i < nodes.size(); i++) {
			BNSufficientStatistics ssNode = getSufficientStatisticsNode((CPTNode) nodes.get(i), dataset);
			nodes.get(i).setSufficientStatistics(ssNode);
		}
	}

	/**
	 * Obtains the sufficient statistics of a node.
	 * 
	 * @param node    a {@code DiscreteNode}
	 * @param dataset dataset from which the sufficient statistics are extracted
	 */
	private void setSufficientStatistics(DiscreteNode node, Dataset dataset) {
		BNSufficientStatistics ssNode = getSufficientStatisticsNode(node, dataset);
		node.setSufficientStatistics(ssNode);
	}

	/**
	 * Given a set of nodes whose sufficient statistics where estimated, computes
	 * their conditional probability tables (CPT).
	 * 
	 * @param nodes list of nodes
	 */
	public void setCPTs(List<? extends Node> nodes) {
		// For each node it is created a new type of node that contains the CPTs.
		for (int i = 0; i < nodes.size(); i++) {
			// The node has to have a CPT
			CPTNode node = (CPTNode) nodes.get(i);
			// Compute the parameters for the current node with its sufficient statistics
			double[][] CPT = estimateCPT(node);
			// CPTNode stores the computed CPT
			node.setCPT(CPT);
		}
	}

	/**
	 * Given a node whose sufficient statistics where estimated, computes its
	 * conditional probability tables (CPT).
	 * 
	 * @param node a {@code CPTNode}
	 */
	public void setCPTs(CPTNode node) {
		// Compute the parameters for the current node with its sufficient statistics
		double[][] CPT = estimateCPT(node);
		// CPTNode stores the computed CPT
		node.setCPT(CPT);
	}

	/**
	 * Estimates the parameters for a certain node from its previously computed
	 * sufficient statistics.
	 * 
	 * @param node a {@code CPTNode}
	 * @return array with probabilities of each possible state of a variable given
	 *         its parents
	 */
	public double[][] estimateCPT(CPTNode node) {
		logger.trace("Learning parameters of BN node {} with maximum likelihood estimation", node.getName());
		// Retrieve sufficient statistics
		double[][] Nx = node.getSufficientStatistics().getNx();
		int numStates = node.getNumStates();
		int numStatesParents = node.getNumStatesParents();
		// Define CPT
		double[][] CPT = new double[numStatesParents][numStates];
		// Iterate over states of the node and its parents
		for (int idxState = 0; idxState < numStates; idxState++) {
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				// Number of sequences where the node and its parents take the given states
				double NxState = Nx[idxStateParents][idxState];
				// Number of sequences where the parents take the given states
				double NState = 0;
				// Iterate over states of the node to get number of times parents take given
				// state independently of node state
				for (int idxAnyState = 0; idxAnyState < numStates; idxAnyState++)
					NState += Nx[idxStateParents][idxAnyState];
				// Probability that the studied variable has the state 'idxState' given the
				// state 'idxStateParents' of the parents
				double prob = NxState / NState;
				// The state may not occur in the training dataset. In that case the probability
				// is set to 0.
				if (Double.isNaN(prob))
					prob = 0;
				// Save the probability of the state
				CPT[idxStateParents][idxState] = prob;
			}
		}
		return CPT;
	}

	protected abstract BNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset);
}
