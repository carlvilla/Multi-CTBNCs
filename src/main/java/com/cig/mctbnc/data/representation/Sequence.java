package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
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

	// Path to the file from which the sequence was extracted (if it exists)
	private String filePath;

	/**
	 * Constructor
	 * 
	 * @param nameVariables
	 * @param nameTimeVariable
	 * @param nameClassVariables
	 * @param valueObservations
	 * @throws ErroneousSequenceException
	 */
	public Sequence(List<String> nameVariables, String nameTimeVariable, List<String> nameClassVariables,
			List<String[]> valueObservations) throws ErroneousSequenceException {
		// Set name of the time variable
		this.nameTimeVariable = nameTimeVariable;
		// Set the names of the features by filtering the names of the class variables
		// and time variable
		this.featureNames = Util.<String>filter(Util.<String>filter(nameVariables, nameClassVariables),
				nameTimeVariable);
		// Define values class variables for the sequence
		setValuesClassVariables(nameVariables, nameClassVariables, valueObservations);
		// Get observations with the values of all variables
		this.observations = new ArrayList<Observation>();
		for (String[] valueObservation : valueObservations) {
			Observation observation = new Observation(nameVariables, nameTimeVariable, valueObservation);
			checkIntegrityObservation(observation);
			this.observations.add(observation);
		}
	}

	/**
	 * Constructor used for the models when generating sequences.
	 * 
	 * @param stateClassVariables
	 * @param transitions
	 * @param nameTimeVariable
	 * @param time
	 * @throws ErroneousSequenceException
	 * 
	 */
	public Sequence(State stateClassVariables, List<State> transitions, String nameTimeVariable, List<Double> time)
			throws ErroneousSequenceException {
		// Set name of the time variable
		this.nameTimeVariable = nameTimeVariable;
		// Set names variables
		List<String> nameVariables = transitions.get(0).getNameVariables();
		// Get names features
		this.featureNames = Util.<String>filter(
				Util.<String>filter(nameVariables, stateClassVariables.getNameVariables()), nameTimeVariable);
		// Set values class variables
		this.classVariablesValues = stateClassVariables.getEvents();
		// Get observations with the values of all variables
		this.observations = new ArrayList<Observation>();
		for (int i = 0; i < time.size(); i++) {
			Observation observation = new Observation(nameVariables, transitions.get(i).getValues(), time.get(i));
			checkIntegrityObservation(observation);
			this.observations.add(observation);
		}
	}

	/**
	 * Remove a feature from the sequence.
	 * 
	 * @param nameFeature
	 */
	public void removeFeature(String nameFeature) {
		for (Observation observation : this.observations)
			observation.removeFeatures(nameFeature);
		this.featureNames.remove(nameFeature);
	}

	/**
	 * Set the path of file from which the sequence was extracted.
	 * 
	 * @param filePath
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Return a Map object with the names and the values of the class variables.
	 * 
	 * @return Map with names and values of the class variables
	 */
	public Map<String, String> getClassVariables() {
		return this.classVariablesValues;
	}

	/**
	 * Return the values of the specified class variable.
	 * 
	 * @param nameClassVariable name of the class variable
	 * @return values of the class variable
	 */
	public String getValueClassVariable(String nameClassVariable) {
		return this.classVariablesValues.get(nameClassVariable);
	}

	/**
	 * Return the names of the class variables.
	 * 
	 * @return names of the class variables
	 */
	public List<String> getClassVariablesNames() {
		Set<String> keys = this.classVariablesValues.keySet();
		return new ArrayList<String>(keys);
	}

	/**
	 * Return the names of the features.
	 * 
	 * @return names of the features.
	 */
	public List<String> getFeatureNames() {
		return this.featureNames;
	}

	/**
	 * Return the name of the time variable.
	 * 
	 * @return name of the time variable
	 */
	public String getTimeVariableName() {
		return this.nameTimeVariable;
	}

	/**
	 * Return all the observations of the sequence.
	 * 
	 * @return list of observations
	 */
	public List<Observation> getObservations() {
		return this.observations;
	}

	/**
	 * Return the number of observations that the sequence contains.
	 * 
	 * @return number of observations
	 */
	public int getNumObservations() {
		return this.observations.size();
	}

	/**
	 * Get all the possible states of a specific variable.
	 * 
	 * @param nameVariable name of the variable whose possible states we want to
	 *                     know
	 * @return Array with the states of the variable
	 */
	public String[] getStates(String nameVariable) {
		// If it is a class variable, there can only be one value per sequence.
		if (this.classVariablesValues.containsKey(nameVariable)) {
			return new String[] { this.classVariablesValues.get(nameVariable) };
		}
		// If it is a feature, it is stored in set all its possible unique values.
		Set<String> states = new HashSet<String>();
		for (Observation observation : this.observations) {
			states.add(observation.getValueVariable(nameVariable));
		}
		return states.toArray(new String[states.size()]);
	}

	/**
	 * Return the path of file from which the sequence was extracted.
	 * 
	 * @return path of file from which the sequence was extracted
	 */
	public String getFilePath() {
		return this.filePath;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("----- TIME VARIABLE -----\n");
		sb.append(this.nameTimeVariable);
		sb.append("\n");
		for (Observation observation : this.observations) {
			sb.append(observation.getTimeValue());
			sb.append("\n");
		}
		sb.append("\n");
		sb.append("----- CLASS VARIABLES -----\n");
		sb.append(String.join(",", this.classVariablesValues.keySet()));
		sb.append("\n");
		sb.append(String.join(",", this.classVariablesValues.values()));
		sb.append("\n");
		sb.append("----- FEATURES -----\n");
		sb.append(Arrays.toString(this.featureNames.toArray()));
		sb.append("\n");
		for (Observation observation : this.observations) {
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
		this.classVariablesValues = new LinkedHashMap<String, String>();
		for (String nameClassVariable : nameClassVariables) {
			// It is obtained the index of each class variable in the observations
			for (int i = 0; i < nameVariables.size(); i++) {
				if (nameVariables.get(i).equals(nameClassVariable)) {
					this.classVariablesValues.put(nameClassVariable, valueObservations.get(0)[i]);
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
		for (String nameClassVariable : this.classVariablesValues.keySet()) {
			if (!observation.getValueVariable(nameClassVariable)
					.equals(this.classVariablesValues.get(nameClassVariable))) {
				throw new ErroneousSequenceException("Observations have different values for the class variables");
			}
		}

	}
}
