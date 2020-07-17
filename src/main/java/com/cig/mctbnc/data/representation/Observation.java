package com.cig.mctbnc.data.representation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Defines an observation of a sequence.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Observation {

	private double timeValue;
	private Map<String, String> mapValues;

	public Observation(String[] nameVariables, String[] nameClassVariables, String nameTimeVariable, String[] values) {

		// Store name of the variable and its value for the observation
		mapValues = new HashMap<String, String>();
		for (int i = 0; i < nameVariables.length; i++) {
			if (!Arrays.asList(nameClassVariables).contains(nameVariables[i])
					&& !nameVariables[i].equals(nameTimeVariable)) {
				mapValues.put(nameVariables[i], values[i]);
			} else if (nameVariables[i].equals(nameTimeVariable)) {
				timeValue = Double.parseDouble(values[i]);
			}
		}
	}

	public String[] getFeatureNames() {
		Set<String> keys = mapValues.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	public String[] getValues() {
		Collection<String> values = mapValues.values();
		return values.toArray(new String[values.size()]);
	}

	public String getValueFeature(String nodeName) {
		return mapValues.get(nodeName);
	}

	public double getTimeValue() {
		return timeValue;
	}

}