package es.upm.fi.cig.multictbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousSequenceException;
import es.upm.fi.cig.multictbnc.util.Util;

/**
 * Represents a sequence of multivariate data, i.e., a set of data points with
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
	private List<String> namefeatureVariables;

	// Path to the file from which the sequence was extracted (if it exists)
	private String filePath;

	/**
	 * Constructs a {@code Sequence}. It receives the names of all the variables and
	 * those that are time and class variables, and the observations of the
	 * sequence.
	 * 
	 * @param nameVariables      names of all the variables in the sequence. It is
	 *                           assumed that they are presented in the same order
	 *                           as they appear in the data of the observations
	 * @param nameTimeVariable   name of the time variable
	 * @param nameClassVariables name of the class variables
	 * @param dataObservations   list of arrays with the values of the observations
	 * @throws ErroneousSequenceException if a valid sequence cannot be created with
	 *                                    the provided data
	 */
	public Sequence(List<String> nameVariables, String nameTimeVariable, List<String> nameClassVariables,
			List<String[]> dataObservations) throws ErroneousSequenceException {
		// Set name of the time variable
		this.nameTimeVariable = nameTimeVariable;
		// Set the names of the features by filtering the names of the class variables
		// and time variable
		this.namefeatureVariables = Util.<String>filter(Util.<String>filter(nameVariables, nameClassVariables),
				nameTimeVariable);
		// Define values class variables for the sequence
		setValuesClassVariables(nameVariables, nameClassVariables, dataObservations);
		generateObservations(nameVariables, nameTimeVariable, dataObservations);
	}

	/**
	 * Constructs a {@code Sequence}. It receives the names of allthe variables and
	 * the observations of the sequence.
	 * 
	 * @param nameVariables        names of all the variables in the sequence. It is
	 *                             assumed that they are presented in the same order
	 *                             as they appear in the data of the observations
	 * @param nameTimeVariable     name of the time variable
	 * @param nameClassVariables   names of the class variables
	 * @param nameFeatureVariables names of the feature variables
	 * @param dataObservations     list of arrays with the values of the
	 *                             observations
	 * @throws ErroneousSequenceException if a valid sequence cannot be created with
	 *                                    the provided data
	 */
	public Sequence(List<String> nameVariables, String nameTimeVariable, List<String> nameClassVariables,
			List<String> nameFeatureVariables, List<String[]> dataObservations) throws ErroneousSequenceException {
		// Set name of the time variable
		this.nameTimeVariable = nameTimeVariable;
		// Set the names of the features by filtering the names of the class variables
		// and time variable
		this.namefeatureVariables = nameFeatureVariables;
		if (nameClassVariables != null)
			// Define values class variables for the sequence
			setValuesClassVariables(nameVariables, nameClassVariables, dataObservations);
		// Generate the observations with the values of all variables
		generateObservations(nameVariables, nameTimeVariable, dataObservations);
	}

	/**
	 * Generate the observations of the sequence.
	 * 
	 * @param nameVariables    names of all the variables in the sequence. It is
	 *                         assumed that they are presented in the same order as
	 *                         they appear in the data of the observations
	 * @param nameTimeVariable name of the time variable
	 * @param dataObservations
	 * @throws ErroneousSequenceException
	 */
	private void generateObservations(List<String> nameVariables, String nameTimeVariable,
			List<String[]> dataObservations) throws ErroneousSequenceException {
		this.observations = new ArrayList<Observation>();
		for (String[] dataObservation : dataObservations) {
			Observation observation = new Observation(nameVariables, nameTimeVariable, dataObservation);
			checkIntegrityObservation(observation);
			this.observations.add(observation);
		}
	}

	/**
	 * Constructs a {@code Sequence}. This constructor is used by the models to
	 * sample sequences.
	 * 
	 * @param stateClassVariables class configuration of the class variables
	 * @param observations        list of {@code State} representing the
	 *                            observations of the sequence
	 * @param nameTimeVariable    name of the time variable
	 * @param timestamps          list of {@code Double} representing the timestamps
	 *                            when each observation of the sequence was obtained
	 * @throws ErroneousSequenceException if a valid sequence cannot be created with
	 *                                    the provided data
	 * 
	 */
	public Sequence(State stateClassVariables, List<State> observations, String nameTimeVariable,
			List<Double> timestamps) throws ErroneousSequenceException {
		if (observations.size() != timestamps.size())
			throw new ErroneousSequenceException("The number of observations and timestamps differ");
		// Set name of the time variable
		this.nameTimeVariable = nameTimeVariable;
		// Set names variables
		List<String> nameVariables = observations.get(0).getNameVariables();
		// Get names feature variables
		this.namefeatureVariables = Util.<String>filter(
				Util.<String>filter(nameVariables, stateClassVariables.getNameVariables()), nameTimeVariable);
		// Set values class variables
		this.classVariablesValues = stateClassVariables.getEvents();
		// Get observations with the values of all variables
		this.observations = new ArrayList<Observation>();
		for (int i = 0; i < timestamps.size(); i++) {
			Observation observation = new Observation(nameVariables, observations.get(i).getValues(),
					timestamps.get(i));
			checkIntegrityObservation(observation);
			this.observations.add(observation);
		}
	}

	/**
	 * Removes a feature from the sequence.
	 * 
	 * @param nameFeature name of the feature variable
	 */
	public void removeFeature(String nameFeature) {
		for (Observation observation : this.observations)
			observation.removeFeatureVariable(nameFeature);
		this.namefeatureVariables.remove(nameFeature);
	}

	/**
	 * Sets the path of the file from which the sequence was extracted.
	 * 
	 * @param filePath path of the file
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Returns a Map object with the names and the values of the class variables.
	 * 
	 * @return Map with names and values of the class variables
	 */
	public Map<String, String> getClassVariables() {
		return this.classVariablesValues;
	}

	/**
	 * Returns the values of the specified class variable.
	 * 
	 * @param nameClassVariable name of the class variable
	 * @return values of the class variable
	 */
	public String getValueClassVariable(String nameClassVariable) {
		return this.classVariablesValues.get(nameClassVariable);
	}

	/**
	 * Returns the names of the class variables.
	 * 
	 * @return names of the class variables
	 */
	public List<String> getNameClassVariables() {
		Set<String> keys = this.classVariablesValues.keySet();
		return new ArrayList<String>(keys);
	}

	/**
	 * Returns the names of the feature variables.
	 * 
	 * @return names of the feature variables.
	 */
	public List<String> getNameFeatureVariables() {
		return this.namefeatureVariables;
	}

	/**
	 * Returns the name of the time variable.
	 * 
	 * @return name of the time variable
	 */
	public String getNameTimeVariable() {
		return this.nameTimeVariable;
	}

	/**
	 * Returns all the observations of the sequence.
	 * 
	 * @return list of observations
	 */
	public List<Observation> getObservations() {
		return this.observations;
	}

	/**
	 * Returns the number of observations that the sequence contains.
	 * 
	 * @return number of observations
	 */
	public int getNumObservations() {
		return this.observations.size();
	}

	/**
	 * Gets all the possible states of a specific variable.
	 * 
	 * @param nameVariable name of the variable whose possible states we want to
	 *                     know
	 * @return Array with the states of the variable
	 */
	public String[] getStates(String nameVariable) {
		// If it is a class variable, there can only be one value per sequence.
		if (this.classVariablesValues != null && this.classVariablesValues.containsKey(nameVariable)) {
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
	 * Returns the path of file from which the sequence was extracted.
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
		sb.append("----- FEATURE VARIABLES -----\n");
		sb.append(Arrays.toString(this.namefeatureVariables.toArray()));
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
	 * @param nameVariables      names of all variables
	 * @param nameClassVariables names of the class variables
	 * @param dataObservations   list of arrays with the values of the observations
	 */
	private void setValuesClassVariables(List<String> nameVariables, List<String> nameClassVariables,
			List<String[]> dataObservations) {
		// LinkedHashMap maintains the order of the class variables as in the dataset
		this.classVariablesValues = new LinkedHashMap<String, String>();
		for (String nameClassVariable : nameClassVariables) {
			// It is obtained the index of each class variable in the observations
			for (int i = 0; i < nameVariables.size(); i++) {
				if (nameVariables.get(i).equals(nameClassVariable)) {
					this.classVariablesValues.put(nameClassVariable, dataObservations.get(0)[i]);
				}
			}
		}
	}

	/**
	 * Checks if the observation has different values for the class variables (if
	 * any) than those defined for the sequence. In such a case, the sequence is
	 * erroneous and an exception is thrown
	 * 
	 * @param observation observation to analyze
	 * @throws ErroneousSequenceException if a valid sequence cannot be created with
	 *                                    the provided data
	 */
	private void checkIntegrityObservation(Observation observation) throws ErroneousSequenceException {
		if (this.classVariablesValues != null)
			for (String nameClassVariable : this.classVariablesValues.keySet()) {
				if (!observation.getValueVariable(nameClassVariable)
						.equals(this.classVariablesValues.get(nameClassVariable))) {
					throw new ErroneousSequenceException("Observations have different values for the class variables");
				}
			}

	}
}
