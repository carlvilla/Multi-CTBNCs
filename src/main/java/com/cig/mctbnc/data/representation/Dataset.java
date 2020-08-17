package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.exceptions.ErroneousSequenceException;
import com.cig.mctbnc.util.Util;

public class Dataset {
	private List<Sequence> sequences;
	private String nameTimeVariable;
	private List<String> nameFeatures;
	private List<String> nameClassVariables;
	private List<String> nameFiles;
	static Logger logger = LogManager.getLogger(Dataset.class);

	public Dataset(String nameTimeVariable, List<String> nameClassVariables) {
		sequences = new ArrayList<Sequence>();
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
	}

	public Dataset(List<Sequence> sequences) {
		this.sequences = sequences;
		nameFeatures = sequences.get(0).getFeatureNames();
		nameClassVariables = sequences.get(0).getClassVariablesNames();
		nameTimeVariable = sequences.get(0).getTimeVariableName();
	}

	/**
	 * Receive a list of Strings (a sequence), create a Sequence object and add it
	 * to the dataset. The first array of Strings has to contain the name of the
	 * variables.
	 * 
	 * @param data list of Strings (a sequence) where the first array contains the
	 *             name of the variables
	 */
	public void addSequence(List<String[]> data) {
		try {
			// Check if it is possible to add the sequence
			checkIntegrityData(data);
			// Obtain names of variables
			List<String> nameVariablesSequence = Arrays.asList(data.get(0));
			// If there are no sequences in the dataset, it is stored the name of the
			// features. They are given by the names of the variables that were not
			// defined as the time variable or class variable.
			if (getSequences().size() == 0) {
				nameFeatures = nameVariablesSequence.stream()
						.filter(name -> !name.equals(nameTimeVariable) && !nameClassVariables.contains(name))
						.collect(Collectors.toList());
			}
			// Drop names of variables
			data.remove(0);
			// Create and add sequence to the dataset
			Sequence sequence = new Sequence(nameVariablesSequence, nameTimeVariable, nameClassVariables, data);
			sequences.add(sequence);
		} catch (ErroneousSequenceException e) {
			logger.warn(e.getMessage());
		}
	}

	/**
	 * Those features with zero variance are removed from the dataset.
	 */
	public void removeZeroVarianceFeatures() {
		for (String nameFeature : nameFeatures) {
			if (getStatesVariable(nameFeature).size() == 1) {
				logger.warn("Features {} is removed since its variance is zero.", nameFeature);
				removeFeature(nameFeature);
			}
		}
	}

	/**
	 * Set the names of the files from which the data was extracted.
	 * 
	 * @param list
	 */
	public void setNameFiles(List<String> list) {
		this.nameFiles = list;
	}

	/**
	 * Remove a feature from the dataset.
	 * 
	 * @param nameFeature
	 */
	private void removeFeature(String nameFeature) {
		for (Sequence sequence : sequences) {
			sequence.removeFeature(nameFeature);
		}
	}

	/**
	 * Return the sequences of the dataset.
	 * 
	 * @return list with the sequences of the dataset
	 */
	public List<Sequence> getSequences() {
		return sequences;
	}

	public String getNameTimeVariable() {
		return nameTimeVariable;
	}

	public List<String> getNameFeatures() {
		return nameFeatures;
	}

	public List<String> getNameClassVariables() {
		return nameClassVariables;
	}

	/**
	 * Return the name of all the variables except the time variable.
	 * 
	 * @return name of all the variables except the time variable
	 */
	public List<String> getNameVariables() {
		List<String> nameVariables = new ArrayList<String>();
		nameVariables.addAll(getNameFeatures());
		nameVariables.addAll(getNameClassVariables());
		return nameVariables;
	}

	/**
	 * Return the name of all the variables, including the time variable.
	 * 
	 * @return name of all the variables
	 */
	public List<String> getNameAllVariables() {
		List<String> nameVariables = getNameVariables();
		nameVariables.add(getNameTimeVariable());
		return nameVariables;
	}

	public int getNumFeatures() {
		return nameFeatures.size();
	}

	public int getNumClassVariables() {
		return nameClassVariables.size();
	}

	/**
	 * Return the number of variables (without the variable for the time).
	 * 
	 * @return the number of variables
	 */
	public int getNumVariables() {
		return getNumClassVariables() + getNumFeatures();
	}

	/**
	 * Return the number of data points. In this case, this is the number of
	 * sequences.
	 * 
	 * @return number of sequences
	 */
	public int getNumDataPoints() {
		return getSequences().size();
	}

