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
	 * @param nameNode name of the node
	 * @param newValue new value for the node in this state
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

	/**
	 * Return the values for some specified nodes.
	 * 
	 * @param nameNodes names of the nodes
	 * @return values of the specified nodes for this state
	 */
	public String[] getValueNodes(List<String> nameNodes) {
		String[] values = new String[nameNodes.size()];
		for (int i = 0; i < nameNodes.size(); i++)
			values[i] = events.get(nameNodes.get(i));
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
