package com.cig.mctbnc.data.reader;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;

/**
 * Common attributes and methods for dataset readers.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class AbstractDatasetReader implements DatasetReader {
	String datasetFolder;
	List<String> nameAcceptedFiles;
	String nameTimeVariable;
	List<String> nameClassVariables;
	List<String> excludeVariables;
	Dataset trainingDataset;
	Dataset testingDataset;
	static Logger logger = LogManager.getLogger(AbstractDatasetReader.class);

	public AbstractDatasetReader(String datasetFolder, String nameTimeVariable, List<String> nameClassVariables,
			List<String> excludeVariables) {
		this.datasetFolder = datasetFolder;
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
		this.excludeVariables = excludeVariables;
	}

	@Override
	public List<String> getAcceptedFiles() {
		return this.nameAcceptedFiles;
	}

}