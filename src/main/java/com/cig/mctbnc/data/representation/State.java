package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Represent the state of certain nodes (events) by keeping their names and
 * values. A certain state can only contain one event for each variable.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class State {

	private Map<String, String> events;

	public State() {
		events = new HashMap<String, String>();
	}

	public State(Map<String, String> events) {
		this.events = new HashMap<String, String>();
		addEvents(events);
	}

	public void addEvent(String nameVariable, String valueVariable) {
		this.events.put(nameVariable, valueVariable);

	}

	public void addEvents(Map<String, String> events) {
		for (Map.Entry<String, String> event : events.entrySet()) {
			addEvent(event.getKey(), event.getValue());
		}
	}

	/**
	 * Given the name of a node, it is modified its value.
	 * 
	 * @param nameNode
	 *            name of the node
	 * @param newValue
	 *            new value for the node in this state
	 */
	public void modifyEventValue(String nameNode, String newValue) {
		events.replace(nameNode, newValue);
	}

	public Map<String, String> getEvents() {
		return events;
	}

	public int getNumEvents() {
		return events.size();
	}

	public List<String> getNameVariables() {
		return new ArrayList<>(events.keySet());
	}

	/**
	 * Return the value for a specified node.
	 * 
	 * @param nameNode
	 * @return value of the specified node for this state
	 */
	public String getValueNode(String nameNode) {
		return events.get(nameNode);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (object == null || object.getClass() != this.getClass())
			return false;

		State otherState = (State) object;
		Map<String, String> eventsOther = otherState.getEvents();

		return events.equals(eventsOther);
	}

	@Override
	public int hashCode() {
		return events.hashCode();
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
