package es.upm.fi.cig.multictbnc.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import javafx.concurrent.Task;

/**
 * Task that allows to execute the training of a model in a background thread.
 * This prevents the UI from freezing.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class TrainingTask extends Task<Void> {
	MultiCTBNC<?, ?> model;
	DatasetReader datasetReader;
	Logger logger = LogManager.getLogger(TrainingTask.class);

	/**
	 * Constructs a {@code TrainingTask} that receives an {@code MultiCTBNC} model and a
	 * {@code datasetReader}.
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
		// Display learned model
		this.model.display();
		updateMessage("Idle");
	}

	@Override
	protected void failed() {
		String msg = "An error occurred while training the model";
		this.logger.error(msg);
		updateMessage("Idle - " + msg);
	}

}
