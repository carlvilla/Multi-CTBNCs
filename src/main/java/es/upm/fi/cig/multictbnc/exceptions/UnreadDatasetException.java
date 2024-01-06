package es.upm.fi.cig.multictbnc.exceptions;

/**
 * Thrown when the provided dataset could not be read.
 *
 * @author Carlos Villa Blanco
 */
public class UnreadDatasetException extends Exception {

	/**
	 * Constructs a {@code UnreadDatasetException} with the specified detail message.
	 *
	 * @param msg detail message
	 */
	public UnreadDatasetException(String msg) {
		super("Dataset was not created - " + msg);
	}

}