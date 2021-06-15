package es.upm.fi.cig.multictbnc.learning.structure.optimization.scores;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Abstract class defining common variables and methods for likelihood based
 * scores.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class AbstractLikelihood {
	protected String penalizationFunction;
	protected Map<String, DoubleUnaryOperator> penalizationFunctionMap = new HashMap<>() {
		private static final long serialVersionUID = 1L;
		{
			put("BIC", N -> Math.log(N) / 2);
			put("AIC", N -> 1.0);
		}
	};

	/**
	 * Receives the name of the penalization function for the structure complexity.
	 * 
	 * @param penalizationFunction name of the penalization function
	 */
	public AbstractLikelihood(String penalizationFunction) {
		this.penalizationFunction = penalizationFunction;
	}
}
