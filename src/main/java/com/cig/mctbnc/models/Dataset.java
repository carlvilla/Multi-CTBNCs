package main.java.com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dataset {

	private List<Sequence> sequences;
	private Map<String, Integer> indexVariables;
	private String[] nameFeatures;
	private String[] nameClassVariables;
	private String nameTimeVariable;

	public Dataset(Map<String, Integer> indexVariables, List<Sequence> sequences) {
		this.sequences = sequences;
		this.indexVariables = indexVariables;
		nameFeatures = sequences.get(0).getFeatureNames();
		nameClassVariables = sequences.get(0).getClassVariablesNames();
		nameTimeVariable = sequences.get(0).getTimeVariableName();
	}

	public String[] getNameFeatures() {
		return nameFeatures;
	}

	public String[] getNameClassVariables() {
		return nameClassVariables;
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
	 * Return the number of data points. In this case, this is the number of sequences. 
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
				for (Event event : query.getEvents()) {
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
