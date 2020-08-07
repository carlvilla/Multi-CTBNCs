package com.cig.mctbnc.classification;

import com.cig.mctbnc.data.representation.Dataset;

/**
 * Interface representing classification models. Gives the necessary methods to
 * perform inference in a PGM.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface Classifier {

	/**
	 * Predict the state of the class variables given a dataset where is only known
	 * the state of the features.
	 * 
	 * @param dataset dataset used to make predictions
	 * @return predictions bidimensional string array with the predictions of the
	 *         class variables for all the instances of the dataset
	 */
	public String[][] predict(Dataset dataset);

}
