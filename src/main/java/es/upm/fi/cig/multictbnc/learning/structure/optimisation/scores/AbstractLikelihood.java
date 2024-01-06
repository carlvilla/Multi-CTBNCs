package es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Abstract class defining common variables and methods for likelihood-based scores.
 *
 * @author Carlos Villa Blanco
 */
public class AbstractLikelihood {
	String penalisationFunction;
	Map<String, DoubleUnaryOperator> penalisationFunctionMap = new HashMap<>() {
		{
			put("BIC", N -> Math.log(N) / 2);
			put("AIC", N -> 1.0);
		}
	};

	/**
	 * Receives the name of the penalisation function for the structure complexity.
	 *
	 * @param penalisationFunction name of the penalisation function
	 */
	public AbstractLikelihood(String penalisationFunction) {
		this.penalisationFunction = penalisationFunction;
	}

	/**
	 * Returns the name of the penalisation function.
	 *
	 * @return name of the penalisation function
	 */
	public String getNamePenalisationFunction() {
		return this.penalisationFunction;
	}

	/**
	 * Returns the name of the penalisation function.
	 *
	 * @return name of the penalisation function
	 */
	public DoubleUnaryOperator getPenalisationFunction() {
		return this.penalisationFunctionMap.get(this.getNamePenalisationFunction());
	}

}