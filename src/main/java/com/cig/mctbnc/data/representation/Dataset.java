package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			if (sequences.size() == 0) {
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
	 * @return  the number of variables
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
		return sequences.size();
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
		String[][] valuesClassVariables = new String[sequences.size()][getNumClassVariables()];
		for (int i = 0; i < sequences.size(); i++)
			valuesClassVariables[i] = sequences.get(i).getValuesClassVariables();
		return valuesClassVariables;
	}

	public List<State> getStatesVariable(String nameVariable) {
		Set<State> states = new HashSet<State>();
		for (Sequence sequence : sequences) {
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
	 * @param nameVariable
	 * @return
	 */
	public List<State> getStatesVariables(List<String> nameVariables) {
		// It is used a set of lists in order to not have unique combinations
		// of values for the studied variables
		Set<State> states = new HashSet<State>();
		for (Sequence sequence : sequences) {
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
	 * Number of times the specified variables take certain values
	 * 
	 * @param query
	 *            specify the variables and their values
	 * @return
	 */
	public int getNumOccurrences(State query) {
		int numOccurrences = 0;
		String[] nameVariables = query.getNameVariables();

		// If all the variables are class variables, then it is not necessary to
		// check the observations of each sequence
		boolean onlyClassVariable = Arrays.asList(getNameClassVariables()).containsAll(Arrays.asList(nameVariables));

		if (onlyClassVariable) {
			for (Sequence sequence : sequences) {
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
