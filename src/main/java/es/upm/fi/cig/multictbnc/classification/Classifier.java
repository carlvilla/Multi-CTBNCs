package es.upm.fi.cig.multictbnc.classification;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;

/**
 * Interface representing classification models. It gives the necessary methods to perform inference in a probabilistic
 * graphical model (PGM).
 *
 * @author Carlos Villa Blanco
 */
public interface Classifier {

	/**
	 * Predicts the values of the class variables for each instance of a dataset.
	 *
	 * @param dataset               dataset used to make predictions
	 * @param estimateProbabilities true to estimate the probabilities of the class configurations, false otherwise
	 * @return two-dimensional {@code String} array with the predictions of the class variables for all the
	 * instances of
	 * the dataset
	 */
	Prediction[] predict(Dataset dataset, boolean estimateProbabilities);

}