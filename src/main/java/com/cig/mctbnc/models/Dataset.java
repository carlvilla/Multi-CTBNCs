package main.java.com.cig.mctbnc.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dataset {
	
	private List<Sequence> sequences;
	private String[] nameFeatures;
	private String[] nameClassVariables;
	
	public Dataset(List<Sequence> sequences) {
		this.sequences = sequences;
		nameFeatures = sequences.get(0).getFeatureNames();
		nameClassVariables = sequences.get(0).getClassVariablesNames();
	}
	
	public String[] getNameFeatures() {
		return nameFeatures;
	}
	
	public String[] getNameClassVariables() {
		return nameClassVariables;
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

}
