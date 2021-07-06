package es.upm.fi.cig.multictbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousSequenceException;

/**
 * Represents a time series dataset, which stores sequences and provides methods
 * to access and modify their information.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Dataset {
	private List<Sequence> sequences;
	private String nameTimeVariable;
	private List<String> nameFeatureVariables;
	private List<String> nameClassVariables;
	// A list of class variables that should be ignored
	private List<String> ignoredClassVariables;
	// Store the possible states of a variable to avoid recomputations
	private Map<String, List<String>> statesVariables;
	static Logger logger = LogManager.getLogger(Dataset.class);

	/**
	 * Constructor that creates an empty dataset with the names of the time variable
	 * and class variables.
	 * 
	 * @param nameTimeVariable   name of the time variable
	 * @param nameClassVariables names of the class variables
	 */
	public Dataset(String nameTimeVariable, List<String> nameClassVariables) {
		this.sequences = new ArrayList<Sequence>();
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
		initialiazeStructures();
	}

	/**
	 * Constructor that creates a dataset with a list of sequences.
	 * 
	 * @param sequences list of {@code Sequence}
	 */
	public Dataset(List<Sequence> sequences) {
		this.sequences = sequences;
		this.nameFeatureVariables = sequences.get(0).getNameFeatureVariables();
		this.nameClassVariables = sequences.get(0).getNameClassVariables();
		this.nameTimeVariable = sequences.get(0).getNameTimeVariable();
		initialiazeStructures();
	}

	private void initialiazeStructures() {
		this.statesVariables = new HashMap<String, List<String>>();
	}

	/**
	 * Receives a list of Strings (a sequence) from which a {@code Sequence} is
	 * created, and add it to the dataset. The first array of Strings has to contain
	 * the name of the variables.
	 * 
	 * @param data list of Strings (a sequence) where the first array contains the
	 *             name of the variables
	 * @return <code>true</code> if the sequence was successfully added to the
	 *         dataset; <code>false</code> otherwise.
	 */
	public boolean addSequence(List<String[]> data) {
		return addSequence(data, "No file path");
	}

	/**
	 * Receives a list of Strings (a sequence) and the path of the file from which
	 * it was extracted. Then create a {@code Sequence} and add it to the dataset.
	 * The first array of Strings representing the sequence has to contain the name
	 * of the variables.
	 * 
	 * @param data     list of Strings (a sequence) where the first array contains
	 *                 the name of the variables
	 * @param filePath path of the file from from which the sequence was extracted
	 * @return <code>true</code> if the sequence was successfully added to the
	 *         dataset; <code>false</code> otherwise.
	 */
	public boolean addSequence(List<String[]> data, String filePath) {
		try {
			Sequence sequence = createSequence(data);
			sequence.setFilePath(filePath);
			this.sequences.add(sequence);
			return true;
		} catch (ErroneousSequenceException e) {
			logger.warn("Sequence {} not added. {}", filePath, e.getMessage());
			return false;
		}
	}

	/**
	 * Receives a list of Strings (a sequence) from which a {@code Sequence} is
	 * created.
	 * 
	 * @param data data from which the {@code Sequence} is generated
	 * @return a {@code Sequence}
	 * @throws ErroneousSequenceException
	 */
	private Sequence createSequence(List<String[]> data) throws ErroneousSequenceException {
		// Check if it is possible to add the sequence
		checkIntegrityData(data);
		// Obtain names of variables
		List<String> nameVariablesSequence = Arrays.asList(data.get(0));
		// If there are no sequences in the dataset, it is stored the name of the
		// feature variables. They are given by the names of the variables that were not
		// defined as the time variable or class variable.
		if (getSequences().size() == 0)
			this.nameFeatureVariables = extractFeatureNames(nameVariablesSequence);
		// Drop names of variables
		data.remove(0);
		// Create sequence. Check if information about class variables is provided
		return new Sequence(nameVariablesSequence, this.nameTimeVariable, this.nameClassVariables,
				this.nameFeatureVariables, data);

	}

	/**
	 * Extract the name of the feature variables knowing which are the time and
	 * class variables.
	 * 
	 * @param nameVariablesSequence names of all variables
	 * @return names of the feature variables
	 */
	private List<String> extractFeatureNames(List<String> nameVariablesSequence) {
		return nameVariablesSequence.stream()
				.filter(name -> !name.equals(this.nameTimeVariable)
						&& (this.nameClassVariables == null || !this.nameClassVariables.contains(name)))
				.collect(Collectors.toList());
	}

	/**
	 * Removes from the dataset those feature variables with zero variance.
	 */
	public void removeZeroVarianceFeatures() {
		// Use temporal list to avoid a concurrent modification exception
		List<String> tempList = new ArrayList<String>(this.nameFeatureVariables);
		for (String nameFeature : tempList) {
			if (getPossibleStatesVariable(nameFeature).size() == 1) {
				logger.warn("Feature variable {} is removed since its variance is zero", nameFeature);
				removeFeature(nameFeature);
			}
		}
	}

	/**
	 * Removes a feature from the dataset.
	 * 
	 * @param nameFeature name of the feature variable to remove
	 */
	private void removeFeature(String nameFeature) {
		// Remove the feature from all the sequences of the dataset
		for (Sequence sequence : this.sequences)
			sequence.removeFeature(nameFeature);
		// Remove the name of the feature from the dataset
		this.nameFeatureVariables.remove(nameFeature);
	}

	/**
	 * Sets the class variables to ignored.
	 * 
	 * @param ignoredClassVariables names of the class variables to ignore
	 */
	public void setIgnoredClassVariables(List<String> ignoredClassVariables) {
		this.ignoredClassVariables = ignoredClassVariables;
	}

	/**
	 * Returns the sequences of the dataset.
	 * 
	 * @return list with the sequences of the dataset
	 */
	public List<Sequence> getSequences() {
		return new ArrayList<Sequence>(this.sequences);
	}

	/**
	 * Returns the name of the time variable.
	 * 
	 * @return name of time variable
	 */
	public String getNameTimeVariable() {
		return this.nameTimeVariable;
	}

	/**
	 * Returns the names of the feature variables.
	 * 
	 * @return list with the names of the feature variables
	 */
	public List<String> getNameFeatureVariables() {
		return this.nameFeatureVariables;
	}

	/**
	 * Returns the name of the class variables. It is filtered those class variables
	 * that should be ignored.
	 * 
	 * @return list with the names of the class variables
	 */
	public List<String> getNameClassVariables() {
		if (!(this.ignoredClassVariables == null || this.ignoredClassVariables.isEmpty()))
			return this.nameClassVariables.stream().filter(var -> !this.ignoredClassVariables.contains(var))
					.collect(Collectors.toList());
		return this.nameClassVariables;
	}

	/**
	 * Returns the name of all the variables except the time variable. The returned
	 * list contains first the features and then the class variables.
	 * 
	 * @return name of all the variables except the time variable
	 */
	public List<String> getNameVariables() {
		List<String> nameVariables = new ArrayList<String>();
		nameVariables.addAll(getNameFeatureVariables());
		// If there are class variables, add them to the list
		Optional.ofNullable(getNameClassVariables()).ifPresent(nameVariables::addAll);
		return nameVariables;
	}

	/**
	 * Returns the name of all the variables, including the time variable.
	 * 
	 * @return name of all the variables
	 */
	public List<String> getNameAllVariables() {
		List<String> nameVariables = new ArrayList<String>();
		nameVariables.add(getNameTimeVariable());
		nameVariables.addAll(getNameVariables());
		return nameVariables;
	}

	/**
	 * Returns the number of feature variables.
	 * 
	 * @return number of feature variables
	 */
	public int getNumFeatureVariables() {
		return this.nameFeatureVariables.size();
	}

	/**
	 * Returns the number of class variables.
	 * 
	 * @return number of class variables.
	 */
	public int getNumClassVariables() {
		return this.nameClassVariables.size();
	}

	/**
	 * Returns the number of variables (without the variable for the time).
	 * 
	 * @return the number of variables
	 */
	public int getNumVariables() {
		return getNumClassVariables() + getNumFeatureVariables();
	}

	/**
	 * Returns the number of data points. In this case, this is the number of
	 * sequences.
	 * 
	 * @return number of sequences
	 */
	public int getNumDataPoints() {
		return getSequences().size();
	}

	/**
	 * Returns the number of observations in the dataset, i.e., the number of
	 * transitions that occur in all the sequences.
	 * 
	 * @return number of observations
	 */
	public int getNumObservation() {
		return this.sequences.stream().mapToInt(sequence -> sequence.getNumObservations()).sum();
	}

	/**
	 * Gets the states of the class variables for each of the sequences.
	 * 
	 * @return array of {@code State} objects
	 */
	public State[] getStatesClassVariables() {
		State[] stateClassVariables = new State[getSequences().size()];
		for (int i = 0; i < getSequences().size(); i++)
			stateClassVariables[i] = new State(getSequences().get(i).getClassVariables());
		return stateClassVariables;
	}

	/**
	 * Sets states of all variables. This method is used when a training and testing
	 * dataset is defined and the training dataset needs to know all possible
	 * states.
	 * 
	 * @param statesVariables a {code Map} linking the names of the variables with
	 *                        their possible states
	 * 
	 */
	public void setStatesVariables(Map<String, List<String>> statesVariables) {
		this.statesVariables = statesVariables;
	}

	/**
	 * Gets the possible states of all variables.
	 * 
	 * @return array of {@code State} objects
	 */
	public Map<String, List<String>> getStatesVariables() {
		return this.statesVariables;
	}

	/**
	 * Returns the possible states of the specified variable. The states of the
	 * variable are extracted once and stored in a map to avoid recomputations. In
	 * order to not return always a reference to the same State list, the State
	 * objects from the map are copied.
	 * 
	 * @param nameVariable variable name
	 * @return states of the variable
	 */
	public List<String> getPossibleStatesVariable(String nameVariable) {
		// Extract states from the Map (if previously obtained)
		List<String> states = this.statesVariables.get(nameVariable);
		if (states == null) {
			// Use a HashSet to save each state just once
			Set<String> statesSet = new HashSet<String>();
			for (Sequence sequence : getSequences()) {
				String[] statesSequence = sequence.getStates(nameVariable);
				for (int i = 0; i < statesSequence.length; i++) {
					statesSet.add(statesSequence[i]);
				}
			}
			states = new ArrayList<String>(statesSet);
			this.statesVariables.put(nameVariable, states);
		}
		return states;
	}

	/**
	 * Counts the number of times in the dataset that the specified variables (in
	 * {@code State} object "query") take certain values.
	 * 
	 * @param query {@code State} object that specifies the variables and their
	 *              values
	 * @return number of times specified values take a certain state together
	 */
	public int getNumOccurrences(State query) {
		int numOccurrences = 0;
		List<String> nameVariables = query.getNameVariables();
		// If all the variables are class variables, then it is not necessary to
		// check the observations of each sequence
		boolean onlyClassVariable = getNameClassVariables().containsAll(nameVariables);
		if (onlyClassVariable)
			for (Sequence sequence : getSequences()) {
				boolean occurrence = true;
				// Check if the events of the query are the same as the events in the sequence
				Map<String, String> events = query.getEvents();
				for (String nameVariable : events.keySet()) {
					occurrence = events.get(nameVariable).equals(sequence.getValueClassVariable(nameVariable));
					if (!occurrence)
						break;
				}
				if (occurrence)
					numOccurrences++;
			}
		// If there are feature variables, it is necessary to study the observations
		else
			throw new UnsupportedOperationException();
		return numOccurrences;
	}

	/**
	 * Counts the number of times in the dataset that a certain variable transitions
	 * from a certain state ("fromState") to another ("toState"), while its parents
	 * take a certain state ("fromState"). It is assumed that the studied variable
	 * is the first one of the {@code State} objects.
	 * 
	 * @param fromState give the original states of the variable and its parents
	 * @param toState   give the state of the variable after the transition
	 * @return number of times the transition occurs
	 */
	public int getNumOccurrencesTransition(State fromState, State toState) {
		String nameVariable = toState.getNameVariables().get(0);
		List<String> nameParents = null;
		if (fromState.getNumEvents() > 1)
			// The variable has parents
			nameParents = fromState.getNameVariables().stream().filter(name -> !name.equals(nameVariable))
					.collect(Collectors.toList());
		int numOccurrences = 0;
		for (Sequence sequence : getSequences())
			for (int i = 1; i < sequence.getNumObservations(); i++) {
				// It is obtained one observation representing the states of the variables
				// before and after the transition
				Observation observationBefore = sequence.getObservations().get(i - 1);
				Observation observationAfter = sequence.getObservations().get(i);
				// Check if the variable starts from the expected value
				boolean expectedValueBefore = observationBefore.getValueVariable(nameVariable)
						.equals(fromState.getValueVariable(nameVariable));
				if (expectedValueBefore) {
					// Check if the studied variable transitions to the expected value
					boolean expectedValueAfter = observationAfter.getValueVariable(nameVariable)
							.equals(toState.getValueVariable(nameVariable));
					// It is only necessary to check the states of the parents if the variable
					// transitions from and to the expected values
					if (expectedValueAfter && nameParents != null) {
						boolean expectedValueParents = true;
						for (String nameParent : nameParents) {
							expectedValueParents = expectedValueParents && observationBefore
									.getValueVariable(nameParent).equals(fromState.getValueVariable(nameParent));
							if (!expectedValueParents)
								continue;
						}
						numOccurrences++;
					} else if (expectedValueAfter)
						numOccurrences++;
				}
			}
		return numOccurrences;
	}

	/**
	 * Returns how much time some variables stay in a certain state.
	 * 
	 * @param state state that contains the variables and the values to study
	 * @return time the state is maintained
	 */
	public double getTimeState(State state) {
		double time = 0;
		for (Sequence sequence : getSequences())
			for (int i = 0; i < sequence.getNumObservations(); i++) {
				// A pivot observation is created to check if it has the studied state and, in
				// that case, how many subsequent observations have the same state too
				Observation pivotObservation = sequence.getObservations().get(i);
				// Check that every variable has the expected value in the pivot observation
				boolean isInStudiedState = observationInState(pivotObservation, state);
				if (isInStudiedState)
					for (int j = i + 1; j < sequence.getNumObservations(); j++) {
						// Check which subsequent observations have the studied state
						Observation subsequentObservation = sequence.getObservations().get(j);
						// Check that every variable has the expected value in the subsequent
						// observation
						isInStudiedState = observationInState(subsequentObservation, state);
						// If the subsequent observation has the studied state too, the time is computed
						// if it is the last observation of the sequence
						if (isInStudiedState && j == sequence.getNumObservations() - 1) {
							time += subsequentObservation.getTimeValue() - pivotObservation.getTimeValue();
							// As it is the last observation of the sequence, it is moved the pivot
							// observation to the last one to move to the following sequence
							i = sequence.getNumObservations();
						}
						// If the subsequent observation has a different state, the time is computed and
						// the pivot observation will be the following one
						else {
							time += subsequentObservation.getTimeValue() - pivotObservation.getTimeValue();
							// Set pivot observation
							i = j + 1;
							// Exit the loop to change the pivot observation
							break;
						}
					}
			}
		return time;
	}

	/**
	 * Checks if it is possible to create a sequence with the provided data. It will
	 * be throw and exception if this is not the case.
	 * 
	 * @param data list of arrays that contains the data of the sequence
	 * @throws ErroneousSequenceException if a valid sequence cannot be created with
	 *                                    the provided data
	 */
	private void checkIntegrityData(List<String[]> data) throws ErroneousSequenceException {
		// Check content data. There should be at least two arrays, one for the names of
		// the variables and another for an observation
		if (data.size() < 3) {
			String message = "Sequences must contain, at least, two observations";
			throw new ErroneousSequenceException(message);
		}
		// Check names of variables
		List<String> nameVariablesSequence = Arrays.asList(data.get(0));
		if (!nameVariablesSequence.contains(this.nameTimeVariable)) {
			String message = String.format("Time variable '%s' not specified", this.nameTimeVariable);
			throw new ErroneousSequenceException(message);
		} else if (this.nameClassVariables != null && !nameVariablesSequence.containsAll(this.nameClassVariables)) {
			// There are no class variables if the dataset is for classification
			String message = "One or more specified class variables are not present in the sequence";
			throw new ErroneousSequenceException(message);
		} else if (getNumDataPoints() > 0 && (!nameVariablesSequence.containsAll(getNameAllVariables())
				|| !getNameAllVariables().containsAll(nameVariablesSequence))) {
			// If there is a sequence in the dataset, they must have the same variables
			String message = "Sequences cannot have different variables";
			throw new ErroneousSequenceException(message);
		}
	}

	/**
	 * Checks if an observation is in a specified {@code State}, i.e., if the values
	 * of the variables specified in the {@code State} object are the same as the
	 * values specified for those same variables in the {@code Observation} object.
	 * 
	 * @param observation observation to study
	 * @param state       state that is checked in the observation
	 * @return true if the observation has the specified state, false otherwise
	 */
	private boolean observationInState(Observation observation, State state) {
		List<String> nameVariables = state.getNameVariables();
		boolean ObservationHasState = true;
		for (String nameVariable : nameVariables) {
			ObservationHasState = ObservationHasState
					& observation.getValueVariable(nameVariable).equals(state.getValueVariable(nameVariable));
			// If one variable has a different state, the others are not checked
			if (!ObservationHasState)
				break;
		}
		return ObservationHasState;
	}
}