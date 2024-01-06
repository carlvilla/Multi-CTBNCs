package es.upm.fi.cig.multictbnc.exceptions;

/**
 * Thrown when a valid sequence could not be created with the provided data.
 *
 * @author Carlos Villa Blanco
 */
public class ErroneousSequenceException extends Exception {

	/**
	 * Constructs a {@code ErroneousSequenceException} with the specified detail message.
	 *
	 * @param msg detail message
	 */
	public ErroneousSequenceException(String msg) {
		super(msg);
	}
}