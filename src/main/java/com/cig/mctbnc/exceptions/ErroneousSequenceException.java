package com.cig.mctbnc.exceptions;

public class ErroneousSequenceException extends Exception {

	private static final long serialVersionUID = 1L;

	public ErroneousSequenceException(String msg) {
		super("Sequence not added - " + msg);
	}
}
