package es.upm.fi.cig.multictbnc.exceptions;

import java.io.Serial;

/**
 * Thrown when a valid sequence could not be created with the provided data.
 *
 * @author Carlos Villa Blanco
 */
public class ErroneousSequenceException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code ErroneousSequenceException} with the specified detail message.
	 *
	 * @param msg detail message
	 */
	public ErroneousSequenceException(String msg) {
		super(msg);
	}
}