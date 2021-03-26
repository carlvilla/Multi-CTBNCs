package com.cig.mctbnc.gui.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.performance.ValidationMethod;

import javafx.concurrent.Task;

public class EvaluationTask extends Task<Void> {
	ValidationMethod validationMethod;
	MCTBNC<?, ?> model;
	Logger logger = LogManager.getLogger(EvaluationTask.class);

	public EvaluationTask(ValidationMethod validationMethod, MCTBNC<?, ?> model) {
		this.validationMethod = validationMethod;
		this.model = model;
	}

	@Override
	protected Void call() {
		// Evaluate the performance of the model
		// status.setText("Evaluating model...");
		validationMethod.evaluate(model);
		// status.setText("Idle");
		return null;
	}

}
