package com.cig.mctbnc.exceptions;

/**
 * Exception used when an expected variable is not found in a dataset file.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class VariableNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public VariableNotFoundException() {
	}

	public VariableNotFoundException(String str) {
		super(str);
	}
}
