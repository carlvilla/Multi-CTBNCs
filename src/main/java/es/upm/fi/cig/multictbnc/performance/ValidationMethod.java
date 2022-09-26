package es.upm.fi.cig.multictbnc.performance;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;

import java.util.Map;

/**
 * Abstract class defining common methods for validation algorithms.
 *
 * @author Carlos Villa Blanco
 */
public abstract class ValidationMethod {
	private MetricsWriter metricsWriter;

	/**
	 * Displays the model obtained with the validation method.
	 *
	 * @param model obtained model
	 */
	public void displayModel(MultiCTBNC<?, ?> model) {
		System.out.println(model);
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
	 * Evaluates the performance of the specified model and returns the results.
	 *
	 * @param model model to evaluate
	 * @return results saved in a {@code Map}.
	 * @throws UnreadDatasetException  if there was an error while reading a dataset
	 * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
	 */
	public abstract Map<String, Double> evaluate(MultiCTBNC<?, ?> model)
			throws UnreadDatasetException, ErroneousValueException;

	/**
	 * Defines the metrics writer used to save the results of the evaluation.
	 *
	 * @param metricsWriter a {@code MetricsWriter} used to save the results of the evaluation
	 */
	public void setWriter(MetricsWriter metricsWriter) {
		this.metricsWriter = metricsWriter;
	}

}