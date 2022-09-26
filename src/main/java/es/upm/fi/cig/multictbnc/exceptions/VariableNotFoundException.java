package es.upm.fi.cig.multictbnc.exceptions;

import java.io.Serial;

/**
 * Thrown when an expected variable is not found in a provided dataset.
 *
 * @author Carlos Villa Blanco
 */
public class VariableNotFoundException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

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