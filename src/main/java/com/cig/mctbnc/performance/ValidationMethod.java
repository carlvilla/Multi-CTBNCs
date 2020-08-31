package com.cig.mctbnc.performance;

import com.cig.mctbnc.models.MCTBNC;

/**
 * Define common methods for validation algorithms.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface ValidationMethod {

	/**
	 * Evaluate the performance of the specified model.
	 * 
	 * @param model model to evaluate
	 */
	public void evaluate(MCTBNC<?, ?> model);

}
