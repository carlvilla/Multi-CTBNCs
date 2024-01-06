package es.upm.fi.cig.multictbnc.exceptions;

/**
 * Thrown when an expected variable is not found in a provided dataset.
 *
 * @author Carlos Villa Blanco
 */
public class VariableNotFoundException extends Exception {

	/**
	 * Constructs a {@code VariableNotFoundException} with no detail message.
	 */
	public VariableNotFoundException() {
		super();
	}

	/**
	 * Constructs a {@code VariableNotFoundException} with the specified detail message.
	 *
	 * @param str detail message
	 */
	public VariableNotFoundException(String str) {
		super(str);
	}
}