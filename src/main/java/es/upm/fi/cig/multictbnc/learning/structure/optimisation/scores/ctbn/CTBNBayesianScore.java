package es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn;

import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.commons.math3.special.Gamma;

/**
 * Implements the Bayesian Dirichlet equivalence metric for CTBNs with nodes that have CIMs (Nodelman et al., 2003).
 */
public class CTBNBayesianScore implements CTBNScoreFunction {

	@Override
	public double compute(CTBN<? extends Node> ctbn) {
		double bdeScore = 0;
		for (int idxNode = 0; idxNode < ctbn.getNumNodes(); idxNode++)
			bdeScore += compute(ctbn, idxNode);
		return bdeScore;
	}

	@Override
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Hyperparameters of the hyperprior distribution
		double mxyHP = ss.getMxyHyperparameter();
		double mxHP = ss.getMxHyperparameter();
		double txHP = ss.getTxHyperparameter();
		// Number of states of the node and its parents
		int numStates = node.getNumStates();
		int numStatesParents = node.getNumStatesParents();
		// Estimate score
		double bdeScore = 0.0;
		// Iterate over all states of the node (from where a transition begins)
		for (int idxFromState = 0; idxFromState < numStates; idxFromState++) {
			// Iterate over all states of the node's parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				// Establish the state of the node and its parents
				double mx = ss.getMx()[idxStateParents][idxFromState];
				double tx = ss.getTx()[idxStateParents][idxFromState];
				bdeScore += Gamma.logGamma(mx + 1) + (mxHP + 1) * Math.log(txHP);
				bdeScore -= Gamma.logGamma(mxHP + 1) + (mx + 1) * Math.log(tx);
				bdeScore += Gamma.logGamma(mxHP) - Gamma.logGamma(mx);
				// Iterate over all the states of the node (where a transition ends)
				for (int idxToState = 0; idxToState < numStates; idxToState++) {
					if (idxToState != idxFromState) {
						double mxy = ss.getMxy()[idxStateParents][idxFromState][idxToState];
						bdeScore += Gamma.logGamma(mxy) - Gamma.logGamma(mxyHP);
					}
				}
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

	@Override
	public boolean isDecomposable() {
		return true;
	}

}