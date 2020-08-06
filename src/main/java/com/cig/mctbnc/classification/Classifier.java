package com.cig.mctbnc.classification;

import com.cig.mctbnc.data.representation.Dataset;

/**
 * Interface representing classification models.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface Classifier {

	/**
	 * Predict the class variables of the instances of a dataset
	 * 
	 * @param dataset dataset used to make predictions
	 * @return predictions
	 */
	public String[][] predict(Dataset dataset);

}
