package com.cig.mctbnc.performance;

import java.util.Map;

import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.performance.writers.MetricsWriter;

/**
 * Abstract class defining common methods for validation algorithms.
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

	/**
	 * Defines the metrics writer used to save the results of the evaluation.
	 * 
	 * @param metricsWriter
	 */
	public void setWriter(MetricsWriter metricsWriter) {
		this.metricsWriter = metricsWriter;
	}

	/**
	 * Display the results obtained with the validation method.
	 * 
	 * @param results
	 */
	public void displayResults(Map<String, Double> results) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
		if (this.metricsWriter != null)
			this.metricsWriter.write(results);
	}

	/**
	 * Display the model obtained with the validation method.
	 *
	 * @param model
	 */
	public void displayModel(MCTBNC<?, ?> model) {
		System.out.println(model);
	}

}
