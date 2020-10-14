package com.cig.mctbnc.learning.structure.optimization.scores;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public class AbstractLogLikelihood {
	protected String penalizationFunction;
	protected Map<String, DoubleUnaryOperator> penalizationFunctionMap = new HashMap<>() {
		private static final long serialVersionUID = 1L;
		{
			put("BIC", N -> Math.log(N) / 2);
			put("AIC", N -> 1.0);
		}
	};

	/**
	 * Receives the name of the penalization function used for the structure
	 * complexity.
	 * 
	 * @param penalizationFunction
	 */
	public AbstractLogLikelihood(String penalizationFunction) {
		this.penalizationFunction = penalizationFunction;
	}
}
