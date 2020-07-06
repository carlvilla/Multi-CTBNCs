package main.java.com.cig.mctbnc.models;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Carlos Villa <carlos.villa@upm.es>
 *
 * Defines an observation of a sequence.
 * 
 * @param <TimeType> type of the time interval (Integer = discrete time, Double = continuous time)
 */
public class Observation {
	
	private Map<String, String> mapValues;
	
	public Observation(String[] nameVariables, String[] nameClassVariables, String[] values) {
		mapValues = new HashMap<String, String>();
		for(int i=0;i<nameVariables.length;i++) {
			if(!Arrays.asList(nameClassVariables).contains(nameVariables[i])) {
				mapValues.put(nameVariables[i], values[i]);
			}	
		}	
	}
	
	public String[] getFeatureNames() {
		Set<String> keys = mapValues.keySet();
		return keys.toArray(new String[keys.size()]);
	}
	
	public String[] getValues() {
		Collection<String> values = mapValues.values();
		return values.toArray(new String[values.size()]);
	}	
	
	public String getValueFeature(String nodeName) {
		return mapValues.get(nodeName);
	}

}
