package es.upm.fi.cig.mctbnc.writers.performance;

import java.util.List;
import java.util.Map;

/**
 * Defines classes that write the results of evaluation metrics on different
 * outputs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class MetricsWriter {
	List<String> nameClassVariables;

	/**
	 * Writes the given results.
	 * 
	 * @param results a {@code Map} with the results of the evaluation metrics
	 */
	public abstract void write(Map<String, Double> results);

	/**
	 * Closes the writer.
	 */
	public void close() {
		return;
	}

	/**
	 * Establishes the class variables that the writer should take into account.
	 * 
	 * @param nameClassVariables name of the class variables
	 */
	public void setClassVariables(List<String> nameClassVariables) {
		this.nameClassVariables = nameClassVariables;
	}

}