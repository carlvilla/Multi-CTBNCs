package main.java.com.cig.mctbnc.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dataset {
	
	private List<Sequence> sequences;
	private Map<String, Integer> indexVariables;
	private String[] nameFeatures;
	private String[] nameClassVariables;
	private String ;
	
	public Dataset(Map<String, Integer> indexVariables, List<Sequence> sequences) {
		this.sequences = sequences;
		this.indexVariables = indexVariables;
		nameFeatures = sequences.get(0).getFeatureNames();
		nameClassVariables = sequences.get(0).getClassVariablesNames();
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
		return getNumClassVariables() + getNumFeatures();
	}	
	
	/**
	 * Get the values of the class variables for all the sequences.
	 * @return
	 */
	public String[][] getValuesClassVariables(){
		String[][] valuesClassVariables = new String[sequences.size()][getNumClassVariables()];
		for(int i=0;i<sequences.size();i++)
			valuesClassVariables[i] = sequences.get(i).getValuesClassVariables();
		return valuesClassVariables;
	}
	
	public String[] getStatesVariable(String nameVariable) {
		Set<String> states = new HashSet<String>();
		for(Sequence sequence: sequences) {
			String[] statesSequence = sequence.getStates(nameVariable);
			for(String state:statesSequence) {
				states.add(state);
			}
		}
		return states.toArray(new String[states.size()]);
	}
	
	/**
	 * Return the possible combination of values of the specified variables
	 * @param nameVariable
	 * @return
	 */
	public String[][] getStatesVariables(String[] nameVariables) {
		Set<String[]> states = new HashSet<String[]>();
		for(Sequence sequence: sequences) {
			String[] statesSequence = sequence.getStates(nameVariables);
				states.add(statesSequence);
			}
		return states.toArray(new String[states.size()][nameVariables.length]);
	}
	
	/**
	 * Number of times the specified variables take certain values
	 * @param query specify the variables and their values
	 * @return
	 */
	public int getNumOccurrences(Map<String, String> query) {
		int numOccurrences = 0;
		String[] variableNames = query.keySet().toArray(new String[query.keySet().size()]);	
	
		// If all the variables are class variables, then it is not necessary to
		// check the observations of each sequence
		boolean onlyClassVariable = Arrays.asList(getNameClassVariables()).containsAll(Arrays.asList(variableNames));			
		if(onlyClassVariable) {
			for(Sequence sequence:sequences) {
				boolean occurrence = true;
				for(String variableName:variableNames)
					occurrence = query.get(variableName).equals(sequence.getValueClassVariable(variableName));
					if(!occurrence)
						continue;
				if(occurrence)
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
