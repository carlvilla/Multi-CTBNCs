package com.cig.mctbnc.data.representation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines an observation of a sequence.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Observation {

	// Store the value of the time variable when the observation occurred
	private double timeValue;
	// Store the value of all the variables (except the time variable)
	private Map<String, String> variablesValues;

	public Observation(List<String> nameVariables, String nameTimeVariable, String[] values) {
		// Store name of the variable and its value for the observation
		variablesValues = new HashMap<String, String>();
		for (int i = 0; i < nameVariables.size(); i++) {
			if (!nameVariables.get(i).equals(nameTimeVariable)) {
				variablesValues.put(nameVariables.get(i), values[i]);
			} else if (nameVariables.get(i).equals(nameTimeVariable)) {
				timeValue = Double.parseDouble(values[i]);
			}
		}
	}

	public String[] getVariableNames() {
		Set<String> keys = variablesValues.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	public String[] getValues() {
		Collection<String> values = variablesValues.values();
		return values.toArray(new String[values.size()]);
	}

	public String getValueVariable(String nodeName) {
		return variablesValues.get(nodeName);
	}

	public double getTimeValue() {
		return timeValue;
	}

}
