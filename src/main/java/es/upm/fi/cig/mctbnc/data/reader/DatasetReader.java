package es.upm.fi.cig.mctbnc.data.reader;

import java.util.List;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.exceptions.UnreadDatasetException;

/**
 * Interface for classes that read datasets.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface DatasetReader {

	/**
	 * Receives the names of the time variable and feature variables of a dataset.
	 * This method can be used, for example, when reading datasets to be classified.
	 * 
	 * @param nameTimeVariable name of the time variable
	 * @param nameFeatures     name of the feature variables
	 */
	public void setVariables(String nameTimeVariable, List<String> nameFeatures);

	/**
	 * Receives the names of the time variable, feature variables and class
	 * variables of a dataset. This method can be used, for example, when read
	 * training datasets.
	 * 
	 * @param nameTimeVariable   name of the time variable
	 * @param nameClassVariables names of the class variables
	 * @param nameFeatures       names of the feature variables
	 */
	public void setVariables(String nameTimeVariable, List<String> nameClassVariables, List<String> nameFeatures);

	/**
	 * Returns a dataset.
	 * 
	 * @return a {@code Dataset}
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public Dataset readDataset() throws UnreadDatasetException;

	/**
	 * Returns the name of the time variable.
	 * 
	 * @return name of the time variable.
	 */
	public String getNameTimeVariable();

	/**
	 * Returns the name of the class variables.
	 * 
	 * @return name of the class variables.
	 */
	public List<String> getNameClassVariables();

	/**
	 * Returns the name of the features.
	 * 
	 * @return name of the features.
	 */
	public List<String> getNameFeatures();

	/**
	 * Returns the names of all the variables of the dataset, included those are are
	 * not used.
	 * 
	 * @return names of the variables
	 */
	public List<String> getNameVariables();

	/**
	 * Defines a previously read dataset as out-of-date, so it should be reloaded.
	 * 
	 * @param outdated <code>true</code> to set dataset as out-of-date;
	 *                 <code>false</code> otherwise.
	 */
	public void setDatasetAsOutdated(boolean outdated);

	/**
	 * Indicates if the dataset is out-of-date.
	 * 
	 * @return <code>true</code> if dataset is out-of-date; <code>false</code>
	 *         otherwise.
	 */
	public boolean isDatasetOutdated();

}
