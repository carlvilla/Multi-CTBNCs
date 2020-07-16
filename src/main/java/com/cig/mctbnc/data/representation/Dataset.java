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

public class Dataset {

	private List<Sequence> sequences;
	private Map<String, Integer> indexVariables;
	private String[] nameFeatures;
	private String[] nameClassVariables;
	private String nameTimeVariable;

	static Logger logger = LogManager.getLogger(Dataset.class);

	public Dataset(String nameTimeVariable, String[] nameClassVariables) {
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
	 */
	public void addSequence(List<String[]> data) {
		try {
			String[] nameVariables = data.get(0);
			// If there are no sequences in the dataset, it is stored the name of the
			// features. They are given by the names of the variables that were not
			// defined as the time variable or class variable.
			if (getSequences().size() == 0) {
				nameFeatures = Arrays.asList(nameVariables).stream().filter(
						name -> !name.equals(nameTimeVariable) && !Arrays.asList(nameClassVariables).contains(name))
						.toArray(String[]::new);

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

	/**
	 * Return the sequences of the dataset.
	 * 
	 * @return list with the sequences of the dataset
	 */
	public List<Sequence> getSequences() {
		return sequences;
	}

	/**
	 * Check if the data to add to the dataset is correct. It will be throw and
	 * exception is this is not the case.
	 * 
	 * @param nameVariables
	 * @return
	 * @throws Exception
	 */
	public boolean checkIntegrityData(String[] nameVariables) throws Exception {
		boolean dataCorrect;
		List<String> listNameVariables = Arrays.asList(nameVariables);

		dataCorrect = listNameVariables.contains(nameTimeVariable);
		if (!dataCorrect) {
			// logger.warn("Sequence not added - Time variable '{}' not specified",
			// nameTimeVariable);
			String message = String.format("Sequence not added - Time variable '%s' not specified", nameTimeVariable);
			throw new Exception(message);
		}

		dataCorrect = listNameVariables.containsAll(Arrays.asList(nameClassVariables));
		if (!dataCorrect) {
			// logger.warn("Sequence not added - One or more class variables were not
			// specified");
			String message = "Sequence not added - One or more class variables were not specified";
			throw new Exception(message);
		}

		if (!listNameVariables.containsAll(indexVariables.keySet())) {
			// logger.warn("Sequence not added - Sequences cannot have different
			// variables");
			String message = "Sequence not added - Sequences cannot have different variables";
			throw new Exception(message);
		}

		return dataCorrect;
	}

	public String[] getNameFeatures() {
		return nameFeatures;
	}

	public String[] getNameClassVariables() {
		return nameClassVariables;
	}

	public String getNameTimeVariable() {
		return nameTimeVariable;
	}

	/**
	 * Return the name of all the variables (without the variable for the time).
	 * 
	 * @return the name of all the variables
	 */
	public String[] getNameVariables() {
		return indexVariables.keySet().stream().filter(name -> !name.equals(nameTimeVariable)).toArray(String[]::new);
	}

	/**
	 * Return the number of variables (without the variable for the time).
	 * 
	 * @return the number of variables
	 */
	public int getNumVariables() {
		return getNumClassVariables() + getNumFeatures();
	}

	public int getNumFeatures() {
		return nameFeatures.length;
	}

	public int getNumClassVariables() {
		return nameClassVariables.length;
	}

	/**
	 * Return the number of data points. In this case, this is the number of
	 * sequences.
	 * 
	 * @return
	 */
	public int getNumDataPoints() {
		return getSequences().size();
	}

	/**
	 * Get the values of the specified variables (by name) for all the sequences.
	 * 
	 * @param nameVaribles
	 * @return
	 */
	public String[][] getValuesVariables(String[] nameVaribles) {
		// If all the variables are class variables, then it is not necessary to
		// check the observations of each sequence
		boolean onlyClassVariable = Arrays.asList(getNameClassVariables()).containsAll(Arrays.asList(nameVaribles));
		if (onlyClassVariable) {
			return getValuesClassVariables();
		}

		// TODO
		return null;
	}

	/**
	 * Get the values of the class variables for all the sequences.
	 * 
	 * @return
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
				Event<String> event = new Event<String>(nameVariable, statesSequence[i]);
				state.addEvent(event);
				states.add(state);
			}
		}
		return new ArrayList<State>(states);
	}

	/**
	 * Return the possible combination of values of the specified variables
	 * 
	 * @param nameVariables
	 * @return
	 */
	public List<State> getStatesVariables(List<String> nameVariables) {
		// It is used a set of lists in order to not have unique combinations
		// of values for the studied variables
		Set<State> states = new HashSet<State>();
		for (Sequence sequence : getSequences()) {
			List<String> statesSequence = sequence.getStates(nameVariables);
			State state = new State();
			for (int i = 0; i < nameVariables.size(); i++) {
				Event<String> event = new Event<String>(nameVariables.get(i), statesSequence.get(i));
				state.addEvent(event);
			}
			states.add(state);
		}
		return new ArrayList<State>(states);
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
		String[] nameVariables = query.getNameVariables();
		// If all the variables are class variables, then it is not necessary to
		// check the observations of each sequence
		boolean onlyClassVariable = Arrays.asList(getNameClassVariables()).containsAll(Arrays.asList(nameVariables));
		if (onlyClassVariable) {
			for (Sequence sequence : getSequences()) {
				boolean occurrence = true;
				for (Event<String> event : query.getEvents()) {
					String nameVariable = event.getNameNode();
					occurrence = event.getValue().equals(sequence.getValueClassVariable(nameVariable));
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
	 * 
	 * Count the number of times in the dataset that a certain variable transitions
	 * from a certain state ("fromState") to another ("toState"), while its parents
	 * take a certain state ("fromState").
	 * 
	 * @param fromState
	 *            give the original states of the variable and its parents
	 * @param toState
	 *            give the state of the variable after the transition
	 * @return number of times the transition occurs
	 */
	public int getNumOccurrencesTransition(State fromState, State toState) {
		String nameVariable = toState.getNameVariables()[0];
		String[] nameParents = null;
		if (fromState.getNumEvents() > 1) {
			// The variable has parents
			nameParents = Arrays.asList(fromState.getNameVariables()).stream()
					.filter(name -> !name.equals(nameVariable)).toArray(String[]::new);
		}
		int numOccurrences = 0;
		for (Sequence sequence : getSequences()) {
			for (int i = 1; i < sequence.getNumObservations(); i++) {
				// It is obtained one observation representing the states of the variables
				// before and after the transition
				Observation observationBefore = sequence.getObservations().get(i - 1);
				Observation observationAfter = sequence.getObservations().get(i);

				// Check if the variable starts from the expected value
				boolean expectedValueBefore = observationBefore.getValueFeature(nameVariable)
						.equals(toState.getValueNode(nameVariable));

				// Check if the studied variable transitions to the expected value
				boolean expectedValueAfter = observationAfter.getValueFeature(nameVariable)
						.equals(toState.getValueNode(nameVariable));

				// It is only neccesary to check the states of the parents if the variable
				// transitions from and to the expected values
				if (expectedValueBefore && expectedValueAfter && nameParents != null) {
					boolean expectedValueParents = true;
					for (String nameParent : nameParents) {
						expectedValueParents = expectedValueParents && observationBefore.getValueFeature(nameParent)
								.equals(fromState.getValueNode(nameParent));
					}
					if (expectedValueParents)
						numOccurrences++;
				} else {
					if (expectedValueBefore && expectedValueAfter)
						numOccurrences++;
				}
			}
		}
		return numOccurrences;
	}

	public int getIndexVariable(String nameVariable) {
		return indexVariables.get(nameVariable);
	}

	public Map<String, Integer> getIndexVariables() {
		return indexVariables;
	}

	/**
	 * Create an index for the variables whose names are given.
	 * 
	 * @param nameVariables
	 */
	private void addIndex(String[] nameVariables) {
		for (String nameVariable : nameVariables)
			addIndex(nameVariable);
	}

	/**
	 * Create an index for the the variable whose name is given. This index starts
	 * from 0 and its maximum value is equal to the number of variables. This is
	 * necessary for the adjacency matrices so it is always use the same rows and
	 * columns to refer to a certain variable.
	 * 
	 * @param nameVariable
	 */
	private void addIndex(String nameVariable) {
		if (!indexVariables.containsKey(nameVariable)) {
			// Get next index
			int index = 0;
			if (indexVariables.size() != 0)
				index = indexVariables.size();
			indexVariables.put(nameVariable, index);
		}
	}

}
