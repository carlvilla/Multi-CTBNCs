package com.cig.mctbnc.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.performance.ValidationMethod;

import javafx.concurrent.Task;

/**
 * Task that allows to execute the training and evaluation of a model in a
 * background thread. This prevents the UI from freezing.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class EvaluationTask extends Task<Void> {
	ValidationMethod validationMethod;
	MCTBNC<?, ?> model;
	Logger logger = LogManager.getLogger(EvaluationTask.class);

	/**
	 * Constructs a {@code MultipleCSVReader} that receives a
	 * {@code ValidationMethod} and a {@code MCTBNC} model.
	 * 
	 * @param validationMethod
	 * @param model
	 */
	public EvaluationTask(ValidationMethod validationMethod, MCTBNC<?, ?> model) {
		this.validationMethod = validationMethod;
		this.model = model;
	}

	@Override
	protected Void call() {
		updateMessage("Training and evaluating model...");
		// Evaluate the performance of the model
		try {
			this.validationMethod.evaluate(this.model);
		} catch (UnreadDatasetException e) {
			this.logger.error("Dataset could not be read");
		}
		return null;
	}

	@Override
	protected void succeeded() {
		// Display learned model
		this.model.display();
		updateMessage("Idle");
	}

}
