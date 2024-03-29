package es.upm.fi.cig.multictbnc.exceptions;

/**
 * Thrown when an error occurs due to an incorrect value provided by the user.
 *
 * @author Carlos Villa Blanco
 */
public class ErroneousValueException extends Exception {

	/**
	 * Constructs a {@code ErroneousValue} with the specified detail message.
	 *
	 * @param msg detail message
	 */
	public ErroneousValueException(String msg) {
		super(msg);
	}
}