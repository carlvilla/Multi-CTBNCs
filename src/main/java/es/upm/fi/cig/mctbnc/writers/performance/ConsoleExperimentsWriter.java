package es.upm.fi.cig.mctbnc.writers.performance;

import java.util.Map;

/**
 *
 * Allows writing the results of the experiments through the standard output
 * stream.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ConsoleExperimentsWriter extends MetricsWriter {

	@Override
	public void write(Map<String, Double> results) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
	}

}