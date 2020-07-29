package com.cig.mctbnc.data.reader;

import com.cig.mctbnc.data.representation.Dataset;

public interface DatasetReader {

	/**
	 * Return a dataset.
	 * 
	 * @return dataset
	 */
	public Dataset readDataset();

}
