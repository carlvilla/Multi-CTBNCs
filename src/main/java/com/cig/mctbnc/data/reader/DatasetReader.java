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

}
