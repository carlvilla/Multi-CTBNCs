package es.upm.fi.cig.mctbnc.exceptions;

/**
 * 
 * Thrown when the provided dataset could not be read.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class UnreadDatasetException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code UnreadDatasetException} with the specified detail
	 * message.
	 * 
	 * @param msg detail message
	 */
	public UnreadDatasetException(String msg) {
		super("Dataset was not created - " + msg);
	}
}
