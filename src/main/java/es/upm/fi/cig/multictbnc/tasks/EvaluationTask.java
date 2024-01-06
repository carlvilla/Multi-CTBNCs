package es.upm.fi.cig.multictbnc.tasks;

import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task that allows executing the training and evaluation of a model in a background thread. This prevents the UI from
 * freezing.
 *
 * @author Carlos Villa Blanco
 */
public class EvaluationTask extends Task<Void> {
	private final Logger logger = LogManager.getLogger(EvaluationTask.class);
	private ValidationMethod validationMethod;
	private MultiCTBNC<?, ?> model;

	/**
	 * Constructs an {@code EvaluationTask} that receives a {@code ValidationMethod} and an {@code MultiCTBNC} model.
	 *
	 * @param validationMethod validation method used to evaluate the provided model
	 * @param model            model to train and evaluate
	 */
	public EvaluationTask(ValidationMethod validationMethod, MultiCTBNC<?, ?> model) {
		this.validationMethod = validationMethod;
		this.model = model;
	}

	@Override
	protected Void call() throws Exception {
		updateMessage("Training and evaluating model...");
		// Evaluate the performance of the model
		this.validationMethod.evaluate(this.model);
		return null;
	}

	@Override
	protected void succeeded() {
		// Display learnt model
		try {
			this.model.display();
		} catch (Exception e) {
			this.logger.error(e.getMessage());
		}
		updateMessage("Idle");
	}

	@Override
	protected void failed() {
		this.logger.error(getException().getMessage());
		String msg = "An error occurred while training the model. Check application log for details.";
		updateMessage("Idle - " + msg);
	}

}