package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.util.Util;

public class Dataset {

	private List<Sequence> sequences;
	private Map<String, Integer> indexVariables;
	private List<String> nameFeatures;
	private List<String> nameClassVariables;
	private String nameTimeVariable;
	static Logger logger = LogManager.getLogger(Dataset.class);

	public Dataset(String nameTimeVariable, List<String> nameClassVariables) {
		sequences = new ArrayList<Sequence>();
		indexVariables = new HashMap<String, Integer>();
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
		// Create index for time variable and class variables
		addIndex(nameTimeVariable);
		addIndex(nameClassVariables);
	}

	public Dataset(Map<String, Integer> indexVariables, List<Sequence> sequences) {
		this.sequences = sequences;
		this.indexVariables = indexVariables;
		nameFeatures = sequences.get(0).getFeatureNames();
		nameClassVariables = sequences.get(0).getClassVariablesNames();
		nameTimeVariable = sequences.get(0).getTimeVariableName();
	}

	/**
	 * Receive a list of Strings (a sequence), create a Sequence object and add it
	 * to the dataset. The first array of Strings has to contain the name of the
	 * variables.
	 * 
	 * @param data
	 *            list of Strings (a sequence) where the first array contains the
	 *            name of the variables
	 */
	public void addSequence(List<String[]> data) {
		try {
			List<String> nameVariables = Arrays.asList(data.get(0));
			// If there are no sequences in the dataset, it is stored the name of the
			// features. They are given by the names of the variables that were not
			// defined as the time variable or class variable.
			if (getSequences().size() == 0) {
				nameFeatures = nameVariables.stream()
						.filter(name -> !name.equals(nameTimeVariable) && !nameClassVariables.contains(name))
						.collect(Collectors.toList());

				// Definition of the indexes for the features
				addIndex(nameFeatures);
			}
			// Check if it is possible to add the sequence
			checkIntegrityData(nameVariables);
			// Drop names of variables
			data.remove(0);
			// Create and add sequence to dataset
			Sequence sequence = new Sequence(nameVariables, nameTimeVariable, nameClassVariables, data);
			sequences.add(sequence);
		} catch (IndexOutOfBoundsException e) {
			logger.warn("Sequence not added - The sequence is empty");
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
	}

	/*
	 * 
	 * 
	 * public void addSequence(List<List<String>> data, String[] excludeVariables) {
	 * 
	 * List<String> nameVariables = data.get(0); List<Integer>
	 * variableIndexesToExclude = new ArrayList<Integer>();
	 * 
	 * for(String excludeVariable: excludeVariables) { int index =
	 * nameVariables.indexOf(excludeVariable); if(index == -1) { logger.
	 * warn("Trying to exclude variable {}, but it does not exist in the dataset");
	 * } else { // The name of the variable is removed nameVariables.remove(index);
	 * // The data.stream().map(row -> row.remove(index));
	 * 
	 * 
	 * } }
	 * 
	 * addSequence(data); }
	 * 
	 */

	/**
	 * Return the sequences of the dataset.
	 * 
	 * @return list with the sequences of the dataset
	 */
	public List<Sequence> getSequences() {
		return sequences;
	}

	/**
	 * Check if it is possible to add the specified variables to the dataset. It
	 * will be throw and exception is this is not the case.
	 * 
	 * @param nameVariables
	 *            names of the variables
	 * @return boolean that determines if the addition is valid
	 * @throws Exception
	 */
	public boolean checkIntegrityData(List<String> nameVariables) throws Exception {
		boolean dataCorrect;
		dataCorrect = nameVariables.contains(nameTimeVariable);
		if (!dataCorrect) {
			// logger.warn("Sequence not added - Time variable '{}' not specified",
			// nameTimeVariable);
			String message = String.format("Sequence not added - Time variable '%s' not specified", nameTimeVariable);
			throw new Exception(message);
		}

		dataCorrect = nameVariables.containsAll(nameClassVariables);
		if (!dataCorrect) {
			// logger.warn("Sequence not added - One or more class variables were not
			// specified");
			String message = "Sequence not added - One or more class variables were not specified";
			throw new Exception(message);
		}

		if (!nameVariables.containsAll(indexVariables.keySet())) {
			// logger.warn("Sequence not added - Sequences cannot have different
			// variables");
			String message = "Sequence not added - Sequences cannot have different variables";
			throw new Exception(message);
		}
		return dataCorrect;
	}

	public int getIndexVariable(String nameVariable) {
		return indexVariables.get(nameVariable);
	}

	public Map<String, Integer> getIndexVariables() {
		return indexVariables;
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
	 * Return the name of all the variables (without the variable for the time).
	 * 
	 * @return the name of all the variables
	 */
	public List<String> getNameVariables() {
		return indexVariables.keySet().stream().filter(name -> !name.equals(nameTimeVariable))
				.collect(Collectors.toList());
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
	 * @param nameVaribles
	 *            names of the variables
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
	 * @param nameVariables
	 *            name of the variables whose possible combinations of states we
	 *            want to know
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
	 * @param query
	 *            State object that specifies the variables and their values
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
	 * @param fromState
	 *            give the original states of the variable and its parents
	 * @param toState
	 *            give the state of the variable after the transition
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
	 * @param state
	 *            state that contains the variables and the values to study
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
	 * Check if an observation is in a specified state, i.e., if the values of the
	 * variables specified in the state object are the same as the values specified
	 * for those same variables in the observation object.
	 * 
	 * @param observation
	 *            observation to study
	 * @param state
	 *            state that is checked in the observation
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

	/**
	 * Create an index for the the variable whose name is given. This index starts
	 * from 0 and its maximum value is equal to the number of variables. This is
	 * necessary for the adjacency matrices so it is always use the same rows and
	 * columns to refer to a certain variable. Therefore, the only variable that is
	 * not added is the time variable.
	 * 
	 * @param nameVariable
	 */
	private void addIndex(String nameVariable) {
		if (!indexVariables.containsKey(nameVariable) && !nameVariable.equals(nameTimeVariable)) {
			// Get next index
			int index = 0;
			if (indexVariables.size() != 0)
				index = indexVariables.size();
			indexVariables.put(nameVariable, index);
		}
	}

	/**
	 * Create an index for the variables whose names are given (except for the time
	 * variable).
	 * 
	 * @param nameVariables
	 */
	private void addIndex(List<String> nameVariables) {
		for (String nameVariable : nameVariables)
			if (!nameVariable.equals(nameTimeVariable))
				addIndex(nameVariable);
	}

}
