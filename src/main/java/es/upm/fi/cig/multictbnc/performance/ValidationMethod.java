package es.upm.fi.cig.multictbnc.performance;

import java.util.Map;

import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;

/**
 * Abstract class defining common methods for validation algorithms.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class ValidationMethod {
	MetricsWriter metricsWriter;

	/**
	 * Evaluates the performance of the specified model.
	 * 
	 * @param model model to evaluate
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public abstract void evaluate(MultiCTBNC<?, ?> model) throws UnreadDatasetException;

	/**
	 * Defines the metrics writer used to save the results of the evaluation.
	 * 
	 * @param metricsWriter a {@code MetricsWriter} used to save the results of the
	 *                      evaluation
	 */
	public void setWriter(MetricsWriter metricsWriter) {
		this.metricsWriter = metricsWriter;
	}

	/**
	 * Displays the results obtained with the validation method.
	 * 
	 * @param results a {@code Map} with the results of the validation method
	 */
	public void displayResults(Map<String, Double> results) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
		if (this.metricsWriter != null)
			this.metricsWriter.write(results);
	}

	/**
	 * Displays the model obtained with the validation method.
	 *
	 * @param model obtained model
	 */
	public void displayModel(MultiCTBNC<?, ?> model) {
		System.out.println(model);
	}

}
