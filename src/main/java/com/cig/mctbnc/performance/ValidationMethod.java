package com.cig.mctbnc.performance;

import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.performance.writers.MetricsWriter;

/**
 * Define common methods for validation algorithms.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class ValidationMethod {
	MetricsWriter metricsWriter;

	/**
	 * Evaluate the performance of the specified model.
	 * 
	 * @param model model to evaluate
	 */
	public abstract void evaluate(MCTBNC<?, ?> model);

	public void setWriter(MetricsWriter metricsWriter) {
		this.metricsWriter = metricsWriter;
	}

}
