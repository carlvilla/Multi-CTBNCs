package es.upm.fi.cig.mctbnc.services;

import es.upm.fi.cig.mctbnc.data.reader.DatasetReader;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.tasks.ClassificationTask;
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
	MCTBNC<?, ?> model;
	DatasetReader datasetReader;
	boolean estimateProbabilities;

	/**
	 * Initializes the {@code ClassificationService} by receiving the learned
	 * {@code MCTBNC} model and a {@code DatasetReader} to read the dataset to
	 * classify.
	 * 
	 * @param model                 {@code MCTBNC} model
	 * @param datasetReader         {@code a DatasetReader} to read the dataset to
	 *                              classify
	 * @param estimateProbabilities true to estimate the probabilities of the class
	 *                              configurations, false otherwise
	 */
	public void initializeService(MCTBNC<?, ?> model, DatasetReader datasetReader, boolean estimateProbabilities) {
		this.model = model;
		this.datasetReader = datasetReader;
		this.estimateProbabilities = estimateProbabilities;
	}

	@Override
	protected Task<Void> createTask() {
		return new ClassificationTask(this.model, this.datasetReader, this.estimateProbabilities);
	}

}