package main.java.com.cig.mctbnc.models;

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

	public Sequence(String[] nameVariables, String[] nameClassVariables, List<String[]> valueObservations) {
		// Get observations with values of features
		observations = new ArrayList<Observation>();
		for (String[] valueObservation : valueObservations) {
			observations.add(new Observation(nameVariables, nameClassVariables, valueObservation));
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
	
	public List<Observation> getObservations(){
		return observations;
	}
	
	/**
	 * Get all the possible states of the specified variable 
	 * @param Variable
	 * @return
	 */
	public String[] getStates(String nameVariable) {
		if(classValues.containsKey(nameVariable)) {
			return new String[] {classValues.get(nameVariable)};
		}
		
		Set<String> states = new HashSet<String>();
		for(Observation observation:observations) {
			states.add(observation.getValueFeature(nameVariable));
		}
		return states.toArray(new String[states.size()]);
	}
	
	/**
	 * Get all the possible states of the specified variables together 
	 * @param Variable
	 * @return
	 */
	public String[] getStates(String[] nameVariables) {
		List<String> states = new ArrayList<String>();
		// If it is specified only class variables
		if(classValues.containsKey(nameVariables[0])) {
			for(String nameVariable:nameVariables)
				states.add(classValues.get(nameVariable));
		}
		return (String[]) states.toArray();
	}
	

	public String toString() {
		StringBuilder sb = new StringBuilder();
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
