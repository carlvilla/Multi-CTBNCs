package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cig.mctbnc.exceptions.ErroneousSequenceException;
import com.cig.mctbnc.util.Util;

/**
 * Represent a sequence of multivariate data, i.e., a set of data points with
 * multiple variables where the order is relevant. In this case, a sequence
 * represent a time series, since the data is ordered by a time variable.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Sequence {

	private List<Observation> observations;
	private Map<String, String> classVariablesValues;
	private String nameTimeVariable;
	// It is necessary to store these names since the dataset can be created
	// directly from sequences
	private List<String> featureNames;

	public Sequence(List<String> nameVariables, String nameTimeVariable, List<String> nameClassVariables,
			List<String[]> valueObservations) throws ErroneousSequenceException {
		// Set time variable
		this.nameTimeVariable = nameTimeVariable;
		// Set the names of the features by filtering the names of the class variables
		// and time variable
		this.featureNames = Util.<String>filter(Util.<String>filter(nameVariables, nameClassVariables),
				nameTimeVariable);

		// Define values class variables for the sequence
		setValuesClassVariables(nameVariables, nameClassVariables, valueObservations);

		// Get observations with the values of all variables
		observations = new ArrayList<Observation>();
		for (String[] valueObservation : valueObservations) {
			Observation observation = new Observation(nameVariables, nameTimeVariable, valueObservation);
			checkIntegrityObservation(observation);
			observations.add(observation);
		}
	}

	/**
	 * Remove a feature from the sequence.
	 * 
	 * @param nameFeature
	 */
	public void removeFeature(String nameFeature) {
		for (Observation observation : observations)
			observation.removeFeatures(nameFeature);
		featureNames.remove(nameFeature);
	}

	/**
	 * Return a Map object with the names and the values of the class variables.
	 * 
	 * @return Map with names and values of the class variables
	 */
	public Map<String, String> getClassVariables() {
		return classVariablesValues;
	}

	/**
	 * Return the values of the specified class variable.
	 * 
	 * @param nameClassVariable name of the class variable
	 * @return values of the class variable
	 */
	public String getValueClassVariable(String nameClassVariable) {
		return classVariablesValues.get(nameClassVariable);
	}

	/**
	 * Return the names of the class variables.
	 * 
	 * @return names of the class variables
	 */
	public List<String> getClassVariablesNames() {
		Set<String> keys = classVariablesValues.keySet();
		return new ArrayList<String>(keys);
	}

	/**
	 * Return the names of the features.
	 * 
	 * @return names of the features.
	 */
	public List<String> getFeatureNames() {
		return featureNames;
	}

	/**
	 * Return the name of the time variable.
	 * 
	 * @return name of the time variable
	 */
	public String getTimeVariableName() {
		return nameTimeVariable;
	}

	/**
	 * Return all the observations of the sequence.
	 * 
	 * @return list of observations
	 */
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

	/**
	 * Get all the possible states of a specific variable.
	 * 
	 * @param nameVariable name of the variable whose possible states we want to
	 *                     know
	 * @return Array with the states of the variable.
	 */
	public String[] getStates(String nameVariable) {
		// If it is a class variable, there can only be one value per sequence.
		if (classVariablesValues.containsKey(nameVariable)) {
			return new String[] { classVariablesValues.get(nameVariable) };
		}
		// If it is a feature, it is stored in set all its possible unique values.
		Set<String> states = new HashSet<String>();
		for (Observation observation : observations) {
			states.add(observation.getValueVariable(nameVariable));
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
		sb.append(String.join(",", classVariablesValues.keySet()));
		sb.append("\n");
		sb.append(String.join(",", classVariablesValues.values()));
		sb.append("\n");
		sb.append("----- FEATURES -----\n");
		sb.append(Arrays.toString(featureNames.toArray()));
		sb.append("\n");
		for (Observation observation : observations) {
			sb.append(Arrays.toString(observation.getValues()));
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * A sequence has a unique value for each class variable, so the values of the
	 * class variables for the first observation are stored.
	 * 
	 * @param nameVariables
	 * @param nameClassVariables
	 * @param valueObservations
	 */
	private void setValuesClassVariables(List<String> nameVariables, List<String> nameClassVariables,
			List<String[]> valueObservations) {
		// LinkedHashMap maintains the order of the class variables as in the dataset
		classVariablesValues = new LinkedHashMap<String, String>();
		for (String nameClassVariable : nameClassVariables) {
			// It is obtained the index of each class variable in the observations
			for (int i = 0; i < nameVariables.size(); i++) {
				if (nameVariables.get(i).equals(nameClassVariable)) {
					classVariablesValues.put(nameClassVariable, valueObservations.get(0)[i]);
				}
			}
		}
	}

	/**
	 * Check if the observation has different values for the class variables than
	 * those defined for the sequence. In such a case, the sequence is erroneous and
	 * an exception is thrown
	 * 
	 * @param observation observation to analyze
	 * @throws ErroneousSequenceException
	 */
	private void checkIntegrityObservation(Observation observation) throws ErroneousSequenceException {
		for (String nameClassVariable : classVariablesValues.keySet()) {
			if (!observation.getValueVariable(nameClassVariable).equals(classVariablesValues.get(nameClassVariable))) {
				throw new ErroneousSequenceException("Observations have different values for the class variables");
			}
		}

	}

}
