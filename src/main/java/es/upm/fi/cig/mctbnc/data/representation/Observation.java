package es.upm.fi.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines an observation of a sequence.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Observation {
	// Store the value of the time variable when the observation occurred
	private double timeValue;
	// Store the value of all the variables (except the time variable)
	private Map<String, String> variablesValues;

	/**
	 * Creates an observation given the name of the variables and time variable, and
	 * their values.
	 * 
	 * @param nameVariables    name of the variables
	 * @param nameTimeVariable name of the time variable
	 * @param values           values of all the variables
	 */
	public Observation(List<String> nameVariables, String nameTimeVariable, String[] values) {
		// Store name of the variable and its value for the observation
		this.variablesValues = new LinkedHashMap<String, String>();
		for (int i = 0; i < nameVariables.size(); i++) {
			if (!nameVariables.get(i).equals(nameTimeVariable))
				this.variablesValues.put(nameVariables.get(i), values[i]);
			else
				this.timeValue = Double.parseDouble(values[i]);
		}
	}

	/**
	 * Creates an observation given the value of the time variable separately.
	 * 
	 * @param nameVariables name of the variables
	 * @param values        values of all the variables (except time variable)
	 * @param timeValue     value of the time variable
	 */
	public Observation(List<String> nameVariables, String[] values, double timeValue) {
		// Store name of the variable and its value for the observation
		this.variablesValues = new LinkedHashMap<String, String>();
		for (int i = 0; i < nameVariables.size(); i++)
			this.variablesValues.put(nameVariables.get(i), values[i]);
		// Store time value
		this.timeValue = timeValue;
	}

	/**
	 * Removes the value of a feature from the observation.
	 * 
	 * @param nameFeature name of the feature variable
	 */
	public void removeFeatures(String nameFeature) {
		this.variablesValues.remove(nameFeature);
	}

	/**
	 * Returns the name of the variables in the observation (except the time
	 * variable).
	 * 
	 * @return name of the variables
	 */
	public List<String> getVariableNames() {
		Set<String> keys = this.variablesValues.keySet();
		return new ArrayList<String>(keys);
	}

	/**
	 * Returns the value of all the variables (except the time variable) for the
	 * observation.
	 * 
	 * @return values of the variables
	 */
	public String[] getValues() {
		Collection<String> values = this.variablesValues.values();
		return values.toArray(new String[values.size()]);
	}

	/**
	 * Returns the value of the time variable for the observation.
	 * 
	 * @return value of the time variable
	 */
	public double getTimeValue() {
		return this.timeValue;
	}

	/**
	 * Given the name of a variable, returns its value for the observation.
	 * 
	 * @param nameVariable name of the variable
	 * @return value of the variable for the observation
	 */
	public String getValueVariable(String nameVariable) {
		return this.variablesValues.get(nameVariable);
	}

	/**
	 * Given the names of variables, returns their value for the observation.
	 * 
	 * @param nameVariables names of the variable
	 * @return values of the variables for the observation
	 */
	public String[] getValueVariables(List<String> nameVariables) {
		String[] values = new String[nameVariables.size()];
		for (int i = 0; i < nameVariables.size(); i++)
			values[i] = getValueVariable(nameVariables.get(i));
		return values;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Time|");
		for (String nameVariable : this.variablesValues.keySet())
			sb.append(nameVariable + "|");
		sb.append("\n");
		sb.append(this.timeValue + "|");
		for (String valueVariable : this.variablesValues.values())
			sb.append(valueVariable + "|");
		sb.append("\n");
		return sb.toString();
	}

}
