package es.upm.fi.cig.mctbnc.services;

import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.performance.ValidationMethod;
import es.upm.fi.cig.mctbnc.tasks.EvaluationTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 
 * Service that creates and manages an {@code EvaluationTask}.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class EvaluationService extends Service<Void> {
	ValidationMethod validationMethod;
	MCTBNC<?, ?> model;

	/**
	 * Initializes the {@code EvaluationService} by receiving a
	 * {@code ValidationMethod} and a {@code MCTBNC} model.
	 * 
	 * @param validationMethod validation method
	 * @param model            model to evaluate
	 */
	public void initializeService(ValidationMethod validationMethod, MCTBNC<?, ?> model) {
		this.validationMethod = validationMethod;
		this.model = model;
	}

	@Override
	protected Task<Void> createTask() {
		return new EvaluationTask(this.validationMethod, this.model);
	}

}