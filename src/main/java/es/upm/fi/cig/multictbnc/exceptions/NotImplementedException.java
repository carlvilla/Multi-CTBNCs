package es.upm.fi.cig.multictbnc.exceptions;

/**
 * Thrown when a requested task is not implemented.
 *
 * @author Carlos Villa Blanco
 */
public class NotImplementedException extends Exception {

	/**
	 * Constructs a {@code NotImplementedException} with the specified detail message.
	 *
	 * @param msg detail message
	 */
	public NotImplementedException(String msg) {
		super(msg);
	}
}