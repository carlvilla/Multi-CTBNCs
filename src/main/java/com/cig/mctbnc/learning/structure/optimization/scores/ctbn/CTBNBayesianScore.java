package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import org.apache.commons.math3.special.Gamma;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.learning.structure.optimization.CTBNScoreFunction;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements the Bayesian Dirichlet equivalence metric for CTBNs (Nodelman et
 * al., 2003).
 */
public class CTBNBayesianScore implements CTBNScoreFunction {

	@Override
	public double compute(CTBN<? extends Node> ctbn) {
		double bdeScore = 0;
		for (int indexNode = 0; indexNode < ctbn.getNumNodes(); indexNode++)
			bdeScore += compute(ctbn, indexNode);
		return bdeScore;
	}

	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {

		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();

		System.out.println(node.getName());

//		System.out.println("-----------");
//		System.out.println("Nodo: " + node.getName());
//		System.out.println(Arrays.toString(node.getParents().stream().map(nodop -> nodop.getName()).toArray()));

		// Hyperparameters of the hyperprior distribution
		double mxyHP = ss.getMxyHyperparameter();
		double mxHP = ss.getMxHyperparameter();
		double txHP = ss.getTxHyperparameter();
		// Estimate score
		double bdeScore = 0.0;
		for (State state : node.getQx().keySet()) {

			double aux = bdeScore;


			double mx = ss.getMx().get(state);
			double tx = ss.getTx().get(state);
			
			bdeScore += Gamma.logGamma(mx + 1) + (mxHP + 1) * Math.log(txHP);
			bdeScore -= Gamma.logGamma(mxHP + 1) + (mx + 1) * Math.log(tx);

			bdeScore += Gamma.logGamma(mxHP) - Gamma.logGamma(mx);

			
			if(mxyHP*2 == mx) {
			System.out.println("X1: "+state.getValueVariable("X1") + "\nX2: "+state.getValueVariable("X2")+ "\nX3: "+state.getValueVariable("X3") + "\nC2: "+state.getValueVariable("C2"));
			
			System.out.println(mx - mxHP);
			System.out.println(tx - txHP);
			
			System.out.print("+ Gamma.logGamma(" + mxHP + ") - Gamma.logGamma(" + mx + ")");
			}
			
			double a = Gamma.logGamma(mxHP) - Gamma.logGamma(mx);

			for (State toState : node.getOxy().get(state).keySet()) {
				// Number of times the variable transitions from "state" to "toState"
				double mxy = ss.getMxy().get(state).get(toState);
				bdeScore += Gamma.logGamma(mxyHP) - Gamma.logGamma(mxy);

				if(mxyHP*2 == mx) {
				System.out.print(" + Gamma.logGamma(" + mxyHP + ") - Gamma.logGamma(" + mxy + ")");
				}

			}


			
			
			if(mxyHP*2 == mx) {
			System.out.println("+ Gamma.logGamma(" + mx + "+ 1) + (" + mxHP + "+ 1) * Math.log(" + txHP
					+ ") - Gamma.logGamma(" + mxHP + " + 1) + (" + mx + " + 1) * Math.log(" + tx + ")");
			System.out.println(bdeScore - aux);
			}

		}

//		System.out.println("Total: " + score);

		return bdeScore;
	}

}
