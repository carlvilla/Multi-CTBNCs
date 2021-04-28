package com.cig.mctbnc.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.writers.classification.TxtClassificationWriter;

import javafx.concurrent.Task;

/**
 * Task that allows to execute the classification of sequences in a background
 * thread. This prevents the UI from freezing.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ClassificationTask extends Task<Void> {
	MCTBNC<?, ?> model;
	DatasetReader datasetReader;
	boolean estimateProbabilities;
	Logger logger = LogManager.getLogger(TrainingTask.class);

	/**
	 * Constructs a {@code ClassificationTask} that receives an {@code MCTBNC} model
	 * and a {@code datasetReader}.
	 * 
	 * @param model                 model used to perform the classification
	 * @param datasetReader         dataset reader that provides the sequences to
	 *                              classify
	 * @param estimateProbabilities boolean that determines if the probabilities of
	 *                              the predicted class configurations should be
	 *                              estimated
	 */
	public ClassificationTask(MCTBNC<?, ?> model, DatasetReader datasetReader, boolean estimateProbabilities) {
		this.model = model;
		this.datasetReader = datasetReader;
		this.estimateProbabilities = estimateProbabilities;
	}

	@Override
	protected Void call() throws Exception {
		updateMessage("Reading sequences to classify from the dataset...");
		// Read dataset to classify
		Dataset dataset = this.datasetReader.readDataset();
		updateMessage("Classifying sequences...");
		// Evaluate the performance of the model
		Prediction[] predictions = this.model.predict(dataset, this.estimateProbabilities);
		// Write predictions to a file
		String pathFolderFile = "results/classifications/";
		TxtClassificationWriter.writePredictions(predictions, dataset, pathFolderFile);
		return null;
	}

	@Override
	protected void succeeded() {
		updateMessage("Idle");
	}

}
