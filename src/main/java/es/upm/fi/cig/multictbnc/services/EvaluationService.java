package es.upm.fi.cig.multictbnc.services;

import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.tasks.EvaluationTask;
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
	MultiCTBNC<?, ?> model;

	/**
	 * Initializes the {@code EvaluationService} by receiving a
	 * {@code ValidationMethod} and a {@code MultiCTBNC} model.
	 * 
	 * @param validationMethod validation method
	 * @param model            model to evaluate
	 */
	public void initializeService(ValidationMethod validationMethod, MultiCTBNC<?, ?> model) {
		this.validationMethod = validationMethod;
		this.model = model;
	}

	@Override
	protected Task<Void> createTask() {
		return new EvaluationTask(this.validationMethod, this.model);
	}

}