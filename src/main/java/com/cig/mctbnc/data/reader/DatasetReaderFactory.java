package com.cig.mctbnc.data.reader;

import java.io.FileNotFoundException;

/**
 * Creates dataset readers.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class DatasetReaderFactory {

	/**
	 * Generates the correct dataset reader for the given dataset path.
	 * 
	 * @param path
	 * @return dataset reader
	 * @throws FileNotFoundException
	 */
	public static DatasetReader getDatasetReader(String pathDataset) throws FileNotFoundException {
		// TODO new dataset readers and define how to determine which one to generate
		return new SeparateCSVReader(pathDataset);
	}

}
