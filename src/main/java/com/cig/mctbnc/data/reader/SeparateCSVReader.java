package com.cig.mctbnc.data.reader;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.exceptions.VariableNotFoundException;
import com.opencsv.CSVReader;

/**
 * Provides the logic to read separate CSV files. It is possible to find time
 * series data where the sequences are stored in separate CSV files.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class SeparateCSVReader extends AbstractDatasetReader {
	File[] files;
	List<String> nameAcceptedFiles;

	/**
	 * Constructor. Extracts all the csv files from the specified folder.
	 * 
	 * @param datasetFolder      folder path where the csv files are stored
	 * @param nameTimeVariable
	 * @param nameClassVariables
	 * @param excludeVariables
	 */
	public SeparateCSVReader(String datasetFolder, String nameTimeVariable, List<String> nameClassVariables,
			List<String> excludeVariables) {
		super(datasetFolder, nameTimeVariable, nameClassVariables, excludeVariables);
		logger.info("Generating CSV reader for multiples csv files in {}", datasetFolder);
		// Read all csv files from the specified folder
		File folder = new File(datasetFolder);
		files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String name) {
				return name.endsWith(".csv");
			}
		});
		// Order the files by name
		Arrays.sort(files);
	}

	@Override
	public Dataset readDataset() {
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		nameAcceptedFiles = new ArrayList<String>();
		for (File file : files) {
			try {
				List<String[]> dataSequence = readCSV(file.getAbsolutePath());
				dataset.addSequence(dataSequence);
				nameAcceptedFiles.add(file.getName());
			} catch (VariableNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		dataset.removeZeroVarianceFeatures();
		return dataset;
	}

	/**
	 * Reads a CSV file.
	 * 
	 * @param pathFile         path to the CSV file
	 * @param excludeVariables
	 * @return list with the rows (lists of strings) of the CSV
	 * @throws VariableNotFoundException
	 */
	public List<String[]> readCSV(String pathFile) throws VariableNotFoundException {
		List<String[]> list = new ArrayList<String[]>();
		try {
			FileReader reader = new FileReader(pathFile);
			CSVReader csvReader = new CSVReader(reader);
			// If it was specified variables to ignore
			if (excludeVariables.size() > 0) {
				// Obtain name of the variables
				List<String> head = new ArrayList<String>(Arrays.asList(csvReader.readNext()));
				// Obtain the index of the variables to ignore
				List<Integer> indexesToIgnore = new ArrayList<Integer>();
				for (String excludeVariable : excludeVariables) {
					int index = head.indexOf(excludeVariable);
					if (index == -1) {
						// The variable to exclude does not exist in the analysed CSV
						String message = String.format("Variable %s not found in file %s. The file will be ignored.",
								excludeVariable, pathFile);
						throw new VariableNotFoundException(message);
					}
					indexesToIgnore.add(index);
				}
				// List is sorted so last elements of the lists are removed first
				Collections.sort(indexesToIgnore, Collections.reverseOrder());
				// Remove names of the variables to ignore
				for (int index : indexesToIgnore)
					head.remove(index);
				// Add names of the variables to the final list
				list.add(head.stream().toArray(String[]::new));
				// Read data of the csv
				String[] nextLine;
				while ((nextLine = csvReader.readNext()) != null) {
					List<String> row = new LinkedList<String>(Arrays.asList(nextLine));
					// Remove values of the variables to ignore
					for (int index : indexesToIgnore)
						row.remove(index);
					list.add(row.stream().toArray(String[]::new));
				}
			} else {
				list = csvReader.readAll();
			}
			reader.close();
			csvReader.close();
		} catch (IOException e) {
			logger.warn("Impossible to read file {}", pathFile);
		}
		return list;
	}

	@Override
	public void generateTrainAndTest(double trainingSize, boolean shuffle) {
		logger.info("Generating training ({}%) and testing ({}%) datasets (Hold-out testing)", trainingSize * 100,
				(1 - trainingSize) * 100);
		// Obtain entire dataset
		Dataset dataset = readDataset();
		List<Sequence> sequences = dataset.getSequences();		
		if (shuffle) {
			// The sequences are shuffled before splitting into training and testing
			Random rd = new Random(7); 
			Collections.shuffle(sequences, rd);
			Collections.shuffle(nameAcceptedFiles, rd);
		}
		// Define training and testing sequences
		int lastIndexTraining = (int) (trainingSize * sequences.size());
		List<Sequence> trainingSequences = sequences.subList(0, lastIndexTraining);
		List<Sequence> testingSequences = sequences.subList(lastIndexTraining, sequences.size());
		// Define training and testing datasets
		trainingDataset = new Dataset(trainingSequences);
		testingDataset = new Dataset(testingSequences);
		// Set in the datasets the names of the files from which the data was extracted
		trainingDataset.setNameFiles(nameAcceptedFiles.subList(0, lastIndexTraining));
		testingDataset.setNameFiles(nameAcceptedFiles.subList(lastIndexTraining, sequences.size()));
	}

}
