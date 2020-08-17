package com.cig.mctbnc.data.reader;

import com.cig.mctbnc.data.representation.Dataset;

/**
 * Interface for classes that read datasets.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface DatasetReader {

	/**
	 * Return a dataset.
	 * 
	 * @return dataset
	 */
	public Dataset readDataset();

	/**
	 * Generate a training and a testing dataset.
	 * 
	 * @param trainingSize
	 * @param shuffle      determines if the data is shuffled before splitting into
	 *                     training and testing
	 */
	public void generateTrainAndTest(double trainingSize, boolean shuffle);

	/**
	 * Return training dataset.
	 * 
	 * @return training dataset
	 */
	public Dataset getTraining();

	/**
	 * Return testing dataset.
	 * 
	 * @return testing dataset
	 */
	public Dataset getTesting();

}
