package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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

	/**
	 * Create an observation given the name of the variables and time variable, and
	 * their values.
	 * 
	 * @param nameVariables    name of the variables
	 * @param nameTimeVariable name of the time variable
	 * @param values           values of all the variables
	 */
	public Observation(List<String> nameVariables, String nameTimeVariable, String[] values) {
		// Store name of the variable and its value for the observation
		variablesValues = new LinkedHashMap<String, String>();
		for (int i = 0; i < nameVariables.size(); i++) {
			if (!nameVariables.get(i).equals(nameTimeVariable))
				variablesValues.put(nameVariables.get(i), values[i]);
			else
				timeValue = Double.parseDouble(values[i]);
		}
	}

	/**
	 * Create an observation given the value of the time variable separately.
	 * 
	 * @param nameVariables
	 * @param values
	 * @param timeValue
	 */
	public Observation(List<String> nameVariables, String[] values, double timeValue) {
		// Store name of the variable and its value for the observation
		variablesValues = new LinkedHashMap<String, String>();
		for (int i = 0; i < nameVariables.size(); i++)
			variablesValues.put(nameVariables.get(i), values[i]);
		// Store time value
		this.timeValue = timeValue;
	}

	/**
	 * Remove the value of a feature from the observation.
	 * 
	 * @param nameFeature
	 */
	public void removeFeatures(String nameFeature) {
		variablesValues.remove(nameFeature);
	}

	/**
	 * Return the name of the variables in the observation (except the time
	 * variable).
	 * 
	 * @return name of the variables
	 */
	public List<String> getVariableNames() {
		Set<String> keys = variablesValues.keySet();
		return new ArrayList<String>(keys);
	}

	/**
	 * Return the value of all the variables (except the time variable) for the
	 * observation.
	 * 
	 * @return values of the variables
	 */
	public String[] getValues() {
		Collection<String> values = variablesValues.values();
		return values.toArray(new String[values.size()]);
	}

	/**
	 * Return the value of the time variable for the observation.
	 * 
	 * @return value of the time variable
	 */
	public double getTimeValue() {
		return timeValue;
	}

	/**
	 * Given the name of a variable, return its value for the observation.
	 * 
	 * @param nameVariable name of the variable
	 * @return value of the variable for the observation
	 */
	public String getValueVariable(String nameVariable) {
		return variablesValues.get(nameVariable);
	}

	/**
	 * Given the names of variables, return their value for the observation.
	 * 
	 * @param nameVariables names of the variable
	 * @return values of the variables for the observation
	 */
	public String[] getValueVariables(List<String> nameVariables) {
		String[] values = new String[nameVariables.size()];
		for (int i = 0; i < nameVariables.size(); i++)
			values[i] = getValueVariable(nameVariables.get(i));
		return values;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Time|");
		for (String nameVariable : variablesValues.keySet())
			sb.append(nameVariable + "|");
		sb.append("\n");
		sb.append(timeValue + "|");
		for (String valueVariable : variablesValues.values())
			sb.append(valueVariable + "|");
		sb.append("\n");
		return sb.toString();
	}

}
