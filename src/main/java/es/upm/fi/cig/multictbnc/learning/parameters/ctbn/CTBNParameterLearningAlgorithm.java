package es.upm.fi.cig.multictbnc.learning.parameters.ctbn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Define methods for parameter learning algorithms of continuous-time Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public abstract class CTBNParameterLearningAlgorithm implements ParameterLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(CTBNParameterLearningAlgorithm.class);

	/**
	 * Obtains the sufficient statistics of each node of a CTBN.
	 *
	 * @param nodes   list of nodes
	 * @param dataset dataset used to compute the sufficient statistics
	 */
	public void setSufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		for (int i = 0; i < nodes.size(); i++)
			setSufficientStatistics(nodes.get(i), dataset);
	}

	@Override
	public void learn(Node node, Dataset dataset) {
		setSufficientStatistics(node, dataset);
		setCIM((CIMNode) node);
	}

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		setSufficientStatistics(nodes, dataset);
		setCIMs(nodes);
	}

	@Override
	public void setSufficientStatistics(Node node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = getSufficientStatisticsNode((DiscreteNode) node, dataset);
		node.setSufficientStatistics(ssNode);
	}

	/**
	 * Estimates the parameters for a given node from its computed sufficient statistics. Two parameters that summarise
	 * the CIMs of each variable are estimated. The first one contains the probabilities of the variables leaving a
	 * state and the second one the probabilities of leaving a state for a certain one.
	 *
	 * @param node node with sufficient statistics where it is stored the computed parameters
	 */
	protected void estimateParameters(CIMNode node) {
		logger.trace("Learning parameters of CTBN node {}", node.getName());
		// Initialise structures to store the parameters
		int numStates = node.getNumStates();
		int numStatesParents = node.getNumStatesParents();
		double[][] Qx = new double[numStatesParents][numStates];
		double[][][] Oxx = new double[numStatesParents][numStates][numStates];
		// Retrieve sufficient statistics
		double[][][] Mxy = node.getSufficientStatistics().getMxy();
		double[][] Mx = node.getSufficientStatistics().getMx();
		double[][] Tx = node.getSufficientStatistics().getTx();
		// Iterate over the number of states of the node and its parents
		for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++)
			for (int idxFromState = 0; idxFromState < numStates; idxFromState++) {
				// Number of transitions from this state
				double MxFromState = Mx[idxStateParents][idxFromState];
				double TxFromState = Tx[idxStateParents][idxFromState];
				// Intensity of leaving the state
				double qx = MxFromState / TxFromState;
				// qx can be undefined if the priors are 0 (maximum likelihood estimation)
				if (Double.isNaN(qx))
					qx = 0;
				// Save the estimated intensity
				Qx[idxStateParents][idxFromState] = qx;
				for (int idxToState = 0; idxToState < numStates; idxToState++) {
					if (idxFromState != idxToState) {
						double oxx = Mxy[idxStateParents][idxFromState][idxToState] / MxFromState;
						// The previous operation can be undefined if the priors are 0
						if (Double.isNaN(oxx))
							oxx = 0;
						// Save the probability of transitioning from "fromState" to "toState"
						Oxx[idxStateParents][idxFromState][idxToState] = oxx;
					}
				}
			}
		// Set parameters in the CIMNode object
		node.setParameters(Qx, Oxx);
	}

	/**
	 * Returns the sufficient statistics of a {@code DiscreteNode} for a given {@code Dataset}.
	 *
	 * @param node    a {@code DiscreteNode}
	 * @param dataset dataset from which the sufficient statistics are extracted
	 * @return sufficient statistics of the provided node
	 */
	protected abstract CTBNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset);

	/**
	 * Sets the conditional intensity matrices of a CTBN node.
	 *
	 * @param node node
	 */
	private void setCIM(CIMNode node) {
		estimateParameters(node);
	}

	/**
	 * Sets the conditional intensity matrices of the nodes of a CTBN.
	 *
	 * @param nodes list of nodes
	 */
	private void setCIMs(List<? extends Node> nodes) {
		// For each node is estimated its CIMs
		nodes.stream().forEach(node -> {
			// Compute the parameters for the current node with its sufficient statistics.
			// The parameters are stored in the CIMNode
			setCIM((CIMNode) node);
		});
	}

}