package com.cig.mctbnc.services;

import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.tasks.ClassificationTask;

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
	 * Constructs a {@code TrainingService} that receives a {@code MCTBNC} model.
	 * 
	 * @param model
	 * @param datasetReader         {@code a DatasetReader} to read the training
	 *                              dataset
	 * @param estimateProbabilities
	 */
	public ClassificationService(MCTBNC<?, ?> model, DatasetReader datasetReader, boolean estimateProbabilities) {
		this.model = model;
		this.datasetReader = datasetReader;
		this.estimateProbabilities = estimateProbabilities;
	}

	@Override
	protected Task<Void> createTask() {
		return new ClassificationTask(this.model, this.datasetReader, this.estimateProbabilities);
	}

}
