package es.upm.fi.cig.mctbnc.classification;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;

/**
 * Interface representing classification models. Gives the necessary methods to
 * perform inference in a probabilistic graphical model (PGM).
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface Classifier {

	/**
	 * Predict the values of the class variables for each instance of a dataset.
	 * 
	 * @param dataset               dataset used to make predictions
	 * @param estimateProbabilities determines if the probabilities of the classes
	 *                              are estimated
	 * @return predictions bidimensional string array with the predictions of the
	 *         class variables for all the instances of the dataset
	 */
	public Prediction[] predict(Dataset dataset, boolean estimateProbabilities);

}
