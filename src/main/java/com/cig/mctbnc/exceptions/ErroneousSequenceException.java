package com.cig.mctbnc.exceptions;

/**
 * Thrown when a valid sequence could not be created with the provided data.
 * 
 * @author Carlos Villa Blanco
 */
public class ErroneousSequenceException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code ErroneousSequenceException} with the specified detail
	 * message.
	 * 
	 * @param msg
	 */
	public ErroneousSequenceException(String msg) {
		super("Sequence not added - " + msg);
	}
}
