package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sequence {

	private List<Observation> observations;
	private Map<String, String> classValues;
	private String nameTimeVariable;

	public Sequence(String[] nameVariables, String nameTimeVariable, String[] nameClassVariables,
			List<String[]> valueObservations) {
		// Set time variable
		this.nameTimeVariable = nameTimeVariable;

		// Get observations with values of features
		observations = new ArrayList<Observation>();
		for (String[] valueObservation : valueObservations) {
			observations.add(new Observation(nameVariables, nameClassVariables, nameTimeVariable, valueObservation));
		}

		// A sequence has a unique value for each class variable, so it is stored the
		// values of the class variables for the first observation
		classValues = new HashMap<String, String>();
		for (String nameClassVariable : nameClassVariables) {
			// It is obtained the index of each class variable in the observations
			for (int i = 0; i < nameVariables.length; i++) {
				if (nameVariables[i].equals(nameClassVariable)) {
					classValues.put(nameClassVariable, valueObservations.get(0)[i]);
				}
			}
		}
	}

	public String[] getValuesClassVariables() {
		Collection<String> values = classValues.values();
		return values.toArray(new String[values.size()]);
	}

	public String getValueClassVariable(String nameClassVariable) {
		return classValues.get(nameClassVariable);
	}

	public String[] getClassVariablesNames() {
		Set<String> keys = classValues.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	public String[] getFeatureNames() {
		return observations.get(0).getFeatureNames();
	}

	public String getTimeVariableName() {
		return nameTimeVariable;
	}

	public List<Observation> getObservations() {
		return observations;
	}

	/**
	 * Return the number of observations that the sequence contains.
	 * 
	 * @return number of observations
	 */
	public int getNumObservations() {
		return observations.size();
	}

	public Double[] getTimeValues() {
		return observations.stream().map(ob -> ob.getTimeValue()).toArray(Double[]::new);
	}

	/**
	 * Get all the possible states of a specific variable
	 * 
	 * @param nameVariable
	 *            name of the variable whose possible states we want to know
	 * @return Array with the states of the variable.
	 */
	public String[] getStates(String nameVariable) {
		if (classValues.containsKey(nameVariable)) {
			return new String[] { classValues.get(nameVariable) };
		}

		Set<String> states = new HashSet<String>();
		for (Observation observation : observations) {
			states.add(observation.getValueFeature(nameVariable));
		}
		return states.toArray(new String[states.size()]);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("----- TIME VARIABLE -----\n");
		sb.append(nameTimeVariable);
		sb.append("\n");
		for (Observation observation : observations) {
			sb.append(observation.getTimeValue());
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("----- CLASS VARIABLES -----\n");
		sb.append(String.join(",", classValues.keySet()));
		sb.append("\n");
		sb.append(String.join(",", classValues.values()));
		sb.append("\n");
		sb.append("----- FEATURES -----\n");
		sb.append(Arrays.toString(observations.get(0).getFeatureNames()));
		sb.append("\n");
		for (Observation observation : observations) {
			sb.append(Arrays.toString(observation.getValues()));
			sb.append("\n");
		}
		return sb.toString();
	}

}
