package com.cig.mctbnc.data.representation;

import java.util.Objects;

/**
 * Represent the event where a node has a certain value.
 * 
 * @author carlosvillablanco
 *
 * @param <T>
 *            type used by the node
 */
public class Event<T> {

	private String nameNode;
	private T value;

	public Event(String nameNode, T value) {
		this.nameNode = nameNode;
		this.value = value;
	}

	public String getNameNode() {
		return nameNode;
	}

	public void setValue(T newValue) {
		this.value = newValue;
	}

	public T getValue() {
		return value;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (object == null || object.getClass() != this.getClass())
			return false;

		Event<T> otherEvent = (Event<T>) object;
		return getNameNode() == otherEvent.getNameNode() && value.equals(otherEvent.getValue());
	}

	@Override
	public int hashCode() {
		return Objects.hash(nameNode, value);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getNameNode() + " = " + getValue());
		return sb.toString();
	}

}
