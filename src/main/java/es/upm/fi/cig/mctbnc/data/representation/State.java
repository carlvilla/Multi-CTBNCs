package es.upm.fi.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Represents the state of certain nodes/variables (events) by keeping their
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
	 * Creates an State instance with some events.
	 * 
	 * @param events events to include to the State
	 */
	public State(Map<String, String> events) {
		this.events = new LinkedHashMap<String, String>(events);
	}

	/**
	 * Creates an State instance with one event.
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
	 * @param state {@code State} to clone
	 */
	public State(State state) {
		this.events = new LinkedHashMap<String, String>(state.events);
	}

	/**
	 * Adds an event (a variable taking a certain value) to the state.
	 * 
	 * @param nameVariable  name of the variable
	 * @param valueVariable value of the variable
	 */
	public void addEvent(String nameVariable, String valueVariable) {
		this.events.put(nameVariable, valueVariable);
		initializeHashcode();
	}

	/**
	 * Adds events to the state.
	 * 
	 * @param events events to add
	 */
	public void addEvents(Map<String, String> events) {
		for (Map.Entry<String, String> event : events.entrySet())
			addEvent(event.getKey(), event.getValue());
		initializeHashcode();
	}

	/**
	 * Modifies the value of a given variable.
	 * 
	 * @param nameVariable name of the variable
	 * @param newValue     new value for the variable in this state
	 */
	public void modifyEventValue(String nameVariable, String newValue) {
		this.events.replace(nameVariable, newValue);
		initializeHashcode();
	}

	/**
	 * Removes specified event.
	 * 
	 * @param nameVariable name of the variable whose event is removed.
	 */
	public void removeEvents(String nameVariable) {
		this.events.keySet().remove(nameVariable);
	}

	/**
	 * Removes all events except those whose variable name is specified.
	 * 
	 * @param nameVariables names of the variables whose events should not be
	 *                      removed.
	 */
	public void removeAllEventsExcept(List<String> nameVariables) {
		this.events.keySet().removeIf(k -> !(nameVariables.contains(k)));
	}

	/**
	 * Returns the events of the state.
	 * 
	 * @return Map with events
	 */
	public Map<String, String> getEvents() {
		return this.events;
	}

	/**
	 * Returns number of events.
	 * 
	 * @return number of events
	 */
	public int getNumEvents() {
		return this.events.size();
	}

	/**
	 * Returns the names of the variables collected by the {@code State} object.
	 * 
	 * @return names of the variables
	 */
	public List<String> getNameVariables() {
		return new ArrayList<>(this.events.keySet());
	}

	/**
	 * Returns all the values in the State.
	 * 
	 * @return value of the specified node for this state
	 */
	public String[] getValues() {
		return this.events.values().toArray(String[]::new);
	}

	/**
	 * Returns the value for a specific variable. Null is returned if the variable is
	 * not present.
	 * 
	 * @param nameVariable name of the variable
	 * @return value of the specified node for this state
	 */
	public String getValueVariable(String nameVariable) {
		return this.events.getOrDefault(nameVariable, null);
	}

	/**
	 * Returns the values for some specific variables. Null is returned if the
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
		return this.events.toString();
	}

	/**
	 * Initializes the hashcode field. This method is used when the object is
	 * modified, so the hashcode needs to be recomputed.
	 */
	private void initializeHashcode() {
		this.hashcode = null;
	}

}