	/**
	 * Return the number of observations in the dataset, i.e., the number of
	 * transitions that occur in all the sequences.
	 * 
	 * @return number of observations
	 */
	public int getNumObservation() {
		return sequences.stream().mapToInt(sequence -> sequence.getNumObservations()).sum();
	}

	/**
	 * Get the values of the specified variables (by name) for all the sequences.
	 * 
	 * @param nameVaribles names of the variables
	 * @return bidimensional array with the values of the specified variables
	 */
	public String[][] getValuesVariables(String[] nameVaribles) {
		// If all the variables are class variables, then it is not necessary to
		// check the observations of each sequence
		boolean onlyClassVariable = getNameClassVariables().containsAll(Arrays.asList(nameVaribles));
		if (onlyClassVariable) {
			return getValuesClassVariables();
		}

		// TODO
		return null;
	}

	/**
	 * Get the values of the class variables for all the sequences.
	 * 
	 * @return bidimensional array with the values of the class variables
	 */
	public String[][] getValuesClassVariables() {
		String[][] valuesClassVariables = new String[getSequences().size()][getNumClassVariables()];
		for (int i = 0; i < getSequences().size(); i++)
			valuesClassVariables[i] = getSequences().get(i).getValuesClassVariables();
		return valuesClassVariables;
	}

	public List<State> getStatesVariable(String nameVariable) {
		Set<State> states = new HashSet<State>();
		for (Sequence sequence : getSequences()) {
			String[] statesSequence = sequence.getStates(nameVariable);
			for (int i = 0; i < statesSequence.length; i++) {
				// For every possible value of the variable it is created a State.
				State state = new State();
				// Event<String> event = new Event<String>(nameVariable, statesSequence[i]);
				state.addEvent(nameVariable, statesSequence[i]);
				states.add(state);
			}
		}
		return new ArrayList<State>(states);
	}

	/**
	 * Get all the possible states of the specified variables together. It is
	 * obtained all the combinations between the states of the variables.
	 * 
	 * @param nameVariables name of the variables whose possible combinations of
	 *                      states we want to know
	 * @return a list of as many objects State as possible combinations between the
	 *         specified variables
	 */
	public List<State> getStatesVariables(List<String> nameVariables) {
		if (nameVariables.size() == 1) {
			return getStatesVariable(nameVariables.get(0));
		}
		// Get all possible states for each variable
		List<List<State>> listStatesEachVariable = new ArrayList<List<State>>();
		for (String nameVariable : nameVariables) {
			List<State> statesVariable = getStatesVariable(nameVariable);
			listStatesEachVariable.add(statesVariable);
		}
		List<State> states = Util.cartesianProduct(listStatesEachVariable);
		return states;
	}

	/**
	 * Count the number of times in the dataset that the specified variables (in
	 * State object "query") take certain values.
	 * 
	 * @param query State object that specifies the variables and their values
	 * @return number of times specified values take a certain state together
	 */
	public int getNumOccurrences(State query) {
		int numOccurrences = 0;
		List<String> nameVariables = query.getNameVariables();
		// If all the variables are class variables, then it is not necessary to
		// check the observations of each sequence
		boolean onlyClassVariable = getNameClassVariables().containsAll(nameVariables);
		if (onlyClassVariable) {
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
		}
		// If there are features, it is necessary to study the observations
		else {
			throw new UnsupportedOperationException();
		}
		return numOccurrences;
	}

	/**
	 * Count the number of times in the dataset that a certain variable transitions
	 * from a certain state ("fromState") to another ("toState"), while its parents
	 * take a certain state ("fromState"). It is assumed that the studied variable
	 * is the first one of the State objects.
	 * 
	 * @param fromState give the original states of the variable and its parents
	 * @param toState   give the state of the variable after the transition
	 * @return number of times the transition occurs
	 */
	public int getNumOccurrencesTransition(State fromState, State toState) {
		String nameVariable = toState.getNameVariables().get(0);
		List<String> nameParents = null;
		if (fromState.getNumEvents() > 1) {
			// The variable has parents
			nameParents = fromState.getNameVariables().stream().filter(name -> !name.equals(nameVariable))
					.collect(Collectors.toList());
		}
		int numOccurrences = 0;
		for (Sequence sequence : getSequences()) {
			for (int i = 1; i < sequence.getNumObservations(); i++) {
				// It is obtained one observation representing the states of the variables
				// before and after the transition
				Observation observationBefore = sequence.getObservations().get(i - 1);
				Observation observationAfter = sequence.getObservations().get(i);

				// Check if the variable starts from the expected value
				boolean expectedValueBefore = observationBefore.getValueVariable(nameVariable)
						.equals(fromState.getValueNode(nameVariable));

				if (expectedValueBefore) {
					// Check if the studied variable transitions to the expected value
					boolean expectedValueAfter = observationAfter.getValueVariable(nameVariable)
							.equals(toState.getValueNode(nameVariable));

					// It is only necessary to check the states of the parents if the variable
					// transitions from and to the expected values
					if (expectedValueAfter && nameParents != null) {
						boolean expectedValueParents = true;
						for (String nameParent : nameParents) {
							expectedValueParents = expectedValueParents && observationBefore
									.getValueVariable(nameParent).equals(fromState.getValueNode(nameParent));
							if (!expectedValueParents)
								continue;
						}
						numOccurrences++;
					} else {
						if (expectedValueAfter)
							numOccurrences++;
					}
				}
			}
		}
		return numOccurrences;
	}

