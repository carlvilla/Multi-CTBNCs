package es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn;

import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.commons.math3.special.Gamma;

import java.util.List;

/**
 * Implements the Bayesian Dirichlet equivalence metric for Bayesian networks with nodes that have CPTs (Heckerman et
 * al., 1995).
 */
public class BNBayesianScore implements BNScoreFunction {

	@Override
	@SuppressWarnings("unchecked")
	public double compute(BN<? extends Node> bn) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = (List<CPTNode>) bn.getLearntNodes();
		double bdeScore = 0.0;
		for (CPTNode node : nodes) {
			// Hyperparameters of the hyperprior distribution (The same one is assumed for
			// every state)
			double nxHP = node.getSufficientStatistics().getNxHyperparameter();
			// Retrieve sufficient statistics of the node. They include the hyperparameters
			double[][] Nx = node.getSufficientStatistics().getNx();
			// Number of states of the node and its parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			// Iterate over all states of the node's parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				// Number of times the parents have a certain state independently of the node
				double NState = 0;
				// Iterate over all states of the node (from where a transition begins)
				for (int idxFromStateNode = 0; idxFromStateNode < numStates; idxFromStateNode++) {
					NState += Nx[idxStateParents][idxFromStateNode];
					bdeScore += Gamma.logGamma(Nx[idxStateParents][idxFromStateNode]) - Gamma.logGamma(nxHP);
				}
				bdeScore += Gamma.logGamma(nxHP * numStates) - Gamma.logGamma(NState);
			}
		}
		return bdeScore;
	}

	@Override
	public String getIdentifier() {
		return "Bayesian Dirichlet equivalent";
	}

	@Override
	public String getNamePenalisationFunction() {
		return "No";
	}
}