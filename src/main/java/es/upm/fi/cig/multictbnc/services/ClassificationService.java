package es.upm.fi.cig.multictbnc.services;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.tasks.ClassificationTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 
 * Service that creates and manages a {@code ClassificaionTask}.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ClassificationService extends Service<Void> {
	MultiCTBNC<?, ?> model;
	DatasetReader datasetReader;
	boolean estimateProbabilities;

	/**
	 * Initializes the {@code ClassificationService} by receiving the learned
	 * {@code MultiCTBNC} model and a {@code DatasetReader} to read the dataset to
	 * classify.
	 * 
	 * @param model                 {@code MultiCTBNC} model
	 * @param datasetReader         {@code a DatasetReader} to read the dataset to
	 *                              classify
	 * @param estimateProbabilities true to estimate the probabilities of the class
	 *                              configurations, false otherwise
	 */
	public void initializeService(MultiCTBNC<?, ?> model, DatasetReader datasetReader, boolean estimateProbabilities) {
		this.model = model;
		this.datasetReader = datasetReader;
		this.estimateProbabilities = estimateProbabilities;
	}

	@Override
	protected Task<Void> createTask() {
		return new ClassificationTask(this.model, this.datasetReader, this.estimateProbabilities);
	}

}
