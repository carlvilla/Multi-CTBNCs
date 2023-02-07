package es.upm.fi.cig.multictbnc.tasks;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task that allows executing the training of a model in a background thread. This prevents the UI from freezing.
 *
 * @author Carlos Villa Blanco
 */
public class TrainingTask extends Task<Void> {
	private final Logger logger = LogManager.getLogger(TrainingTask.class);
	private final MultiCTBNC<?, ?> model;
	private final DatasetReader datasetReader;

	/**
	 * Constructs a {@code TrainingTask} that receives an {@code MultiCTBNC} model and a {@code datasetReader}.
	 *
	 * @param model         model to train
	 * @param datasetReader dataset reader that provides the training sequences
	 */
	public TrainingTask(MultiCTBNC<?, ?> model, DatasetReader datasetReader) {
		this.model = model;
		this.datasetReader = datasetReader;
	}

	@Override
	protected Void call() throws Exception {
		// Read training dataset
		updateMessage("Reading training sequences from the dataset...");
		Dataset trainingDataset = this.datasetReader.readDataset();
		// Train the model
		updateMessage("Training model...");
		this.model.learn(trainingDataset);
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