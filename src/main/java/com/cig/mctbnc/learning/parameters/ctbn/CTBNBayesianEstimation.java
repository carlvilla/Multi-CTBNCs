package com.cig.mctbnc.learning.parameters.ctbn;

import java.util.HashMap;
import java.util.Map;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.CIMNode;

/**
 * Bayesian parameter estimation for CTBN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNBayesianEstimation extends CTBNParameterEstimation {

	private double NxyPrior;
	private double NxPrior;
	private double TxPrior;

	/**
	 * Constructor Bayesian parameter estimator for CTBNs.
	 * 
	 * @param NxyPrior
	 * @param NxPrior
	 * @param TxPrior
	 */
	public CTBNBayesianEstimation(double NxyPrior, double NxPrior, double TxPrior) {
		// Definition of imaginary counts of the hyperparameters
		this.NxyPrior = NxyPrior;
		this.NxPrior = NxPrior;
		this.TxPrior = TxPrior;
	}

	/**
	 * Constructor Bayesian parameter estimator for CTBNs with default values
	 * (Codecasa D. and Stella F. (2014)) for the priors.
	 */
	public CTBNBayesianEstimation() {
		this.NxyPrior = 1.0;
		this.NxPrior = 1.0;
		this.TxPrior = 0.005;
	}

	public void setNxyPrior(double nxyPrior) {
		NxyPrior = nxyPrior;
	}

	public void setNxPrior(double nxPrior) {
		NxPrior = nxPrior;
	}

	public void setTxPrior(double txPrior) {
		TxPrior = txPrior;
	}

	@Override
	protected void estimateParameters(CIMNode node) {
		logger.trace(
				"Learning parameters of CTBN node {} with Bayesian estimation using priors Nxx={}, Nx={} and Tx={}",
				node.getName(), this.NxyPrior, this.NxPrior, this.TxPrior);
		// Initialize structures to store the parameters
		Map<State, Double> Qx = new HashMap<State, Double>();
		Map<State, Map<State, Double>> Oxx = new HashMap<State, Map<State, Double>>();
		// Retrieve sufficient statistics
		Map<State, Map<State, Integer>> Nxy = node.getSufficientStatistics().getNxy();
		Map<State, Integer> Nx = node.getSufficientStatistics().getNx();
		Map<State, Double> Tx = node.getSufficientStatistics().getTx();
		// Parameter with probabilities of leaving a certain state
		for (State fromState : Nx.keySet()) {
			double qx = (this.NxyPrior + Nx.get(fromState)) / (this.TxPrior + Tx.get(fromState));
			Qx.put(fromState, qx);
		}
		// Parameter with probabilities of leaving a certain state for another
		for (State fromState : Nxy.keySet()) {
			for (State toState : Nxy.get(fromState).keySet()) {
				if (!Oxx.containsKey(fromState))
					Oxx.put(fromState, new HashMap<State, Double>());
				double oxx = (this.NxyPrior + Nxy.get(fromState).get(toState)) / (this.NxPrior + Nx.get(fromState));
				Oxx.get(fromState).put(toState, oxx);
			}
		}
		// Set parameters in the CIMNode object
		node.setParameters(Qx, Oxx);
	}

}
