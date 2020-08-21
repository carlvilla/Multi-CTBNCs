package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Represent the state of certain nodes/variables (events) by keeping their
 * names and values. A certain state can only contain one event for each
 * variable.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class State {

	private Map<String, String> events;

	/**
	 * Default constructor.
	 */
	public State() {
		events = new HashMap<String, String>();
	}

	/**
	 * Create an State instance with some events.
	 * 
	 * @param events events to include to the State
	 */
	public State(Map<String, String> events) {
		this.events = new HashMap<String, String>();
		addEvents(events);
	}

	/**
	 * Add an event (a variable taking a certain value) to the state.
	 * 
	 * @param nameVariable  name of the variable
	 * @param valueVariable value of the variable
	 */
	public void addEvent(String nameVariable, String valueVariable) {
		this.events.put(nameVariable, valueVariable);

	}

	/**
	 * Add events to the state.
	 * 
	 * @param events events to add
	 */
	public void addEvents(Map<String, String> events) {
		for (Map.Entry<String, String> event : events.entrySet()) {
			addEvent(event.getKey(), event.getValue());
		}
	}

	/**
	 * Given the name of a variables, its value is modified.
	 * 
	 * @param nameVariable name of the variable
	 * @param newValue     new value for the variable in this state
	 */
	public void modifyEventValue(String nameVariable, String newValue) {
		events.replace(nameVariable, newValue);
	}

	/**
	 * Return the events of the state.
	 * 
	 * @return Map with events
	 */
	public Map<String, String> getEvents() {
		return events;
	}

	/**
	 * Return number of events.
	 * 
	 * @return number of events
	 */
	public int getNumEvents() {
		return events.size();
	}

	/**
	 * Return the names of the variables collected by the State object.
	 * 
	 * @return names of the variables
	 */
	public List<String> getNameVariables() {
		return new ArrayList<>(events.keySet());
	}

	/**
	 * Return all the values in the State.
	 * 
	 * @return value of the specified node for this state
	 */
	public String[] getValues() {
		return events.values().toArray(String[]::new);
	}

	/**
	 * Return the value for a specific variable.
	 * 
	 * @param nameVariable name of the variable
	 * @return value of the specified node for this state
	 */
	public String getValueVariable(String nameVariable) {
		return events.get(nameVariable);
	}

	/**
	 * Return the values for some specific variables.
	 * 
	 * @param nameVariables names of the variables
	 * @return values of the specified variables for this state
	 */
	public String[] getValueVariables(List<String> nameVariables) {
		String[] values = new String[nameVariables.size()];
		for (int i = 0; i < nameVariables.size(); i++)
			values[i] = events.get(nameVariables.get(i));
		return values;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass())
			return false;
		// The object is of State type
		State otherState = (State) object;
		// Extract the events of the other State object
		Map<String, String> eventsOther = otherState.getEvents();
		// If there is a different number of events, the states cannot be equal
		if (events.size() != eventsOther.size())
			return false;
		// Check if both maps has the same variables and values
		for (Map.Entry<String, String> entry : events.entrySet()) {
			String valueOther = eventsOther.get(entry.getKey());
			if (valueOther == null || !entry.getValue().equals(valueOther))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hashcode = 1;
		for (Map.Entry<String, String> entry : events.entrySet()) {
			hashcode *= (prime + entry.getKey().hashCode() + entry.getValue().hashCode());
		}
		return hashcode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String variableName : events.keySet()) {
			sb.append(variableName + " = " + events.get(variableName));
			sb.append("\n");
		}
		return sb.toString();
	}

}
