package com.cig.mctbnc.performance.writers;

import java.util.List;
import java.util.Map;

/**
 * Define classes that write the results of the metrics on different outputs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class MetricsWriter {
	List<String> nameClassVariables;

	public abstract void write(Map<String, Double> results);

	/**
	 * Close the writer.
	 */
	public void close() {
	}

	/**
	 * Establish the class variables that the writer should take into account.
	 * 
	 * @param nameClassVariables
	 */
	public void setClassVariables(List<String> nameClassVariables) {
		this.nameClassVariables = nameClassVariables;
	}

}