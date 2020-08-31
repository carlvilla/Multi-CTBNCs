package com.cig.mctbnc.data.reader;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;

/**
 * Interface for classes that read datasets.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface DatasetReader {

	/**
	 * Define the time variable, the class variables and the variables to ignore.
	 * 
	 * @param nameTimeVariable
	 * @param nameClassVariables
	 * @param excludeVariables
	 */
	public void setVariables(String nameTimeVariable, List<String> nameClassVariables, List<String> excludeVariables);

	/**
	 * Return a dataset.
	 * 
	 * @return dataset
	 */
	public Dataset readDataset();

	/**
	 * Return the names of the files from which the dataset was read. Some files
	 * could be rejected due to problems in its content.
	 * 
	 * @return names accepted files
	 */
	List<String> getAcceptedFiles();

	/**
	 * Return the names of all the variables of the dataset, included those are are
	 * not used.
	 * 
	 * @return names of the variables
	 */
	List<String> getAllVariablesDataset();

}
