package com.cig.mctbnc.data.reader;

import java.io.FileNotFoundException;
import java.util.List;

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
	 * @param datasetReader
	 * @param pathDataset
	 * @param sizeSequence
	 * @throws FileNotFoundException
	 */
	public static DatasetReader getDatasetReader(String datasetReader, String pathDataset, int sizeSequence)
			throws FileNotFoundException {
		switch (datasetReader) {
		case ("Multiple CSV"):
			return new MultipleCSVReader(pathDataset);
		default:
			// Unique CSV
			return new SingleCSVReader(pathDataset, sizeSequence);
		}
	}

	/**
	 * Return the name of available dataset readers.
	 * 
	 * @return
	 */
	public static List<String> getAvailableDatasetReaders() {
		return List.of("Multiple CSV", "Single CSV");
	}

	/**
	 * Return the name of available strategies for the extraction of sequences.
	 * 
	 * @return available strategies
	 */
	public static List<String> getAvailableStrategies() {
		return List.of("Fixed size");
	}

}
