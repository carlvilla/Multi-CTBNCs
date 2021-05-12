package es.upm.fi.cig.mctbnc.exceptions;

/**
 * Thrown when an expected variable is not found in a provided dataset.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class VariableNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code VariableNotFoundException} with no detail message.
	 */
	public VariableNotFoundException() {
		super();
	}

	/**
	 * Constructs a {@code VariableNotFoundException} with the specified detail
	 * message.
	 * 
	 * @param str detail message
	 */
	public VariableNotFoundException(String str) {
		super(str);
	}
}
