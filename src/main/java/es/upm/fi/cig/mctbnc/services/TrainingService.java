package es.upm.fi.cig.mctbnc.services;

import es.upm.fi.cig.mctbnc.data.reader.DatasetReader;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.tasks.TrainingTask;
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
	MCTBNC<?, ?> model;
	DatasetReader datasetReader;

	/**
	 * Constructs a {@code TrainingService} that receives a {@code MCTBNC} model.
	 * 
	 * @param model         model to train
	 * @param datasetReader {@code a DatasetReader} to read the training dataset
	 */
	public TrainingService(MCTBNC<?, ?> model, DatasetReader datasetReader) {
		this.model = model;
		this.datasetReader = datasetReader;
	}

	@Override
	protected Task<Void> createTask() {
		return new TrainingTask(this.model, this.datasetReader);
	}

}