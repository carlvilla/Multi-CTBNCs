package es.upm.fi.cig.multictbnc.data.representation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the state of certain nodes/variables (events) by keeping their names and values. A certain state can only
 * contain one event for each variable.
 *
 * @author Carlos Villa Blanco
 */
public class State {
	private Map<String, String> events;
	// Save the hashcode to avoid recomputing it
	private Integer hashcode;

	/**
	 * Default constructor.
	 */
	public State() {
		this.events = new LinkedHashMap<>();
	}

	/**
	 * Creates a State instance with some events.
	 *
	 * @param events events to include to the State
	 */
	public State(Map<String, String> events) {
		this.events = new LinkedHashMap<>(events);
	}

	/**
	 * Creates a State instance with one event.
	 *
	 * @param nameVariable  name of the variable
	 * @param valueVariable value of the variable
	 */
	public State(String nameVariable, String valueVariable) {
		this.events = new LinkedHashMap<>();
		addEvent(nameVariable, valueVariable);
	}

	/**
	 * Constructor to clone states;
	 *
	 * @param state {@code State} to clone
	 */
	public State(State state) {
		this.events = new LinkedHashMap<>(state.events);
	}

	/**
	 * Adds an event (a variable taking a certain value) to the state.
	 *
	 * @param nameVariable  name of the variable
	 * @param valueVariable value of the variable
	 */
	public void addEvent(String nameVariable, String valueVariable) {
		this.events.put(nameVariable, valueVariable);
		initialiseHashcode();
	}

	/**
	 * Adds events to the state.
	 *
	 * @param events events to add
	 */
	public void addEvents(Map<String, String> events) {
		for (Map.Entry<String, String> event : events.entrySet())
			addEvent(event.getKey(), event.getValue());
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
	 * Returns the names of the variables collected by the {@code State} object.
	 *
	 * @return names of the variables
	 */
	public List<String> getNameVariables() {
		return new ArrayList<>(this.events.keySet());
	}

	/**
	 * Returns the number of events.
	 *
	 * @return number of events
	 */
	public int getNumEvents() {
		return this.events.size();
	}

	/**
	 * Returns the value for a specific variable. Null is returned if the variable is not present.
	 *
	 * @param nameVariable name of the variable
	 * @return value of the specified node for this state
	 */
	public String getValueVariable(String nameVariable) {
		return this.events.getOrDefault(nameVariable, null);
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
	 * Modifies the value of a given variable.
	 *
	 * @param nameVariable name of the variable
	 * @param newValue     new value for the variable in this state
	 */
	public void modifyEventValue(String nameVariable, String newValue) {
		this.events.replace(nameVariable, newValue);
		initialiseHashcode();
	}

	@Override
	public int hashCode() {
		if (this.hashcode == null)
			// The hashcode was not computed before
			this.hashcode = this.events.hashCode();
		return this.hashcode;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass())
			return false;
		// The object is of State type
		State otherState = (State) object;
		// Check if both maps have the same variables and values
		return this.events.equals(otherState.getEvents());
	}

	@Override
	public String toString() {
		return this.events.toString();
	}

	/**
	 * Initialises the hashcode field. This method is used when the object is modified, so the hashcode needs to be
	 * recomputed.
	 */
	private void initialiseHashcode() {
		this.hashcode = null;
	}

}