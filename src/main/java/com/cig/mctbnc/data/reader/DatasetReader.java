package com.cig.mctbnc.data.reader;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.exceptions.UnreadDatasetException;

/**
 * Interface for classes that read datasets.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface DatasetReader {

	/**
	 * Define the time variable.
	 * 
	 * @param nameTimeVariable
	 */
	public void setTimeVariable(String nameTimeVariable);

	/**
	 * Define the time variable, the class variables and the features.
	 * 
	 * @param nameTimeVariable
	 * @param nameClassVariables
	 * @param nameFeatures
	 */
	public void setVariables(String nameTimeVariable, List<String> nameClassVariables, List<String> nameFeatures);

	/**
	 * Return a dataset.
	 * 
	 * @return dataset
	 * @throws UnreadDatasetException
	 */
	public Dataset readDataset() throws UnreadDatasetException;

	/**
	 * Return the name of the time variable.
	 * 
	 * @return name of the time variable.
	 */
	public String getNameTimeVariable();

	/**
	 * Return the name of the class variables.
	 * 
	 * @return name of the class variables.
	 */
	public List<String> getNameClassVariables();
	
	/**
	 * Return the name of the features.
	 * 
	 * @return name of the features.
	 */
	public List<String> getNameFeatures();

	/**
	 * Return the names of all the variables of the dataset, included those are are
	 * not used.
	 * 
	 * @return names of the variables
	 */
	public List<String> getNameVariables();

	/**
	 * Define a previously read dataset as out-of-date, so it should be reloaded.
	 * 
	 * @param outdated <code>true</code> to set dataset as out-of-date;
	 *                 <code>false</code> otherwise.
	 */
	public void setDatasetAsOutdated(boolean outdated);

	/**
	 * Indicate if the dataset is out-of-date.
	 * 
	 * @return <code>true</code> if dataset is out-of-date; <code>false</code>
	 *         otherwise.
	 */
	public boolean isDatasetOutdated();

}