	/**
	 * Return how much time some variables stay in a certain state.
	 * 
	 * @param state state that contains the variables and the values to study
	 * @return time the state is maintained
	 */
	public double getTimeState(State state) {
		double time = 0;
		for (Sequence sequence : getSequences()) {
			for (int i = 0; i < sequence.getNumObservations(); i++) {
				// A pivot observation is created to check if it has the studied state and, in
				// that case, how many subsequent observations have the same state too
				Observation pivotObservation = sequence.getObservations().get(i);
				// Check that every variable has the expected value in the pivot observation
				boolean isInStudiedState = observationInState(pivotObservation, state);
				if (isInStudiedState) {
					for (int j = i + 1; j < sequence.getNumObservations(); j++) {
						// Check which subsequent observations have the studied state
						Observation subsequentObservation = sequence.getObservations().get(j);
						// Check that every variable has the expected value in the subsequent
						// observation
						isInStudiedState = observationInState(subsequentObservation, state);
						// If the subsequent observation has the studied state too, the time is computed
						// if it is the last observation of the sequence
						if (isInStudiedState) {
							if (j == sequence.getNumObservations() - 1) {
								time += subsequentObservation.getTimeValue() - pivotObservation.getTimeValue();
								// As it is the last observation of the sequence, it is moved the pivot
								// observation to the last one to move to the following sequence
								i = sequence.getNumObservations();
							}
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
			}
		}
		return time;
	}

	/**
	 * Get the names of the files from which the data was extracted.
	 * 
	 * @return names of the files
	 */
	public List<String> getNameFiles() {
		return nameFiles;
	}

	/**
	 * Check if it is possible to create a sequence with the specified data. It will
	 * be throw and exception if this is not the case.
	 * 
	 * @param data list of arrays that contains the data of the sequence
	 * @throws ErroneousSequenceException
	 */
	private void checkIntegrityData(List<String[]> data) throws ErroneousSequenceException {
		// Check content data. There should be at least two arrays, one for the names of
		// the variables and another for an observation
		if (data.size() < 2) {
			String message = "The sequence is empty";
			throw new ErroneousSequenceException(message);
		}
		// Check names of variables
		List<String> nameVariablesSequence = Arrays.asList(data.get(0));
		if (!nameVariablesSequence.contains(nameTimeVariable)) {
			String message = String.format("Time variable '%s' not specified", nameTimeVariable);
			throw new ErroneousSequenceException(message);
		} else if (!nameVariablesSequence.containsAll(nameClassVariables)) {
			String message = "One or more class variables were not specified";
			throw new ErroneousSequenceException(message);
		} else if (getNumDataPoints() > 0 && (!nameVariablesSequence.containsAll(getNameAllVariables())
				|| !getNameAllVariables().containsAll(nameVariablesSequence))) {
			// If there is a sequence in the dataset, they must have the same variables
			String message = "Sequences cannot have different variables";
			throw new ErroneousSequenceException(message);
		}
	}

	/**
	 * Check if an observation is in a specified state, i.e., if the values of the
	 * variables specified in the state object are the same as the values specified
	 * for those same variables in the observation object.
	 * 
	 * @param observation observation to study
	 * @param state       state that is checked in the observation
	 * @return boolean that determines if the observation has the specified state
	 */
	private boolean observationInState(Observation observation, State state) {
		List<String> nameVariables = state.getNameVariables();
		boolean ObservationHasState = true;
		for (String nameVariable : nameVariables) {
			ObservationHasState = ObservationHasState
					& observation.getValueVariable(nameVariable).equals(state.getValueNode(nameVariable));
			// If any variable has a different state, it is not necessary to check the
			// others
			if (!ObservationHasState) {
				break;
			}
		}
		return ObservationHasState;
	}

}
