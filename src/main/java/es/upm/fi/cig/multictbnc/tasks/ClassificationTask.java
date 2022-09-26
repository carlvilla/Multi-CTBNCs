package es.upm.fi.cig.multictbnc.tasks;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.util.Util;
import es.upm.fi.cig.multictbnc.writers.classification.TXTClassificationWriter;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task that allows executing the classification of sequences in a background thread. This prevents the UI from
 * freezing.
 *
 * @author Carlos Villa Blanco
 */
public class ClassificationTask extends Task<Void> {
	private final Logger logger = LogManager.getLogger(TrainingTask.class);
	private MultiCTBNC<?, ?> model;
	private DatasetReader datasetReader;
	private boolean estimateProbabilities;

	/**
	 * Constructs a {@code ClassificationTask} that receives an {@code MultiCTBNC} model and a {@code datasetReader}.
	 *
	 * @param model                 model used to perform the classification
	 * @param datasetReader         dataset reader that provides the sequences to classify
	 * @param estimateProbabilities true to estimate the probabilities of the predicted class configurations, false
	 *                              otherwise
	 */
	public ClassificationTask(MultiCTBNC<?, ?> model, DatasetReader datasetReader, boolean estimateProbabilities) {
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
		if (Util.isArrayEmpty(predictions))
			throw new ErroneousValueException("Any sequence of the test dataset could be predicted.");
		// Write predictions to a file
		String pathFolderFile = "results/classifications/";
		TXTClassificationWriter.writePredictions(predictions, dataset, pathFolderFile);
		return null;
	}

	@Override
	protected void succeeded() {
		updateMessage("Idle");
	}

	@Override
	protected void failed() {
		this.logger.error(getException().getMessage());
		String msg = "An error occurred while training the model. Check application log for details.";
		updateMessage("Idle - " + msg);
	}

}