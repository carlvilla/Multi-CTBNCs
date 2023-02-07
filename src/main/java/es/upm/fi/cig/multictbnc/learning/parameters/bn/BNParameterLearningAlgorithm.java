package es.upm.fi.cig.multictbnc.learning.parameters.bn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.DiscreteStateNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Defines methods for parameter learning algorithms of discrete Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public abstract class BNParameterLearningAlgorithm implements ParameterLearningAlgorithm {
	static Logger logger = LogManager.getLogger(BNParameterLearningAlgorithm.class);

	/**
	 * Obtains for each node the number of times it takes a certain state while its parents take a certain
	 * instantiation.
	 *
	 * @param nodes   list of nodes
	 * @param dataset dataset from which the sufficient statistics are extracted
	 */
	public void setSufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		for (int i = 0; i < nodes.size(); i++)
			setSufficientStatistics(nodes.get(i), dataset);
	}

	@Override
	public void learn(Node node, Dataset dataset) {
		learn(List.of(node), dataset);
	}

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		setSufficientStatistics(nodes, dataset);
		setCPTs(nodes);
	}

	@Override
	public void setSufficientStatistics(Node node, Dataset dataset) {
		BNSufficientStatistics ssNode = getSufficientStatisticsNode((DiscreteStateNode) node, dataset);
		node.setSufficientStatistics(ssNode);
	}

	/**
	 * Returns the sufficient statistics of a {@code DiscreteNode} for a given {@code Dataset}.
	 *
	 * @param node    a {@code DiscreteNode}
	 * @param dataset dataset from which the sufficient statistics are extracted
	 * @return sufficient statistics of the provided node
	 */
	protected abstract BNSufficientStatistics getSufficientStatisticsNode(DiscreteStateNode node, Dataset dataset);

	/**
	 * Estimates the parameters for a certain node from its previously computed sufficient statistics.
	 *
	 * @param node a {@code CPTNode}
	 * @return array with probabilities of each possible state of a variable given its parents
	 */
	private double[][] estimateCPT(CPTNode node) {
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
				// Iterate over states of the node to get the number of times that its parents
				// take a given state independently of the node state
				for (int idxAnyState = 0; idxAnyState < numStates; idxAnyState++)
					NState += Nx[idxStateParents][idxAnyState];
				// Probability that the studied node has the state 'idxState' given the
				// state 'idxStateParents' of the parents
				double prob = NxState / NState;
				// The state may not occur in the training dataset. In that case, the
				// probability is set to 0.
				if (Double.isNaN(prob))
					prob = 0;
				// Save the probability of the state
				CPT[idxStateParents][idxState] = prob;
			}
		}
		return CPT;
	}

	/**
	 * Given a set of nodes whose sufficient statistics were estimated, the method computes their conditional
	 * probability tables (CPT).
	 *
	 * @param nodes list of nodes
	 */
	private void setCPTs(List<? extends Node> nodes) {
		// The CPTs are estimated and stored in each node
		nodes.parallelStream().forEach(node -> {
			// The node has to have a CPT
			CPTNode nodeAux = (CPTNode) node;
			// Compute the parameters for the current node with its sufficient statistics
			double[][] CPT = estimateCPT(nodeAux);
			// CPTNode stores the computed CPT
			nodeAux.setCPT(CPT);
		});
	}
}