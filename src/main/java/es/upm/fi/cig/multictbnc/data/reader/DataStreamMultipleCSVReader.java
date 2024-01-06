package es.upm.fi.cig.multictbnc.data.reader;

import com.opencsv.CSVReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.exceptions.VariableNotFoundException;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * The class is designed for reading and processing streaming data from multiple CSV files.
 *
 * @author Carlos Villa Blanco
 */
public class DataStreamMultipleCSVReader {
    static Logger logger = LogManager.getLogger(DataStreamMultipleCSVReader.class);
    String datasetFolder;
    String nameTimeVariable;
    List<String> nameClassVariables;
    HashSet<String> nameVariables;
    List<Integer> indexesVariablesToIgnore;
    File[] files;
    int numReadFiles = 0;

    /**
     * This constructor prepares the reader to process CSV files from the specified folder. The constructor also
     * sorts the CSV files by name and checks for their existence.
     *
     * @param datasetFolder      path to the folder containing the CSV files
     * @param nameTimeVariable   name of the time variable
     * @param nameClassVariables list of names of class variables
     * @throws UnreadDatasetException if the datasets could be not be read as no CSV files were found in the
     *                                specified folder
     * @throws FileNotFoundException  if a file cannot be read
     */
    public DataStreamMultipleCSVReader(String datasetFolder, String nameTimeVariable, List<String> nameClassVariables)
            throws UnreadDatasetException, FileNotFoundException {
        this.datasetFolder = datasetFolder;
        this.nameTimeVariable = nameTimeVariable;
        this.nameClassVariables = nameClassVariables;
        this.nameVariables = new HashSet<String>();
        this.nameVariables.add(nameTimeVariable);
        this.nameVariables.addAll(nameClassVariables);
        this.indexesVariablesToIgnore = new ArrayList<Integer>();
        // Store CSV files from the specified folder
        File folder = new File(datasetFolder);
        this.files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File folder, String name) {
                return name.endsWith(".csv");
            }
        });
        if (this.files == null || this.files.length == 0)
            throw new UnreadDatasetException("No CSV files found in the specified folder");
        logger.info("Generating data stream reader for multiple CSV files in {}", datasetFolder);
        // Order the files by name
        Arrays.sort(this.files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Util.extractInt(f1.toString()) - Util.extractInt(f2.toString());
            }
        });
    }

    /**
     * Checks if there is more data to be read.
     *
     * @return {@code true} is there are data arriving from the data stream, {@code false} otherwise.
     */
    public boolean isDataArriving() {
        return files.length > this.numReadFiles;
    }

    /**
     * Reads a specified number of CSV files from the dataset folder and processes them into a dataset. It
     * detects any new feature variables.
     *
     * @param numFiles number of CSV files to read in the current batch
     * @return dataset containing the data from the read CSV files
     * @throws UnreadDatasetException if an issue occurs while processing the sequences in the files
     * @throws FileNotFoundException  if a file cannot be read
     */
    public Dataset readDataset(int numFiles) throws UnreadDatasetException, FileNotFoundException {
        logger.info("Reading a batch stream of {} CSV files in {}", numFiles, this.datasetFolder);
        Dataset dataset = new Dataset(this.nameTimeVariable, this.nameClassVariables);
        for (int i = numReadFiles; i < numReadFiles + numFiles; i++)
            if (i < this.files.length) {
                List<Entry<String, Integer>> newFeatureVariables = detectNewFeatureVariables(this.files[i]);
                readCSV(dataset, this.files[i], newFeatureVariables);
            } else
                // There are no more files to read
                break;
        // Check if any sequence was added to the dataset
        if (dataset.getNumDataPoints() == 0)
            throw new UnreadDatasetException("Any sequence was successfully processed");
        numReadFiles += numFiles;
        return dataset;
    }

    /**
     * Extracts the names of the variables from a CSV file. It is assumed that the names are in the first row.
     *
     * @param csvFile CSV file
     * @return list of entries, where each entry contains the name of a new feature variable and its index
     * @throws FileNotFoundException if the CSV file was not found
     */
    public List<Entry<String, Integer>> detectNewFeatureVariables(File csvFile) throws FileNotFoundException {
        List<Entry<String, Integer>> newFeatureVariables = new ArrayList<Entry<String, Integer>>();
        if (csvFile.isFile()) {
            FileReader reader = new FileReader(csvFile);
            CSVReader csvReader = new CSVReader(reader);
            try {
                List<String> nameVariablesFile = Arrays.asList(csvReader.readNext());
                // Check that the feature variables are the same, if not there could be a new
                // variable or one was removed
                for (int i = 0; i < nameVariablesFile.size(); i++) {
                    if (!this.nameVariables.contains(nameVariablesFile.get(i))) {
                        logger.info("New feature variable {} received",
                                (nameVariablesFile.get(i)));
                        // Save name of the new feature and its column in the CSV
                        newFeatureVariables.add(Map.entry(nameVariablesFile.get(i), i));
                        // Save the feature variable name
                        this.nameVariables.add(nameVariablesFile.get(i));
                    }
                }
            } catch (NullPointerException | IOException e) {
                throw new FileNotFoundException("Impossible to read CSV file");
            } finally {
                closeReader(csvReader);
            }
        } else {
            throw new FileNotFoundException("Impossible to read CSV file: " + csvFile);
        }

        return newFeatureVariables;
    }

    /**
     * Reads a single CSV file.
     *
     * @param file CSV file
     * @throws UnreadDatasetException if an issue occurs while reading the CSV file
     */
    private void readCSV(Dataset dataset, File file, List<Entry<String, Integer>> newFeatureVariables)
            throws UnreadDatasetException {
        try {
            List<String[]> dataSequence = readCSV(file.getAbsolutePath());
            // If a new feature variable was received, previous sequences in the dataset
            // needs to be updated
            if (!newFeatureVariables.isEmpty() && dataset.getNumDataPoints() > 0) {
                for (Entry<String, Integer> newFeatureVariable : newFeatureVariables) {
                    // If a new feature is detected in the middle of a batch, there are two options.
                    // Add the new feature to the other datasets with a state that do not change.
                    // Divide the batch into two batches.
                    //String fillingState = dataSequence.get(1)[newFeatureVariable.getValue()];
                    dataset.addFeatureVariable(newFeatureVariable.getKey());
                }
            }
            dataset.addSequence(dataSequence, file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new UnreadDatasetException("An error occurred while reading the files of the dataset");
        } catch (VariableNotFoundException e) {
            logger.warn(e.getMessage());
        }
    }

    private void closeReader(Closeable reader) {
        try {
            reader.close();
        } catch (IOException e) {
            logger.error("An error occurred while closing the CSV reader: {}", e.getMessage());
        }
    }

    /**
     * Reads a CSV file.
     *
     * @param pathFile path to the CSV file
     * @return list with the rows (lists of strings) of the CSV
     * @throws VariableNotFoundException if a specified variable was not found in the provided files
     * @throws FileNotFoundException     if the CSV file was not found
     */
    private List<String[]> readCSV(String pathFile) throws VariableNotFoundException, FileNotFoundException {
        List<String[]> list = new ArrayList<String[]>();
        FileReader reader = new FileReader(pathFile);
        CSVReader csvReader = new CSVReader(reader);
        try {
            // Read data of the CSV
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                List<String> row = new LinkedList<String>(Arrays.asList(nextLine));
                // Remove values of the variables to ignore
                for (int index : this.indexesVariablesToIgnore)
                    row.remove(index);
                list.add(row.stream().toArray(String[]::new));
            }
        } catch (IOException e) {
            logger.warn("Impossible to read file {}", pathFile);
        } finally {
            closeReader(csvReader);
        }
        return list;
    }

}
