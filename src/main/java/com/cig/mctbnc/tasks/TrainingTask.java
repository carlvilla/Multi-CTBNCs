package com.cig.mctbnc.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.models.MCTBNC;

import javafx.concurrent.Task;

/**
 * Task that allows to execute the training of a model in a background thread.
 * This prevents the UI from freezing.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class TrainingTask extends Task<Void> {
	MCTBNC<?, ?> model;
	DatasetReader datasetReader;
	Logger logger = LogManager.getLogger(TrainingTask.class);

	/**
	 * Constructs a {@code TrainingTask} that receives an {@code MCTBNC} model and a
	 * {@code datasetReader}.
	 * 
	 * @param model         model to train
	 * @param datasetReader dataset reader that provides the training sequences
	 */
	public TrainingTask(MCTBNC<?, ?> model, DatasetReader datasetReader) {
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
}
