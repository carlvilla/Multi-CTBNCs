package com.cig.mctbnc.performance.writers;

import java.util.Map;

/**
 * Define classes that write the results of the metrics on different outputs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface MetricsWriter {

	void write(Map<String, Double> results);

	void close();

}
