package es.upm.fi.cig.multictbnc.exceptions;

import java.io.Serial;

/**
 * Thrown when a requested task is not implemented.
 *
 * @author Carlos Villa Blanco
 */
public class NotImplementedException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code NotImplementedException} with the specified detail message.
	 *
	 * @param msg detail message
	 */
	public NotImplementedException(String msg) {
		super(msg);
	}
}