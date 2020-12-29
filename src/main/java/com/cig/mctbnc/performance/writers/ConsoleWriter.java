package com.cig.mctbnc.performance.writers;

import java.util.Map;

public class ConsoleWriter extends MetricsWriter {

	@Override
	public void write(Map<String, Double> results) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
	}

}
