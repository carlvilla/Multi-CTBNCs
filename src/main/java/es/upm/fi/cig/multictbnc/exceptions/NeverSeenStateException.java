package es.upm.fi.cig.multictbnc.exceptions;

import java.io.Serial;

/**
 * Thrown when a state was never seen before by the classifier.
 *
 * @author Carlos Villa Blanco
 */
public class NeverSeenStateException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code NeverSeenStateException} with the specified detail message.
	 *
	 * @param msg detail message
	 */
	public NeverSeenStateException(String msg) {
		super(msg);
	}

}