package com.cig.mctbnc.data.representation;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Represent the state of certain nodes (events) by keeping their names and
 * values. A certain state can only contain one event for each variable.
 * 
 * @author Carlos Villa (carlos.villa@upm.es)
 *
 */
public class State {

	private List<Event<String>> events;

	public State() {
		events = new ArrayList<Event<String>>();
	}

	public State(List<Event<String>> events) {
		// It is important not to pass the list of events as a reference. This is due to
		// the fact that State objects may be modified to query the dataset. Otherwise,
		// different State objects could reference to the same Event objects and provoke
		// unwanted results if they are modified
		this.events = new ArrayList<Event<String>>();
		for (Event<String> event : events) {
			Event<String> copyEvent = new Event<String>(event.getNameNode(), event.getValue());
			addEvent(copyEvent);
		}
	}

	public void addEvent(Event<String> event) {
		events.add(event);
	}

	public void addEvents(List<Event<String>> events) {
		for (Event<String> event : events)
			this.events.add(event);
	}

	/**
	 * Given the name of a node, it is modified its value in its respective event.
	 * 
	 * @param name
	 * @param k
	 */
	public void modifyEventValue(String nameNode, String newValue) {
		getEventNode(nameNode).setValue(newValue);
	}

	public List<Event<String>> getEvents() {
		return events;
	}

	public int getNumEvents() {
		return events.size();
	}

	public String[] getNameVariables() {
		String[] nameVariables = new String[getNumEvents()];
		for (int i = 0; i < getNumEvents(); i++) {
			nameVariables[i] = events.get(i).getNameNode();
		}
		return nameVariables;
	}

	/**
	 * Given the name of a node, it is returned its respective event.
	 * 
	 * @param nameNode
	 * @return
	 */
	public Event<String> getEventNode(String nameNode) {
		// There should be only one event for the specified variable
		return events.stream().filter(event -> event.getNameNode().equals(nameNode)).findFirst().get();
	}

	/**
	 * Return the value for a specified node.
	 * 
	 * @param nameNode
	 * @return
	 */
	public String getValueNode(String nameNode) {
		return getEventNode(nameNode).getValue();
	}

	@Override
	public boolean equals(Object object) {

		if (this == object)
			return true;

		if (object == null || object.getClass() != this.getClass())
			return false;

		State otherState = (State) object;

		boolean areEqual = true;
		List<Event<String>> eventsOther = otherState.getEvents();
		if (events.size() == eventsOther.size())
			for (int i = 0; i < events.size(); i++) {
				areEqual = areEqual & events.get(i).equals(eventsOther.get(i));
				if (!areEqual)
					break;
			}

		return areEqual;

	}

	@Override
	public int hashCode() {
		return events.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Event<String> event : events) {
			sb.append(event.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

}
