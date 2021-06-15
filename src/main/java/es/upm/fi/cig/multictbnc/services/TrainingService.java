package es.upm.fi.cig.multictbnc.services;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.tasks.TrainingTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 
 * Service that creates and manages a {@code TrainingTask}.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class TrainingService extends Service<Void> {
	MultiCTBNC<?, ?> model;
	DatasetReader datasetReader;

	/**
	 * Initializes the {@code TrainingService} receiving the model to learn and the
	 * {@code a DatasetReader} to read the training dataset.
	 * 
	 * @param model         model to train
	 * @param datasetReader {@code a DatasetReader} to read the training dataset
	 */
	public void initializeService(MultiCTBNC<?, ?> model, DatasetReader datasetReader) {
		this.model = model;
		this.datasetReader = datasetReader;
	}

	@Override
	protected Task<Void> createTask() {
		return new TrainingTask(this.model, this.datasetReader);
	}

}