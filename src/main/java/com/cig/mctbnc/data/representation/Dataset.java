package com.cig.mctbnc.data.representation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
		String[] nameVariables = data.get(0);

		// If there are no sequences in the dataset, it is stored the name of the
		// features. They are given by the names of the variables that were not
		// defined as the time variable or class variable.
		if (sequences.size() == 0) {
			nameFeatures = Arrays.asList(nameVariables).stream()
					.filter(name -> !name.equals(nameTimeVariable) && !Arrays.asList(nameClassVariables).contains(name))
					.toArray(String[]::new);

			// Definition of the indexes of each variable
			String[] allVariables = Stream.of(nameFeatures, nameClassVariables, nameTimeVariable).flatMap(Stream::of)
					.toArray(String[]::new);
			for (String nameVariable : allVariables) {
				indexVariables.put(nameVariable, Arrays.asList(allVariables).indexOf(nameVariable));
			}
		}

		try {
			// Check if it is possible to add the sequence
			checkIntegrityData(nameVariables);
			// Drop names of variables
			data.remove(0);
			// Create and add sequence to dataset
			Sequence sequence = new Sequence(nameVariables, nameTimeVariable, nameClassVariables, data);
			sequences.add(sequence);
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
	}

	/**
	 * Check if the data to add to the dataset is correct
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
			logger.warn("Sequence not added - Time variable '{}' not specified", nameTimeVariable);
			throw new Exception("Sequence not added - Time variable not specified");
		}

		dataCorrect = listNameVariables.contains(nameClassVariables);
		if (!dataCorrect) {
			logger.warn("Sequence not added - One or more class variables were not specified");
			throw new Exception("Sequence not added - One or more class variables were not specified");
		}

		if (!indexVariables.containsKey(listNameVariables)) {
			logger.warn("Sequence not added - Sequences cannot have different variables");
			throw new Exception("Sequences not added - Sequences cannot have different variables");
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

	public int getNumFeatures() {
		return nameFeatures.length;
	}

	public int getNumClassVariables() {
		return nameClassVariables.length;
	}

	public int getNumVariables() {
		// + 1 to include time variable
		return getNumClassVariables() + getNumFeatures() + 1;
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
}
