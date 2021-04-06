package com.cig.mctbnc.services;

import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.performance.ValidationMethod;
import com.cig.mctbnc.tasks.EvaluationTask;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 
 * Service that creates and manages an {@EvaluationTask}.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class EvaluationService extends Service<Void> {
	ValidationMethod validationMethod;
	MCTBNC<?, ?> model;

	/**
	 * Constructs a {@code EvaluationService} that receives a
	 * {@code ValidationMethod} and a {@code MCTBNC} model.
	 * 
	 * @param validationMethod
	 * @param model
	 */
	public EvaluationService(ValidationMethod validationMethod, MCTBNC<?, ?> model) {
		this.validationMethod = validationMethod;
		this.model = model;
	}

	@Override
	protected Task<Void> createTask() {
		return new EvaluationTask(this.validationMethod, this.model);
	}

}