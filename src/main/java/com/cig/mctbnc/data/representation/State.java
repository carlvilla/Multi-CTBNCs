package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
	// Save the hashcode in order to avoid recomputing it
	private Integer hashcode;

	/**
	 * Default constructor.
	 */
	public State() {
		this.events = new LinkedHashMap<String, String>();
	}

	/**
	 * Create an State instance with some events.
	 * 
	 * @param events events to include to the State
	 */
	public State(Map<String, String> events) {
		this.events = new LinkedHashMap<String, String>(events);
	}

	/**
	 * Create an State instance with one event.
	 * 
	 * @param nameVariable  name of the variable
	 * @param valueVariable value of the variable
	 */
	public State(String nameVariable, String valueVariable) {
		this.events = new LinkedHashMap<String, String>();
		addEvent(nameVariable, valueVariable);
	}

	/**
	 * Constructor to clone states;
	 * 
	 * @param state
	 */
	public State(State state) {
		this.events = new LinkedHashMap<String, String>(state.events);
	}

	/**
	 * Add an event (a variable taking a certain value) to the state.
	 * 
	 * @param nameVariable  name of the variable
	 * @param valueVariable value of the variable
	 */
	public void addEvent(String nameVariable, String valueVariable) {
		this.events.put(nameVariable, valueVariable);
		initializeHashcode();
	}

	/**
	 * Add events to the state.
	 * 
	 * @param events events to add
	 */
	public void addEvents(Map<String, String> events) {
		for (Map.Entry<String, String> event : events.entrySet())
			addEvent(event.getKey(), event.getValue());
		initializeHashcode();
	}

	/**
	 * Given the name of a variables, its value is modified.
	 * 
	 * @param nameVariable name of the variable
	 * @param newValue     new value for the variable in this state
	 */
	public void modifyEventValue(String nameVariable, String newValue) {
		this.events.replace(nameVariable, newValue);
		initializeHashcode();
	}

	/**
	 * Remove specified event.
	 * 
	 * @param nameVariable name of the variable whose event is removed.
	 */
	public void removeEvents(String nameVariable) {
		this.events.keySet().remove(nameVariable);
	}

	/**
	 * Remove all events except those whose variable name is specified.
	 * 
	 * @param nameVariables names of the variables whose events should not be
	 *                      removed.
	 */
	public void removeAllEventsExcept(List<String> nameVariables) {
		this.events.keySet().removeIf(k -> !(nameVariables.contains(k)));
	}

	/**
	 * Return the events of the state.
	 * 
	 * @return Map with events
	 */
	public Map<String, String> getEvents() {
		return this.events;
	}

	/**
	 * Return number of events.
	 * 
	 * @return number of events
	 */
	public int getNumEvents() {
		return this.events.size();
	}

	/**
	 * Return the names of the variables collected by the State object.
	 * 
	 * @return names of the variables
	 */
	public List<String> getNameVariables() {
		return new ArrayList<>(this.events.keySet());
	}

	/**
	 * Return all the values in the State.
	 * 
	 * @return value of the specified node for this state
	 */
	public String[] getValues() {
		return this.events.values().toArray(String[]::new);
	}

	/**
	 * Return the value for a specific variable. Null is returned if the variable is
	 * not present.
	 * 
	 * @param nameVariable name of the variable
	 * @return value of the specified node for this state
	 */
	public String getValueVariable(String nameVariable) {
		return this.events.getOrDefault(nameVariable, null);
	}

	/**
	 * Return the values for some specific variables. Null is returned if the
	 * variable is not present.
	 * 
	 * @param nameVariables names of the variables
	 * @return values of the specified variables for this state
	 */
	public String[] getValueVariables(List<String> nameVariables) {
		String[] values = new String[nameVariables.size()];
		for (int i = 0; i < nameVariables.size(); i++)
			values[i] = getValueVariable(nameVariables.get(i));
		return values;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass())
			return false;
		// The object is of State type
		State otherState = (State) object;
		// Check if both maps has the same variables and values
		return this.events.equals(otherState.getEvents());
	}

	@Override
	public int hashCode() {
		if (this.hashcode == null)
			// The hashcode was not computed before
			this.hashcode = this.events.hashCode();
		return this.hashcode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String variableName : this.events.keySet()) {
			sb.append(variableName + " = " + this.events.get(variableName));
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Initialize the hashcode field. This method is used when the object is
	 * modified, so the hashcode needs to be recomputed.
	 */
	private void initializeHashcode() {
		this.hashcode = null;
	}

}
